package com.jpmc.midascore.messaging;

import com.jpmc.midascore.foundation.Transaction;
// import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.kafka.support.KafkaHeaders;
// import org.springframework.messaging.handler.annotation.Header;
// import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id:midas-core}")
    public void onMessage(
        // @Payload Transaction transaction,
        // @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
        // @Header(KafkaHeaders.REPLY_TOPIC) String topic,
        // @Header(KafkaHeaders.OFFSET) long offset,
        // ConsumerRecord<String, Transaction> raw
        Transaction transaction
    ) { 
        log.info("LISTENER RECEIVED: {}", transaction);

        // log.info("Received tx topic={} key={} offset={} -> {}", topic, key, offset, transaction);
    }
}
