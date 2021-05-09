package util;

import exception.CommonException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

import static util.CommonUtils.msg;

/**
 * @author gvaddepally on 27/05/20
 */
public class BasicCryptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCryptor.class);

    private String encryptionKey;
    private String salt;
    private int keyLength;
    private int iterations;
    private String iv;
    private Cipher cipher;

    public BasicCryptor() {
        encryptionKey = "trackitCryptor";
        salt = "trackSagaSalt";
        keyLength = 256;
        iterations = 1000;
        iv = "saga.TrackIt.com";
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            String message = msg("cipher initialization failed due to {}", e.getMessage());
            LOGGER.error(message, e);
            throw new CommonException(message, e);
        }
    }

    public String encrypt(String textToEncrypt) {
        try {
            byte[] bytes = cryptOperation(1, textToEncrypt.getBytes("UTF-8"));
            return Base64.encodeBase64String(bytes);
        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = msg("Error occurred encrypting the data due to {}", ex.getMessage());
            LOGGER.error(message, ex);
            throw new CommonException(message, ex);
        }
    }

    public String decrypt(String textTodecrypt) {
        try {
            byte[] bytes = Base64.decodeBase64(textTodecrypt);
            byte[] decryptedBytes = cryptOperation(2, bytes);
            String result = new String(decryptedBytes, "UTF-8");
            return result;
        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = msg("Error occurred decrypting the data due to {}", ex.getMessage());
            LOGGER.error(message, ex);
            throw new CommonException(message, ex);
        }
    }

    public SecretKey getSecretKey() {
        try {
            PBEKeySpec pbeKeySpec = new PBEKeySpec(encryptionKey.toCharArray(), salt.getBytes(), iterations, keyLength);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            SecretKey secretKey = new SecretKeySpec(secretKeyFactory.generateSecret(pbeKeySpec).getEncoded(), "AES");
            return secretKey;
        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = msg("Error occurred in getting secret key due to {}", ex.getMessage());
            LOGGER.error(message, ex);
            throw new CommonException(message, ex);
        }
    }

    private byte[] cryptOperation(int operation, byte[] bytes) {
        try {
            SecretKey secretKey = getSecretKey();
            cipher.init(operation, secretKey, new IvParameterSpec(iv.getBytes()));
            return cipher.doFinal(bytes);
        } catch (CommonException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = msg("Error occurred while performing crypt operation due to {}", ex.getMessage());
            LOGGER.error(message, ex);
            throw new CommonException(message, ex);
        }
    }
}
