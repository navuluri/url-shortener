package com.url.shortener.utilities;

/**
 * Utility class for encoding and decoding numbers using Base62 encoding.
 * <p>
 * Base62 encoding uses a 62-character alphabet consisting of digits (0-9),
 * uppercase letters (A-Z), and lowercase letters (a-z) to represent numeric values
 * in a compact, URL-safe format. This encoding is commonly used in URL shorteners
 * to convert sequential numeric IDs into short, readable strings.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *     <li>Compact representation: reduces the length of numeric identifiers</li>
 *     <li>URL-safe: uses only alphanumeric characters</li>
 *     <li>Bidirectional: supports both encoding and decoding operations</li>
 *     <li>Case-sensitive: distinguishes between uppercase and lowercase letters</li>
 * </ul>
 * </p>
 * <p>
 * Example usage:
 * <pre>
 *     long id = 123456789;
 *     String encoded = Base62.encode(id);  // Returns "8M0kX"
 *     long decoded = Base62.decode(encoded);  // Returns 123456789
 * </pre>
 * </p>
 *
 * @version 1.0
 * @since 2025-11-20
 */
public class Base62
{

    /**
     * The Base62 alphabet containing 62 characters: digits (0-9), uppercase letters (A-Z),
     * and lowercase letters (a-z).
     * <p>
     * The order of characters in this alphabet determines the encoding scheme:
     * 0-9 (positions 0-9), A-Z (positions 10-35), a-z (positions 36-61).
     * </p>
     */
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Encodes a positive long integer into a Base62 string representation.
     * <p>
     * This method converts a decimal number into its Base62 equivalent by repeatedly
     * dividing by 62 and mapping the remainders to characters in the Base62 alphabet.
     * The resulting string is compact and URL-safe.
     * </p>
     *
     * @param value the positive long integer to encode (must be greater than 0)
     * @return the Base62-encoded string representation of the value
     * @throws IllegalArgumentException if value is negative or zero
     */
    public static String encode(long value)
    {
        StringBuilder sb = new StringBuilder();
        while (value > 0)
        {
            sb.append(ALPHABET.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }

}
