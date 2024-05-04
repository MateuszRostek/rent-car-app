package carrent.controller;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.model.User;
import carrent.service.payment.PaymentService;
import carrent.service.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping
    public PaymentDto createPaymentSession(@RequestBody CreatePaymentRequestDto requestDto) {
        return paymentService.createPaymentSession(requestDto);
    }

    @GetMapping
    public List<PaymentDto> getAllPaymentsByUserId(
            Authentication authentication,
            @RequestParam(name = "user_id", required = false) Long userId) {
        User user = userService.getUserFromAuthentication(authentication);
        return paymentService.getAllPaymentsByUserId(user, userId);
    }
}
