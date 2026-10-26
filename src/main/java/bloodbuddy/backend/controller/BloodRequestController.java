package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.common.ConsentFormResponse;
import bloodbuddy.backend.dto.request.BloodRequestResponse;
import bloodbuddy.backend.dto.request.BloodRequestSubmission;
import bloodbuddy.backend.enums.ConsentType;
import bloodbuddy.backend.service.BloodRequestService;
import bloodbuddy.backend.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/blood-requests")
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final ConsentService consentService;

    public BloodRequestController(BloodRequestService bloodRequestService, ConsentService consentService) {
        this.bloodRequestService = bloodRequestService;
        this.consentService = consentService;
    }

    /** Consent wording the request form must display; its version is sent back on submit. */
    @GetMapping("/consent")
    public ResponseEntity<ApiResponse<ConsentFormResponse>> consent() {
        return ResponseEntity.ok(ApiResponse.success("Recipient consent form", consentService.getActive(ConsentType.RECIPIENT)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BloodRequestResponse>> submit(
            @Valid @RequestBody BloodRequestSubmission submission) {
        BloodRequestResponse response = bloodRequestService.submit(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blood request submitted", response));
    }
}
