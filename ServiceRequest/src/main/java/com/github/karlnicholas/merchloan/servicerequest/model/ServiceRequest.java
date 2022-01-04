package com.github.karlnicholas.merchloan.servicerequest.model;

import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import lombok.*;

import javax.persistence.*;
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
    private ServiceRequestResponse.STATUS status;
    private String statusMessage;
}
