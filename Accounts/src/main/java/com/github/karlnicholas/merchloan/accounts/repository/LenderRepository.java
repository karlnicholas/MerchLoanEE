package com.github.karlnicholas.merchloan.accounts.repository;

import com.github.karlnicholas.merchloan.accounts.model.Lender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LenderRepository extends JpaRepository<Lender, UUID> {
    Optional<Lender> findByLender(String lender);
}
