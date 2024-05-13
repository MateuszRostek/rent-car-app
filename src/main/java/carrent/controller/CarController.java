package carrent.controller;

import carrent.dto.car.CarDto;
import carrent.dto.car.CreateCarRequestDto;
import carrent.service.car.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Car Management", description = "Endpoints for managing cars")
@RestController
@RequestMapping(value = "/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;

    @Operation(
            summary = "Get a car by ID",
            description = "Retrieve information about a specific car "
                    + "based on its unique identifier.")
    @GetMapping("/{id}")
    public CarDto getCarById(@PathVariable Long id) {
        return carService.findById(id);
    }

    @Operation(
            summary = "Get all cars",
            description = "Retrieve a paginated list of all available cars.")
    @GetMapping
    public List<CarDto> getAllCars(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @Operation(
            summary = "Create a new car",
            description = "Create and save a new car "
                    + "by including the required car details in the request body.")
    @PreAuthorize("hasAuthority('MANAGER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarDto createCar(@RequestBody @Valid CreateCarRequestDto car) {
        return carService.save(car);
    }

    @Operation(
            summary = "Update a car",
            description = "Modify the details of an existing car identified by its ID.")
    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{id}")
    public CarDto updateCar(@PathVariable Long id, @RequestBody @Valid CreateCarRequestDto car) {
        return carService.updateById(id, car);
    }

    @Operation(
            summary = "Delete a car",
            description = "Soft delete a car from the database based on its unique identifier.")
    @PreAuthorize("hasAuthority('MANAGER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable Long id) {
        carService.deleteById(id);
    }
}
