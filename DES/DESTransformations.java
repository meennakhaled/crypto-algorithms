import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors; // <--- ADDED THIS IMPORT
import java.util.Collections; // <--- ADDED THIS IMPORT

/**
 * DESTransformations.java
 *
 * Implements the core DES block logic: Initial Permutation (IP), the Feistel
 * function (F), and the Final Permutation (IP Inverse).
 */
public class DESTransformations {

    public static boolean[] initialPermutation(boolean[] block) {
        boolean[] ipResult = BitUtils.permute(block, DESConstants.IP);
        System.out.println("Initial Permutation (IP) Result: " + BitUtils.displayBits(ipResult));
        return ipResult;
    }


    public static boolean[] inverseInitialPermutation(boolean[] block) {
        boolean[] ipInverseResult = BitUtils.permute(block, DESConstants.IP_INVERSE);
        System.out.println("Inverse Initial Permutation (IP Inverse) Result: " + BitUtils.displayBits(ipInverseResult));
        return ipInverseResult;
    }


    public static boolean[] feistelFunction(boolean[] R, boolean[] K, int round) {
        System.out.printf("--- Round %d Feistel Function (R, K) ---\n", round);
        // 1. Expansion P-box (E-Table): 32 bits -> 48 bits
        boolean[] expandedR = BitUtils.permute(R, DESConstants.E_TABLE);
        System.out.println("Expanded R (48 bits): " + BitUtils.displayBits(expandedR));

        // 2. XOR with Subkey: 48 bits XOR 48 bits
        boolean[] xorRK = BitUtils.xor(expandedR, K);
        System.out.println("Expanded R XOR K (48 bits): " + BitUtils.displayBits(xorRK));

        // 3. S-Box Substitution: 48 bits -> 32 bits
        boolean[] sBoxOutput = new boolean[32];
        for (int i = 0; i < 8; i++) {
            // Get the 6-bit block for S-Box 'i'
            boolean[] sixBits = Arrays.copyOfRange(xorRK, i * 6, (i + 1) * 6);

            // Determine row (first and last bits) and column (middle 4 bits)
            int row = (sixBits[0] ? 2 : 0) + (sixBits[5] ? 1 : 0);
            int col = 0;
            for (int j = 1; j < 5; j++) {
                if (sixBits[j]) {
                    col |= (1 << (4 - j));
                }
            }

            // Look up the 4-bit value in the S-Box table
            int value = DESConstants.S_BOXES[i][row][col];
            // Convert the 4-bit integer value back to 4 boolean bits
            for (int j = 0; j < 4; j++) {
                sBoxOutput[i * 4 + j] = ((value >> (3 - j)) & 1) == 1;
            }
        }
        System.out.println("S-Boxes Output (32 bits): " + BitUtils.displayBits(sBoxOutput));

        // 4. P-box Permutation: 32 bits -> 32 bits
        boolean[] pBoxResult = BitUtils.permute(sBoxOutput, DESConstants.P_BOX);
        System.out.println("P-Box Permutation Result: " + BitUtils.displayBits(pBoxResult));

        return pBoxResult;
    }




    public static boolean[] run16Rounds(boolean[] block, List<boolean[]> subkeys, boolean isEncrypt) {
        boolean[][] halves = BitUtils.split(block);
        boolean[] L = halves[0]; // Left 32 bits
        boolean[] R = halves[1]; // Right 32 bits

        // Determine the order of subkeys (forward for Encrypt, reverse for Decrypt)
        List<boolean[]> keys = subkeys;
        if (!isEncrypt) {
            // Fix: Use Collections.reverse() on a *copy* of the list
            keys = new java.util.ArrayList<>(subkeys);
            Collections.reverse(keys);
        }

        System.out.println("\n--- Starting 16 Feistel Rounds ---");

        for (int i = 0; i < 16; i++) {
            boolean[] K = keys.get(i);
            int round = i + 1;
            if (!isEncrypt) {
                // In decryption, round 1 uses K16, round 16 uses K1.
                // Display the key index being used.
                System.out.printf("\n--- Decryption Round %d (Using K%d) ---\n", round, 16 - i);
            } else {
                System.out.printf("\n--- Encryption Round %d (Using K%d) ---\n", round, round);
            }

            boolean[] L_old = L; // L_i-1
            boolean[] R_old = R; // R_i-1

            // New Left L_i = R_i-1
            L = R_old;

            // New Right R_i = L_i-1 XOR F(R_i-1, K_i)
            boolean[] F_output = feistelFunction(R_old, K, isEncrypt ? round : 16 - i);

            // 5. XOR (result of permutation and left)
            R = BitUtils.xor(L_old, F_output);
            System.out.println("L_old XOR F(R_old, K) Result: " + BitUtils.displayBits(R));

            // Display L and R for the next round
            System.out.println("L" + round + ": " + BitUtils.displayBits(L));
            System.out.println("R" + round + ": " + BitUtils.displayBits(R));

            // Note: The swap happens implicitly by setting L=R_old and R=L_old XOR F.
            // However, the assignment specifically mentions "Left and Right swap"
            // which refers to L_i = R_i-1. We display L_i and R_i to show the result.
        }

        // 6. 32-bit Swap (Final Swap of L16 and R16)
        // The final output is R16 followed by L16 (R || L)
        boolean[] finalRL = BitUtils.combine(R, L);
        System.out.println("\n--- Final 32-bit Swap (R16 || L16) ---");
        System.out.println("32-bit Swap Result (R || L): " + BitUtils.displayBits(finalRL));

        return finalRL;
    }
}
