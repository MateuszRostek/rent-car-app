package carrent.repository.rental;

import carrent.model.Rental;
import carrent.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findAllByUser(User user);

    List<Rental> findAllByUserId(Long userId);
}
