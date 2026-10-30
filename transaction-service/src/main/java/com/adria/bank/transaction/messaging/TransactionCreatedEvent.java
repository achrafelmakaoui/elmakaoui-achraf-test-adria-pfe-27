package com.adria.bank.transaction.messaging;

import com.adria.bank.transaction.domain.Transaction;

public record TransactionCreatedEvent(Transaction transaction) {
}
