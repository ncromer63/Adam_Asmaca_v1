# Adam Asmaca - V1 (Akademik Teslim Sürümü)

Bu proje, Süleyman Demirel Üniversitesi Bilgisayar Mühendisliği bölümü ödev yönergesine kesin bir şekilde uyumlu olarak geliştirilmiş Adam Asmaca oyununun temel sürümüdür.

## Sistem Gereksinimleri ve Dosya Yolları

Proje, yönerge gereği dinamik veri dosyalarını ve oyun görsellerini projenin kendi klasöründen değil, doğrudan bilgisayarın `C:\` sürücüsündeki kök dizinden okuyacak şekilde tasarlanmıştır. Programın çalışabilmesi için aşağıdaki dizin yapısının bilgisayarda mevcut olması zorunludur:

* `C:\P2Oyun\TXTDosyalar\kelimeler.txt` (Oyunun çekeceği kelime havuzu)
* `C:\P2Oyun\TXTDosyalar\oyunlar.txt` (Oynanan oyunların skor kayıtları)
* `C:\P2Oyun\TXTDosyalar\log.txt` (Sistem logları)
* `C:\P2Oyun\Resimler\` (Adam asılma adımlarını gösteren 1.jpg, 2.jpg... dosyaları)

## Kurulum ve Çalıştırma

1. `P2Oyun` klasörünü eksiksiz bir şekilde bilgisayarınızın `C:\` dizinine yerleştirin.
2. Proje kodlarının bulunduğu dizinde terminal/komut satırı açın.
3. Kodu derleyin ve çalıştırın:
   ```bash
   javac GameFrame.java
   java GameFrame