package at.ac.tuwien.e0826357.cardioDataServer.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import at.ac.tuwien.e0826357.cardioDataViewer.service.ServiceException;

public class CommunicationService {

	private static CommunicationService instance;
	private ServerSocket serverSocket;
	
	private CommunicationService(int port) throws ServiceException {
		try {
			this.serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			throw new ServiceException(e);
		}
	}
	
	public static CommunicationService getInstance(int port) throws ServiceException {
		if (instance == null) instance = new CommunicationService(port);
		return instance;
	}

	public void start() {
		try {
			Socket socket = serverSocket.accept();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stop() {
		
	}
	
	public void tell(String message) {
		
	}
	
}
