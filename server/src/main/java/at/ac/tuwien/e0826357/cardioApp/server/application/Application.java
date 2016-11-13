package at.ac.tuwien.e0826357.cardioApp.server.application;

import at.ac.tuwien.e0826357.cardioApp.commons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioApp.commons.service.ServiceException;
import at.ac.tuwien.e0826357.cardioApp.server.service.data.CardiovascularDataSQLiteDAO;
import at.ac.tuwien.e0826357.cardioApp.server.service.data.CardiovascularDataService;
import at.ac.tuwien.e0826357.cardioApp.server.service.data.DataLayerException;
import at.ac.tuwien.e0826357.cardioApp.server.service.data.DataService;
import at.ac.tuwien.e0826357.cardioApp.server.service.data.Database;
import at.ac.tuwien.e0826357.cardioApp.server.service.sending.CardiovascularDataTCPSenderService;
import at.ac.tuwien.e0826357.cardioApp.server.service.sending.SenderService;
import at.ac.tuwien.e0826357.cardioApp.server.service.transmission.AutoPacingBatchTransmitterService;
import at.ac.tuwien.e0826357.cardioApp.server.service.transmission.TransmitterService;

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
