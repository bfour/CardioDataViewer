/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.ac.tuwien.e0826357.cardioapp.application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.R;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.GenericGetter;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.Triple;
import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;
import at.ac.tuwien.e0826357.cardioapp.service.CardiovascularDataService;
import at.ac.tuwien.e0826357.cardioapp.service.GraphViewObserver;
import at.ac.tuwien.e0826357.cardioapp.service.ServiceManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity {

    private CardiovascularDataService dataService;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.graphs);

        // Set up the user interaction to manually show or hide the system UI.a
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.startstop_button).setOnTouchListener(mDelayHideTouchListener);

        findViewById(R.id.test_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != MotionEvent.ACTION_DOWN)
                    return false;
                Intent intent = new Intent(MainActivity.this, TestActivity.class);
                startActivity(intent);
                return true;
            }
        });

        // setup graphs etc.
        GraphViewObserver serviceObserver = new GraphViewObserver(setupGraphs());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            String serverAddress = sharedPref.getString("serverAddress", "");
            Integer serverPort = Integer.parseInt(sharedPref.getString("serverPort", ""));
            dataService = ServiceManager.getInstance(
                    serverAddress, serverPort).getCardiovascularDataService(MainActivity.this, serviceObserver);
            findViewById(R.id.startstop_button).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (dataService.isRunning()) {
                            dataService.stop();
                            ((Button) findViewById(R.id.startstop_button)).setText(R.string.start_stop_button_start);
                        } else {
                            dataService.start();
                            ((Button) findViewById(R.id.startstop_button)).setText(R.string.start_stop_button_stop);
                        }
                    }
                    return false;
                }
            });
        } catch (NumberFormatException e) {
            e.printStackTrace();
            // TODO implement proper error handling
            Toast.makeText(
                    MainActivity.this,
                    "Sorry, port number seems to be invalid. Closing.",
                    Toast.LENGTH_LONG).show();
        } catch (ServiceException e) {
            e.printStackTrace();
            // TODO implement proper error handling
            Toast.makeText(
                    MainActivity.this,
                    "Sorry, something went wrong. Closing.",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent openSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(openSettingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> setupGraphs() {

        List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> list = new ArrayList<>(
                3);

        // lead I
        GraphView graphI = (GraphView) findViewById(R.id.graph1);
        graphI.setTitle("I");

        graphI.getViewport().setXAxisBoundsManual(true);
//        graphI.getViewport().setMaxXAxisSize(10000);
        graphI.getViewport().setMinX(0);
        graphI.getViewport().setMaxX(0+10000);
        graphI.getViewport().setYAxisBoundsManual(true);
        graphI.getViewport().setMinY(-1);
        graphI.getViewport().setMaxY(1.5);

        graphI.setHorizontalScrollBarEnabled(true);
        graphI.getViewport().setScalable(true);
        graphI.getViewport().setScalableY(false);
        graphI.getViewport().setScrollable(true);
        graphI.getViewport().setScrollableY(false);
//        graphI.setSoundEffectsEnabled(true);


        graphI.getGridLabelRenderer().setNumHorizontalLabels(6);

        LineGraphSeries<DataPoint> seriesI = new LineGraphSeries<>();
        seriesI.setTitle("I");
        seriesI.setDrawDataPoints(true);
        seriesI.setDataPointsRadius(10);
        graphI.addSeries(seriesI);
        list.add(new Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>(
                graphI, seriesI,
                new GenericGetter<CardiovascularData, Double>() {
                    @Override
                    public Double get(CardiovascularData obj) {
                        return (double) obj.getLeadI();
                    }
                }));

        // lead II
        GraphView graphII = (GraphView) findViewById(R.id.graph2);
        graphII.setTitle("II");
        LineGraphSeries<DataPoint> seriesII = new LineGraphSeries<>();
        seriesII.setTitle("II");
        graphII.addSeries(seriesII);
        list.add(new Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>(
                graphII, seriesII,
                new GenericGetter<CardiovascularData, Double>() {
                    @Override
                    public Double get(CardiovascularData obj) {
                        return (double) obj.getLeadII();
                    }
                }));

        // lead III
        GraphView graphIII = (GraphView) findViewById(R.id.graph3);
        graphIII.setTitle("III");
        LineGraphSeries<DataPoint> seriesIII = new LineGraphSeries<>();
        seriesIII.setTitle("III");
        graphIII.addSeries(seriesIII);
        list.add(new Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>(
                graphIII, seriesIII,
                new GenericGetter<CardiovascularData, Double>() {
                    @Override
                    public Double get(CardiovascularData obj) {
                        return (double) obj.getLeadIII();
                    }
                }));

        return list;
    }

}
