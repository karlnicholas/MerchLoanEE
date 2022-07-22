package com.github.karlnicholas.merchloan.servicerequest.message;

import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.sql.SQLException;

@MessageDriven(name = "ServiceRequestCheckRequestMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/ServiceRequestCheckRequestQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class ServiceRequestCheckRequestListener implements MessageListener {
    @Inject
    private Session session;
    private final QueryService queryService;

    @Inject
    public ServiceRequestCheckRequestListener(QueryService queryService) {
        this.queryService = queryService;
    }
    @Override
    public void onMessage(Message message) {
        log.trace("receivedCheckRequestMessage");
        try {
            session.createProducer(message.getJMSReplyTo()).send(session.createObjectMessage(queryService.checkRequest()));
        } catch (SQLException | JMSException e) {
            log.error("receivedCheckRequestMessage", e);
            throw new EJBException(e);
        }
    }
}
