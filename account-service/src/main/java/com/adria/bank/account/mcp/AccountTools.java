package com.adria.bank.account.mcp;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.service.AccountService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountTools {

    private final AccountService service;

    public AccountTools(AccountService service) {
        this.service = service;
    }

    @Tool(description = "List all bank accounts with id, customerReference, status, currency and balance")
    public List<Account> listAccounts() {
        return service.findAll();
    }

    @Tool(description = "Get one bank account (including its balance) from its UUID")
    public Account getAccount(@ToolParam(description = "UUID of the account") String accountId) {
        return service.findById(UUID.fromString(accountId));
    }

    @Tool(description = "Create a new bank account for a customer")
    public Account createAccount(
            @ToolParam(description = "Customer reference, e.g. CUST-001") String customerReference,
            @ToolParam(description = "3-letter currency code, e.g. EUR or MAD") String currency,
            @ToolParam(description = "Initial balance, 0 or more") double initialBalance) {
        return service.create(customerReference, currency, initialBalance);
    }

    @Tool(description = "Block or unblock an account. Status must be ACTIVE or BLOCKED")
    public Account setAccountStatus(
            @ToolParam(description = "UUID of the account") String accountId,
            @ToolParam(description = "ACTIVE or BLOCKED") String status) {
        return service.updateStatus(UUID.fromString(accountId), AccountStatus.valueOf(status.toUpperCase()));
    }
}
