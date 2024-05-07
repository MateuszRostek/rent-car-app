package carrent.repository.payment;

import carrent.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("from Payment p where p.rental.user.id = :userId")
    List<Payment> findAllByUserId(Long userId);

    Optional<Payment> findByRentalIdAndType(Long rentalId, Payment.Type type);
}
