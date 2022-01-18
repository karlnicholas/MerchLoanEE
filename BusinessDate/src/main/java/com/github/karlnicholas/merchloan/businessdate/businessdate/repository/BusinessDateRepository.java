package com.github.karlnicholas.merchloan.businessdate.businessdate.repository;

import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BusinessDateRepository extends JpaRepository<BusinessDate, Long> {
}
