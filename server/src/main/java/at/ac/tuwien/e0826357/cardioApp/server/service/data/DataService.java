package at.ac.tuwien.e0826357.cardioApp.server.service.data;

import java.util.List;

public interface DataService<T> {

	/**
	 * Gets the next available items that chronologically follow the ones
	 * returned by the last call of this method.
	 * 
	 * @return a chronologically sorted list of T, an empty list if no such data
	 *         is available
	 * @throws DataLayerException
	 */
	List<T> getNext() throws DataLayerException;

}
