package com.instantsystem.parking.config;

import com.instantsystem.parking.domain.port.in.FindNearbyParkingsUseCase;
import com.instantsystem.parking.domain.port.out.CityCatalog;
import com.instantsystem.parking.domain.port.out.ParkingProvider;
import com.instantsystem.parking.domain.services.FindNearbyParkingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Câblage : le seul endroit où Spring "connaît" les services du domaine,
 * qui restent eux-mêmes sans annotation.
 */
@Configuration
public class DomainConfig {

    @Bean
    FindNearbyParkingsUseCase findNearbyParkingsUseCase(CityCatalog cityCatalog, ParkingProvider parkingProvider) {
        return new FindNearbyParkingsService(cityCatalog, parkingProvider);
    }

    /** Client HTTP partagé par tous les tenants ; l'URL est portée par chaque provider. */
    @Bean
    RestClient parkingRestClient(RestClient.Builder builder, ParkingProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());
        return builder.requestFactory(factory).build();
    }
}
