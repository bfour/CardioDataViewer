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

package at.ac.tuwien.e0826357.cardioapp.wearableSimulator.application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator.EcgCalc;
import at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator.EcgLogWindow;
import at.ac.tuwien.e0826357.cardioapp.wearableSimulator.service.MITECGGenerator.EcgParam;

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
			//connection
			//		.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

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

			EcgParam params = new EcgParam();
			params.setN(861); // num of heartbeats
            EcgLogWindow logWindow = new EcgLogWindow();
            logWindow.setVisible(true);
            long startTime = System.currentTimeMillis();
			while (true) {
				EcgCalc calc1 = new EcgCalc(params, logWindow);
				EcgCalc calc2 = new EcgCalc(params, logWindow);
				EcgCalc calc3 = new EcgCalc(params, logWindow);
				if (!calc1.calculateEcg() || !calc2.calculateEcg() || !calc3.calculateEcg())
					throw new RuntimeException("calculator failed");
				for (int i = 0; i < calc1.getEcgResultNumRows(); i++) {
					insertStatement.setLong(1, startTime + (long) calc1.getEcgResultTimeSec(i)/1000);
					insertStatement.setInt(2, (int) calc1.getEcgResultVoltage(i));
					insertStatement.setInt(3, (int) calc2.getEcgResultVoltage(i));
					insertStatement.setInt(4, (int) calc3.getEcgResultVoltage(i));
					insertStatement.setInt(5, 0);
				}
			}

		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}

	}
}
