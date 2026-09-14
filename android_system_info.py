#!/usr/bin/env python3
"""Android System Inspector

Read-only diagnostic tool for your own Android phone.
Works best in Termux. It uses Android shell commands when available
and falls back to Python/platform information where possible.

No root required. It does not modify system settings or collect/send data.
"""

import os
import platform
import re
import shutil
import subprocess
from datetime import datetime


def run(cmd):
    try:
        p = subprocess.run(cmd, capture_output=True, text=True, timeout=5)
        return (p.stdout or p.stderr).strip()
    except Exception:
        return ""


def prop(name):
    out = run(["getprop", name])
    return out or "Tidak tersedia"


def first_number(text):
    m = re.search(r"[-+]?\d+(?:\.\d+)?", text or "")
    return m.group(0) if m else None


def read_file(path):
    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            return f.read().strip()
    except Exception:
        return ""


def battery_info():
    raw = run(["dumpsys", "battery"])
    info = {}
    for line in raw.splitlines():
        if ":" in line:
            k, v = line.split(":", 1)
            info[k.strip()] = v.strip()

    level = info.get("level", "?")
    voltage = info.get("voltage", "?")
    temp = info.get("temperature", "?")
    status = info.get("status", "?")
    health = info.get("health", "?")
    current = ""
    power = ""

    # Android exposes battery current through BatteryManager on many devices.
    current_raw = run(["dumpsys", "battery"])
    m = re.search(r"(?im)^\s*(?:current now|current_now|charge counter):\s*(-?\d+)", current_raw)
    if m:
        current = m.group(1) + " (unit depends on Android build)"

    # sysfs fallback: values are commonly microamps and microvolts.
    current_paths = [
        "/sys/class/power_supply/battery/current_now",
        "/sys/class/power_supply/bms/current_now",
    ]
    voltage_paths = [
        "/sys/class/power_supply/battery/voltage_now",
        "/sys/class/power_supply/bms/voltage_now",
    ]
    current_sys = next((read_file(p) for p in current_paths if read_file(p)), "")
    voltage_sys = next((read_file(p) for p in voltage_paths if read_file(p)), "")

    if current_sys and voltage_sys:
        try:
            i = abs(float(current_sys)) / 1_000_000
            v = float(voltage_sys) / 1_000_000
            power = f"{i * v:.2f} W (estimasi saat ini)"
        except ValueError:
            pass

    print("[BATTERY]")
    print(f"  Level       : {level}%")
    print(f"  Voltage     : {voltage} mV")
    print(f"  Temperature : {float(temp)/10:.1f} °C" if temp.isdigit() else f"  Temperature : {temp}")
    print(f"  Status      : {status}")
    print(f"  Health      : {health}")
    print(f"  Current     : {current or 'Tidak tersedia'}")
    print(f"  Power       : {power or 'Tidak tersedia'}")
    print("  Catatan     : daya charger (watt) ≠ kapasitas baterai. Nilai watt hanya bisa dihitung jika arus + tegangan tersedia.")


def storage_info():
    print("[STORAGE]")
    for path in ["/", "/data", "/sdcard"]:
        if os.path.exists(path):
            try:
                total, used, free = shutil.disk_usage(path)
                print(f"  {path:8} : total {total/2**30:.2f} GB | used {used/2**30:.2f} GB | free {free/2**30:.2f} GB")
            except OSError:
                pass


def memory_info():
    print("[RAM]")
    mem = {}
    for line in read_file("/proc/meminfo").splitlines():
        parts = line.split()
        if len(parts) >= 2:
            try:
                mem[parts[0].rstrip(":")] = int(parts[1])
            except ValueError:
                pass
    if "MemTotal" in mem:
        total = mem["MemTotal"] / 1024 / 1024
        avail = mem.get("MemAvailable", 0) / 1024 / 1024
        print(f"  Total     : {total:.2f} GB")
        print(f"  Available : {avail:.2f} GB")
        print(f"  Used      : {max(total-avail, 0):.2f} GB")
    else:
        print("  Tidak tersedia")


def cpu_info():
    print("[CPU / SOC]")
    print(f"  ABI       : {prop('ro.product.cpu.abi')}")
    print(f"  ABI list  : {prop('ro.product.cpu.abilist')}")
    print(f"  Hardware  : {prop('ro.hardware')}")
    print(f"  Chipset   : {prop('ro.soc.manufacturer')} {prop('ro.soc.model')}")
    print(f"  Cores     : {os.cpu_count() or 'Tidak tersedia'}")
    freq = run(["sh", "-c", "cat /sys/devices/system/cpu/cpu*/cpufreq/scaling_cur_freq 2>/dev/null"])
    if freq:
        vals = []
        for x in freq.splitlines():
            try:
                vals.append(float(x)/1_000_000)
            except ValueError:
                pass
        if vals:
            print(f"  Current MHz: min {min(vals)*1000:.0f} | max {max(vals)*1000:.0f}")


def software_info():
    print("[SOFTWARE]")
    print(f"  Manufacturer : {prop('ro.product.manufacturer')}")
    print(f"  Model        : {prop('ro.product.model')}")
    print(f"  Device       : {prop('ro.product.device')}")
    print(f"  Android      : {prop('ro.build.version.release')}")
    print(f"  SDK          : {prop('ro.build.version.sdk')}")
    print(f"  Build        : {prop('ro.build.display.id')}")
    print(f"  Kernel       : {platform.release()}")
    print(f"  Python       : {platform.python_version()}")


def display_info():
    print("[DISPLAY]")
    print(f"  Resolution   : {prop('vendor.display-size')} / {prop('ro.sf.lcd_density')} dpi density")
    wm = run(["wm", "size"])
    density = run(["wm", "density"])
    print(f"  WM size      : {wm or 'Tidak tersedia'}")
    print(f"  WM density   : {density or 'Tidak tersedia'}")
    refresh = run(["dumpsys", "display"])
    rates = sorted(set(re.findall(r"(?:refreshRate|fps)[^0-9]*(\d+(?:\.\d+)?)", refresh, re.I)))
    if rates:
        print(f"  Refresh rates : {', '.join(rates)} Hz")


def network_info():
    print("[NETWORK]")
    print(f"  Hostname : {run(['getprop', 'net.hostname']) or platform.node() or 'Tidak tersedia'}")
    print(f"  Wi-Fi IP : {run(['sh', '-c', "ip -4 addr show wlan0 2>/dev/null | awk '/inet /{print $2}'"] ) or 'Tidak tersedia'}")
    print(f"  Gateway  : {run(['sh', '-c', "ip route 2>/dev/null | awk '/default/{print $3; exit}'"] ) or 'Tidak tersedia'}")


def sensors_info():
    print("[SENSORS]")
    out = run(["pm", "list", "features"])
    sensors = [x.strip() for x in out.splitlines() if "sensor" in x.lower()]
    print(f"  Sensor features: {len(sensors)}")
    for x in sensors:
        print(f"    {x}")


def main():
    print("=" * 64)
    print("ANDROID SYSTEM INSPECTOR — READ ONLY")
    print(datetime.now().strftime("Waktu cek: %Y-%m-%d %H:%M:%S"))
    print("=" * 64)
    software_info()
    cpu_info()
    memory_info()
    storage_info()
    battery_info()
    display_info()
    network_info()
    sensors_info()
    print("\nSelesai. Tidak ada pengaturan sistem yang diubah.")


if __name__ == "__main__":
    main()
