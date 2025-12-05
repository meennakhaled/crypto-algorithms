public class AESLogger {
    private static String phase = "";
    private static int block = -1;

    public static void setContext(String p, int b) {
        phase = p;
        block = b;
    }

    public static void clearContext() {
        phase = "";
        block = -1;
    }

    public static void round(int round) {
        String blk = block >= 0 ? " B" + block : "";
        System.out.println("\n[" + phase + blk + "][ROUND " + round + "]");
    }

    public static void section(String title) {
        String blk = block >= 0 ? " B" + block : "";
        System.out.println("\n[" + phase + blk + "] " + title);
    }

    public static void info(String msg) {
        System.out.println("  - " + msg);
    }
}
