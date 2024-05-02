package carrent.controller;

import carrent.dto.rental.BasicRentalDto;
import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.model.User;
import carrent.service.rental.RentalService;
import carrent.service.user.UserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(value = "/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final UserService userService;

    @GetMapping("/{id}")
    public RentalDto getRentalById(Authentication authentication, @PathVariable Long id) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.findRentalByUserAndId(user, id);
    }

    @GetMapping()
    public List<BasicRentalDto> getAllRentalsByUserAndRentalStatus(
            Authentication authentication,
            @RequestParam(name = "user_id", required = false) Long userId,
            @RequestParam(name = "is_active", required = false) Boolean isActive) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.findAllRentalsByUserAndRentalStatus(user, isActive, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto createNewRental(
            Authentication authentication,
            @RequestBody @Valid RentalRequestDto requestDto) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.createNewRental(user, requestDto);
    }

    @PostMapping("/{id}/return")
    public RentalDto returnRental(Authentication authentication, @PathVariable Long id) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.returnRentalByUserAndId(user, id);
    }
}