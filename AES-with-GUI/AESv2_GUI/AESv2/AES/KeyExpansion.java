import java.util.Arrays;

/**
 * KeyExpansion.java
 * Generates the expanded key schedule (11 round keys) from the 128-bit key,
 * printing detailed step-by-step intermediate outputs.
 */
public class KeyExpansion {

    /** Performs circular left shift of a 32-bit word (RotWord). */
    private static int rotWord(int word) {
        int rotated = (word << 8) | ((word >> 24) & 0xFF);
        System.out.printf("    After RotWord: %08x -> %08x\n", word, rotated);
        return rotated;
    }

    /** Substitutes the bytes of a 32-bit word using the S-Box (SubWord). */
    private static int subWord(int word) {
        byte[] bytes = AES_Utils.wordToBytes(word);
        int substituted = 0;

        System.out.print("    After SubWord: ");
        for (int i = 0; i < 4; i++) {
            int subByte = AES_Constants.S_BOX[bytes[i] & 0xFF];
            substituted |= (subByte << (24 - i * 8));
            System.out.printf("%02x(%02x) ", bytes[i] & 0xFF, subByte);
        }
        System.out.printf("-> %08x\n", substituted);
        return substituted;
    }

    /** Generates all 11 Round Keys (44 words total) with step-by-step output. */
    public static int[] expandKey(byte[] key) {
        int[] w = new int[AES_Constants.TOTAL_WORDS]; //TOTAL_WORDS = 4 * (10 + 1); because 11 round
                                                      //each round need for words 11*4=44 words total
        int i;

        // Load the initial key into the first 4 words
        for (i = 0; i < AES_Constants.KEY_WORDS; i++) { // KEY_WORDS = 4
            w[i] = AES_Utils.bytesToWord(Arrays.copyOfRange(key, i * 4, (i + 1) * 4));//genete intial key
        }

        System.out.println("--- Key Generation ---");
        System.out.printf("Initial Key W0-W3: %08x %08x %08x %08x\n", w[0], w[1], w[2], w[3]);



        int temp;
        while (i < AES_Constants.TOTAL_WORDS) { //from i =4 --> TOTAL_WORDS = 4 * (10 + 1)=44

            //Just For ADD ROUND HEADER
            if (i % AES_Constants.KEY_WORDS == 0) {
                int roundNum = i / AES_Constants.KEY_WORDS;
                System.out.println("\n##ROUND" + roundNum);
            }

            temp = w[i - 1];

            System.out.printf("\nGenerating Word W%d:\n", i);

            if (i % AES_Constants.KEY_WORDS == 0) {
                // Step 1: RotWord
                temp = rotWord(temp);

                // Step 2: SubWord
                temp = subWord(temp);

                // Step 3: XOR with Rcon
                int rconWord = AES_Constants.RCON[i / AES_Constants.KEY_WORDS - 1] << 24;

                System.out.printf("    Using Rcon[%d] = %08x (first byte: %02x)\n",
                        i / AES_Constants.KEY_WORDS - 1, rconWord, (rconWord >> 24) & 0xFF);

                temp ^= rconWord;
                System.out.printf("    After XOR with Rcon: %08x\n", temp);
            }

            // Step 4: XOR with W[i-4]
            int before = w[i - AES_Constants.KEY_WORDS];
            w[i] = before ^ temp;
            System.out.printf("    After XOR with W%d = %08x -> W%d = %08x\n",
                    i - AES_Constants.KEY_WORDS, before, i, w[i]);

            i++;
        }// end ot the wile

        System.out.println("\nExpanded Key Schedule:");
        for (int j = 0; j < AES_Constants.TOTAL_WORDS; j++) {
            if (j % 4 == 0) {
                System.out.printf("  Round Key %02d (W%d-W%d): ", j / 4, j, j + 3);
            }
            System.out.printf("%08x ", w[j]);
            if (j % 4 == 3) System.out.println();
        }
        System.out.println("--------------------------------------\n");

        return w;
    }
}
