package com.example.tina.droplets;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DisplayPopup extends Activity {
    private TextView resume, quit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        TextView text = (TextView) findViewById(R.id.pause);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/game.ttf");
        text.setTypeface(tf);*/
        setContentView(R.layout.activity_display_popup);

        TextView resume = (TextView) findViewById(R.id.resume);
        resume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView quit = (TextView) findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                ((Activity) Drops.mContext).finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        Drops.reset();
    }

}
