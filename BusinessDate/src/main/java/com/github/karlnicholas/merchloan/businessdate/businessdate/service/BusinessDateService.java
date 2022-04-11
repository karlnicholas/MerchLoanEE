package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.message.MQProducers;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.businessdate.businessdate.repository.BusinessDateRepository;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BusinessDateService {
    private final RedisComponent redisComponent;
    private final BusinessDateRepository businessDateRepository;
    private final MQProducers mqProducers;

    public BusinessDateService(RedisComponent redisComponent, BusinessDateRepository businessDateRepository, MQProducers mqProducers) {
        this.redisComponent = redisComponent;
        this.businessDateRepository = businessDateRepository;
        this.mqProducers = mqProducers;
    }

    public BusinessDate updateBusinessDate(LocalDate businessDate) throws IOException, InterruptedException {
        Optional<BusinessDate> existingBusinessDate = businessDateRepository.findById(1L);
        if ( existingBusinessDate.isPresent()) {
            Instant start = Instant.now();
            BusinessDate priorBusinessDate = BusinessDate.builder().businessDate(existingBusinessDate.get().getBusinessDate()).build();
            Boolean requestPending = null;
            requestPending = (Boolean) mqProducers.servicerequestCheckRequest();
            if (requestPending.booleanValue()) {
                throw new IllegalStateException("Still processing prior business date" + priorBusinessDate.getBusinessDate());
            }
            businessDateRepository.save(BusinessDate.builder().id(1L).businessDate(businessDate).build());
            redisComponent.updateBusinessDate(businessDate);
            startBillingCycle(priorBusinessDate.getBusinessDate());
            log.info("updateBusinessDate {} {}", businessDate, Duration.between(start, Instant.now()));
            return priorBusinessDate;
        } else {
            throw new IllegalStateException("Business Date No State: " + businessDate);
        }
    }

    public void initializeBusinessDate() {
        BusinessDate businessDate = businessDateRepository.findById(1L).orElse(BusinessDate.builder().id(1L).businessDate(LocalDate.now()).build());
        businessDateRepository.save(businessDate);
        redisComponent.updateBusinessDate(businessDate.getBusinessDate());
    }

    public void startBillingCycle(LocalDate priorBusinessDate) throws IOException, InterruptedException {
        List<BillingCycle> loansToCycle = (List<BillingCycle>) mqProducers.acccountQueryLoansToCycle(priorBusinessDate);
        for( BillingCycle billingCycle: loansToCycle) {
            mqProducers.serviceRequestBillLoan(billingCycle);
        }
    }
}
