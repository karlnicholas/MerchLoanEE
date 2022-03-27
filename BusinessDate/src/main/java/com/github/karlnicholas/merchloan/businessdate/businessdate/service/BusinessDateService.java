package com.github.karlnicholas.merchloan.businessdate.businessdate.service;

import com.github.karlnicholas.merchloan.businessdate.businessdate.repository.BusinessDateRepository;
import com.github.karlnicholas.merchloan.businessdate.businessdate.model.BusinessDate;
import com.github.karlnicholas.merchloan.jms.config.RabbitMqProperties;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.redis.component.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class BusinessDateService {
    private final RedisComponent redisComponent;
    private final BusinessDateRepository businessDateRepository;
    private final RabbitMqSender rabbitMqSender;
    private final BatchingRabbitTemplate batchingRabbitTemplate;
    private final RabbitMqProperties rabbitMqProperties;
    private final SimpleMessageConverter simpleMessageConverter;
    private final MessageProperties messageProperties;


    public BusinessDateService(RedisComponent redisComponent, BusinessDateRepository businessDateRepository, RabbitMqSender rabbitMqSender, BatchingRabbitTemplate batchingRabbitTemplate, RabbitMqProperties rabbitMqProperties) {
        this.redisComponent = redisComponent;
        this.businessDateRepository = businessDateRepository;
        this.rabbitMqSender = rabbitMqSender;
        this.batchingRabbitTemplate = batchingRabbitTemplate;
        this.rabbitMqProperties = rabbitMqProperties;
        simpleMessageConverter = new SimpleMessageConverter();
        messageProperties = new MessageProperties();
        messageProperties.setType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT);
    }

    public BusinessDate updateBusinessDate(LocalDate businessDate) {
        log.info("updateBusinessDate {}", businessDate);
        return businessDateRepository.findById(1L).map(pr -> {
            BusinessDate priorBusinessDate = BusinessDate.builder().businessDate(pr.getBusinessDate()).build();
            Boolean requestPending = (Boolean) rabbitMqSender.servicerequestCheckRequest();
            if (requestPending.booleanValue()) {
                throw new IllegalStateException("Still processing prior business date" + priorBusinessDate.getBusinessDate());
            }
            businessDateRepository.save(BusinessDate.builder().id(1L).businessDate(businessDate).build());
            redisComponent.updateBusinessDate(businessDate);
            startBillingCycle(priorBusinessDate.getBusinessDate());
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
        loansToCycle.forEach(billingCycle -> {
            batchingRabbitTemplate.send(rabbitMqProperties.getExchange(), rabbitMqProperties.getServiceRequestBillLoanRoutingkey(), simpleMessageConverter.toMessage(billingCycle, messageProperties), null);
        });
    }
}
