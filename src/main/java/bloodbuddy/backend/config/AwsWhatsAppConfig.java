package bloodbuddy.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.socialmessaging.SocialMessagingClient;

/**
 * Builds the AWS End User Messaging Social client used for WhatsApp sends. The client is
 * always constructed (no network call happens here); whether a send actually fires is
 * guarded by {@code aws.whatsapp.*} in {@link bloodbuddy.backend.service.WhatsAppNotificationService}.
 */
@Configuration
public class AwsWhatsAppConfig {

    @Bean
    public SocialMessagingClient socialMessagingClient(
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            WhatsAppProperties props) {
        return SocialMessagingClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
