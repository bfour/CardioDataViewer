/*
 * Copyright 2016 Florian Pollak (fpdevelop@gmail.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.ac.tuwien.e0826357.cardioapp.server.service.data;

import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;

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
