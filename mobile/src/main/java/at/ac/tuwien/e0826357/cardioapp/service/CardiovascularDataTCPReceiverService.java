package at.ac.tuwien.e0826357.cardioapp.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import at.ac.tuwien.e0826357.cardioApp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioApp.commons.service.CardiovascularDataMarshaller;
import at.ac.tuwien.e0826357.cardioApp.commons.service.ServiceException;

public class CardiovascularDataTCPReceiverService extends
		CardiovascularDataService {

	private static class Receiver implements Runnable {

		private CardiovascularDataTCPReceiverService serv;
		private int port;
		private String serverAddress;

		public Receiver(CardiovascularDataTCPReceiverService serv,
				String serverAddress, int port) {
			this.serv = serv;
			this.serverAddress = serverAddress;
			this.port = port;
		}

		@Override
		public void run() {
			String line = "";
			Socket socket = null;
			try {
				socket = new Socket(serverAddress, port);
				BufferedReader inboundStream = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				while ((line = inboundStream.readLine()) != null) {
					serv.receive(CardiovascularDataMarshaller.unmarshal(line));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (socket != null)
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}

	}

	private Thread thread;
	private String serverAddress;
	private int port;

	public CardiovascularDataTCPReceiverService(String serverAddress, int port)
			throws ServiceException {
		this.serverAddress = serverAddress;
		this.port = port;
	}

	@Override
	public void start() {
		thread = new Thread(new Receiver(this, serverAddress, port));
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
