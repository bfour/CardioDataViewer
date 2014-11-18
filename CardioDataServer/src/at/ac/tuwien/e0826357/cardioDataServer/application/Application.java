package at.ac.tuwien.e0826357.cardioDataServer.application;

import at.ac.tuwien.e0826357.cardioDataServer.service.sending.TCPSenderService;
import at.ac.tuwien.e0826357.cardioDataViewer.service.ServiceException;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			TCPSenderService commServ = new TCPSenderService(1861);
		} catch (ServiceException e) {
			System.err.println("sorry, failed to initialize TCP communication service: "+e.getMessage());
		}
		
	}

}
