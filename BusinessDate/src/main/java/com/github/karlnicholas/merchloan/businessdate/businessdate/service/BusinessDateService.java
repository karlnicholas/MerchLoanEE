package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.businessdate.businessdate.repository.BusinessDateRepository;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSenderOrig;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class BusinessDateService {
    private final RedisComponent redisComponent;
    private final BusinessDateRepository businessDateRepository;
    private final RabbitMqSenderOrig rabbitMqSender;

    public BusinessDateService(RedisComponent redisComponent, BusinessDateRepository businessDateRepository, RabbitMqSenderOrig rabbitMqSender) {
        this.redisComponent = redisComponent;
        this.businessDateRepository = businessDateRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public BusinessDate updateBusinessDate(LocalDate businessDate) {
        return businessDateRepository.findById(1L).map(pr -> {
            Instant start = Instant.now();
            BusinessDate priorBusinessDate = BusinessDate.builder().businessDate(pr.getBusinessDate()).build();
            Boolean requestPending = (Boolean) rabbitMqSender.servicerequestCheckRequest();
            if (requestPending.booleanValue()) {
                throw new IllegalStateException("Still processing prior business date" + priorBusinessDate.getBusinessDate());
            }
            businessDateRepository.save(BusinessDate.builder().id(1L).businessDate(businessDate).build());
            redisComponent.updateBusinessDate(businessDate);
            startBillingCycle(priorBusinessDate.getBusinessDate());
            log.info("updateBusinessDate {} {}", businessDate, Duration.between(start, Instant.now()));
            return priorBusinessDate;
        }).orElseThrow();
    }

    public void initializeBusinessDate() {
        BusinessDate businessDate = businessDateRepository.findById(1L).orElse(BusinessDate.builder().id(1L).businessDate(LocalDate.now()).build());
        businessDateRepository.save(businessDate);
        redisComponent.updateBusinessDate(businessDate.getBusinessDate());
    }

    public void startBillingCycle(LocalDate priorBusinessDate) {
        List<BillingCycle> loansToCycle = (List<BillingCycle>) rabbitMqSender.acccountQueryLoansToCycle(priorBusinessDate);
        loansToCycle.forEach(rabbitMqSender::serviceRequestBillLoan);
    }
}
