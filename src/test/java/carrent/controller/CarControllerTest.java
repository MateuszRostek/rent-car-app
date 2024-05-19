package carrent.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import carrent.dto.car.CarDto;
import carrent.dto.car.CreateCarRequestDto;
import carrent.model.Car;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
    protected static MockMvc mockMvc;
    private static final String BASE_ENDPOINT = "/cars";

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
                    new ClassPathResource("database/car/remove-all-cars.sql"));
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/car/add-two-cars.sql"));
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
                    new ClassPathResource("database/car/remove-all-cars.sql"));
        }
    }

    @Test
    void getCarById_ValidCarId_ReturnsCarDto() throws Exception {
        Long validCarId = 1L;
        CarDto expected = createTestCarDto(validCarId);

        MvcResult result = mockMvc.perform(
                        get(BASE_ENDPOINT + "/" + validCarId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                CarDto.class);

        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    void getAllCars_Valid_ReturnsListOfAllCarDto() throws Exception {
        Long validFirstCarId = 1L;
        Long validSecondCarId = 2L;
        List<CarDto> expected = List.of(
                createTestCarDto(validFirstCarId), createSecondTestCarDto(validSecondCarId));

        MvcResult result = mockMvc.perform(
                        get(BASE_ENDPOINT).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        CarDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                CarDto[].class);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.length);
        assertIterableEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @Sql(scripts = {"classpath:database/car/remove-created-car.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void createCar_ValidRequestAndUserRole_ReturnsCarDto() throws Exception {
        Long validCarId = 3L;
        CreateCarRequestDto requestDto = createTestCarRequestDto();
        CarDto expected = createTestCarDtoFromRequest(validCarId, requestDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(post(BASE_ENDPOINT)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                CarDto.class);

        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @Sql(scripts = {
            "classpath:database/car/remove-all-cars.sql",
            "classpath:database/car/add-two-cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void updateCar_ValidCarIdUserRoleAndRequest_ReturnsCarDto() throws Exception {
        Long validCarId = 2L;
        CreateCarRequestDto requestDto = createTestCarRequestDto();
        CarDto expected = createTestCarDtoFromRequest(validCarId, requestDto);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        MvcResult result = mockMvc.perform(put(BASE_ENDPOINT + "/" + validCarId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        CarDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                CarDto.class);

        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "manager", authorities = {"MANAGER"})
    @Sql(scripts = {
            "classpath:database/car/remove-all-cars.sql",
            "classpath:database/car/add-two-cars.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void deleteCar_ValidCarIdAndUserRole_ReturnsNoContentStatus() throws Exception {
        long validCarId = 2L;

        mockMvc.perform(delete(BASE_ENDPOINT + "/" + validCarId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();
        mockMvc.perform(
                        get(BASE_ENDPOINT + "/" + validCarId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    private CreateCarRequestDto createTestCarRequestDto() {
        return new CreateCarRequestDto(
                "X7",
                "BMW",
                "SUV",
                2,
                BigDecimal.valueOf(139.99));
    }

    private CarDto createTestCarDto(Long id) {
        return new CarDto(
                id,
                "Astra",
                "Opel",
                Car.Type.HATCHBACK,
                3,
                BigDecimal.valueOf(40.99));
    }

    private CarDto createSecondTestCarDto(Long id) {
        return new CarDto(
                id,
                "i30",
                "Hyundai",
                Car.Type.HATCHBACK,
                2,
                BigDecimal.valueOf(49.99));
    }

    private CarDto createTestCarDtoFromRequest(Long id, CreateCarRequestDto requestDto) {
        return new CarDto(
                id,
                requestDto.model(),
                requestDto.brand(),
                Car.Type.valueOf(requestDto.type()),
                requestDto.inventory(),
                requestDto.dailyFee());
    }
}
