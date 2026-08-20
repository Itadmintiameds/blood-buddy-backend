package com.bloodbuddy.services;

import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioOtpService {

    @Value("${twilio.verify.service-sid}")
    private String serviceSid;

    // SEND OTP
    public String sendOtp(String mobileNumber) {

        System.out.println("Sending OTP to: " + mobileNumber);
        System.out.println("Verify Service SID: " + serviceSid);

        Verification verification =
                Verification
                        .creator(
                                serviceSid,
                                mobileNumber,
                                "sms"
                        )
                        .create();

        String status = verification.getStatus();

        System.out.println("OTP status: " + status);

        return status;
    }

    // VERIFY OTP
    public boolean verifyOtp(String mobileNumber, String otp) {

        System.out.println("Verifying OTP for: " + mobileNumber);

        VerificationCheck verificationCheck =
                VerificationCheck
                        .creator(serviceSid)
                        .setTo(mobileNumber)
                        .setCode(otp)
                        .create();

        String status = verificationCheck.getStatus();

        System.out.println("OTP verification status: " + status);

        return "approved".equalsIgnoreCase(status);
    }
}