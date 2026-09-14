#!/usr/bin/env python3
"""Analyze a directory by file type and size. Read-only; never deletes files."""
import argparse, os
from collections import defaultdict

p = argparse.ArgumentParser(description="Analyze storage usage safely")
p.add_argument("path", nargs="?", default=".")
p.add_argument("--top", type=int, default=20)
a = p.parse_args()
root = os.path.abspath(os.path.expanduser(a.path))
if not os.path.isdir(root):
    raise SystemExit(f"Not a directory: {root}")

sizes = defaultdict(int); files = 0; errors = 0
for base, _, names in os.walk(root):
    for name in names:
        path = os.path.join(base, name)
        try:
            size = os.path.getsize(path)
            ext = os.path.splitext(name)[1].lower() or "[no extension]"
            sizes[ext] += size; files += 1
        except OSError:
            errors += 1

def mb(n): return n / (1024**2)
print(f"Storage analysis: {root}")
print(f"Files scanned: {files} | inaccessible: {errors}")
print("\nLargest file types:")
for ext, size in sorted(sizes.items(), key=lambda x: x[1], reverse=True)[:a.top]:
    print(f"{ext:18} {mb(size):10.2f} MB")
print("\nRead-only: nothing was moved, renamed or deleted.")
