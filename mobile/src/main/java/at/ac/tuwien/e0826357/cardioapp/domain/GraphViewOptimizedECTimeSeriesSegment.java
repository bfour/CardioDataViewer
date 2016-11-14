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

import com.jjoe64.graphview.series.DataPoint;

import java.util.Map;

public class GraphViewOptimizedECTimeSeriesSegment extends TimeSeriesSegment<Double> {

    private int currentIndex;
    private DataPoint[] gvData;

    public GraphViewOptimizedECTimeSeriesSegment(int numberOfEntries) {
        super(0);
        currentIndex = 0;
        gvData = new DataPoint[numberOfEntries];
    }

    /**
     * @throws IndexOutOfBoundsException if trying to add more entries than initially specified
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
