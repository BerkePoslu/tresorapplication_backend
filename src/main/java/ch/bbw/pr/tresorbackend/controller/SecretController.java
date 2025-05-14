package ch.bbw.pr.tresorbackend.controller;

import ch.bbw.pr.tresorbackend.model.Secret;
import ch.bbw.pr.tresorbackend.model.NewSecret;
import ch.bbw.pr.tresorbackend.model.EncryptCredentials;
import ch.bbw.pr.tresorbackend.model.User;
import ch.bbw.pr.tresorbackend.service.SecretService;
import ch.bbw.pr.tresorbackend.service.UserService;
import ch.bbw.pr.tresorbackend.util.EncryptUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SecretController
 * @author Peter Rutschmann
 */
@RestController
@AllArgsConstructor
@RequestMapping("api/secrets")
public class SecretController {

   private final SecretService secretService;
   private final UserService userService;
   private final EncryptUtil encryptUtil;
   private final ObjectMapper objectMapper;

   // create secret REST API
   @PostMapping
   public ResponseEntity<String> createSecret2(@Valid @RequestBody NewSecret newSecret, BindingResult bindingResult) {
      //input validation
      if (bindingResult.hasErrors()) {
         List<String> errors = bindingResult.getFieldErrors().stream()
               .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
               .collect(Collectors.toList());
         System.out.println("SecretController.createSecret " + errors);

         JsonArray arr = new JsonArray();
         errors.forEach(arr::add);
         JsonObject obj = new JsonObject();
         obj.add("message", arr);
         String json = new Gson().toJson(obj);

         System.out.println("SecretController.createSecret, validation fails: " + json);
         return ResponseEntity.badRequest().body(json);
      }
      System.out.println("SecretController.createSecret, input validation passed");

      try {
         User user = userService.findByEmail(newSecret.getEmail());

         // Convert content to JSON string and encrypt it
         String jsonContent = objectMapper.writeValueAsString(newSecret.getContent());
         encryptUtil.setSecretKey(newSecret.getEncryptPassword());
         String encryptedContent = encryptUtil.encrypt(jsonContent);
         
         Secret secret = new Secret(
               null,
               user.getId(),
               encryptedContent
         );

         //save secret in db
         secretService.createSecret(secret);
         System.out.println("SecretController.createSecret, secret saved in db");
         JsonObject obj = new JsonObject();
         obj.addProperty("answer", "Secret saved");
         String json = new Gson().toJson(obj);
         System.out.println("SecretController.createSecret " + json);
         return ResponseEntity.accepted().body(json);
      } catch (Exception e) {
         JsonObject obj = new JsonObject();
         obj.addProperty("error", "Error saving secret: " + e.getMessage());
         return ResponseEntity.badRequest().body(new Gson().toJson(obj));
      }
   }

   // Build Get Secrets by userId REST API
   @PostMapping("/byuserid")
   public ResponseEntity<List<Secret>> getSecretsByUserId(@RequestBody EncryptCredentials credentials) {
      System.out.println("SecretController.getSecretsByUserId " + credentials);

      List<Secret> secrets = secretService.getSecretsByUserId(credentials.getUserId());
      if (secrets.isEmpty()) {
         System.out.println("SecretController.getSecretsByUserId secret isEmpty");
         return ResponseEntity.notFound().build();
      }
      
      for (Secret secret : secrets) {
         try {
            String decryptedContent = encryptUtil.decrypt(credentials.getEncryptPassword(), secret.getContent());
            if (!"Invalid".equals(decryptedContent)) {
                // The decrypted content should already be valid JSON
                secret.setContent(decryptedContent);
            } else {
                secret.setContent("{\"error\": \"Unable to decrypt content. Wrong password?\"}");
            }
         } catch (Exception e) {
            System.out.println("SecretController.getSecretsByUserId " + e + " " + secret);
            secret.setContent("{\"error\": \"Unable to decrypt content. Wrong password?\"}");
         }
      }

      System.out.println("SecretController.getSecretsByUserId " + secrets);
      return ResponseEntity.ok(secrets);
   }

   // Build Get Secrets by email REST API
   @PostMapping("/byemail")
   public ResponseEntity<?> getSecretsByEmail(@RequestBody EncryptCredentials credentials) {
      try {
         List<Secret> secrets = secretService.getSecretsByEmail(credentials.getEmail());
         if (secrets.isEmpty()) {
            return ResponseEntity.notFound().build();
         }

         // Decrypt content for each secret
         encryptUtil.setSecretKey(credentials.getEncryptPassword());
         for (Secret secret : secrets) {
            try {
               String decryptedContent = encryptUtil.decrypt(credentials.getEncryptPassword(), secret.getContent());
               if (!"Invalid".equals(decryptedContent)) {
                  // The decrypted content should already be valid JSON
                  secret.setContent(decryptedContent);
               } else {
                  secret.setContent("{\"error\": \"Unable to decrypt content. Wrong password?\"}");
               }
            } catch (Exception e) {
               System.out.println("SecretController.getSecretsByEmail " + e + " " + secret);
               secret.setContent("{\"error\": \"Unable to decrypt content. Wrong password?\"}");
            }
         }

         return ResponseEntity.ok(secrets);
      } catch (Exception e) {
         JsonObject error = new JsonObject();
         error.addProperty("error", "Error retrieving secrets: " + e.getMessage());
         return ResponseEntity.badRequest().body(new Gson().toJson(error));
      }
   }

   // Keep the GET endpoint for backward compatibility but mark it as deprecated
   @Deprecated
   @GetMapping("/{email}")
   public ResponseEntity<?> getSecretsByEmailGet(@PathVariable String email) {
      try {
         List<Secret> secrets = secretService.getSecretsByEmail(email);
         return ResponseEntity.ok(secrets);
      } catch (Exception e) {
         JsonObject error = new JsonObject();
         error.addProperty("error", "Error retrieving secrets: " + e.getMessage());
         return ResponseEntity.badRequest().body(new Gson().toJson(error));
      }
   }

   // Build Get All Secrets REST API
   @GetMapping
   public ResponseEntity<List<Secret>> getAllSecrets() {
      List<Secret> secrets = secretService.getAllSecrets();
      return new ResponseEntity<>(secrets, HttpStatus.OK);
   }

   // Build Update Secret REST API
   @PutMapping("/{id}")
   public ResponseEntity<?> updateSecret(@PathVariable Long id, @RequestBody Secret secret) {
      try {
         // Encrypt the content before updating
         String encryptedContent = encryptUtil.encryptSecret(secret.getContent());
         secret.setContent(encryptedContent);
         
         Secret updatedSecret = secretService.updateSecret(id, secret);
         return ResponseEntity.ok(updatedSecret);
      } catch (Exception e) {
         return ResponseEntity.badRequest().body("Error updating secret: " + e.getMessage());
      }
   }

   // Build Delete Secret REST API
   @DeleteMapping("/{id}")
   public ResponseEntity<?> deleteSecret(@PathVariable Long id) {
      try {
         secretService.deleteSecret(id);
         return ResponseEntity.ok().build();
      } catch (Exception e) {
         return ResponseEntity.badRequest().body("Error deleting secret: " + e.getMessage());
      }
   }
}
