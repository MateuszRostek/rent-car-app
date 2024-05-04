package carrent.service.payment.impl;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.mapper.PaymentMapper;
import carrent.model.Payment;
import carrent.model.Role;
import carrent.model.User;
import carrent.repository.payment.PaymentRepository;
import carrent.service.payment.PaymentService;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceStripeImpl implements PaymentService {
    private static final Role.RoleName MANAGER_ROLENAME = Role.RoleName.MANAGER;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto createPaymentSession(CreatePaymentRequestDto requestDto) {
        return null;
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
