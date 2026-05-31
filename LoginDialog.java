import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginDialog extends JDialog {

    private static final String KLASOR_YOLU = "C:\\P2Oyun\\TXTDosyalar";
    private static final String SIFRE_DOSYASI = KLASOR_YOLU + "\\sifre.txt";
    private static final String LOG_DOSYASI = KLASOR_YOLU + "\\log.txt";

    private JPasswordField passwordField;
    private JLabel infoLabel;
    private JLabel dateTimeLabel;
    private boolean ilkKurulum = false;
    private String kayitliSifre = "";
    private int hataSayaci = 0;
    private boolean basariliGiris = false;

    public LoginDialog(Frame owner) {
        super(owner, "Şifre Ekranı", true);
        setLayout(new BorderLayout(10, 10));
        setSize(450, 300);
        setLocationRelativeTo(owner);

        klasorKontrol();

        sifreDosyasiKontrol();


        infoLabel = new JLabel(ilkKurulum ? "İlk kez açtınız, lütfen yeni bir şifre belirleyin" : "Lütfen giriş için şifreyi yazın", SwingConstants.CENTER);
        passwordField = new JPasswordField(12);
        JButton onaylaButton = new JButton("Onayla/Giriş Yap");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel = new JLabel(dtf.format(LocalDateTime.now()), SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.add(infoLabel);
        
        JPanel passWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passWrapper.add(passwordField);
        centerPanel.add(passWrapper);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(dateTimeLabel, BorderLayout.NORTH);
        southPanel.add(onaylaButton, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);


        onaylaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sifreKontrol();
            }
        });

        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sifreKontrol();
            }
        });
    }

    private void klasorKontrol() {
        File klasor = new File(KLASOR_YOLU);
        if (!klasor.exists()) {
            klasor.mkdirs();
            logYaz("Klasör oluşturuldu: " + KLASOR_YOLU);
        }
    }

    private void sifreDosyasiKontrol() {
        File sifreDosyasi = new File(SIFRE_DOSYASI);
        if (!sifreDosyasi.exists() || sifreDosyasi.length() == 0) {
            ilkKurulum = true;
        } else {
            try {
                kayitliSifre = new String(Files.readAllBytes(Paths.get(SIFRE_DOSYASI))).trim();
            } catch (IOException ex) {
                logYaz("Şifre dosyası okunurken hata: " + ex.getMessage());
            }
        }
    }

    private void sifreKontrol() {
        char[] girilenSifre = passwordField.getPassword();
        String girilenSifreStr = new String(girilenSifre);

        if (ilkKurulum) {
            try (FileWriter writer = new FileWriter(SIFRE_DOSYASI)) {
                writer.write(girilenSifreStr);
                logYaz("Yeni şifre oluşturuldu");
                JOptionPane.showMessageDialog(this, "Şifre başarıyla kaydedildi.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
                
                ilkKurulum = false;
                kayitliSifre = girilenSifreStr; 
                
                infoLabel.setText("Lütfen giriş için şifreyi yazın");
                passwordField.setText("");
            } catch (IOException ex) {
                logYaz("Şifre kaydedilirken hata: " + ex.getMessage());
            }
        } else {
            if (girilenSifreStr.equals(kayitliSifre)) {
                logYaz("Başarılı giriş yapıldı");
                basariliGiris = true;
                dispose();
            } else {
                hataSayaci++;
                logYaz("Hatalı giriş denemesi");
                if (hataSayaci >= 3) {
                    JOptionPane.showMessageDialog(this, "3 kez hatalı giriş yaptınız. Program kapanıyor.", "Hata", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(this, "Hatalı şifre, kalan hak: " + (3 - hataSayaci), "Hata", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void logYaz(String mesaj) {
        try (FileWriter writer = new FileWriter(LOG_DOSYASI, true)) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String logSatiri = dtf.format(LocalDateTime.now()) + " - " + mesaj + "\n";
            writer.write(logSatiri);
        } catch (IOException ex) {
            System.err.println("Log yazılırken hata: " + ex.getMessage());
        }
    }

    public boolean isBasariliGiris() {
        return basariliGiris;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog dialog = new LoginDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            if (dialog.isBasariliGiris()) {
                System.out.println("Giriş başarılı!");
            }
        });
    }
}