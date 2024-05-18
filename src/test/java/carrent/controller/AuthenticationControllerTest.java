package carrent.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carrent.dto.user.UserLoginRequestDto;
import carrent.dto.user.UserLoginResponseDto;
import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {
    protected static MockMvc mockMvc;
    private static final String BASE_ENDPOINT = "/auth";
    private static final String EVERY_TOKEN_START = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ";

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
                    new ClassPathResource("database/user/remove-all-users-with-roles.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/user/add-two-users-with-roles.sql"));
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
                    new ClassPathResource("database/user/remove-all-users-with-roles.sql"));
        }
    }

    @Test
    @Sql(scripts = {"classpath:database/user/remove-created-user.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void registerUser_ValidUserRegistrationRequestDto_ReturnsUserRegistrationResponseDto()
            throws Exception {
        UserRegistrationRequestDto requestDto = createTestRegistrationRequestDto();
        UserRegistrationResponseDto expected = createTestRegistrationResponseDto(requestDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post(BASE_ENDPOINT + "/registration")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        UserRegistrationResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserRegistrationResponseDto.class);

        assertNotNull(actual.id());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    void loginUser_ValidUserLoginRequestDto_ReturnsUserLoginResponseDto() throws Exception {
        UserLoginRequestDto requestDto = createTestLoginRequest();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post(BASE_ENDPOINT + "/login")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserLoginResponseDto.class);

        assertNotNull(actual);
        assertTrue(actual.token().startsWith(EVERY_TOKEN_START));
    }

    private UserLoginRequestDto createTestLoginRequest() {
        return new UserLoginRequestDto(
                "paul@customer.com",
                "12345678");
    }

    private UserRegistrationRequestDto createTestRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                "basic@valid.com",
                "basicpassword123",
                "basicpassword123",
                "Basic First Name",
                "Basic Last Name"
        );
    }

    private UserRegistrationResponseDto createTestRegistrationResponseDto(
            UserRegistrationRequestDto requestDto) {
        return new UserRegistrationResponseDto(
                1L,
                requestDto.email(),
                requestDto.firstName(),
                requestDto.lastName());
    }
}
