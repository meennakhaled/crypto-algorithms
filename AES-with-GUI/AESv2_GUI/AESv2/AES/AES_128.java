import java.util.Arrays;
import java.util.Scanner;

public class AES_128 {

    public static byte[] encryptBlock(byte[] block, int[] w) {
        byte[][] state = AES_Utils.bytesToState(block);
        AESLogger.section("Initial Key Addition");
        AESCore.addRoundKey(state, w, 0);

        for (int round = 1; round < AES_Constants.ROUNDS; round++) {
            AESLogger.round(round);
            AESCore.subBytes(state);
            AESCore.shiftRows(state);
            AESCore.mixColumns(state);
            AESCore.addRoundKey(state, w, round);
        }

        AESLogger.section("Final Round (no MixColumns)");
        AESCore.subBytes(state);
        AESCore.shiftRows(state);
        AESCore.addRoundKey(state, w, AES_Constants.ROUNDS);

        return AES_Utils.stateToBytes(state);
    }

    public static byte[] decryptBlock(byte[] block, int[] w) {
        byte[][] state = AES_Utils.bytesToState(block);
        AESLogger.section("Decryption Phase Start");
        AESLogger.section("Initial AddRoundKey (Final Key)");
        AESCore.addRoundKey(state, w, AES_Constants.ROUNDS);

        for (int round = AES_Constants.ROUNDS - 1; round >= 1; round--) {
            AESLogger.round(AES_Constants.ROUNDS - round);
            AESCore.invShiftRows(state);
            AESCore.invSubBytes(state);
            AESCore.addRoundKey(state, w, round);
            AESCore.invMixColumns(state);
        }

        AESLogger.section("Final Inverse Round (Round 0 Key)");
        AESCore.invShiftRows(state);
        AESCore.invSubBytes(state);
        AESCore.addRoundKey(state, w, 0);

        return AES_Utils.stateToBytes(state);
    }

    private static byte[][] splitAndShowBlocks(String plaintext) {
        System.out.println("\nPlaintext is: " + plaintext);
        byte[] bytes = plaintext.getBytes();
        int totalBlocks = (int) Math.ceil(bytes.length / 16.0);
        if (totalBlocks == 0) totalBlocks = 1;
        byte[][] blocks = new byte[totalBlocks][16];

        int index = 0;
        for (int i = 0; i < totalBlocks; i++) {
            for (int j = 0; j < 16; j++) {
                if (index < bytes.length) {
                    blocks[i][j] = bytes[index++];
                } else {
                    blocks[i][j] = '#';
                }
            }
            System.out.print("Block " + (i + 1) + ": ");
            System.out.println(new String(blocks[i]));
        }

        return blocks;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("  AES-128 MODULAR IMPLEMENTATION  ");
        System.out.println("=================================================");

        System.out.print("Enter plaintext: ");
        String plaintextStr = scanner.nextLine();

        byte[][] blocks = splitAndShowBlocks(plaintextStr);

//////////////////////////////////////////////////////////////////////////////////////
        byte[] keyBytes = null;
        while (keyBytes == null) {
            System.out.print("\nEnter 32 Hexa-decimal Key (128 bits / 16 bytes): ");
            String keyHex = scanner.nextLine().trim();
            try {
                keyBytes = AES_Utils.hexToBytes(keyHex, AES_Constants.BLOCK_SIZE);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
///////////////////////////////////////////////////////////////////////////////////////////
        int[] expandedKey = KeyExpansion.expandKey(keyBytes);

        System.out.println("\n\n#################################################");
        System.out.println("                 ENCRYPTION START");
        System.out.println("#################################################");

        byte[] cipherBytes = new byte[blocks.length * AES_Constants.BLOCK_SIZE];

        for (int i = 0; i < blocks.length; i++) {
            System.out.printf("\n=================================================\n");
            System.out.printf("  Processing Block %d/%d (ENCRYPT)\n", i + 1, blocks.length);
            System.out.printf("=================================================\n");

            AESLogger.setContext("ENC", i + 1);
            byte[] encryptedBlock = encryptBlock(blocks[i], expandedKey);
            System.arraycopy(encryptedBlock, 0, cipherBytes, i * 16, 16);

            String cipherHexBlock = AES_Utils.bytesToHex(encryptedBlock);
            String cipherCharsBlock = new String(encryptedBlock);

            AESLogger.clearContext();
        }

        String cipherHex = AES_Utils.bytesToHex(cipherBytes);
        String cipherChars = new String(cipherBytes);

        System.out.println("\nOutput from encryption (Hexa-decimal):\n" + cipherHex);
        System.out.println("Output from encryption (Characters):\n" + cipherChars);

        System.out.println("\n\n#################################################");
        System.out.println("\n\n#################################################");
        System.out.println("\n\n#################################################");
        System.out.println("                 DECRYPTION START");
        System.out.println("#################################################");
        System.out.println("\n\n#################################################");


        byte[] decryptedPaddedBytes = new byte[cipherBytes.length];

        for (int i = 0; i < blocks.length; i++) {
            System.out.printf("\n=================================================\n");
            System.out.printf("  Processing Inverse Block %d/%d (DECRYPT)\n", i + 1, blocks.length);
            System.out.printf("=================================================\n");

            AESLogger.setContext("DEC", i + 1);
            byte[] encryptedBlock = Arrays.copyOfRange(cipherBytes, i * 16, (i + 1) * 16);
            byte[] decryptedBlock = decryptBlock(encryptedBlock, expandedKey);
            System.arraycopy(decryptedBlock, 0, decryptedPaddedBytes, i * 16, 16);

            System.out.println("Decrypted Block " + (i + 1) + " (Hex): " + AES_Utils.bytesToHex(decryptedBlock));
            System.out.println("Decrypted Block " + (i + 1) + " (Chars): " + new String(decryptedBlock));
            AESLogger.clearContext();
        }

        StringBuilder recovered = new StringBuilder();
        for (byte b : decryptedPaddedBytes) {
            if ((char) b == '#') break;
            recovered.append((char) b);
        }

        System.out.println("\nDecryption Final Result:");
        System.out.println("Original Plaintext: " + plaintextStr);
        System.out.println("Decrypted Plaintext: " + recovered);

        scanner.close();
    }
}
