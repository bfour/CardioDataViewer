package at.ac.tuwien.e0826357.cardioDataCreater.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import at.ac.tuwien.e0826357.cardioDataCommons.domain.CardiovascularData;
import at.ac.tuwien.e0826357.cardioDataCreater.service.RandomECGDataService;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			// prepare
			Class.forName("org.sqlite.JDBC");
			Connection connection = DriverManager
					.getConnection("jdbc:sqlite:test.db");

			Statement dropStatement = connection.createStatement();
			String dropDataTableSQL = "DROP TABLE IF EXISTS data;";
			dropStatement.executeUpdate(dropDataTableSQL);
			dropStatement.close();

			Statement createStatement = connection.createStatement();
			String createDataTableSQL = "CREATE TABLE IF NOT EXISTS data "
					+ "(ID LONG PRIMARY KEY NOT NULL, "
					+ "ECGA INTEGER NOT NULL, " + "ECGB INTEGER NOT NULL, "
					+ "ECGC INTEGER NOT NULL, "
					+ "oxygenSaturationPerMille INTEGER NOT NULL);";
			createStatement.executeUpdate(createDataTableSQL);
			createStatement.close();

			// insert data
			RandomECGDataService dataServA = new RandomECGDataService();
			RandomECGDataService dataServB = new RandomECGDataService();
			RandomECGDataService dataServC = new RandomECGDataService();
			RandomECGDataService dataServOxy = new RandomECGDataService();
			PreparedStatement insertStatement = connection
					.prepareStatement("INSERT INTO data (ID,ECGA,ECGB,ECGC,oxygenSaturationPerMille) "
							+ "VALUES (?,?,?,?,?)");

			long startTime = System.currentTimeMillis();
			while (true) {

				long timeDiff = System.currentTimeMillis() - startTime;
				CardiovascularData data = new CardiovascularData(
						System.currentTimeMillis(), dataServA.getECG(timeDiff),
						dataServB.getECG(timeDiff), dataServC.getECG(timeDiff),
						dataServOxy.getOxy(timeDiff));

				// System.out.println(data);

				insertStatement.setLong(1, data.getTime());
				insertStatement.setInt(2, data.getECGA());
				insertStatement.setInt(3, data.getECGB());
				insertStatement.setInt(4, data.getECGC());
				insertStatement.setInt(5, data.getOxygenSaturationPerMille());

				insertStatement.execute();
				Thread.sleep(1);

			}

		} catch (SQLException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
			return;
		}

	}
}
