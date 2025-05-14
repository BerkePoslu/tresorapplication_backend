package ch.bbw.pr.tresorbackend.util;

import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * EncryptUtil
 * Used to encrypt and decrypt secrets using BCrypt
 * @author Peter Rutschmann
 */
@Component
public class EncryptUtil {
    private String secretKey;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private PasswordEncryptionService passwordService;

    public EncryptUtil() {
        this.objectMapper = new ObjectMapper();
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        try {
            // Create a JSON object to store both the content and its hash
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("content", data);
            jsonNode.put("hash", passwordService.hashPassword(secretKey));
            
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    public String decrypt(String password, String encryptedData) {
        if (password == null || encryptedData == null) {
            return "Invalid";
        }
        try {
            // Parse the JSON to get the content and hash
            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(encryptedData);
            String storedHash = jsonNode.get("hash").asText();
            
            // Verify the password matches the stored hash
            if (passwordService.verifyPassword(password, storedHash)) {
                return jsonNode.get("content").asText();
            }
            return "Invalid";
        } catch (Exception e) {
            return "Invalid";
        }
    }

    public String encryptSecret(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        try {
            // Verify the content is valid JSON first
            objectMapper.readTree(content);
            
            // Create a JSON object to store both the content and its hash
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("content", content);
            jsonNode.put("hash", passwordService.hashPassword(secretKey));
            
            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting content", e);
        }
    }

    public boolean verifySecret(String content, String encryptedContent) {
        if (content == null || content.isEmpty() || encryptedContent == null || encryptedContent.isEmpty()) {
            return false;
        }
        try {
            // Parse the JSON to get the content and hash
            ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(encryptedContent);
            String storedHash = jsonNode.get("hash").asText();
            String storedContent = jsonNode.get("content").asText();
            
            return passwordService.verifyPassword(secretKey, storedHash) && content.equals(storedContent);
        } catch (Exception e) {
            return false;
        }
    }
}
