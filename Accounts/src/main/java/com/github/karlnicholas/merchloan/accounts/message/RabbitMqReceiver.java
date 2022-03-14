package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.accounts.service.RegisterManagementService;
import com.github.karlnicholas.merchloan.apimessage.message.BillingCycleChargeRequest;
import com.github.karlnicholas.merchloan.apimessage.message.CreditRequest;
import com.github.karlnicholas.merchloan.apimessage.message.DebitRequest;
import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.accounts.service.AccountManagementService;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final AccountManagementService accountManagementService;
    private final RegisterManagementService registerManagementService;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;
    private final RedisComponent redisComponent;
    private final RabbitMqSender rabbitMqSender;


    public RabbitMqReceiver(AccountManagementService accountManagementService, RegisterManagementService registerManagementService, QueryService queryService, RedisComponent redisComponent, RabbitMqSender rabbitMqSender) {
        this.accountManagementService = accountManagementService;
        this.registerManagementService = registerManagementService;
        this.queryService = queryService;
        this.redisComponent = redisComponent;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        // No need for implementation
    }

    @RabbitListener(queues = "${rabbitmq.account.createaccount.queue}")
    public void receivedCreateAccountMessage(CreateAccount createAccount) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            log.info("CreateAccount Details Received is.. {}", createAccount);
            accountManagementService.createAccount(createAccount, requestResponse);
        } catch (Exception ex) {
            log.error("void receivedCreateAccountMessage(CreateAccount createAccount) {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        } finally {
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.funding.queue}")
    public void receivedFundingMessage(FundLoan fundLoan) {
        // M= P [r (1+r)^n/ ((1+r)^n)-1)]
        // r = .10 / 12 = 0.00833
        // 10000 * 0.00833(1.00833)^12 / ((1.00833)^12)-1]
        // 10000 * 0.0092059/0.104713067
        // 10000 * 0.08791548
        // = 879.16
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
        try {
            log.info("FundLoan Received {} ", fundLoan);
            accountManagementService.fundAccount(fundLoan, requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.fundLoan(
                        DebitLoan.builder()
                                .id(fundLoan.getId())
                                .amount(fundLoan.getAmount())
                                .date(fundLoan.getStartDate())
                                .loanId(fundLoan.getId())
                                .description(fundLoan.getDescription())
                                .build(),
                        requestResponse);
            }
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        } catch (Exception ex) {
            log.error("void receivedFundingMessage(FundLoan funding) {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.validate.credit.queue}")
    public void receivedValidateCreditMessage(CreditLoan creditLoan) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(creditLoan.getId())
                .build();
        try {
            log.info("CreditLoan Received {} ", creditLoan);
            accountManagementService.validateLoan(creditLoan.getLoanId(), requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.creditLoan(CreditLoan.builder()
                        .id(creditLoan.getId())
                        .amount(creditLoan.getAmount())
                        .date(creditLoan.getDate())
                        .loanId(creditLoan.getLoanId())
                        .description(creditLoan.getDescription())
                        .build(), requestResponse);
            }
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        } catch (Exception ex) {
            log.error("void receivedValidateCreditMessage(CreditLoan creditLoan) {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.validate.debit.queue}")
    public void receivedValidateDebitMessage(DebitLoan debitLoan) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            log.info("DebitLoan Received {} ", debitLoan);
            accountManagementService.validateLoan(debitLoan.getLoanId(), requestResponse);
            if (requestResponse.isSuccess()) {
                registerManagementService.debitLoan(DebitLoan.builder()
                                .id(debitLoan.getId())
                                .amount(debitLoan.getAmount())
                                .date(debitLoan.getDate())
                                .loanId(debitLoan.getLoanId())
                                .description(debitLoan.getDescription())
                                .build(),
                        requestResponse);
            }
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        } catch (Exception ex) {
            log.error("void receivedValidateDebitMessage(DebitLoan debitLoan) {}", ex.getMessage());
            requestResponse.setError(ex.getMessage());
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.closeloan.queue}")
    public void receivedCloseLoanMessage(CloseLoan closeLoan) {
        ServiceRequestResponse serviceRequestResponse = ServiceRequestResponse.builder().id(closeLoan.getId()).build();
        try {
            log.info("CloseLoan Received {} ", closeLoan);
            Optional<LoanDto> loanOpt = queryService.queryLoanId(closeLoan.getLoanId());
            if (loanOpt.isPresent()) {
                if (closeLoan.getAmount().compareTo(loanOpt.get().getPayoffAmount()) == 0) {
                    rabbitMqSender.serviceRequestBillingCycleCharge(BillingCycleChargeRequest.builder()
                            .id(closeLoan.getInterestChargeId())
                            .date(closeLoan.getDate())
                            .debitRequest(new DebitRequest(closeLoan.getLoanId(), loanOpt.get().getCurrentInterest(), "Interest"))
                            .build()
                    );
                    // determine interest balance
                    rabbitMqSender.serviceRequestBillingCycleCharge(BillingCycleChargeRequest.builder()
                            .id(closeLoan.getPaymentId())
                            .date(closeLoan.getDate())
                            .creditRequest(new CreditRequest(closeLoan.getLoanId(), closeLoan.getAmount(), "Payoff Payment"))
                            .build()
                    );
                    // wait for responses
                    int responseCount = 2;
                    int sixtySeconds = 120;
                    while (sixtySeconds > 0) {
                        sleep(500);
                        if (redisComponent.countChargeCompleted(closeLoan.getLoanId()) == responseCount) {
                            break;
                        }
                        sixtySeconds--;
                    }
// UNDO
                    closeLoan.setLoanDto(loanOpt.get());
                    closeLoan.setLastStatementDate(loanOpt.get().getLastStatementDate());
                    registerCloseLoan(closeLoan);
                } else {
                    serviceRequestResponse.setFailure("PayoffAmount incorrect. Required: " + loanOpt.get().getPayoffAmount());
                    rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
                }
            } else {
                serviceRequestResponse.setFailure("loan not found for id: " + closeLoan.getLoanId());
                rabbitMqSender.serviceRequestServiceRequest(serviceRequestResponse);
            }
        } catch (Exception ex) {
            log.error("void receivedCloseLoanMessage(DebitLoan debitLoan) {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    public void registerCloseLoan(CloseLoan closeLoan) {
        try {
            log.info("CloseLoan Received {}", closeLoan);
            StatementHeader statementHeader = StatementHeader.builder()
                    .id(closeLoan.getId())
                    .accountId(closeLoan.getLoanDto().getAccountId())
                    .loanId(closeLoan.getLoanId())
                    .statementDate(closeLoan.getDate())
                    .startDate(closeLoan.getLastStatementDate().plusDays(1))
                    .endDate(closeLoan.getDate())
                    .build();
            registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            rabbitMqSender.statementCloseStatement(statementHeader);
        } catch (Exception ex) {
            log.error("void receivedDebitLoanMessage(DebitLoan debitLoan) exception {}", ex.getMessage());
            try {
                ServiceRequestResponse requestResponse = new ServiceRequestResponse(closeLoan.getId(), ServiceRequestMessage.STATUS.ERROR, ex.getMessage());
                rabbitMqSender.serviceRequestServiceRequest(requestResponse);
            } catch (Exception innerEx) {
                log.error("ERROR SENDING ERROR", ex);
            }
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.loanclosed.queue}")
    public void receivedLoanClosedMessage(StatementHeader statementHeader) {
        try {
            log.info("LoanClosed Received {} ", statementHeader);
            accountManagementService.closeLoan(statementHeader.getLoanId());
            rabbitMqSender.serviceRequestServiceRequest(
                    ServiceRequestResponse.builder().id(statementHeader.getId())
                            .status(ServiceRequestMessage.STATUS.SUCCESS)
                            .statusMessage(ServiceRequestMessage.STATUS.SUCCESS.name())
                            .build());
        } catch (Exception ex) {
            log.error("void receivedValidateDebitMessage(DebitLoan debitLoan) {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.query.statementheader.queue}")
    public StatementHeader receivedStatementHeaderMessage(StatementHeader statementHeader) {
        try {
            log.info("StatementHeader Received {}", statementHeader);
            ServiceRequestResponse serviceRequestResponse = accountManagementService.statementHeader(statementHeader);
            if (serviceRequestResponse.isSuccess())
                registerManagementService.setStatementHeaderRegisterEntryies(statementHeader);
            return statementHeader;
        } catch (Exception ex) {
            log.error("String receivedStatementHeaderMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.query.loanstocycle.queue}")
    public List<BillingCycle> receivedLoansToCyceMessage(LocalDate businessDate) {
        try {
            log.info("LoansToCyce Received {}", businessDate);
            return accountManagementService.loansToCycle(businessDate);
        } catch (Exception ex) {
            log.error("String receivedQueryLoanIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.billingcyclecharge.queue}")
    public void receivedBillingCycleChargeMessage(BillingCycleCharge billingCycleCharge) {
        try {
            log.info("BillingCycleCharge Received {}", billingCycleCharge);
            registerManagementService.billingCycleCharge(billingCycleCharge);
        } catch (Exception ex) {
            log.error("void receivedDebitLoanMessage(DebitLoan debitLoan) exception {}", ex.getMessage());
            billingCycleCharge.setError(ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        } finally {
            rabbitMqSender.serviceRequestChargeCompleted(billingCycleCharge);
        }
    }
    @RabbitListener(queues = "${rabbitmq.account.query.account.id.queue}")
    public String receivedQueryAccountIdMessage(UUID id) {
        try {
            log.info("QueryAccountId Received {}}", id);
            Optional<Account> r = queryService.queryAccountId(id);
            if (r.isPresent()) {
                return objectMapper.writeValueAsString(r.get());
            } else {
                return "ERROR: id not found: " + id;
            }
        } catch (Exception ex) {
            log.error("String receivedQueryAccountIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.account.query.loan.id.queue}")
    public String receivedQueryLoanIdMessage(UUID id) {
        try {
            log.info("QueryLoanId Received {}", id);
            Optional<LoanDto> r = queryService.queryLoanId(id);
            if (r.isPresent()) {
                return objectMapper.writeValueAsString(r.get());
            } else {
                return "ERROR: Loan not found for id: " + id;
            }
        } catch (Exception ex) {
            log.error("String receivedQueryLoanIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    private void sleep(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

}
