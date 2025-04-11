package ch.bbw.pr.tresorbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

/**
 * PasswordEncryptionService
 * Handles password hashing and verification using BCrypt
 * @author Peter Rutschmann
 */
@Service
public class PasswordEncryptionService {

    private BCryptPasswordEncoder encoder;
    
    @Value("${BCRYPT_STRENGTH:10}")
    private int bcryptStrength;

    public PasswordEncryptionService() {
        // Constructor is now empty, initialization moved to @PostConstruct
    }

    @PostConstruct
    public void init() {
        this.encoder = new BCryptPasswordEncoder(bcryptStrength);
    }

    public String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(password);
    }

    public boolean verifyPassword(String plaintext, String encryptedPassword) {
        if (plaintext == null || encryptedPassword == null) {
            return false;
        }
        return encoder.matches(plaintext, encryptedPassword);
    }

    public String compareHashesString(String plaintext, String encryptedPassword) {
        if (verifyPassword(plaintext, encryptedPassword)) {
            return plaintext;
        }
        return "Invalid";
    }
}
