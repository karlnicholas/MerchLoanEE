package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.register.model.CreditEntry;
import com.github.karlnicholas.merchloan.register.model.DebitEntry;
import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import com.github.karlnicholas.merchloan.register.repository.CreditEntryRepository;
import com.github.karlnicholas.merchloan.register.repository.DebitEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class QueryService {
    private final CreditEntryRepository creditEntryRepository;
    private final DebitEntryRepository debitEntryRepository;

    @Autowired
    public QueryService(CreditEntryRepository creditEntryRepository, DebitEntryRepository debitEntryRepository) {
        this.creditEntryRepository = creditEntryRepository;
        this.debitEntryRepository = debitEntryRepository;
    }

    public List<? extends RegisterEntry> queryRegisterByLoanId(UUID id) {
        List<RegisterEntry> results = new ArrayList<>();
        List<CreditEntry> cs = creditEntryRepository.findAllByLoanId(id);
        results.addAll(cs);
        List<DebitEntry> ds = debitEntryRepository.findAllByLoanId(id);
        results.addAll(ds);
        return results;
    }
}
