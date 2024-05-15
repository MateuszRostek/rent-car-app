package carrent.repository.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import carrent.model.Car;
import carrent.model.Payment;
import carrent.model.Rental;
import carrent.model.Role;
import carrent.model.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    public static final String ADD_TWO_PAYMENTS_PATH =
            "classpath:database/payment/add-two-payments-with-necessities.sql";
    public static final String REMOVE_ALL_PAYMENTS_PATH =
            "classpath:database/payment/remove-all-payments-with-necessities.sql";
    private static final Long VALID_USER_ID = 1L;
    private static final Long INVALID_USER_ID = 222L;
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Find a List of all Payments by valid User ID")
    @Sql(scripts = {REMOVE_ALL_PAYMENTS_PATH, ADD_TWO_PAYMENTS_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAllByUserId_ValidUserId_ReturnsListWithOnePayment() {
        Payment expected = getValidPayment();
        List<Payment> actual = paymentRepository.findAllByUserId(VALID_USER_ID);
        assertEquals(1, actual.size());
        assertThat(actual.get(0)).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    @DisplayName("Find a List of all Payments by invalid User ID")
    @Sql(scripts = {REMOVE_ALL_PAYMENTS_PATH, ADD_TWO_PAYMENTS_PATH},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void findAllByUserId_InvalidUserId_ReturnsEmptyList() {
        List<Payment> expected = new ArrayList<>();
        List<Payment> actual = paymentRepository.findAllByUserId(INVALID_USER_ID);
        assertEquals(0, actual.size());
        assertEquals(expected, actual);
    }

    private Payment getValidPayment() {
        Role expectedRole = new Role();
        expectedRole.setId(2L);
        expectedRole.setName(Role.RoleName.MANAGER);
        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setEmail("john@manager.com");
        expectedUser.setFirstName("John");
        expectedUser.setLastName("Jackson");
        expectedUser.setPassword("$2a$10$2UWH5EMjHJGwl1JbzyXd1uG1OS7W1pmOhWQXcF9nFByYM7aGUhlS6");
        expectedUser.setRoles(Set.of(expectedRole));
        Car expectedCar = new Car();
        expectedCar.setId(10L);
        expectedCar.setBrand("Opel");
        expectedCar.setModel("Astra");
        expectedCar.setType(Car.Type.HATCHBACK);
        expectedCar.setInventory(3);
        expectedCar.setDailyFee(BigDecimal.valueOf(40.99));
        Rental expectedRental = new Rental();
        expectedRental.setId(2L);
        expectedRental.setUser(expectedUser);
        expectedRental.setCar(expectedCar);
        expectedRental.setRentalDate(LocalDate.of(2024, 1, 6));
        expectedRental.setReturnDate(LocalDate.of(2024, 1, 9));
        Payment expected = new Payment();
        expected.setId(2L);
        expected.setStatus(Payment.Status.PENDING);
        expected.setType(Payment.Type.PAYMENT);
        expected.setRental(expectedRental);
        expected.setAmountToPay(BigDecimal.valueOf(100).setScale(2, RoundingMode.UNNECESSARY));
        expected.setSessionId("sessionId2");
        expected.setSessionUrl("sessionUrl2");
        return expected;
    }
}
