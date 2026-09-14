#!/usr/bin/env python3
"""Read-only battery monitor for Android/Termux and laptops when psutil is available."""
import os, platform, time

def read(path):
    try:
        with open(path, encoding="utf-8", errors="ignore") as f: return f.read().strip()
    except OSError: return None

print("=" * 58)
print("BATTERY MONITOR - READ ONLY")
print("=" * 58)

battery = "/sys/class/power_supply/battery"
if os.path.isdir(battery):
    for key in ("status", "capacity", "health", "voltage_now", "current_now", "temp", "charge_full", "charge_full_design", "cycle_count"):
        v = read(os.path.join(battery, key))
        if v is not None:
            if key == "voltage_now":
                try: v = f"{int(v)/1_000_000:.3f} V"
                except: pass
            elif key == "current_now":
                try: v = f"{int(v)/1_000_000:.3f} A"
                except: pass
            elif key == "temp":
                try: v = f"{int(v)/10:.1f} °C"
                except: pass
            elif key.startswith("charge_"):
                try: v = f"{int(v)/1_000_000:.0f} mAh"
                except: pass
            print(f"{key:20}: {v}")

    try:
        voltage = int(read(os.path.join(battery,"voltage_now"))) / 1_000_000
        current = abs(int(read(os.path.join(battery,"current_now")))) / 1_000_000
        print(f"Estimated instantaneous power: {voltage*current:.2f} W")
        print("Note: this is battery electrical power, not guaranteed charger wattage.")
    except Exception:
        print("Estimated instantaneous power: unavailable")
else:
    print("Android battery sysfs not detected.")
    try:
        import psutil
        b = psutil.sensors_battery()
        if b:
            print("Percent:", b.percent, "%")
            print("Plugged:", b.power_plugged)
            print("Seconds left:", b.secsleft)
        else: print("Laptop battery: unavailable")
    except ImportError:
        print("For PC battery data, install psutil: python -m pip install psutil")

print("\nNo charging or power settings were changed.")
