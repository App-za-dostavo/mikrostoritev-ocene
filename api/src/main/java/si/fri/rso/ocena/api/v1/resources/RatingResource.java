package si.fri.rso.ocena.api.v1.resources;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import si.fri.rso.ocena.lib.Rating;
import si.fri.rso.ocena.services.beans.RatingBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/ocena")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@CrossOrigin(supportedMethods = "GET, POST, PUT, HEAD, DELETE, OPTIONS")
public class RatingResource {

    private Logger log = Logger.getLogger(RatingResource.class.getName());

    @Inject
    private RatingBean ratingBean;

    @Context
    protected UriInfo uriInfo;

    @Operation(description = "Get all ratings metadata.", summary = "Get all metadata")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "List of rating metadata",
                    content = @Content(schema = @Schema(implementation = Rating.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Number of objects in list")}
            )})
    @GET
    public Response getRating() {

        List<Rating> rating = ratingBean.getRatingsFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(rating).build();
    }

    @Operation(description = "Get metadata for 1 rating.", summary = "Get metadata for 1 rating by its id number.")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Rating metadata",
                    content = @Content(
                            schema = @Schema(implementation = Rating.class))
            )})
    @GET
    @Path("/{id}")
    public Response getRating(@Parameter(description = "Metadata ID.", required = true)
                                  @PathParam("id") Integer id) {

        Rating rating = ratingBean.getRatings(id);

        if (rating == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(rating).build();
    }

    @Operation(description = "Add rating metadata.", summary = "Add metadata")
    @APIResponses({
            @APIResponse(responseCode = "201",
                    description = "Metadata successfully added."
            ),
            @APIResponse(responseCode = "405", description = "Validation error .")
    })
    @POST
    public Response createRating(@RequestBody(
            description = "DTO object with rating metadata.",
            required = true, content = @Content(
            schema = @Schema(implementation = Rating.class))) Rating rating) {

        if (rating.getDeliveryPerson() == null || rating.getStars() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            if (rating.getRatingsCount() == null) {
                rating.setRatingsCount(1);
            }
            rating = ratingBean.createRating(rating);
        }

        return Response.status(Response.Status.CONFLICT).entity(rating).build();
    }

    @Operation(description = "Update metadata for 1 rating.", summary = "Update metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Metadata successfully updated."
            )
    })
    @PUT
    @Path("{id}")
    public Response putRating(@Parameter(description = "Metadata ID.", required = true)
                                  @PathParam("id") Integer id,
                              @RequestBody(
                                      description = "DTO object with rating metadata.",
                                      required = true, content = @Content(
                                      schema = @Schema(implementation = Rating.class))) Rating rating) {

        rating = ratingBean.putRating(id, rating);

        if (rating == null) {
            return Response.status(Response.Status.NOT_MODIFIED).build();
        }
        return Response.status(Response.Status.OK).entity(rating).build();
    }

    @Operation(description = "Delete metadata for 1 rating.", summary = "Delete metadata")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Metadata successfully deleted."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Not found."
            )
    })
    @DELETE
    @Path("{id}")
    public Response deleteRating(@Parameter(description = "Metadata ID.", required = true)
                                     @PathParam("id") Integer id) {

        boolean deleted = ratingBean.deleteRating(id);

        if (deleted) {
            return  Response.status(Response.Status.OK).build();
        } else return Response.status(Response.Status.NOT_FOUND).build();
    }
}

