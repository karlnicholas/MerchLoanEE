package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntry;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import com.github.karlnicholas.merchloan.statement.service.StatementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class RabbitMqReceiver implements RabbitListenerConfigurer {
    private final StatementService statementService;
    private final QueryService queryService;
    private final RabbitMqSender rabbitMqSender;
    private final ObjectMapper objectMapper;

    public RabbitMqReceiver(StatementService statementService, QueryService queryService, RabbitMqSender rabbitMqSender) {
        this.statementService = statementService;
        this.queryService = queryService;
        this.rabbitMqSender = rabbitMqSender;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.statement.statement.queue}", returnExceptions = "true")
    public void receivedStatementMessage(StatementHeader statementHeader) {
        try {
            log.info("Statement Received {}", statementHeader);
            ServiceRequestResponse requestResponse = ServiceRequestResponse.builder().id(statementHeader.getId()).build();
            Optional<Statement> statementExistsOpt = statementService.findStatement(statementHeader);
            if (statementExistsOpt.isEmpty()) {
                statementHeader = (StatementHeader) rabbitMqSender.accountStatementHeader(statementHeader);
                if ( statementHeader.getCustomer() != null ) {
                    Optional<Statement> lastStatement = statementService.findLastStatement(statementHeader);
                    BigDecimal startingBalance = lastStatement.map(Statement::getEndingBalance).orElse(BigDecimal.ZERO.setScale(2));
                    BigDecimal endingBalance = startingBalance;
                    for (RegisterEntry re: statementHeader.getRegisterEntries()) {
                        if ( re.getCredit() != null ) {
                            endingBalance = endingBalance.subtract(re.getCredit());
                            re.setBalance(endingBalance);
                        }
                        if ( re.getDebit() != null ) {
                            endingBalance = endingBalance.add(re.getDebit());
                            re.setBalance(endingBalance);
                        }
                    }
                    statementService.saveStatement(statementHeader, startingBalance, endingBalance);
                    requestResponse.setSuccess("Statement created");

                } else {
                    requestResponse.setFailure("Account/Loan not found for accountId " + statementHeader.getAccountId() + " and loanId " + statementHeader.getLoanId());
                }
            } else {
                requestResponse.setFailure("Statement already exists for loanId " + statementHeader.getLoanId() + " and statement date " + statementHeader.getStatementDate());
            }
            rabbitMqSender.serviceRequestServiceRequest(requestResponse);
        } catch ( Exception ex) {
            log.error("void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.query.statement.queue}", returnExceptions = "true")
    public String receivedQueryStatementMessage(UUID id) {
        try {
            log.info("QueryStatement Received {}", id);
            return queryService.findById(id).map(Statement::getStatement).orElse("No statement found for id " + id);
        } catch ( Exception ex) {
            log.error("String receivedServiceRequestQueryIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}