package sk.rolandkortvely.cassovia.helpers;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Class for Hashing
 */
public class Hash {

    /**
     * Create new hash from String
     *
     * @param text String to hash
     * @return hash from the given string
     */
    public static String make(String text) {
        return BCrypt.hashpw(text, BCrypt.gensalt());
    }

    /**
     * Validate string against hash
     *
     * @param text string to check hash against
     * @param hash hash to validate
     * @return true if hashes are equals
     */
    public static boolean check(String text, String hash) {
        return BCrypt.checkpw(text, hash);
    }
}
