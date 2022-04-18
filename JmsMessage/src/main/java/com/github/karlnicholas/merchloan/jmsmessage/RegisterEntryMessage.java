package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegisterEntryMessage implements Serializable {
    private LocalDate date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private BigDecimal balance;
    private LocalDateTime timeStamp;
}
