package com.github.karlnicholas.merchloan.businessdate.businessdate.repository;

import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessDateRepository extends JpaRepository<BusinessDate, Long> {
}
