package at.ac.tuwien.e0826357.cardioDataViewer.service;

import com.jjoe64.graphview.GraphView.GraphViewData;

public class SinusTestDataService extends CardiovascularDataService {

	class SinusGenerator implements Runnable {

		private static final double ROUND_LENGTH = 2 * Math.PI;

		private int timeSeriesLength = 681;
		private int secondsPerRound = 4;
		private long pauseInMSec = 18;

		private boolean allowedToContinue = false;
		private SinusTestDataService service;

		public SinusGenerator(SinusTestDataService service) {
			this.service = service;
		}

		public void stop() {
			allowedToContinue = false;
		}

		@Override
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
						// calc value
						// note: velocity is defined as ROUND_LENGTH and
						// secondsPerRound are defined
						// --> we have to calculate length for the time passed
						// since last round
						// --> v = s/t -> s = v*t
						double velocityInRPS = ROUND_LENGTH / secondsPerRound;
						double deltaLength = (velocityInRPS * elapsedMSec)/1000;
						double newValue = previousValue + deltaLength;
						if (newValue > ROUND_LENGTH) {
							int times = (int) Math.floor(newValue/ROUND_LENGTH);
							newValue -= ROUND_LENGTH * (times);
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
		(new Thread(generator)).start();
	}

	@Override
	public void stop() {
		generator.stop();
	}

	private void receive(Long time, Double value) {
		setChanged();
//		notifyObservers(new GraphViewData((double) time, value));
		notifyObservers(new GraphViewData(x++, value));
	}

}
