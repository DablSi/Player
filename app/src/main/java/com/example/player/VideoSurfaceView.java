package com.example.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;

//данный класс не используется и является запасным прототипом
//на случай, если данной синхронизации видео будет недостаточно
// !!! this class is not used and is a spare prototype
// !!! in case this video sync is not enough
public class VideoSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawThread drawThread;
    int x = 0, y = 0, width = 640, height = 480;
    public static boolean end = false;

    public VideoSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        end = false;
        drawThread = new DrawThread(getContext(), getHolder());
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.requestStop();
    }

    class DrawThread extends Thread {

        private SurfaceHolder surfaceHolder;

        private volatile boolean running = true; // флаг для остановки потока

        public DrawThread(Context context, SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void requestStop() {
            running = false;
            end = true;
        }

        @Override
        public void run() {
            final Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                public void run() {
                    if (ExtractMpegFramesTest.list.size() > 0) {
                        if (!running)
                            timer.cancel();
                        Canvas canvas = surfaceHolder.lockCanvas();
                        Paint paint = new Paint();
                        try {
                            DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
                            Log.e("Size", ExtractMpegFramesTest.list.size() + "");
                            Bitmap bm = Bitmap.createBitmap(ExtractMpegFramesTest.list.get(0), x, y, width, height);
                            bm = Bitmap.createScaledBitmap(bm, (int) (width * ((float) metrics.widthPixels / (float) width)), (int) ((float) height * ((float) metrics.heightPixels / (float) height)), true);
                            canvas.drawBitmap(bm, new Matrix(), paint);
                            ExtractMpegFramesTest.list.remove(0);
                        } catch (NoSuchElementException e) {
                            e.printStackTrace();
                            if (end)
                                running = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (running)
                                surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }, 0, 33);
        }
    }
}

