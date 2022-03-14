package com.github.karlnicholas.merchloan.redis.component;

import com.github.karlnicholas.merchloan.jmsmessage.BillingCycleCharge;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class RedisComponent {
    private final RedisTemplate<Long, LocalDate> redisTemplateBusinessDate;
    private final RedisTemplate<UUID, BillingCycleCharge> redisTemplateBillingCycle;

    public RedisComponent(RedisTemplate<Long, LocalDate> redisTemplateBusinessDate, RedisTemplate<UUID, BillingCycleCharge> redisTemplateBillingCycleCharge) {
        this.redisTemplateBusinessDate = redisTemplateBusinessDate;
        this.redisTemplateBillingCycle = redisTemplateBillingCycleCharge;
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
        SetOperations<UUID, BillingCycleCharge> opsForSet = redisTemplateBillingCycle.opsForSet();
        BillingCycleCharge billingCycleCharge = opsForSet.pop(loanId);
        if ( opsForSet.size(loanId) == 0 ) {
            redisTemplateBillingCycle.delete(loanId);
        }
        return billingCycleCharge;
    }
}
