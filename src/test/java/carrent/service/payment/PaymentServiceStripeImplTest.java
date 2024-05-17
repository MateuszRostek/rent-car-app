package carrent.service.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.dto.payment.PaymentPausedDto;
import carrent.exception.TooManyPaymentsException;
import carrent.mapper.PaymentMapper;
import carrent.model.Car;
import carrent.model.Payment;
import carrent.model.Rental;
import carrent.model.Role;
import carrent.model.User;
import carrent.repository.payment.PaymentRepository;
import carrent.repository.rental.RentalRepository;
import carrent.service.notification.NotificationService;
import carrent.service.payment.impl.PaymentServiceStripeImpl;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceStripeImplTest {
    private static final Payment.Type SAMPLE_PAYMENT_TYPE = Payment.Type.PAYMENT;
    private static final Payment.Status SAMPLE_PAID_STATUS = Payment.Status.PAID;
    private static final Payment.Status SAMPLE_PENDING_STATUS = Payment.Status.PENDING;
    private static final BigDecimal SAMPLE_PAYMENT_AMOUNT = BigDecimal.valueOf(99.99);
    private static final BigDecimal SAMPLE_DAILY_FREE = BigDecimal.valueOf(49.99);
    private static final String SAMPLE_STRIPE_API_KEY = "Sample_Key";
    private static final String SAMPLE_SESSION_URL = "http://test-session-url";
    private static final String SAMPLE_SESSION_ID = "test-session-id";
    private static final String STRIPE_PAYMENT_STATUS_PAID = "paid";
    private static final String STRIPE_PAYMENT_STATUS_PENDING = "pending";
    private static final Role.RoleName SAMPLE_DEFAULT_ROLE = Role.RoleName.CUSTOMER;
    private static final Role.RoleName SAMPLE_MANAGER_ROLE = Role.RoleName.MANAGER;
    private static final Long VALID_PAYMENT_ID = 1L;
    private static final Long VALID_RENTAL_ID = 1L;
    private static final Long VALID_USER_ID = 1L;
    private static final Long INVALID_USER_ID = 1000L;
    private static final Long INVALID_RENTAL_ID = 1000L;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private PaymentServiceStripeImpl paymentService;

    @Test
    @DisplayName("Create payment session (Everything Valid) - Returns PaymentDto")
    void createPaymentSession_EverythingValid_ReturnsPaymentDto() throws StripeException {
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto(
                VALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE);
        Rental modelRental = createTestRental();
        modelRental.getCar().setDailyFee(SAMPLE_DAILY_FREE);
        Payment modelPayment = createTestPayment(modelRental, SAMPLE_PENDING_STATUS);
        PaymentDto expected = createTestPaymentDto(modelPayment);
        when(paymentRepository.findByRentalIdAndType(requestDto.rentalId(), requestDto.type()))
                .thenReturn(Optional.empty());
        when(rentalRepository.findById(requestDto.rentalId()))
                .thenReturn(Optional.of(modelRental));
        when(paymentRepository.save(any(Payment.class))).thenReturn(modelPayment);
        when(paymentMapper.toDtoFromModel(modelPayment)).thenReturn(expected);

        try (MockedStatic<Session> ignored = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(Session.create(any(SessionCreateParams.class))).thenReturn(session);
            when(session.getUrl()).thenReturn(SAMPLE_SESSION_URL);
            when(session.getId()).thenReturn(SAMPLE_SESSION_ID);

            PaymentDto actual =
                    paymentService.createPaymentSession(SAMPLE_STRIPE_API_KEY, requestDto);

            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(paymentRepository, times(2)).findByRentalIdAndType(any(), any());
            verify(rentalRepository, times(2)).findById(any());
            verify(paymentRepository, times(1)).save(any());
            verify(paymentMapper, times(1)).toDtoFromModel(any());
            verifyNoMoreInteractions(
                    paymentMapper, paymentRepository, rentalRepository, notificationService);
        }
    }

    @Test
    @DisplayName("Create payment session (session for this rental ID and type already created)"
            + " - Throws TooManyPaymentsException")
    void createPaymentSession_SessionAlreadyCreated_ThrowsTooManyPaymentsException() {
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto(
                VALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE);
        Rental modelRental = createTestRental();
        Payment modelPayment = createTestPayment(modelRental, SAMPLE_PENDING_STATUS);
        when(paymentRepository.findByRentalIdAndType(requestDto.rentalId(), requestDto.type()))
                .thenReturn(Optional.of(modelPayment));
        String expected = "Too many payment records for the given rental ID: "
                + requestDto.rentalId()
                + ". Finish previous payment instead of creating a new one!";

        TooManyPaymentsException exception = assertThrows(
                TooManyPaymentsException.class,
                () -> paymentService.createPaymentSession(SAMPLE_STRIPE_API_KEY, requestDto));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(paymentRepository, times(1)).findByRentalIdAndType(any(), any());
        verifyNoMoreInteractions(
                paymentMapper, paymentRepository, rentalRepository, notificationService);
    }

    @Test
    @DisplayName("Get paused payment message with passed Rental ID "
            + "- Returns PaymentPausedDto")
    void getCancelPaymentPausedMessage_RentalIdPassed_ReturnsPaymentPausedDto() {
        Long rentalId = 1L;
        PaymentPausedDto expected = new PaymentPausedDto("The Payment was canceled! "
                + "It can be made later, but the session is available for only 24 hours!"
                + "Rental ID: " + rentalId);

        PaymentPausedDto actual = paymentService.getCancelPaymentPausedMessage(rentalId);

        assertEquals(expected, actual);
        verifyNoMoreInteractions(
                paymentMapper, paymentRepository, rentalRepository, notificationService);
    }

    @Test
    @DisplayName("Check Successful Payment (payment successfully paid) - Returns PaymentDto")
    void checkSuccessfulPayment_PaymentSuccessfullyPaid_ReturnsPaymentDto()
            throws StripeException {
        Rental rental = createTestRental();
        Payment modelPayment = createTestPayment(rental, SAMPLE_PAID_STATUS);
        PaymentDto expected = createTestPaymentDto(modelPayment);
        when(paymentRepository.findByRentalIdAndType(VALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE))
                .thenReturn(Optional.of(modelPayment));
        when(paymentRepository.save(modelPayment)).thenReturn(modelPayment);
        when(paymentMapper.toDtoFromModel(modelPayment)).thenReturn(expected);
        doNothing().when(notificationService).sendSuccessfulPaymentNotification(expected);

        try (MockedStatic<Session> ignored = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getPaymentStatus()).thenReturn(STRIPE_PAYMENT_STATUS_PAID);
            when(Session.retrieve(modelPayment.getSessionId())).thenReturn(session);

            PaymentDto actual = paymentService.checkSuccessfulPayment(
                    SAMPLE_STRIPE_API_KEY, VALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE);

            assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
            verify(paymentRepository, times(1)).findByRentalIdAndType(any(), any());
            verify(paymentRepository, times(1)).save(any());
            verify(paymentMapper, times(1)).toDtoFromModel(any());
            verify(notificationService).sendSuccessfulPaymentNotification(expected);
            verifyNoMoreInteractions(
                    paymentMapper, paymentRepository, rentalRepository, notificationService);
        }
    }

    @Test
    @DisplayName("Check Successful Payment (payment doesn't exist) "
            + "- Throws EntityNotFoundException")
    void checkSuccessfulPayment_PaymentDoesntExist_ThrowsEntityNotFoundException() {
        when(paymentRepository.findByRentalIdAndType(INVALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE))
                .thenReturn(Optional.empty());
        String expected = "Can't find payment with id: " + INVALID_RENTAL_ID;

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.checkSuccessfulPayment(
                        SAMPLE_STRIPE_API_KEY, INVALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(paymentRepository, times(1)).findByRentalIdAndType(any(), any());
        verifyNoMoreInteractions(
                paymentMapper, paymentRepository, rentalRepository, notificationService);
    }

    @Test
    @DisplayName("Check Successful Payment (payment is not paid) - Throws AccessDeniedException")
    void checkSuccessfulPayment_PaymentIsNotPaid_ThrowsAccessDeniedException()
            throws StripeException {
        Rental rental = createTestRental();
        Payment modelPayment = createTestPayment(rental, SAMPLE_PENDING_STATUS);
        when(paymentRepository.findByRentalIdAndType(VALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE))
                .thenReturn(Optional.of(modelPayment));
        String expected = "Can't access this endpoint - The payment is not paid!";

        try (MockedStatic<Session> ignored = mockStatic(Session.class)) {
            Session session = mock(Session.class);
            when(session.getPaymentStatus()).thenReturn(STRIPE_PAYMENT_STATUS_PENDING);
            when(Session.retrieve(modelPayment.getSessionId())).thenReturn(session);

            AccessDeniedException exception = assertThrows(
                    AccessDeniedException.class,
                    () -> paymentService.checkSuccessfulPayment(
                            SAMPLE_STRIPE_API_KEY, VALID_RENTAL_ID, SAMPLE_PAYMENT_TYPE));
            String actual = exception.getMessage();

            assertEquals(expected, actual);
            verify(paymentRepository, times(1)).findByRentalIdAndType(any(), any());
            verifyNoMoreInteractions(
                    paymentMapper, paymentRepository, rentalRepository, notificationService);
        }
    }

    @Test
    @DisplayName("Find all payments (Customer accessing own payments) "
            + "- Returns List of PaymentDto")
    void getAllPaymentsByUserId_UserAccessingOwnRentals_ReturnsListPaymentDto() {
        User modelUser = createTestUser(SAMPLE_DEFAULT_ROLE);
        Rental rental = createTestRental();
        Payment modelPayment = createTestPayment(rental, SAMPLE_PENDING_STATUS);
        List<Payment> modelPaymentList = List.of(modelPayment);
        PaymentDto paymentDto = createTestPaymentDto(modelPayment);
        List<PaymentDto> expected = List.of(paymentDto);
        when(paymentRepository.findAllByUserId(modelUser.getId())).thenReturn(modelPaymentList);
        when(paymentMapper.toDtoFromModel(modelPayment)).thenReturn(paymentDto);

        List<PaymentDto> actual = paymentService.getAllPaymentsByUserId(
                modelUser, modelUser.getId());

        assertIterableEquals(expected, actual);
        verify(paymentRepository, times(1)).findAllByUserId(any());
        verify(paymentMapper, times(1)).toDtoFromModel(any());
        verifyNoMoreInteractions(
                paymentMapper, paymentRepository, rentalRepository, notificationService);
    }

    @Test
    @DisplayName("Find all payments (Manager accessing all payments) "
            + "- Returns List of PaymentDto")
    void getAllPaymentsByUserId_ManagerAccessingAllRentals_ReturnsListPaymentDto() {
        User modelUser = createTestUser(SAMPLE_MANAGER_ROLE);
        Rental firstRental = createTestRental();
        Rental secondRental = createTestRental();
        Payment firstPayment = createTestPayment(firstRental, SAMPLE_PENDING_STATUS);
        Payment secondPayment = createTestPayment(secondRental, SAMPLE_PENDING_STATUS);
        List<Payment> paymentList = List.of(firstPayment, secondPayment);
        PaymentDto firstPaymentDto = createTestPaymentDto(firstPayment);
        PaymentDto secondPaymentDto = createTestPaymentDto(secondPayment);
        when(paymentRepository.findAll()).thenReturn(paymentList);
        when(paymentMapper.toDtoFromModel(firstPayment)).thenReturn(firstPaymentDto);
        when(paymentMapper.toDtoFromModel(secondPayment)).thenReturn(secondPaymentDto);
        List<PaymentDto> expected = List.of(firstPaymentDto, secondPaymentDto);

        List<PaymentDto> actual = paymentService.getAllPaymentsByUserId(modelUser, null);

        assertIterableEquals(expected, actual);
        verify(paymentRepository, times(1)).findAll();
        verify(paymentMapper, times(2)).toDtoFromModel(any());
        verifyNoMoreInteractions(
                paymentMapper, paymentRepository, rentalRepository, notificationService);
    }

    @Test
    @DisplayName("Find all payments (Customer accessing other customer's payments) "
            + "- Throws AccessDeniedException")
    void getAllPaymentsByUserId_InvalidPermissions_ThrowsAccessDeniedException() {
        User modelUser = createTestUser(SAMPLE_DEFAULT_ROLE);
        String expected = "This user is not allowed to access these payments";

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> paymentService.getAllPaymentsByUserId(
                        modelUser, INVALID_USER_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verifyNoMoreInteractions(
                paymentMapper, paymentRepository, rentalRepository, notificationService);
    }

    private PaymentDto createTestPaymentDto(Payment modelPayment) {
        return new PaymentDto(
                modelPayment.getId(),
                modelPayment.getStatus(),
                modelPayment.getType(),
                modelPayment.getRental().getId(),
                modelPayment.getAmountToPay(),
                modelPayment.getSessionUrl(),
                modelPayment.getSessionId());
    }

    private Payment createTestPayment(Rental rental, Payment.Status status) {
        Payment payment = new Payment();
        payment.setId(VALID_PAYMENT_ID);
        payment.setRental(rental);
        payment.setAmountToPay(SAMPLE_PAYMENT_AMOUNT);
        payment.setStatus(status);
        payment.setType(SAMPLE_PAYMENT_TYPE);
        payment.setSessionUrl(SAMPLE_SESSION_URL);
        payment.setSessionId(SAMPLE_SESSION_ID);
        return payment;
    }

    private Rental createTestRental() {
        Rental rental = new Rental();
        rental.setId(VALID_RENTAL_ID);
        rental.setCar(new Car());
        rental.setUser(new User());
        rental.setRentalDate(LocalDate.now().minusDays(5));
        rental.setReturnDate(LocalDate.now().plusDays(4));
        return rental;
    }

    private User createTestUser(Role.RoleName roleName) {
        Role role = new Role();
        role.setName(roleName);
        User user = new User();
        user.setId(VALID_USER_ID);
        user.setRoles(Set.of(role));
        return user;
    }
}
