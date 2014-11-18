package at.ac.tuwien.e0826357.cardioDataServer.service.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

	Connection conn;
	
	public Database(String className, String url) throws DataLayerException {
		try {
			Class.forName(className);
			conn = DriverManager.getConnection("jdbc:sqlite:test.db");
		} catch (ClassNotFoundException | SQLException e) {
			throw new DataLayerException("failed to initialize database connection", e);
		}

	}

	public Connection getConn() {
		return conn;
	}

}