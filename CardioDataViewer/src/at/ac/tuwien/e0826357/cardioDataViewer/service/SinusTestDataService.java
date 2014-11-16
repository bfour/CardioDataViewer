package at.ac.tuwien.e0826357.cardioDataViewer.service;

import at.ac.tuwien.e0826357.cardioDataViewer.domain.ECTimeSeriesSegment;
import at.ac.tuwien.e0826357.cardioDataViewer.domain.GraphViewOptimizedECTimeSeriesSegment;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class SinusTestDataService extends CardiovascularDataService {

	class SinusGenerator implements Runnable {

		private static final double ROUND_LENGTH = 2 * Math.PI;

		private int timeSeriesLength = 681;
		private int secondsPerRound = 1;
		private long pauseInMSec = 10;

		private ECTimeSeriesSegment segment;
		private boolean allowedToContinue = false;
		private SinusTestDataService service;

		public SinusGenerator(SinusTestDataService service) {
			this.service = service;
		}

		public void stop() {
			allowedToContinue = false;
		}

		// @Override
		public void run() {
			try {
				allowedToContinue = true;
				while (allowedToContinue) {
					long time = System.currentTimeMillis();
					long previousTime = time;
					double previousValue = 0;
					for (int count = 1; count <= timeSeriesLength; count++) {
						Thread.sleep(pauseInMSec);
						// measure time
						time = System.currentTimeMillis();
						long elapsedMSec = time - previousTime;
						double elapsedSec = elapsedMSec / 1000;
						// calc value
						// note: velocity is defined as ROUND_LENGTH and
						// secondsPerRound are defined
						// --> we have to calculate length for the time passed
						// since last round
						// --> v = s/t -> s = v*t
						double velocityInRPS = ROUND_LENGTH / secondsPerRound;
						double deltaLength = velocityInRPS * elapsedSec;
						double newValue = previousValue + deltaLength;
						if (newValue > ROUND_LENGTH) {
							newValue -= ROUND_LENGTH;
						}
						// save
						service.receive(time, Math.sin(newValue));
						// remember values for next round
						previousValue = newValue;
						previousTime = time;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private static SinusTestDataService instance;
	private SinusGenerator generator;
	private int x = 1;
	private int y = 1;

	private SinusTestDataService() {
	}

	public static SinusTestDataService getInstance() {
		if (instance == null)
			instance = new SinusTestDataService();
		return instance;
	}

	@Override
	public void start() {
		generator = new SinusGenerator(this);
		generator.run();
	}

	@Override
	public void stop() {
		generator.stop();
	}

	private void receive(Long time, Double value) {
		setChanged();
//		notifyObservers(new GraphViewData((double) (time/1000), Math.round(value)));
		notifyObservers(new GraphViewData(x++, y++));
	}

}
