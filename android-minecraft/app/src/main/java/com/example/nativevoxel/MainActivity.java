package com.example.nativevoxel;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private GameView gameView;
    private LinearLayout menu;
    private LinearLayout hud;
    private TextView status;
    private Button play;
    private Button settings;
    private Button quit;
    private Button jump;
    private Button back;

    private SoundPool soundPool;
    private int clickSound = 0;
    private int confirmSound = 0;
    private boolean assetsReady = false;
    private int sensitivityLabel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemBars();

        AudioAttributes audio = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audio)
                .setMaxStreams(4)
                .build();

        FrameLayout root = new FrameLayout(this);
        gameView = new GameView(this);
        root.addView(gameView, new FrameLayout.LayoutParams(-1, -1));

        menu = buildMenu();
        root.addView(menu, new FrameLayout.LayoutParams(-1, -1));

        hud = buildHud();
        hud.setVisibility(View.GONE);
        root.addView(hud, new FrameLayout.LayoutParams(-1, -1));

        setContentView(root);

        status.setText("DOWNLOADING CC0 ASSETS…");
        setButtonsEnabled(false);

        AssetLoader.prepare(this, () -> runOnUiThread(this::onAssetsReady),
                error -> runOnUiThread(() -> {
                    status.setText("ASSET DOWNLOAD FAILED");
                    status.setTextColor(Color.rgb(255, 180, 180));
                }));
    }

    private void onAssetsReady() {
        assetsReady = true;
        gameView.refreshAssets();

        if (AssetLoader.click != null && AssetLoader.click.isFile()) {
            clickSound = soundPool.load(AssetLoader.click.getAbsolutePath(), 1);
        }
        if (AssetLoader.confirm != null && AssetLoader.confirm.isFile()) {
            confirmSound = soundPool.load(AssetLoader.confirm.getAbsolutePath(), 1);
        }

        status.setText("CC0 ASSETS READY");
        status.setTextColor(Color.WHITE);
        styleButton(play);
        styleButton(settings);
        styleButton(quit);
        styleButton(jump);
        styleButton(back);
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        if (play != null) play.setEnabled(enabled);
        if (settings != null) settings.setEnabled(enabled);
        if (quit != null) quit.setEnabled(enabled);
        if (jump != null) jump.setEnabled(enabled);
        if (back != null) back.setEnabled(enabled);
    }

    private void playClick(boolean confirm) {
        int id = confirm && confirmSound != 0 ? confirmSound : clickSound;
        if (id != 0 && soundPool != null) {
            soundPool.play(id, 0.75f, 0.75f, 1, 0, 1f);
        }
    }

    private LinearLayout buildMenu() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(36), dp(18), dp(36), dp(18));

        TextView title = new TextView(this);
        title.setText("BLOCKWORLD");
        title.setTextColor(Color.WHITE);
        title.setTextSize(42);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        title.setShadowLayer(8f, 4f, 4f, Color.BLACK);
        box.addView(title, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView sub = new TextView(this);
        sub.setText("NATIVE VOXEL SANDBOX");
        sub.setTextColor(Color.WHITE);
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setShadowLayer(5f, 2f, 2f, Color.BLACK);
        box.addView(sub, new LinearLayout.LayoutParams(-1, dp(34)));

        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(11);
        status.setGravity(Gravity.CENTER);
        status.setShadowLayer(4f, 2f, 2f, Color.BLACK);
        box.addView(status, new LinearLayout.LayoutParams(-1, dp(30)));

        play = button("PLAY");
        settings = button("SETTINGS");
        quit = button("QUIT");
        box.addView(play, buttonParams());
        box.addView(settings, buttonParams());
        box.addView(quit, buttonParams());

        play.setOnClickListener(v -> {
            if (!assetsReady) return;
            playClick(true);
            menu.setVisibility(View.GONE);
            hud.setVisibility(View.VISIBLE);
            gameView.getWorldRenderer().setPlaying(true);
        });

        settings.setOnClickListener(v -> {
            playClick(false);
            gameView.getWorldRenderer().toggleSensitivity();
            sensitivityLabel++;
            if (sensitivityLabel > 3) sensitivityLabel = 1;
            status.setText("CAMERA SENSITIVITY " + sensitivityLabel + "/3");
        });

        quit.setOnClickListener(v -> {
            playClick(false);
            finish();
        });

        return box;
    }

    private LinearLayout buildHud() {
        LinearLayout overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.BOTTOM);
        overlay.setPadding(dp(24), dp(24), dp(24), dp(24));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.END | Gravity.BOTTOM);

        jump = button("JUMP");
        back = button("MENU");
        bottom.addView(back, smallButtonParams());
        bottom.addView(jump, smallButtonParams());
        overlay.addView(bottom, new LinearLayout.LayoutParams(-1, dp(84)));

        back.setOnClickListener(v -> {
            playClick(false);
            gameView.getWorldRenderer().setPlaying(false);
            hud.setVisibility(View.GONE);
            menu.setVisibility(View.VISIBLE);
        });

        jump.setOnClickListener(v -> {
            playClick(false);
            gameView.getWorldRenderer().jumpPressed();
        });

        TextView hint = new TextView(this);
        hint.setText("LEFT SIDE: MOVE    RIGHT SIDE: LOOK");
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(11);
        hint.setShadowLayer(4f, 2f, 2f, Color.BLACK);
        hint.setGravity(Gravity.START | Gravity.BOTTOM);
        overlay.addView(hint, new LinearLayout.LayoutParams(-1, dp(26)));

        return overlay;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12), dp(4), dp(12), dp(4));
        b.setBackgroundColor(Color.rgb(62, 62, 62));
        return b;
    }

    private void styleButton(Button b) {
        if (AssetLoader.button != null && AssetLoader.button.isFile()) {
            android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeFile(AssetLoader.button.getAbsolutePath());
            if (bmp != null) {
                b.setBackground(new BitmapDrawable(getResources(), bmp));
                b.setTextColor(Color.WHITE);
            }
        }
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(340), dp(60));
        p.gravity = Gravity.CENTER;
        p.setMargins(0, dp(6), 0, dp(6));
        return p;
    }

    private LinearLayout.LayoutParams smallButtonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(150), dp(58));
        p.setMargins(dp(6), 0, dp(6), 0);
        return p;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    protected void onDestroy() {
        if (soundPool != null) soundPool.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (hud.getVisibility() == View.VISIBLE) {
            back.performClick();
        } else {
            super.onBackPressed();
        }
    }
}
