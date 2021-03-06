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
