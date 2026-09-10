package bloodbuddy.backend.security;

import bloodbuddy.backend.entity.Users;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String role;
    private final Long bloodCentreId;
    private final boolean active;

    public CustomUserDetails(Long userId, String username, String password,
                             String role, Long bloodCentreId, boolean active) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.bloodCentreId = bloodCentreId;
        this.active = active;
    }

    /** Built during login from the persisted user (carries the BCrypt hash). */
    public static CustomUserDetails fromEntity(Users user) {
        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole() != null ? user.getRole().getRoleName() : null,
                user.getBloodCentre() != null ? user.getBloodCentre().getBloodCentreId() : null,
                Boolean.TRUE.equals(user.getIsActive()));
    }

    /** Rebuilt on each request straight from verified JWT claims (no password). */
    public static CustomUserDetails fromClaims(Long userId, String username, String role, Long bloodCentreId) {
        return new CustomUserDetails(userId, username, null, role, bloodCentreId, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
