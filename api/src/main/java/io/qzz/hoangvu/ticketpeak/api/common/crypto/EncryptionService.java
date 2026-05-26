package io.qzz.hoangvu.ticketpeak.api.common.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {
    private final EncryptionProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();
    private Key defaultKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    public EncryptionService(EncryptionProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (properties.secretKey() == null || properties.secretKey().isBlank()) {
            throw new IllegalStateException("ticketpeak.encryption.secret-key must be set");
        }
        byte[] keyBytes = Base64.getDecoder().decode(properties.secretKey());
        if (keyBytes.length != 32) {
            throw new IllegalStateException("Encryption secret key must be exactly 32 bytes");
        }
        this.defaultKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        return encrypt(plaintext, defaultKey);
    }

    public String encrypt(String plaintext, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String ciphertext) {
        return decrypt(ciphertext, defaultKey);
    }

    public String decrypt(String ciphertextBase64, Key key) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(ciphertextBase64);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, encrypted, 0, GCM_IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plaintext = cipher.doFinal(encrypted, GCM_IV_LENGTH, encrypted.length - GCM_IV_LENGTH);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
