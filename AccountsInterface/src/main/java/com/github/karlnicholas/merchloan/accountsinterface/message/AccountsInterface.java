package com.github.karlnicholas.merchloan.accountsinterface.message;

import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AccountsInterface {
    StatementHeader receivedStatementHeaderMessage(StatementHeader statementHeader);

    List<BillingCycle> receivedLoansToCycleMessage(LocalDate businessDate);

    RegisterEntryMessage receivedBillingCycleChargeMessage(BillingCycleCharge billingCycleCharge);

    String receivedQueryAccountIdMessage(UUID id);

    String receivedQueryLoanIdMessage(UUID id);
}
