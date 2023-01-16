package id.tunas.grocery.notification.email;

import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Consumes(MediaType.APPLICATION_JSON)
@Path("/v1/email")
public class EmailController {

    @Inject
    EmailService emailService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestSendEmail(JsonObject body) {
        emailService.requestSendEmail(body);
        return Response.ok().entity(Map.of()).build();
    }
}