package com.instantsystem.parking.adapters.out;

import com.instantsystem.parking.config.ParkingProperties.TenantProperties;

/**
 * Point d'extension côté infrastructure : chaque format de source de données
 * fournit une fabrique. Le routeur choisit la fabrique par le champ
 * "format" de la configuration du tenant.
 */
public interface ParkingProviderFactory {

    String format();

    ParkingSource create(TenantProperties tenant);
}
