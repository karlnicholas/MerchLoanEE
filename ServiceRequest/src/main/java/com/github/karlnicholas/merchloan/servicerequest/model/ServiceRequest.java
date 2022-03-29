package com.github.karlnicholas.merchloan.servicerequest.model;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @Lob
    private String request;
    private String requestType;
    private LocalDateTime localDateTime;
    private ServiceRequestMessage.STATUS status;
    private String statusMessage;
    private Integer retryCount;
}
