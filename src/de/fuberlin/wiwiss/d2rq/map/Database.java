package de.fuberlin.wiwiss.d2rq.map;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

import de.fuberlin.wiwiss.d2rq.D2RQException;
import de.fuberlin.wiwiss.d2rq.sql.ConnectedDB;
import de.fuberlin.wiwiss.d2rq.vocab.D2RQ;


/**
 * Representation of a d2rq:Database from the mapping file.
 *
 * @author Chris Bizer chris@bizer.de
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @version $Id: Database.java,v 1.14 2006/09/13 14:06:23 cyganiak Exp $
 */
public class Database extends MapObject {

	/**
	 * Pre-registers a JDBC driver if its class can be found on the
	 * classpath. If the class is not found, nothing will happen.
	 * @param driverClassName Fully qualified class name of a JDBC driver
	 */
	public static void registerJDBCDriverIfPresent(String driverClassName) {
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException ex) {
			// not present, just ignore this driver
		}
	}

	/**
	 * Registers a JDBC driver class.
	 * @param driverClassName Fully qualified class name of a JDBC driver
	 * @throws D2RQException If the class could not be found
	 */
	public static void registerJDBCDriver(String driverClassName) {
		try {
			Class.forName(driverClassName);
		} catch (ClassNotFoundException ex) {
			throw new D2RQException("Database driver class not found: " + driverClassName,
					D2RQException.DATABASE_JDBCDRIVER_CLASS_NOT_FOUND);
		}
	}
	
	/**
	 * Tries to guess the class name of a suitable JDBC driver from a JDBC URL.
	 * This only works in the unlikely case that the driver has been registered
	 * earlier using Class.forName(classname).
	 * @param jdbcURL A JDBC URL
	 * @return The corresponding JDBC driver class name, or <tt>null</tt> if not known
	 */
	public static String guessJDBCDriverClass(String jdbcURL) {
		try {
			return DriverManager.getDriver(jdbcURL).getClass().getName();
		} catch (SQLException ex) {
			return null;
		}
	}
		
	private String odbcDSN;
	private String jdbcDSN;
	private String jdbcDriver;
	private String username;
	private String password;
	private Set textColumns = new HashSet();
	private Set numericColumns = new HashSet();
	private Set dateColumns = new HashSet();
    private String expressionTranslator = null;		// class name
	private boolean allowDistinct = true;
	private ConnectedDB connection = null;
	
	public Database(Resource resource) {
		super(resource);
	}

	public void setODBCDSN(String odbcDSN) {
		assertNotYetDefined(this.odbcDSN, D2RQ.odbcDSN, 
				D2RQException.DATABASE_DUPLICATE_ODBCDSN);
		this.odbcDSN = odbcDSN;
	}

	public void setJDBCDSN(String jdbcDSN) {
		assertNotYetDefined(this.jdbcDSN, D2RQ.jdbcDSN,
				D2RQException.DATABASE_DUPLICATE_JDBCDSN);
		this.jdbcDSN = jdbcDSN;
	}

	public void setJDBCDriver(String jdbcDriver) {
		assertNotYetDefined(this.jdbcDriver, D2RQ.jdbcDriver,
				D2RQException.DATABASE_DUPLICATE_JDBCDRIVER);
		this.jdbcDriver = jdbcDriver;
	}

	public void setUsername(String username) {
		assertNotYetDefined(this.username, D2RQ.username,
				D2RQException.DATABASE_DUPLICATE_USERNAME);
		this.username = username;
	}

	public void setPassword(String password) {
		assertNotYetDefined(this.password, D2RQ.password,
				D2RQException.DATABASE_DUPLICATE_PASSWORD);
		this.password = password;
	}

	public void addTextColumn(String column) {
		this.textColumns.add(column);
	}
	
	public void addNumericColumn(String column) {
		this.numericColumns.add(column);
	}
	
	public void addDateColumn(String column) {
		this.dateColumns.add(column);
	}
	
	public void setExpressionTranslator(String expressionTranslator) {
		this.expressionTranslator = expressionTranslator;
	}
	
	public void setAllowDistinct(boolean b) {
		this.allowDistinct = b;
	}

	public ConnectedDB connectedDB() {
		if (this.connection != null) {
			return this.connection;
		}
		String url;
		String driver = null;
		if (this.odbcDSN != null) {
			driver = "sun.jdbc.odbc.JdbcOdbcDriver";
			url = "jdbc:odbc:" + this.odbcDSN;
		} else {
			driver = this.jdbcDriver;
			url = this.jdbcDSN;
		}
		if (driver != null) {
			registerJDBCDriver(driver);
		}
		this.connection = new ConnectedDB(url, this.username, this.password,
				this.expressionTranslator, this.allowDistinct,
				this.textColumns, this.numericColumns, this.dateColumns);
		return this.connection;
	}

	public String toString() {
		return "d2rq:Database " + super.toString();
	}

	public void validate() throws D2RQException {
		if (this.jdbcDSN != null && this.odbcDSN != null) {
			throw new D2RQException("Can't combine d2rq:odbcDSN with d2rq:jdbcDSN",
					D2RQException.DATABASE_ODBC_WITH_JDBC);
		}
		if (this.jdbcDSN != null && this.jdbcDriver == null) {
			throw new D2RQException("Missing d2rq:jdbcDriver",
					D2RQException.DATABASE_MISSING_JDBCDRIVER);
		}
		if (this.odbcDSN != null && this.jdbcDriver != null) {
			throw new D2RQException("Can't use d2rq:jdbcDriver with jdbc:odbcDSN",
					D2RQException.DATABASE_ODBC_WITH_JDBCDRIVER);
		}
		// TODO
	}
}