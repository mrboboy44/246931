package com.example.nativevoxel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Locale;

public class GameView extends GLSurfaceView {
    private final WorldRenderer renderer;

    public GameView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new WorldRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public WorldRenderer getWorldRenderer() {
        return renderer;
    }

    public void refreshAssets() {
        queueEvent(renderer::reloadTextures);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                renderer.touchDown(x, y, getWidth(), getHeight());
                return true;
            case MotionEvent.ACTION_MOVE:
                renderer.touchMove(x, y);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                renderer.touchUp();
                return true;
        }
        return true;
    }

    public static class WorldRenderer implements GLSurfaceView.Renderer {
        private static final int W = 24;
        private static final int H = 10;
        private static final int D = 24;

        private static final int AIR = 0;
        private static final int GRASS = 1;
        private static final int DIRT = 2;
        private static final int STONE = 3;
        private static final int WOOD = 4;
        private static final int SAND = 5;

        private final int[][][] world = new int[W][H][D];
        private final ArrayList<Float>[] vertices = new ArrayList[6];
        private final FloatBuffer[] buffers = new FloatBuffer[6];
        private final int[] vertexCounts = new int[6];
        private final int[] textures = new int[6];

        private int program;
        private int aPosition;
        private int aTexCoord;
        private int aShade;
        private int uMvp;
        private int uTexture;

        private final float[] projection = new float[16];
        private final float[] view = new float[16];
        private final float[] mvp = new float[16];

        private boolean assetsReady;
        private boolean playing;
        private boolean touchingMove;
        private boolean touchingLook;
        private float touchStartX;
        private float touchStartY;
        private float lastLookX;
        private float lastLookY;
        private float moveX;
        private float moveZ;

        private float playerX = 12.5f;
        private float playerZ = 16.5f;
        private float playerY = 5.0f;
        private float velocityY = 0f;
        private boolean grounded = true;
        private float yaw = 225f;
        private float pitch = -10f;
        private float sensitivity = 0.22f;
        private long lastTimeNs;

        private float menuAngle;

        public WorldRenderer() {
            for (int i = 0; i < vertices.length; i++) vertices[i] = new ArrayList<>();
            buildWorld();
        }

        private void buildWorld() {
            for (int x = 0; x < W; x++) {
                for (int z = 0; z < D; z++) {
                    float wave = (float)(Math.sin(x * 0.55) * 0.9 + Math.cos(z * 0.42) * 0.9
                            + Math.sin((x + z) * 0.22) * 0.8);
                    int top = Math.max(1, Math.min(H - 2, 3 + Math.round(wave)));
                    for (int y = 0; y <= top; y++) {
                        if (y == top) world[x][y][z] = GRASS;
                        else if (y >= top - 2) world[x][y][z] = DIRT;
                        else world[x][y][z] = STONE;
                    }
                    if (top >= 3 && x > 2 && z > 2 && x < W - 3 && z < D - 3
                            && ((x * 31 + z * 17) % 37 == 4)) {
                        addTree(x, top + 1, z);
                    }
                }
            }
            rebuildMesh();
            playerY = surfaceY((int) playerX, (int) playerZ) + 1.65f;
        }

        private void addTree(int x, int y, int z) {
            if (y + 4 >= H) return;
            for (int i = 0; i < 3; i++) world[x][y + i][z] = WOOD;
            int leafBase = y + 2;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= 3 && inWorld(x + dx, leafBase, z + dz)) {
                        world[x + dx][leafBase][z + dz] = GRASS;
                    }
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (inWorld(x + dx, leafBase + 1, z + dz)) {
                        world[x + dx][leafBase + 1][z + dz] = GRASS;
                    }
                }
            }
        }

        private boolean inWorld(int x, int y, int z) {
            return x >= 0 && x < W && y >= 0 && y < H && z >= 0 && z < D;
        }

        private int surfaceY(int x, int z) {
            x = Math.max(0, Math.min(W - 1, x));
            z = Math.max(0, Math.min(D - 1, z));
            for (int y = H - 1; y >= 0; y--) {
                if (world[x][y][z] != AIR) return y;
            }
            return 0;
        }

        private void rebuildMesh() {
            for (ArrayList<Float> list : vertices) list.clear();
            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {
                    for (int z = 0; z < D; z++) {
                        int type = world[x][y][z];
                        if (type == AIR) continue;
                        if (!inWorld(x, y + 1, z) || world[x][y + 1][z] == AIR)
                            addFace(type, x, y, z, 0);
                        if (!inWorld(x, y - 1, z) || world[x][y - 1][z] == AIR)
                            addFace(type, x, y, z, 1);
                        if (!inWorld(x - 1, y, z) || world[x - 1][y][z] == AIR)
                            addFace(type, x, y, z, 2);
                        if (!inWorld(x + 1, y, z) || world[x + 1][y][z] == AIR)
                            addFace(type, x, y, z, 3);
                        if (!inWorld(x, y, z - 1) || world[x][y][z - 1] == AIR)
                            addFace(type, x, y, z, 4);
                        if (!inWorld(x, y, z + 1) || world[x][y][z + 1] == AIR)
                            addFace(type, x, y, z, 5);
                    }
                }
            }
            for (int i = 1; i <= 5; i++) {
                float[] data = new float[vertices[i].size()];
                for (int j = 0; j < data.length; j++) data[j] = vertices[i].get(j);
                ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4).order(ByteOrder.nativeOrder());
                FloatBuffer fb = bb.asFloatBuffer();
                fb.put(data).position(0);
                buffers[i] = fb;
                vertexCounts[i] = data.length / 6;
            }
        }

        private void addFace(int type, int x, int y, int z, int face) {
            float shade = face == 0 ? 1.00f : (face == 1 ? 0.55f : 0.78f);
            float x0 = x, x1 = x + 1f;
            float y0 = y, y1 = y + 1f;
            float z0 = z, z1 = z + 1f;
            switch (face) {
                case 0: addQuad(type, shade, x0,y1,z0, x1,y1,z0, x1,y1,z1, x0,y1,z1); break;
                case 1: addQuad(type, shade, x0,y0,z1, x1,y0,z1, x1,y0,z0, x0,y0,z0); break;
                case 2: addQuad(type, shade, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0); break;
                case 3: addQuad(type, shade, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1); break;
                case 4: addQuad(type, shade, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0); break;
                case 5: addQuad(type, shade, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1); break;
            }
        }

        private void addQuad(int type, float shade,
                             float x0,float y0,float z0,
                             float x1,float y1,float z1,
                             float x2,float y2,float z2,
                             float x3,float y3,float z3) {
            addVertex(vertices[type], x0,y0,z0, 0,0,shade);
            addVertex(vertices[type], x1,y1,z1, 1,0,shade);
            addVertex(vertices[type], x2,y2,z2, 1,1,shade);
            addVertex(vertices[type], x0,y0,z0, 0,0,shade);
            addVertex(vertices[type], x2,y2,z2, 1,1,shade);
            addVertex(vertices[type], x3,y3,z3, 0,1,shade);
        }

        private void addVertex(ArrayList<Float> list, float x,float y,float z,float u,float v,float shade) {
            list.add(x); list.add(y); list.add(z); list.add(u); list.add(v); list.add(shade);
        }

        @Override
        public void onSurfaceCreated(javax.microedition.khronos.egl.EGLConfig config) {
            GLES20.glClearColor(0.47f, 0.73f, 0.94f, 1f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            program = buildProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            aPosition = GLES20.glGetAttribLocation(program, "aPosition");
            aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
            aShade = GLES20.glGetAttribLocation(program, "aShade");
            uMvp = GLES20.glGetUniformLocation(program, "uMvp");
            uTexture = GLES20.glGetUniformLocation(program, "uTexture");
            reloadTextures();
            lastTimeNs = System.nanoTime();
        }

        @Override
        public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            float ratio = Math.max(0.1f, width / (float) Math.max(1, height));
            Matrix.perspectiveM(projection, 0, 65f, ratio, 0.05f, 250f);
        }

        @Override
        public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
            long now = System.nanoTime();
            float dt = Math.min(0.05f, (now - lastTimeNs) / 1_000_000_000f);
            lastTimeNs = now;

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            update(dt);

            float cx, cy, cz;
            float lx, ly, lz;
            if (!playing) {
                menuAngle += dt * 4f;
                double rad = Math.toRadians(menuAngle);
                cx = 12f + (float)Math.cos(rad) * 19f;
                cz = 12f + (float)Math.sin(rad) * 19f;
                cy = 11.5f;
                lx = 12f; ly = 3.5f; lz = 12f;
            } else {
                float yawRad = (float)Math.toRadians(yaw);
                float pitchRad = (float)Math.toRadians(pitch);
                cx = playerX;
                cy = playerY;
                cz = playerZ;
                lx = cx + (float)Math.cos(pitchRad) * (float)Math.cos(yawRad);
                ly = cy + (float)Math.sin(pitchRad);
                lz = cz + (float)Math.cos(pitchRad) * (float)Math.sin(yawRad);
            }

            Matrix.setLookAtM(view, 0, cx,cy,cz, lx,ly,lz, 0f,1f,0f);
            Matrix.multiplyMM(mvp, 0, projection, 0, view, 0);

            GLES20.glUseProgram(program);
            GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glUniform1i(uTexture, 0);

            for (int type = 1; type <= 5; type++) {
                if (buffers[type] == null || vertexCounts[type] == 0) continue;
                if (textures[type] != 0 && assetsReady) {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[type]);
                } else {
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
                }
                FloatBuffer fb = buffers[type];
                fb.position(0);
                GLES20.glEnableVertexAttribArray(aPosition);
                GLES20.glEnableVertexAttribArray(aTexCoord);
                GLES20.glEnableVertexAttribArray(aShade);
                fb.position(0);
                GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 24, fb);
                fb.position(3);
                GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 24, fb);
                fb.position(5);
                GLES20.glVertexAttribPointer(aShade, 1, GLES20.GL_FLOAT, false, 24, fb);
                if (assetsReady) {
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCounts[type]);
                }
                GLES20.glDisableVertexAttribArray(aPosition);
                GLES20.glDisableVertexAttribArray(aTexCoord);
                GLES20.glDisableVertexAttribArray(aShade);
            }
        }

        private void update(float dt) {
            if (!playing) return;
            float speed = 4.4f;
            float yawRad = (float)Math.toRadians(yaw);
            float forwardX = (float)Math.cos(yawRad);
            float forwardZ = (float)Math.sin(yawRad);
            float rightX = -forwardZ;
            float rightZ = forwardX;

            playerX += (forwardX * moveZ + rightX * moveX) * speed * dt;
            playerZ += (forwardZ * moveZ + rightZ * moveX) * speed * dt;

            playerX = Math.max(1.2f, Math.min(W - 1.2f, playerX));
            playerZ = Math.max(1.2f, Math.min(D - 1.2f, playerZ));

            float ground = surfaceY((int)playerX, (int)playerZ) + 1.65f;
            if (!grounded || playerY > ground + 0.02f) {
                velocityY -= 16f * dt;
                playerY += velocityY * dt;
                if (playerY <= ground) {
                    playerY = ground;
                    velocityY = 0f;
                    grounded = true;
                }
            } else {
                playerY = ground;
                grounded = true;
            }
        }

        public void setPlaying(boolean value) {
            playing = value;
            if (!value) {
                moveX = moveZ = 0f;
            }
        }

        public void toggleSensitivity() {
            sensitivity = sensitivity < 0.3f ? 0.34f : 0.22f;
        }

        public void jumpPressed() {
            if (playing && grounded) {
                velocityY = 7.2f;
                grounded = false;
            }
        }

        public void touchDown(float x, float y, int width, int height) {
            if (!playing) return;
            if (x < width * 0.46f && y > height * 0.35f) {
                touchingMove = true;
                touchStartX = x;
                touchStartY = y;
                updateMove(x,y);
            } else {
                touchingLook = true;
                lastLookX = x;
                lastLookY = y;
            }
        }

        public void touchMove(float x, float y) {
            if (touchingMove) updateMove(x,y);
            if (touchingLook) {
                float dx = x - lastLookX;
                float dy = y - lastLookY;
                yaw += dx * sensitivity;
                pitch -= dy * sensitivity;
                pitch = Math.max(-80f, Math.min(70f, pitch));
                lastLookX = x;
                lastLookY = y;
            }
        }

        public void touchUp() {
            touchingMove = false;
            touchingLook = false;
            moveX = moveZ = 0f;
        }

        private void updateMove(float x, float y) {
            float dx = (x - touchStartX) / 110f;
            float dy = (y - touchStartY) / 110f;
            float len = (float)Math.sqrt(dx*dx + dy*dy);
            if (len > 1f) {
                dx /= len;
                dy /= len;
            }
            moveX = dx;
            moveZ = -dy;
        }

        public void reloadTextures() {
            int[] old = textures.clone();
            if (old[1] != 0) GLES20.glDeleteTextures(5, old, 1);
            for (int i = 0; i < textures.length; i++) textures[i] = 0;

            File[] files = new File[] {
                    null, AssetLoader.grass, AssetLoader.dirt, AssetLoader.stone,
                    AssetLoader.wood, AssetLoader.sand != null ? AssetLoader.sand : AssetLoader.dirt
            };
            for (int i = 1; i <= 5; i++) {
                if (files[i] != null && files[i].isFile()) textures[i] = uploadTexture(files[i]);
            }
            assetsReady = textures[1] != 0 && textures[2] != 0 && textures[3] != 0;
        }

        private int uploadTexture(File file) {
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bmp == null) return 0;
            int[] id = new int[1];
            GLES20.glGenTextures(1, id, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            bmp.recycle();
            return id[0];
        }

        private int buildProgram(String vertexSource, String fragmentSource) {
            int vs = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            int fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            int p = GLES20.glCreateProgram();
            GLES20.glAttachShader(p, vs);
            GLES20.glAttachShader(p, fs);
            GLES20.glLinkProgram(p);
            int[] ok = new int[1];
            GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, ok, 0);
            if (ok[0] == 0) throw new RuntimeException("OpenGL program link failed.");
            GLES20.glDeleteShader(vs);
            GLES20.glDeleteShader(fs);
            return p;
        }

        private int compileShader(int type, String source) {
            int s = GLES20.glCreateShader(type);
            GLES20.glShaderSource(s, source);
            GLES20.glCompileShader(s);
            int[] ok = new int[1];
            GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0);
            if (ok[0] == 0) throw new RuntimeException("OpenGL shader compile failed: " + GLES20.glGetShaderInfoLog(s));
            return s;
        }

        private static final String VERTEX_SHADER =
                "attribute vec3 aPosition;\n" +
                "attribute vec2 aTexCoord;\n" +
                "attribute float aShade;\n" +
                "uniform mat4 uMvp;\n" +
                "varying vec2 vTexCoord;\n" +
                "varying float vShade;\n" +
                "void main(){ gl_Position=uMvp*vec4(aPosition,1.0); vTexCoord=aTexCoord; vShade=aShade; }";

        private static final String FRAGMENT_SHADER =
                "precision mediump float;\n" +
                "uniform sampler2D uTexture;\n" +
                "varying vec2 vTexCoord;\n" +
                "varying float vShade;\n" +
                "void main(){ vec4 c=texture2D(uTexture,vTexCoord); if(c.a<0.08) discard; gl_FragColor=vec4(c.rgb*vShade,c.a); }";
    }
}
