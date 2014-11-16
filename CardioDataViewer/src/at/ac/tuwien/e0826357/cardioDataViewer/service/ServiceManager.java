package at.ac.tuwien.e0826357.cardioDataViewer.service;

public class ServiceManager {

	private static ServiceManager instance;

	private ServiceManager() {
	}

	public static ServiceManager getInstance() {
		if (instance == null)
			instance = new ServiceManager();
		return instance;
	}

	public CardiovascularDataService getCardiovascularDataService() {
//		 return CardiovascularDataService.getInstance();
		 return SinusTestDataService.getInstance();
	}

//	public SinusTestDataService getSinusTestDataService() {
//	}

}
