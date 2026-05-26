package io.qzz.hoangvu.ticketpeak.api.common.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ticketpeak.encryption")
public record EncryptionProperties(String secretKey) {}
