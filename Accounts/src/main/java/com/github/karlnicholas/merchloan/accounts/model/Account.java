package com.github.karlnicholas.merchloan.accounts.model;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private UUID id;
    private String customer;
    private LocalDate createDate;
}
