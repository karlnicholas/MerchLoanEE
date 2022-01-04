package com.github.karlnicholas.merchloan.register.repository;

import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

@Repository
public interface RegisterEntryRepository extends JpaRepository<RegisterEntry, UUID> {
    List<RegisterEntry> queryByLoanIdOrderByRowNum(UUID id);
}
