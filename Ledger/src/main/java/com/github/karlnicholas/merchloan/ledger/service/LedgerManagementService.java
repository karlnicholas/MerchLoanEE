package com.github.karlnicholas.merchloan.ledger.service;

import com.github.karlnicholas.merchloan.jmsmessage.CreditToLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitFromLoan;
import com.github.karlnicholas.merchloan.ledger.model.CreditEntry;
import com.github.karlnicholas.merchloan.ledger.model.DebitEntry;
import com.github.karlnicholas.merchloan.ledger.repository.CreditEntryRepository;
import com.github.karlnicholas.merchloan.ledger.repository.DebitEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LedgerManagementService {
    private final CreditEntryRepository creditEntryRepository;
    private final DebitEntryRepository debitEntryRepository;

    public LedgerManagementService(CreditEntryRepository creditEntryRepository, DebitEntryRepository debitEntryRepository) {
        this.creditEntryRepository = creditEntryRepository;
        this.debitEntryRepository = debitEntryRepository;
    }

    public DebitEntry debitFromLoan(DebitFromLoan debitFromLoan) {
        DebitEntry debitEntry = DebitEntry.builder()
                .id(debitFromLoan.getId())
                .loanId(debitFromLoan.getLoanId())
                .date(debitFromLoan.getDate())
                .amount(debitFromLoan.getAmount())
                .build();
        try {
            return debitEntryRepository.save(debitEntry);
        } catch ( DuplicateKeyException dke) {
            log.warn("DebitEntry debitFromLoan(DebitFromLoan debitFromLoan) duplicate key: {}", dke.getMessage());
            return debitEntry;
        }
    }
    public CreditEntry creditToLoan(CreditToLoan creditToLoan) {
        CreditEntry creditEntry = CreditEntry.builder()
                .id(creditToLoan.getId())
                .loanId(creditToLoan.getLoanId())
                .date(creditToLoan.getDate())
                .amount(creditToLoan.getAmount())
                .build();
        try {
            return creditEntryRepository.save(creditEntry);
        } catch ( DuplicateKeyException dke) {
            log.warn("CreditEntry creditToLoan(CreditToLoan creditToLoan) duplicate key: {}", dke.getMessage());
            return creditEntry;
        }
    }
}
