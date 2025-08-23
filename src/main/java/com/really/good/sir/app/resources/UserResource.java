package com.really.good.sir.app.resources;

import com.really.good.sir.app.models.User;

import javax.ws.rs.*;
import java.util.*;

@Path("/users")
public class UserResource {
    private static final Map<Integer, User> users = new HashMap<>();

    static {
        User user1 = new User();
        user1.setName("mike1");
        User user2 = new User();
        user2.setName("mike2");
        users.put(1, user1);
        users.put(2, user2);
    }

    @GET
    @Produces(value = "application/json")
    public Collection<User> getAll() {
        return users.values();
    }

    @GET
    @Produces(value = "application/json")
    @Path(value = "/{id}")
    public User getById(@PathParam(value = "id") Integer userId) {
        return users.get(userId);
    }

    @POST
    @Consumes(value = "application/json")
    public Integer create(User user) {
        users.put(user.getId(), user);
        return user.getId();
    }

    @PUT
    @Consumes(value = "application/json")
    public Integer update(User user) {
        users.put(user.getId(), user);
        return user.getId();
    }

    @DELETE
    @Path(value = "/{id}")
    public User deleteById(@PathParam(value = "id") Integer userId) {
        return users.remove(userId);
    }
}

