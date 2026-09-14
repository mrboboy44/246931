#!/usr/bin/env python3
"""
Business Manager - offline-first personal business toolkit.

Works well in Termux on Android and also on Windows/macOS/Linux.
No account, API key, or internet connection is required.
Data is stored locally in business_data.json.

Features:
- Products/services and prices
- Sales transactions
- Expense tracking
- Automatic profit calculation
- Stock tracking
- Customer notes
- Daily/monthly summaries
- CSV export
"""

import csv
import json
import os
from datetime import datetime

DATA_FILE = "business_data.json"


def load_data():
    if not os.path.exists(DATA_FILE):
        return {"products": [], "sales": [], "expenses": [], "customers": []}
    try:
        with open(DATA_FILE, "r", encoding="utf-8") as f:
            data = json.load(f)
        for key in ("products", "sales", "expenses", "customers"):
            data.setdefault(key, [])
        return data
    except (OSError, json.JSONDecodeError):
        print("Data file tidak dapat dibaca. Membuat database baru.")
        return {"products": [], "sales": [], "expenses": [], "customers": []}


def save_data(data):
    tmp = DATA_FILE + ".tmp"
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    os.replace(tmp, DATA_FILE)


def money(value):
    return "Rp " + f"{value:,.0f}".replace(",", ".")


def number(prompt, default=None):
    while True:
        raw = input(prompt).strip()
        if raw == "" and default is not None:
            return default
        try:
            return float(raw.replace(",", ".").replace(".", "", raw.count(".") > 1))
        except ValueError:
            print("Masukkan angka yang valid.")


def add_product(data):
    name = input("Nama produk/jasa: ").strip()
    if not name:
        return
    price = number("Harga jual (Rp): ")
    cost = number("Modal per unit (Rp): ")
    stock = int(number("Stok awal: ", 0))
    data["products"].append({"name": name, "price": price, "cost": cost, "stock": stock})
    save_data(data)
    print("Produk tersimpan.")


def list_products(data):
    if not data["products"]:
        print("Belum ada produk.")
        return
    print("\nPRODUK")
    for i, p in enumerate(data["products"], 1):
        print(f"{i}. {p['name']} | jual {money(p['price'])} | modal {money(p['cost'])} | stok {p['stock']}")


def record_sale(data):
    list_products(data)
    if not data["products"]:
        return
    idx = int(number("Nomor produk: ")) - 1
    if idx < 0 or idx >= len(data["products"]):
        print("Produk tidak ditemukan.")
        return
    p = data["products"][idx]
    qty = int(number("Jumlah terjual: "))
    if qty <= 0:
        return
    if p["stock"] < qty:
        print(f"Stok tidak cukup. Tersedia: {p['stock']}.")
        return
    customer = input("Nama pelanggan (opsional): ").strip()
    total = p["price"] * qty
    cost = p["cost"] * qty
    p["stock"] -= qty
    data["sales"].append({
        "date": datetime.now().isoformat(timespec="seconds"),
        "product": p["name"], "qty": qty, "revenue": total, "cost": cost,
        "profit": total - cost, "customer": customer
    })
    save_data(data)
    print(f"Penjualan dicatat: {money(total)} | laba kotor: {money(total-cost)}")


def add_expense(data):
    name = input("Nama pengeluaran: ").strip()
    if not name:
        return
    amount = number("Jumlah (Rp): ")
    category = input("Kategori (stok/transport/promosi/lainnya): ").strip() or "lainnya"
    data["expenses"].append({
        "date": datetime.now().isoformat(timespec="seconds"),
        "name": name, "amount": amount, "category": category
    })
    save_data(data)
    print("Pengeluaran dicatat.")


def add_customer(data):
    name = input("Nama pelanggan: ").strip()
    if not name:
        return
    contact = input("Kontak (opsional): ").strip()
    note = input("Catatan: ").strip()
    data["customers"].append({"name": name, "contact": contact, "note": note})
    save_data(data)
    print("Pelanggan tersimpan.")


def summary(data):
    now = datetime.now()
    month = now.strftime("%Y-%m")
    sales = [s for s in data["sales"] if s["date"].startswith(month)]
    expenses = [e for e in data["expenses"] if e["date"].startswith(month)]
    revenue = sum(s["revenue"] for s in sales)
    cogs = sum(s["cost"] for s in sales)
    extra = sum(e["amount"] for e in expenses)
    profit = revenue - cogs - extra
    units = sum(s["qty"] for s in sales)
    print(f"\nRINGKASAN {month}")
    print(f"Omzet       : {money(revenue)}")
    print(f"Modal barang: {money(cogs)}")
    print(f"Pengeluaran : {money(extra)}")
    print(f"Laba bersih*: {money(profit)}")
    print(f"Unit terjual: {units}")
    print("*Perhitungan sederhana berdasarkan data yang dimasukkan.")


def export_csv(data):
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"business_sales_{stamp}.csv"
    with open(filename, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.DictWriter(f, fieldnames=["date", "product", "qty", "revenue", "cost", "profit", "customer"])
        writer.writeheader()
        writer.writerows(data["sales"])
    print(f"Export selesai: {filename}")


def main():
    data = load_data()
    while True:
        print("\n=== BUSINESS MANAGER ===")
        print("1. Tambah produk/jasa")
        print("2. Lihat produk & stok")
        print("3. Catat penjualan")
        print("4. Catat pengeluaran")
        print("5. Tambah pelanggan")
        print("6. Ringkasan bulan ini")
        print("7. Export penjualan CSV")
        print("0. Keluar")
        choice = input("Pilih: ").strip()
        try:
            if choice == "1": add_product(data)
            elif choice == "2": list_products(data)
            elif choice == "3": record_sale(data)
            elif choice == "4": add_expense(data)
            elif choice == "5": add_customer(data)
            elif choice == "6": summary(data)
            elif choice == "7": export_csv(data)
            elif choice == "0": break
            else: print("Pilihan tidak tersedia.")
        except (ValueError, EOFError, KeyboardInterrupt):
            print("Input dibatalkan atau tidak valid.")


if __name__ == "__main__":
    main()
