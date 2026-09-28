package com.instantsystem.parking.adapters.out.registry;

import com.instantsystem.parking.config.ParkingProperties;
import com.instantsystem.parking.config.ParkingProperties.ZoneProperties;
import com.instantsystem.parking.config.ParkingProperties.TenantProperties;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.Geopoint;
import com.instantsystem.parking.domain.models.Zone;
import com.instantsystem.parking.domain.port.out.CityCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Référentiel des villes alimenté par application.yml. Un identifiant dupliqué
 * fait échouer le démarrage.
 */
@Component
public class ConfiguredCityCatalog implements CityCatalog {

    private static final Logger log = LoggerFactory.getLogger(ConfiguredCityCatalog.class);

    private final Map<String, City> cities = new LinkedHashMap<>();

    public ConfiguredCityCatalog(ParkingProperties properties) {
        for (TenantProperties tenant : properties.tenants()) {
            if (cities.containsKey(tenant.id())) {
                throw new IllegalStateException("Tenant dupliqué : " + tenant.id());
            }

            ZoneProperties b = tenant.zone();

            City city = new City(tenant.id(), tenant.name(), new Zone(b.minLatitude(), b.maxLatitude(), b.minLongitude(), b.maxLongitude()));
            cities.put(city.id(), city);

            log.info("Ville chargée : {} ({})", city.id(), city.name());
        }
        if (cities.isEmpty()) {
            log.warn("Aucun tenant configuré : toutes les requêtes répondront 404");
        }
    }

    @Override
    public Optional<City> findCity(Geopoint position) {
        return cities.values().stream()
                .filter(city -> city.zone().contains(position))
                .findFirst();
    }
}
