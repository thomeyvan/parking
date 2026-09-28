package com.instantsystem.parking.domain.exception;

/**
 * Exception qui se produit lorsque le fournisseur de parkings ne peut pas fournir les données demandées.
 */
public class ParkingProviderException extends RuntimeException {

    public ParkingProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
