package com.github.karlnicholas.merchloan.servicerequestinterface.message;

import javax.ejb.EJBException;
import javax.ejb.Remote;
import java.util.UUID;

@Remote
public interface ServiceRequestEjb {
    String queryId(UUID id) throws EJBException;
    Boolean checkRequest() throws EJBException;
}
