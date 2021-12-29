package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.CreditAccount;
import com.github.karlnicholas.merchloan.jmsmessage.DebitAccount;
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

    public void debitAccount(DebitAccount debitAccount) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(debitAccount.getId())
                .build();
        try {
            DebitEntry debitEntry = DebitEntry.builder()
                    .id(debitAccount.getId())
                    .loanId(debitAccount.getLoanId())
                    .date(debitAccount.getDate())
                    .debit(debitAccount.getAmount())
                    .description(debitAccount.getDescription())
                    .build();
            debitEntryRepository.save(debitEntry);
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("CreditEntry creditAccount(CreditAccount creditAccount) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                    debitAccount.getRetryCount() == 0
                            ? ServiceRequestResponse.STATUS.FAILURE
                            : ServiceRequestResponse.STATUS.SUCCESS
            );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        rabbitMqSender.sendServiceRequest(serviceRequestResponse);
    }

    public void creditAccount(CreditAccount creditAccount) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder()
                .id(creditAccount.getId())
                .build();
        try {
            CreditEntry creditEntry = CreditEntry.builder()
                    .id(creditAccount.getId())
                    .loanId(creditAccount.getLoanId())
                    .date(creditAccount.getDate())
                    .credit(creditAccount.getAmount())
                    .description(creditAccount.getDescription())
                    .build();
            creditEntryRepository.save(creditEntry);
            serviceRequestResponse.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequestResponse.setStatusMessage("Success");
        } catch (DuplicateKeyException dke) {
            log.warn("CreditEntry creditAccount(CreditAccount creditAccount) duplicate key: {}", dke.getMessage());
            serviceRequestResponse.setStatus(
                            creditAccount.getRetryCount() == 0
                                    ? ServiceRequestResponse.STATUS.FAILURE
                                    : ServiceRequestResponse.STATUS.SUCCESS
                    );
            serviceRequestResponse.setStatusMessage(dke.getMessage());
        }
        rabbitMqSender.sendServiceRequest(serviceRequestResponse);
    }
}
