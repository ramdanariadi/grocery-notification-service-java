package id.tunas.grocery.notification.email;

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
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.SesV2ClientBuilder;
import software.amazon.awssdk.services.sesv2.model.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
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

    private SesV2Client sesV2Client;

    void onStart(@Observes StartupEvent ev){
        LOGGER.info("start {}", Time.SYSTEM);
    }

    @Incoming("send-email")
    @Blocking
    public void sendEmail(String request){
        JsonObject sendRequest = new JsonObject(request);
        String to = sendRequest.getString("to");
        String subject = sendRequest.getString("sub");
        String message = sendRequest.getString("message");
        Destination destination = Destination.builder().toAddresses(to).build();
        Content sub = Content.builder().data(subject).build();
        Body body = Body.builder().text(builder -> builder.data(message)).build();
        Message msg = Message.builder().subject(sub).body(body).build();
        EmailContent content = EmailContent.builder().simple(msg).build();
        SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(content)
                .fromEmailAddress(sender.orElse(""))
                .build();

        LOGGER.info("sender {}", sendRequest.getString("sender"));
    }

    public void requestSendEmail(){
        JsonObject emailRequest = new JsonObject();
        emailRequest.put("sender","foo");
        requestSendEmailEmitter.send(emailRequest.encode());
    }

    @Scheduled(every = "10s")
    public void log(){
        LOGGER.info("time {}", LocalDateTime.now().getSecond());
    }
}
