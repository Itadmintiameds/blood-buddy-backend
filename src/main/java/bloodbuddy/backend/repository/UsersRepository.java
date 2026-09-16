package bloodbuddy.backend.repository;

import bloodbuddy.backend.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByUsername(String username);

    Optional<Users> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Eager-fetch role and centre so authorities/centre-id are available outside
    // the load transaction (login + token generation).
    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.role LEFT JOIN FETCH u.bloodCentre WHERE u.username = :username")
    Optional<Users> findByUsernameWithDetails(@Param("username") String username);
}
