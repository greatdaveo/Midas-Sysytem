package com.jpmc.midascore.service;

import com.jpmc.midascore.client.IncentiveClient;
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
    private final IncentiveClient incentiveClient;


    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository,
                               IncentiveClient incentiveClient) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveClient = incentiveClient;
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

        // To fetch incentive from REST API and apply to recipient ONLY
        float incentive = incentiveClient.fetchIncentive(tx);
        if (incentive > 0f) {
            recipient.setBalance(recipient.getBalance() + incentive);
        }


        // To persist everything atomically
        userRepository.save(sender);
        userRepository.save(recipient);

        transactionRepository.save(new TransactionRecord(sender, recipient, amount, incentive));

        log.debug("Recorded tx: {} -> {} amount={}, incentive={}", senderId, recipientId, amount, incentive);
    }
}
