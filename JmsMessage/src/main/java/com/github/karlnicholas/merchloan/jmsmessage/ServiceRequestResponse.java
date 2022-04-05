package com.github.karlnicholas.merchloan.jmsmessage;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    public final void setId(UUID id) {
        this.id = id;
    }
    public final UUID getId() {
        return id;
    }
    // test is Success
    public boolean isSuccess() {
        return this.status.equals(ServiceRequestMessage.STATUS.SUCCESS);
    }

    // SUCCESS is with "SUCCESS" statusMessage
    public ServiceRequestResponse setSuccess() {
        this.status = ServiceRequestMessage.STATUS.SUCCESS;
        this.statusMessage = ServiceRequestMessage.STATUS.SUCCESS.name();
        return this;
    }

    // SUCCESS is good.
    public ServiceRequestResponse setSuccess(String statusMessage) {
        this.status = ServiceRequestMessage.STATUS.SUCCESS;
        this.statusMessage = statusMessage;
        return this;
    }

    // FAILURE is fatal and requires intervention
    public ServiceRequestResponse setFailure(String statusMessage) {
        this.status = ServiceRequestMessage.STATUS.FAILURE;
        this.statusMessage = statusMessage;
        return this;
    }

    // ERROR can be retried
    public ServiceRequestResponse setError(String statusMessage) {
        this.status = ServiceRequestMessage.STATUS.ERROR;
        this.statusMessage = statusMessage;
        return this;
    }
}
