package at.ac.tuwien.e0826357.cardioDataViewer.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import at.ac.tuwien.e0826357.cardioDataCommons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioDataCommons.service.CardiovascularDataMarshaller;
import at.ac.tuwien.e0826357.cardioDataCommons.service.ServiceException;

public class CardiovascularDataTCPReceiverService extends
		CardiovascularDataService {

	private static class Receiver implements Runnable {

		private CardiovascularDataTCPReceiverService serv;
		private BufferedReader inboundStream;

		public Receiver(CardiovascularDataTCPReceiverService serv,
				BufferedReader inboundStream) {
			this.serv = serv;
			this.inboundStream = inboundStream;
		}

		@Override
		public void run() {
			String line = "";
			try {
				while ((line = inboundStream.readLine()) != null) {
					serv.receive(CardiovascularDataMarshaller.unmarshal(line));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private Socket socket;
	private BufferedReader inboundStream;
	private Thread thread;

	public CardiovascularDataTCPReceiverService(String serverAddress, int port)
			throws ServiceException {
		try {
			socket = new Socket(serverAddress, port);
			inboundStream = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void start() {
		thread = new Thread(new Receiver(this, inboundStream));
		thread.start();
	}

	@Override
	public void stop() {
		thread.interrupt();
	}

	private void receive(CardiovascularData data) {
		setChanged();
		notifyObservers(data);
	}

}
