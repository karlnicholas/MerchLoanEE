package com.github.karlnicholas.merchloan.servicerequest.component;

public class ServiceRequestException extends Exception {
    public ServiceRequestException(String message) {
        super(message);
    }
    ServiceRequestException(String message, Throwable throwable) {
        super(message, throwable);
    }
    public ServiceRequestException(Throwable throwable) {
        super(throwable);
    }
}
