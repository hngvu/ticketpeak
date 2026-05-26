package io.qzz.hoangvu.ticketpeak.api.ticket.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import io.qzz.hoangvu.ticketpeak.api.common.crypto.EncryptionService;
import io.qzz.hoangvu.ticketpeak.api.ticket.config.TotpProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class TotpService {

    private final TotpProperties totpProperties;
    private final EncryptionService encryptionService;
    private final TimeBasedOneTimePasswordGenerator totpGenerator;
    private final SecureRandom secureRandom;
    private Key encryptionKey;

    public TotpService(TotpProperties totpProperties, EncryptionService encryptionService) {
        this.totpProperties = totpProperties;
        this.encryptionService = encryptionService;
        try {
            this.totpGenerator = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize TOTP generator", e);
        }
        this.secureRandom = new SecureRandom();
    }

    @PostConstruct
    public void init() {
        if (totpProperties.encryptionKey() == null || totpProperties.encryptionKey().isBlank()) {
            throw new IllegalStateException("ticketpeak.totp.encryption-key must be set");
        }
        byte[] keyBytes = Base64.getDecoder().decode(totpProperties.encryptionKey());
        if (keyBytes.length != 32) {
            throw new IllegalStateException("encryption key must be exactly 32 bytes (256 bits) decoded");
        }
        this.encryptionKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String generateSecret() {
        byte[] secretBytes = new byte[20];
        secureRandom.nextBytes(secretBytes);
        return Base64.getEncoder().encodeToString(secretBytes);
    }

    public String encrypt(String rawSecret) {
        return encryptionService.encrypt(rawSecret, encryptionKey);
    }

    private String decrypt(String ciphertextBase64) {
        return encryptionService.decrypt(ciphertextBase64, encryptionKey);
    }

    private Key getTotpKeyFromSecret(String rawSecretBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(rawSecretBase64);
        return new SecretKeySpec(keyBytes, totpGenerator.getAlgorithm());
    }

    public String generateOtp(String totpSecretEnc) {
        try {
            String rawSecret = decrypt(totpSecretEnc);
            Key key = getTotpKeyFromSecret(rawSecret);
            return String.format("%06d", totpGenerator.generateOneTimePassword(key, Instant.now()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate OTP", e);
        }
    }

    public boolean verifyOtp(String totpSecretEnc, String otpFromScanner) {
        try {
            String rawSecret = decrypt(totpSecretEnc);
            Key key = getTotpKeyFromSecret(rawSecret);
            int otpValue = Integer.parseInt(otpFromScanner);

            Instant now = Instant.now();
            Instant windowMinusOne = now.minus(Duration.ofSeconds(30));
            Instant windowPlusOne = now.plus(Duration.ofSeconds(30));

            // Check current window and +/- 1 window for clock skew
            return totpGenerator.generateOneTimePassword(key, now) == otpValue ||
                   totpGenerator.generateOneTimePassword(key, windowMinusOne) == otpValue ||
                   totpGenerator.generateOneTimePassword(key, windowPlusOne) == otpValue;
        } catch (NumberFormatException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify OTP", e);
        }
    }

    public long computeWindowExpiry() {
        long currentSeconds = Instant.now().getEpochSecond();
        long windowStart = (currentSeconds / 30) * 30;
        return (windowStart + 30) * 1000;
    }
}
