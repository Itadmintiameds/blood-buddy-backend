package bloodbuddy.backend.dto.master;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private final Long roleId;
    private final String roleName;
    private final Boolean isActive;
}
