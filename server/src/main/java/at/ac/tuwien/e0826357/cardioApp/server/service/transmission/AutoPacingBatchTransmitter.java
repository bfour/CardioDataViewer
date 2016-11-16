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

package at.ac.tuwien.e0826357.cardioapp.server.service.transmission;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.server.service.data.DataLayerException;
import at.ac.tuwien.e0826357.cardioapp.server.service.data.DataService;
import at.ac.tuwien.e0826357.cardioapp.server.service.sending.SenderService;

class AutoPacingBatchTransmitter<T> implements Runnable {

	private static final int MIN_THREAD_PAUSE_INMSEC = 18;
	private static final int MAX_THREAD_PAUSE_INMSEC = 486;
	private static final int AUTO_PACE_STEP_IN_MS = 46;

	private DataService<T> dataServ;
	private SenderService<T> senderServ;
	private int batchTargetSize;

	public AutoPacingBatchTransmitter(DataService<T> dataServ,
									  SenderService<T> senderServ, int batchTargetSize) {
		this.dataServ = dataServ;
		this.senderServ = senderServ;
		this.batchTargetSize = batchTargetSize;
	}

	@Override
	public void run() {
		List<T> data = new ArrayList<>(0);
		int pause = MIN_THREAD_PAUSE_INMSEC; // start with fastest pace
		while (true) {
			try {
				data = dataServ.getNext();
//				System.out
//						.println("retrieved " + data.size() + " data records");
				senderServ.batchTell(data);
//				System.out.println("transmitted data records");
			} catch (DataLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pause = calculateNextPause(pause, data.size());
//				System.out.println("auto-pause at " + pause);
				Thread.sleep(pause);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private int calculateNextPause(int currentPause, int lastBatchSize) {
		int newPause = currentPause;
		if (lastBatchSize > batchTargetSize) {
			newPause -= AUTO_PACE_STEP_IN_MS;
		} else {
			newPause += AUTO_PACE_STEP_IN_MS;
		}
		if (newPause < MIN_THREAD_PAUSE_INMSEC)
			return MIN_THREAD_PAUSE_INMSEC;
		if (newPause > MAX_THREAD_PAUSE_INMSEC)
			return MAX_THREAD_PAUSE_INMSEC;
		return newPause;
	}

}