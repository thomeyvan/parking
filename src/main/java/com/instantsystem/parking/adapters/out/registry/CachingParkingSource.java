package com.instantsystem.parking.adapters.out.registry;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.instantsystem.parking.adapters.out.ParkingSource;
import com.instantsystem.parking.domain.models.Parking;

import java.time.Duration;
import java.util.List;

/**
 * Décorateur : met en cache la réponse d'une source pendant une durée courte.
 * Un cache par tenant, donc les villes ne se mélangent jamais.
 */
class CachingParkingSource implements ParkingSource {
    private static final String KEY = "CACHE_PARKING_KEY";

    private final LoadingCache<String, List<Parking>> cache;

    CachingParkingSource(ParkingSource delegate, Duration ttl) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .build(key -> delegate.findAll());
    }

    @Override
    public List<Parking> findAll() {
        return cache.get(KEY);
    }
}
