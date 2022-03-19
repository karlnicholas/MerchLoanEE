package com.github.karlnicholas.merchloan.client.process;

import java.time.LocalDate;

public class BusinessDateMonitor {
    private LocalDate businessDate;

    public synchronized void newDate(LocalDate businessDate) {
        this.businessDate = businessDate;
        notifyAll();
    }

    public LocalDate retrieveDate() {
        return businessDate;
    }

}
