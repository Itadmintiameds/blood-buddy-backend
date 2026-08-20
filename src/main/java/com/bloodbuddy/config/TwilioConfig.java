package com.bloodbuddy.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.verify.service-sid}")
    private String serviceSid;

    @PostConstruct
    public void init() {
        System.out.println("Twilio Auth Token present: "
                + (authToken != null && !authToken.isBlank()));
        System.out.println("Twilio Service SID: " + serviceSid);

        Twilio.init(accountSid, authToken);
    }
}