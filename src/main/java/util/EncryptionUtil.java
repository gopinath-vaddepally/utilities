package util;

import javax.crypto.SecretKey;

/**
 * @author gvaddepally on 02/06/20
 */
public class EncryptionUtil {
    private static BasicCryptor basicCryptor;

    static {
        basicCryptor = new BasicCryptor();
    }

    public static String encrypt(String plainText) {
        return basicCryptor.encrypt(plainText);
    }

    public static String decrypt(String encryptedText) {
        return basicCryptor.decrypt(encryptedText);
    }

    public static SecretKey getSecretKey() {
        return basicCryptor.getSecretKey();
    }
}
