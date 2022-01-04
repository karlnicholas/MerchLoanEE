package com.github.karlnicholas.merchloan.register.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(indexes = {
        @Index(columnList = "loanId"),
        @Index(columnList = "rowNum"),
        @Index(unique = true, columnList = "loanId, rowNum")
})
public class RegisterEntry {
    enum CREDITDEBIT {CREDIT, DEBIT}
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(columnDefinition = "BINARY(16)")
    private UUID loanId;
    private Integer rowNum;
    private LocalDate date;
    private String description;
    private BigDecimal debit;
    private BigDecimal credit;
}
