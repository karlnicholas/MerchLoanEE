package com.github.karlnicholas.merchloan.statement.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.UUID;

@MessageDriven(name = "QueryStatementsMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/StatementQueryStatementsQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@Slf4j
public class QueryStatementsListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private QueryService queryService;
    private final ObjectMapper objectMapper;

    public QueryStatementsListener() {
        objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public void onMessage(Message message) {
        try {
            UUID loanId = message.getBody(UUID.class);
            log.debug("QueryStatementsListener {}", loanId);
            String result = objectMapper.writeValueAsString(queryService.findByLoanId(loanId));
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(result));
        } catch (Exception ex) {
            log.error("QueryStatementsListener exception", ex);
            throw new EJBException(ex);
        }
    }
}
