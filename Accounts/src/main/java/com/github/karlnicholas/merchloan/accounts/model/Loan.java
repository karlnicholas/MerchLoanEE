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
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @ManyToOne
    private Account account;
    private LocalDate startDate;

}
