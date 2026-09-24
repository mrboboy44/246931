package com.ardangame.pixel;

import android.content.Context;
import android.graphics.*;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class GameView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int W,H;
    private float scale=1f, camX=0;
    private long lastNs=System.nanoTime();
    private int state=0; // menu, play, clear, game over
    private int level=1, score=0, coins=0, lives=3;
    private final Player player = new Player();
    private final Enemy[] enemies = new Enemy[]{new Enemy(720),new Enemy(1220),new Enemy(1730),new Enemy(2400),new Enemy(3220),new Enemy(3980)};
    private final Map<Integer,Integer> pointers=new HashMap<>();
    private boolean left,right,jump;
    private static final float VW=960f,VH=540f;
    private static final float GRAV=1700f,SPEED=260f,JUMP=-630f;

    public GameView(Context c){super(c);setFocusable(true);}

    @Override protected void onDraw(Canvas c){
        W=getWidth(); H=getHeight(); scale=Math.min(W/VW,H/VH); float ox=(W-VW*scale)/2f, oy=(H-VH*scale)/2f;
        c.drawColor(Color.rgb(112,198,255)); c.save(); c.translate(ox,oy); c.scale(scale,scale);
        long now=System.nanoTime(); float dt=Math.min(0.033f,(now-lastNs)/1_000_000_000f); lastNs=now;
        if(state==0) drawMenu(c); else if(state==1){update(dt);drawWorld(c);drawHud(c);} else if(state==2){drawWorld(c);drawOverlay(c,"LEVEL CLEAR","Tap to continue");} else {drawWorld(c);drawOverlay(c,"GAME OVER","Tap to restart");}
        c.restore();postInvalidateOnAnimation();
    }

    private void drawMenu(Canvas c){
        sky(c); p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(58);p.setColor(Color.WHITE);c.drawText("SUPER PIXEL",480,130,p);p.setTextSize(68);p.setColor(0xFFFFE04A);c.drawText("ADVENTURE",480,200,p);
        drawPlayerSprite(c,464,248,0,1.0f);
        p.setColor(0xEE1E2A3A);c.drawRoundRect(300,355,660,455,22,22,p);p.setTextSize(28);p.setColor(Color.WHITE);c.drawText("PLAY",480,414,p);
        p.setTextSize(16);p.setColor(0xFFC9D6E6);c.drawText("◀ ▶ move   •   ▲ jump",480,478,p);p.setTextSize(12);p.setColor(0xFF203040);c.drawText("original pixel-art fan-style demo",480,520,p);
    }

    private void sky(Canvas c){
        p.setColor(0xFF70C6FF);c.drawRect(0,0,VW,VH,p);
        for(int i=0;i<10;i++){float x=i*150-(camX*0.2f)%150;drawCloud(c,x,70+(i%3)*34);}
        for(int i=0;i<8;i++){float x=i*190-(camX*0.34f)%190;p.setColor(i%2==0?0xFF75C76E:0xFF64B45E);Path h=new Path();h.moveTo(x-95,468);h.lineTo(x,285);h.lineTo(x+95,468);h.close();c.drawPath(h,p);}
    }

    private void update(float dt){
        player.vx=(right?SPEED:0)-(left?SPEED:0);
        if(jump && player.onGround && !player.jumpHeld){player.vy=JUMP;player.onGround=false;player.jumpHeld=true;sfx(650,960,0.15);}
        if(!jump)player.jumpHeld=false;
        player.vy+=GRAV*dt;player.x+=player.vx*dt;player.y+=player.vy*dt;player.onGround=false;
        float ground=468;
        if(player.y+player.h>=ground&&player.vy>=0){player.y=ground-player.h;player.vy=0;player.onGround=true;}
        float[][] plats={{360,400,500,430},{820,380,980,410},{1340,340,1480,370},{1880,385,2080,415},{2700,350,2860,380},{3480,390,3670,420},{4200,320,4380,350},{540,430,640,462},{1060,420,1160,452},{1520,410,1620,442},{2190,430,2320,462},{2900,410,3010,442},{3750,420,3880,452}};
        for(float[] q:plats){if(player.x+player.w>q[0]&&player.x<q[2]&&player.y+player.h>q[1]&&player.y+player.h<q[3]+18&&player.vy>=0){player.y=q[1]-player.h;player.vy=0;player.onGround=true;}}
        if(player.y>620){lives--; if(lives<=0)state=3;else resetPlayer();}
        float[][] cs={{430,360},{610,395},{910,335},{1140,375},{1410,295},{1710,420},{1950,340},{2250,385},{2780,305},{3560,350},{3840,380},{4320,275}};
        for(int i=0;i<cs.length;i++)if(!player.coinGot[i]&&dist(player.x+16,player.y+18,cs[i][0],cs[i][1])<34){player.coinGot[i]=true;coins++;score+=100;sfx(880,1320,0.11);}
        for(Enemy e:enemies){if(!e.alive)continue;e.x+=e.vx*dt;if(e.x<e.min)e.vx=Math.abs(e.vx);if(e.x>e.max)e.vx=-Math.abs(e.vx);e.anim+=dt;if(intersects(player.x,player.y,player.w,player.h,e.x,e.y,32,32)){if(player.vy>180){e.alive=false;player.vy=-350;score+=250;sfx(150,100,0.10);}else{lives--;if(lives<=0)state=3;else resetPlayer();}}}
        player.anim+=dt*(player.vx==0?2.5f:10f); float target=player.x-300;camX+=(Math.max(0,Math.min(4020,target))-camX)*Math.min(1,dt*5f);if(player.x>4550)state=2;
    }

    private void drawWorld(Canvas c){
        sky(c);c.save();c.translate(-camX,0);
        p.setColor(0xFF4C9AD2);c.drawRect(0,468,4900,540,p);
        float[][] ground={{0,468,700,540},{760,468,1280,540},{1300,468,1760,540},{1820,468,2420,540},{2480,468,3200,540},{3260,468,4000,540},{4060,468,4600,540}};
        for(float[] q:ground)drawGround(c,q[0],q[1],q[2],q[3]);
        float[][] plats={{360,400,500,430},{820,380,980,410},{1340,340,1480,370},{1880,385,2080,415},{2700,350,2860,380},{3480,390,3670,420},{4200,320,4380,350},{540,430,640,462},{1060,420,1160,452},{1520,410,1620,442},{2190,430,2320,462},{2900,410,3010,442},{3750,420,3880,452}};
        for(float[] q:plats)drawGround(c,q[0],q[1],q[2],q[3]);
        drawPipe(c,1080,414);drawPipe(c,3050,400);drawPipe(c,3920,416);drawGoal(c,4540,320);
        float[][] cs={{430,360},{610,395},{910,335},{1140,375},{1410,295},{1710,420},{1950,340},{2250,385},{2780,305},{3560,350},{3840,380},{4320,275}};
        for(int i=0;i<cs.length;i++)if(!player.coinGot[i])drawCoin(c,cs[i][0],cs[i][1],((int)(System.nanoTime()/90000000L)+i)&3);
        for(Enemy e:enemies)if(e.alive)drawEnemy(c,e);
        drawPlayerSprite(c,player.x,player.y,(player.vx==0?0:((int)player.anim)&3)+(player.vy<-80?1:0),1f);
        c.restore();
    }

    private void drawGround(Canvas c,float x1,float y1,float x2,float y2){p.setColor(0xFF8A4F31);c.drawRect(x1,y1,x2,y2,p);p.setColor(0xFF4CA84B);c.drawRect(x1,y1,x2,Math.min(y1+12,y2),p);p.setColor(0xFF6D3C2C);for(int x=(int)x1+10;x<x2-8;x+=28)c.drawRect(x,y1+22,x+5,y1+27,p);}
    private void drawPipe(Canvas c,float x,float y){p.setColor(0xFF3FA65D);c.drawRect(x,y,x+68,y+54,p);p.setColor(0xFF6BCB78);c.drawRect(x-7,y,x+75,y+14,p);}
    private void drawGoal(Canvas c,float x,float y){p.setColor(Color.WHITE);c.drawRect(x,y,x+5,y+148,p);p.setColor(0xFFE84B4B);Path f=new Path();f.moveTo(x+5,y+12);f.lineTo(x+62,y+30);f.lineTo(x+5,y+46);f.close();c.drawPath(f,p);}
    private void drawCloud(Canvas c,float x,float y){p.setColor(0xDDFFFFFF);c.drawCircle(x,y,18,p);c.drawCircle(x+22,y-8,23,p);c.drawCircle(x+50,y,18,p);c.drawRect(x,y,x+50,y+18,p);}

    // Pixel-art sprites are generated with rectangles, giving frame-by-frame animation without external engines.
    private void drawPlayerSprite(Canvas c,float x,float y,int frame,float s){c.save();c.translate(x,y);c.scale(s,s);int bob=(frame&1)==1?1:0;
        p.setColor(0xFF23202B);c.drawRect(6,42,12,48,p);c.drawRect(19,42,25,48,p);p.setColor(0xFF2D76C8);c.drawRect(9,33+bob,15,42+bob,p);c.drawRect(17,33+bob,23,42+bob,p);c.drawRect(8,23+bob,23,36+bob,p);p.setColor(0xFFF0A24B);c.drawRect(11,21+bob,21,30+bob,p);p.setColor(0xFFF3BA7A);c.drawRect(4,25+bob,8,36+bob,p);c.drawRect(24,25+bob,28,36+bob,p);c.drawRect(8,9+bob,24,24+bob,p);p.setColor(0xFFE04040);c.drawRect(10,6+bob,22,11+bob,p);c.drawRect(7,9+bob,24,12+bob,p);p.setColor(0xFF4E3328);c.drawRect(8,11+bob,11,18+bob,p);c.drawRect(21,12+bob,24,18+bob,p);p.setColor(0xFF202020);c.drawRect(18,15+bob,19,16+bob,p);c.restore();}
    private void drawEnemy(Canvas c,Enemy e){int leg=((int)(e.anim*5))&1;p.setColor(0xFF8F5336);c.drawRect(e.x+5,e.y+8,e.x+27,e.y+26,p);p.setColor(0xFFC06F48);c.drawRect(e.x+8,e.y+10,e.x+24,e.y+16,p);p.setColor(0xFF171717);c.drawRect(e.x+8,e.y+18,e.x+11,e.y+20,p);c.drawRect(e.x+20,e.y+18,e.x+23,e.y+20,p);p.setColor(0xFF3A2B24);if(leg==0){c.drawRect(e.x+6,e.y+25,e.x+12,e.y+30,p);c.drawRect(e.x+19,e.y+25,e.x+25,e.y+30,p);}else{c.drawRect(e.x+4,e.y+25,e.x+11,e.y+29,p);c.drawRect(e.x+20,e.y+25,e.x+27,e.y+29,p);}}
    private void drawCoin(Canvas c,float x,float y,int f){float w=new float[]{14,10,7,10}[f];p.setColor(0xFFFFDC49);c.drawOval(x-w/2,y-10,x+w/2,y+10,p);p.setColor(0xFFD69A26);c.drawOval(x-w/2+2,y-8,x+w/2-2,y+8,p);}

    private void drawHud(Canvas c){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextAlign(Paint.Align.LEFT);p.setTextSize(18);p.setColor(Color.WHITE);c.drawText("WORLD "+level+"-1",24,34,p);c.drawText("SCORE "+score,24,58,p);c.drawText("COINS "+coins,220,34,p);c.drawText("LIVES "+lives,220,58,p);p.setColor(0x66304B66);c.drawCircle(70,464,48,p);c.drawCircle(175,464,48,p);c.drawCircle(875,464,58,p);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(34);p.setColor(Color.WHITE);c.drawText("◀",70,476,p);c.drawText("▶",175,476,p);c.drawText("▲",875,476,p);}
    private void drawOverlay(Canvas c,String title,String sub){p.setColor(0x990E1724);c.drawRect(0,0,VW,VH,p);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(0xFFFFE35A);p.setTextSize(54);c.drawText(title,480,240,p);p.setColor(Color.WHITE);p.setTextSize(24);c.drawText(sub,480,290,p);}

    private void sfx(int f1,int f2,float dur){new Thread(()->{try{int sr=22050,n=(int)(dur*sr),min=AudioTrack.getMinBufferSize(sr,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);AudioTrack t=new AudioTrack(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build(),new AudioFormat.Builder().setSampleRate(sr).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build(),Math.max(min,n*2),AudioTrack.MODE_STATIC,AudioManager.AUDIO_SESSION_ID_GENERATE);short[] b=new short[n];for(int i=0;i<n;i++){double tt=i/(double)sr;double f=f1+(f2-f1)*(i/(double)n);double env=1.0-Math.min(1.0,i/(double)n);b[i]=(short)(Math.sin(2*Math.PI*f*tt)*env*5000);}t.write(b,0,b.length);t.play();Thread.sleep((long)(dur*1000+80));t.release();}catch(Exception ignored){}}).start();}

    @Override public boolean onTouchEvent(MotionEvent e){int a=e.getActionMasked(),idx=e.getActionIndex();if(state==0){if(a==MotionEvent.ACTION_DOWN){float x=e.getX()/scale,y=e.getY()/scale;if(x>280&&x<680&&y>340&&y<470)startGame();}return true;}if(state==2){if(a==MotionEvent.ACTION_DOWN){level++;score+=500;resetLevel();state=1;}return true;}if(state==3){if(a==MotionEvent.ACTION_DOWN){lives=3;coins=0;score=0;level=1;resetLevel();state=1;}return true;}if(a==MotionEvent.ACTION_DOWN||a==MotionEvent.ACTION_POINTER_DOWN){int id=e.getPointerId(idx);pointers.put(id,controlAt(e.getX(idx)/scale,e.getY(idx)/scale));rebuild();return true;}if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_POINTER_UP||a==MotionEvent.ACTION_CANCEL){pointers.remove(e.getPointerId(idx));rebuild();return true;}if(a==MotionEvent.ACTION_MOVE){for(int i=0;i<e.getPointerCount();i++)pointers.put(e.getPointerId(i),controlAt(e.getX(i)/scale,e.getY(i)/scale));rebuild();return true;}return true;}
    private int controlAt(float x,float y){if(y<400)return 0;if(x<125)return 1;if(x<250)return 2;if(x>790)return 3;return 0;}
    private void rebuild(){left=right=jump=false;for(int v:pointers.values()){if(v==1)left=true;else if(v==2)right=true;else if(v==3)jump=true;}}
    private void startGame(){score=coins=0;lives=3;level=1;resetLevel();state=1;}
    private void resetLevel(){for(Enemy e:enemies){e.alive=true;e.anim=0;}for(int i=0;i<player.coinGot.length;i++)player.coinGot[i]=false;camX=0;resetPlayer();}
    private void resetPlayer(){player.x=90;player.y=420;player.vx=0;player.vy=0;player.anim=0;player.onGround=false;}
    private static boolean intersects(float x,float y,float w,float h,float a,float b,float aw,float ah){return x<a+aw&&x+w>a&&y<b+ah&&y+h>b;}
    private static float dist(float a,float b,float c,float d){float x=a-c,y=b-d;return(float)Math.sqrt(x*x+y*y);}
    private static class Player{float x=90,y=420,vx,vy,anim;float w=30,h=44;boolean onGround,jumpHeld;boolean[] coinGot=new boolean[12];}
    private static class Enemy{float x,y=436,vx=55,min,max,anim;boolean alive=true;Enemy(float x){this.x=x;min=x-75;max=x+75;}}
}
