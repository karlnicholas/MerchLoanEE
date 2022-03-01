package com.github.karlnicholas.merchloan.jmsmessage;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ServiceRequestResponse implements Serializable {
    public enum STATUS {PENDING, SUCCESS, FAILURE}
    private UUID id;
    private STATUS status;
    private String statusMessage;

    public void setId(UUID id) {
        this.id = id;
    }
    public boolean isSuccess() {
        return this.status.equals(STATUS.SUCCESS);
    }
    public void setSuccess() {
        this.status = STATUS.SUCCESS;
        this.statusMessage = STATUS.SUCCESS.name();
    }
    public void setSuccess(String statusMessage) {
        this.status = STATUS.SUCCESS;
        this.statusMessage = statusMessage;
    }
    public void setFailure(String statusMessage) {
        this.status = STATUS.FAILURE;
        this.statusMessage = statusMessage;
    }
}
