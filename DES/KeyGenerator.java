import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


public class KeyGenerator {

    private final List<boolean[]> subkeys = new ArrayList<>();
    private boolean[] c_i; // Left half of the key after PC-1 (28 bits)
    private boolean[] d_i; // Right half of the key after PC-1 (28 bits)

    public KeyGenerator(boolean[] initialKeyBits) {
        // 1. Initial Permutation (PC-1)
        boolean[] pc1Result = BitUtils.permute(initialKeyBits, DESConstants.PC1);
        System.out.println("--- Key Generation (K1-K16) ---");
        System.out.println("1. Initial 64-bit Key (Binary): " + BitUtils.displayBits(initialKeyBits));
        System.out.println("   Key after PC-1 (56 bits): " + BitUtils.displayBits(pc1Result)); // 1 mark

        // Split into C0 and D0 (28 bits each)
        boolean[][] splitCD = BitUtils.split(pc1Result);
        c_i = splitCD[0];
        d_i = splitCD[1];

        // 2. Generate 16 Subkeys
        for (int i = 0; i < 16; i++) {
            // 2. Left Shift (Shift Schedule)
            int shifts = DESConstants.SHIFT_SCHEDULE[i];
            c_i = BitUtils.leftShift(c_i, shifts);
            d_i = BitUtils.leftShift(d_i, shifts);
            System.out.printf("Round %d: C%d shifted %d bit(s): %s\n", i + 1, i + 1, shifts, BitUtils.displayBits(c_i)); // 1 mark
            System.out.printf("Round %d: D%d shifted %d bit(s): %s\n", i + 1, i + 1, shifts, BitUtils.displayBits(d_i));

            // Combine C_i and D_i (56 bits)
            boolean[] combinedCD = BitUtils.combine(c_i, d_i);

            // 3. Permuted Choice 2 (PC-2)
            boolean[] subkey = BitUtils.permute(combinedCD, DESConstants.PC2);
            subkeys.add(subkey);

            // Display K_i for grading
            System.out.printf("Round %d: Subkey K%d (48 bits after PC-2): %s\n", i + 1, i + 1, BitUtils.displayBits(subkey)); // 1 mark (PC-2)
        }
        System.out.println("--- End Key Generation ---");
    }

    /**
     * Returns the list of 16 48-bit subkeys.
     * @return The list of subkeys.
     */
    public List<boolean[]> getSubkeys() {
        return subkeys;
    }
}
