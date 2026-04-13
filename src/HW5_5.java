import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HW5_5 {

    // Relative frequency (%) of each letter in Hebrew text (alef-tav)
    private static final double[] HEBREW_FREQUENCIES = {
            8.0, 4.9, 5.0, 3.6, 6.2, 9.1, 1.8, 2.4, 1.4, 6.7,
            4.0, 6.5, 7.4, 7.0, 2.4, 3.1, 2.8, 2.0, 2.3, 6.8, 8.8, 4.3
    };

    // The 22 Hebrew letters
    private static final char[] HEBREW_LETTERS = {
            '\u05d0','\u05d1','\u05d2','\u05d3','\u05d4','\u05d5','\u05d6','\u05d7',
            '\u05d8','\u05d9','\u05db','\u05dc','\u05de','\u05e0','\u05e1','\u05e2',
            '\u05e4','\u05e6','\u05e7','\u05e8','\u05e9','\u05ea'
    };

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the API Key: ");
        String apiKey = sc.nextLine();

        System.out.print("Enter the plaintext: ");
        String plaintext = sc.nextLine();

        System.out.print("Enter Caesar shift: ");
        int shift = Integer.parseInt(sc.nextLine());

        // Step 1: Translate to Hebrew
        String hebrewText = translateToHebrew(plaintext, apiKey);
        System.out.println("Hebrew translation: " + hebrewText);

        // Step 2: Encrypt with Caesar Cipher
        String ciphertext = encryptCaesar(hebrewText, shift);
        System.out.println("Encrypted: " + ciphertext);

        // Step 3: Frequency Analysis
        System.out.println("\nFrequency Analysis:");
        frequencyAnalysis(ciphertext);

        // Step 4: Brute force decrypt
        System.out.println("\nBrute Force Result:");
        String bestGuess = decryptUsingFrequencyAnalysis(ciphertext);
        System.out.println("Decrypted text (best guess): " + bestGuess);
    }

    // Google Translate REST call - translated from English to Hebrew (he)
    public static String translateToHebrew(String text, String apiKey) throws Exception {
        String urlStr = "https://translation.googleapis.com/language/translate/v2" + "?key=" + apiKey;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonInput = "{ \"q\": \"" + text + "\", \"target\": \"he\" }";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonInput.getBytes("utf-8"));
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }

        String full = response.toString();
        System.out.println(full);

        // Parse out just the translatedText value
        String marker = "\"translatedText\": \"";
        int start = full.indexOf(marker) + marker.length();
        int end = full.indexOf("\"", start);
        return full.substring(start, end);
    }

    // Caesar encrypt using Hebrew alphabet (22 letters)
    public static String encryptCaesar(String text, int shift) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = getIndex(c);
            if (idx >= 0) {
                result.append(HEBREW_LETTERS[(idx + shift) % 22]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Tries all 22 possible Caesar shifts and selects the one that produces
    // text closest to standard Hebrew letter frequencies.
    public static String decryptUsingFrequencyAnalysis(String ciphertext) {
        String bestDecryption = "";
        double lowestChiSquare = Double.MAX_VALUE;

        for (int shift = 0; shift < 22; shift++) {
            String decryptedText = decryptWithShift(ciphertext, shift);
            double chiSquare = calculateChiSquare(decryptedText);
            if (chiSquare < lowestChiSquare) {
                lowestChiSquare = chiSquare;
                bestDecryption = decryptedText;
            }
        }
        return bestDecryption;
    }

    // Decrypts a Caesar cipher using a given shift value
    public static String decryptWithShift(String text, int shift) {
        StringBuilder decryptedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            int idx = getIndex(c);
            if (idx >= 0) {
                decryptedText.append(HEBREW_LETTERS[(idx - shift + 22) % 22]);
            } else {
                decryptedText.append(c);
            }
        }
        return decryptedText.toString();
    }

    // Computes the Chi-Square statistic against Hebrew frequencies
    public static double calculateChiSquare(String text) {
        int[] letterCounts = new int[22];
        int totalLetters = 0;

        for (char c : text.toCharArray()) {
            int idx = getIndex(c);
            if (idx >= 0) {
                letterCounts[idx]++;
                totalLetters++;
            }
        }

        if (totalLetters == 0) return Double.MAX_VALUE;

        double chiSquare = 0.0;
        for (int i = 0; i < 22; i++) {
            double observed = letterCounts[i];
            double expected = totalLetters * HEBREW_FREQUENCIES[i] / 100;
            chiSquare += Math.pow(observed - expected, 2) / expected;
        }
        return chiSquare;
    }

    // Frequency analysis - show how often each Hebrew letter appears
    public static void frequencyAnalysis(String text) {
        int[] counts = new int[22];
        int total = 0;
        for (char c : text.toCharArray()) {
            int idx = getIndex(c);
            if (idx >= 0) { counts[idx]++; total++; }
        }
        for (int i = 0; i < 22; i++) {
            if (counts[i] > 0)
                System.out.printf("%s : %d (%.1f%%)%n", HEBREW_LETTERS[i], counts[i], counts[i] * 100.0 / total);
        }
    }

    // Returns index of a Hebrew letter in our array, or -1 if not Hebrew
    public static int getIndex(char c) {
        for (int i = 0; i < HEBREW_LETTERS.length; i++)
            if (HEBREW_LETTERS[i] == c) return i;
        return -1;
    }
}