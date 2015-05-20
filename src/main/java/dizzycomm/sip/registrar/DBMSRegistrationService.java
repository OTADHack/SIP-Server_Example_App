package dizzycomm.sip.registrar;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An example registration implementation that stores it's data in an external
 * RDBMS database. This is in contrast to the LocalRegistrationService which
 * stores its data in local memory.
 * 
 * @author Copyright (c) 2007 by BEA Worldwide Education Services, All Rights
 *         Reserved.
 **/
final class DBMSRegistrationService implements RegistrationService {

	public static final String DATA_SOURCE_NAME = "DizzyCommDBDS";

	public static final boolean DEBUG = Boolean
			.getBoolean("dizzycomm.sip.registrar.debug");

	private static final String REG_BY_CALLID = "SELECT REGISTRATION FROM REGISTRY WHERE CALLID = ? ";
	private static final String DEL_BY_CALLID = "DELETE FROM REGISTRY WHERE CALLID = ? ";
	private static final String ALL_REGS = "SELECT REGISTRATION FROM REGISTRY";
	private static final String ADD_NEW_REG = "INSERT INTO REGISTRY (CALLID, REGISTRATION, USERNAME, HOST, PORT) "
			+ "VALUES (?, ?, ?, ?, ?)";
	private static final String GET_REG = "SELECT REGISTRATION FROM REGISTRY "
			+ "WHERE USERNAME = ? AND HOST = ? AND PORT = ?";
	private static final String UPDATE_REG = "UPDATE REGISTRY SET REGISTRATION = ? "
			+ "WHERE USERNAME = ? AND HOST = ? AND PORT = ?";
	private static final String CLEAR_REG = "DELETE FROM REGISTRY";
	private static final String SET_STAMP = "INSERT INTO REGSTAMP VALUES (?)";
	private static final String CHECK_STAMP = "SELECT * FROM REGSTAMP";
	private static final String CLEAR_STAMP = "DELETE FROM REGSTAMP";

	private DataSource dataSource = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see dizzycomm.sip.registrar.RegistrationService#init()
	 */
	public void init() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			System.out.println("Failed to get InitialContext");
			e.printStackTrace();
			throw new IllegalStateException();
		}
		try {
			dataSource = (DataSource) ctx.lookup(DATA_SOURCE_NAME);
		} catch (NamingException e) {
			System.out.println("Unable to find pool with name: "
					+ DATA_SOURCE_NAME);
			e.printStackTrace();
			throw new IllegalStateException();
		}
		if (dataSource == null) {
			System.out.println("Unable to find pool with name: "
					+ DATA_SOURCE_NAME);
			throw new IllegalStateException();
		} else {
			// Check to clear the Registry out if we re-initialized
			doTimeStamp();
		}
	}

	/**
 * 
 */
	private void doTimeStamp() {
		// Check the stamp
		if (!evalStamp()) {
			// Clear the stamp
			clearStamp();
			// Add a fresh Stamp
			long now = System.currentTimeMillis();
			setStamp(now);

			// New stamp was made, so clear the registry
			clearRegistry();
			System.out.println("%%% Dizzy S-CSCF - Registry Re-initialzed");
		} else {
			System.out
					.println("%%% Dizzy S-CSCF - No Registry Re-init needed.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dizzycomm.sip.registrar.RegistrationService#getByCallID(java.lang.String)
	 */
	public Registration getByCallID(String callID) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(REG_BY_CALLID);
			pstmt.setString(1, callID);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				byte[] attrBytes = rs.getBytes(1);
				return deSerialize(attrBytes);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dizzycomm.sip.registrar.RegistrationService#removeByCallID(java.lang.
	 * String)
	 */
	public Registration removeByCallID(String callID) {
		Registration old = getByCallID(callID);
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(DEL_BY_CALLID);
			pstmt.setString(1, callID);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
		return old;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dizzycomm.sip.registrar.RegistrationService#add(dizzycomm.sip.registrar
	 * .Registration)
	 */
	public void add(Registration registration) {
		SipURI uri = registration.getUri();
		String user = uri.getUser();
		if (user == null)
			user = "";
		String host = uri.getHost();
		int port = uri.getPort();

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(ADD_NEW_REG);
			pstmt.setString(1, registration.getCallID());
			byte[] bytes = serialize(registration);
			pstmt.setBinaryStream(2, new ByteArrayInputStream(bytes),
					bytes.length);
			pstmt.setString(3, user);
			pstmt.setString(4, host);
			pstmt.setInt(5, port);

			pstmt.executeUpdate();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
	}

	/**
	 * @param registration
	 */
	public void save(Registration registration) {
		SipURI uri = registration.getUri();
		String user = uri.getUser();
		if (user == null)
			user = "";
		String host = uri.getHost();
		int port = uri.getPort();

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(UPDATE_REG);
			byte[] bytes = serialize(registration);
			pstmt.setBinaryStream(1, new ByteArrayInputStream(bytes),
					bytes.length);
			pstmt.setString(2, user);
			pstmt.setString(3, host);
			pstmt.setInt(4, port);

			pstmt.executeUpdate();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dizzycomm.sip.registrar.RegistrationService#resolve(javax.servlet.sip
	 * .SipURI)
	 */
	public Address[] resolve(SipURI aor) {
		String user = aor.getUser();
		if (user == null)
			user = "";
		String host = aor.getHost();
		int port = aor.getPort();

		ArrayList<Address> l = new ArrayList<Address>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs;
		try {
			conn = getConnection();
			long now = System.currentTimeMillis();
			pstmt = conn.prepareStatement(GET_REG);
			pstmt.setString(1, user);
			pstmt.setString(2, host);
			pstmt.setInt(3, port);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				Registration r = deSerialize(rs.getBytes(1));
				if (r.getExpiration() < now) {
					removeByCallID(r.getCallID());
					continue;
				}
				for (Address a : r.getContacts()) {
					a.setExpires((int) (r.getExpiration() - now) / 1000);
					l.add(a);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
		return l.toArray(new Address[l.size()]);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dizzycomm.sip.registrar.RegistrationService#getAllRegistrations()
	 */
	public Map<SipURI, Registration> getAllRegistrations() {
		TreeMap<SipURI, Registration> byUri = new TreeMap<SipURI, Registration>(
				new Comparator<SipURI>() {
					public int compare(SipURI u1, SipURI u2) {
						int diff = u1.getHost().compareTo(u2.getHost());
						if (diff != 0)
							return diff;
						diff = u1.getUser().compareTo(u2.getUser());
						return diff != 0 ? diff : u1.getPort() - u2.getPort();
					}
				});
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(ALL_REGS);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				byte[] attrBytes = rs.getBytes(1);
				Registration registration = deSerialize(attrBytes);
				byUri.put(registration.getUri(), registration);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
		return byUri;
	}

	/**
 * 
 */
	public void clearRegistry() {

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(CLEAR_REG);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
	}

	/**
	 * @param now
	 */
	public void setStamp(long now) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(SET_STAMP);
			pstmt.setLong(1, now);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
	}

	/**
 * 
 */
	public void clearStamp() {

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(CLEAR_STAMP);

			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
	}

	/**
	 * @return
	 */
	public long getStamp() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long stamp = 0;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(CHECK_STAMP);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				stamp = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException ignore) {
					handleException();
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException ignore) {
					handleException();
				}
		}
		return stamp;
	}

	/**
	 * @return
	 */
	public boolean evalStamp() {
		boolean result = false;
		long now = System.currentTimeMillis();

		// Get the current stamp
		long stamp = getStamp();

		// Add 20 seconds to the stamp to compensate for possible startup time
		// of another
		// engine that might be running this code.
		stamp = stamp + 20000;

		System.out.println("%%% Dizzy S-CSCF - Registration System: \n Stamp:"
				+ stamp);
		System.out.println("Now: " + now);

		if (now <= stamp) {
			result = true;
			System.out.println("%%% Dizzy S-CSCF - Stamp less than Now.");
		}

		return result;

	}

	/**
	 * @param registration
	 * @return
	 * @throws IOException
	 */
	private byte[] serialize(Registration registration) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(registration);
		oos.flush();
		byte[] serBytes = baos.toByteArray();
		if (DEBUG)
			System.out.println("Serialized " + serBytes.length + " bytes");
		return serBytes;
	}

	/**
	 * @param serBytes
	 * @return
	 * @throws IOException
	 */
	private Registration deSerialize(byte[] serBytes) throws IOException {
		if (serBytes == null || serBytes.length < 1)
			return null;
		if (DEBUG)
			System.out.println("Deserializing " + serBytes.length + " bytes");
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(serBytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Registration) ois.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	/**
 * 
 */
	private void handleException() {
	}
}
