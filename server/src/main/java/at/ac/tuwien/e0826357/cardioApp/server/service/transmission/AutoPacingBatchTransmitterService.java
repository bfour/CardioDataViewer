package at.ac.tuwien.e0826357.cardioapp.server.service.transmission;

import at.ac.tuwien.e0826357.cardioapp.server.service.data.DataService;
import at.ac.tuwien.e0826357.cardioapp.server.service.sending.SenderService;

public class AutoPacingBatchTransmitterService<T> extends TransmitterService<T> {

	private int batchTargetSize;

	public AutoPacingBatchTransmitterService(DataService<T> dataServ,
											 SenderService<T> senderServ, int batchTargetSize) {
		super(dataServ, senderServ);
		this.batchTargetSize = batchTargetSize;
	}

	@Override
	public Runnable getTransmitter() {
		return new AutoPacingBatchTransmitter<>(getDataServ(),
				getSenderServ(), batchTargetSize);
	}

}
