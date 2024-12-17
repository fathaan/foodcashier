Berikut adalah panduan step by step lengkap untuk cloning dan menjalankan proyek aplikasi kelompok FAGL serta penjelasan fitur, dependensi, dan screenshot UI aplikasi.

---

## 📲 Rancang Bangun Aplikasi Kasir Mobile, Project Mandiri - Politeknik Negeri Lampung

<div style="text-align: center;">
    <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/UI_Screenshot.png" alt="splashscreen" width="500"/>
</div>


**Deskripsi Proyek**  
Aplikasi kasir berbasis mobile ini dirancang untuk mempermudah proses transaksi di kantin. Aplikasi ini otomatis mencatat riwayat pembelian, memantau pendapatan harian, dan menyediakan struk digital untuk efisiensi pengguna.

---

### Langkah 1: Menyiapkan Lingkungan Kerja

#### Prasyarat
1. **Android Studio**: Pastikan Anda telah menginstal Android Studio versi terbaru.
2. **Gradle**: Biasanya sudah termasuk dalam instalasi Android Studio.
3. **JDK**: Pastikan JDK yang sesuai sudah terpasang dan didukung oleh Android Studio.

---

### Langkah 2: Clone Project dari GitLab

1. **Clone Repository ke Komputer Anda**  
   Untuk memulai, clone repository dari GitLab ke komputer lokal:
   ```bash
   git clone <URL_Repository>
   ```

2. **Buka di Android Studio**  
   Buka Android Studio dan pilih **“Open an existing Android Studio project”**, lalu arahkan ke folder proyek yang telah di-clone.

3. **Sinkronkan Proyek dengan Gradle**  
   Setelah proyek terbuka, sinkronkan dengan Gradle agar semua dependensi terunduh dan terintegrasi dengan benar.  
   Klik **"Sync Now"** di bagian atas Android Studio jika ada peringatan sinkronisasi.

4. **Jalankan Proyek**  
   Setelah sinkronisasi berhasil, Anda dapat menjalankan proyek dengan menekan tombol **Run** (ikon ▶️) di Android Studio.

---

### 🔗 Penjelasan Dependensi Proyek

**Dependensi Firebase**
1. **Firebase BOM**: Mengelola konsistensi versi Firebase.
2. **Firebase Authentication**: Mengotentikasi pengguna saat login.
3. **Firestore**: Penyimpanan noSQL untuk transaksi dan data produk.
4. **Firebase Storage**: Menyimpan file digital seperti struk atau gambar produk.

```gradle
implementation platform('com.google.firebase:firebase-bom:31.0.0')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-storage'
```

**Dependensi Dasar Aplikasi Android**
1. **AppCompat**: Mendukung kompatibilitas UI.
2. **ConstraintLayout**: Membuat layout yang fleksibel.
3. **Material, Activity, CardView**: Mendukung desain material dan tampilan kartu.

```gradle
implementation 'androidx.appcompat:appcompat:1.7.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.activity:activity:1.5.1'
implementation 'com.google.android.material:material:1.6.1'
implementation 'androidx.cardview:cardview:1.0.0'
```

**Splash API**
- Menampilkan layar splash ketika aplikasi mulai.

```gradle
implementation 'androidx.core:core-splashscreen:1.0.0'
```

**Fragment dan Navigasi**
- **Fragment**: Mempermudah modularitas UI.
- **Navigasi**: Mengelola navigasi antar halaman.

```gradle
implementation 'androidx.fragment:fragment:1.5.5'
implementation 'androidx.navigation:navigation-fragment:2.5.3'
implementation 'androidx.navigation:navigation-ui:2.5.3'
```

**RecyclerView**
- Menampilkan daftar item (misalnya, produk atau transaksi).

```gradle
implementation 'androidx.recyclerview:recyclerview:1.3.0'
```

**Room Database (SQLite)**
- Menyimpan data secara lokal untuk caching.
  
```gradle
implementation 'androidx.room:room-runtime:2.5.0'
annotationProcessor 'androidx.room:room-compiler:2.5.0'
```

**Pencetakan Bluetooth** *(Opsional)*
- Menyediakan pencetakan struk digital melalui Bluetooth.

---

### ✨ Fitur Utama Aplikasi

1. **Pencatatan Transaksi Otomatis**  
   Setiap transaksi yang dilakukan akan dicatat otomatis dalam aplikasi, memberikan kemudahan pemilik kantin dalam mengawasi penjualan.

2. **Laporan Pendapatan Harian**  
   Memungkinkan pemantauan pendapatan harian secara real-time.

3. **Struk Digital**  
   Menyediakan struk digital yang dapat diakses atau dicetak dengan perangkat Bluetooth.

4. **Pengelolaan Produk dan Harga**  
   Memungkinkan pengguna menambahkan, memperbarui, atau menghapus produk beserta harga.

5. **Otentikasi Pengguna dengan Firebase**  
   Melindungi data transaksi dengan autentikasi pengguna Firebase.

6. **Caching Data Menggunakan Room Database**  
   Menyimpan data secara lokal untuk akses lebih cepat meskipun dalam kondisi offline.

---

### 📸 Screenshot UI Aplikasi


| **Tampilan**                           | **Deskripsi**                                       |
|----------------------------------------|-----------------------------------------------------|
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Splash_Screen.jpg" width="75"/>                      | Menampilkan logo aplikasi saat aplikasi dimulai.    |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Form_Login.jpg" width="75"/>                       | Autentikasi pengguna untuk mengakses aplikasi.      |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Form_Registrasi.jpg" width="75"/>                  | Formulir untuk mendaftarkan pengguna baru.          |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Home_Screen.jpg" width="75"/>                        | Tampilan utama dengan akses ke fitur aplikasi.      |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Profile_Screen.jpg" width="75"/>                     | Menampilkan dan mengelola profil pengguna.          |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Category_Screen.jpg" width="75"/>                    | Menampilkan kategori produk yang tersedia.          |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Menu_Screen.jpg" width="75"/>                        | Daftar produk yang dijual dalam setiap kategori.    |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Cashier_-_Order_Screen.jpg" width="75"/>      | Tampilan kasir untuk melakukan pembayaran pesanan.  |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Cashier_-_Detail_Order_Screen.jpg" width="75"/> | Rincian pesanan yang akan dibayar.                |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Receipt_Screen.jpg" width="75"/> | Menampilkan struk digital setelah pembayaran.   |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/History_Screen.jpg" width="75"/>               | Riwayat transaksi yang telah dilakukan.             |
| <img src="https://git.enigmacamp.com/cic/polinela-i/fagl-team/foodcashier-app/-/raw/master/UI%20Screenshot/Report_Income_Screen.jpg" width="75"/>         | Laporan pendapatan yang diperoleh dari transaksi.   |


---

### 📑 Keunggulan Aplikasi

- **Mudah Digunakan**: Aplikasi dirancang dengan antarmuka yang sederhana dan intuitif, cocok untuk pengguna dengan berbagai tingkat pengalaman teknologi.
- **Real-Time Data**: Penggunaan Firebase Firestore memberikan data transaksi secara real-time dan memungkinkan akses multi-perangkat.
- **Efisien dengan Struk Digital**: Aplikasi mendukung penyimpanan struk digital dan pencetakan Bluetooth, sehingga meminimalisir penggunaan kertas.
- **Data Aman**: Firebase Authentication memastikan bahwa data pengguna terlindungi dan hanya dapat diakses oleh pengguna yang terotorisasi.

--- 


### Progress App
Project ini Sudah 95% Selesai,
Hanya saja masih ada kekurangan pada : Fitur Forgot Password dan Fitur Filter Date Pada Report Income

