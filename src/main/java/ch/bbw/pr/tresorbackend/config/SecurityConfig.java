package ch.bbw.pr.tresorbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SecurityConfig
 * Configuration class for security-related settings
 * @author Peter Rutschmann
 */
@Configuration
public class SecurityConfig {

    @Value("${BCRYPT_STRENGTH:10}")
    private int bcryptStrength;

    @Value("${COOKIE_ENCRYPTION_KEY}")
    private String cookieEncryptionKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    public String getCookieEncryptionKey() {
        return cookieEncryptionKey;
    }
} 
