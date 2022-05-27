package com.github.karlnicholas.merchloan.accountsinterface.message;

import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;

import javax.ejb.EJBException;
import javax.ejb.Remote;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Remote
public interface AccountsEjb {
    StatementHeader statementHeader(StatementHeader statementHeader) throws EJBException;

    List<BillingCycle> loansToCycle(LocalDate businessDate) throws EJBException;

    RegisterEntryMessage billingCycleCharge(BillingCycleCharge billingCycleCharge) throws EJBException;

    String queryAccountId(UUID id) throws EJBException;

    String queryLoanId(UUID id) throws EJBException;
}
