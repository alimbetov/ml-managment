package kz.moon.app.seclevel.utils;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class FileHashUtil {

    public static String calculateSHA256(InputStream inputStream) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }

        byte[] hashBytes = digest.digest();
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
