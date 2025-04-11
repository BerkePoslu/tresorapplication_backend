package ch.bbw.pr.tresorbackend.util;
import ch.bbw.pr.tresorbackend.service.PasswordEncryptionService;

/**
 * EncryptUtil
 * Used to encrypt content.
 * Not implemented yet.
 * @author Peter Rutschmann
 */
public class EncryptUtil {
   PasswordEncryptionService passwordEncryptionService = new PasswordEncryptionService();

   String secretKey;

   public EncryptUtil(String secretKey) {
      this.secretKey = secretKey;
   }

   public String encrypt(String data) {
      passwordEncryptionService.hashPassword(data);
      return data;
   }

   public String decrypt(String data,String  hashedData) {
      return passwordEncryptionService.compareHashesString(data, hashedData);
   }
}
