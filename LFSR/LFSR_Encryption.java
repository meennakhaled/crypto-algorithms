import java.util.*;

public class LFSR {

    // Function to take user's name
    public static String getUserName() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: ");
        return sc.nextLine();
    }

    // Function to take m value
    public static int getMValue() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter m value (maximum 9): ");
        int m = sc.nextInt();
        while (m < 1 || m > 9) {
            System.out.print("Invalid! Enter m value between 1 and 9: ");
            m = sc.nextInt();
        }
        return m;
    }

    // Function to take polynomial input
    public static List<Integer> getPolynomial(int m) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your polynomial (ex, if m = 4: x^4 + x^2 + 1): ");
        String poly = sc.nextLine().replaceAll("\\s+", ""); // remove spaces

        List<Integer> taps = new ArrayList<>();

        String[] terms = poly.split("\\+");
        for (String term : terms) {
            if (term.equals("1")) {
                taps.add(0); // constant term x^0
            } else if (term.equals("x")) {
                taps.add(1);
            } else if (term.startsWith("x^")) {
                taps.add(Integer.parseInt(term.substring(2)));
            }
        }

        System.out.println("Detected tap positions (powers of x): " + taps);
        return taps;
    }

    // Function to take initial vector
    public static String getInitialVector(int m) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the initial vector (" + m + " bits): ");
        String vector = sc.nextLine();

        while (vector.length() != m) {
            System.out.print("Invalid! Enter exactly " + m + " bits: ");
            vector = sc.nextLine();
        }

        return vector;
    }

    // Convert string to binary
    public static String stringToBinary(String text) {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    // Convert binary to string
    public static String binaryToString(String binary) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            int end = Math.min(i + 8, binary.length());
            int charCode = Integer.parseInt(binary.substring(i, end), 2);
            text.append((char) charCode);
        }
        return text.toString();
    }

    // Perform one LFSR step
    public static int stepLFSR(int[] reg, List<Integer> taps) {
        int m = reg.length;
        int feedback = 0;
        for (int tap : taps) {
            if (tap == m) continue;
            feedback ^= reg[m - 1 - tap];
        }

        int output = reg[m - 1];
        for (int i = m - 1; i > 0; i--) reg[i] = reg[i - 1];
        reg[0] = feedback;
        return output;
    }

    // Process LFSR with warm-up
    public static void processLFSR(String name, String initialVector, int m, List<Integer> taps, int warmUpSteps) {
        String binaryMessage = stringToBinary(name);
        System.out.println("\nBinary representation of \"" + name + "\": " + binaryMessage);

        int[] register = new int[m];
        for (int i = 0; i < m; i++) register[i] = Character.getNumericValue(initialVector.charAt(i));

        // Warm-up phase
        System.out.println("\n--- Warm-up phase (" + warmUpSteps + " steps) ---");
        for (int i = 0; i < warmUpSteps; i++) {
            int out = stepLFSR(register, taps);
        }

        // Now start encryption
        System.out.println("\n--- Encryption phase ---");
        System.out.println("Clock | Register State | Feedback Bit | Output Bit");
        System.out.println("---------------------------------------------------");

        StringBuilder keyStream = new StringBuilder();

        for (int clock = 1; clock <= binaryMessage.length(); clock++) {
            int mLen = register.length;
            int feedbackBit = 0;
            for (int tap : taps) {
                if (tap == mLen) continue;
                feedbackBit ^= register[mLen - 1 - tap];
            }

            int outputBit = register[mLen - 1];

            System.out.print(clock + "     | ");
            for (int bit : register) System.out.print(bit);
            System.out.println("           |     " + feedbackBit + "         |     " + outputBit);

            keyStream.append(outputBit);

            for (int i = mLen - 1; i > 0; i--) register[i] = register[i - 1];
            register[0] = feedbackBit;
        }

        System.out.println("---------------------------------------------------");
        System.out.println("Generated keystream: " + keyStream);

        // Encrypt
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < binaryMessage.length(); i++) {
            int bit = (binaryMessage.charAt(i) - '0') ^ (keyStream.charAt(i % keyStream.length()) - '0');
            encrypted.append(bit);
        }

        System.out.println("\nEncrypted binary: " + encrypted);
        String encryptedText = binaryToString(encrypted.toString());
        System.out.println("Encrypted text: " + encryptedText);

        // Decrypt
        StringBuilder decrypted = new StringBuilder();
        for (int i = 0; i < encrypted.length(); i++) {
            int bit = (encrypted.charAt(i) - '0') ^ (keyStream.charAt(i % keyStream.length()) - '0');
            decrypted.append(bit);
        }

        System.out.println("\nDecrypted binary: " + decrypted);
        String decryptedText = binaryToString(decrypted.toString());
        System.out.println("Decrypted text: " + decryptedText);
    }

    // Helper for polynomial type test
    public static String regToString(int[] reg) {
        StringBuilder sb = new StringBuilder();
        for (int bit : reg) sb.append(bit);
        return sb.toString();
    }

    public static String testPolynomialType(List<Integer> taps, int[] initReg) {
        int registerLength = initReg.length;
        int[] reg = Arrays.copyOf(initReg, registerLength);
        String startState = regToString(reg);
        int maxPeriod = (1 << registerLength) - 1;
        int step = 0;

        while (true) {
            step++;
            stepLFSR(reg, taps);
            String currentState = regToString(reg);
            int index = step + 1;

            if (currentState.equals(startState)) {
                if (step == maxPeriod)
                    return "Primitive at index" + index;
                else if (step > 1)
                    return "Irreducible at index" + index;
                else
                    return "Reducible";
            }

            if (step > maxPeriod)
                break;
        }

        return "Reducible";
    }

    public static void main(String[] args) {
        String name = getUserName();
        int m = getMValue();
        List<Integer> taps = getPolynomial(m);
        String initialVector = getInitialVector(m);

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter warm-up steps to ignore: ");
        int warmUpSteps = sc.nextInt();

        processLFSR(name, initialVector, m, taps, warmUpSteps);

        int[] initReg = new int[m];
        for (int i = 0; i < m; i++) initReg[i] = Character.getNumericValue(initialVector.charAt(i));

        System.out.println("\nPolynomial Type: " + testPolynomialType(taps, initReg));
    }
}