#!/usr/bin/env python3
"""Xbox controller tester for a PC. Requires pygame; no game files are modified."""
try:
    import pygame
except ImportError:
    raise SystemExit("Install dependency first: python -m pip install pygame")

pygame.init(); pygame.joystick.init()
count = pygame.joystick.get_count()
print(f"Controllers detected: {count}")
if count == 0:
    print("Connect an Xbox-compatible controller via USB or Bluetooth and restart.")
    raise SystemExit

for i in range(count):
    j = pygame.joystick.Joystick(i); j.init()
    print(f"[{i}] {j.get_name()} | axes={j.get_numaxes()} buttons={j.get_numbuttons()} hats={j.get_numhats()}")

print("\nLive test window. Press controller buttons/sticks. Close window to exit.")
clock = pygame.time.Clock(); running = True
while running:
    for e in pygame.event.get():
        if e.type == pygame.QUIT: running = False
        elif e.type == pygame.JOYBUTTONDOWN: print(f"Button {e.button}: DOWN")
        elif e.type == pygame.JOYBUTTONUP: print(f"Button {e.button}: UP")
        elif e.type == pygame.JOYHATMOTION: print(f"D-pad: {e.value}")
        elif e.type == pygame.JOYAXISMOTION and abs(e.value) > 0.15: print(f"Axis {e.axis}: {e.value:+.2f}")
    clock.tick(120)
pygame.quit()
