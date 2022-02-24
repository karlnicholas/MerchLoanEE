package com.github.karlnicholas.merchloan.redis.component;

import com.github.karlnicholas.merchloan.jmsmessage.BillingCycle;
import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RedisComponent {
    private final RedisTemplate<Long, LocalDate> redisTemplateBusinessDate;
    private final RedisTemplate<UUID, BillingCycleCharge> redisTemplateBillingCycle;
    private final RedisTemplate<LocalDate, List<UUID>> redisTemplateLoansToCycle;

    public RedisComponent(RedisTemplate<Long, LocalDate> redisTemplateBusinessDate, RedisTemplate<UUID, BillingCycleCharge> redisTemplateBillingCycleCharge, RedisTemplate<LocalDate, List<UUID>> redisTemplateLoansToCycle) {
        this.redisTemplateBusinessDate = redisTemplateBusinessDate;
        this.redisTemplateBillingCycle = redisTemplateBillingCycleCharge;
        this.redisTemplateLoansToCycle = redisTemplateLoansToCycle;
    }

    public void updateBusinessDate(LocalDate businessDate) {
        redisTemplateBusinessDate.opsForValue().set(1L, businessDate);
    }

    public LocalDate getBusinessDate() {
        return redisTemplateBusinessDate.opsForValue().get(1L);
    }

    public void chargeCompleted(BillingCycleCharge billingCycleCharge) {
        redisTemplateBillingCycle.opsForSet().add(billingCycleCharge.getLoanId(), billingCycleCharge);
    }

    public Long countChargeCompleted(UUID loanId) {
        return redisTemplateBillingCycle.opsForSet().size(loanId);
    }

    public BillingCycleCharge popChargeCompleted(UUID loanId) {
        return redisTemplateBillingCycle.opsForSet().pop(loanId);
    }

    public void setLoansToCycle(LocalDate businessDate, List<UUID> loansToCycle) {
        redisTemplateLoansToCycle.opsForSet().add(businessDate, loansToCycle);
    }
}
