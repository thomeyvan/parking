package com.instantsystem.parking.adapters.out.registry;

import com.instantsystem.parking.config.ParkingProperties;
import com.instantsystem.parking.config.ParkingProperties.TenantProperties;
import com.instantsystem.parking.config.ParkingProperties.ZoneProperties;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.Geopoint;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfiguredCityCatalogTest {

    private static final ZoneProperties POITIERS_BOUNDS = new ZoneProperties(46.52, 46.64, 0.27, 0.42);
    private static final ZoneProperties ANNEMASSE_BOUNDS = new ZoneProperties(46.16, 46.22, 6.20, 6.27);

    private static TenantProperties tenant(String id, ZoneProperties zone) {
        return new TenantProperties(id, id.toUpperCase(), "any", "https://" + id, zone);
    }

    private static ParkingProperties properties(List<TenantProperties> tenants) {
        return new ParkingProperties(1000, 20, Duration.ofSeconds(30), Duration.ofSeconds(1), Duration.ofSeconds(1), tenants);
    }

    @Test
    void resolvesCityByIdAndByPosition() {
        ConfiguredCityCatalog catalog = new ConfiguredCityCatalog(properties(List.of(
                tenant("poitiers", POITIERS_BOUNDS), tenant("annemasse", ANNEMASSE_BOUNDS))));

        assertThat(catalog.findCity(new Geopoint(46.58, 0.34))).map(City::id).contains("poitiers");
        assertThat(catalog.findCity(new Geopoint(46.19, 6.23))).map(City::id).contains("annemasse");
        assertThat(catalog.findCity(new Geopoint(0, 0))).isEmpty();
    }

    @Test
    void firstDeclaredCityWinsOnOverlap() {
        ConfiguredCityCatalog catalog = new ConfiguredCityCatalog(properties(List.of(
                tenant("first", POITIERS_BOUNDS), tenant("second", POITIERS_BOUNDS))));

        assertThat(catalog.findCity(new Geopoint(46.58, 0.34))).map(City::id).contains("first");
    }

    @Test
    void rejectsDuplicateTenantIds() {
        var tenants = List.of(tenant("poitiers", POITIERS_BOUNDS), tenant("poitiers", POITIERS_BOUNDS));

        assertThatThrownBy(() -> new ConfiguredCityCatalog(properties(tenants)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dupliqué");
    }
}