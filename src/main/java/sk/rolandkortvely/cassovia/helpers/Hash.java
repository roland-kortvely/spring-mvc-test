package sk.rolandkortvely.cassovia.helpers;

import org.apache.commons.codec.digest.DigestUtils;

public class Hash {

    public static String make(String text) {
        return DigestUtils.sha256Hex(text);
    }

    public static boolean check(String text, String hash) {
        return DigestUtils.sha256Hex(text).equals(hash);
    }
}
