#!/usr/bin/env python3
"""Cross-platform read-only system report for Android (Termux), Windows, Linux and macOS."""
import os, platform, shutil, subprocess, sys
from datetime import datetime

def cmd(args):
    try:
        return subprocess.check_output(args, text=True, stderr=subprocess.DEVNULL, timeout=3).strip()
    except Exception:
        return ""

def bytes_gb(n):
    return f"{n / (1024**3):.2f} GB"

print("=" * 58)
print("SYSTEM REPORT - READ ONLY")
print("=" * 58)
print("Time:", datetime.now().astimezone().isoformat(timespec="seconds"))
print("OS:", platform.system(), platform.release())
print("Machine:", platform.machine())
print("Python:", platform.python_version())
print("Hostname:", platform.node())
print("CPU:", platform.processor() or "Unknown")
print("CPU cores:", os.cpu_count() or "Unknown")

try:
    load = os.getloadavg()
    print("Load average:", ", ".join(f"{x:.2f}" for x in load))
except (AttributeError, OSError):
    pass

try:
    total, used, free = shutil.disk_usage(os.path.expanduser("~"))
    print("Home storage:", bytes_gb(used), "used /", bytes_gb(total), "total /", bytes_gb(free), "free")
except OSError:
    pass

meminfo = "/proc/meminfo"
if os.path.exists(meminfo):
    data = {}
    with open(meminfo, encoding="utf-8", errors="ignore") as f:
        for line in f:
            p = line.split()
            if len(p) >= 2 and p[0] in ("MemTotal:", "MemAvailable:"):
                data[p[0]] = int(p[1]) * 1024
    if data:
        total = data.get("MemTotal:", 0)
        avail = data.get("MemAvailable:", 0)
        print("RAM:", bytes_gb(total), "total /", bytes_gb(max(0, total-avail)), "used")

if platform.system() == "Windows":
    print("Windows PowerShell:", cmd(["powershell", "-NoProfile", "-Command", "$PSVersionTable.PSVersion.ToString()"]) or "Unknown")
elif platform.system() == "Android":
    print("Android release:", cmd(["getprop", "ro.build.version.release"]) or "Unknown")
    print("Android model:", cmd(["getprop", "ro.product.model"]) or "Unknown")

print("\nNo files were changed and no data was uploaded.")
