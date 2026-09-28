package com.instantsystem.parking.adapters.out.portail;

import com.instantsystem.parking.domain.models.Parking;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OpenDataParkingMapperTest {

    private static OpenDataParkingResponse response;
    private final OpenDataParkingMapper mapper = new OpenDataParkingMapper();

    @BeforeAll
    static void loadFixture() throws Exception {
        var jsonMapper = JsonMapper.builder().build();
        try (InputStream in = OpenDataParkingMapperTest.class.getResourceAsStream("/lines.json")) {
            response = jsonMapper.readValue(in, OpenDataParkingResponse.class);
        }
    }

    @Test
    void fixtureIsDeserialised() {
        assertThat(response.total()).isEqualTo(9);
        assertThat(response.results()).hasSize(9);
    }

    @Test
    void mapsARecordWithCoordinates() {
        var theatre = response.results().stream().filter(r -> "THEATRE".equals(r.nom())).findFirst().orElseThrow();

        Optional<Parking> parking = mapper.toDomain(theatre);

        assertThat(parking).isPresent();
        assertThat(parking.get().id()).isEqualTo("3");
        assertThat(parking.get().name()).isEqualTo("THEATRE");
        assertThat(parking.get().capacity()).isEqualTo(320);
        assertThat(parking.get().availablePlaces()).isEqualTo(34);
        assertThat(parking.get().position().latitude()).isEqualTo(46.58383455409422);
        assertThat(parking.get().position().longitude()).isEqualTo(0.33779491061805567);
        assertThat(parking.get().updatedAt()).isEqualTo(Instant.parse("2026-09-25T12:26:48.000Z"));
    }

    @Test
    void skipsRecordsWithoutCoordinates() {
        List<String> skipped = response.results().stream()
                .filter(r -> mapper.toDomain(r).isEmpty())
                .map(OpenDataParkingResponse.Record::nom)
                .toList();

        assertThat(skipped).containsExactlyInAnyOrder("GARE EFFIA", "CORDELIERS");
    }

    @Test
    void mapsSevenOfNineRecords() {
        long mapped = response.results().stream().map(mapper::toDomain).filter(Optional::isPresent).count();

        assertThat(mapped).isEqualTo(7);
    }

    @Test
    void capsAvailableSpotsAtCapacity() {
        var inconsistent = new OpenDataParkingResponse.Record(42, "TEST", 100, 150, "46.58, 0.34", null);

        assertThat(mapper.toDomain(inconsistent)).get()
                .extracting(Parking::availablePlaces).isEqualTo(100);
    }
}