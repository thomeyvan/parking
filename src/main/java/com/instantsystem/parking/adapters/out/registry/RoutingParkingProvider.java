package com.instantsystem.parking.adapters.out.registry;


import com.instantsystem.parking.adapters.out.ParkingProviderFactory;
import com.instantsystem.parking.adapters.out.ParkingSource;
import com.instantsystem.parking.config.ParkingProperties;
import com.instantsystem.parking.config.ParkingProperties.TenantProperties;
import com.instantsystem.parking.domain.models.City;
import com.instantsystem.parking.domain.models.Parking;
import com.instantsystem.parking.domain.port.out.ParkingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Unique implémentation du port ParkingProvider. Construit au démarrage une
 * source (avec cache) par tenant, en choisissant la fabrique par format, et
 * route chaque appel vers la source de la ville demandée. Un format inconnu
 * fait échouer le démarrage.
 */
@Component
public class RoutingParkingProvider implements ParkingProvider {

    private static final Logger log = LoggerFactory.getLogger(RoutingParkingProvider.class);

    private final Map<String, ParkingSource> sourcesByCity = new HashMap<>();

    public RoutingParkingProvider(ParkingProperties properties, List<ParkingProviderFactory> factories) {
        Map<String, ParkingProviderFactory> byFormat = factories.stream()
                .collect(Collectors.toMap(ParkingProviderFactory::format, Function.identity()));

        for (TenantProperties tenant : properties.tenants()) {
            ParkingProviderFactory factory = byFormat.get(tenant.format());

            if (factory == null) {
                throw new IllegalStateException("Format inconnu '" + tenant.format()
                        + "' pour le tenant " + tenant.id() + " ; formats disponibles : " + byFormat.keySet());
            }

            sourcesByCity.put(tenant.id(), new CachingParkingSource(factory.create(tenant), properties.cacheTtl()));
            log.info("Source chargée : {} format={} url={}", tenant.id(), tenant.format(), tenant.url());
        }
    }

    @Override
    public List<Parking> findAll(City city) {
        ParkingSource source = sourcesByCity.get(city.id());

        if (source == null) {
            throw new IllegalStateException("Aucune source configurée pour la ville " + city.id());
        }

        return source.findAll();
    }
}
