package com.github.karlnicholas.merchloan.servicerequest.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.dto.RequestStatusDto;
import com.github.karlnicholas.merchloan.servicerequest.model.ServiceRequest;
import com.github.karlnicholas.merchloan.servicerequest.service.QueryService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.Optional;
import java.util.UUID;

@MessageDriven(name = "ServiceRequestQueryIdMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/ServiceRequestQueryIdQueue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Slf4j
public class ServiceRequestQueryIdListener implements MessageListener {
    @Inject
    private Session session;
    private final QueryService queryService;
    private final ObjectMapper objectMapper;

    @Inject
    public ServiceRequestQueryIdListener(QueryService queryService) {
        this.queryService = queryService;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    @Override
    public void onMessage(Message message) {
        try {
            UUID id = message.getBody(UUID.class);
            log.debug("ServiceRequestQueryIdListener: {}", id);
            Optional<ServiceRequest> requestOpt = queryService.getServiceRequest(id);
            String response;
            if (requestOpt.isPresent()) {
                ServiceRequest request = requestOpt.get();
                response = objectMapper.writeValueAsString(RequestStatusDto.builder()
                        .id(request.getId())
                        .localDateTime(request.getLocalDateTime())
                        .status(request.getStatus().name())
                        .statusMessage(request.getStatusMessage())
                        .build());
            } else {
                response = "ERROR: id not found: " + id;
            }
            session.createProducer(message.getJMSReplyTo()).send(session.createObjectMessage(response));
        } catch (Exception e) {
            log.error("receivedCheckRequestMessage", e);
            throw new EJBException(e);
        }
    }
}
