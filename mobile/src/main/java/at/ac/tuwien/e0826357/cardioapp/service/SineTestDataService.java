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

import com.jjoe64.graphview.series.DataPoint;

public class SineTestDataService extends CardiovascularDataService {

	class SineGenerator implements Runnable {

		private static final double ROUND_LENGTH = 2 * Math.PI;

		private int timeSeriesLength = 681;
		private int secondsPerRound = 4;
		private long pauseInMSec = 18;

		private boolean allowedToContinue = false;
		private SineTestDataService service;

		public SineGenerator(SineTestDataService service) {
			this.service = service;
		}

		public void stop() {
			allowedToContinue = false;
		}

        public void resume() {
            allowedToContinue = true;
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

	private static SineTestDataService instance;
	private SineGenerator generator;
	private int x = 1;
	private boolean isRunning;

	private SineTestDataService() {
		isRunning = false;
	}

	public static SineTestDataService getInstance() {
		if (instance == null)
			instance = new SineTestDataService();
		return instance;
	}

	@Override
	public void start() {
		generator = new SineGenerator(this);
		(new Thread(generator)).start();
		isRunning = true;
	}

	@Override
	public void stop() {
        generator.stop();
		isRunning = false;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	private void receive(Long time, Double value) {
		setChanged();
//		notifyObservers(new GraphViewData((double) time, value));
		notifyObservers(new DataPoint(x++, value));
	}

}
