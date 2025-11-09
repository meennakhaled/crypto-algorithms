import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * DES.java
 *
 * The main executable class for the DES implementation.
 * Handles input/output, key validation, plaintext padding and blocking,
 * and calls the key generation and transformation logic.
 */
public class DES {
    // Custom padding method using zero bytes
    private static String padText(String text, int blockSize) {
        int paddingSize = blockSize - (text.length() % blockSize);
        if (paddingSize == blockSize) {
            return text;
        }

        StringBuilder paddedText = new StringBuilder(text);
        char paddingChar = (char) 0; // استخدام صفر كـ padding
        for (int i = 0; i < paddingSize; i++) {
            paddedText.append(paddingChar);
        }
        return paddedText.toString();
    }


    public static boolean[] encryptBlock(boolean[] blockBits, List<boolean[]> subkeys) {
        System.out.println("\n*** ENCRYPTION OF ONE BLOCK ***");
        // 1. Initial Permutation (0.5 mark)
        boolean[] ipResult = DESTransformations.initialPermutation(blockBits);

        // 2. 16 Rounds (2 marks total for round structure + 4.5 marks for inner function)
        boolean[] afterRounds = DESTransformations.run16Rounds(ipResult, subkeys, true);

        // 3. Inverse Initial Permutation (0.5 mark)
        boolean[] finalCipherBlock = DESTransformations.inverseInitialPermutation(afterRounds);
        System.out.println("Final Cipher Block (Binary): " + BitUtils.displayBits(finalCipherBlock));

        return finalCipherBlock;
    }


    public static boolean[] decryptBlock(boolean[] blockBits, List<boolean[]> subkeys) {
        System.out.println("\n*** DECRYPTION OF ONE BLOCK ***");
        // 1. Initial Permutation (0.5 mark)
        boolean[] ipResult = DESTransformations.initialPermutation(blockBits);

        // 2. 16 Rounds (using subkeys in reverse order)
        boolean[] afterRounds = DESTransformations.run16Rounds(ipResult, subkeys, false);

        // 3. Inverse Initial Permutation (0.5 mark)
        boolean[] finalPlainBlock = DESTransformations.inverseInitialPermutation(afterRounds);
        System.out.println("Final Decrypted Plain Block (Binary): " + BitUtils.displayBits(finalPlainBlock));

        return finalPlainBlock;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String plaintext = "";
        String key = "";

        // --- Step 1: Plaintext Input Handling ---
        System.out.println("Enter Plain Text (any size):");
        plaintext = scanner.nextLine();



        // --- Step 2: Key Input Handling --- (0.5 mark)
        while (true) {
            System.out.println("\nEnter 8-character Key (64 bits):");
            key = scanner.nextLine();
            if (key.length() == 8) {
                break;
            }
            System.err.println("Error: Key must be exactly 8 characters long. Please try again.");
        }

        String paddedPlaintext = padText(plaintext, 8);
        if (paddedPlaintext.length() != plaintext.length()) {
            System.out.println("Plaintext length (" + plaintext.length() + ") is not multiple of 8, padding...");
        }

        //
        // Convert the key string to 64 bits (boolean array)
        boolean[] initialKeyBits = BitUtils.stringToBits(key);

        // --- Step 3: Key Generation ---
        KeyGenerator keyGen = new KeyGenerator(initialKeyBits);
        List<boolean[]> subkeys = keyGen.getSubkeys();


        // --- Step 4: Encryption
        List<boolean[]> cipherBlocks = new ArrayList<>();
        byte[] paddedBytes = paddedPlaintext.getBytes();
        int numBlocks = paddedBytes.length / 8;

        System.out.println("\n=======================================================");
        System.out.println("STARTING ENCRYPTION (" + numBlocks + " blocks)");
        System.out.println("=======================================================");

        for (int i = 0; i < numBlocks; i++) {
            System.out.printf("\n<<< ENCRYPTING BLOCK %d/%d >>>\n", i + 1, numBlocks);

            // Extract the 8-byte block (64 bits)
            byte[] blockBytes = Arrays.copyOfRange(paddedBytes, i * 8, (i + 1) * 8);
            boolean[] blockBits = BitUtils.stringToBits(new String(blockBytes));

            // Encrypt the block
            boolean[] cipherBlockBits = encryptBlock(blockBits, subkeys);
            cipherBlocks.add(cipherBlockBits);
        }

        // Combine all cipher blocks and display the output
        int totalCipherSize = cipherBlocks.stream().mapToInt(a -> a.length).sum();
        boolean[] fullCipherBits = new boolean[totalCipherSize];
        int currentPos = 0;
        for (boolean[] block : cipherBlocks) {
            System.arraycopy(block, 0, fullCipherBits, currentPos, block.length);
            currentPos += block.length;
        }

        String fullCipherHex = BitUtils.bitsToHex(fullCipherBits);

        System.out.println("\n\n=======================================================");
        System.out.println(">>> FINAL ENCRYPTION OUTPUT (Output1) <<<");
        System.out.println("-------------------------------------------------------");
        System.out.println("Full Cipher (Hexadecimal): " + fullCipherHex);

        // Convert to ASCII for the Decryption demonstration
        String fullCiphertext = BitUtils.bitsToString(fullCipherBits);
        System.out.println("Full Cipher (ASCII - May contain unprintable chars): " + fullCiphertext.replace('\n', ' '));
        System.out.println("=======================================================");


        // --- Step 5: Decryption  ---
        List<boolean[]> decryptedBlocks = new ArrayList<>();
        System.out.println("\n\n=======================================================");
        System.out.println("STARTING DECRYPTION (" + numBlocks + " blocks)");
        System.out.println("=======================================================");

        for (int i = 0; i < numBlocks; i++) {
            System.out.printf("\n<<< DECRYPTING BLOCK %d/%d >>>\n", i + 1, numBlocks);

            boolean[] cipherBlockBits = cipherBlocks.get(i);

            // Decrypt the block
            boolean[] decryptedBlockBits = decryptBlock(cipherBlockBits, subkeys);
            decryptedBlocks.add(decryptedBlockBits);
        }

        // Combine all decrypted blocks and (do NOT unpad; return padded plaintext as requested)
        int totalDecryptedSize = decryptedBlocks.stream().mapToInt(a -> a.length).sum();
        boolean[] fullDecryptedBits = new boolean[totalDecryptedSize];
        currentPos = 0; // Reset position for decryption
        for (boolean[] block : decryptedBlocks) {
            System.arraycopy(block, 0, fullDecryptedBits, currentPos, block.length);
            currentPos += block.length;
        }

        String recoveredPaddedText = BitUtils.bitsToString(fullDecryptedBits);
        // NOTE: as requested, do NOT remove padding; show the decrypted text including padding
        System.out.println("\n\n=======================================================");
        System.out.println(">>> FINAL DECRYPTION OUTPUT <<<");
        System.out.println("-------------------------------------------------------");
        System.out.println("Recovered Plaintext (WITH padding): " + showPrintable(recoveredPaddedText));
        System.out.println("=======================================================");

        scanner.close();
    }

    // helper to make non-printable padding visible in output (shows \xHH for non-printables)
    private static String showPrintable(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 32 && c <= 126) { // printable ASCII
                sb.append(c);
            } else {
                sb.append(String.format("\\x%02X", (int) c));
            }
        }
        return sb.toString();
    }
}
