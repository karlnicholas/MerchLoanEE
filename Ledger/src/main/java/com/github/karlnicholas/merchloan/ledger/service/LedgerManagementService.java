package com.github.karlnicholas.merchloan.ledger.service;

import com.github.karlnicholas.merchloan.jmsmessage.CreditAccount;
import com.github.karlnicholas.merchloan.jmsmessage.DebitAccount;
import com.github.karlnicholas.merchloan.ledger.model.CreditEntry;
import com.github.karlnicholas.merchloan.ledger.model.DebitEntry;
import com.github.karlnicholas.merchloan.ledger.repository.CreditEntryRepository;
import com.github.karlnicholas.merchloan.ledger.repository.DebitEntryRepository;
import lombok.extern.slf4j.Slf4j;
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

    public DebitEntry debitAccount(DebitAccount debitAccount) {
        DebitEntry debitEntry = DebitEntry.builder()
                .id(debitAccount.getId())
                .loanId(debitAccount.getLoanId())
                .date(debitAccount.getDate())
                .debit(debitAccount.getAmount())
                .build();
        return debitEntryRepository.save(debitEntry);
    }

    public CreditEntry creditAccount(CreditAccount creditAccount) {
        CreditEntry creditEntry = CreditEntry.builder()
                .id(creditAccount.getId())
                .loanId(creditAccount.getLoanId())
                .date(creditAccount.getDate())
                .credit(creditAccount.getAmount())
                .build();
        return creditEntryRepository.save(creditEntry);
    }
}
