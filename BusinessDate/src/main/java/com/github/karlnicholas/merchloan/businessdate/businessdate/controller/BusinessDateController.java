package com.github.karlnicholas.merchloan.businessdate.businessdate.controller;

import com.github.karlnicholas.merchloan.businessdate.businessdate.service.BusinessDateService;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.time.LocalDate;

@Path("/")
public class BusinessDateController {
    @Inject
    private BusinessDateService businessDateService;

    @POST
    @Path("businessdate")
    @Consumes(MediaType.TEXT_PLAIN)
    public void postBusinessDate(String businessDate) throws SQLException, JMSException {
        businessDateService.updateBusinessDate(LocalDate.parse(businessDate));
    }
}
