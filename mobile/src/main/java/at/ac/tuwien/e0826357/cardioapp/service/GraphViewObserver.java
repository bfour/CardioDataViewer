package at.ac.tuwien.e0826357.cardioapp.service;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.GenericGetter;
import at.ac.tuwien.e0826357.cardioapp.commons.domain.Triple;

public class GraphViewObserver implements Observer {

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

    @Override
    public void update(Observable obs, Object obj) {
        // TODO
        if (obj instanceof CardiovascularData) {
            update(obs, (CardiovascularData) obj);
        }
        hasChanged = true;
    }

    // public void update(Observable obs, ECTimeSeriesSegment segment) {
    // TODO
    // }
    private void update(Observable obs, CardiovascularData data) {
        long x = data.getTime();
        for (Triple<GraphView, LineGraphSeries<DataPoint>, GenericGetter<CardiovascularData, Double>> triple : graphsAndSeries) {
            triple.getB().appendData(
                    new DataPoint(new Date(x), triple.getC().get(data)),
                    true, VIEWPORT_WIDTH * 2);
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
