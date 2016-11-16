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

package at.ac.tuwien.e0826357.cardioapp.service;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.GenericGetter;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.Triple;

public class GraphViewObserver {

    private static final int VIEWPORT_WIDTH = 18161;

    private List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> graphsAndSeries;
    private boolean hasChanged;
    private long currentXAnchor;

    public GraphViewObserver(
            List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> graphsAndSeries) {
        this.graphsAndSeries = graphsAndSeries;
        hasChanged = false;
        currentXAnchor = 0;
    }

    public void update(CardiovascularData data) {
        long x = data.getTime();
        for (Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>> triple : graphsAndSeries) {
            triple.getB().appendData(
                    new DataPoint(new Date(x), triple.getC().get(data)),
                    true, VIEWPORT_WIDTH * 2);
            triple.getA().getViewport().setMinX(x);
            triple.getA().getViewport().setMaxX(x+1000);
        }
        if (x > currentXAnchor + VIEWPORT_WIDTH) {
            if (x > currentXAnchor + VIEWPORT_WIDTH * 2) {
                // way behind with setting viewpoint -> set to last x
                currentXAnchor = x + VIEWPORT_WIDTH;
            } else {
                // make one VIEWPORT_WIDTH-step
                currentXAnchor = currentXAnchor + VIEWPORT_WIDTH;
            }
            // for (Triple<GraphView, LineGraphSeries<DataPoint>,
            // GenericGetter<CardiovascularData, Double>> triple :
            // graphsAndSeries)
            // triple.getA().setViewPort(currentXAnchor, VIEWPORT_WIDTH);
        }
    }

    public synchronized boolean isHasChangedAndReset() {
        boolean hasChangedCur = hasChanged;
        hasChanged = false;
        return hasChangedCur;
    }

}
