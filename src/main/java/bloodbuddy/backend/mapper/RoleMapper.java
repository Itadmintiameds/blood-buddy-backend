package bloodbuddy.backend.mapper;

import bloodbuddy.backend.dto.master.RoleResponse;
import bloodbuddy.backend.entity.masters.Roles;

/** Maps {@link Roles} entities to their response DTOs. */
public final class RoleMapper {

    private RoleMapper() {
    }

    public static RoleResponse toResponse(Roles role) {
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .isActive(role.getIsActive())
                .build();
    }
}
