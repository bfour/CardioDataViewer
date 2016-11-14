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

package at.ac.tuwien.e0826357.cardioapp.service;

import android.content.Context;

import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;

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

	public CardiovascularDataService getCardiovascularDataService(Context context) throws ServiceException {
		// return SineTestDataService.getInstance();
		return new CardiovascularDataTCPReceiverService(context, serverURL, serverPort);
	}

}
