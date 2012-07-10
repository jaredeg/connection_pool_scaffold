package com.opower.connectionpool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class ConnectionPoolImpl implements Runnable, ConnectionPool {

	private String driver, url, username, password;
	private int maxConnections;
	private List<Connection> availableConnections, busyConnections;
	private boolean connectionPending = false;

	public ConnectionPoolImpl(String driver, String url, String username,
			String password, int initialConnections, int maximumConnections) throws SQLException {
		
		//Checks to confirm input is valid
		checkConnetionConstructor(driver, url, username, password, initialConnections, maximumConnections);
		
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.maxConnections = maximumConnections;
	
		// Thread Safe Connections
		availableConnections =  Collections.synchronizedList(new ArrayList<Connection>(initialConnections));
		busyConnections = Collections.synchronizedList(new ArrayList<Connection>());
		
		//Construct a list of new Connections
		for (int i = 0; i < initialConnections; i++){
			availableConnections.add(makeNewConnection());
		}

	}
	
	
		// Error checking for the Constructor 
	private void checkConnetionConstructor(String driver, String url, String username,
			String password, int initialConnections, int maximumConnections){

		if (driver == null){
			throw new IllegalArgumentException("Null driver was inputed.");
		}
		if (url == null){
			throw new IllegalArgumentException("Null url was given");
		}
		if (username == null ){
			throw new IllegalArgumentException("Null username given");
		}
		if (password == null){
			throw new IllegalArgumentException("Null passowrd given. The password must have a value, even if empty.");
		}
		
		if (initialConnections <= 0){
			throw new IllegalArgumentException("initialConnections can not be negative.");
		}

		if (maximumConnections < 1){
			throw new IllegalArgumentException("maximumConnections must be at least 1 connection.");
		}
		
		if (initialConnections > maximumConnections){
			throw new IllegalArgumentException("initialConnections can not be greater then maximumConnections.");
		}
		
	}

	public synchronized Connection getConnection() throws SQLException {
		//Checks first if their are more connections available.
		if (availableConnections.isEmpty()){
			
			//Before making another First check if maxConnections has been reached,
			//and if not start a new connection
			if (((availableConnections.size() + busyConnections.size()) < maxConnections) && !connectionPending){
				connectionPending = true;
				try {
					Thread connectThread = new Thread(this);
					connectThread.start();
				} catch (Exception e){
				}
			} 
			// Wait for either a new connection to be established
			// or for an existing connection to be free.
			try {
				wait();
			} catch (Exception e){
			}
			//Waiting until a connection is freed up, this code will run once it is free
			return (getConnection());
		} else {
			
			int lastIndex = availableConnections.size() - 1;
			Connection existingConnection = (Connection) availableConnections.get(lastIndex);
			availableConnections.remove(lastIndex);
			
			
			if (existingConnection.isClosed()){
				notifyAll(); 
				return (getConnection());
			} else {
				busyConnections.add(existingConnection);
				return (existingConnection);
			}
		}
	}

	public void run(){
		try {
			Connection connection = makeNewConnection();
			synchronized (this){
				availableConnections.add(connection);
				connectionPending = false;
				notifyAll();
			}
		} catch (Exception e){ 
		}
	}

	// Makes a new connection.  Either in the foreground on background depending on the call.
	private Connection makeNewConnection() throws SQLException {
		try{
			// Load database driver if not already loaded
			Class.forName(driver);
			// Establish network connection to database
			Connection connection = DriverManager.getConnection(url, username, password);
			return (connection);
		} catch (Exception e){
			throw new SQLException("Can't find class for driver: " + driver);
		}
	}

	public synchronized void releaseConnection(Connection connection) throws SQLException{
		busyConnections.remove(connection);
		availableConnections.add(connection);
		// Wake up to all the connections that are waiting
		notifyAll();
	}

	// Closes all the connections. we need to make sure that no connections are
	// in use before calling.  
	public synchronized void closeAllConnections(){
		closeConnections(availableConnections);
		availableConnections = Collections.synchronizedList(new ArrayList<Connection>());
		closeConnections(busyConnections);
		busyConnections = Collections.synchronizedList(new ArrayList<Connection>());
	}

	
	//Easy remove of closing all the connections
	private void closeConnections(List<Connection> connections) {
		try {
			for (Connection connection : connections) {
				if (!connection.isClosed()) {
					connection.close();
				}
			}
		} catch (SQLException sqle) {
		}
	}
	
	
	// For Testing
	public synchronized int getNumberOfAvailableConnections(){
		return availableConnections.size();
	}
	// For Testing
	public synchronized int getNumberOfBusyConnections(){
		return busyConnections.size();
	}

}

