package com.adria.bank.transaction.mcp;

import com.adria.bank.transaction.domain.Transaction;
import com.adria.bank.transaction.service.CreateTransactionCommand;
import com.adria.bank.transaction.service.TransactionService;
import com.adria.bank.transaction.service.TransferCommand;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionTools {

    private final TransactionService service;

    public TransactionTools(TransactionService service) {
        this.service = service;
    }

    @Tool(description = "List the transactions of one account (newest first), or all transactions when no accountId is given")
    public List<Transaction> listTransactions(
            @ToolParam(description = "UUID of the account, optional", required = false) String accountId) {
        return (accountId == null || accountId.isBlank())
                ? service.findAll()
                : service.findByAccount(UUID.fromString(accountId));
    }

    @Tool(description = "Get one transaction from its UUID")
    public Transaction getTransaction(@ToolParam(description = "UUID of the transaction") String transactionId) {
        return service.findById(UUID.fromString(transactionId));
    }

    @Tool(description = "Record a payment (debit) on an account. The amount must be > 0 and not above the bank maximum; "
            + "the account must be ACTIVE, in the same currency, and have enough balance")
    public Transaction createTransaction(
            @ToolParam(description = "UUID of the account") String accountId,
            @ToolParam(description = "Amount, strictly positive") double amount,
            @ToolParam(description = "3-letter currency code, e.g. EUR") String currency,
            @ToolParam(description = "Merchant category, e.g. GROCERY, TRAVEL, ELECTRONICS") String merchantCategory,
            @ToolParam(description = "Country code of the payment, e.g. FR, MA") String country) {
        return service.create(new CreateTransactionCommand(UUID.fromString(accountId),
                BigDecimal.valueOf(amount), currency, merchantCategory, country));
    }

    @Tool(description = "Transfer money from one account to another account (same currency). Returns the two resulting transactions")
    public List<Transaction> transfer(
            @ToolParam(description = "UUID of the source account") String fromAccountId,
            @ToolParam(description = "UUID of the destination account") String toAccountId,
            @ToolParam(description = "Amount, strictly positive") double amount,
            @ToolParam(description = "3-letter currency code, e.g. EUR") String currency) {
        return service.transfer(new TransferCommand(UUID.fromString(fromAccountId),
                UUID.fromString(toAccountId), BigDecimal.valueOf(amount), currency));
    }
}
