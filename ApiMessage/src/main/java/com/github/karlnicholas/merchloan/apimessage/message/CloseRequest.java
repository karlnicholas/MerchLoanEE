package com.github.karlnicholas.merchloan.apimessage.message;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CloseRequest implements ServiceRequestMessage {
    private UUID loanId;
    private BigDecimal amount;
    private String description;
}
