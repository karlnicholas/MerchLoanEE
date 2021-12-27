package com.github.karlnicholas.merchloan.accounts.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    private UUID id;
    @ManyToOne
    private Account account;
    @ManyToOne
    private Lender lender;
    private LocalDate startDate;

}
