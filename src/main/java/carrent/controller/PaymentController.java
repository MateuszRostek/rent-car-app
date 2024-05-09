package carrent.controller;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.dto.payment.PaymentPausedDto;
import carrent.model.Payment;
import carrent.model.User;
import carrent.service.payment.PaymentService;
import carrent.service.user.UserService;
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

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto createPaymentSession(
            @Value("${stripe.api.key}") String stripeApiKey,
            @RequestBody CreatePaymentRequestDto requestDto) {
        return paymentService.createPaymentSession(stripeApiKey, requestDto);
    }

    @GetMapping
    public List<PaymentDto> getAllPaymentsByUserId(
            Authentication authentication,
            @RequestParam(name = "user_id", required = false) Long userId) {
        User user = userService.getUserFromAuthentication(authentication);
        return paymentService.getAllPaymentsByUserId(user, userId);
    }

    @GetMapping("/success/{rentalId}")
    public PaymentDto checkSuccessfulPayment(
            @Value("${stripe.api.key}") String stripeApiKey,
            @PathVariable Long rentalId,
            @RequestParam Payment.Type type) {
        return paymentService.checkSuccessfulPayment(stripeApiKey, rentalId, type);
    }

    @GetMapping("/cancel/{rentalId}")
    public PaymentPausedDto getCancelPaymentPausedMessage(@PathVariable Long rentalId) {
        return paymentService.getCancelPaymentPausedMessage(rentalId);
    }
}
