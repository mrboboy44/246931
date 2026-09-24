package com.example.jungleplatformer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class GameView extends View {
    private static final float W = 1536f, H = 1024f;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float x=225, y=420, vx, vy, anim;
    private boolean left,right,jump,grounded;
    private int hearts=3, coins=0, gems=0;

    private final float[][] platforms = {
        {0,760,970,1024},{125,495,590,535},{735,458,1490,515},
        {765,265,1120,315},{1170,188,1495,245},{1210,760,1536,1024}
    };

    public GameView(Context c){ super(c); setFocusable(true); text.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); }

    @Override protected void onDraw(Canvas c){
        float dt=0.016f;
        anim += dt;
        update(dt);
        drawWorld(c);
        postInvalidateOnAnimation();
    }

    private void update(float dt){
        float target=(right?290:0)-(left?290:0);
        vx += (target-vx)*Math.min(1,dt*10);
        if(!left&&!right) vx*=0.90f;
        vy += 1150*dt;
        if(jump&&grounded){ vy=-530; grounded=false; }
        float oldBottom=y+58;
        x+=vx*dt; y+=vy*dt;
        x=Math.max(15,Math.min(W-50,x));
        grounded=false;
        for(float[] q:platforms){
            if(x+38>q[0]&&x<q[2]&&oldBottom<=q[1]&&y+58>=q[1]&&vy>=0){
                y=q[1]-58; vy=0; grounded=true; break;
            }
        }
        if(y>H+70){ hearts--; if(hearts<=0)hearts=3; x=225;y=425;vy=0; }
        if(Math.hypot(x-650,y-470)<70) coins=Math.max(coins,5);
        if(Math.hypot(x-865,y-305)<70) gems=Math.max(gems,2);
    }

    private void drawWorld(Canvas c){
        float s=Math.min(getWidth()/W,getHeight()/H), ox=(getWidth()-W*s)/2, oy=(getHeight()-H*s)/2;
        c.drawColor(Color.rgb(18,67,73));
        c.save(); c.translate(ox,oy); c.scale(s,s);
        drawBackground(c); drawPlatforms(c); drawObjects(c); drawPlayer(c); drawControls(c);
        c.restore(); drawHud(c);
    }

    private void drawBackground(Canvas c){
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(93,171,161)); c.drawRect(0,0,W,H,p);
        p.setColor(Color.rgb(66,137,139));
        for(int i=0;i<9;i++){
            float bx=i*190-40;
            Path t=new Path(); t.moveTo(bx,0); t.lineTo(bx+100,0); t.lineTo(bx+45,230);
            t.lineTo(bx+140,390); t.lineTo(bx+80,570); t.lineTo(bx-20,710); t.lineTo(bx-40,0); t.close();
            c.drawPath(t,p);
        }
        p.setColor(Color.rgb(42,95,99));
        p.setStrokeWidth(22); p.setStyle(Paint.Style.STROKE);
        for(int i=0;i<7;i++){ Path t=new Path(); float bx=60+i*230; t.moveTo(bx,0); t.cubicTo(bx-30,130,bx+90,200,bx+10,340); t.cubicTo(bx-40,460,bx+45,540,bx+15,700); c.drawPath(t,p); }
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.argb(65,245,248,211));
        for(int i=0;i<35;i++){ float px=(i*131)%W, py=(i*79)%H; c.drawCircle(px,py,2+(i%3),p); }
    }

    private void drawPlatforms(Canvas c){
        for(float[] q:platforms){
            p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(118,62,28));
            c.drawRoundRect(new RectF(q[0],q[1]+18,q[2],q[3]+50),24,24,p);
            p.setColor(Color.rgb(66,117,47));
            c.drawRect(q[0],q[1],q[2],q[1]+34,p);
            p.setColor(Color.rgb(112,178,67));
            c.drawRect(q[0],q[1],q[2],q[1]+9,p);
            p.setColor(Color.rgb(70,104,49));
            for(float xx=q[0]+24;xx<q[2]-12;xx+=65){
                Path g=new Path(); g.moveTo(xx,q[1]+8); g.lineTo(xx+12,q[1]+28); g.lineTo(xx+24,q[1]+8); g.close(); c.drawPath(g,p);
            }
            p.setColor(Color.rgb(91,72,48));
            for(float xx=q[0]+45;xx<q[2]-10;xx+=130) c.drawCircle(xx,q[1]+55,10,p);
        }
    }

    private void drawObjects(Canvas c){
        palm(c,245,485,0.9f); palm(c,770,255,0.95f);
        palm(c,1320,180,0.75f);
        sign(c,205,470,"?");
        sign(c,985,425,"→");
        sign(c,1410,405,"→");
        crate(c,1240,725,70,80); crate(c,1300,650,65,90);
        chest(c,300,445); chest(c,760,420);
        egg(c,870,428); apple(c,1390,445); mushrooms(c,1080,425);
        ladder(c,970,270,160);
        gems(c,415,92); starBox(c,330,150); questionBox(c,550,150);
        coinColumn(c,675,350);
        key(c,67,192); key(c,1435,340);
        cherry(c,55,565);
        barrel(c,655,700);
    }

    private void palm(Canvas c,float x,float y,float z){
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(18*z); p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(Color.rgb(109,64,34)); Path t=new Path(); t.moveTo(x,y+120*z); t.cubicTo(x-38*z,y+75*z,x-12*z,y+18*z,x+10*z,y-5*z); c.drawPath(t,p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(35,193,87));
        for(int i=0;i<5;i++){ double a=(-1.7+i*0.85); float ex=x+(float)Math.cos(a)*65*z, ey=y+(float)Math.sin(a)*55*z; c.drawOval(new RectF(Math.min(x,ex),Math.min(y-15*z,ey-15*z),Math.max(x,ex),Math.max(y+15*z,ey+15*z)),p); }
        p.setColor(Color.rgb(170,89,41)); c.drawOval(new RectF(x-22*z,y+20*z,x+22*z,y+55*z),p);
    }

    private void sign(Canvas c,float x,float y,String s){
        p.setColor(Color.rgb(95,57,34)); c.drawRect(x,y,x+10,y+70,p);
        p.setColor(Color.rgb(197,139,72)); Path a=new Path(); a.moveTo(x-20,y); a.lineTo(x+95,y+8); a.lineTo(x+100,y+48); a.lineTo(x-25,y+38); a.close(); c.drawPath(a,p);
        text.setTextSize(32); text.setColor(Color.rgb(78,43,31)); c.drawText(s,x+26,y+35,text);
    }

    private void crate(Canvas c,float x,float y,float w,float h){
        p.setColor(Color.rgb(76,76,78)); c.drawRoundRect(new RectF(x,y,x+w,y+h),8,8,p);
        p.setColor(Color.rgb(117,117,118)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(7); c.drawRect(x+4,y+4,x+w-4,y+h-4,p);
        p.setStyle(Paint.Style.FILL);
    }

    private void chest(Canvas c,float x,float y){
        p.setColor(Color.rgb(164,91,42)); c.drawRoundRect(new RectF(x,y,x+88,y+55),12,12,p);
        p.setColor(Color.rgb(229,163,71)); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(6); c.drawLine(x+7,y+26,x+81,y+26,p); c.drawRect(x+42,y+21,x+52,y+40,p); p.setStyle(Paint.Style.FILL);
    }

    private void egg(Canvas c,float x,float y){ p.setColor(Color.rgb(229,229,205)); c.drawOval(new RectF(x,y,x+42,y+55),p); p.setColor(Color.rgb(137,197,102)); c.drawCircle(x+12,y+20,6,p); c.drawCircle(x+28,y+34,6,p); }
    private void apple(Canvas c,float x,float y){ p.setColor(Color.rgb(201,42,38)); c.drawCircle(x,y,25,p); p.setColor(Color.rgb(65,98,41)); c.drawRect(x-3,y-31,x+2,y-22,p); }
    private void mushrooms(Canvas c,float x,float y){ for(int i=0;i<3;i++){float xx=x+i*34; p.setColor(Color.WHITE);c.drawRect(xx,y+18,xx+9,y+44,p);p.setColor(Color.rgb(216,51,44));c.drawOval(new RectF(xx-9,y,xx+18,y+27),p);} }
    private void ladder(Canvas c,float x,float y,float h){ p.setColor(Color.rgb(203,46,119)); p.setStrokeWidth(9); c.drawLine(x,y,x,y+h,p); c.drawLine(x+68,y,x+68,y+h,p); for(float yy=y+18;yy<y+h;yy+=30)c.drawLine(x,yy,x+68,yy,p); }
    private void gems(Canvas c,float x,float y){ for(int i=0;i<2;i++){p.setColor(Color.rgb(219,92,219)); Path g=new Path(); float xx=x+i*72; g.moveTo(xx,y+30);g.lineTo(xx+30,y);g.lineTo(xx+62,y+10);g.lineTo(xx+48,y+55);g.close();c.drawPath(g,p);} }
    private void starBox(Canvas c,float x,float y){ box(c,x,y,Color.rgb(136,66,143)); text.setTextSize(35);text.setColor(Color.rgb(255,211,56));c.drawText("★",x+18,y+39,text); }
    private void questionBox(Canvas c,float x,float y){ box(c,x,y,Color.rgb(137,58,139));text.setTextSize(35);text.setColor(Color.rgb(255,211,56));c.drawText("?",x+25,y+39,text); }
    private void box(Canvas c,float x,float y,int color){p.setColor(color);c.drawRoundRect(new RectF(x,y,x+78,y+78),8,8,p);p.setColor(Color.argb(100,255,255,255));c.drawRect(x+5,y+5,x+73,y+11,p);p.setColor(Color.rgb(98,43,44));c.drawRect(x+10,y+62,x+68,y+68,p); }
    private void coinColumn(Canvas c,float x,float y){for(int i=0;i<5;i++){p.setColor(Color.rgb(244,171,35));c.drawCircle(x,y+i*38,13,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);p.setColor(Color.rgb(255,224,75));c.drawCircle(x,y+i*38,13,p);p.setStyle(Paint.Style.FILL);}}
    private void key(Canvas c,float x,float y){p.setColor(Color.rgb(238,175,37));c.drawCircle(x,y,9,p);c.drawRect(x+5,y-4,x+34,y+4,p);c.drawRect(x+26,y+1,x+31,y+11,p);c.drawRect(x+17,y+1,x+22,y+8,p);}
    private void cherry(Canvas c,float x,float y){p.setColor(Color.rgb(212,42,38));c.drawCircle(x,y,13,p);c.drawCircle(x+18,y+4,13,p);p.setColor(Color.rgb(64,112,49));p.setStrokeWidth(4);c.drawLine(x+2,y-8,x-4,y-26,p);c.drawLine(x+15,y-7,x+8,y-25,p);}
    private void barrel(Canvas c,float x,float y){p.setColor(Color.rgb(164,105,57));c.drawOval(new RectF(x,y,x+66,y+85),p);p.setColor(Color.rgb(99,65,38));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(6);c.drawOval(new RectF(x+7,y+4,x+59,y+81),p);c.drawLine(x+4,y+26,x+62,y+26,p);c.drawLine(x+4,y+57,x+62,y+57,p);p.setStyle(Paint.Style.FILL);}

    private void drawPlayer(Canvas c){
        float yy=y+(grounded?(float)Math.sin(anim*9)*2:0);
        p.setColor(Color.rgb(244,188,120));c.drawCircle(x+20,yy+15,15,p);
        p.setColor(Color.rgb(68,91,165));c.drawRoundRect(new RectF(x+6,yy+28,x+34,yy+53),8,8,p);
        p.setColor(Color.rgb(40,48,80));c.drawRect(x+8,yy+49,x+17,yy+60,p);c.drawRect(x+23,yy+49,x+32,yy+60,p);
        p.setColor(Color.WHITE);c.drawCircle(x+15,yy+13,3,p);c.drawCircle(x+25,yy+13,3,p);
        p.setColor(Color.BLACK);c.drawCircle(x+15,yy+13,1.5f,p);c.drawCircle(x+25,yy+13,1.5f,p);
    }

    private void drawControls(Canvas c){
        float y0=H-92;
        p.setAlpha(75);p.setColor(Color.BLACK);c.drawCircle(90,y0,50,p);c.drawCircle(205,y0,50,p);c.drawCircle(W-105,y0,56,p);
        p.setAlpha(225);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(42);text.setColor(Color.WHITE);c.drawText("‹",90,y0+14,text);c.drawText("›",205,y0+14,text);text.setTextSize(24);c.drawText("JUMP",W-105,y0+8,text);text.setTextAlign(Paint.Align.LEFT);p.setAlpha(255);
    }

    private void drawHud(Canvas c){
        text.setShadowLayer(4,2,2,Color.BLACK);text.setColor(Color.WHITE);text.setTextSize(34);c.drawText("♥ ♥ ♥",24,44,text);c.drawText("◆ "+gems+"   ● "+coins,24,84,text);text.clearShadowLayer();
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        int a=e.getActionMasked();
        if(a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL){left=right=jump=false;return true;}
        float s=Math.min(getWidth()/W,getHeight()/H), ox=(getWidth()-W*s)/2, oy=(getHeight()-H*s)/2;
        float tx=(e.getX()-ox)/s, ty=(e.getY()-oy)/s;
        left=tx<155&&ty>H-190; right=tx>=155&&tx<295&&ty>H-190; jump=tx>W-210&&ty>H-210;
        return true;
    }
}
