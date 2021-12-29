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
    public enum STATUS {PENDING, SUCCESS, FAILURE}
    private UUID id;
    private STATUS status;
    private String statusMessage;
}
