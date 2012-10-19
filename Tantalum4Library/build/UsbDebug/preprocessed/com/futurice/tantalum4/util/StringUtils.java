package com.futurice.tantalum4.util;

import com.futurice.tantalum4.log.L;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.lcdui.Font;

/**
 * Utility methods for String handling
 *
 * @author ssaa, paul houghton
 */
public class StringUtils {

    private final static String UNRESERVED_CHARS = ".-_0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQURSTUVWXYZ*~";
    private static StringUtils singleton;
    private final static String ELIPSIS = "...";

    private static synchronized StringUtils getStringUtils() {
        if (singleton == null) {
            singleton = new StringUtils();
        }

        return singleton;
    }

    private StringUtils() {
    }

    ;
    
    /**
     * Truncates the string to fit the maxWidth. If truncated, an elipsis "..."
     * is displayed to indicate this.
     *
     * @param str
     * @param font
     * @param maxWidth
     * @return String - truncated string with ellipsis added to end of the
     * string
     */
    public static String truncate(String str, final Font font, final int maxWidth) {
        if (font.stringWidth(str) > maxWidth) {
            final StringBuffer truncated = new StringBuffer(str);
            while (font.stringWidth(truncated.toString()) > maxWidth) {
                truncated.deleteCharAt(truncated.length() - 1);
            }
            truncated.delete(truncated.length() - ELIPSIS.length(), truncated.length());
            truncated.append(ELIPSIS);
            str = truncated.toString();
        }

        return str;
    }

    /**
     * Split a string in to several lines of text which will display within a
     * maximum width.
     *
     * @param vector
     * @param str
     * @param font
     * @param maxWidth
     * @return
     */
    public static void splitToLines(final Vector vector, final String text, final Font font, final int maxWidth) {
        int lastSpace = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                lastSpace = i;
            }
            final int len = font.stringWidth(text.substring(0, i));
            if (len > maxWidth) {
                vector.addElement(text.substring(0, lastSpace + 1).trim());
                splitToLines(vector, text.substring(lastSpace + 1), font, maxWidth);
                return;
            }
        }
        vector.addElement(text.trim());
    }

    /**
     * This method can not be static in order to access the current instance's
     * path
     *
     * @param name
     * @return
     * @throws IOException
     */
    private byte[] doReadBytesFromJAR(final String name) throws IOException {
        final InputStream in = getClass().getResourceAsStream(name);
        final byte[] bytes;

        try {
            bytes = new byte[in.available()];
            in.read(bytes);
        } finally {
            in.close();
        }

        return bytes;
    }

    /**
     * Return a byte[] stored as a file in the JAR package
     *
     * @param name
     * @return
     * @throws IOException
     */
    public static byte[] readBytesFromJAR(final String name) throws IOException {
        return getStringUtils().doReadBytesFromJAR(name);
    }

    /**
     * Return a String object stored as a file in the JAR package
     *
     * @param name
     * @return
     * @throws IOException
     */
    public static String readStringFromJAR(final String name) throws IOException {
        return new String(readBytesFromJAR(name));
    }

    /**
     * UTF-8 URL decode of data received from a web service
     * 
     * @param in
     * @return 
     */
    public static String urlDecode(final String in) {
        final char[] chars = new char[in.length()];
        final StringBuffer sb = new StringBuffer(in.length());

        in.getChars(0, in.length(), chars, 0);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '+') {
                sb.append(' ');
            } else if (chars[i] == '%') {
                int c = ((Character.digit(chars[++i], 16) << 4) + Character.digit(chars[++i], 16));
                if (c >= 0x7F) {
                    // multi-byte
                    int byteCount = 0;
                    int c2 = c;
                    while ((c2 & 80) != 0) {
                        byteCount++;
                        c2 <<= 1;
                    }
                    if (i + 3 * byteCount >= chars.length) {
                        L.i("Illegal urlDecode, string not long enough for multi-byte decode", in + " : index=" + i);
                    } else {
                        switch (byteCount) {
                            case 2:
                                c &= 0x1F;
                                c <<= 6;
                                // #xx second digit
                                c2 = ((Character.digit(chars[++i], 16) << 4) + Character.digit(chars[++i], 16));
                                c2 &= 0x3F;
                                c |= c2;
                                break;
                            case 3:
                                c &= 0x0F;
                                c <<= 12;
                                // #xx second digit
                                c2 = ((Character.digit(chars[++i], 16) << 4) + Character.digit(chars[++i], 16));
                                c2 &= 0x3F;
                                c |= c2 << 6;
                                // #xx third digit
                                c2 = ((Character.digit(chars[++i], 16) << 4) + Character.digit(chars[++i], 16));
                                c2 &= 0x3F;
                                c |= c2;
                                break;
                            default:
                                L.i("Unsupported urlDecode, bytes=" + byteCount, in + " : index=" + i);
                        }
                    }
                }
                sb.append(c);
            } else {
                sb.append(chars[i]);
            }
        }

        return sb.toString();
    }
    
    /**
     * UTF-8 url encode data before submitting to a web service
     * 
     * @param in
     * @return 
     */
    public static String urlEncode(final String in) {
        final StringBuffer sb = new StringBuffer(10 + in.length() * 5 / 4);
        final int l = in.length();
        
        for (int i = 0; i < l; i++) {
            int c = in.charAt(i);

            if (UNRESERVED_CHARS.indexOf(c) >= 0) {
                sb.append(c);
            } else if (c == ' ') {
                sb.append('+');
            } else if (c < 0x80) {
                // single byte
                hexEncode(c, sb);
            } else if (c < 0x800) {
                // two byte
                hexEncode(0x60 | (c >>> 6), sb);
                hexEncode(0x80 | (c & 0x3F), sb);
            } else {
                // three byte
                hexEncode(0xF0 | (c >>> 12), sb);
                hexEncode(0x80 | ((c >>> 6) & 0x3F), sb);
                hexEncode(0x80 | (c & 0x3F), sb);
            }
        }

        return sb.toString();
    }

    /**
     * Add the byte in #xx hex format for URL Encoding
     * 
     * @param i
     * @param sb 
     */
    private static void hexEncode(int i, StringBuffer sb) {
        final String s = Integer.toHexString(i);

        sb.append('%');
        if (s.length() == 1) {
            sb.append('0');
        }
        sb.append(s);
    }
}
