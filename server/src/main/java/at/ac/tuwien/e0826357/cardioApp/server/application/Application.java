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

package at.ac.tuwien.e0826357.cardioapp.server.application;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioapp.commons.service.ServiceException;
import at.ac.tuwien.e0826357.cardioapp.server.service.data.CardiovascularDataSQLiteDAO;
import at.ac.tuwien.e0826357.cardioapp.server.service.data.CardiovascularDataService;
import at.ac.tuwien.e0826357.cardioapp.server.service.data.DataLayerException;
import at.ac.tuwien.e0826357.cardioapp.server.service.data.DataService;
import at.ac.tuwien.e0826357.cardioapp.server.service.data.Database;
import at.ac.tuwien.e0826357.cardioapp.server.service.sending.CardiovascularDataTCPSenderService;
import at.ac.tuwien.e0826357.cardioapp.server.service.sending.SenderService;
import at.ac.tuwien.e0826357.cardioapp.server.service.transmission.AutoPacingBatchTransmitterService;
import at.ac.tuwien.e0826357.cardioapp.server.service.transmission.TransmitterService;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.err
					.println("invalid parameters; required: <executable> <databasePath> <port>");
			return;
		}

		try {
			String dbPath = args[0];
			// System.out.println("path: "+dbPath);
			int port = Integer.parseInt(args[1]);
			Database db = new Database("org.sqlite.JDBC", "jdbc:sqlite:"
					+ dbPath);
			DataService<CardiovascularData> dataService = CardiovascularDataService
					.getInstance(new CardiovascularDataSQLiteDAO(db));
			SenderService<CardiovascularData> senderService = new CardiovascularDataTCPSenderService(
					port);
			TransmitterService<CardiovascularData> transmitterService = new AutoPacingBatchTransmitterService<CardiovascularData>(
					dataService, senderService, 1681);
			transmitterService.start();
		} catch (ServiceException | DataLayerException | NumberFormatException e) {
			System.err
					.println("sorry, failed to initialize: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
