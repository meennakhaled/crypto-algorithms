import java.util.Arrays;

/**
 * AES_Utils.java
 * Provides utility functions for conversions, padding, and formatting.
 */
public class AES_Utils {

    /** Converts a byte array to its hexadecimal string representation. */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    /** Hex string to byte array converter, with key validation. */
    public static byte[] hexToBytes(String hex, int expectedLength) {
        if (hex.length() != expectedLength * 2) {
            throw new IllegalArgumentException(
                    "Key handling: Key must be " + (expectedLength * 2) +
                            " hexadecimal characters (" + expectedLength * 8 + " bits). Please try again."
            );
        }

        byte[] bytes = new byte[expectedLength];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            bytes[i] = (byte) v;
        }
        return bytes;
    }

    /** Converts a word (int) to a byte array (MSB first). */
    public static byte[] wordToBytes(int word) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (word >>> 24);
        bytes[1] = (byte) (word >>> 16);
        bytes[2] = (byte) (word >>> 8);
        bytes[3] = (byte) word;
        return bytes;
    }

    /** Converts a byte array (4 bytes) to an integer word. */
    public static int bytesToWord(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }

    /** Converts a 16-byte array into a 4x4 State matrix (column-major order). */
    public static byte[][] bytesToState(byte[] bytes) {
        byte[][] state = new byte[4][4];
        for (int i = 0; i < AES_Constants.BLOCK_SIZE; i++) {
            state[i % 4][i / 4] = bytes[i];
        }
        return state;
    }

    /** Converts a 4x4 State matrix back to a 16-byte array (column-major order). */
    public static byte[] stateToBytes(byte[][] state) {
        byte[] bytes = new byte[AES_Constants.BLOCK_SIZE];
        for (int i = 0; i < AES_Constants.BLOCK_SIZE; i++) {
            bytes[i] = state[i % 4][i / 4];
        }
        return bytes;
    }

    /** Utility to format the 4x4 state array for printing (column-major). */
    public static String formatState(byte[][] state) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 4; r++) {
            sb.append("      Row ").append(r).append(": ");
            for (int c = 0; c < 4; c++) {
                sb.append(String.format("%02x ", state[r][c] & 0xFF));
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }


}