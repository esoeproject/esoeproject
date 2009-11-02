package com.qut.middleware.esoe.sessions.data.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.data.SessionCacheDAO;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheDAOException;
import com.qut.middleware.esoe.sessions.impl.PrincipalImpl;
import com.qut.middleware.esoe.util.CalendarUtils;

public class SessionCacheDAOImpl extends SqlMapClientDaoSupport implements SessionCacheDAO
{
	/*
	 * Implementation note:
	 * validSession is implemented as an update statement that sets the last accessed time.
	 * Any method needing to update the last accessed time can simply call this method.
	 */
	
	private static final String QUERY_GET_SESSION = "getSession";
	private static final String QUERY_GET_SESSION_BY_SAML_ID = "getSessionBySAMLID";
	private static final String QUERY_GET_ACTIVE_ENTITY_SESSIONS = "getActiveEntitySessions";
	private static final String QUERY_GET_SESSION_CACHE_SIZE = "getSessionCacheSize";
	private static final String QUERY_GET_IDLE_SESSIONS = "getIdleSessions";
	private static final String QUERY_INSERT_SESSION = "insertSession";
	private static final String QUERY_INSERT_DESCRIPTOR = "insertDescriptor";
	private static final String QUERY_INSERT_DESCRIPTOR_SESSION = "insertDescriptorSession";
	private static final String QUERY_UPDATE_SESSION_LAST_ACCESSED = "updateSessionLastAccessed";
	private static final String QUERY_UPDATE_SESSION_IDLE_GRACE_EXPIRY = "updateSessionIdleGraceExpiry";
	private static final String QUERY_UPDATE_SESSION_SAML_ID = "updateSessionSAMLID";
	private static final String QUERY_UPDATE_SESSION_ATTRIBUTES = "updateSessionAttributes";
	private static final String QUERY_DELETE_SESSION = "deleteSession";
	private static final String QUERY_DELETE_EXPIRED_SESSIONS = "deleteExpiredSessions";
	private static final String QUERY_DELETE_IDLE_SESSIONS = "deleteIdleSessions";
	private static final String QUERY_DELETE_ACTIVE_ENTITY_SESSIONS = "deleteActiveEntitySessions";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public void addSession(Principal principal) throws SessionCacheDAOException
	{
		SessionData sessionData = new SessionData();
		Map<String, IdentityAttribute> attributeMap = principal.getAttributes();

		byte[] attributeBlob = serializeAttributeMap(attributeMap);

		long sessionNotOnOrAfter =  principal.getSessionNotOnOrAfter();

		SimpleTimeZone utc = new SimpleTimeZone(0, ConfigurationConstants.timeZone);
		Calendar thisCalendar = new GregorianCalendar(utc);
		long currentTime = thisCalendar.getTimeInMillis();
		
		String sessionID = principal.getSessionID();
		
		if (sessionID == null)
		{
			// Early enforcement of "not null" constraint.
			this.logger.error("Refusing to insert session with null session ID.");
			throw new SessionCacheDAOException("Refusing to insert session with null session ID.");
		}
		if (currentTime >= sessionNotOnOrAfter)
		{
			// We don't want a session in the database that has already expired. Trap this condition.
			this.logger.error("Refusing to insert pre-expired session with ID " + sessionID + " .. Session not on or after value was " + sessionNotOnOrAfter + " (current time is " + currentTime + ")");
			throw new SessionCacheDAOException("Refusing to enter pre-expired session with ID " + sessionID + " .. Session not on or after value was " + sessionNotOnOrAfter + " (current time is " + currentTime + ")");
		}
		

		this.logger.debug("Session {} not on or after value is {}  ({}) .. current time {} ({}) .. authnID id {} .. Session is valid for {} ms", new Object[] { sessionID, sessionNotOnOrAfter, new Date(sessionNotOnOrAfter), currentTime, new Date(currentTime), principal.getPrincipalAuthnIdentifier(), (sessionNotOnOrAfter - currentTime) });

		// Populate SQL parameter object.
		sessionData.setSessionID(sessionID);
		sessionData.setSamlAuthnIdentifier(principal.getSAMLAuthnIdentifier());
		sessionData.setAuthenticationClassContext(principal.getAuthenticationContextClass());
		sessionData.setAuthnTimestamp(principal.getAuthnTimestamp());
		sessionData.setSessionNotOnOrAfter(sessionNotOnOrAfter);
		sessionData.setLastAccessed(principal.getLastAccessed());
		sessionData.setPrincipalAuthnIdentifier(principal.getPrincipalAuthnIdentifier());
		sessionData.setAttributes(attributeBlob);

		try
		{
			// Make call to insert session.
			this.getSqlMapClient().insert(SessionCacheDAOImpl.QUERY_INSERT_SESSION, sessionData);
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to insert principal session. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_INSERT_SESSION + ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to insert principal session due to underlying data source failure.");
		}
	}

	private byte[] serializeAttributeMap(Map<String, IdentityAttribute> attributeMap) throws SessionCacheDAOException
	{
		byte[] attributeBlob = null;
		try
		{
			// Serialize the attribute map into a byte array so we can store it as a BLOB
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutputStream outputStream = new ObjectOutputStream(stream);
			outputStream.writeObject(attributeMap);
			outputStream.close();
			stream.close();
			attributeBlob = stream.toByteArray();
		}
		catch (IOException e)
		{
			throw new SessionCacheDAOException("Unable to write attribute map to byte array. Serialization failed due to I/O error. Exception: " + e.getLocalizedMessage(), e);
		}
		return attributeBlob;
	}

	public int deleteIdleSessions() throws SessionCacheDAOException
	{
		try
		{
			GregorianCalendar thisCal = CalendarUtils.generateXMLCalendar().toGregorianCalendar();
			
			SessionData sessionParam = new SessionData();
			sessionParam.setIdleGraceExpiry(System.currentTimeMillis());
								
			this.logger.debug(MessageFormat.format("Deleting all sessions with idle grace expiry less than {0} ..." , sessionParam.getIdleGraceExpiry()) );
			int idleRemoved = this.getSqlMapClient().delete(SessionCacheDAOImpl.QUERY_DELETE_IDLE_SESSIONS, sessionParam);
									
			long duration = System.currentTimeMillis() - thisCal.getTimeInMillis();
			
			this.logger.debug(MessageFormat.format("Session cleanup query completed in {0} milliseconds. {1} sessions removed. ", duration ,  idleRemoved) );
		
			return idleRemoved;
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to delete Idle Sessions. SQL error occurred trying to execute {}." + SessionCacheDAOImpl.QUERY_DELETE_IDLE_SESSIONS);
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to delete Idle sessions due to underlying data source failure.");
		}
	}

	public int deleteExpiredSessions() throws SessionCacheDAOException
	{
		try
		{
			GregorianCalendar thisCal = CalendarUtils.generateXMLCalendar().toGregorianCalendar();;
			
			SessionData sessionParam = new SessionData();
			sessionParam.setSessionNotOnOrAfter(System.currentTimeMillis());
		
			this.logger.debug(MessageFormat.format("Deleting all sessions with notOnOrAfter less than {0}  .. " , sessionParam.getSessionNotOnOrAfter()) );
			int expiredRemoved = this.getSqlMapClient().delete(SessionCacheDAOImpl.QUERY_DELETE_EXPIRED_SESSIONS, sessionParam);
										
			long duration = System.currentTimeMillis() - thisCal.getTimeInMillis();
			
			this.logger.debug(MessageFormat.format("Session cleanup query completed in {0} milliseconds. {1} sessions removed. ", duration ,  expiredRemoved) );
		
			return expiredRemoved ;
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to delete expired sessions. SQL error occurred trying to execute {}. ", SessionCacheDAOImpl.QUERY_DELETE_EXPIRED_SESSIONS);
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to delete expired sesions due to underlying data source failure.");
		}
	}
	
	
	public Principal getSession(String sessionID) throws SessionCacheDAOException
	{
		return this.getSession(sessionID, true);
	}

	
	public Principal getSession(String sessionID, boolean updateLastAccessed) throws SessionCacheDAOException
	{
		SessionData sessionParam = new SessionData();
		
		// Query by session ID and current time so we don't get an expired session
		sessionParam.setSessionID(sessionID);
		sessionParam.setSessionNotOnOrAfter(System.currentTimeMillis());
		sessionParam.setLastAccessed(System.currentTimeMillis());
		
		if(updateLastAccessed)
		{
			try
			{
				this.getSqlMapClient().update(SessionCacheDAOImpl.QUERY_UPDATE_SESSION_LAST_ACCESSED, sessionParam);
			}
			catch(SQLException e)
			{
				this.logger.warn(MessageFormat.format("Unable to update lastAccessed timestamp for session {0}.",  sessionID));
				this.logger.debug("Exception: " + e.getLocalizedMessage());
			}
		}
		
		Principal principal = this.getSessionFromQuery(SessionCacheDAOImpl.QUERY_GET_SESSION, sessionParam);
		
		String value = "null";
		if (principal != null)
		{
			value = "Principal with SAML ID: " + principal.getPrincipalAuthnIdentifier() + "  Principal authn identifier: " + principal.getPrincipalAuthnIdentifier();
		}
		
		this.logger.debug("Query for session by session ID: {} returned {}", sessionID, value);
		return principal;
	}
	
	public List<String> getIdleSessions(long idleTimePeriod) throws SessionCacheDAOException
	{
		try
		{			
			SessionData sessionParam = new SessionData();
			
			//Get all sessions not modified within given time period
			sessionParam.setLastAccessed(System.currentTimeMillis() - (idleTimePeriod));
			
			this.logger.debug(MessageFormat.format("Querying database for sessions not accessed after {0} ({1}). " , sessionParam.getLastAccessed(), new Date(sessionParam.getLastAccessed()) ));
			
			SqlMapClient sqlMapClient = this.getSqlMapClient();
			
			List<String> idleSessions = (List<String>) sqlMapClient.queryForList(SessionCacheDAOImpl.QUERY_GET_IDLE_SESSIONS, sessionParam) ;
			
			return idleSessions;
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured while attempting to retrieve idle  sessions. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_GET_IDLE_SESSIONS+ ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to retrieve idle  sessions due to underlying data source failure.");
		}
			
	}
	
	public Principal getSessionBySAMLID(String samlID) throws SessionCacheDAOException
	{
		SessionData sessionParam = new SessionData();
		
		// Query by SAML ID and current time so we don't get an expired session
		sessionParam.setSamlAuthnIdentifier(samlID);
		sessionParam.setSessionNotOnOrAfter(System.currentTimeMillis());
		
		this.logger.debug(java.text.MessageFormat.format("Running query {0}." , SessionCacheDAOImpl.QUERY_GET_SESSION_BY_SAML_ID) );
		
		Principal principal = this.getSessionFromQuery(SessionCacheDAOImpl.QUERY_GET_SESSION_BY_SAML_ID, sessionParam);
		String value = "null";
		if (principal != null)
		{
			value = "principal with session ID: " + principal.getSessionID() + "  Principal authn identifier: " + principal.getPrincipalAuthnIdentifier();
		}
		
		this.logger.debug("Query for session by SAML ID: {} returned {}", samlID, value);
		return principal;
	}
	
	@SuppressWarnings("unchecked")
	private Principal getSessionFromQuery(String queryName, SessionData queryParam) throws SessionCacheDAOException
	{
		try
		{
			SqlMapClient sqlMapClient = this.getSqlMapClient();
			SessionData sessionData = new SessionData();
						
			sessionData = (SessionData)sqlMapClient.queryForObject(queryName, queryParam, sessionData);
			if (sessionData == null)
			{
				this.logger.debug("Query returned no principal data for session id: {} notOnOrAfter {}", queryParam.getSessionID(), queryParam.getSessionNotOnOrAfter());
				return null;
			}
			
			PrincipalImpl principal = new PrincipalImpl();
			principal.setSessionID(sessionData.getSessionID());
			principal.setSAMLAuthnIdentifier(sessionData.getSamlAuthnIdentifier());
			principal.setPrincipalAuthnIdentifier(sessionData.getPrincipalAuthnIdentifier());
			principal.setLastAccessed(sessionData.getLastAccessed());
			principal.setAuthnTimestamp(sessionData.getAuthnTimestamp());
			principal.setAuthenticationContextClass(sessionData.getAuthenticationClassContext());
			principal.setSessionNotOnOrAfter(sessionData.getSessionNotOnOrAfter());
			ByteArrayInputStream stream = new ByteArrayInputStream(sessionData.getAttributes());
			ObjectInputStream inputStream = new ObjectInputStream(stream);
			
			Map<String, IdentityAttribute> attributes = (Map<String, IdentityAttribute>)inputStream.readObject();

			for (String key : attributes.keySet())
			{
				principal.putAttribute(key, attributes.get(key));
			}
			
			RowHandler rowHandler = new EntityDescriptorSessionRowHandler(principal);
			sqlMapClient.queryWithRowHandler(SessionCacheDAOImpl.QUERY_GET_ACTIVE_ENTITY_SESSIONS, queryParam, rowHandler);
			
			return principal;
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured while attempting to retrieve principal active endpoints. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_GET_ACTIVE_ENTITY_SESSIONS + ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to retrieve active endpoints due to underlying data source failure.");
		}
		catch (IOException e)
		{
			throw new SessionCacheDAOException("I/O error occurred trying to read attribute map from BLOB.", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new SessionCacheDAOException("Class not found error occurred trying to read attribute map from BLOB. NOTE: Check that ESOE has been deployed correctly as this would signal a serious classpath problem.", e);
		}
	}

	public int getSize() throws SessionCacheDAOException
	{
		try
		{
			return ((Integer)this.getSqlMapClient().queryForObject(SessionCacheDAOImpl.QUERY_GET_SESSION_CACHE_SIZE)).intValue();
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error ocured attempting to retrieve cache size. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_GET_SESSION_CACHE_SIZE + ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to retrieve session cache size due to underlying data source failure.");
		}
	}

	public boolean deleteSession(String sessionID) throws SessionCacheDAOException
	{
		try
		{
			SessionData sessionData = new SessionData();
			sessionData.setSessionID(sessionID);
			int result = this.getSqlMapClient().delete(SessionCacheDAOImpl.QUERY_DELETE_SESSION, sessionData);
			return (result > 0);
		}
		catch (SQLException e)
		{ 
			this.logger.error("A DataSource error occured attempting to delete principal session. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_DELETE_SESSION+ ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to delete session due to underlying data source failure.");
		}
	}

	public void updateSessionSAMLID(Principal data) throws SessionCacheDAOException
	{
		try
		{
			SessionData sessionData = new SessionData();
			sessionData.setSessionID(data.getSessionID());
			sessionData.setSamlAuthnIdentifier(data.getPrincipalAuthnIdentifier());
			
			this.getSqlMapClient().update(SessionCacheDAOImpl.QUERY_UPDATE_SESSION_SAML_ID, sessionData);
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to update SAML session ID. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_UPDATE_SESSION_SAML_ID + ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to update session SAML ID due to underlying data source failure.");
		}
	}

	public boolean validSession(String sessionID) throws SessionCacheDAOException
	{
		try
		{
			SessionData sessionData = new SessionData();
			sessionData.setSessionID(sessionID);
			sessionData.setLastAccessed(System.currentTimeMillis());
			
			this.logger.debug(MessageFormat.format("Verifying session {0} for validity.", sessionID) )			;
			
			int result = this.getSqlMapClient().update(SessionCacheDAOImpl.QUERY_UPDATE_SESSION_LAST_ACCESSED, sessionData);
			
			// If the result is non-zero, there was a row updated which means that the session is valid.
			return (result != 0);
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to set last updated session. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_UPDATE_SESSION_LAST_ACCESSED + ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to verify session due to underlying data source failure.");
			}
	}

	public void addDescriptor(Principal principal, String entityID) throws InvalidSessionIdentifierException, SessionCacheDAOException
	{
		try
		{
			DescriptorSessionData descriptorSessionData = new DescriptorSessionData();
			descriptorSessionData.setSessionID(principal.getSessionID());
			descriptorSessionData.setEntityID(entityID);
			
			this.getSqlMapClient().insert(SessionCacheDAOImpl.QUERY_INSERT_DESCRIPTOR, descriptorSessionData);
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to insert session descriptor. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_INSERT_DESCRIPTOR + ". ");
			this.logger.debug("Exception: " + e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to add session descriptor due to underlying data source failure.");
		}
	}
	

	public void addDescriptorSessionIdentifier(Principal principal, String entityID, String descriptorSessionID) throws  SessionCacheDAOException, InvalidDescriptorIdentifierException, SessionCacheDAOException
	{
		try
		{
			// TODO checks for invalid id sexceptions 
			
			DescriptorSessionData descriptorSessionData = new DescriptorSessionData();
			descriptorSessionData.setSessionID(principal.getSessionID());
			descriptorSessionData.setEntityID(entityID);
			descriptorSessionData.setEntitySessionID(descriptorSessionID);
			
			this.getSqlMapClient().insert(SessionCacheDAOImpl.QUERY_INSERT_DESCRIPTOR_SESSION, descriptorSessionData);
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to insert active entity session descriptor. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_INSERT_DESCRIPTOR_SESSION + ". ");
			this.logger.debug("Exception: ", e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to add descriptor session ientifier due to underlying data source failure.");
		}
	}
	
	public void updatePrincipalAttributes(Principal principal) throws  SessionCacheDAOException
	{
		try
		{
			SessionData sessionData = new SessionData();
			byte[] attributes = this.serializeAttributeMap(principal.getAttributes());
			sessionData.setAttributes(attributes);
			sessionData.setSessionID(principal.getSessionID());
			
			int result = this.getSqlMapClient().update(SessionCacheDAOImpl.QUERY_UPDATE_SESSION_ATTRIBUTES, sessionData);
			
			if (result <= 0)
			{
				// TODO really? .. is this required?
				throw new SessionCacheDAOException("No rows were updated with session attributes."); 
			}
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to update session Principal attributes. SQL error occurred trying to execute " + SessionCacheDAOImpl.QUERY_UPDATE_SESSION_ATTRIBUTES + ". ");
			this.logger.debug(e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to update principal attributes due to underlying data source failure.");
		}
	}
	
	/* Update all details for the given principals sessions, if the session has become idle and has been logged out of active entities.
	 * 
	 */
	public boolean updateIdleEntitySessions(Principal principal, long idleGraceExpiryTime) throws  SessionCacheDAOException
	{
		try
		{
			SessionData sessionData = new SessionData();
			sessionData.setSessionID(principal.getSessionID());
			sessionData.setIdleGraceExpiry(idleGraceExpiryTime);
			
			this.logger.debug("Running query to delete active sessions for " + sessionData.getSessionID());
			
			int result = this.getSqlMapClient().update(SessionCacheDAOImpl.QUERY_DELETE_ACTIVE_ENTITY_SESSIONS, sessionData);
						
			this.logger.debug(MessageFormat.format("Deleted {0} active entity sessions for user {1}.", result, principal.getPrincipalAuthnIdentifier()) );
			
			result = this.getSqlMapClient().update(SessionCacheDAOImpl.QUERY_UPDATE_SESSION_IDLE_GRACE_EXPIRY, sessionData);
						
			if (result <= 0)
			{
				this.logger.error("A DataSource error occured attempting to set idle grace expiry time for session " + sessionData.getSessionID());
			}
			else
				this.logger.debug(MessageFormat.format("Setting idle grace time expiry to {0}.",  sessionData.getIdleGraceExpiry()) );
			
			return (result != 0);
			
		}
		catch (SQLException e)
		{
			this.logger.error("A DataSource error occured attempting to update idle grace expiry and active entities. SQL error occurred trying to execute {}." , SessionCacheDAOImpl.QUERY_UPDATE_SESSION_IDLE_GRACE_EXPIRY);
			this.logger.debug(e.getLocalizedMessage());
			throw new SessionCacheDAOException("Unable to update idle grace expiry and active entities due to underlying data source failure.");
	
		}
	}
	

	private class EntityDescriptorSessionRowHandler implements RowHandler
	{
		private Principal principal;
		
		public EntityDescriptorSessionRowHandler(Principal principal)
		{
			this.principal = principal;
		}
		
		@Override
		public void handleRow(Object dataObj)
		{
			if (!(dataObj instanceof DescriptorSessionData))
			{
				throw new IllegalArgumentException("Data object passed to EntityDescriptorSessionRowHandler was not of the expected type. Type was: "
						+ (dataObj == null ? "null" : dataObj.getClass()));
			}
			
			DescriptorSessionData entitySessionsData = (DescriptorSessionData)dataObj;
			String entityID = entitySessionsData.getEntityID();
			String entitySessionID = entitySessionsData.getEntitySessionID();
			
			this.principal.addEntitySessionIndex(entityID, entitySessionID);
		
		}
	}
}
