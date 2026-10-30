package com.adria.bank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "account-service", fallbackFactory = AccountClientFallbackFactory.class)
public interface AccountClient {

    @GetMapping("/api/accounts/{id}")
    AccountView getAccount(@PathVariable("id") UUID id);

    @PostMapping("/api/accounts/{id}/debit")
    AccountView debit(@PathVariable("id") UUID id, @RequestBody AmountRequest request);

    @PostMapping("/api/accounts/{id}/credit")
    AccountView credit(@PathVariable("id") UUID id, @RequestBody AmountRequest request);
}
