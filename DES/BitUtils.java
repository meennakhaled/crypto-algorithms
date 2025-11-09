import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BitUtils {

    public static boolean[] stringToBits(String text) {
        byte[] bytes = text.getBytes();
        boolean[] bits = new boolean[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                // j=0 is the MSB (most significant bit, index 7) of the byte
                // (bytes[i] >> (7 - j)) & 1 checks the bit position
                bits[i * 8 + j] = ((bytes[i] >> (7 - j)) & 1) == 1;
            }
        }
        return bits;
    }


    public static String bitsToString(boolean[] bits) {
        if (bits.length % 8 != 0) {
            // Should not happen with proper DES blocks, but for safety.
            throw new IllegalArgumentException("Bit array length must be a multiple of 8.");
        }
        byte[] bytes = new byte[bits.length / 8];
        for (int i = 0; i < bytes.length; i++) {
            int currentByte = 0;
            for (int j = 0; j < 8; j++) {
                if (bits[i * 8 + j]) {
                    // Set the bit position from MSB (7) down to LSB (0)
                    currentByte |= (1 << (7 - j));
                }
            }
            bytes[i] = (byte) currentByte;
        }
        return new String(bytes);
    }

    public static String bitsToHex(boolean[] bits) {
        StringBuilder hex = new StringBuilder();
        // Pad with leading zeros if necessary to make it a multiple of 4
        int length = bits.length;
        int pad = 4 - (length % 4);
        if (pad != 4 && pad != 0) {
            length += pad;
        }

        for (int i = 0; i < length; i += 4) {
            int val = 0;
            for (int j = 0; j < 4; j++) {
                int bitIndex = i + j;
                if (bitIndex < bits.length && bits[bitIndex]) {
                    val |= (1 << (3 - j));
                }
            }
            hex.append(Integer.toHexString(val));
        }
        return hex.toString().toUpperCase();
    }


    public static boolean[] permute(boolean[] input, int[] table) {
        boolean[] output = new boolean[table.length];
        for (int i = 0; i < table.length; i++) {
            output[i] = input[table[i] - 1];
        }
        return output;
    }

    public static boolean[] xor(boolean[] a, boolean[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Bit arrays for XOR must be the same length.");
        }
        boolean[] result = new boolean[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = a[i] ^ b[i]; // Standard XOR operation
        }
        return result;
    }

    public static boolean[] leftShift(boolean[] input, int shifts) {
        boolean[] output = new boolean[input.length];
        for (int i = 0; i < input.length; i++) {
            // New position is calculated: (current index - shifts) mod length
            output[i] = input[(i + shifts) % input.length];
        }
        return output;
    }


    public static boolean[][] split(boolean[] input) {
        if (input.length % 2 != 0) {
            throw new IllegalArgumentException("Input array must have an even length to split.");
        }
        int half = input.length / 2;
        boolean[] left = Arrays.copyOfRange(input, 0, half);
        boolean[] right = Arrays.copyOfRange(input, half, input.length);
        return new boolean[][]{left, right};
    }


    public static boolean[] combine(boolean[] left, boolean[] right) {
        boolean[] result = new boolean[left.length + right.length];
        System.arraycopy(left, 0, result, 0, left.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }

    /**
     * Displays a boolean array as a sequence of '0's and '1's.
     * @return The binary string representation.
     */
    public static String displayBits(boolean[] bits) {
        StringBuilder sb = new StringBuilder();
        for (boolean bit : bits) {
            sb.append(bit ? '1' : '0');
        }
        return sb.toString();
    }
}
