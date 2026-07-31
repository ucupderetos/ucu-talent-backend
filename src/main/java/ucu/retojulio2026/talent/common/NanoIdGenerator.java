package ucu.retojulio2026.talent.common;

import java.security.SecureRandom;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

public final class NanoIdGenerator {

    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_".toCharArray();

    private static final int ID_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    private NanoIdGenerator() {
    }

    public static String generate() {
        return NanoIdUtils.randomNanoId(RANDOM, ALPHABET, ID_LENGTH);
    }
}
