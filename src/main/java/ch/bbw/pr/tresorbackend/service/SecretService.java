package ch.bbw.pr.tresorbackend.service;

import ch.bbw.pr.tresorbackend.model.Secret;
import java.util.List;

/**
 * SecretService
 * Interface for secret management operations
 * @author Peter Rutschmann
 */
public interface SecretService {
    List<Secret> getSecretsByEmail(String email);
    Secret createSecret(Secret secret);
    Secret updateSecret(Long id, Secret secret);
    void deleteSecret(Long id);
    Secret getSecretById(Long secretId);
    List<Secret> getAllSecrets();
    List<Secret> getSecretsByUserId(Long userId);
}
