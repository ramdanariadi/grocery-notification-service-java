package id.tunas.grocery.notification.email;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.vertx.core.json.JsonObject;
import org.apache.kafka.common.utils.Time;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class EmailService {
    private final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Inject
    @Channel("send-email-request")
    Emitter<String> requestSendEmailEmitter;

    @ConfigProperty(name = "aws.ses.email.sender")
    Optional<String> sender;

    @ConfigProperty(name = "aws.ses.region")
    Optional<String> awsRegion;

    private SesV2Client sesV2Client;

    @Inject
    @Location("email-verification.html")
    Template template;

    void onStart(@Observes StartupEvent ev){
        sesV2Client = SesV2Client.builder().region(Region.of(awsRegion.orElse(""))).build();
        LOGGER.info("start {}", Time.SYSTEM);
    }

    @Incoming("send-email")
    @Blocking
    public void sendEmail(String request) throws URISyntaxException, IOException {
        JsonObject sendRequest = new JsonObject(request);
        String to = sendRequest.getString("to");
        String subject = sendRequest.getString("sub");
        String message = sendRequest.getString("message");
        String html = template.data("message", message).render();
        LOGGER.info("template {}", html);
        Destination destination = Destination.builder().toAddresses(to).build();
        Content sub = Content.builder().data(subject).build();
        Body body = Body.builder().html(builder -> builder.data(html)).build();
        Message msg = Message.builder().subject(sub).body(body).build();
        EmailContent content = EmailContent.builder().simple(msg).build();
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(content)
                .fromEmailAddress(sender.orElse(""))
                .build();
        SendEmailResponse sendEmailResponse = sesV2Client.sendEmail(sendEmailRequest);
        LOGGER.info("sendEmailResponse {}", sendEmailResponse.messageId());
        LOGGER.info("sender {}", sendRequest.getString("sender"));
    }

    public void requestSendEmail(JsonObject body){
        requestSendEmailEmitter.send(body.encode());
    }

    @Scheduled(every = "10s")
    public void log(){
        LOGGER.info("time {}", LocalDateTime.now().getSecond());
    }
}
