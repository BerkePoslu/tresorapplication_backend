package ch.bbw.pr.tresorbackend.service.impl;

import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.repository.SecretRepository;
import ch.bbw.pr.tresorbackend.repository.UserRepository;
import ch.bbw.pr.tresorbackend.service.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SecretServiceImpl
 * Implementation of the SecretService interface
 * @author Peter Rutschmann
 */
@Service
public class SecretServiceImpl implements SecretService {

    @Autowired
    private SecretRepository secretRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Secret> getSecretsByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return secretRepository.findByUserId(user.getId());
    }

    @Override
    public Secret createSecret(Secret secret) {
        return secretRepository.save(secret);
    }

    @Override
    public Secret updateSecret(Long id, Secret secret) {
        Secret existingSecret = secretRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Secret not found"));
        existingSecret.setContent(secret.getContent());
        return secretRepository.save(existingSecret);
    }

    @Override
    public void deleteSecret(Long id) {
        secretRepository.deleteById(id);
    }

    @Override
    public Secret getSecretById(Long secretId) {
        return secretRepository.findById(secretId).orElse(null);
    }

    @Override
    public List<Secret> getAllSecrets() {
        return secretRepository.findAll();
    }

    @Override
    public List<Secret> getSecretsByUserId(Long userId) {
        return secretRepository.findByUserId(userId);
    }
}
