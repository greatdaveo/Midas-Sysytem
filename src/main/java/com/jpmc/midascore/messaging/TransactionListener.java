package com.jpmc.midascore.messaging;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);
    
    private final TransactionService transactionService;

    public TransactionListener(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id:midas-core}")
    public void onMessage(Transaction transaction) { 
        log.info("LISTENER RECEIVED: {}", transaction);
        transactionService.process(transaction);
    }
}
