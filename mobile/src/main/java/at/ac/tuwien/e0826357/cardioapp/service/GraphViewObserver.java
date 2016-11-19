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

import android.os.Handler;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.GenericGetter;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.Triple;

public class GraphViewObserver {

    private static final int VIEWPORT_WIDTH = 18161;
    private final Handler handler;

    private List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> graphsAndSeries;
    private boolean hasChanged;
    private long currentXAnchor;

    private long lastX = 0;

    private List<DataPoint> buffer = new ArrayList<>(2000);

    public GraphViewObserver(
            final List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> graphsAndSeries) {
        this.graphsAndSeries = graphsAndSeries;
        hasChanged = false;
        currentXAnchor = 0;
        handler = new Handler();
        Runnable runner2 = new Runnable() {
            @Override
            public void run() {
                for (DataPoint datapoint : pop()) {
                    graphsAndSeries.get(1).getB().appendData(datapoint, true, 2000);
//                    triple.getA().getViewport().setMinX(x - lastX);
//                    triple.getA().getViewport().setMaxX((x - lastX) + 1000);
//                triple.getA().setScrollX(100);
                }
                handler.postDelayed(this, 200);
            }
        };
        handler.postDelayed(runner2, 1000);
    }

    public synchronized void update(final CardiovascularData data) {
        long x = data.getTime();
        if (lastX == 0) lastX = x;
        buffer.add(new DataPoint(x - lastX, data.getLeadII()));
//        final long x = data.getTime();
//        if (lastX == 0) lastX = x;
//        int count = 1;
//        for (final Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>> triple : graphsAndSeries) {
//            if (count == 2)
//                handler.postDelayed(, 10);
//            if (count == 2) break;
//            count++;
//        }
    }

    public synchronized List<DataPoint> pop() {
        List<DataPoint> retCopy = new ArrayList<>(buffer);
        buffer.clear();
        return retCopy;
    }

}
