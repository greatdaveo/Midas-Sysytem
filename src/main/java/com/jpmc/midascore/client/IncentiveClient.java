package com.jpmc.midascore.client;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {
    private static final Logger log = LoggerFactory.getLogger(IncentiveClient.class);

    private final RestTemplate restTemplate;
    private final String url;

    public IncentiveClient(RestTemplate restTemplate,
                           @Value("${incentives.url}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    public float fetchIncentive(Transaction tx) {
        try {
            Incentive resp = restTemplate.postForObject(url, tx, Incentive.class);
            float amt = (resp != null ? resp.getAmount() : 0f);
            return Math.max(0f, amt); // never negative
        } catch (Exception ex) {
            log.warn("Incentive API unavailable, defaulting incentive=0. Cause: {}", ex.getMessage());
            return 0f;
        }
    }
}
