package com.instantsystem.parking.adapters.out.portail;

import com.instantsystem.parking.adapters.out.ParkingSource;
import com.instantsystem.parking.domain.exception.ParkingProviderException;
import com.instantsystem.parking.domain.models.Parking;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

public class OpenDataParkingSource implements ParkingSource {

    private final RestClient restClient;
    private final String url;
    private final OpenDataParkingMapper mapper = new OpenDataParkingMapper();

    public OpenDataParkingSource(RestClient restClient, String url) {
        this.restClient = restClient;
        this.url = url;
    }

    @Override
    public List<Parking> findAll() {
        OpenDataParkingResponse response;

        try {
            response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(OpenDataParkingResponse.class);
        } catch (RestClientException e) {
            throw new ParkingProviderException("Echec de l'API URL: " + url, e);
        }

        if (response == null || response.results() == null) {
            throw new ParkingProviderException("Réponse vide de la source " + url, null);
        }

        return response.results().stream()
                .map(mapper::toDomain)
                .flatMap(Optional::stream)
                .toList();
    }
}
