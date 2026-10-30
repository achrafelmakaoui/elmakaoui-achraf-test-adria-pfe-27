package com.adria.bank.transaction.messaging;

import com.adria.bank.transaction.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Locale;
import java.util.UUID;

@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public TransactionEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                     @Value("${bank.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        Transaction t = event.transaction();
        String key = t.getAccountId().toString();
        String json = toJson(t);
        try {
            kafkaTemplate.send(topic, key, json).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Could not publish TransactionCreated for transaction {}: {}", t.getId(), error.toString());
                } else {
                    log.info("Published TransactionCreated {} on {} (key={}, partition={})",
                            t.getId(), topic, key, result.getRecordMetadata().partition());
                }
            });
        } catch (RuntimeException e) {
            // The bank data is already committed: a Kafka outage must not fail the user's request.
            log.error("Kafka unavailable, event for transaction {} not published: {}", t.getId(), e.toString());
        }
    }

    static String toJson(Transaction t) {
        return String.format(Locale.ROOT,
                "{\"eventId\":\"%s\",\"eventType\":\"TransactionCreated\",\"transactionId\":\"%s\","
                        + "\"accountId\":\"%s\",\"amount\":%s,\"currency\":\"%s\",\"occurredAt\":\"%s\"}",
                UUID.randomUUID(), t.getId(), t.getAccountId(),
                t.getAmount().toPlainString(), t.getCurrency(), t.getCreatedAt());
    }
}
