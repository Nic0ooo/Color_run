// REMPLACER PostalCodeMapper.java par cette version plus intelligente

package fr.esgi.color_run.util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class PostalCodeMapper {

    // Map des plages d'arrondissements pour les grandes villes
    private static final Map<String, PostalCodeRange> POSTAL_CODE_RANGES = new HashMap<>();

    static {
        // Paris : 75001 à 75020
        POSTAL_CODE_RANGES.put("75000", new PostalCodeRange(75001, 75020, "Paris"));

        // Lyon : 69001 à 69009
        POSTAL_CODE_RANGES.put("69000", new PostalCodeRange(69001, 69009, "Lyon"));

        // Marseille : 13001 à 13016
        POSTAL_CODE_RANGES.put("13000", new PostalCodeRange(13001, 13016, "Marseille"));
    }

    /**
     * Génère la liste de TOUS les codes postaux valides pour un code "rond"
     * @param postalCode le code postal à corriger (ex: "69000")
     * @return liste des codes postaux valides (ex: [69001, 69002, ..., 69009])
     */
    public static List<Integer> getAllValidPostalCodes(String postalCode) {
        if (postalCode == null || postalCode.trim().isEmpty()) {
            return new ArrayList<>();
        }

        PostalCodeRange range = POSTAL_CODE_RANGES.get(postalCode.trim());
        if (range != null) {
            List<Integer> codes = new ArrayList<>();
            for (int i = range.start; i <= range.end; i++) {
                codes.add(i);
            }
            System.out.println("📍 " + postalCode + " étendu vers tous les arrondissements de " + range.cityName + ": " + codes);
            return codes;
        }

        // Si ce n'est pas un code "rond", retourner le code tel quel
        try {
            return List.of(Integer.parseInt(postalCode.trim()));
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Vérifie si un code postal doit être étendu
     */
    public static boolean shouldExpand(String postalCode) {
        return POSTAL_CODE_RANGES.containsKey(postalCode);
    }

    /**
     * Obtient le nom de la ville pour un code postal "rond"
     */
    public static String getCityName(String postalCode) {
        PostalCodeRange range = POSTAL_CODE_RANGES.get(postalCode);
        return range != null ? range.cityName : null;
    }

    /**
     * Classe interne pour représenter une plage de codes postaux
     */
    private static class PostalCodeRange {
        final int start;
        final int end;
        final String cityName;

        PostalCodeRange(int start, int end, String cityName) {
            this.start = start;
            this.end = end;
            this.cityName = cityName;
        }
    }

    // *** GARDER LA COMPATIBILITÉ avec l'ancienne méthode ***
    @Deprecated
    public static String correctPostalCode(String postalCode) {
        List<Integer> codes = getAllValidPostalCodes(postalCode);
        if (!codes.isEmpty()) {
            return codes.get(0).toString(); // Premier arrondissement pour compatibilité
        }
        return postalCode;
    }
}