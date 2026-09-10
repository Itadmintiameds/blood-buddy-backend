package bloodbuddy.backend.controller;

import bloodbuddy.backend.common.ApiResponse;
import bloodbuddy.backend.dto.master.BloodComponentResponse;
import bloodbuddy.backend.dto.master.BloodGroupResponse;
import bloodbuddy.backend.dto.master.RoleResponse;
import bloodbuddy.backend.service.MasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public read-only master/reference data for UI dropdowns (never created via the app). */
@RestController
@RequestMapping("/public/masters")
public class MasterController {

    private final MasterService masterService;

    public MasterController(MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping("/blood-groups")
    public ResponseEntity<ApiResponse<List<BloodGroupResponse>>> bloodGroups() {
        return ResponseEntity.ok(ApiResponse.success("Blood groups fetched", masterService.getBloodGroups()));
    }

    @GetMapping("/blood-components")
    public ResponseEntity<ApiResponse<List<BloodComponentResponse>>> bloodComponents() {
        return ResponseEntity.ok(ApiResponse.success("Blood components fetched", masterService.getBloodComponents()));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> roles() {
        return ResponseEntity.ok(ApiResponse.success("Roles fetched", masterService.getRoles()));
    }
}
