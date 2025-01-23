package akhil.DataUnlimited.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import akhil.DataUnlimited.model.types.Types;

public class SQLite {
	private static final Logger lo = LogManager.getLogger(SQLite.class.getName());

	public static void closeConnection(Connection con, Statement pstmt, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
		}
		try {
			if (pstmt != null)
				pstmt.close();
		} catch (Exception e) {
		}
		try {
			if (con != null)
				con.close();
		} catch (Exception e) {
		}
	}

	public static Connection connect(String dbConnectionString) {
		try {
			//Class.forName(Types.DBCLASS);
			Connection conn = null;
			if (Types.DBCLASS.contains("sqlite"))
				conn = DriverManager.getConnection(dbConnectionString);
			else
				conn = DriverManager.getConnection(dbConnectionString, Types.DBUSR, Types.DBPWD);
			return conn;
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, LogStackTrace.get(e));
			} else
				lo.error(LogStackTrace.get(e));

			return null;
		} catch (Exception e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, LogStackTrace.get(e));
			} else
				lo.error(LogStackTrace.get(e));
			return null;
		}
	}

	public static synchronized Integer ddlQuery(String dbUrl, String query) {
		Connection con = null;
		Statement pstmt = null;
		Integer result = null;
		try {
			con = connect(dbUrl);
			assert con != null;
			pstmt = con.createStatement();
			result = pstmt.executeUpdate(query);
			if (Types.getInstance().getIsUI()) {
				DULogger.log(400, "INFO: SQL Lite result...." + result);
			} else
				lo.info("INFO: SQL Lite result...." + result);
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} catch (Exception e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} finally {
			closeConnection(con, pstmt, null);
		}
		return result;
	}

	public static synchronized Integer dmlQuery(String dbUrl, String query, String... values) {
		Connection con = null;
		PreparedStatement pstmt = null;
		Integer result = null;
		try {
			con = connect(dbUrl);
			assert con != null;
			pstmt = con.prepareStatement(query);
			int index = 1;
			for (String s : values) {
				pstmt.setString(index, s);
				index++;
			}
			result = pstmt.executeUpdate();
			if (Types.getInstance().getIsUI()) {
				DULogger.log(400, "INFO: SQL Lite result...." + result);
			} else
				lo.info("INFO: SQL Lite result...." + result);
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} catch (Exception e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} finally {
			closeConnection(con, pstmt, null);
		}
		return result;
	}

	public static List<List<String>> selectQuery(String dbUrl, String query, String... values) {
		List<List<String>> result = new ArrayList<>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = connect(dbUrl);
			assert con != null;
			pstmt = con.prepareStatement(query);
			int index = 1;
			for (String s : values) {
				pstmt.setString(index, s);
				index++;
			}
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int hmp = rsmd.getColumnCount();
			while (rs.next()) {
				List<String> row = new ArrayList<>();
				for (int i = 1; i <= hmp; i++) {
					row.add(rs.getString(i));
				}
				result.add(row);
			}
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} catch (Exception e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} finally {
			closeConnection(con, pstmt, rs);
		}
		return result;
	}

	public static List<List<String>> selectQuery(String dbUrl, String query) {
		List<List<String>> result = new ArrayList<>();
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = connect(dbUrl);
			assert con != null;
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int hmp = rsmd.getColumnCount();
			while (rs.next()) {
				List<String> row = new ArrayList<>();
				for (int i = 1; i <= hmp; i++) {
					row.add(rs.getString(i));
				}
				result.add(row);
			}
		} catch (SQLException e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} catch (Exception e) {
			if (Types.getInstance().getIsUI()) {
				DULogger.log(200, query + "\n" + LogStackTrace.get(e));
			} else
				lo.error(query + "\n" + LogStackTrace.get(e));
		} finally {
			closeConnection(con, pstmt, rs);
		}
		return result;
	}

	public static void driverDeregister() {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, LogStackTrace.get(e));
				} else
					lo.error(LogStackTrace.get(e));
			} catch (Exception e) {
				if (Types.getInstance().getIsUI()) {
					DULogger.log(200, LogStackTrace.get(e));
				} else
					lo.error(LogStackTrace.get(e));
			}
		}
	}
}
