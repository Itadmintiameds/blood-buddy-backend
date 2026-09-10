package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.donor.DonorRegistrationRequest;
import bloodbuddy.backend.dto.donor.DonorResponse;
import bloodbuddy.backend.service.DonorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/donors")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DonorResponse>> register(@Valid @RequestBody DonorRegistrationRequest request) {
        DonorResponse response = donorService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Donor registered successfully", response));
    }
}
