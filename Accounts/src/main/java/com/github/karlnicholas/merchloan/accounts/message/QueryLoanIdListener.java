package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@MessageDriven(name = "QueryLoanIdMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountQueryLoanIdQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class QueryLoanIdListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private QueryService queryService;
    private final ObjectMapper objectMapper;

    public QueryLoanIdListener() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    @Override
    public void onMessage(Message message) {
        try {
            UUID id = message.getBody(UUID.class);
            log.debug("receivedQueryLoanIdMessage: {}", id);
            Optional<LoanDto> r = queryService.queryLoanId(id);
            String result;
            if (r.isPresent()) {
                result = objectMapper.writeValueAsString(r.get());
            } else {
                result = "ERROR: Loan not found for id: " + id;
            }
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(result));
        } catch (SQLException | JsonProcessingException | JMSException | InterruptedException e ) {
            throw new EJBException(e);
        }
    }
}
