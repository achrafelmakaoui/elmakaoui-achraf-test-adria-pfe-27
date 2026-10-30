package com.adria.bank.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    private double balance;

    @Version
    private Long version;

    protected Account() {

    }

    public Account(String customerReference, String currency, double balance) {
        this.id = UUID.randomUUID();
        this.customerReference = customerReference;
        this.currency = currency;
        this.balance = balance;
        this.status = AccountStatus.ACTIVE;
    }

    public Account(UUID id, String customerReference, String currency, double balance, AccountStatus status) {
        this.id = id;
        this.customerReference = customerReference;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getCustomerReference() { return customerReference; }
    public AccountStatus getStatus() { return status; }
    public String getCurrency() { return currency; }
    public double getBalance() { return balance; }

    public void setStatus(AccountStatus status) { this.status = status; }
    public void setBalance(double balance) { this.balance = balance; }
}
