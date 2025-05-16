package fr.esgi.color_run.utils;

import java.util.HashMap;
import java.util.Map;

// fr/esgi/color_run/utils/VerificationCodeStorage.java
public class VerificationCodeStorage {
    private static final Map<String, String> codeMap = new HashMap<>();
    private static final Map<String, String> tokenToEmail = new HashMap<>();
    private static final Map<String, Long> tokenExpiry = new HashMap<>();

    public static void storeCode(String email, String code) {
        codeMap.put(email, code);
    }

    public static String getCode(String email) {
        return codeMap.get(email);
    }

    public static void removeCode(String email) {
        codeMap.remove(email);
    }
    public static void storeToken(String email, String token, int minutesValid) {
        tokenToEmail.put(token, email);
        tokenExpiry.put(token, System.currentTimeMillis() + (minutesValid * 60 * 1000));
    }

    public static String getEmailByToken(String token) {
        Long expiry = tokenExpiry.get(token);
        if (expiry != null && System.currentTimeMillis() <= expiry) {
            return tokenToEmail.get(token);
        }
        removeToken(token);
        return null;
    }

    public static void removeToken(String token) {
        tokenToEmail.remove(token);
        tokenExpiry.remove(token);
    }
}

