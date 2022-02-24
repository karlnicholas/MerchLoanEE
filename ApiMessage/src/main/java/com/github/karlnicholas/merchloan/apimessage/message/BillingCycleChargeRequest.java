package com.github.karlnicholas.merchloan.apimessage.message;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class BillingCycleChargeRequest implements ServiceRequestMessage {
    private LocalDate date;
    private UUID id;
    private DebitRequest debitRequest;
    private CreditRequest creditRequest;
}
