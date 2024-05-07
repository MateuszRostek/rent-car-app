package carrent.service.payment.impl;

import static java.time.temporal.ChronoUnit.DAYS;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.exception.PaymentAlreadyPaidException;
import carrent.exception.StripeSessionCreationException;
import carrent.exception.TooManyPaymentsException;
import carrent.mapper.PaymentMapper;
import carrent.model.Payment;
import carrent.model.Rental;
import carrent.model.Role;
import carrent.model.User;
import carrent.repository.payment.PaymentRepository;
import carrent.repository.rental.RentalRepository;
import carrent.service.payment.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceStripeImpl implements PaymentService {
    private static final Role.RoleName MANAGER_ROLENAME = Role.RoleName.MANAGER;
    private static final BigDecimal FINE_MULTIPLIER = new BigDecimal("1.7");
    private static final BigDecimal DOLLARS_TO_CENTS = new BigDecimal("100");
    private static final String SUCCESS_URL_STRING = "http://localhost:8080/api/payments/success/";
    private static final String CANCEL_URL_STRING = "http://localhost:8080/api/payments/cancel/";
    private static final String CURRENCY = "USD";
    private static final String PAYMENT_NAME = "Payment for car rental - rental ID: ";
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RentalRepository rentalRepository;

    @Override
    @Transactional
    public PaymentDto createPaymentSession(
            String stripeApiKey,
            CreatePaymentRequestDto requestDto) {
        Stripe.apiKey = stripeApiKey;
        Long rentalId = requestDto.rentalId();
        checkIfAlreadyPaid(requestDto);
        BigDecimal totalPrice = calculateTotalPrice(requestDto).setScale(2, RoundingMode.CEILING);

        SessionCreateParams sessionCreateParams = SessionCreateParams.builder()
                .setSuccessUrl(UriComponentsBuilder.fromHttpUrl(
                        SUCCESS_URL_STRING + rentalId).toUriString())
                .setCancelUrl(UriComponentsBuilder.fromHttpUrl(
                        CANCEL_URL_STRING + rentalId).toUriString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmountDecimal(
                                                        totalPrice
                                                                .multiply(DOLLARS_TO_CENTS))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData
                                                                .ProductData.builder()
                                                                .setName(PAYMENT_NAME + rentalId)
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .build();

        Session session;
        try {
            session = Session.create(sessionCreateParams);
        } catch (StripeException e) {
            throw new StripeSessionCreationException("Can't create Stripe session!", e);
        }

        Payment payment = new Payment();
        payment.setStatus(Payment.Status.PENDING);
        payment.setType(requestDto.type());
        payment.setRental(getRentalFromDb(rentalId));
        payment.setAmountToPay(totalPrice);
        payment.setSessionUrl(session.getUrl());
        payment.setSessionId(session.getId());
        return paymentMapper.toDtoFromModel(paymentRepository.save(payment));
    }

    @Override
    public List<PaymentDto> getAllPaymentsByUserId(User user, Long userId) {
        if (checkIfUserIsManager(user)) {
            return createPaymentStreamForManager(userId)
                    .map(paymentMapper::toDtoFromModel)
                    .toList();
        }
        if (userId == null || user.getId().equals(userId)) {
            return paymentRepository.findAllByUserId(userId).stream()
                    .map(paymentMapper::toDtoFromModel)
                    .toList();
        }
        throw new AccessDeniedException("This user is not allowed to access these payments");
    }

    private void checkIfAlreadyPaid(CreatePaymentRequestDto requestDto) {
        Optional<Payment> paymentFromDb;
        try {
            paymentFromDb = paymentRepository.findByRentalIdAndType(
                    requestDto.rentalId(), requestDto.type());
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new TooManyPaymentsException(
                    "Too many payment records for the given rental ID: " + requestDto.rentalId()
            + ". Finish previous payment instead of creating a new one!");
        }
        if (paymentFromDb.isPresent()
                && paymentFromDb.get().getStatus().equals(Payment.Status.PAID)) {
            throw new PaymentAlreadyPaidException("This payment has been already paid!");
        }
    }

    private BigDecimal calculateTotalPrice(CreatePaymentRequestDto requestDto) {
        Rental rentalFromDb = getRentalFromDb(requestDto.rentalId());

        if (requestDto.type() == Payment.Type.PAYMENT) {
            long daysOfRental = DAYS.between(
                    rentalFromDb.getRentalDate(), rentalFromDb.getReturnDate());
            return rentalFromDb.getCar().getDailyFee()
                    .multiply(BigDecimal.valueOf(daysOfRental));
        }
        long overdueDays = DAYS.between(
                rentalFromDb.getReturnDate(), rentalFromDb.getActualReturnDate());
        return rentalFromDb.getCar().getDailyFee()
                .multiply(BigDecimal.valueOf(overdueDays))
                .multiply(FINE_MULTIPLIER);
    }

    private Rental getRentalFromDb(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental with id " + rentalId + " not found"));
    }

    private boolean checkIfUserIsManager(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(MANAGER_ROLENAME));
    }

    private Stream<Payment> createPaymentStreamForManager(Long userId) {
        return userId == null
                ? paymentRepository.findAll().stream()
                : paymentRepository.findAllByUserId(userId).stream();
    }
}
