package fr.esgi.color_run.utils;

import java.util.HashMap;
import java.util.Map;

// fr/esgi/color_run/utils/VerificationCodeStorage.java
public class VerificationCodeStorage {
    private static final Map<String, String> codeMap = new HashMap<>();

    public static void storeCode(String email, String code) {
        codeMap.put(email, code);
    }

    public static String getCode(String email) {
        return codeMap.get(email);
    }

    public static void removeCode(String email) {
        codeMap.remove(email);
    }
}

