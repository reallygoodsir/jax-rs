package com.really.good.sir.app.resources;

import com.really.good.sir.app.models.Book;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

@Path("/books")
public class BookResource {
    private final List<Book> books = new ArrayList<>();

    {
        books.add(new Book(1, "Java 21"));
        books.add(new Book(2, "Java 23"));
    }

    @GET
    public List<Book> getAllBooks() {
        return books;
    }

    @GET
    @Produces(value = "application/json")
    @Path("/{id}")
    public Book findBook(@PathParam(value = "id") Integer id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
