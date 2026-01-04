import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;

public class RSA {

    private static final Random random = new Random();
    private static final int S = 100; // Fermat iterations

    // =====================================================
    // Square and Multiply Algorithm
    // =====================================================
    public static BigInteger squareAndMultiplyAlgorithm(
            BigInteger x, BigInteger H, BigInteger n) {

        BigInteger result = BigInteger.ONE;
        String binaryExponent = H.toString(2); //convert the power to binary

        for (int i = 0; i < binaryExponent.length(); i++) {
            result = result.multiply(result).mod(n);
            if (binaryExponent.charAt(i) == '1') {
                result = result.multiply(x).mod(n);
            }
        }
        return result;
    }

    // =====================================================
    // Fermat Primality Test
    // =====================================================
    public static boolean fermatPrimalityTest(BigInteger p, int s) {

        if (p.compareTo(BigInteger.valueOf(2)) < 0) return false; //أي رقم أقل من 2 مش prime
        if (p.equals(BigInteger.valueOf(2))) return true;
        if (p.mod(BigInteger.TWO).equals(BigInteger.ZERO)) return false; //أي رقم زوجي غير 2 مش prime

        for (int i = 0; i < s; i++) {
            BigInteger a;
            do {
                a = new BigInteger(p.bitLength(), random);
            } while (a.compareTo(BigInteger.TWO) < 0 ||
                    a.compareTo(p.subtract(BigInteger.TWO)) > 0);



            if (!squareAndMultiplyAlgorithm(
                    a, p.subtract(BigInteger.ONE), p)
                    .equals(BigInteger.ONE)) {
                return false;
            }
        }
        return true;
    }

    // =====================================================
    // Prime Generation using Fermat Algorithm
    // =====================================================
    public static BigInteger generatePrimeUsingFermat(BigInteger x) {

        BigInteger p;
        do {
            p = new BigInteger(x.bitLength() + 2, random);
        } while (p.compareTo(x) <= 0 ||
                !fermatPrimalityTest(p, S));

        return p;
    }

    // =====================================================
    // Extended Euclidean Algorithm
    // =====================================================
    public static BigInteger extendedEuclideanAlgorithm(
            BigInteger e, BigInteger phi) {

        BigInteger originalPhi = phi;
        BigInteger x0 = BigInteger.ZERO;
        BigInteger x1 = BigInteger.ONE;

        while (e.compareTo(BigInteger.ONE) > 0) {
            BigInteger q = e.divide(phi);

            BigInteger temp = phi;
            phi = e.mod(phi);
            e = temp;

            temp = x0;
            x0 = x1.subtract(q.multiply(x0));
            x1 = temp;
        }

        if (x1.compareTo(BigInteger.ZERO) < 0) {
            x1 = x1.add(originalPhi);
        }

        return x1;
    }

    // =====================================================
    // GCD
    // =====================================================
    public static BigInteger computeGCD(
            BigInteger a, BigInteger b) {
        return a.gcd(b);
    }

    // =====================================================
    // RSA Key Generation
    // =====================================================
    public static BigInteger[] generateRSAKeys(BigInteger x) {

        // =====================================================
        // generate p, q, n, phi
        BigInteger p = generatePrimeUsingFermat(x);
        BigInteger q;
        do {
            q = generatePrimeUsingFermat(x);
        } while (q.equals(p));


        BigInteger n = p.multiply(q);
        BigInteger phi =
                p.subtract(BigInteger.ONE)
                        .multiply(q.subtract(BigInteger.ONE));
        // =====================================================
         // generate e and d
        // =====================================================
        BigInteger e;
        do {
            e = new BigInteger(phi.bitLength() - 1, random);
        } while (e.compareTo(BigInteger.ONE) <= 0 ||  //not less than 0 or equal/greater than phi or gcd !=1
                e.compareTo(phi) >= 0 ||
                !computeGCD(e, phi).equals(BigInteger.ONE));


        BigInteger d = extendedEuclideanAlgorithm(e, phi);

        return new BigInteger[]{p, q, n, phi, e, d};
    }

    // =====================================================
    // RSA Decryption using CRT
    // =====================================================
    public static BigInteger decryptUsingCRT(
            BigInteger cipherText,
            BigInteger p,
            BigInteger q,
            BigInteger d,
            BigInteger n) {


        BigInteger dp = d.mod(p.subtract(BigInteger.ONE));
        BigInteger dq = d.mod(q.subtract(BigInteger.ONE));

        BigInteger yp = cipherText.mod(p);
        BigInteger yq = cipherText.mod(q);


        BigInteger xp =
                squareAndMultiplyAlgorithm(yp, dp, p);
        BigInteger xq =
                squareAndMultiplyAlgorithm(yq, dq, q);

        BigInteger cp = extendedEuclideanAlgorithm(q, p);
        BigInteger cq = extendedEuclideanAlgorithm(p, q);

        return (q.multiply(cp).multiply(xp)
                .add(p.multiply(cq).multiply(xq)))
                .mod(n);
    }

    // =====================================================
    // MAIN
    // =====================================================
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter plain text: ");
        String plainText = sc.nextLine();

        System.out.println("\n===== RSA Processing =====");

        for (int i = 0; i < plainText.length(); i++) {

            char ch = plainText.charAt(i);
            BigInteger x = BigInteger.valueOf((int) ch);

            // -------- Key Generation --------
            BigInteger[] keys = generateRSAKeys(x);

            BigInteger p = keys[0];
            BigInteger q = keys[1];
            BigInteger n = keys[2];
            BigInteger e = keys[4];
            BigInteger d = keys[5];

            // -------- Encryption --------
            BigInteger cipherText =
                    squareAndMultiplyAlgorithm(x, e, n);

            // -------- Decryption --------
            BigInteger decryptedX =
                    decryptUsingCRT(cipherText, p, q, d, n);

            // -------- Output --------
            System.out.println("Character: '" + ch +
                    "' (ASCII: " + x + ")");
            System.out.println("p = " + p);
            System.out.println("q = " + q);
            System.out.println(
                    "Public Key (e, n) = (" + e + ", " + n + ")");
            System.out.println("Private Key d = " + d);
            System.out.println("Cipher Text = " + cipherText);
            System.out.println(
                    "Decrypted ASCII = " + decryptedX +
                            " ('" + (char) decryptedX.intValue() + "')");
            System.out.println("------------------------------------");
        }

        sc.close();
    }
}
