# Native Voxel Android

Native Android OpenGL ES 2.0 voxel sandbox.

- First launch downloads CC0 textures, UI sprites, and interface sounds from OpenGameArt/Kenney over HTTPS.
- No generated texture artwork is included.
- Main menu is a Minecraft-inspired voxel scene with centered play/settings/quit controls.
- Play mode has touch movement, camera look, jump, and a small procedural voxel world.
- Build artifact: app/build/outputs/apk/debug/app-debug.apk

Asset sources:
- CC0 Minecraft-style textures: https://opengameart.org/content/assorted-minecraft-style-textures
- CC0 Kenney interface sounds: https://opengameart.org/content/interface-sounds
- CC0 Kenney mobile controls: https://opengameart.org/content/mobile-controls

The source uses the assets by downloading the published packs at first launch, then extracting and caching them locally.
