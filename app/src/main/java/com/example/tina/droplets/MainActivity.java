package com.example.tina.droplets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends Activity {
    private static Drops drops;
    private static boolean inGame = false;
    private static boolean inHowToPlay = false;
    public static boolean inDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        inGame = false;

    }

    public void startDrops(View view) {

        drops = new Drops(this);

        setContentView(drops);
        inGame = true;

    }

    public void howToPlay(View view) {

        setContentView(R.layout.how_to_play);
        inHowToPlay = true;

    }

    @Override
    public void onBackPressed() {

        // Pressed back in game
        if (inGame) {

            Intent intent = new Intent(this, DisplayPopup.class);
            startActivity(intent);

            // Correctly pause drops game thread
            Drops.isPaused();

        // Pressed back in How To Play
        } else if (inHowToPlay) {

            setContentView(R.layout.activity_main);
            inHowToPlay = false;

        }   else {

            // Close the app
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // If drops is on pause
        if (Drops.isPaused) {

            // Restart the timer and set paused to false
            Drops.restartTimer();
            Drops.isPaused = false;
        }
    }
}

class Drops extends SurfaceView implements SurfaceHolder.Callback {

    public static Context mContext; // Context in this thread

    private GameThread thread; // Thread of game

    // Screen dimensions
    private static int screenW; //Device's screen width.
    private static int screenW13; //1/3 screen width
    private static int screenW23; //2/3 screen width
    private static int screenH; //Devices's screen height.
    private static int screenH14; //1/3 screen width
    private static int screenH34; //3/3 screen width

    private static float x; // X coordinate of user touch

    private Bitmap origDrop, drop, origUmb, umb; // Bitmap of images

    // Dimensions of bitmaps
    private static int dropH, dropW;
    private static int umbH, umbW;

    // Positions for bitmaps on screen
    private static int dropY; // Drop y position.
    private static float umbX, umbY; // X and Y coordinates of umbrella

    // Offsets for drops
    private static final int dropOffsetPos1 = 79;
    private static final int dropOffsetPos2 = 50;
    private static final int dropOffsetPos3 = 30;
    private static final int dropOffsetY = 20;

    // Offsets for umbrella
    private static final int umbOffsetPos1 = 13;
    private static final int umbOffsetPos2 = 12;
    private static final int umbOffsetPos3 = 30;
    private static final int umbOffsetY = 45;


    // Random generated placement of drops
    private Random randomGenerator;
    private int[] random = new int[3];

    // Score and seconds  left
    private static int score = 0; // Score of user
    private static int secUntilFinished;

    private static final int textSize = 50;

    // Offsets for score and seconds left
    private static final int scoreOffset = 20;
    private static final int secUntilFinishedOffset = 160;
    private static final int scoreOffsetY = 55;
    private static final int secUntilFinishedOffsetY = 55;


    // Timer for game
    public static CountDownTimer timer;
    private static int timeLeft;

    public static boolean isPaused = false; // Checks if the game is paused

    private boolean touchedBefore = false; // Keeps track of finger up/down movement

    private Paint mPaint = new Paint(); // Paints on score and seconds left

    public Drops(Context context) {
        super(context);

        mContext = context;

        // Decode images
        origDrop = BitmapFactory.decodeResource(getResources(), R.drawable.drop); //Load a droplet image
        origUmb = BitmapFactory.decodeResource(getResources(), R.drawable.umb); //Load a umbrella image

        // Get dimensions
        dropH = origDrop.getHeight();
        dropW = origDrop. getWidth();

        umbH = origUmb.getHeight();
        umbW = origUmb.getWidth();

        //Set thread
        getHolder().addCallback(this);

        setFocusable(true);

        // Call reset to set up
        reset();

    }

    public static void reset() {
        umbX = screenW13 - umbOffsetPos2;
        umbY = screenH34 + umbOffsetY;

        score = 0;
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(20000, 1000) {

            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished % 1 == 0) {
                    secUntilFinished = (int) (millisUntilFinished / 1000);
                }
            }

            public void onFinish() {
                secUntilFinished = 0;

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                View view = inflater.inflate(R.layout.game_over, null);
                builder.setView(view);

                View closeButton = view.findViewById(R.id.quit);
                View retryButton = view.findViewById(R.id.retry);

                TextView scoreText = (TextView) view.findViewById(R.id.score);
                scoreText.setText(String.valueOf(score));

                final AlertDialog finishDialog = builder.create();
                finishDialog.show();
                MainActivity.inDialog = true;

                closeButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View clicked) {
                        if (clicked.getId() == R.id.quit) {
                            finishDialog.dismiss();
                            ((Activity) mContext).finish();
                        }
                    }
                });

                // Accounts for pressing back button in dialog
                finishDialog.setOnKeyListener(new Dialog.OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        // TODO Auto-generated method stub
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            reset();
                            finishDialog.dismiss();
                        }
                        return true;
                    }
                });

                retryButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View clicked) {
                        if (clicked.getId() == R.id.retry) {
                            Drops.reset();
                            finishDialog.dismiss();
                        }
                    }
                });


            }
        }.start();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //This event-method provides the real dimensions of this custom view.
        screenW = w;
        screenW13 = screenW / 3;
        screenW23 = screenW13 * 2;
        screenH = h;
        screenH14 = screenH / 4;
        screenH34 = screenH14 * 3;

        // Load real umbrella and drop
        umb = Bitmap.createScaledBitmap(origUmb, (int)(umbW*.352), (int)(umbH*.352), true);
        drop = Bitmap.createScaledBitmap(origDrop, (int)(dropW*.33), (int)(dropH*.33), true);

        // Generate random location for drops
        randomGenerator = new Random();

        for (int index = 0; index < 3; ++index) {
            int randomInt = randomGenerator.nextInt(3);
            random[index] = randomInt;
        }

        // Set up initial position of umbrella
        umbX = screenW13 - umbOffsetPos2;
        umbY = screenH34 + umbOffsetY;


    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {

        x = ev.getX();

        if (!touchedBefore) {
            // Position 0
            if (x <= screenW13) {

                if (random[2] == 0) {
                    move();
                }

                //Move umbrella
                umbX = umbOffsetPos1;

                // Position 1
            } else if (x > screenW13 && x <= screenW23) {

                if (random[2] == 1) {
                    move();
                }

                //Move umbrella
                umbX = screenW13 - umbOffsetPos2;

                // Position 2
            } else {

                if (random[2] == 2) {
                    move();
                }

                //Move umbrella
                umbX = screenW23 - umbOffsetPos3;

            }
        }

        switch (ev.getAction()) {

            // If finger has pressed down
            case MotionEvent.ACTION_DOWN: {

                touchedBefore = true;

                break;

            }

            // If finger goes up
            case MotionEvent.ACTION_UP: {

                touchedBefore = false;

                break;
            }

        }

        return true;
    }

    // Move each drop and umbrella
    private void move() {

        ++score;

        // Move drops through array
        for (int index = random.length - 2; index >= 0; --index) {
            random[index + 1] = random[index];
        }

        // Add random to first one
        random[0] = randomGenerator.nextInt(3);

    }


    public static void restartTimer() {
            // Cancel the previous timer
            timer.cancel();

            // Set up new timer
            timer = new CountDownTimer(timeLeft*1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished % 1 == 0) {
                        secUntilFinished = (int) (millisUntilFinished / 1000);
                    }
                }

                // When the timer finishes
                public void onFinish() {
                    secUntilFinished = 0;

                    // Set up dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                    View view = inflater.inflate(R.layout.game_over, null);
                    builder.setView(view);

                    View closeButton = view.findViewById(R.id.quit);
                    View retryButton = view.findViewById(R.id.retry);
                    TextView scoreText = (TextView) view.findViewById(R.id.score);

                    scoreText.setText(String.valueOf(score));

                    final AlertDialog finishDialog = builder.create();
                    finishDialog.show();
                    MainActivity.inDialog = true;

                    closeButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View clicked) {
                            if (clicked.getId() == R.id.quit) {

                                // Quit the game
                                finishDialog.dismiss();
                                ((Activity) mContext).finish();
                            }
                        }
                    });

                    retryButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View clicked) {
                            if (clicked.getId() == R.id.retry) {

                                // Reset timer and dismiss dialog
                                Drops.reset();
                                finishDialog.dismiss();
                            }
                        }
                    });
                }
            }.start();

        isPaused = false;

    }

    public static void isPaused() {

        isPaused = true;
        timeLeft = secUntilFinished;
        timer.cancel();

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas != null) {

            // Prepare a paint
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(5);
            mPaint.setAntiAlias(true);

            // Paint background
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, screenW, screenH, mPaint);


            // Place umbrella on canvas
            canvas.drawBitmap(umb, umbX, umbY, null);

            // Place drops on canvas
            for (int index = 0; index < 3; ++index) {
                dropY = (index) * screenH14;

                if (random[index] == 0) {
                    canvas.drawBitmap(drop, dropOffsetPos1, dropY + dropOffsetY, null);
                } else if (random[index] == 1) {
                    canvas.drawBitmap(drop, screenW13 + dropOffsetPos2, dropY + dropOffsetY, null);
                } else if (random[index] == 2) {
                    canvas.drawBitmap(drop, screenW23 + dropOffsetPos3, dropY + dropOffsetY, null);
                }
            }

            // Set text size
            mPaint.setTextSize(textSize);

            // Draw score
            mPaint.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(score), scoreOffset, scoreOffsetY, mPaint);

            // Draw seconds left
            canvas.drawText(String.valueOf(secUntilFinished) + " sec", screenW - secUntilFinishedOffset, secUntilFinishedOffsetY, mPaint);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {

            }
        }
    }


    class GameThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private Drops gameView;
        private boolean run = false;

        public GameThread(SurfaceHolder surfaceHolder, Drops gameView) {
            this.surfaceHolder = surfaceHolder;
            this.gameView = gameView;
        }

        public void setRunning(boolean run) {
            this.run = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return surfaceHolder;
        }

        @SuppressLint("WrongCall")
        @Override
        public void run() {
            Canvas c;
            while (run) {
                c = null;

                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        //call methods to draw and process next fame
                        gameView.onDraw(c);
                    }
                } finally {
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}