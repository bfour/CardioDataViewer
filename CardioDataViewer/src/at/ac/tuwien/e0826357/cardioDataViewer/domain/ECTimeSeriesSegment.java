package at.ac.tuwien.e0826357.cardioDataViewer.domain;

import java.util.Map;


public class ECTimeSeriesSegment {

	private Map<Long, Double> valueMap;
		
	public ECTimeSeriesSegment(int numberOfEntries) {
		valueMap = new PreAllocatedImmutableEntriesMap<Long, Double>(numberOfEntries);
	}
	
	public void addEntry(Long time, Double value) {
		valueMap.put(time, value);
	}

	public Map<Long, Double> getValueMap() {
		return valueMap;
	}
	
}
