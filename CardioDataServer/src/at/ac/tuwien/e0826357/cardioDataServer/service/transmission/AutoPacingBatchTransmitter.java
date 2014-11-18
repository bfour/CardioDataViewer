package at.ac.tuwien.e0826357.cardioDataServer.service.transmission;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioDataServer.service.data.DataLayerException;
import at.ac.tuwien.e0826357.cardioDataServer.service.data.DataService;
import at.ac.tuwien.e0826357.cardioDataServer.service.sending.SenderService;

class AutoPacingBatchTransmitter<T> implements Runnable {

	private static final int MIN_THREAD_PAUSE_INMSEC = 18;
	private static final int MAX_THREAD_PAUSE_INMSEC = 1861;
	private static final int AUTO_PACE_STEP_IN_MS = 16;

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
				senderServ.batchTell(data);
			} catch (DataLayerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				pause = calculateNextPause(pause, data.size());
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