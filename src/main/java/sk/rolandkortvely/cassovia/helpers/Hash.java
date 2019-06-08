package sk.rolandkortvely.cassovia.helpers;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class Hash {

    public static String make(String text) {
        return BCrypt.hashpw(text, BCrypt.gensalt());
    }

    public static boolean check(String text, String hash) {
        return BCrypt.checkpw(text, hash);
    }
}
