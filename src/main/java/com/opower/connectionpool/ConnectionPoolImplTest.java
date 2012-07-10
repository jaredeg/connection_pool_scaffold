package com.opower.connectionpool;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

//First round of tests Test to make sure input is working as intended. That is that if
//a constructor is called incorrectly the correct exception is thrown.
//Second round of tests does some simple generating of connections and then releasing them.

public class ConnectionPoolImplTest {


	private static final String DRIVER = "org.hsqldb.jdbcDriver";
	private static final String URL = "jdbc:hsqldb:mem:testdb";
	private static final String USER = "sa";
	private static final String PASSWORD = "";
	private static final int MAX_CONNECTIONS = 9;
	private static final int INITIAL_CONNECTIONS = 2;
	private static final int BAD_CONNECTION = -1;

	private ConnectionPool pool = null;

	@Before
	public void setUp() throws SQLException {
		pool = new ConnectionPoolImpl(DRIVER, URL, USER, PASSWORD,
				INITIAL_CONNECTIONS, MAX_CONNECTIONS);
	}

	@After
	public void tearDown() {
		pool = null;
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullDriver() throws SQLException {
		pool = new ConnectionPoolImpl(null, URL, USER, PASSWORD,
				INITIAL_CONNECTIONS, MAX_CONNECTIONS);
	}

	@Test(expected = SQLException.class)
	public void testDriverNotFound() throws SQLException {
		pool = new ConnectionPoolImpl("some.funky.driver", URL,	USER, PASSWORD,
				INITIAL_CONNECTIONS, MAX_CONNECTIONS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullUrl() throws SQLException {
		pool = new ConnectionPoolImpl(DRIVER, null, USER, PASSWORD,
				INITIAL_CONNECTIONS, MAX_CONNECTIONS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullUserName() throws SQLException {
		pool = new ConnectionPoolImpl(DRIVER, URL, null, PASSWORD,
				INITIAL_CONNECTIONS, MAX_CONNECTIONS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullPassword() throws SQLException {
		pool = new ConnectionPoolImpl(DRIVER, URL, USER, null,
				INITIAL_CONNECTIONS, MAX_CONNECTIONS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidMaxConnections() throws SQLException {
		pool = new ConnectionPoolImpl(DRIVER, URL, USER, PASSWORD,
				INITIAL_CONNECTIONS, BAD_CONNECTION);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidInitialonnections() throws SQLException {
		pool = new ConnectionPoolImpl(DRIVER, URL, USER, PASSWORD,
				BAD_CONNECTION, MAX_CONNECTIONS);
	}

	@Test
	public void testPoolInitialization() throws SQLException {
		assertTrue("unexpected size of pool",
				INITIAL_CONNECTIONS == pool.getNumberOfAvailableConnections());
	}
//Second Round
	@Test
	public void testGetConnection() throws SQLException {
		pool.getConnection();
		assertTrue(pool.getNumberOfAvailableConnections() == 1);
		assertTrue(pool.getNumberOfBusyConnections() == 1);
	}

	@Test
	public void testReleaseConnection() throws SQLException {
		Connection conn = pool.getConnection();
		assertTrue(pool.getNumberOfBusyConnections() == 1);
		pool.releaseConnection(conn);
		assertTrue(pool.getNumberOfBusyConnections() == 0);
	}

	@Test
	public void testCloseAllConnections() throws SQLException {
		pool.getConnection();
		assertTrue(pool.getNumberOfBusyConnections() == 1);
		assertTrue(pool.getNumberOfAvailableConnections() == 1);
		pool.closeAllConnections();
		assertTrue(pool.getNumberOfBusyConnections() == 0);
		assertTrue(pool.getNumberOfAvailableConnections() == 0);
	}
}
