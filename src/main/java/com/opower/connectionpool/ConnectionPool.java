package com.opower.connectionpool;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Run J-Unit Tests to confirm connection to hsqldb is working as intended.
 */
public interface ConnectionPool {

    /**
     * Gets a connection from the connection pool.
     * 
     * @return a valid connection from the pool.
     */
    Connection getConnection() throws SQLException;

    /**
     * Releases a connection back into the connection pool.
     * 
     * @param connection the connection to return to the pool
     * @throws java.sql.SQLException
     */
    void releaseConnection(Connection connection) throws SQLException;
    
	/**
	 * Method to calculate number of available connections in the pool.
	 * @return number of available connections.
	 */
	public int getNumberOfAvailableConnections();

	/**
	 * Method to calculate number of busy connections in the pool.
	 * @return number of busy connections.
	 */
	public int getNumberOfBusyConnections();

	/**
	 * Method to close all the connections.
	 */
	public void closeAllConnections();

    
}
