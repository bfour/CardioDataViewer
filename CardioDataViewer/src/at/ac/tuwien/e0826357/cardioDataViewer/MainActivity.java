package at.ac.tuwien.e0826357.cardioDataViewer;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import at.ac.tuwien.e0826357.cardioDataViewer.domain.ECTimeSeriesSegment;
import at.ac.tuwien.e0826357.cardioDataViewer.service.CardiovascularDataService;
import at.ac.tuwien.e0826357.cardioDataViewer.service.ServiceManager;
import at.ac.tuwien.e0826357.cardioDataViewer.util.SystemUiHider;
import at.ac.tuwien.e0826357.sinustest.R;

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

	private CardiovascularDataService dataService;
	private GraphView graph;

	private static class GraphViewObserver implements Observer {

		private GraphViewSeries series;

		public GraphViewObserver(GraphViewSeries series) {
			this.series = series;
		}

		@Override
		public void update(Observable obs, Object obj) {
			// TODO
			if (obj instanceof GraphViewDataInterface) {
				update(obs, (GraphViewDataInterface) obj);
			}
		}

		public void update(Observable obs, ECTimeSeriesSegment segment) {
			// TODO
		}

		public void update(Observable obs, GraphViewDataInterface data) {
			series.appendData(data, false, 1861);
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
		graph.setViewPort(0, 186);
		graph.setScrollable(true);
		graph.setScalable(true);
		graph.setShowLegend(true);
		graph.setLegendAlign(LegendAlign.BOTTOM);
		
		graph.setManualMaxY(true);
		graph.setManualMinY(true);
		graph.setManualYMaxBound(1.0);
		graph.setManualYMinBound(-1.0);

		graphLayout.addView(graph);

		// logic
		GraphViewSeries channelOneSeries = new GraphViewSeries(
				"Channel 1 Signal", new GraphViewSeriesStyle(Color.BLACK, 2),
				new GraphViewData[] { new GraphViewData(0, 0) });
		this.graph.addSeries(channelOneSeries);

		Observer serviceObserver = new GraphViewObserver(channelOneSeries);
		dataService = ServiceManager.getInstance()
				.getCardiovascularDataService();
		dataService.addObserver(serviceObserver);
		dataService.start();

	}

	@Override
	public void onResume() {
		// TODO
		super.onResume();
		// dataService.start();
	}

	@Override
	public void onPause() {
		// TODO
		super.onPause();
		// dataService.stop();
	}

}
