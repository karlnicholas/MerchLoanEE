package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.*;
import javax.inject.Inject;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
@Remote(AccountsEjb.class)
@Slf4j
@TransactionManagement(TransactionManagementType.BEAN)
public class AccountsEjbImpl implements AccountsEjb {
    @Inject
    private AccountManagementService accountManagementService;
    @Inject
    private RegisterManagementService registerManagementService;
    @Inject
    private QueryService queryService;
    private final ObjectMapper objectMapper;

    public AccountsEjbImpl() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public StatementHeader statementHeader(StatementHeader statementHeader) throws EJBException {
        try {
            log.debug("receivedStatementHeaderMessage {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            return statementHeader;
        } catch (Exception ex) {
            log.error("receivedStatementHeaderMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }

    @Override
    public List<BillingCycle> loansToCycle(LocalDate businessDate) throws EJBException {
        try {
            log.trace("receivedLoansToCycleMessage: {}", businessDate);
            return accountManagementService.loansToCycle(businessDate);
        } catch (Exception ex) {
            log.error("receivedLoansToCycleMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }

    @Override
    public RegisterEntryMessage billingCycleCharge(BillingCycleCharge billingCycleCharge) throws EJBException {
        try {
            log.debug("receivedBillingCycleChargeMessage: {}", billingCycleCharge);
            RegisterEntry re = registerManagementService.billingCycleCharge(billingCycleCharge);
            return RegisterEntryMessage.builder()
                    .date(re.getDate())
                    .credit(re.getCredit())
                    .debit(re.getDebit())
                    .description(re.getDescription())
                    .timeStamp(re.getTimeStamp())
                    .build();
        } catch (Exception ex) {
            log.error("receivedBillingCycleChargeMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }

    @Override
    public String queryAccountId(UUID id) throws EJBException {
        try {
            log.debug("receivedQueryAccountIdMessage: {}", id);
            Optional<Account> accountOpt = queryService.queryAccountId(id);
            if (accountOpt.isPresent()) {
                return objectMapper.writeValueAsString(accountOpt.get());
            } else {
                return "ERROR: id not found: " + id;
            }
        } catch (Exception ex) {
            log.error("receivedQueryAccountIdMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }

    @Override
    public String queryLoanId(UUID id) throws EJBException {
        try {
            log.debug("receivedQueryLoanIdMessage: {}", id);
            Optional<LoanDto> r = queryService.queryLoanId(id);
            if (r.isPresent()) {
                return objectMapper.writeValueAsString(r.get());
            } else {
                return "ERROR: Loan not found for id: " + id;
            }
        } catch ( SQLException | JsonProcessingException ex) {
            log.error("receivedQueryLoanIdMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }
}
