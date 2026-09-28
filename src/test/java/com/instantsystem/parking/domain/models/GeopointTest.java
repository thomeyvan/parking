package com.instantsystem.parking.domain.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class GeopointTest {

    @Test
    void rejectsOutOfRangeCoordinates() {
        assertThatThrownBy(() -> new Geopoint(91, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("latitude hors bornes : 91.0");
        assertThatThrownBy(() -> new Geopoint(0, 181))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("longitude hors bornes : 181.0");
    }

    @Test
    void distanceToItselfReturnZero() {
        Geopoint position = new Geopoint(46.58, 0.34);
        assertThat(position.distanceToOtherPoint(position)).isZero();
    }

    @Test
    void oneDegreeOfLatitudeIsAboutOneHundredElevenKilometers() {
        Geopoint position1 = new Geopoint(0, 0);
        Geopoint position2 = new Geopoint(1, 0);
        assertThat(position1.distanceToOtherPoint(position2)).isCloseTo(111_195d, within(100d));
    }

    @Test
    void distanceIsSymmetric() {
        var hotelDeVille = new Geopoint(46.5793235337795, 0.3385507838016221);
        var theatre = new Geopoint(46.58383455409422, 0.33779491061805567);
        assertThat(hotelDeVille.distanceToOtherPoint(theatre)).isCloseTo(theatre.distanceToOtherPoint(hotelDeVille), within(0.001));
    }

}