package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.repository.BusinessDateRepository;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BusinessDateService {
    private final RedisComponent redisComponent;
    private final BusinessDateRepository businessDateRepository;
    private final RabbitMqSender rabbitMqSender;

    public BusinessDateService(RedisComponent redisComponent, BusinessDateRepository businessDateRepository, RabbitMqSender rabbitMqSender) {
        this.redisComponent = redisComponent;
        this.businessDateRepository = businessDateRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public BusinessDate updateBusinessDate(LocalDate businessDate) {
        BusinessDate priorBusinessDate = BusinessDate.builder().businessDate(businessDateRepository.findById(1L).get().getBusinessDate()).build();
        businessDateRepository.save(BusinessDate.builder().id(1L).businessDate(businessDate).build());
        redisComponent.updateBusinessDate(businessDate);
        return priorBusinessDate;
    }

    public void initializeBusinessDate() {
        BusinessDate businessDate = businessDateRepository.findById(1L).orElse(BusinessDate.builder().id(1L).businessDate(LocalDate.now()).build());
        businessDateRepository.save(businessDate);
        redisComponent.updateBusinessDate(businessDate.getBusinessDate());
    }

    @Async
    public void startBillingCycle(BusinessDate priorBusinessDate) {
        System.out.println("D1");
        boolean waiting = true;
        while(waiting) {
            try {
                Thread.sleep(1000);
                Boolean requestPending = (Boolean) rabbitMqSender.servicerequestCheckRequest(priorBusinessDate.getBusinessDate());
                if ( !requestPending )
                    waiting = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                waiting = false;

            }
        }
        System.out.println("D2");
        List<BillingCycle> loansToCycle = (List<BillingCycle>) rabbitMqSender.acccountLoansToCycle(priorBusinessDate.getBusinessDate());
        loansToCycle.forEach(rabbitMqSender::serviceRequestBillLoan);

    }
}
