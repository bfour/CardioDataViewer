package at.ac.tuwien.e0826357.cardioapp.domain;

import java.util.Map;

public class TimeSeriesSegment<V> {

	private Map<Long, V> valueMap;
	
	public TimeSeriesSegment(int numberOfEntries) {
		valueMap = new PreAllocatedImmutableEntriesMap<Long, V>(numberOfEntries);
	}
	
	public void addEntry(Long time, V value) {
		valueMap.put(time, value);
	}

	public Map<Long, V> getValueMap() {
		return valueMap;
	}

}
