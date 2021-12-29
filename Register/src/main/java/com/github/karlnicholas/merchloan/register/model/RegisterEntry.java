package com.github.karlnicholas.merchloan.register.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class RegisterEntry {
    @Id
    private UUID id;
    private UUID loanId;
    private LocalDate date;
    private String description;
}
