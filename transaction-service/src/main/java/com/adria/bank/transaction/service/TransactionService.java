package com.adria.bank.transaction.service;

import com.adria.bank.transaction.client.AccountClient;
import com.adria.bank.transaction.client.AccountView;
import com.adria.bank.transaction.client.AmountRequest;
import com.adria.bank.transaction.config.TransactionLimits;
import com.adria.bank.transaction.domain.Transaction;
import com.adria.bank.transaction.domain.TransactionType;
import com.adria.bank.transaction.messaging.TransactionCreatedEvent;
import com.adria.bank.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository repository;
    private final AccountClient accountClient;
    private final TransactionLimits limits;
    private final ApplicationEventPublisher events;

    public TransactionService(TransactionRepository repository, AccountClient accountClient,
                              TransactionLimits limits, ApplicationEventPublisher events) {
        this.repository = repository;
        this.accountClient = accountClient;
        this.limits = limits;
        this.events = events;
    }

    public Transaction create(CreateTransactionCommand command) {
        BigDecimal amount = checkAmount(command.amount());
        String currency = command.currency().toUpperCase();

        AccountView account = accountClient.getAccount(command.accountId());
        checkUsable(account, currency);
        checkBalance(account, amount);

        Transaction transaction = repository.save(new Transaction(command.accountId(), amount, currency,
                command.merchantCategory(), command.country(), TransactionType.PAYMENT));

        accountClient.debit(command.accountId(), new AmountRequest(amount.doubleValue()));

        events.publishEvent(new TransactionCreatedEvent(transaction));
        return transaction;
    }

    public List<Transaction> transfer(TransferCommand command) {
        if (command.fromAccountId().equals(command.toAccountId())) {
            throw new BusinessRuleException("Source and destination accounts must be different");
        }
        BigDecimal amount = checkAmount(command.amount());
        String currency = command.currency().toUpperCase();

        AccountView from = accountClient.getAccount(command.fromAccountId());
        AccountView to = accountClient.getAccount(command.toAccountId());
        checkUsable(from, currency);
        checkUsable(to, currency);
        checkBalance(from, amount);

        Transaction out = repository.save(new Transaction(from.id(), amount, currency,
                "TRANSFER", "N/A", TransactionType.TRANSFER_OUT));
        Transaction in = repository.save(new Transaction(to.id(), amount, currency,
                "TRANSFER", "N/A", TransactionType.TRANSFER_IN));

        accountClient.debit(from.id(), new AmountRequest(amount.doubleValue()));
        try {
            accountClient.credit(to.id(), new AmountRequest(amount.doubleValue()));
        } catch (RuntimeException e) {
            log.warn("Credit of {} failed, refunding {}", to.id(), from.id());
            accountClient.credit(from.id(), new AmountRequest(amount.doubleValue()));
            throw e;
        }

        events.publishEvent(new TransactionCreatedEvent(out));
        events.publishEvent(new TransactionCreatedEvent(in));
        return List.of(out, in);
    }

    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Transaction> findByAccount(UUID accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction " + id + " not found"));
    }

    private BigDecimal checkAmount(BigDecimal raw) {
        BigDecimal amount = raw == null ? BigDecimal.ZERO : raw.setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new BusinessRuleException("The amount must be strictly positive");
        }
        if (amount.compareTo(limits.getMaxAmount()) > 0) {
            throw new BusinessRuleException("The amount " + amount + " exceeds the maximum allowed amount of "
                    + limits.getMaxAmount());
        }
        return amount;
    }

    private void checkUsable(AccountView account, String currency) {
        if (!"ACTIVE".equals(account.status())) {
            throw new BusinessRuleException("Account " + account.id() + " is " + account.status() + ", not ACTIVE");
        }
        if (!account.currency().equalsIgnoreCase(currency)) {
            throw new BusinessRuleException("Currency mismatch: account " + account.id() + " is in "
                    + account.currency() + " but the transaction is in " + currency);
        }
    }

    private void checkBalance(AccountView account, BigDecimal amount) {
        if (account.balance() < amount.doubleValue()) {
            throw new BusinessRuleException("Insufficient balance on account " + account.id()
                    + " (balance " + account.balance() + ", amount " + amount + ")");
        }
    }
}
