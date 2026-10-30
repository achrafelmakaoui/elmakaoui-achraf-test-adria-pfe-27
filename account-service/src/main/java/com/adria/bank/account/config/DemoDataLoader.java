package com.adria.bank.account.config;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DemoDataLoader {

    public static final UUID ACCOUNT_A = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ACCOUNT_B = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ACCOUNT_BLOCKED = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Bean
    CommandLineRunner loadDemoAccounts(AccountRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(new Account(ACCOUNT_A, "CUST-001", "EUR", 5000.0, AccountStatus.ACTIVE));
            repository.save(new Account(ACCOUNT_B, "CUST-002", "EUR", 1000.0, AccountStatus.ACTIVE));
            repository.save(new Account(ACCOUNT_BLOCKED, "CUST-003", "EUR", 800.0, AccountStatus.BLOCKED));
        };
    }
}
