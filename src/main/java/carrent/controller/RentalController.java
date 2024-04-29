package carrent.controller;

import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.model.User;
import carrent.service.rental.RentalService;
import carrent.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto createNewRental(
            Authentication authentication,
            @RequestBody @Valid RentalRequestDto requestDto) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.createNewRental(user, requestDto);
    }
}
