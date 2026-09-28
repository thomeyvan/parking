package com.instantsystem.parking.adapters.out.portail;

import com.instantsystem.parking.domain.exception.ParkingProviderException;
import com.instantsystem.parking.domain.models.Parking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenDataParkingSourceTest {

    private static final String URL = "https://data.grandpoitiers.fr/data-fair/api/v1/datasets/mobilites-stationnement-des-parkings-en-temps-reel/lines";

    private MockRestServiceServer server;
    private OpenDataParkingSource provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new OpenDataParkingSource(builder.build(), URL);
    }

    @Test
    void returnsMappedParkingsFromRealPayload() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess(new ClassPathResource("/lines.json"), MediaType.APPLICATION_JSON));

        List<Parking> parkings = provider.findAll();

        assertThat(parkings).hasSize(7);
        assertThat(parkings).extracting(Parking::name).contains("THEATRE", "HOTEL DE VILLE", "BLOSSAC TISON");
        server.verify();
    }

    @Test
    void wrapsHttpErrorsInDomainException() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(provider::findAll).isInstanceOf(ParkingProviderException.class);
    }

    @Test
    void rejectsPayloadWithoutResults() {
        server.expect(requestTo(URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(provider::findAll).isInstanceOf(ParkingProviderException.class);
    }
}