package com.instantsystem.parking.adapters.in.rest;

import com.instantsystem.parking.config.ParkingProperties;
import com.instantsystem.parking.domain.models.Geopoint;
import com.instantsystem.parking.domain.port.in.FindNearbyParkingsUseCase;
import com.instantsystem.parking.domain.port.in.NearbyParkingQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parkings")
@Validated
@Tag(name = "Parkings", description = "Parkings à proximité d'une position")
public class ParkingController {

    private final FindNearbyParkingsUseCase findNearbyParkings;
    private final ParkingProperties properties;

    public ParkingController(FindNearbyParkingsUseCase findNearbyParkings, ParkingProperties properties) {
        this.findNearbyParkings = findNearbyParkings;
        this.properties = properties;
    }

    @GetMapping
    @Operation(
        summary = "Liste les parkings dans un rayon autour d'une position, triés par distance. La ville est déduite de la position"
    )
    public NearbyParkingsResponse nearby(
            @Parameter(description = "Latitude de l'utilisateur", example = "46.5802")
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double latitude,
            @Parameter(description = "Longitude de l'utilisateur", example = "0.3404")
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double longitude,
            @Parameter(description = "Rayon de recherche en mètres")
            @RequestParam(required = false) @Min(1) @Max(50_000) Integer radius,
            @Parameter(description = "Nombre maximal de résultats")
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit) {

        var query = new NearbyParkingQuery(
            new Geopoint(latitude, longitude),
            radius != null ? radius : properties.defaultRadius(),
            limit != null ? limit : properties.defaultLimit()
        );

        return NearbyParkingsResponse.from(findNearbyParkings.findNearbyParkings(query));
    }
}
