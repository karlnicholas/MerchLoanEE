package com.github.karlnicholas.merchloan.apimessage.message;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class FundingRequest implements ServiceRequestMessage {
    private UUID accountId;
    private BigDecimal amount;
    private String description;
}
