package com.github.karlnicholas.merchloan.servicerequest.model;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest {
    private UUID id;
    private String request;
    private String requestType;
    private LocalDateTime localDateTime;
    private ServiceRequestMessage.STATUS status;
    private String statusMessage;
    private Integer retryCount;
}
