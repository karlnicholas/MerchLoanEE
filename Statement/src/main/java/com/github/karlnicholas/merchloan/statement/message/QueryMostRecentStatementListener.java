package com.github.karlnicholas.merchloan.statement.message;

import com.github.karlnicholas.merchloan.jmsmessage.MostRecentStatement;
import com.github.karlnicholas.merchloan.statement.service.QueryService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.*;
import java.util.UUID;

@MessageDriven(name = "ServiceRequestCheckRequestMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/ServiceRequestCheckRequestQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@Slf4j
public class QueryMostRecentStatementListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private QueryService queryService;

    @Override
    public void onMessage(Message message) {
        try {
            UUID loanId = message.getBody(UUID.class);
            log.debug("onQueryMostRecentStatementMessage {}", loanId);
            MostRecentStatement mostRecentStatement = queryService.findMostRecentStatement(loanId)
                    .map(statement -> MostRecentStatement.builder()
                            .id(statement.getId())
                            .loanId(loanId)
                            .statementDate(statement.getStatementDate())
                            .endingBalance(statement.getEndingBalance())
                            .startingBalance(statement.getStartingBalance())
                            .build())
                    .orElse(MostRecentStatement.builder()
                            .loanId(loanId)
                            .build()
                    );
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(mostRecentStatement));
        } catch (Exception ex) {
            log.error("onQueryMostRecentStatementMessage exception", ex);
            throw new EJBException(ex);
        }

    }
}
