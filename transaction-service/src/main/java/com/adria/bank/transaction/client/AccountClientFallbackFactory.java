package com.adria.bank.transaction.client;

import com.adria.bank.transaction.service.BusinessRuleException;
import com.adria.bank.transaction.service.NotFoundException;
import com.adria.bank.transaction.service.ServiceUnavailableException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountClientFallbackFactory implements FallbackFactory<AccountClient> {

    private static final Logger log = LoggerFactory.getLogger(AccountClientFallbackFactory.class);

    @Override
    public AccountClient create(Throwable cause) {
        return new AccountClient() {
            @Override
            public AccountView getAccount(UUID id) {
                throw translate("getAccount", cause);
            }

            @Override
            public AccountView debit(UUID id, AmountRequest request) {
                throw translate("debit", cause);
            }

            @Override
            public AccountView credit(UUID id, AmountRequest request) {
                throw translate("credit", cause);
            }
        };
    }

    private RuntimeException translate(String operation, Throwable cause) {
        if (cause instanceof FeignException.NotFound) {
            return new NotFoundException("Account not found (account-service answered 404)");
        }
        if (cause instanceof FeignException.Conflict || cause instanceof FeignException.BadRequest) {
            return new BusinessRuleException("account-service refused the operation: " + cause.getMessage());
        }
        log.warn("account-service unavailable during {}: {}", operation, cause.toString());
        return new ServiceUnavailableException(
                "account-service is currently unavailable, please retry later", cause);
    }
}
