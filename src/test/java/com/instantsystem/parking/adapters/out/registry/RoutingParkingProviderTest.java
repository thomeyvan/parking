package com.instantsystem.parking.adapters.out.registry;

import com.instantsystem.parking.adapters.out.ParkingProviderFactory;
import com.instantsystem.parking.adapters.out.ParkingSource;
import com.instantsystem.parking.config.ParkingProperties;
import com.instantsystem.parking.config.ParkingProperties.TenantProperties;
import com.instantsystem.parking.config.ParkingProperties.ZoneProperties;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.Geopoint;
import com.instantsystem.parking.domain.models.Parking;
import com.instantsystem.parking.domain.models.Zone;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Le routeur est testé avec une fabrique factice : on vérifie qu'il route
 * vers la bonne source, isole le cache par tenant et refuse un format inconnu.
 */
class RoutingParkingProviderTest {
    private static final ZoneProperties BOUNDS = new ZoneProperties(46.52, 46.64, 0.27, 0.42);
    private static final City POITIERS = new City("poitiers", "Poitiers", new Zone(46.52, 46.64, 0.27, 0.42));
    private static final City ANNEMASSE = new City("annemasse", "Annemasse", new Zone(46.16, 46.22, 6.20, 6.27));
    private static final City UNKNOWN = new City("atlantis", "Atlantis", new Zone(0, 1, 0, 1));

    private static TenantProperties tenant(String id, String format, String url) {
        return new TenantProperties(id, id.toUpperCase(), format, url, BOUNDS);
    }

    private static ParkingProperties properties(List<TenantProperties> tenants) {
        return new ParkingProperties(1000, 20, Duration.ofMinutes(5), Duration.ofSeconds(1), Duration.ofSeconds(1), tenants);
    }

    /** Fabrique factice : chaque source renvoie un parking nommé d'après son URL, et compte ses appels réels. */
    static class FakeFactory implements ParkingProviderFactory {
        final AtomicInteger calls = new AtomicInteger();

        public String format() {
            return "fake";
        }

        public ParkingSource create(TenantProperties tenant) {
            return () -> {
                calls.incrementAndGet();
                return List.of(new Parking(tenant.id() + "-1", tenant.url(), 10, 5, new Geopoint(46.58, 0.34), null));
            };
        }
    }

    @Test
    void routesEachCityToItsOwnSource() {
        var router = new RoutingParkingProvider(properties(List.of(
                tenant("poitiers", "fake", "https://a"),
                tenant("annemasse", "fake", "https://b"))), List.of(new FakeFactory()));

        assertThat(router.findAll(POITIERS)).extracting(Parking::name).containsExactly("https://a");
        assertThat(router.findAll(ANNEMASSE)).extracting(Parking::name).containsExactly("https://b");
    }

    @Test
    void cachesPerTenant() {
        var factory = new FakeFactory();
        var router = new RoutingParkingProvider(properties(List.of(
                tenant("poitiers", "fake", "https://a"),
                tenant("annemasse", "fake", "https://b"))), List.of(factory));

        router.findAll(POITIERS);
        router.findAll(POITIERS);
        router.findAll(ANNEMASSE);

        assertThat(factory.calls.get()).isEqualTo(2);
    }

    @Test
    void failsOnCityWithoutSource() {
        var router = new RoutingParkingProvider(properties(List.of(
                tenant("poitiers", "fake", "https://a"))), List.of(new FakeFactory()));

        assertThatThrownBy(() -> router.findAll(UNKNOWN)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsUnknownFormatAtStartup() {
        var tenants = List.of(tenant("poitiers", "unknown-format", "https://a"));

        assertThatThrownBy(() -> new RoutingParkingProvider(properties(tenants), List.of(new FakeFactory())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unknown-format");
    }
}