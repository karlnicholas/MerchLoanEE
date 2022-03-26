package com.github.karlnicholas.merchloan.servicerequest.component;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

@FunctionalInterface
public interface ExceptionFunction<T, R> {
    public R route(T t, Boolean retry, UUID existingId) throws JsonProcessingException;
}
