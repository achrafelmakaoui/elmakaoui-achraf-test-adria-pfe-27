package com.adria.bank.transaction.service;

import com.adria.bank.transaction.client.AccountClient;
import com.adria.bank.transaction.client.AccountView;
import com.adria.bank.transaction.client.AmountRequest;
import com.adria.bank.transaction.config.TransactionLimits;
import com.adria.bank.transaction.domain.Transaction;
import com.adria.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionServiceTest {

    private final UUID accountId = UUID.randomUUID();
    private final UUID otherId = UUID.randomUUID();

    private TransactionRepository repository;
    private AccountClient accountClient;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(TransactionRepository.class);
        accountClient = Mockito.mock(AccountClient.class);
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new TransactionService(repository, accountClient,
                new TransactionLimits(new BigDecimal("1000")), Mockito.mock(ApplicationEventPublisher.class));
    }

    private CreateTransactionCommand payment(String amount, String currency) {
        return new CreateTransactionCommand(accountId, new BigDecimal(amount), currency, "GROCERY", "FR");
    }

    private void account(UUID id, String status, String currency, double balance) {
        when(accountClient.getAccount(id)).thenReturn(new AccountView(id, "C", status, currency, balance));
    }

    @Test
    void validPaymentIsSavedAndBalanceUpdated() {
        account(accountId, "ACTIVE", "EUR", 500);
        Transaction t = service.create(payment("250.00", "EUR"));
        assertEquals(new BigDecimal("250.00"), t.getAmount());
        verify(repository).save(any(Transaction.class));
        verify(accountClient).debit(eq(accountId), any(AmountRequest.class));
    }

    @Test
    void amountAboveMaximumIsRejected() {
        account(accountId, "ACTIVE", "EUR", 5000);
        assertThrows(BusinessRuleException.class, () -> service.create(payment("1000.01", "EUR")));
        verify(repository, never()).save(any(Transaction.class));
    }

    @Test
    void zeroAmountIsRejected() {
        assertThrows(BusinessRuleException.class, () -> service.create(payment("0", "EUR")));
    }

    @Test
    void blockedAccountIsRejected() {
        account(accountId, "BLOCKED", "EUR", 500);
        assertThrows(BusinessRuleException.class, () -> service.create(payment("10", "EUR")));
    }

    @Test
    void currencyMismatchIsRejected() {
        account(accountId, "ACTIVE", "MAD", 500);
        assertThrows(BusinessRuleException.class, () -> service.create(payment("10", "EUR")));
    }

    @Test
    void insufficientBalanceIsRejected() {
        account(accountId, "ACTIVE", "EUR", 5);
        assertThrows(BusinessRuleException.class, () -> service.create(payment("10", "EUR")));
        verify(accountClient, never()).debit(any(), any());
    }

    @Test
    void transferCreatesTwoTransactionsAndMovesMoney() {
        account(accountId, "ACTIVE", "EUR", 500);
        account(otherId, "ACTIVE", "EUR", 0);
        List<Transaction> result = service.transfer(
                new TransferCommand(accountId, otherId, new BigDecimal("100"), "EUR"));
        assertEquals(2, result.size());
        verify(accountClient).debit(eq(accountId), any(AmountRequest.class));
        verify(accountClient).credit(eq(otherId), any(AmountRequest.class));
    }

    @Test
    void transferToSameAccountIsRejected() {
        assertThrows(BusinessRuleException.class, () -> service.transfer(
                new TransferCommand(accountId, accountId, new BigDecimal("100"), "EUR")));
    }
}
