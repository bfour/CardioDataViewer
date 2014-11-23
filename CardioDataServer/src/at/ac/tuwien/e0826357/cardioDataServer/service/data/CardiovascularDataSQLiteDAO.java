package at.ac.tuwien.e0826357.cardioDataServer.service.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.e0826357.cardioDataCommons.domain.CardiovascularData;

public class CardiovascularDataSQLiteDAO implements CardiovascularDataDAO {

	private Connection conn;

	private PreparedStatement getAllAfterPrepStatement;

	public CardiovascularDataSQLiteDAO(Database db) {
		this.conn = db.getConn();
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
