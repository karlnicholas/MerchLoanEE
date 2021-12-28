package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ServiceRequestResponse implements Serializable {
    private UUID id;
    private String status;
    private String statusMessage;
}
