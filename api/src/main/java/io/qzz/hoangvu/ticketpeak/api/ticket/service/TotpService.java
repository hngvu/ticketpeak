package io.qzz.hoangvu.ticketpeak.api.ticket.service;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import io.qzz.hoangvu.ticketpeak.api.ticket.config.TotpProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class TotpService {

    private final TotpProperties totpProperties;
    private final TimeBasedOneTimePasswordGenerator totpGenerator;
    private final SecureRandom secureRandom;
    private Key encryptionKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public TotpService(TotpProperties totpProperties) {
        this.totpProperties = totpProperties;
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
        return Base64.getEncoder().encodeToString(secretBytes); // We can just use Base64 as the raw secret
    }

    public String encrypt(String rawSecret) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, spec);

            byte[] cipherText = cipher.doFinal(rawSecret.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt TOTP secret", e);
        }
    }

    private String decrypt(String ciphertextBase64) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(ciphertextBase64);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, encrypted, 0, GCM_IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, spec);

            byte[] plaintext = cipher.doFinal(encrypted, GCM_IV_LENGTH, encrypted.length - GCM_IV_LENGTH);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt TOTP secret", e);
        }
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
