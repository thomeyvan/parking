package com.instantsystem.parking.adapters.out.portail;

import com.instantsystem.parking.adapters.out.ParkingProviderFactory;
import com.instantsystem.parking.adapters.out.ParkingSource;
import com.instantsystem.parking.config.ParkingProperties.TenantProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenDataParkingSourceFactory implements ParkingProviderFactory {
    public static final String FORMAT = "ods-parking";

    private final RestClient restClient;

    public OpenDataParkingSourceFactory(RestClient parkingRestClient) {
        this.restClient = parkingRestClient;
    }

    @Override
    public String format() {
        return FORMAT;
    }

    @Override
    public ParkingSource create(TenantProperties tenant) {
        return new OpenDataParkingSource(restClient, tenant.url());
    }
}
