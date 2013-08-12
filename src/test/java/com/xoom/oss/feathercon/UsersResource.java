package com.xoom.oss.feathercon;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {
    @GET
    public Response getUser() {
        return Response.ok(new User("Bob Loblaw", "bob@lawbomb.example.com")).build();
    }
}


