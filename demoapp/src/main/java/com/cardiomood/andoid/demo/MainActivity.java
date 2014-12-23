package com.cardiomood.andoid.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;


public class MainActivity extends ActionBarActivity {

    private SpeedometerGauge speedometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);
        speedometer.setMaxSpeed(300);
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setMaxSpeed(300);
        speedometer.setMajorTickStep(30);
        speedometer.setMinorTicks(2);
        speedometer.addColoredRange(30, 140, Color.GREEN);
        speedometer.addColoredRange(140, 180, Color.YELLOW);
        speedometer.addColoredRange(180, 400, Color.RED);
        speedometer.setSpeed(80, 1000, 300);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
