package com.github.karlnicholas.merchloan.jms.message;

public class RabbitMqSenderOrig {
//    private final RabbitMqProperties rabbitMqProperties;
//
//    @Autowired
//    public RabbitMqSenderOrig(RabbitMqProperties rabbitMqProperties) {
//        this.rabbitMqProperties = rabbitMqProperties;
//    }
//
//    public void accountCreateAccount(CreateAccount createAccount) {
//        log.debug("accountCreateAccount: {}", createAccount);
//        BasicProperties basicProperties = new BasicProperties();
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCreateaccountRoutingKey(), null, createAccount);
//    }
//
//    public void accountFundLoan(FundLoan fundLoan) {
//        log.debug("accountFundLoan: {}", fundLoan);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountFundingRoutingKey(), fundLoan);
//    }
//
//    public void accountValidateCredit(CreditLoan creditLoan) {
//        log.debug("accountValidateCredit: {}", creditLoan);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateCreditRoutingkey(), creditLoan);
//    }
//
//    public void accountValidateDebit(DebitLoan debitLoan) {
//        log.debug("accountValidateDebit: {}", debitLoan);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountValidateDebitRoutingkey(), debitLoan);
//    }
//
//    public Object accountBillingCycleCharge(BillingCycleCharge billingCycleCharge) {
//        log.debug("accountBillingCycleCharge: {}", billingCycleCharge);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountBillingCycleChargeRoutingKey(), billingCycleCharge);
//    }
//
//    public Object queryAccount(UUID id) {
//        log.debug("queryAccount: {}", id);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryAccountIdRoutingKey(), id);
//    }
//
//    public Object accountQueryStatementHeader(StatementHeader statementHeader) {
//        log.debug("accountQueryStatementHeader: {}", statementHeader);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryStatementHeaderRoutingKey(), statementHeader);
//    }
//
//    public void statementStatement(StatementHeader statementHeader) {
//        log.debug("statementStatement: {}", statementHeader);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementStatementRoutingkey(), statementHeader);
//    }
//
//    public void serviceRequestServiceRequest(ServiceRequestResponse serviceRequest) {
//        log.debug("serviceRequestServiceRequest: {}", serviceRequest);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestRoutingkey(), serviceRequest);
//    }
//
//    public Object queryServiceRequest(UUID id) {
//        log.debug("queryServiceRequest: {}", id);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getServicerequestQueryIdRoutingkey(), id);
//    }
//
//    public Object queryLoan(UUID id) {
//        log.debug("queryLoan: {}", id);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoanIdRoutingKey(), id);
//    }
//
//    public Object queryStatement(UUID id) {
//        log.debug("queryStatement: {}", id);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementRoutingkey(), id);
//    }
//
//    public Object queryStatements(UUID id) {
//        log.debug("queryStatements: {}", id);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryStatementsRoutingkey(), id);
//    }
//
//    public Object queryMostRecentStatement(UUID loanId) {
//        log.debug("queryMostRecentStatement: {}", loanId);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementQueryMostRecentStatementRoutingkey(), loanId);
//    }
//
//    public Object servicerequestCheckRequest() {
//        log.debug("servicerequestCheckRequest:");
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestCheckRequestRoutingkey(), new Message(new byte[0]));
//    }
//
//    public Object acccountQueryLoansToCycle(LocalDate businessDate) {
//        log.debug("acccountQueryLoansToCycle: {}", businessDate);
//        return rabbitTemplate.convertSendAndReceive(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountQueryLoansToCycleRoutingkey(), businessDate);
//    }
//
//    public void serviceRequestBillLoan(BillingCycle billingCycle) {
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanRoutingkey(), billingCycle);
//    }
//
//    public void accountCloseLoan(CloseLoan closeLoan) {
//        log.debug("accountCloseLoan: {}", closeLoan);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountCloseLoanRoutingkey(), closeLoan);
//    }
//
//    public void statementCloseStatement(StatementHeader statementHeader) {
//        log.debug("statementCloseStatement: {}", statementHeader);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getStatementCloseStatementRoutingkey(), statementHeader);
//    }
//
//    public void accountLoanClosed(StatementHeader statementHeader) {
//        log.debug("accountLoanClosed: {}", statementHeader);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getAccountLoanClosedRoutingkey(), statementHeader);
//    }
//
//    public void serviceRequestStatementComplete(StatementCompleteResponse requestResponse) {
//        log.debug("serviceRequestStatementComplete: {}", requestResponse);
//        rabbitTemplate.convertAndSend(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestStatementCompleteRoutingkey(), requestResponse);
//    }
//
}
