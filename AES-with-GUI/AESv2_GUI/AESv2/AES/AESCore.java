import java.util.Arrays;

public class AESCore {

    /*
     * AddRoundKey:
     * XORs the state matrix with the round key for the given round.
     * The key is taken from the expanded key schedule (4 words per round).
     */
    public static void addRoundKey(byte[][] block, int[] w, int round) {
        AESLogger.round(round);
        AESLogger.info("Add Round Key (Round " + round + "):");

        for (int c = 0; c < 4; c++) {
            int keyWord = w[round * 4 + c]; //choose word for this column
            byte[] keyBytes = AES_Utils.wordToBytes(keyWord);
            for (int r = 0; r < 4; r++) {
                block[r][c] = (byte) (block[r][c] ^ keyBytes[r]);
            }
        }
        System.out.println("    State after AddRoundKey:\n" + AES_Utils.formatState(block));
    }

    /*
     * SubBytes:
     * Applies the AES S-Box substitution to each byte in the state.
     * This introduces non-linearity into the cipher.
     */
    public static void subBytes(byte[][] blockMatrix) {
        AESLogger.info("Byte Substitution (SubBytes):");
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                blockMatrix[r][c] = (byte) AES_Constants.S_BOX[blockMatrix[r][c] & 0xFF];
            }
        }
        System.out.println("    State after SubBytes:\n" + AES_Utils.formatState(blockMatrix));
    }

    /*
     * ShiftRows:
     * Shifts each row of the state matrix left by a fixed offset:
     * row 0 = no shift
     * row 1 = shift by 1
     * row 2 = shift by 2
     * row 3 = shift by 3
     */
    public static void shiftRows(byte[][] blockMatrix) {
        AESLogger.info("Shift Rows:");
        byte[] temp = new byte[4];

        for (int c = 0; c < 4; c++) temp[c] = blockMatrix[1][c]; //take a copy
        for (int c = 0; c < 4; c++) blockMatrix[1][c] = temp[(c + 1) % 4]; //make the shift

        for (int c = 0; c < 4; c++) temp[c] = blockMatrix[2][c];
        for (int c = 0; c < 4; c++) blockMatrix[2][c] = temp[(c + 2) % 4];

        for (int c = 0; c < 4; c++) temp[c] = blockMatrix[3][c];
        for (int c = 0; c < 4; c++) blockMatrix[3][c] = temp[(c + 3) % 4];

        System.out.println("    State after ShiftRows:\n" + AES_Utils.formatState(blockMatrix));
    }

    /*
     * MixColumns:
     * Applies the AES MixColumns transformation:
     * Each column is multiplied by the fixed AES matrix in GF(2^8).
     * This provides diffusion across the 4 bytes of every column.
     */
    public static void mixColumns(byte[][] blockMatrix) {
        AESLogger.info("Mix Columns:");
        for (int c = 0; c < 4; c++) {
            // take the values of the column
            byte a0 = blockMatrix[0][c],
                    a1 = blockMatrix[1][c],
                    a2 = blockMatrix[2][c],
                    a3 = blockMatrix[3][c];

            blockMatrix[0][c] = (byte) (mul2(a0) ^ mul3(a1) ^ (a2 & 0xFF) ^ (a3 & 0xFF));// as a long devision
            blockMatrix[1][c] = (byte) ((a0 & 0xFF) ^ mul2(a1) ^ mul3(a2) ^ (a3 & 0xFF));
            blockMatrix[2][c] = (byte) ((a0 & 0xFF) ^ (a1 & 0xFF) ^ mul2(a2) ^ mul3(a3));
            blockMatrix[3][c] = (byte) (mul3(a0) ^ (a1 & 0xFF) ^ (a2 & 0xFF) ^ mul2(a3));
        }
        System.out.println("    State after MixColumns:\n" + AES_Utils.formatState(blockMatrix));
    }

    /*
     * mul2:
     * Multiply a byte by 2 in GF(2^8) with AES reduction (x^8 + x^4 + x^3 + x + 1).
     */
    private static byte mul2(byte b) {
        int v = (b & 0xFF) << 1; // shift  by 1 (multiply by 2) mean also miltiply the polynomial by x
        if ((b & 0x80) != 0) // if it exeed 8 bits then we need to %
            v ^= 0x1B; // the irreducible polynomial 27 to make it 8 bits
        return (byte) v;
    }

    /*
     * mul3:
     * Multiply a byte by 3 in GF(2^8). (3*b = 2*b XOR b)
     */
    private static byte mul3(byte b) {
        return (byte) (mul2(b) ^ b);
    }

    /*
     * InvMixColumns:
     * Applies the inverse MixColumns operation using multipliers:
     * 14, 11, 13, 9 — also in GF(2^8).
     */
    public static void invMixColumns(byte[][] state) {
        AESLogger.info("Inverse Mix Columns:");
        for (int c = 0; c < 4; c++) {
            byte a0 = state[0][c], a1 = state[1][c], a2 = state[2][c], a3 = state[3][c];
            state[0][c] = (byte) (mul14(a0) ^ mul11(a1) ^ mul13(a2) ^ mul9(a3));
            state[1][c] = (byte) (mul9(a0) ^ mul14(a1) ^ mul11(a2) ^ mul13(a3));
            state[2][c] = (byte) (mul13(a0) ^ mul9(a1) ^ mul14(a2) ^ mul11(a3));
            state[3][c] = (byte) (mul11(a0) ^ mul13(a1) ^ mul9(a2) ^ mul14(a3));
        }
        System.out.println("    State after InvMixColumns:\n" + AES_Utils.formatState(state));
    }

    /*
     * mul9 = (8*b) XOR b
     */
    private static byte mul9(byte b) {
        return (byte) (mul2(mul2(mul2(b))) ^ b);
    }

    /*
     * mul11 = (8*b) XOR (2*b) XOR b
     */
    private static byte mul11(byte b) {
        return (byte) (mul2(mul2(mul2(b))) ^ mul2(b) ^ b);
    }

    /*
     * mul13 = (8*b) XOR (4*b) XOR b
     */
    private static byte mul13(byte b) {
        return (byte) (mul2(mul2(mul2(b))) ^ mul2(mul2(b)) ^ b);
    }

    /*
     * mul14 = (8*b) XOR (4*b) XOR (2*b)
     */
    private static byte mul14(byte b) {
        return (byte) (mul2(mul2(mul2(b))) ^ mul2(mul2(b)) ^ mul2(b));
    }

    /*
     * InvShiftRows:
     * Reverses the ShiftRows transformation:
     * row 1 shifts right by 1, row 2 by 2, row 3 by 3.
     */
    public static void invShiftRows(byte[][] state) {
        AESLogger.info("Inverse Shift Rows:");
        byte[] temp = new byte[4];

        for (int c = 0; c < 4; c++) temp[c] = state[1][c];
        for (int c = 0; c < 4; c++) state[1][c] = temp[(c + 3) % 4];

        for (int c = 0; c < 4; c++) temp[c] = state[2][c];
        for (int c = 0; c < 4; c++) state[2][c] = temp[(c + 2) % 4];

        for (int c = 0; c < 4; c++) temp[c] = state[3][c];
        for (int c = 0; c < 4; c++) state[3][c] = temp[(c + 1) % 4];

        System.out.println("    State after InvShiftRows:\n" + AES_Utils.formatState(state));
    }

    /*
     * InvSubBytes:
     * Applies the AES inverse S-Box to each byte in the state.
     */
    public static void invSubBytes(byte[][] state) {
        AESLogger.info("Inverse Byte Substitution (InvSubBytes):");
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                state[r][c] = (byte) AES_Constants.INV_S_BOX[state[r][c] & 0xFF];
            }
        }
        System.out.println("    State after InvSubBytes:\n" + AES_Utils.formatState(state));
    }
}
