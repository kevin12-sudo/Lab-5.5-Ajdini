import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

//Student Name
public class HW5_5 {

    // Relative frequency (%) of each letter in Hebrew text (alef-tav)
    private static final double[] HEBREW_FREQUENCIES = {
            8.0, 4.9, 5.0, 3.6, 6.2, 9.1, 1.8, 2.4, 1.4, 6.7,
            4.0, 6.5, 7.4, 7.0, 2.4, 3.1, 2.8, 2.0, 2.3, 6.8, 8.8, 4.3
    };

    private static final char[] HEBREW_LETTERS = {
            'א','ב','ג','ד','ה','ו','ז','ח',
            'ט','י','כ','ל','מ','נ','ס','ע',
            'פ','צ','ק','ר','ש','ת'
    };

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the API Key: ");
        String apiKey = sc.nextLine();

        System.out.print("Enter the plaintext: ");
        String plaintext = sc.nextLine();

        System.out.print("Enter Caesar shift: ");
        int shift = Integer.parseInt(sc.nextLine());

        // Translate to Hebrew using Google Translate
        String ciphertext = translateToHebrew(plaintext, apiKey);
        System.out.println("Hebrew: " + ciphertext);

        // Encrypt with Caesar Cipher
        ciphertext = encryptCaesar(ciphertext, shift);
        System.out.println("Encrypted: " + ciphertext);

        // Attempt to decrypt using frequency analysis
        String bestGuess = decryptUsingFrequencyAnalysis(ciphertext);

        // Output the most likely plaintext
        System.out.println("Decrypted text (best guess): " + bestGuess);
    }

    // Google Translate to Hebrew
    public static String translateToHebrew(String text, String apiKey) throws Exception {
        URL url = new URL("https://translation.googleapis.com/language/translate/v2?key=" + apiKey);
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
        System.out.println(response.toString());

        String full = response.toString();
        String marker = "\"translatedText\": \"";
        int start = full.indexOf(marker) + marker.length();
        int end = full.indexOf("\"", start);
        return full.substring(start, end);
    }

    // Tries all 22 possible Caesar shifts and selects the one that produces
    // text closest to standard Hebrew letter frequencies.
    public static String decryptUsingFrequencyAnalysis(String ciphertext) {
        String bestDecryption = "";
        double lowestChiSquare = Double.MAX_VALUE; // Lower is better match

        // Try all possible shifts (0-21)
        for (int shift = 0; shift < 22; shift++) {

            // Decrypt using the current shift
            String decryptedText = decryptWithShift(ciphertext, shift);

            // Measure how "Hebrew-like" the result is
            double chiSquare = calculateChiSquare(decryptedText);

            // Keep track of the best (lowest chi-square score)
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

    // Computes the Chi-Square statistic against Hebrew letter frequencies.
    // Lower value = closer match to Hebrew = more likely correct decryption
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

    // Returns index of a Hebrew letter, or -1 if not a Hebrew letter
    public static int getIndex(char c) {
        for (int i = 0; i < HEBREW_LETTERS.length; i++)
            if (HEBREW_LETTERS[i] == c) return i;
        return -1;
    }
}