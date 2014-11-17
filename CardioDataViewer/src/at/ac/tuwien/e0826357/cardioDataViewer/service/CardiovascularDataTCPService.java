package at.ac.tuwien.e0826357.cardioDataViewer.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

public class CardiovascularDataTCPService extends CardiovascularDataService {

	private Socket socket;
	private Reader inboundStream;
	private Writer outboundStream;

	public CardiovascularDataTCPService(String serverAddress, int port)
			throws ServiceException {
		try {
			socket = new Socket(serverAddress, port);
			outboundStream = new PrintWriter(socket.getOutputStream(), true);
			inboundStream = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}
