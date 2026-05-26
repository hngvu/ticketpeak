package io.qzz.hoangvu.ticketpeak.api.ticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ticketpeak.totp")
public record TotpProperties(String encryptionKey) {
}
