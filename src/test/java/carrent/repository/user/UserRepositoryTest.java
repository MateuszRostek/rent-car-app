package carrent.repository.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import carrent.model.Role;
import carrent.model.User;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    private static final String VALID_EMAIL = "john@manager.com";
    private static final String INVALID_EMAIL = "bob@random.com";
    private static final String REMOVE_ALL_USERS_WITH_ROLES_PATH =
            "classpath:database/user/remove-all-users-with-roles.sql";
    private static final String ADD_TWO_USERS_WITH_ROLES_PATH =
            "classpath:database/user/add-two-users-with-roles.sql";
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find a User by valid email")
    @Sql(scripts = {REMOVE_ALL_USERS_WITH_ROLES_PATH, ADD_TWO_USERS_WITH_ROLES_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByEmail_ValidEmail_ReturnsOptionalWithCorrectUser() {
        User expected = getValidManagerUser();
        Optional<User> actual = userRepository.findByEmail(VALID_EMAIL);
        assertFalse(actual.isEmpty());
        assertThat(actual.get()).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("Find a User by invalid email")
    @Sql(scripts = {REMOVE_ALL_USERS_WITH_ROLES_PATH, ADD_TWO_USERS_WITH_ROLES_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findByEmail_InvalidEmail_ReturnsEmptyOptional() {
        Optional<User> expected = Optional.empty();
        Optional<User> actual = userRepository.findByEmail(INVALID_EMAIL);
        assertEquals(expected, actual);
    }

    private User getValidManagerUser() {
        Role expectedRole = new Role();
        expectedRole.setId(2L);
        expectedRole.setName(Role.RoleName.MANAGER);
        User expected = new User();
        expected.setId(1L);
        expected.setEmail(VALID_EMAIL);
        expected.setFirstName("John");
        expected.setLastName("Jackson");
        expected.setPassword("$2a$10$2UWH5EMjHJGwl1JbzyXd1uG1OS7W1pmOhWQXcF9nFByYM7aGUhlS6");
        expected.setRoles(Set.of(expectedRole));
        return expected;
    }
}
