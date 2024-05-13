package carrent.controller;

import carrent.dto.rental.BasicRentalDto;
import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.model.User;
import carrent.service.rental.RentalService;
import carrent.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Rental Management", description = "Endpoints for managing rentals")
@RestController
@RequestMapping(value = "/rentals")
@RequiredArgsConstructor
public class RentalController {
    private final RentalService rentalService;
    private final UserService userService;

    @Operation(
            summary = "Get a rental by ID",
            description = "Retrieve information about a specific rental "
                    + "based on its unique identifier.")
    @GetMapping("/{id}")
    public RentalDto getRentalById(Authentication authentication, @PathVariable Long id) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.findRentalByUserAndId(user, id);
    }

    @Operation(
            summary = "Get all rentals",
            description = "Retrieve a list of rentals based on the user and rental status "
                    + "- managers can access all users' rentals, "
                    + "while customers can only access their own rentals.")
    @GetMapping()
    public List<BasicRentalDto> getAllRentalsByUserAndRentalStatus(
            Authentication authentication,
            @RequestParam(name = "user_id", required = false) Long userId,
            @RequestParam(name = "is_active", required = false) Boolean isActive) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.findAllRentalsByUserAndRentalStatus(user, isActive, userId);
    }

    @Operation(
            summary = "Create a new rental",
            description = "Create and save a new rental for the authenticated user.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalDto createNewRental(
            Authentication authentication,
            @RequestBody @Valid RentalRequestDto requestDto) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.createNewRental(user, requestDto);
    }

    @Operation(
            summary = "Return a rental",
            description = "Return a rental identified by its ID for the authenticated user.")
    @PostMapping("/{id}/return")
    public RentalDto returnRental(Authentication authentication, @PathVariable Long id) {
        User user = userService.getUserFromAuthentication(authentication);
        return rentalService.returnRentalByUserAndId(user, id);
    }
}
