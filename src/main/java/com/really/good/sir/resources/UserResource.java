package com.really.good.sir.resources;

import com.really.good.sir.models.User;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Map<Integer, User> users = new HashMap<>();

    static {
        users.put(1, new User(1, "Alice", "alice@example.com"));
        users.put(2, new User(2, "Bob", "bob@example.com"));
    }

    // GET all users
    @GET
    public Collection<User> getAllUsers(
            @QueryParam("limit") @DefaultValue("10") int limit,
            @MatrixParam("role") String role,
            @HeaderParam("X-Request-ID") String requestId,
            @CookieParam("sessionId") Cookie sessionCookie,
            @Context UriInfo uriInfo) {

        System.out.println("limit " + limit);
        System.out.println("role " + role);

        System.out.println("RequestId: " + requestId);
        System.out.println("Session cookie: " + (sessionCookie != null ? sessionCookie.getValue() : "none"));
        System.out.println("Base URI: " + uriInfo.getBaseUri());
        System.out.println("Path: " + uriInfo.getPath());


        return users.values().stream().limit(limit).toList();
    }

    // GET one user
    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") int id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    // CREATE user
    @POST
    public Response createUser(User user, @Context UriInfo uriInfo) {
        users.put(user.getId(), user);
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Integer.toString(user.getId()));
        return Response.created(builder.build()).entity(user).build();
    }

    // UPDATE user
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") int id, User updatedUser) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User not found");
        }
        updatedUser.setId(id);
        users.put(id, updatedUser);
        return Response.ok(updatedUser).build();
    }

    // DELETE user
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id) {
        User removed = users.remove(id);
        if (removed == null) {
            throw new NotFoundException("User not found");
        }
        return Response.noContent().build();
    }

    // HEAD (check if user exists)
    @HEAD
    @Path("/{id}")
    public Response headUser(@PathParam("id") int id) {
        if (users.containsKey(id)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    // OPTIONS (return allowed methods)
    @OPTIONS
    public Response options() {
        return Response.ok()
                .allow("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
                .build();
    }
}

