package com.github.karlnicholas.merchloan.client.process;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDate;

public class BusinessDateEvent extends ApplicationEvent {
    private LocalDate message;

    public BusinessDateEvent(Object source, LocalDate message) {
        super(source);
        this.message = message;
    }

    public LocalDate getMessage() {
        return message;
    }
}
