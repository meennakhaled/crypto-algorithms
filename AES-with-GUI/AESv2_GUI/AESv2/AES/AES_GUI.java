import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class AES_GUI extends JFrame {

    private JTextField inputField;
    private JTextField keyField;
    private JTextArea outputArea;
    private JTextArea logArea;

    public AES_GUI() {
        setTitle("AES-128 Encryption/Decryption");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        inputPanel.add(new JLabel("Plaintext / Ciphertext (Hex for Decrypt):"));
        inputField = new JTextField();
        inputPanel.add(inputField);

        inputPanel.add(new JLabel("Key (32 Hex Characters):"));
        keyField = new JTextField("12345678901234567890123456789000"); // default key
        inputPanel.add(keyField);

        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton encryptButton = new JButton("Encrypt");
        JButton decryptButton = new JButton("Decrypt");
        JButton clearButton = new JButton("Clear Logs");

        encryptButton.setBackground(new Color(70, 130, 180));
        encryptButton.setForeground(Color.WHITE);
        decryptButton.setBackground(new Color(220, 20, 60));
        decryptButton.setForeground(Color.WHITE);

        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Result"));
        outputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        outputArea = new JTextArea(3, 20);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        mainPanel.add(outputPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Detailed Execution Logs"));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        mainPanel.add(logPanel);

        PrintStream printStream = new PrintStream(new CustomOutputStream(logArea));
        System.setOut(printStream);

        encryptButton.addActionListener(e -> performEncryption());
        decryptButton.addActionListener(e -> performDecryption());
        clearButton.addActionListener(e -> logArea.setText(""));
    }

    private void performEncryption() {
        try {
            String plaintext = inputField.getText();
            String keyHexRaw = keyField.getText();

            if (plaintext == null || plaintext.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter plaintext.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // cleanup key
            String keyHex = keyHexRaw == null ? "" : keyHexRaw.trim().replaceAll("\\s+", "").toLowerCase();
            int expectedKeyHexLen = 32;
            int expectedKeyBytes = 16;

            if (!isHex(keyHex) || keyHex.length() != expectedKeyHexLen) {
                JOptionPane.showMessageDialog(this, "Key must be exactly " + expectedKeyHexLen + " hex characters (16 bytes).", "Key Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] keyBytes;
            try {
                keyBytes = AES_Utils.hexToBytes(keyHex, expectedKeyBytes);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to parse key: " + ex.getMessage(), "Key Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int[] expandedKey = KeyExpansion.expandKey(keyBytes);

            System.out.println("\n--- Starting Encryption ---");
            System.out.println("Plaintext: " + plaintext);
            System.out.println("Key: " + keyHex);

            byte[] bytes = plaintext.getBytes();
            int totalBlocks = (int) Math.ceil(bytes.length / 16.0);
            if (totalBlocks == 0) totalBlocks = 1;

            byte[] cipherBytes = new byte[totalBlocks * 16];

            for (int i = 0; i < totalBlocks; i++) {
                byte[] block = new byte[16];
                for (int j = 0; j < 16; j++) {
                    int idx = i * 16 + j;
                    if (idx < bytes.length) {
                        block[j] = bytes[idx];
                    } else {
                        block[j] = '#';
                    }
                }

                AESLogger.setContext("ENC", i + 1);
                byte[] encryptedBlock = AES_128.encryptBlock(block, expandedKey);
                System.arraycopy(encryptedBlock, 0, cipherBytes, i * 16, 16);
                AESLogger.clearContext();
            }

            String cipherHex = AES_Utils.bytesToHex(cipherBytes);
            outputArea.setText(cipherHex);

            // auto-fill inputField so user can click Decrypt immediately
            inputField.setText(cipherHex);

            System.out.println("Encryption Complete. Ciphertext (Hex): " + cipherHex);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void performDecryption() {
        try {
            String cipherHexRaw = inputField.getText();
            String keyHexRaw = keyField.getText();

            if (cipherHexRaw == null || cipherHexRaw.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter ciphertext (Hex).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // cleanup: remove whitespace/newlines and lowercase
            String cipherHex = cipherHexRaw.replaceAll("\\s+", "").toLowerCase();
            String keyHex = keyHexRaw == null ? "" : keyHexRaw.trim().replaceAll("\\s+", "").toLowerCase();

            int expectedKeyHexLen = 32;
            int expectedKeyBytes = 16;

            if (!isHex(keyHex) || keyHex.length() != expectedKeyHexLen) {
                JOptionPane.showMessageDialog(this, "Key must be exactly " + expectedKeyHexLen + " hex characters (16 bytes).", "Key Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isHex(cipherHex)) {
                JOptionPane.showMessageDialog(this, "Ciphertext must be hexadecimal.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cipherHex.length() % 32 != 0) {
                JOptionPane.showMessageDialog(this, "Ciphertext length must be a multiple of 32 hex characters (16-byte blocks).", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] keyBytes;
            try {
                keyBytes = AES_Utils.hexToBytes(keyHex, expectedKeyBytes);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to parse key: " + ex.getMessage(), "Key Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int[] expandedKey = KeyExpansion.expandKey(keyBytes);
            byte[] cipherBytes = AES_Utils.hexToBytes(cipherHex, cipherHex.length() / 2);

            System.out.println("\n--- Starting Decryption ---");
            System.out.println("Ciphertext (Hex): " + cipherHex);

            int totalBlocks = cipherBytes.length / 16;
            byte[] decryptedPaddedBytes = new byte[cipherBytes.length];

            for (int i = 0; i < totalBlocks; i++) {
                byte[] block = Arrays.copyOfRange(cipherBytes, i * 16, (i + 1) * 16);

                AESLogger.setContext("DEC", i + 1);
                byte[] decryptedBlock = AES_128.decryptBlock(block, expandedKey);
                System.arraycopy(decryptedBlock, 0, decryptedPaddedBytes, i * 16, 16);
                AESLogger.clearContext();
            }

            StringBuilder recovered = new StringBuilder();
            for (byte b : decryptedPaddedBytes) {
                if ((char) b == '#') break;
                recovered.append((char) b);
            }

            outputArea.setText(recovered.toString());
            System.out.println("Decryption Complete. Recovered Text: " + recovered.toString());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean isHex(String s) {
        return s != null && s.matches("(?i)^[0-9a-f]+$");
    }

    private class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new AES_GUI().setVisible(true));
    }
}
