package com.adria.bank.transaction.web.graphql;

import com.adria.bank.transaction.domain.Transaction;
import com.adria.bank.transaction.service.CreateTransactionCommand;
import com.adria.bank.transaction.service.TransactionService;
import com.adria.bank.transaction.service.TransferCommand;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class TransactionGraphQlController {

    private final TransactionService service;

    public TransactionGraphQlController(TransactionService service) {
        this.service = service;
    }

    @QueryMapping
    public List<Transaction> transactions(@Argument String accountId) {
        return accountId == null ? service.findAll() : service.findByAccount(UUID.fromString(accountId));
    }

    @QueryMapping
    public Transaction transaction(@Argument String id) {
        return service.findById(UUID.fromString(id));
    }

    @MutationMapping
    public Transaction createTransaction(@Argument CreateTransactionCommand input) {
        return service.create(input);
    }

    @MutationMapping
    public List<Transaction> transfer(@Argument TransferCommand input) {
        return service.transfer(input);
    }

    @SchemaMapping(typeName = "Transaction", field = "createdAt")
    public String createdAt(Transaction transaction) {
        return transaction.getCreatedAt().toString();
    }
}
