package com.github.karlnicholas.merchloan.servicerequest.model;

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
    private UUID id;
    @Lob
    private String request;
    private String requestType;
    private LocalDateTime localDateTime;
    private Boolean transacted;
}
