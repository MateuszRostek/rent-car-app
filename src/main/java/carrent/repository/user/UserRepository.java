package carrent.repository.user;

import carrent.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("from User u join fetch u.roles where lower(u.email) like lower(:email)")
    Optional<User> findByEmail(String email);
}
