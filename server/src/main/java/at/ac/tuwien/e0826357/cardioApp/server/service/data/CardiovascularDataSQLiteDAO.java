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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioapp.commons.domain.CardiovascularData;

public class CardiovascularDataSQLiteDAO implements CardiovascularDataDAO {

	private Connection conn;

	private PreparedStatement getAllAfterPrepStatement;

	public CardiovascularDataSQLiteDAO(Database db) {
		this.conn = db.getConn();
		try {
			this.conn
					.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<CardiovascularData> getAllAfter(long time)
			throws DataLayerException {

		// lazy init prepared statement
		if (getAllAfterPrepStatement == null) {
			try {
				getAllAfterPrepStatement = conn
						.prepareStatement("SELECT ID, ECGA, ECGB, ECGC, oxygenSaturationPerMille "
								+ "FROM data WHERE ID > ?;");
			} catch (SQLException e) {
				throw new DataLayerException("failed to prepare statement", e);
			}
		}

		// get
		try {
			getAllAfterPrepStatement.setLong(1, time);
			ResultSet result = getAllAfterPrepStatement.executeQuery();
			// check if there's at least one entry, so we can check for
			// numOfRows, which is used for allocating exactly the amount of
			// space in the ArrayList that is needed
			// boolean hasAtLeastOne = result.next();
			// if (!hasAtLeastOne)
			// return new ArrayList<>(0);
			// int numOfRows = result.getInt("numOfRows");
			List<CardiovascularData> data = new ArrayList<>();
			while (result.next()) {
				long ID = result.getLong(1);
				System.out.println(ID);
				int ECGA = result.getInt(2);
				int ECGB = result.getInt(3);
				int ECGC = result.getInt(4);
				int oxygenSaturationPerMille = result.getInt(5);
				data.add(new CardiovascularData(ID, ECGA, ECGB, ECGC,
						oxygenSaturationPerMille));
			}
			return data;
		} catch (SQLException e) {
			throw new DataLayerException("failed to get data", e);
		}

	}
}
