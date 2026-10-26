package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.common.ConsentFormResponse;
import bloodbuddy.backend.dto.donor.DonorRegistrationRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.enums.ConsentType;
import bloodbuddy.backend.service.ConsentService;
import bloodbuddy.backend.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/donors")
public class DonorController {

    private final DonorService donorService;
    private final ConsentService consentService;

    public DonorController(DonorService donorService, ConsentService consentService) {
        this.donorService = donorService;
        this.consentService = consentService;
    }

    /** Consent wording the registration form must display; its version is sent back on register. */
    @GetMapping("/consent")
    public ResponseEntity<ApiResponse<ConsentFormResponse>> consent() {
        return ResponseEntity.ok(ApiResponse.success("Donor consent form", consentService.getActive(ConsentType.DONOR)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DonorResponse>> register(@Valid @RequestBody DonorRegistrationRequest request) {
        DonorResponse response = donorService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Donor registered successfully", response));
    }
}
