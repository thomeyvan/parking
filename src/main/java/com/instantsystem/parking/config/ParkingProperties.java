package com.instantsystem.parking.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "parking")
public record ParkingProperties(
        @DefaultValue("1000") int defaultRadius,
        @DefaultValue("20") int defaultLimit,
        @DefaultValue("30s") Duration cacheTtl,
        @DefaultValue("2s") Duration connectTimeout,
        @DefaultValue("3s") Duration readTimeout,
        @DefaultValue List<@Valid TenantProperties> tenants) {

    /** Un tenant = une ville, son format de source et son URL propre. */
    public record TenantProperties(
            @NotBlank String id,
            @NotBlank String name,
            @NotBlank String format,
            @NotBlank String url,
            @NotNull @Valid ZoneProperties zone) {
    }

    public record ZoneProperties(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
    }
}
