package org.acme.hibernate.reactive;

import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.reactive.mutiny.Mutiny;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.mutiny.Uni;

@Path("fruits")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class FruitMutinyResource {
    private static final Logger LOGGER = Logger.getLogger(FruitMutinyResource.class);

    @Inject
    Mutiny.SessionFactory sf;

    @GET
    public Uni<List<Fruit>> get() {
        return sf.withTransaction((s,t) -> s
                .createNamedQuery("Fruits.findAll", Fruit.class)
                .getResultList()
        );
    }

    @GET
    @Path("{id}")
    public Uni<Fruit> getSingle(@RestPath Integer id) {
        return sf.withTransaction((s,t) -> s.find(Fruit.class, id));
    }

    @POST
    public Uni<Response> create(Fruit fruit) {
        if (fruit == null || fruit.getId() != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return sf.withTransaction((s,t) -> s.persist(fruit))
                .replaceWith(() -> Response.ok(fruit).status(CREATED).build());
    }
}
