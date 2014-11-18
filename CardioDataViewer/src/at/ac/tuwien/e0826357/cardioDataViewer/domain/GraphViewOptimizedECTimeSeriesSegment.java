package at.ac.tuwien.e0826357.cardioDataViewer.domain;

import java.util.Map;

import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class GraphViewOptimizedECTimeSeriesSegment extends TimeSeriesSegment<Double> {

	private int currentIndex;
	private GraphViewData[] gvData;

	public GraphViewOptimizedECTimeSeriesSegment(int numberOfEntries) {
		super(0);
		currentIndex = 0;
		gvData = new GraphViewData[numberOfEntries];
	}

	/**
	 * @throws IndexOutOfBoundsException
	 *             if trying to add more entries than initially specified
	 */
	@Override
	public void addEntry(Long time, Double value) {
		gvData[currentIndex] = new GraphViewData(time, value);
		currentIndex++;
	}

	public GraphViewSeries getGraphViewSeries() {
		return new GraphViewSeries(gvData);
	}

	@Override
	public Map<Long, Double> getValueMap() {
		Map<Long, Double> map = new PreAllocatedImmutableEntriesMap<Long, Double>(gvData.length);
		for (GraphViewData gvDataEntry : gvData) {
			map.put((long) gvDataEntry.getX(), gvDataEntry.getY());
		}
		return map;
	}

}
