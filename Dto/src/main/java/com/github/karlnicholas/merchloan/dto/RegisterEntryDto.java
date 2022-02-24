package com.github.karlnicholas.merchloan.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class RegisterEntryDto implements Serializable {
    private Integer rowNum;
    private LocalDate date;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal debit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal credit;
    private BigDecimal balance;
}
