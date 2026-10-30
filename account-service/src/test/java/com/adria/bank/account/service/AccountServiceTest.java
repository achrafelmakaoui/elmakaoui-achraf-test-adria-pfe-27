package com.adria.bank.account.service;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AccountServiceTest {

    private AccountRepository repository;
    private AccountService service;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AccountRepository.class);
        when(repository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new AccountService(repository);
    }

    @Test
    void debitReducesBalance() {
        when(repository.findById(id)).thenReturn(Optional.of(new Account(id, "C1", "EUR", 100.0, AccountStatus.ACTIVE)));
        assertEquals(60.0, service.debit(id, 40.0).getBalance());
    }

    @Test
    void debitRejectedWhenBalanceTooLow() {
        when(repository.findById(id)).thenReturn(Optional.of(new Account(id, "C1", "EUR", 10.0, AccountStatus.ACTIVE)));
        assertThrows(BusinessRuleException.class, () -> service.debit(id, 40.0));
    }

    @Test
    void debitRejectedWhenAccountBlocked() {
        when(repository.findById(id)).thenReturn(Optional.of(new Account(id, "C1", "EUR", 100.0, AccountStatus.BLOCKED)));
        assertThrows(BusinessRuleException.class, () -> service.debit(id, 10.0));
    }

    @Test
    void unknownAccountIsNotFound() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.findById(id));
    }
}
