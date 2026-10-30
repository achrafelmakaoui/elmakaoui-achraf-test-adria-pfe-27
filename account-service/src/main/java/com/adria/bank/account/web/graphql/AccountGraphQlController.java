package com.adria.bank.account.web.graphql;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.service.AccountService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class AccountGraphQlController {

    private final AccountService service;

    public AccountGraphQlController(AccountService service) {
        this.service = service;
    }

    @QueryMapping
    public List<Account> accounts() {
        return service.findAll();
    }

    @QueryMapping
    public Account account(@Argument String id) {
        return service.findById(UUID.fromString(id));
    }

    @MutationMapping
    public Account createAccount(@Argument String customerReference,
                                 @Argument String currency,
                                 @Argument double initialBalance) {
        return service.create(customerReference, currency, initialBalance);
    }

    @MutationMapping
    public Account updateAccountStatus(@Argument String id, @Argument AccountStatus status) {
        return service.updateStatus(UUID.fromString(id), status);
    }
}
