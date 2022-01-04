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
public class Account {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String customer;
    private LocalDate createDate;

}
