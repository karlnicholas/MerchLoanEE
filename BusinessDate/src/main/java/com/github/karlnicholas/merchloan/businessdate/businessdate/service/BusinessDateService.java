package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb;
import com.github.karlnicholas.merchloan.businessdate.businessdate.dao.BusinessDateDao;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class BusinessDateService {
    @Resource(lookup = "java:comp/DefaultJMSConnectionFactory")
    private ConnectionFactory connectionFactory;
    private JMSContext jmsContext;
    @Resource(lookup = "java:global/jms/queue/ServiceRequestBillLoanQueue")
    private Queue serviceRequestBillLoanQueue;
    private JMSProducer jmsProducer;
    @Resource(lookup = "java:jboss/datasources/BusinessDateDS")
    private DataSource dataSource;
    @Inject
    private RedisComponent redisComponent;
    @Inject
    private BusinessDateDao businessDateDao;
    @EJB(lookup = "ejb:merchloanee/servicerequest/ServiceRequestEjbImpl!com.github.karlnicholas.merchloan.servicerequestinterface.message.ServiceRequestEjb")
    private ServiceRequestEjb serviceRequestEjb;
    @EJB(lookup = "ejb:merchloanee/accounts/AccountsEjbImpl!com.github.karlnicholas.merchloan.accountsinterface.message.AccountsEjb")
    private AccountsEjb accountsEjb;

    @PostConstruct
    public void postConstruct() {
        jmsContext = connectionFactory.createContext();
        jmsProducer = jmsContext.createProducer().setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    }

    @PreDestroy
    public void preDestroy() {
        jmsContext.close();
    }

    public void updateBusinessDate(LocalDate businessDate) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            Optional<BusinessDate> existingBusinessDate = businessDateDao.findById(con, 1L);
            if (existingBusinessDate.isPresent()) {
                Instant start = Instant.now();
                BusinessDate priorBusinessDate = BusinessDate.builder().date(existingBusinessDate.get().getDate()).build();
                Boolean stillProcessing = serviceRequestEjb.checkRequest();
                if (stillProcessing == null || stillProcessing) {
                    throw new java.lang.IllegalStateException("Still processing prior business date" + priorBusinessDate.getDate());
                }
                businessDateDao.updateDate(con, BusinessDate.builder().id(1L).date(businessDate).build());
                redisComponent.updateBusinessDate(businessDate);
                startBillingCycle(priorBusinessDate.getDate());
                log.info("updateBusinessDate {} {}", businessDate, Duration.between(start, Instant.now()));
            } else {
                throw new java.lang.IllegalStateException("Business Date No State: " + businessDate);
            }
        }
    }

    public void initializeBusinessDate() throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            Optional<BusinessDate> businessDate = businessDateDao.findById(con, 1L);
            BusinessDate currentBd = BusinessDate.builder().id(1L).date(LocalDate.now()).build();
            if ( businessDate.isPresent()) {
                businessDateDao.updateDate(con, currentBd);
            } else {
                businessDateDao.insert(con, currentBd);
            }
            redisComponent.updateBusinessDate(currentBd.getDate());
        }
    }

    public void startBillingCycle(LocalDate priorBusinessDate) {
        List<BillingCycle> loansToCycle = accountsEjb.loansToCycle(priorBusinessDate);
        for( BillingCycle billingCycle: loansToCycle) {
            jmsProducer.send(serviceRequestBillLoanQueue, jmsContext.createObjectMessage(billingCycle));
        }
    }
}
