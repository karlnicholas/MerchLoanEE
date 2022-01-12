package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntry;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.statement.model.Statement;
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
    private final RabbitMqSender rabbitMqSender;

    public RabbitMqReceiver(StatementService statementService, RabbitMqSender rabbitMqSender) {
        this.statementService = statementService;
        this.rabbitMqSender = rabbitMqSender;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }

    @RabbitListener(queues = "${rabbitmq.statement.statement.queue}", returnExceptions = "true")
    public void receivedStatementMessage(StatementHeader statementHeader) {
        try {
            log.info("Statement Received {}", statementHeader);
            Optional<Statement> statementExistsOpt = statementService.findStatement(statementHeader);
            if (statementExistsOpt.isEmpty()) {
                statementHeader = (StatementHeader) rabbitMqSender.accountStatementHeader(statementHeader);
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
            }
            rabbitMqSender.serviceRequestServiceRequest(
                    ServiceRequestResponse.builder()
                            .id(statementHeader.getId())
                            .status(ServiceRequestResponse.STATUS.SUCCESS)
                            .statusMessage("SUCCESS")
                            .build());
        } catch ( Exception ex) {
            log.error("void receivedServiceRequestMessage(ServiceRequestResponse serviceRequest) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

    @RabbitListener(queues = "${rabbitmq.statement.query.statement.queue}", returnExceptions = "true")
    public String receivedQueryStatementMessage(UUID id) {
        try {
            log.info("ServiceRequestQueryId Received {}", id);
            return "hello";
        } catch ( Exception ex) {
            log.error("String receivedServiceRequestQueryIdMessage(UUID id) exception {}", ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }

}