package at.ac.tuwien.e0826357.cardioDataViewer;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import at.ac.tuwien.e0826357.cardioDataViewer.service.CardiovascularDataService;
import at.ac.tuwien.e0826357.cardioDataViewer.service.ServiceManager;
import at.ac.tuwien.e0826357.cardioDataViewer.util.SystemUiHider;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {

	private static final boolean AUTO_HIDE = true;
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = false;
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	private SystemUiHider mSystemUiHider;

	private CardiovascularDataService dataService;
	private GraphView graph;

	private static class GraphViewObserver implements Observer {

		private GraphViewSeries series;
		private boolean hasChanged;

		public GraphViewObserver(GraphViewSeries series) {
			this.series = series;
			hasChanged = false;
		}

		@Override
		public void update(Observable obs, Object obj) {
			// TODO
			if (obj instanceof GraphViewDataInterface) {
				update(obs, (GraphViewDataInterface) obj);
			}
			hasChanged = true;
		}

		// public void update(Observable obs, ECTimeSeriesSegment segment) {
		// TODO
		// }

		private void update(Observable obs, GraphViewDataInterface data) {
			series.appendData(data, false, 1861);
		}

		public synchronized boolean isHasChangedAndReset() {
			boolean hasChangedCur = hasChanged;
			hasChanged = false;
			return hasChangedCur;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// auxiliary
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.main_activity);
		LinearLayout graphLayout = (LinearLayout) findViewById(R.id.graphLayout);
		this.graph = new LineGraphView(this, "Channel 1");

		graph.setBackgroundColor(Color.WHITE);
		graph.setViewPort(0, 1861);
		graph.setScrollable(true);
		graph.setScalable(false);
		graph.setShowLegend(false);
		graph.setLegendAlign(LegendAlign.BOTTOM);

		graph.setManualMaxY(true);
		graph.setManualMinY(true);
		graph.setManualYMaxBound(1.0);
		graph.setManualYMinBound(-1.0);

		graphLayout.addView(graph);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, graphLayout,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		graphLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);

		// logic
		GraphViewSeries channelOneSeries = new GraphViewSeries(
				"Channel 1 Signal", new GraphViewSeriesStyle(Color.BLACK, 2),
				new GraphViewData[] { new GraphViewData(0, 0) });
		this.graph.addSeries(channelOneSeries);

		final GraphViewObserver serviceObserver = new GraphViewObserver(
				channelOneSeries);
		dataService = ServiceManager.getInstance()
				.getCardiovascularDataService();
		dataService.addObserver(serviceObserver);

		final Handler threadHandler = new Handler();
		final Runnable redrawAction = new Runnable() {
			@Override
			public void run() {
				if (serviceObserver.isHasChangedAndReset())
					graph.redrawAll();
			}
		};
		Thread refresherThread = new Thread() {
			@Override
			public void run() {
				while (!isInterrupted()) {
					try {
						sleep(18);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					threadHandler.post(redrawAction);
				}
			}
		};
		refresherThread.start();

		dataService.start();

	}

	@Override
	public void onResume() {
		// TODO
		super.onResume();
		if (dataService != null)
			dataService.start();
	}

	@Override
	public void onPause() {
		// TODO
		super.onPause();
		if (dataService != null)
			dataService.stop();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility") // TODO (low) check
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

}
