package bloodbuddy.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.socialmessaging.SocialMessagingClient;

/**
 * Builds the AWS End User Messaging Social client used for WhatsApp sends. The client is
 * always constructed (no network call happens here); whether a send actually fires is
 * guarded by {@code aws.whatsapp.*} in {@link bloodbuddy.backend.service.WhatsAppNotificationService}.
 *
 * <p>Uses explicit keys when configured; otherwise falls back to the SDK default chain
 * (env vars, ECS task role, instance profile, ~/.aws), so deployed environments can rely
 * on an IAM task role instead of static keys.
 */
@Configuration
public class AwsWhatsAppConfig {

    private static final String PLACEHOLDER = "dummy";

    @Bean
    public SocialMessagingClient socialMessagingClient(
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            WhatsAppProperties props) {
        return SocialMessagingClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(credentialsProvider(accessKey, secretKey))
                .build();
    }

    private static AwsCredentialsProvider credentialsProvider(String accessKey, String secretKey) {
        if (isSet(accessKey) && isSet(secretKey)) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank() && !PLACEHOLDER.equals(value);
    }
}
