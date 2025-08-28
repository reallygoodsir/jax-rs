# Tomcat & Jersey Request Flow

## Tomcat Startup

1. **Parse `web.xml`**
    - Tomcat reads the deployment descriptor (`web.xml`).

2. **Servlet Creation**
    - If Tomcat sees a `load-on-startup` configuration with value `1`, it creates an instance of the Jersey servlet defined in `web.xml`.

3. **Initialization**
    - Tomcat calls the `init()` method of the Jersey servlet.
    - During `init()`, the Jersey framework scans the configured package.

4. **Annotation Scanning & Route Mapping**
    - All classes are scanned.
    - Classes with JAX-RS annotations (e.g., `@Path`, `@QueryParam`) are discovered.
    - A **route mapping** is created:
        - **URL → Class + Method** (e.g., `/users → UserResource.getAllUsers`).
    - The mapping is stored for processing future requests.

---

## Tomcat Running (Request Handling)

1. **Client Request**
    - A client sends an HTTP request to the server.

2. **Request & Response Objects**
    - Tomcat creates:
        - `HttpServletRequest` (populated with request data).
        - `HttpServletResponse`.

3. **Servlet Dispatch**
    - Tomcat calls Jersey `ServletContainer`’s `service()` method, passing `HttpServletRequest` and `HttpServletResponse`.

4. **Route Resolution**
    - Jersey `ServletContainer` uses the pre-built route mapping to determine which class and method should handle the request.

5. **Parameter Binding**
    - Data extracted from `HttpServletRequest` is mapped to method parameters.

6. **Method Execution & Response Building**
    - The target method executes and returns a result.
    - Jersey converts the response into JSON (based on `@Produces`).

7. **HTTP Response**
    - Jersey inserts the JSON result into `HttpServletResponse`.
    - Tomcat sends the finalized HTTP response back to the client.

8. **Client Receives Response**
    - The client processes the response.
