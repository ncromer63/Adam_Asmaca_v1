import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class GameFrame extends JFrame {

    private static final String RESIMLER_KLASORU = "C:\\P2Oyun\\Resimler";
    private static final String KELIMELER_DOSYASI = "C:\\P2Oyun\\TXTDosyalar\\kelimeler.txt";
    private static final String OYUNLAR_DOSYASI = "C:\\P2Oyun\\TXTDosyalar\\oyunlar.txt";
    private static final String LOG_DOSYASI = "C:\\P2Oyun\\TXTDosyalar\\log.txt";
    private static final String SIFRE_DOSYASI = "C:\\P2Oyun\\TXTDosyalar\\sifre.txt";


    private String secilenKelime = "";
    private List<JLabel> harfLabels = new ArrayList<>();
    private int hataSayisi = 0;
    private int sure = 0;
    private int ipucuKullanimi = 0; 
    private int rekorPuan = 0; 
    private Timer timer;
    private Set<Character> kullanilanYanlisHarfler = new HashSet<>();


    private JLabel sureLabel;
    private JProgressBar sureBar;
    private JLabel resimLabel;
    private JLabel rekorLabel; 
    private JTextField harfTahminField;
    private JTextField kelimeTahminField;
    private JButton tahminEtButton;
    private JButton ipucuButton; 
    private JPanel harfPaneli;
    private JLabel yanlisHarflerLabel;
    private JLabel kalanHakLabel; 
    private JTable skorTable;
    private JTable logTable;
    private DefaultTableModel skorTableModel;
    private DefaultTableModel logTableModel;


    private CardLayout oyunCardLayout;
    private JPanel oyunMerkezCardPanel;
    private JPanel topControlPanel;

    private Color currentFg;

    public GameFrame() {
        setTitle("Adam Asmaca Projesi");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        topControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        
        rekorLabel = new JLabel("Rekor: 0");
        rekorLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton btnDark = createThemeButton("● Siyah", new Color(30, 30, 30), Color.WHITE);
        JButton btnNavy = createThemeButton("● Lacivert", new Color(15, 25, 40), Color.CYAN);
        JButton btnWhite = createThemeButton("● Beyaz", Color.WHITE, Color.DARK_GRAY);

        btnDark.addActionListener(e -> temaUygula(new Color(18, 18, 18), Color.WHITE, new Color(45, 45, 45)));
        btnNavy.addActionListener(e -> temaUygula(new Color(10, 20, 35), new Color(220, 220, 220), new Color(0, 150, 136)));
        btnWhite.addActionListener(e -> temaUygula(new Color(250, 250, 250), Color.BLACK, new Color(230, 230, 230)));

        topControlPanel.add(rekorLabel);
        topControlPanel.add(new JLabel("|  Görünüm: "));
        topControlPanel.add(btnDark);
        topControlPanel.add(btnNavy);
        topControlPanel.add(btnWhite);
        add(topControlPanel, BorderLayout.NORTH);


        JTabbedPane tabbedPane = new JTabbedPane();


        JPanel oyunPaneli = oyunOynamaPaneliOlustur();
        tabbedPane.addTab("Oyun Oyna", oyunPaneli);


        JPanel skorPaneli = eskiSkorlarPaneliOlustur();
        tabbedPane.addTab("Eski Skorlar", skorPaneli);

        JPanel logPaneli = loglarPaneliOlustur();
        tabbedPane.addTab("Loglar", logPaneli);

        add(tabbedPane, BorderLayout.CENTER);

        rekorYukle();

        temaUygula(new Color(10, 20, 35), new Color(220, 220, 220), new Color(0, 150, 136));
    }

    private JButton createThemeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg, 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        return btn;
    }

    private void temaUygula(Color bg, Color fg, Color compBg) {
        this.currentFg = fg;
        getContentPane().setBackground(bg);
        topControlPanel.setBackground(bg);
        temaRekursif(getContentPane(), bg, fg, compBg);
        
        JTable[] tables = {skorTable, logTable};
        for(JTable t : tables) {
            if (t != null) {
                t.setBackground(bg);
                t.setForeground(fg);
                t.setGridColor(compBg);
                t.getTableHeader().setBackground(compBg);
                t.getTableHeader().setForeground(fg);
            }
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void temaRekursif(Container container, Color bg, Color fg, Color compBg) {
        for (Component c : container.getComponents()) {
            if (topControlPanel != null && c instanceof JButton && topControlPanel.isAncestorOf(c)) { 
                continue; 
            }
            c.setBackground(bg);
            c.setForeground(fg);
            if (c instanceof JButton || c instanceof JTextField) {
                c.setBackground(compBg);
                c.setForeground(fg);
                if (c instanceof JTextField) {
                    ((JTextField) c).setCaretColor(fg);
                    ((JTextField) c).setBorder(BorderFactory.createLineBorder(fg, 1));
                }
            } else if (c instanceof JLabel) {
                if (c != kalanHakLabel && c != yanlisHarflerLabel) {
                    c.setForeground(fg);
                }
            } else if (c instanceof JProgressBar) {

                c.setBackground(compBg);
                continue; 
            }
            if (c instanceof Container) {
                temaRekursif((Container) c, bg, fg, compBg);
            }
        }
    }

    private JPanel oyunOynamaPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        harfPaneli = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.add(harfPaneli, BorderLayout.NORTH);

        oyunCardLayout = new CardLayout();
        oyunMerkezCardPanel = new JPanel(oyunCardLayout);

        JPanel baslangicGirisPaneli = new JPanel(new GridBagLayout());
        JButton merkezBaslaButton = new JButton("Oyuna Başla");
        merkezBaslaButton.setFont(new Font("Arial", Font.BOLD, 18));
        merkezBaslaButton.setPreferredSize(new Dimension(220, 60));
        merkezBaslaButton.addActionListener(e -> oyunuBaslat());
        baslangicGirisPaneli.add(merkezBaslaButton);

        JPanel aktifOyunPaneli = new JPanel(new BorderLayout(10, 10));

        resimLabel = new JLabel("", SwingConstants.CENTER);
        resimLabel.setPreferredSize(new Dimension(300, 300));
        aktifOyunPaneli.add(resimLabel, BorderLayout.NORTH);

        JPanel merkezAltPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel tahminPaneli = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        harfTahminField = new JTextField(5);
        kelimeTahminField = new JTextField(15);
        
        tahminEtButton = new JButton("Tahmin Et");
        tahminEtButton.setEnabled(false);
        
        ipucuButton = new JButton("İpucu Al (-2 Hak) [3/3]");
        ipucuButton.setEnabled(false);

        harfTahminField.addActionListener(e -> tahminEt());
        kelimeTahminField.addActionListener(e -> tahminEt());

        tahminPaneli.add(new JLabel("Harf:"));
        tahminPaneli.add(harfTahminField);
        tahminPaneli.add(new JLabel("Kelime:"));
        tahminPaneli.add(kelimeTahminField);
        tahminPaneli.add(tahminEtButton);
        tahminPaneli.add(ipucuButton);

        JPanel yanlisHarflerPaneli = new JPanel(new FlowLayout(FlowLayout.CENTER));
        yanlisHarflerPaneli.add(new JLabel("Kullanılan Yanlış Harfler:"));
        yanlisHarflerLabel = new JLabel("");
        yanlisHarflerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        yanlisHarflerLabel.setForeground(Color.RED);
        yanlisHarflerPaneli.add(yanlisHarflerLabel);

        merkezAltPanel.add(tahminPaneli);
        merkezAltPanel.add(yanlisHarflerPaneli);
        aktifOyunPaneli.add(merkezAltPanel, BorderLayout.CENTER);

        oyunMerkezCardPanel.add(baslangicGirisPaneli, "GIRIS_EKRANI");
        oyunMerkezCardPanel.add(aktifOyunPaneli, "OYUN_EKRANI");

        oyunCardLayout.show(oyunMerkezCardPanel, "GIRIS_EKRANI");
        panel.add(oyunMerkezCardPanel, BorderLayout.CENTER);

        JPanel altPanel = new JPanel(new BorderLayout(10, 10));
        altPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20)); 
        
        JPanel surePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        surePanel.setOpaque(false);
        sureLabel = new JLabel("Süre: 0 sn");
        sureLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        sureBar = new JProgressBar(0, 30);
        sureBar.setValue(0);
        sureBar.setPreferredSize(new Dimension(150, 18));
        sureBar.setForeground(new Color(46, 204, 113));
        sureBar.setBorderPainted(false);
        
        surePanel.add(sureLabel);
        surePanel.add(sureBar);
        
        kalanHakLabel = new JLabel("Kalan Hak: 11", SwingConstants.CENTER);
        kalanHakLabel.setFont(new Font("Arial", Font.BOLD, 16));
        kalanHakLabel.setForeground(Color.WHITE);
        kalanHakLabel.setBackground(new Color(220, 20, 60)); 
        kalanHakLabel.setOpaque(true); 
        kalanHakLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15)); 

        altPanel.add(surePanel, BorderLayout.WEST);
        altPanel.add(kalanHakLabel, BorderLayout.EAST);
        panel.add(altPanel, BorderLayout.SOUTH);

        tahminEtButton.addActionListener(e -> tahminEt());
        ipucuButton.addActionListener(e -> ipucuVer());

        return panel;
    }

    private JPanel eskiSkorlarPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] kolonlar = {"Tarih/Saat", "Süre", "Sonuç", "Puan"}; 
        skorTableModel = new DefaultTableModel(kolonlar, 0);
        skorTable = new JTable(skorTableModel);
        JScrollPane scrollPane = new JScrollPane(skorTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton temizleButton = new JButton("Temizle");
        temizleButton.addActionListener(e -> {
            String sifre = JOptionPane.showInputDialog(panel, "Lütfen şifreyi girin:");
            if (sifre != null && sifre.equals(getSifre())) {
                try (FileWriter writer = new FileWriter(OYUNLAR_DOSYASI, false)) {
                    writer.write("");
                    skorTableModel.setRowCount(0);
                    rekorPuan = 0;
                    rekorLabel.setText("Rekor: 0");
                    logYaz("Eski skorlar temizlendi");
                    loglariYukle();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel, "Dosya temizlenirken hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } else if (sifre != null) {
                JOptionPane.showMessageDialog(panel, "Yanlış şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(temizleButton, BorderLayout.SOUTH);
        skorlariYukle();

        return panel;
    }

    private JPanel loglarPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] kolonlar = {"Tarih/Saat", "Log Mesajı"};
        logTableModel = new DefaultTableModel(kolonlar, 0);
        logTable = new JTable(logTableModel);
        JScrollPane scrollPane = new JScrollPane(logTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton temizleButton = new JButton("Temizle");
        temizleButton.addActionListener(e -> {
            String sifre = JOptionPane.showInputDialog(panel, "Lütfen şifreyi girin:");
            if (sifre != null && sifre.equals(getSifre())) {
                try (FileWriter writer = new FileWriter(LOG_DOSYASI, false)) {
                    writer.write("");
                    logTableModel.setRowCount(0);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel, "Dosya temizlenirken hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            } else if (sifre != null) {
                JOptionPane.showMessageDialog(panel, "Yanlış şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(temizleButton, BorderLayout.SOUTH);
        loglariYukle();

        return panel;
    }

    private void oyunuBaslat() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        secilenKelime = kelimeSec();
        if (secilenKelime == null) {
            JOptionPane.showMessageDialog(this, "Uygun kelime bulunamadı! Lütfen kelimeler.txt dosyasını kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        oyunCardLayout.show(oyunMerkezCardPanel, "OYUN_EKRANI");

        harfLabels.clear();
        harfPaneli.removeAll();

        for (int i = 0; i < secilenKelime.length(); i++) {
            JLabel label = new JLabel("*");
            label.setFont(new Font("Arial", Font.BOLD, 24));
            if (currentFg != null) {
                label.setForeground(currentFg);
            }
            harfPaneli.add(label);
            harfLabels.add(label);
        }

        hataSayisi = 0;
        sure = 0;
        ipucuKullanimi = 0; 
        kullanilanYanlisHarfler.clear();
        
        sureLabel.setText("Süre: 0 sn");
        sureBar.setValue(0);
        sureBar.setForeground(new Color(46, 204, 113));

        yanlisHarflerLabel.setText("");
        kalanHakLabel.setText("Kalan Hak: 11"); 

        resimYukle(0);
        tahminEtButton.setEnabled(true);
        ipucuButton.setEnabled(true);
        ipucuButton.setText("İpucu Al (-2 Hak) [3/3]"); 

        harfTahminField.setText("");
        kelimeTahminField.setText("");
        harfTahminField.requestFocus();


        timer = new Timer(1000, e -> {
            sure++;
            sureLabel.setText("Süre: " + sure + " sn");
            
            if (sure <= 20) {
                sureBar.setValue(sure);
                sureBar.setForeground(new Color(46, 204, 113));
            } else if (sure <= 30) {
                sureBar.setValue(sure);
                sureBar.setForeground(new Color(243, 156, 18)); 
            } else {
                sureBar.setValue(30);
                sureBar.setForeground(new Color(231, 76, 60));
            }
        });
        timer.start();

        harfPaneli.revalidate();
        harfPaneli.repaint();
    }

    private String kelimeSec() {
        try {
            File dosya = new File(KELIMELER_DOSYASI);
            if (!dosya.exists()) return null;

            List<String> kelimeler = Files.readAllLines(Paths.get(KELIMELER_DOSYASI));
            List<String> uygunKelimeler = new ArrayList<>();
            for (String kelime : kelimeler) {
                kelime = kelime.trim();
                if (kelime.length() >= 6) {
                    uygunKelimeler.add(kelime);
                }
            }
            if (uygunKelimeler.isEmpty()) return null;
            Random random = new Random();
            return uygunKelimeler.get(random.nextInt(uygunKelimeler.size())).toUpperCase(Locale.ENGLISH);
        } catch (IOException e) {
            return null;
        }
    }

    private void ipucuVer() {
        if (ipucuKullanimi >= 3) {
            JOptionPane.showMessageDialog(this, "Maksimum ipucu hakkınızı (3/3) kullandınız!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (11 - hataSayisi <= 2) {
            JOptionPane.showMessageDialog(this, "İpucu kullanmak için en az 3 hakkınız olmalıdır!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Character> acilmamisHarfler = new ArrayList<>();
        for (int i = 0; i < secilenKelime.length(); i++) {
            if (harfLabels.get(i).getText().equals("*")) {
                char c = secilenKelime.charAt(i);
                if (!acilmamisHarfler.contains(c)) {
                    acilmamisHarfler.add(c);
                }
            }
        }

        if (acilmamisHarfler.isEmpty()) return;

        char secilenIpucuHarfi = acilmamisHarfler.get(new Random().nextInt(acilmamisHarfler.size()));

        for (int i = 0; i < secilenKelime.length(); i++) {
            if (secilenKelime.charAt(i) == secilenIpucuHarfi) {
                harfLabels.get(i).setText(String.valueOf(secilenIpucuHarfi));
            }
        }

        hataSayisi += 2;
        ipucuKullanimi++;
        
        resimYukle(hataSayisi);
        kalanHakLabel.setText("Kalan Hak: " + (11 - hataSayisi));
        ipucuButton.setText("İpucu Al (-2 Hak) [" + (3 - ipucuKullanimi) + "/3]");
        
        if (ipucuKullanimi >= 3) {
            ipucuButton.setEnabled(false);
        }
        
        logYaz("İpucu kullanıldı (-2 hak). Açılan harf: " + secilenIpucuHarfi);

        boolean tumHarflerAcildi = true;
        for (JLabel label : harfLabels) {
            if (label.getText().equals("*")) {
                tumHarflerAcildi = false;
                break;
            }
        }

        if (tumHarflerAcildi) {
            oyunuBitir(true);
        } else if (hataSayisi >= 11) {
            oyunuBitir(false);
        }

        harfTahminField.requestFocus();
    }

    private void tahminEt() {
        String harfTahmin = harfTahminField.getText().trim().toUpperCase(Locale.ENGLISH);
        String kelimeTahmin = kelimeTahminField.getText().trim().toUpperCase(Locale.ENGLISH);

        if (harfTahmin.isEmpty() && kelimeTahmin.isEmpty()) return;

        if (!harfTahmin.isEmpty() && !kelimeTahmin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen aynı anda hem harf hem kelime tahmini yapmayın.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            harfTahminField.setText("");
            kelimeTahminField.setText("");
            return;
        }

        boolean harfDogru = false;
        boolean kelimeDogru = kelimeTahmin.equals(secilenKelime);

        if (!harfTahmin.isEmpty()) {
            char tahminEdilenHarf = harfTahmin.charAt(0);

            if (kullanilanYanlisHarfler.contains(tahminEdilenHarf)) {
                JOptionPane.showMessageDialog(this, "Bu harfi zaten denediniz ve yanlış çıktı!", "Uyarı", JOptionPane.WARNING_MESSAGE);
                harfTahminField.setText("");
                return;
            }

            boolean zatenBulundu = false;
            for (JLabel label : harfLabels) {
                if (label.getText().equals(String.valueOf(tahminEdilenHarf))) {
                    zatenBulundu = true;
                    break;
                }
            }
            
            if (zatenBulundu) {
                JOptionPane.showMessageDialog(this, "Bu harf zaten tabloda açık!", "Uyarı", JOptionPane.WARNING_MESSAGE);
                harfTahminField.setText("");
                return;
            }

            for (int i = 0; i < secilenKelime.length(); i++) {
                if (secilenKelime.charAt(i) == tahminEdilenHarf && harfLabels.get(i).getText().equals("*")) {
                    harfLabels.get(i).setText(String.valueOf(tahminEdilenHarf));
                    harfDogru = true;
                }
            }

            if (!harfDogru) {
                kullanilanYanlisHarfler.add(tahminEdilenHarf);
                StringBuilder yanlislarStr = new StringBuilder();
                for (Character c : kullanilanYanlisHarfler) {
                    yanlislarStr.append(c).append("  ");
                }
                yanlisHarflerLabel.setText(yanlislarStr.toString());
            }
        }

        if (kelimeDogru) {
            for (int i = 0; i < secilenKelime.length(); i++) {
                harfLabels.get(i).setText(String.valueOf(secilenKelime.charAt(i)));
            }
            oyunuBitir(true);
            return;
        }

        if (!harfDogru && !kelimeDogru) {
            hataSayisi++;
            resimYukle(hataSayisi);
            kalanHakLabel.setText("Kalan Hak: " + (11 - hataSayisi)); 
            
            if (hataSayisi >= 11) {
                for (int i = 0; i < secilenKelime.length(); i++) {
                    harfLabels.get(i).setText(String.valueOf(secilenKelime.charAt(i)));
                }
                oyunuBitir(false);
                return;
            }
        }

        boolean tumHarflerAcildi = true;
        for (JLabel label : harfLabels) {
            if (label.getText().equals("*")) {
                tumHarflerAcildi = false;
                break;
            }
        }
        
        if (tumHarflerAcildi) {
            oyunuBitir(true);
        }

        harfTahminField.setText("");
        kelimeTahminField.setText("");
        harfTahminField.requestFocus();
    }

    private void oyunuBitir(boolean kazandi) {
        timer.stop();
        tahminEtButton.setEnabled(false);
        ipucuButton.setEnabled(false);
        
        int kazanilanPuan = 0;
        if (kazandi) {
            kazanilanPuan = 100;
            if (sure > 30) {
                kazanilanPuan -= (sure - 30) * 1; 
            }
            kazanilanPuan -= (ipucuKullanimi * 10); 
            
            if (kazanilanPuan < 0) {
                kazanilanPuan = 0; 
            }
        }

        String sonuc = kazandi ? "Kazandı" : "Kaybetti";
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String tarihSaat = dtf.format(LocalDateTime.now());
        
        String kayit = tarihSaat + "," + sure + "," + sonuc + "," + kazanilanPuan + "\n";

        try (FileWriter writer = new FileWriter(OYUNLAR_DOSYASI, true)) {
            writer.write(kayit);
            logYaz("Oyun sonlandı: " + sonuc + ", Süre: " + sure + " sn, Puan: " + kazanilanPuan);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Oyun kaydedilirken hata oluştu!", "Hata", JOptionPane.ERROR_MESSAGE);
        }

        skorlariYukle();
        loglariYukle();

        boolean rekorKirildi = false;
        if (kazanilanPuan > rekorPuan) {
            rekorPuan = kazanilanPuan;
            rekorLabel.setText("Rekor: " + rekorPuan);
            rekorKirildi = true;
            konfetiPatlat(); 
        }

        String tebrikEki = rekorKirildi ? "\nYENİ REKOR KIRILDI!" : "";
        String mesaj = "Oyun sonlandı! Sonuç: " + sonuc + "\nSüre: " + sure + " saniye\nPuan: " + kazanilanPuan + tebrikEki + "\n\nNe yapmak istersiniz?";
        Object[] secenekler = {"Oyuna Devam Et", "Oyunu Bırak"};

        int secim = JOptionPane.showOptionDialog(
                this, mesaj, "Oyun Bitti",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, secenekler, secenekler[0]
        );

        if (secim == JOptionPane.YES_OPTION) {
            oyunuBaslat();
        } else {
            System.exit(0);
        }
    }

    private void konfetiPatlat() {
        class ConfettiPanel extends JPanel {
            private final List<Point> points = new ArrayList<>();
            private final List<Color> colors = new ArrayList<>();
            private final List<Integer> sizes = new ArrayList<>();
            private int frames = 0;
            private final Timer animTimer;

            public ConfettiPanel() {
                setOpaque(false);
                Random r = new Random();
                for (int i = 0; i < 150; i++) {
                    points.add(new Point(r.nextInt(950), r.nextInt(680) - 680));
                    colors.add(new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
                    sizes.add(r.nextInt(8) + 6);
                }
                animTimer = new Timer(25, e -> {
                    frames++;
                    for (int i = 0; i < points.size(); i++) {
                        Point p = points.get(i);
                        p.y += r.nextInt(5) + 4; 
                        p.x += r.nextInt(3) - 1; 
                    }
                    repaint();
                    if (frames > 100) { 
                        ((Timer) e.getSource()).stop();
                        setVisible(false);
                    }
                });
            }
            public void start() { animTimer.start(); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (int i = 0; i < points.size(); i++) {
                    g.setColor(colors.get(i));
                    Point p = points.get(i);
                    g.fillRect(p.x, p.y, sizes.get(i), sizes.get(i));
                }
            }
        }

        ConfettiPanel cp = new ConfettiPanel();
        setGlassPane(cp);
        cp.setVisible(true);
        cp.start();
    }

    private void resimYukle(int hataSayisi) {
        if (hataSayisi == 0) {
            resimLabel.setIcon(null);
            resimLabel.setText("");
            return;
        }
        if (hataSayisi > 11) hataSayisi = 11;
        
        String resimYolu = RESIMLER_KLASORU + "\\" + hataSayisi + ".jpg";
        File dosya = new File(resimYolu);
        if (dosya.exists()) {
            resimLabel.setText("");
            ImageIcon icon = new ImageIcon(resimYolu);
            Image img = icon.getImage().getScaledInstance(250, 250, Image.SCALE_SMOOTH);
            resimLabel.setIcon(new ImageIcon(img));
        } else {
            resimLabel.setIcon(null);
            resimLabel.setText("Resim eksik: " + hataSayisi + ".jpg");
        }
    }

    private void rekorYukle() {
        try {
            File dosya = new File(OYUNLAR_DOSYASI);
            if (!dosya.exists()) return;

            List<String> satirlar = Files.readAllLines(Paths.get(OYUNLAR_DOSYASI));
            for (String satir : satirlar) {
                String[] parcalar = satir.split(",");
                if (parcalar.length == 4) { 
                    int p = Integer.parseInt(parcalar[3].trim());
                    if (p > rekorPuan) {
                        rekorPuan = p;
                    }
                }
            }
            rekorLabel.setText("Rekor: " + rekorPuan);
        } catch (Exception e) {
            rekorLabel.setText("Rekor: 0");
        }
    }

    private void skorlariYukle() {
        try {
            File dosya = new File(OYUNLAR_DOSYASI);
            if (!dosya.exists()) return;

            List<String> satirlar = Files.readAllLines(Paths.get(OYUNLAR_DOSYASI));
            skorTableModel.setRowCount(0);
            for (String satir : satirlar) {
                String[] parcalar = satir.split(",");
                if (parcalar.length == 3) {
                    String[] uyumluParcalar = {parcalar[0], parcalar[1], parcalar[2], "0"};
                    skorTableModel.addRow(uyumluParcalar);
                } else if (parcalar.length == 4) {
                    skorTableModel.addRow(parcalar);
                }
            }
        } catch (IOException e) {
        }
    }

    private void loglariYukle() {
        try {
            File dosya = new File(LOG_DOSYASI);
            if (!dosya.exists()) return;

            List<String> satirlar = Files.readAllLines(Paths.get(LOG_DOSYASI));
            logTableModel.setRowCount(0);
            for (String satir : satirlar) {
                String[] parcalar = satir.split(" - ");
                if (parcalar.length == 2) {
                    logTableModel.addRow(new Object[]{parcalar[0], parcalar[1]});
                }
            }
        } catch (IOException e) {
        }
    }

    private String getSifre() {
        try {
            return new String(Files.readAllBytes(Paths.get(SIFRE_DOSYASI))).trim();
        } catch (IOException e) {
            return "";
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            if (loginDialog.isBasariliGiris()) {
                GameFrame gameFrame = new GameFrame();
                gameFrame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}