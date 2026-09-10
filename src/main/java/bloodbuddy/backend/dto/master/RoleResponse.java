package bloodbuddy.backend.dto.master;

import bloodbuddy.backend.entity.masters.Roles;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private final Long roleId;
    private final String roleName;
    private final Boolean isActive;

    public static RoleResponse fromEntity(Roles role) {
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .isActive(role.getIsActive())
                .build();
    }
}
