package com.example.community_library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Every 10 seconds: default passwords that were not changed in time stop working.
// Turned off in tests (src/test/resources/application.properties).
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.keycloak.temp-password-expiry-job", havingValue = "true", matchIfMissing = true)
public class TempPasswordExpiryJob {

    private final UserAdminService userAdminService;

    @Scheduled(fixedDelayString = "${app.keycloak.temp-password-check-ms:10000}", initialDelay = 10000)
    public void run() {
        userAdminService.expireUnusedPasswords();
    }
}
