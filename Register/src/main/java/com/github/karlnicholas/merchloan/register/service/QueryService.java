package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import com.github.karlnicholas.merchloan.register.repository.RegisterEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class QueryService {
    private final RegisterEntryRepository registerEntryRepository;

    @Autowired
    public QueryService(RegisterEntryRepository registerEntryRepository) {
        this.registerEntryRepository = registerEntryRepository;
    }

    public List<RegisterEntry> queryRegisterByLoanId(UUID id) {
        return registerEntryRepository.queryByLoanIdOrderByRowNum(id);
    }
}
