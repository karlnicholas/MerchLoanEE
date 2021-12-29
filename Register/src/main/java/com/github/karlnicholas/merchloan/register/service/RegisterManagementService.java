package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.CreditLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.register.model.CreditEntry;
import com.github.karlnicholas.merchloan.register.model.DebitEntry;
import com.github.karlnicholas.merchloan.register.repository.CreditEntryRepository;
import com.github.karlnicholas.merchloan.register.repository.DebitEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RegisterManagementService {
    private final CreditEntryRepository creditEntryRepository;
    private final DebitEntryRepository debitEntryRepository;
    private final RabbitMqSender rabbitMqSender;

    public RegisterManagementService(CreditEntryRepository creditEntryRepository, DebitEntryRepository debitEntryRepository, RabbitMqSender rabbitMqSender) {
        this.creditEntryRepository = creditEntryRepository;
        this.debitEntryRepository = debitEntryRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public void debitLoan(DebitLoan debitLoan) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            DebitEntry debitEntry = DebitEntry.builder()
                    .id(debitLoan.getId())
                    .loanId(debitLoan.getLoanId())
                    .date(debitLoan.getDate())
                    .debit(debitLoan.getAmount())
                    .description(debitLoan.getDescription())
                    .build();
            debitEntryRepository.save(debitEntry);
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("void debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                    debitLoan.getRetryCount() == 0
                            ? ServiceRequestResponse.STATUS.FAILURE
                            : ServiceRequestResponse.STATUS.SUCCESS
            );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        rabbitMqSender.sendServiceRequest(serviceRequestResponse);
    }

    public void creditLoan(CreditLoan creditLoan) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(creditLoan.getId())
                .build();
        try {
            CreditEntry creditEntry = CreditEntry.builder()
                    .id(creditLoan.getId())
                    .loanId(creditLoan.getLoanId())
                    .date(creditLoan.getDate())
                    .credit(creditLoan.getAmount())
                    .description(creditLoan.getDescription())
                    .build();
            creditEntryRepository.save(creditEntry);
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("void creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                            creditLoan.getRetryCount() == 0
                                    ? ServiceRequestResponse.STATUS.FAILURE
                                    : ServiceRequestResponse.STATUS.SUCCESS
                    );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        rabbitMqSender.sendServiceRequest(serviceRequestResponse);
    }
}
