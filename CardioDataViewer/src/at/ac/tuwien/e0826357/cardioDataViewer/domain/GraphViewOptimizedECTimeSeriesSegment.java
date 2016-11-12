package at.ac.tuwien.e0826357.cardioDataViewer.domain;

import java.util.Map;

import com.jjoe64.graphview.series.DataPoint;

public class GraphViewOptimizedECTimeSeriesSegment extends TimeSeriesSegment<Double> {

	private int currentIndex;
	private DataPoint[] gvData;

	public GraphViewOptimizedECTimeSeriesSegment(int numberOfEntries) {
		super(0);
		currentIndex = 0;
		gvData = new DataPoint[numberOfEntries];
	}

	/**
	 * @throws IndexOutOfBoundsException
	 *             if trying to add more entries than initially specified
	 */
	@Override
	public void addEntry(Long time, Double value) {
		gvData[currentIndex] = new DataPoint(time, value);
		currentIndex++;
	}

	@Override
	public Map<Long, Double> getValueMap() {
		Map<Long, Double> map = new PreAllocatedImmutableEntriesMap<Long, Double>(gvData.length);
		for (DataPoint gvDataEntry : gvData) {
			map.put((long) gvDataEntry.getX(), gvDataEntry.getY());
		}
		return map;
	}

}
