package com.adria.bank.transaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RefreshScope
public class TransactionLimits {

    private final BigDecimal maxAmount;

    public TransactionLimits(@Value("${bank.transaction.max-amount:1000}") BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
}
