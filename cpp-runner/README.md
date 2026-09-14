# Putra C++ Runner

Runner C++ ringan berbasis web untuk HP. Tulis kode, isi stdin, lalu tekan **Run C++** tanpa membuka terminal.

## Cara kerja

- Editor C++ langsung di browser.
- Compiler dipilih dari daftar Compiler Explorer/Godbolt.
- Eksekusi dikirim ke sandbox Compiler Explorer.
- Mendukung input `stdin` dan flag seperti `-std=c++20 -O2`.
- Tidak memerlukan compiler lokal di HP.

## Catatan

Eksekusi membutuhkan koneksi internet karena compiler berjalan di server. Jangan masukkan API key, password, atau data rahasia ke editor/input.
