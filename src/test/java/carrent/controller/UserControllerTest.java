package carrent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carrent.dto.user.UserInfoResponseDto;
import carrent.dto.user.UserRoleUpdateRequestDto;
import carrent.dto.user.UserUpdateRequestDto;
import carrent.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    protected static MockMvc mockMvc;
    private static final String BASE_ENDPOINT = "/users";
    private static final String SAMPLE_USER_EMAIL = "paul@customer.com";
    private static final String SAMPLE_FIRST_NAME = "Paul";
    private static final String SAMPLE_LAST_NAME = "Walker";
    private static final String SAMPLE_CUSTOMER_ROLE_NAME = Role.RoleName.CUSTOMER.name();
    private static final String SAMPLE_MANAGER_ROLE_NAME = Role.RoleName.MANAGER.name();

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/user/remove-all-users-with-roles.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/user/add-two-users-with-roles.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/user/remove-all-users-with-roles.sql"));
        }
    }

    @WithUserDetails(SAMPLE_USER_EMAIL)
    @Test
    void getProfileInfo_Valid_ReturnsUserInfoResponseDto() throws Exception {
        UserInfoResponseDto expected = createTestUserInfoResponse(
                SAMPLE_FIRST_NAME, SAMPLE_LAST_NAME, SAMPLE_CUSTOMER_ROLE_NAME);

        MvcResult result = mockMvc.perform(get(BASE_ENDPOINT + "/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserInfoResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserInfoResponseDto.class);

        assertEquals(expected, actual);
    }

    @WithUserDetails(SAMPLE_USER_EMAIL)
    @Test
    void updateProfileInfo_ValidRequestDto_ReturnsUserInfoResponseDto() throws Exception {
        String newFirstName = "Jack";
        String newLastName = "Sparrow";
        UserUpdateRequestDto request = new UserUpdateRequestDto(newFirstName, newLastName);
        UserInfoResponseDto expected = createTestUserInfoResponse(
                request.firstName(), request.lastName(), SAMPLE_CUSTOMER_ROLE_NAME);
        String jsonRequest = objectMapper.writeValueAsString(expected);

        MvcResult result = mockMvc.perform(patch(BASE_ENDPOINT + "/me")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserInfoResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserInfoResponseDto.class);

        assertEquals(expected, actual);
    }

    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @Sql(scripts = {
            "classpath:database/user/remove-all-users-with-roles.sql",
            "classpath:database/user/add-two-users-with-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void updateUserRoles_ValidUserIdAndRequest_ReturnsUserInfoResponseDto() throws Exception {
        Long validUserId = 2L;
        UserRoleUpdateRequestDto request =
                new UserRoleUpdateRequestDto(Set.of(SAMPLE_MANAGER_ROLE_NAME));
        UserInfoResponseDto expected = createTestUserInfoResponse(
                SAMPLE_FIRST_NAME, SAMPLE_LAST_NAME, SAMPLE_MANAGER_ROLE_NAME);
        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(put(BASE_ENDPOINT + "/" + validUserId + "/role")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserInfoResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserInfoResponseDto.class);

        assertEquals(expected, actual);
    }

    private UserInfoResponseDto createTestUserInfoResponse(
            String firstName, String lastName, String roleName) {
        return new UserInfoResponseDto(
                SAMPLE_USER_EMAIL,
                firstName,
                lastName,
                Set.of(roleName));
    }
}
