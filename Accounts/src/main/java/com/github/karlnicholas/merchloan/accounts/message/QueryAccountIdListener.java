package com.github.karlnicholas.merchloan.accounts.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.service.QueryService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.JMSContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Optional;
import java.util.UUID;

@MessageDriven(name = "QueryAccountIdMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/AccountsQueryAccountIdQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class QueryAccountIdListener implements MessageListener {
    @Inject
    private JMSContext jmsContext;
    @Inject
    private QueryService queryService;
    private final ObjectMapper objectMapper;

    public QueryAccountIdListener() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    @Override
    public void onMessage(Message message) {
        try {
            UUID id = message.getBody(UUID.class);
            log.debug("receivedQueryAccountIdMessage: {}", id);
            Optional<Account> accountOpt = queryService.queryAccountId(id);
            String result;
            if (accountOpt.isPresent()) {
                result = objectMapper.writeValueAsString(accountOpt.get());
            } else {
                result = "ERROR: id not found: " + id;
            }
            jmsContext.createProducer()
                    .setDeliveryMode(DeliveryMode.NON_PERSISTENT)
                    .setJMSCorrelationID(message.getJMSCorrelationID())
                    .send(message.getJMSReplyTo(), jmsContext.createObjectMessage(result));
        } catch (Exception ex) {
            log.error("receivedQueryAccountIdMessage exception {}", ex.getMessage());
            throw new EJBException(ex);
        }
    }
}
