package akhil.DataUnlimited.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class DBUtil {

	private static final Logger lo = LogManager.getLogger(DBUtil.class.getName());
	private static DBUtil dbu;

	private DBUtil() {
	}

	public static DBUtil getInstance() {
		if (dbu == null) {
			dbu = new DBUtil();
		}
		return dbu;
	}

	private Connection getConnection(String connectionString, String user, String passwd) {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(connectionString, user, passwd);
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, "ERROR: Connection Failed! Check output console.\n" + connectionString + "\n"
						+ LogStackTrace.get(e));
			else
				lo.error("ERROR: Connection Failed! Check output console.\n" + connectionString + "\n"
						+ LogStackTrace.get(e));

		}

		if (connection != null) {
			return connection;
		} else {
			return null;
		}
	}

	public List<String> executeSQL(Connection c, String sql) {
		List<String> toReturn = new ArrayList<>();
		try (Statement stmt = c.createStatement(); ResultSet rs = stmt.executeQuery(sql);) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int hmp = rsmd.getColumnCount();
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i <= hmp; i++) {
				sb.append(rsmd.getColumnName(i));
				if (i != hmp)
					sb.append("<DMSDELIM>");
			}
			toReturn.add(sb.toString());
			while (rs.next()) {
				StringBuilder row = new StringBuilder();
				for (int i = 1; i <= hmp; i++) {
					row.append(rs.getString(i));
					if (i != hmp)
						row.append("<DMSDELIM>");
				}
				if (Types.getInstance().getIsUI())
					DULogger.log(500, "DEBUG: DB Query Result: " + row);
				else
					lo.debug("DEBUG: DB Query Result: " + row);

				toReturn.add(row.toString());
			}
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI())
				DULogger.log(200, "ERROR: SQL Exception in running query...\n" + sql + "\n" + LogStackTrace.get(e));
			else
				lo.error("ERROR: SQL Exception in running query...\n" + sql + "\n" + LogStackTrace.get(e));

		}
		return toReturn;
	}

	public List<String> executeSQL(String connectionString, String user, String passwd, String sql) {
		Connection c = getConnection(connectionString, user, passwd);
		return executeSQL(c, sql);
	}
}