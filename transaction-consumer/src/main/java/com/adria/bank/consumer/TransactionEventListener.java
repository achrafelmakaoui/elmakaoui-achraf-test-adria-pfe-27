package com.adria.bank.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    @KafkaListener(topics = "${bank.kafka.topic:bank.transactions.created}")
    public void onTransactionCreated(ConsumerRecord<String, String> record) {
        log.info("TransactionCreated received | topic={} partition={} offset={} key(accountId)={} payload={}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());
    }
}
