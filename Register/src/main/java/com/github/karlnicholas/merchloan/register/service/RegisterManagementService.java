package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.jmsmessage.CreditLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.register.model.LoanState;
import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import com.github.karlnicholas.merchloan.register.repository.LoanStateRepository;
import com.github.karlnicholas.merchloan.register.repository.RegisterEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class RegisterManagementService {
    private final RegisterEntryRepository registerEntryRepository;
    private final LoanStateRepository loanStateRepository;

    public RegisterManagementService(RegisterEntryRepository registerEntryRepository, LoanStateRepository loanStateRepository) {
        this.registerEntryRepository = registerEntryRepository;
        this.loanStateRepository = loanStateRepository;
    }

    public ServiceRequestResponse fundLoan(DebitLoan fundLoan) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
        //TODO: Better logic here
        try {
            loanStateRepository.save(
                    LoanState.builder()
                            .loanId(fundLoan.getLoanId())
                            .startDate(fundLoan.getDate())
                            .balance(fundLoan.getAmount())
                            .currentRowNum(1)
                            .build());
            registerEntryRepository.save(RegisterEntry.builder()
                    .id(fundLoan.getId())
                    .loanId(fundLoan.getLoanId())
                    .date(fundLoan.getDate())
                    .debit(fundLoan.getAmount())
                    .description(fundLoan.getDescription())
                    .rowNum(1)
                    .build());
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse createLoan(CreateLoan createLoan) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                    fundLoan.getRetryCount() == 0
                            ? ServiceRequestResponse.STATUS.FAILURE
                            : ServiceRequestResponse.STATUS.SUCCESS
            );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        return serviceRequestResponse;
    }

    public ServiceRequestResponse debitLoan(DebitLoan debitLoan) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            RegisterEntry debitEntry = RegisterEntry.builder()
                    .id(debitLoan.getId())
                    .loanId(debitLoan.getLoanId())
                    .date(debitLoan.getDate())
                    .debit(debitLoan.getAmount())
                    .description(debitLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(debitLoan.getLoanId());
            if ( loanStateOpt.isPresent() ) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum()+1);
                loanState.setBalance(loanState.getBalance().add(debitEntry.getDebit()));
                debitEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryRepository.save(debitEntry);
                loanStateRepository.save(loanState);
            }
            log.info("debit done sleeping");
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                    debitLoan.getRetryCount() == 0
                            ? ServiceRequestResponse.STATUS.FAILURE
                            : ServiceRequestResponse.STATUS.SUCCESS
            );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        ThreadSleep(9000);
        return serviceRequestResponse;
    }

    public ServiceRequestResponse creditLoan(CreditLoan creditLoan) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(creditLoan.getId())
                .build();
        try {
            RegisterEntry creditEntry = RegisterEntry.builder()
                    .id(creditLoan.getId())
                    .loanId(creditLoan.getLoanId())
                    .date(creditLoan.getDate())
                    .credit(creditLoan.getAmount())
                    .description(creditLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(creditLoan.getLoanId());
            if ( loanStateOpt.isPresent() ) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum()+1);
                loanState.setBalance(loanState.getBalance().subtract(creditEntry.getCredit()));
                creditEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryRepository.save(creditEntry);
                loanStateRepository.save(loanState);
            }
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                    creditLoan.getRetryCount() == 0
                            ? ServiceRequestResponse.STATUS.FAILURE
                            : ServiceRequestResponse.STATUS.SUCCESS
            );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        return serviceRequestResponse;
    }
    private void ThreadSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
