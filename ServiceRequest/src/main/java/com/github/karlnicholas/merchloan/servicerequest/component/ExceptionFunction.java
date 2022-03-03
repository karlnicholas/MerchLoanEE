package com.github.karlnicholas.merchloan.servicerequest.component;

import com.fasterxml.jackson.core.JsonProcessingException;

@FunctionalInterface
public interface ExceptionFunction<T, R> {
    public R route(T t, Boolean retry) throws JsonProcessingException;
}
