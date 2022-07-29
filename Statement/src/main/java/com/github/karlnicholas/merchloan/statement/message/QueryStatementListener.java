package com.github.karlnicholas.merchloan.statement.message;

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

@MessageDriven(name = "QueryStatementMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/StatementQueryStatementQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@Slf4j
public class QueryStatementListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private QueryService queryService;

    @Override
    public void onMessage(Message message) {
        try {
            UUID loanId = message.getBody(UUID.class);
            log.debug("onQueryStatementMessage {}", loanId);
            String result = queryService.findById(loanId).map(Statement::getStatementDoc).orElse("ERROR: No statement found for id " + loanId);
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(result));
        } catch (Exception ex) {
            log.error("onQueryStatementMessage exception", ex);
            throw new EJBException(ex);
        }
    }
}
