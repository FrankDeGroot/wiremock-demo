package fjtdg.demo.encrypted_url_parameter;

import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
@Log4j2
public class Encryptor {

    public static final String AES = "AES";

    public String key() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(AES);
            SecureRandom random = new SecureRandom();
            keyGen.init(random);
            SecretKey secretKey = keyGen.generateKey();
            return new String(Base64.encodeBase64(secretKey.getEncoded()));
        } catch (NoSuchAlgorithmException exception) {
            log.error(exception);
            return null;
        }
    }

    public String encrypt(final String key, final String plain) {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decodeBase64(key), AES);
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.encodeBase64URLSafeString(cipher.doFinal(plain.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            log.error(exception);
            return null;
        }
    }

    public String decrypt(final String key, final String encrypted) {
        try {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.decodeBase64(key), AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.decodeBase64(encrypted)));
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException exception) {
            log.error(exception);
            return null;
        }
    }
}
