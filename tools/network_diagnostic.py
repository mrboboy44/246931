#!/usr/bin/env python3
"""Simple read-only network diagnostic for Android/Termux and PC."""
import platform, socket, subprocess, urllib.request, time

def run(args):
    try:
        return subprocess.check_output(args, text=True, stderr=subprocess.STDOUT, timeout=5).strip()
    except Exception as e:
        return f"Unavailable: {e}"

print("=" * 58)
print("NETWORK DIAGNOSTIC - READ ONLY")
print("=" * 58)
print("Device:", platform.system(), platform.release())
print("Hostname:", socket.gethostname())
try:
    print("Local IP:", socket.gethostbyname(socket.gethostname()))
except Exception:
    print("Local IP: Unknown")

for host in ("1.1.1.1", "8.8.8.8"):
    start = time.perf_counter()
    try:
        socket.create_connection((host, 53), timeout=3).close()
        print(f"Reach {host}: YES ({(time.perf_counter()-start)*1000:.1f} ms)")
    except OSError:
        print(f"Reach {host}: NO")

start = time.perf_counter()
try:
    with urllib.request.urlopen("https://www.gstatic.com/generate_204", timeout=5) as r:
        print(f"Internet HTTP: YES (HTTP {r.status}, {(time.perf_counter()-start)*1000:.1f} ms)")
except Exception as e:
    print("Internet HTTP: NO -", e)

if platform.system() == "Windows":
    print("\nIP configuration:\n", run(["ipconfig"]))
elif platform.system() in ("Linux", "Darwin"):
    print("\nInterfaces:\n", run(["ip", "addr"]) if platform.system() == "Linux" else run(["ifconfig"]))

print("\nNo network settings were changed.")
