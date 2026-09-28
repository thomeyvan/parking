package com.instantsystem.parking.domain.exception;

/**
 * Exception qui se produit lorsque la ville demandée est inconnue.
 */
public class UnknownCityException extends RuntimeException {

    public UnknownCityException(String message) {
        super(message);
    }
}
