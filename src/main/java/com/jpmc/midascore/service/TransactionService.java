package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    // To Validate and persist a transaction. If invalid, do nothing
    @Transactional
    public void process(Transaction tx) {
        if (tx == null) return;
        long senderId = tx.getSenderId();
        long recipientId = tx.getRecipientId();
        float amount = tx.getAmount();

        // For basic guards
        if (amount <= 0) {
            log.debug("Discarding tx: non-positive amount {}", amount);
            return;
        }

        // To load users
        UserRecord sender = userRepository.findById(senderId);
        UserRecord recipient = userRepository.findById(recipientId);

        if (sender == null || recipient == null) {
            log.debug("Discarding tx: sender or recipient not found (s={}, r={})", senderId, recipientId);
            return;
        }

        if (sender.getBalance() < amount) {
            log.debug("Discarding tx: insufficient funds (sender={}, balance={}, amount={})",
                    senderId, sender.getBalance(), amount);
            return;
        }

        // To apply balances
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // To persist everything atomically
        userRepository.save(sender);
        userRepository.save(recipient);

        transactionRepository.save(new TransactionRecord(sender, recipient, amount));

        log.debug("Recorded tx: {} -> {} amount={}", senderId, recipientId, amount);
    }
}
