package com.github.karlnicholas.merchloan.jmsmessage;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.UUID;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ServiceRequestResponse implements Serializable {
    private UUID id;
    private ServiceRequestMessage.STATUS status;
    private String statusMessage;

    public void setId(UUID id) {
        this.id = id;
    }

    // test is Success
    public boolean isSuccess() {
        return this.status.equals(ServiceRequestMessage.STATUS.SUCCESS);
    }

    // SUCCESS is with "SUCCESS" statusMessage
    public void setSuccess() {
        this.status = ServiceRequestMessage.STATUS.SUCCESS;
        this.statusMessage = ServiceRequestMessage.STATUS.SUCCESS.name();
    }

    // SUCCESS is good.
    public void setSuccess(String statusMessage) {
        this.status = ServiceRequestMessage.STATUS.SUCCESS;
        this.statusMessage = statusMessage;
    }

    // FAILURE is fatal and requires intervention
    public void setFailure(String statusMessage) {
        this.status = ServiceRequestMessage.STATUS.FAILURE;
        this.statusMessage = statusMessage;
    }

    // ERROR can be retried
    public void setError(String statusMessage) {
        this.status = ServiceRequestMessage.STATUS.ERROR;
        this.statusMessage = statusMessage;
    }
}
