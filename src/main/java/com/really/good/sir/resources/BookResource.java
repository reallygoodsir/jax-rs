package com.really.good.sir.resources;

import com.really.good.sir.models.Book;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {
    private static final Map<String, Book> books = new HashMap<>();

    @GET
    public Response getAllBooks() {
        return Response.ok(books.values()).build();
    }

    @GET
    @Path("/{bookId}")
    public Response getBook(@PathParam("bookId") final String bookId) {
        if (!books.containsKey(bookId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book id not found")
                    .build();
        }
        return Response.ok(books.get(bookId)).build();
    }

    @POST
    public Response createBook(final Book book, @Context final UriInfo urlInfo) {
        final String bookId = UUID.randomUUID().toString();
        book.setId(bookId);
        books.put(bookId, book);
        Response response = Response.created(urlInfo.getAbsolutePathBuilder().path(bookId).build())
                .entity(book)
                .build();
        return response;
    }

    @PUT
    public Response updateBook(final Book book, @Context final UriInfo urlInfo) {
        if (!books.containsKey(book.getId())) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book id not found")
                    .build();
        }
        books.put(book.getId(), book);
        return Response.ok(book).build();

    }

    @DELETE
    @Path("/{bookId}")
    public Response deleteBook(@PathParam("bookId") final String bookId) {
        if (!books.containsKey(bookId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book id not found")
                    .build();
        }
        books.remove(bookId);
        return Response.noContent().build();

    }
}
