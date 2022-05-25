package com.github.karlnicholas.merchloan.servicerequest.component;

import jakarta.jms.Session;

import java.util.UUID;

@FunctionalInterface
public interface ExceptionFunction<T, R> {
    public R route(T t, Boolean retry, UUID existingId) throws ServiceRequestException;
}
