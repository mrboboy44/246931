package com.example.nativevoxel;

import android.content.Context;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class AssetLoader {
    private AssetLoader() {}

    public static final String TEXTURE_PACK =
            "https://opengameart.org/sites/default/files/assorted_textures_1.zip";
    public static final String SOUND_PACK =
            "https://opengameart.org/sites/default/files/kenney_interfaceSounds.zip";
    public static final String CONTROL_PACK =
            "https://opengameart.org/sites/default/files/mobile-controls-1.0.zip";

    public static File grass;
    public static File dirt;
    public static File stone;
    public static File wood;
    public static File sand;
    public static File button;
    public static File click;
    public static File confirm;

    public static void prepare(Context context, Runnable ready, java.util.function.Consumer<Exception> failed) {
        new Thread(() -> {
            try {
                File root = new File(context.getFilesDir(), "remote_assets");
                File textures = new File(root, "textures");
                File sounds = new File(root, "sounds");
                File controls = new File(root, "controls");
                ensureDir(textures);
                ensureDir(sounds);
                ensureDir(controls);

                File textureZip = new File(root, "textures.zip");
                File soundZip = new File(root, "sounds.zip");
                File controlZip = new File(root, "controls.zip");

                downloadIfMissing(TEXTURE_PACK, textureZip);
                downloadIfMissing(SOUND_PACK, soundZip);
                downloadIfMissing(CONTROL_PACK, controlZip);

                unzipIfEmpty(textureZip, textures);
                unzipIfEmpty(soundZip, sounds);
                unzipIfEmpty(controlZip, controls);

                grass = findBest(textures, ".png", "grass", "grass_block", "default_grass");
                dirt = findBest(textures, ".png", "dirt", "dirt_block");
                stone = findBest(textures, ".png", "stone", "stone_block");
                wood = findBest(textures, ".png", "wood", "log", "oak");
                sand = findBest(textures, ".png", "sand", "sand_block");

                button = findBest(controls, ".png", "button", "square", "control", "hud");
                click = findBest(sounds, ".ogg", "click_001", "click");
                confirm = findBest(sounds, ".ogg", "confirmation_001", "confirmation", "select");

                if (grass == null || dirt == null || stone == null) {
                    throw new IOException("CC0 texture pack did not contain enough block textures.");
                }
                ready.run();
            } catch (Exception e) {
                failed.accept(e);
            }
        }, "asset-download").start();
    }

    private static void downloadIfMissing(String urlString, File target) throws IOException {
        if (target.isFile() && target.length() > 0) return;
        File tmp = new File(target.getParentFile(), target.getName() + ".part");
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "NativeVoxelAndroid/1.0");
        int code = connection.getResponseCode();
        if (code < 200 || code >= 300) {
            connection.disconnect();
            throw new IOException("Asset download failed: HTTP " + code + " from " + urlString);
        }
        try (InputStream in = connection.getInputStream();
             OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp))) {
            byte[] buffer = new byte[32768];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
        } finally {
            connection.disconnect();
        }
        if (!tmp.renameTo(target)) {
            throw new IOException("Could not finalize asset download: " + target);
        }
    }

    private static void unzipIfEmpty(File zipFile, File outputDir) throws IOException {
        File[] existing = outputDir.listFiles();
        if (existing != null && existing.length > 0) return;

        byte[] buffer = new byte[32768];
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name == null || name.isEmpty()) continue;
                File out = new File(outputDir, name);
                String canonicalRoot = outputDir.getCanonicalPath() + File.separator;
                String canonicalOut = out.getCanonicalPath();
                if (!canonicalOut.startsWith(canonicalRoot)) continue;
                if (entry.isDirectory()) {
                    ensureDir(out);
                    continue;
                }
                File parent = out.getParentFile();
                if (parent != null) ensureDir(parent);
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(out))) {
                    int n;
                    while ((n = zis.read(buffer)) != -1) os.write(buffer, 0, n);
                }
            }
        }
    }

    private static File findBest(File root, String extension, String... hints) {
        List<File> candidates = new ArrayList<>();
        collectFiles(root, candidates);
        File best = null;
        int bestScore = Integer.MIN_VALUE;
        for (File file : candidates) {
            String name = file.getName().toLowerCase(Locale.US);
            if (!name.endsWith(extension)) continue;
            int score = 0;
            for (String hint : hints) {
                if (name.contains(hint.toLowerCase(Locale.US))) score += 10;
            }
            if (score > bestScore) {
                bestScore = score;
                best = file;
            }
        }
        return best;
    }

    private static void collectFiles(File root, List<File> out) {
        File[] files = root.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) collectFiles(f, out);
            else out.add(f);
        }
    }

    private static void ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Could not create " + dir);
    }
}
