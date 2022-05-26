package com.github.karlnicholas.merchloan.servicerequestinterface.message;

import javax.ejb.Remote;
import java.util.UUID;

@Remote
public interface ServiceRequestBeans {
    String queryId(UUID id) throws Throwable;
    Boolean checkRequest() throws Throwable;
}
