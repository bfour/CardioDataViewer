package at.ac.tuwien.e0826357.cardioDataCreater.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import at.ac.tuwien.e0826357.cardioDataCreater.service.MITECGGenerator.EcgCalc;
import at.ac.tuwien.e0826357.cardioDataCreater.service.MITECGGenerator.EcgParam;

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
			connection
					.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

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
			PreparedStatement insertStatement = connection
					.prepareStatement("INSERT INTO data (ID,ECGA,ECGB,ECGC,oxygenSaturationPerMille) "
							+ "VALUES (?,?,?,?,?)");

			EcgCalc calc = new EcgCalc(new EcgParam(), null);
			if (!calc.calculateEcg())
				throw new RuntimeException("calculator failed");
			for (int i = 0; i < calc.getEcgResultNumRows(); i++) {
				insertStatement.setLong(1, (long) calc.getEcgResultTime(i));
				insertStatement.setInt(2, (int) calc.getEcgResultVoltage(i));
				insertStatement.setInt(3, 0);
				insertStatement.setInt(4, 0);
				insertStatement.setInt(5, 0);
			}

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

	}
}
