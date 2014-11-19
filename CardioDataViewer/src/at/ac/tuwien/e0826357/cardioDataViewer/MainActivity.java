package at.ac.tuwien.e0826357.cardioDataViewer;

import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import at.ac.tuwien.e0826357.cardioDataCommons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioDataCommons.service.ServiceException;
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
			} else if (obj instanceof CardiovascularData) {
				update(obs, (CardiovascularData) obj);
			}
			hasChanged = true;
		}

		// public void update(Observable obs, ECTimeSeriesSegment segment) {
		// TODO
		// }
		private void update(Observable obs, CardiovascularData data) {
			series.appendData(
					new GraphViewData(data.getTime(), data.getECGA()), false,
					18161);
		}

		private void update(Observable obs, GraphViewDataInterface data) {
			series.appendData(data, false, 1861);
		}

		public synchronized boolean isHasChangedAndReset() {
			boolean hasChangedCur = hasChanged;
			hasChanged = false;
			return hasChangedCur;
		}

	}

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
	private Thread refresherThread;
	private GraphViewObserver serviceObserver;
	private GraphView graph;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// auxiliary
		super.onCreate(savedInstanceState);

		// layout
		setContentView(R.layout.main_activity);
		LinearLayout graphLayout = (LinearLayout) findViewById(R.id.graphLayout);
		this.graph = new LineGraphView(this, "Channel 1");

		graph.setBackgroundColor(Color.WHITE);
		graph.setViewPort(0, 18161);
		graph.setScrollable(true);
		graph.setScalable(false);
		graph.setShowLegend(false);
		graph.setLegendAlign(LegendAlign.BOTTOM);

		graph.setManualMaxY(true);
		graph.setManualMinY(true);
		graph.setManualYMaxBound(1024);
		graph.setManualYMinBound(0);

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
		// findViewById(R.id.dummy_button).setOnTouchListener(
		// mDelayHideTouchListener);

		// ask for server location
		letUserSetServer();

		// logic
		GraphViewSeries channelOneSeries = new GraphViewSeries(
				"Channel 1 Signal", new GraphViewSeriesStyle(Color.BLACK, 2),
				new GraphViewData[] { new GraphViewData(0, 0) });
		this.graph.addSeries(channelOneSeries);

		serviceObserver = new GraphViewObserver(channelOneSeries);

		final Handler threadHandler = new Handler();
		final Runnable redrawAction = new Runnable() {
			@Override
			public void run() {
				if (serviceObserver.isHasChangedAndReset())
					graph.redrawAll();
			}
		};
		refresherThread = new Thread() {
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

	}

	public void setDataService(CardiovascularDataService service) {
		dataService.addObserver(this.serviceObserver);
		dataService.start();
	}

	@SuppressLint("InflateParams")
	// TODO (optional) cleanup
	private void letUserSetServer() {

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();
		final View view = inflater.inflate(
				R.layout.server_address_prompt_dialog, null);
		dialogBuilder.setView(view);
		dialogBuilder.setCancelable(true);

		dialogBuilder.setPositiveButton(getResources().getString(R.string.OK),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int id) {
						try {
							EditText serverAddressEdit = (EditText) view
									.findViewById(R.id.serverAddress);
							EditText serverPortEdit = (EditText) view
									.findViewById(R.id.serverPort);
							int port = -1;
							try {
								port = Integer.parseInt(serverPortEdit
										.getText().toString());
							} catch (NumberFormatException e) {
								// TODO
							}
							dataService = ServiceManager.getInstance(
									serverAddressEdit.getText().toString(),
									port).getCardiovascularDataService();
							setDataService(dataService);
						} catch (ServiceException e1) {
							Toast.makeText(
									MainActivity.this,
									"Sorry, failed to get data service. Closing.",
									Toast.LENGTH_LONG).show();
							MainActivity.this.finish();
						} catch (NumberFormatException e) {
							Toast.makeText(
									MainActivity.this,
									"Port does not seem to be a number. Closing.",
									Toast.LENGTH_LONG).show();
							MainActivity.this.finish();
						}
					}
				});

		dialogBuilder.setNegativeButton(
				getResources().getString(R.string.Cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int id) {
						dialogInterface.cancel();
					}
				});

		dialogBuilder.create().show();

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
		@SuppressLint("ClickableViewAccessibility")
		// TODO (low) check
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
