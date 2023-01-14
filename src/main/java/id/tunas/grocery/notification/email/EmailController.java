package id.tunas.grocery.notification.email;

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
    public Response requestSendEmail() {
        emailService.requestSendEmail();
        return Response.ok().entity(Map.of()).build();
    }
}