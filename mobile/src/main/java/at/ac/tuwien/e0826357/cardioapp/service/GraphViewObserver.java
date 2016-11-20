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

    private static final int BUFFER_BUCKETS = 20000;
    private static final int BUCKET_SIZE = 10;

    private final Handler handler;

    private List<List<CardiovascularData>> bufferBuckets = new ArrayList<>(BUFFER_BUCKETS);
    private int lastBucketPopped = -1;
    private List<CardiovascularData> emptyList = new ArrayList<>(0);

    public GraphViewObserver(
            final List<Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>>> graphsAndSeries) {

        handler = new Handler();
        Runnable runner = new Runnable() {

            // TODO optimize timing

            private long lastTime = -1;
            private int cycleCounter = 0;
            private long diffDiffMs = 0;

            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                for (CardiovascularData row : pop()) {
                    cycleCounter++;
                    long time = row.getTime();
                    if (lastTime == -1)
                        lastTime = time;
                    for (Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>> triple : graphsAndSeries)
                        triple.getB().appendData(new DataPoint(time, triple.getC().get(row)), true, 2000, cycleCounter != 1);
                    // every 10 cycles determine wait (draw in real time: one second in graph drawn in one second)
                    if (cycleCounter == 10) {
                        cycleCounter = 0;
                        long diffInGraph = time - lastTime;
                        long now = System.currentTimeMillis();
                        long diffInReality = now - startTime;
                        diffDiffMs = (diffInGraph - diffInReality) + (diffDiffMs < 0 ? diffDiffMs : 0);
                        if (diffDiffMs > 0)
                            try {
                                Thread.sleep(diffDiffMs);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                        startTime = System.currentTimeMillis();
                        lastTime = time;
                    }
                }
                handler.postDelayed(this, 10);
            }
        };
        handler.postDelayed(runner, 1000);
    }

    public synchronized void update(final CardiovascularData data) {
        if (bufferBuckets.size() == 0)
            bufferBuckets.add(new ArrayList<CardiovascularData>(BUCKET_SIZE));
        List<CardiovascularData> lastBucket = bufferBuckets.get(bufferBuckets.size()-1);
        if (lastBucket.size() == BUCKET_SIZE) {
            List<CardiovascularData> newBucket = new ArrayList<>(BUCKET_SIZE);
            newBucket.add(data);
            bufferBuckets.add(newBucket);
        } else {
            lastBucket.add(data);
        }
    }

    public synchronized List<CardiovascularData> pop() {
        if (bufferBuckets.size() == 0)
            return emptyList;
        List<CardiovascularData> retCopy = bufferBuckets.get(0);
        bufferBuckets.remove(0);
        return retCopy;
    }

}
