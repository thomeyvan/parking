package com.instantsystem.parking.domain.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZoneTest {

    private final Zone poitiers = new Zone(46.3051556, 46.7852034, -0.0471733, 0.7745855);

    @Test
    void containsPositionInside() {
        assertThat(poitiers.contains(new Geopoint(46.58, 0.34))).isTrue();
    }

    @Test
    void excludesPositionOutside() {
        assertThat(poitiers.contains(new Geopoint(46.19, 6.23))).isFalse();
    }

    @Test
    void rejectsInvertedZones() {
        assertThatThrownBy(() -> new Zone(47, 46, 0, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Périmètre géographique invalide");
    }
}