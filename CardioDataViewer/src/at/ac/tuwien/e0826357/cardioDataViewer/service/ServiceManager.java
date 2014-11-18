package at.ac.tuwien.e0826357.cardioDataViewer.service;

import at.ac.tuwien.e0826357.cardioDataCommons.service.ServiceException;

public class ServiceManager {

	private static ServiceManager instance;

	private ServiceManager() {
	}

	public static ServiceManager getInstance() {
		if (instance == null)
			instance = new ServiceManager();
		return instance;
	}

	public CardiovascularDataService getCardiovascularDataService() throws ServiceException {
		// return SineTestDataService.getInstance();
		return new CardiovascularDataTCPReceiverService("10.0.2.2", 1861);
	}

}
