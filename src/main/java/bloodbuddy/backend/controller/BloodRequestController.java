package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.request.BloodRequestResponse;
import bloodbuddy.backend.dto.request.BloodRequestSubmission;
import bloodbuddy.backend.service.BloodRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/blood-requests")
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;

    public BloodRequestController(BloodRequestService bloodRequestService) {
        this.bloodRequestService = bloodRequestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BloodRequestResponse>> submit(
            @Valid @RequestBody BloodRequestSubmission submission) {
        BloodRequestResponse response = bloodRequestService.submit(submission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Blood request submitted", response));
    }
}
