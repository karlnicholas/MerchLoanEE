package com.github.karlnicholas.merchloan.accounts.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@Table(indexes = {
//        @Index(columnList = "loanId"),
//        @Index(columnList = "rowNum"),
//        @Index(unique = true, columnList = "loanId, rowNum")
//})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterEntry {
    enum CREDITDEBIT {CREDIT, DEBIT}
//    @Id
//    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
//    @Column(columnDefinition = "BINARY(16)")
    private UUID loanId;
    private LocalDate date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
    private LocalDateTime timeStamp;
}
