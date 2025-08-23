package com.really.good.sir;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherServlet extends HttpServlet {
    private Map<String, RequestHandler> routeMappings = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(ServletConfig config) {
        final String packageName = config.getInitParameter("scan-package");
        try {
            final List<Class<?>> classes = findClasses(packageName);
            routeMappings = createRouteMapping(classes);
            System.out.println();
            for (Map.Entry<String, RequestHandler> entry : routeMappings.entrySet()) {
                System.out.println("KEY [ " + entry.getKey() + " ]       VALUE " + entry.getValue());
            }
            System.out.println();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void service(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String path = req.getPathInfo();
        final String httpMethod = req.getMethod();

        RequestHandler handler = null;
        Matcher matched = null;

        for (Map.Entry<String, RequestHandler> entry : routeMappings.entrySet()) {
            final String[] keyParts = entry.getKey().split(" ", 2);
            final String methodType = keyParts[0];
            final String regex = keyParts[1];

            if (!httpMethod.equalsIgnoreCase(methodType)) {
                continue;
            }

            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                handler = entry.getValue();
                matched = matcher;
                break;
            }
        }

        final PrintWriter writer = resp.getWriter();

        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.println("{\"error\":\"Not Found: " + httpMethod + " " + path + "\"}");
            return;
        }

        // Check Content-Type for methods with body (POST, PUT)
        if ((httpMethod.equals("POST") || httpMethod.equals("PUT")) && !handler.consumes.isEmpty()) {
            String contentType = req.getContentType();
            if (contentType == null || handler.consumes.stream().noneMatch(contentType::contains)) {
                resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE); // 415
                writer.println("{\"error\":\"Unsupported Media Type: " + contentType + "\"}");
                return;
            }
        }

        // Check Accept header for response
        final String acceptHeader = req.getHeader("Accept");
        if (!handler.produces.isEmpty()) {
            if (acceptHeader == null || handler.produces.stream().noneMatch(acceptHeader::contains)) {
                resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE); // 406
                writer.println("{\"error\":\"Not Acceptable: " + acceptHeader + "\"}");
                return;
            }
        }

        resp.setContentType(handler.produces.isEmpty() ? "application/json" : handler.produces.get(0));

        try {
            Object result;
            Parameter[] params = handler.method.getParameters();
            Object[] args = new Object[params.length];

            for (int i = 0; i < params.length; i++) {
                if (params[i].isAnnotationPresent(PathParam.class)) {
                    String value = matched.group(1);
                    Class<?> type = params[i].getType();
                    if (type == Integer.class || type == int.class) {
                        args[i] = Integer.valueOf(value);
                    } else {
                        args[i] = value;
                    }
                } else if (!handler.consumes.isEmpty()) {
                    Class<?> paramType = params[i].getType();
                    args[i] = objectMapper.readValue(req.getInputStream(), paramType);
                }
            }

            result = handler.method.invoke(handler.instance, args);

            if (result != null) {
                final String json = objectMapper.writeValueAsString(result);
                writer.println(json);
            } else {
                writer.println("{}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(writer);
        }
    }

    private List<Class<?>> findClasses(String packageName) throws Exception {
        final List<Class<?>> classes = new ArrayList<>();
        final String newPackageName = packageName.replace('.', '/');
        final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(newPackageName);
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            final File directory = new File(url.toURI());
            for (String fileName : Objects.requireNonNull(directory.list())) {
                if (fileName.endsWith(".class")) {
                    final String className = packageName + "." + fileName.replace(".class", "");
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }

    private Map<String, RequestHandler> createRouteMapping(final List<Class<?>> classes) throws Exception {
        final Map<String, RequestHandler> result = new HashMap<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Path.class)) {
                final Path classPath = clazz.getAnnotation(Path.class);
                final String baseUrl = classPath.value();
                final Object instance = clazz.getDeclaredConstructor().newInstance();

                for (Method method : clazz.getDeclaredMethods()) {
                    String httpMethod = null;

                    if (method.isAnnotationPresent(GET.class)) httpMethod = "GET";
                    else if (method.isAnnotationPresent(POST.class)) httpMethod = "POST";
                    else if (method.isAnnotationPresent(PUT.class)) httpMethod = "PUT";
                    else if (method.isAnnotationPresent(DELETE.class)) httpMethod = "DELETE";

                    if (httpMethod != null) {
                        String fullUrl = baseUrl;
                        if (method.isAnnotationPresent(Path.class)) {
                            final Path path = method.getAnnotation(Path.class);
                            fullUrl = baseUrl + path.value();
                        }

                        final String regex = "^" + fullUrl.replaceAll("\\{[^/]+}", "([^/]+)") + "$";

                        // Capture @Consumes and @Produces for the method
                        Consumes consumes = method.getAnnotation(Consumes.class);
                        Produces produces = method.getAnnotation(Produces.class);

                        final String key = httpMethod + " " + regex;
                        result.put(key,
                                new RequestHandler(instance, method,
                                        consumes != null ? Arrays.asList(consumes.value()) : Collections.emptyList(),
                                        produces != null ? Arrays.asList(produces.value()) : Collections.emptyList()
                                ));
                    }
                }
            }
        }
        return result;
    }
}
