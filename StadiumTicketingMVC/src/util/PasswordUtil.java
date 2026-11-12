package util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 120000;
    private PasswordUtil() { }

    public static String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String hash(String password, String encodedSalt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), Base64.getDecoder().decode(encodedSalt), ITERATIONS, 256);
        try {
            byte[] result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể mã hóa mật khẩu", ex);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verify(String password, String salt, String hash) {
        try {
            return MessageDigest.isEqual(Base64.getDecoder().decode(hash),
                    Base64.getDecoder().decode(hash(password, salt)));
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
