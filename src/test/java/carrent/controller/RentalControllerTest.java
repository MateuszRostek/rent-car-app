package carrent.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carrent.dto.car.CarDto;
import carrent.dto.rental.BasicRentalDto;
import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.model.Car;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalControllerTest {
    protected static MockMvc mockMvc;
    private static final String BASE_ENDPOINT = "/rentals";
    private static final String SAMPLE_USER_EMAIL = "paul@customer.com";
    private static final Long VALID_USER_ID = 2L;

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
                            "database/rental/remove-all-rentals-with-necessities.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/user/add-two-users-with-roles.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/rental/add-two-rentals-with-necessities.sql"));
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
                            "database/rental/remove-all-rentals-with-necessities.sql"));
        }
    }

    @WithUserDetails(SAMPLE_USER_EMAIL)
    @Test
    void getRentalById_UserAccessingHisRental_ReturnsRentalDto() throws Exception {
        Long validRentalId = 1L;
        RentalDto expected = createTestRentalDto(validRentalId);

        MvcResult result = mockMvc.perform(get(BASE_ENDPOINT + "/" + validRentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalDto.class);

        assertNotNull(actual);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @WithUserDetails(SAMPLE_USER_EMAIL)
    @Test
    void getAllRentalsByUserAndRentalStatus_UserAccessingHisAllRentals_ReturnsListOfRentalDto()
            throws Exception {
        Long validRentalId = 1L;
        Long validCarId = 1L;
        List<BasicRentalDto> expected = List.of(
                createTestBasicRentalDto(validRentalId, validCarId));

        MvcResult result = mockMvc.perform(get(BASE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BasicRentalDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BasicRentalDto[].class);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.length);
        assertIterableEquals(expected, Arrays.stream(actual).toList());
    }

    @WithUserDetails(SAMPLE_USER_EMAIL)
    @Sql(scripts = {"classpath:database/rental/remove-all-rentals-with-necessities.sql",
            "classpath:database/user/add-two-users-with-roles.sql",
            "classpath:database/rental/add-two-rentals-with-necessities.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void createNewRental_ValidUserAndRentalRequest_ReturnsRentalDto() throws Exception {
        Long validCarId = 1L;
        Long validNewRentalId = 3L;
        RentalRequestDto requestDto = new RentalRequestDto(10, validCarId);
        RentalDto expected = createTestRentalDtoFromRequest(validNewRentalId, requestDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post(BASE_ENDPOINT)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                RentalDto.class);

        assertNotNull(actual);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    @WithUserDetails(SAMPLE_USER_EMAIL)
    @Sql(scripts = {"classpath:database/rental/remove-all-rentals-with-necessities.sql",
            "classpath:database/user/add-two-users-with-roles.sql",
            "classpath:database/rental/add-two-rentals-with-necessities.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void returnRental_ValidUserAndRentalId_ReturnsRentalDto() throws Exception {
        Long validRentalId = 1L;
        RentalDto expected = createTestReturnedRentalDto(validRentalId);

        MvcResult result = mockMvc.perform(
                post(BASE_ENDPOINT + "/" + validRentalId + "/return")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        RentalDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RentalDto.class);

        assertNotNull(actual);
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    private BasicRentalDto createTestBasicRentalDto(Long rentalId, Long carId) {
        return new BasicRentalDto(
                rentalId,
                LocalDate.of(2021, 1, 11),
                LocalDate.of(2021, 2, 8),
                null,
                carId,
                VALID_USER_ID);
    }

    private RentalDto createTestRentalDto(Long rentalId) {
        return new RentalDto(
                rentalId,
                LocalDate.of(2021, 1, 11),
                LocalDate.of(2021, 2, 8),
                null,
                createTestCarDto(1),
                VALID_USER_ID);
    }

    private RentalDto createTestReturnedRentalDto(Long rentalId) {
        return new RentalDto(
                rentalId,
                LocalDate.of(2021, 1, 11),
                LocalDate.of(2021, 2, 8),
                LocalDate.now(),
                createTestCarDto(0),
                VALID_USER_ID);
    }

    private RentalDto createTestRentalDtoFromRequest(
            Long rentalId, RentalRequestDto requestDto) {
        return new RentalDto(
                rentalId,
                LocalDate.now(),
                LocalDate.now().plusDays(requestDto.daysOfRental()),
                null,
                createTestCarDto(1),
                VALID_USER_ID);
    }

    private CarDto createTestCarDto(int inventory) {
        return new CarDto(
                1L,
                "Astra",
                "Opel",
                Car.Type.HATCHBACK,
                inventory,
                BigDecimal.valueOf(40.99));
    }
}
