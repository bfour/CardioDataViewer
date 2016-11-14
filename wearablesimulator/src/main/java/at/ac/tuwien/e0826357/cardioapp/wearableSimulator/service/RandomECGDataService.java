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

package at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service;

import java.util.Random;

public class RandomECGDataService {

	private static final int MIN_ECG = 0;
	private static final int MAX_ECG = 1024;
	private static final int BASE_ECG = 486;
	private static final int PEAK_ECG = 861;
	private static final int LOW_ECG = 18;

	private Random noiseRandomizer;
	private Random noiseSignumRandomizer;
	private Random oxygenSaturationRandomizer;
	private long lastPeak = 0;
	private boolean lastWasPeak = false;

	public RandomECGDataService() {
		this.noiseRandomizer = new Random();
		this.noiseSignumRandomizer = new Random();
		this.oxygenSaturationRandomizer = new Random();
	}

	public int getECG(long timeDiffInMSec) {
		int signal = emulateHumanSignal(timeDiffInMSec);
		int noise = noiseRandomizer.nextInt(11);
		if (noiseSignumRandomizer.nextBoolean()) {
			signal = signal + noise;
		} else {
			signal = signal - noise;
		}
		if (signal < MIN_ECG)
			return MIN_ECG;
		if (signal > MAX_ECG)
			return MAX_ECG;
		return signal;
	}

	public int getOxy(long timeDiffInMSec) {
		return oxygenSaturationRandomizer.nextInt(100);
	}

	private int emulateHumanSignal(long timeDiffInMSec) {
		long diff = timeDiffInMSec - lastPeak;
		// System.err.println("diff " + diff);
		if (lastWasPeak) {
			lastWasPeak = false;
			return LOW_ECG;
		}
		// every 1000ms +- 18ms, send a peak
		if (diff > 1000) {
			lastPeak = timeDiffInMSec;
			lastWasPeak = true;
			return PEAK_ECG;
		}
		return BASE_ECG;
	}

}
