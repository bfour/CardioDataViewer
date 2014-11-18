package at.ac.tuwien.e0826357.cardioDataServer.service.transmission;


import at.ac.tuwien.e0826357.cardioDataServer.service.data.DataService;
import at.ac.tuwien.e0826357.cardioDataServer.service.sending.SenderService;

public abstract class TransmitterService<T> {

	private Thread thread;
	private DataService<T> dataServ;
	private SenderService<T> senderServ;

	public TransmitterService(DataService<T> dataServ,
			SenderService<T> senderServ) {
		this.dataServ = dataServ;
		this.senderServ = senderServ;
	}

	protected DataService<T> getDataServ() {
		return dataServ;
	}

	protected SenderService<T> getSenderServ() {
		return senderServ;
	}

	public void start() {
		thread = new Thread();
		thread.start();
	}

	public void stop() {
		thread.interrupt();
	}
	
	public abstract Runnable getTransmitter();

}
