package bloodbuddy.backend.service;

import bloodbuddy.backend.dto.master.BloodComponentResponse;
import bloodbuddy.backend.dto.master.BloodGroupResponse;
import bloodbuddy.backend.dto.master.RoleResponse;
import bloodbuddy.backend.repository.BloodComponentsRepository;
import bloodbuddy.backend.repository.BloodGroupRepository;
import bloodbuddy.backend.repository.RolesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MasterService {

    private final BloodGroupRepository bloodGroupRepository;
    private final BloodComponentsRepository bloodComponentsRepository;
    private final RolesRepository rolesRepository;

    public MasterService(BloodGroupRepository bloodGroupRepository,
                         BloodComponentsRepository bloodComponentsRepository,
                         RolesRepository rolesRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.bloodComponentsRepository = bloodComponentsRepository;
        this.rolesRepository = rolesRepository;
    }

    @Transactional(readOnly = true)
    public List<BloodGroupResponse> getBloodGroups() {
        return bloodGroupRepository.findAll().stream()
                .map(BloodGroupResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BloodComponentResponse> getBloodComponents() {
        return bloodComponentsRepository.findAll().stream()
                .map(BloodComponentResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return rolesRepository.findAll().stream()
                .map(RoleResponse::fromEntity)
                .toList();
    }
}
