package carrent.controller;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.dto.payment.PaymentPausedDto;
import carrent.model.Payment;
import carrent.model.User;
import carrent.service.payment.PaymentService;
import carrent.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment Management", description = "Endpoints for managing payments")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Operation(
            summary = "Create a payment session",
            description = "Create a payment session using Stripe API for processing payments.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto createPaymentSession(
            @RequestBody CreatePaymentRequestDto requestDto) {
        return paymentService.createPaymentSession(stripeApiKey, requestDto);
    }

    @Operation(
            summary = "Get all payments by user ID",
            description = "Retrieve a list of payments based on the user ID "
                    + "- managers can access all users' payments, "
                    + "while customers can only access their own payments.")
    @GetMapping
    public List<PaymentDto> getAllPaymentsByUserId(
            Authentication authentication,
            @RequestParam(name = "user_id", required = false) Long userId) {
        User user = userService.getUserFromAuthentication(authentication);
        return paymentService.getAllPaymentsByUserId(user, userId);
    }

    @Operation(
            summary = "Check successful payment",
            description = "Check the success of a payment "
                    + "based on the rental ID and payment type - Stripe API redirection.")
    @GetMapping("/success/{rentalId}")
    public PaymentDto checkSuccessfulPayment(
            @PathVariable Long rentalId,
            @RequestParam Payment.Type type) {
        return paymentService.checkSuccessfulPayment(stripeApiKey, rentalId, type);
    }

    @Operation(
            summary = "Get cancel payment paused message",
            description = "Retrieve a message indicating "
                    + "that the payment cancellation process is paused - Stripe API redirection.")
    @GetMapping("/cancel/{rentalId}")
    public PaymentPausedDto getCancelPaymentPausedMessage(@PathVariable Long rentalId) {
        return paymentService.getCancelPaymentPausedMessage(rentalId);
    }
}
