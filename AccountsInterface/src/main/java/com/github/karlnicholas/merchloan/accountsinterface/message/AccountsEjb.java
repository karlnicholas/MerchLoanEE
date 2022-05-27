package com.github.karlnicholas.merchloan.accountsinterface.message;

import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;

import javax.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Remote
public interface AccountsEjb {
    StatementHeader statementHeader(StatementHeader statementHeader) throws Throwable;

    List<BillingCycle> loansToCycle(LocalDate businessDate) throws Throwable;

    RegisterEntryMessage billingCycleCharge(BillingCycleCharge billingCycleCharge) throws Throwable;

    String queryAccountId(UUID id) throws Throwable;

    String queryLoanId(UUID id) throws Throwable;
}
