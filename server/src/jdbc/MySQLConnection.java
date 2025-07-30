package jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * The class handles any SQL query needed
 */
public class MySQLConnection {
	private Connection con;

	/**
	 * Class Constructor
	 */
	protected MySQLConnection() {
		createDatabaseAndTable();
		con = connectToDB();
	}

	/**
	 * Getter of con
	 * @return con
	 */
	public Connection getCon() {
		return con ;
	}

	/**
	 * Start a connection to DB bpark returns con
	 * @return con
	 */
	private Connection connectToDB() {	
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeed");
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("Driver definition failed");
		}
		try {
			Connection conn = DriverManager
					.getConnection("jdbc:mysql://localhost/bpark?serverTimezone=Asia/Jerusalem&useSSL=false", "root", "Aa123456");
			System.out.println("DB connection succeed");
			return conn;
		} catch (Exception ex) {
			/* handle the error */
			System.out.println("connection failed");
			return null;
		}

	}

	/**
	 * Connect to the MySQL server (without specifying a database)
	 * Happens at Server Startup
	 * @return Connection object
	 */
	private Connection connectToMySQL() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			System.out.println("Driver definition succeeded");
		} catch (Exception ex) {
			System.out.println("Driver definition failed");
			return null;
		}
		try {
			// Connecting to MySQL without specifying a database
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost?serverTimezone=Asia/Jerusalem&useSSL=false",
					"root", "Aa123456");
			System.out.println("DB connection succeeded");
			return conn;
		} catch (Exception ex) {
			System.out.println("Connection failed");
			return null;
		}
	}

	/**
	 * @param con
	 */
	private void disconnectFromDB(Connection con) {
		// Closes the connection
		try {
			if (con != null && !con.isClosed()) {
				con.close(); // Close the connection after the operation is complete
				System.out.println("Connection closed after retrieving orders.");
			}
		} catch (SQLException e) {
			System.out.println("Failed to close the connection.");
			e.printStackTrace();
		}
	}

	/**
	 * Create the database if it doesn't exist, and import the SQL file if needed
	 */
	private void createDatabaseAndTable() {
		try {
			// Step 1: Connect to MySQL (without specifying a database)
			con = connectToMySQL();
			if (con == null) {
				throw new SQLException();
			}

			// Step 2: Create the database if it doesn't exist
			String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS bpark";
			Statement stmt = con.createStatement();
			stmt.executeUpdate(createDatabaseSQL);
			System.out.println("Database created or already exists.");

			// Step 3: Reconnect to the bpark database
			stmt.close();
			disconnectFromDB(con);
			con = connectToDB();
			if (con == null) {
				System.out.println("Failed to reconnect to bpark database.");
				return;
			}

			// Step 4: Select the bpark database explicitly
			Statement useStmt = con.createStatement();
			String chooseBpark = "USE bpark;";
			useStmt.execute(chooseBpark);
			System.out.println("Using bpark database.");

			// Step 5: Check if the table exists
			String showOrder = "SHOW TABLES LIKE 'subscribers'";
			ResultSet rs = useStmt.executeQuery(showOrder);
			if (!rs.next()) {
				System.out.println("Table 'subscribers' does not exist. Proceeding with import.");
				// If table does not exist, import the SQL file
				importSQLFile();
			} else {
				System.out.println("Table 'subscribers' already exists. Skipping import.");
			}

			rs.close();
			useStmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Failed to create database or table.");
		} finally {
			disconnectFromDB(con);
		}
	}

	/**
	 * Import SQL file to create table and insert data
	 */
	private void importSQLFile() {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("DB.sql");
			if (is == null) {
				throw new Exception("SQL file not found in classpath.");
			}
			Scanner scanner = new Scanner(is, "UTF-8").useDelimiter(";");
			Statement stmt = con.createStatement();
			while (scanner.hasNext()) {
				String line = scanner.next().trim();
				if (!line.isEmpty()) {
					stmt.execute(line);
				}
			}
			scanner.close();
			stmt.close();
			System.out.println("SQL file imported successfully.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to import SQL file.");
		}
	}
}
