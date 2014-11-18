package at.ac.tuwien.e0826357.cardioDataServer.service.data;

import java.util.List;

import at.ac.tuwien.e0826357.cardioDataCommons.domain.CardiovascularData;

public class CardiovascularDataService implements
		DataService<CardiovascularData> {

	private static CardiovascularDataService instance;
	private CardiovascularDataDAO cardioDAO;
	private long lastID = -1;

	private CardiovascularDataService(CardiovascularDataDAO cardioDAO) {
		this.cardioDAO = cardioDAO;
	}

	public static CardiovascularDataService getInstance(
			CardiovascularDataDAO cardioDAO) {
		if (instance == null)
			instance = new CardiovascularDataService(cardioDAO);
		return instance;
	}

	/**
	 * Gets the next available items that chronologically follow the ones
	 * returned by the last call of this method.
	 * 
	 * @return a chronologically sorted list of CardiovascularData, an empty
	 *         list if no such data is available
	 * @throws DataLayerException
	 */
	@Override
	public synchronized List<CardiovascularData> getNext()
			throws DataLayerException {
		List<CardiovascularData> list = cardioDAO.getAllAfter(lastID);
		if (list.size() > 0) {
			CardiovascularData lastEntry = list.get(list.size() - 1);
			lastID = lastEntry.getTime();
		}
		return list;
	}

}
