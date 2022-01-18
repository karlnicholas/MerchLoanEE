package com.github.karlnicholas.merchloan.businessdate.businessdate.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDate {
    @Id
    private Long id;
    private LocalDate businessDate;
}
