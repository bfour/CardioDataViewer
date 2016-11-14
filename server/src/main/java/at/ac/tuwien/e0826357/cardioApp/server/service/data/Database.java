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
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

	Connection conn;
	
	public Database(String className, String url) throws DataLayerException {
		try {
			Class.forName(className);
			conn = DriverManager.getConnection(url);
		} catch (ClassNotFoundException | SQLException e) {
			throw new DataLayerException("failed to initialize database connection", e);
		}

	}

	public Connection getConn() {
		return conn;
	}

}
