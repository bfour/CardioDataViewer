package at.ac.tuwien.e0826357.cardioDataViewer.service;

import at.ac.tuwien.e0826357.cardioDataCommons.service.ServiceException;

public class ServiceManager {

	private static ServiceManager instance;
	
	private String serverURL;
	private int serverPort;

	private ServiceManager(String serverURL, int serverPort) {
		this.serverURL = serverURL;
		this.serverPort = serverPort;
	}

	public static ServiceManager getInstance(String serverURL, int serverPort) {
		if (instance == null)
			instance = new ServiceManager(serverURL, serverPort);
		return instance;
	}

	public CardiovascularDataService getCardiovascularDataService() throws ServiceException {
		// return SineTestDataService.getInstance();
		return new CardiovascularDataTCPReceiverService(serverURL, serverPort);
	}

}
