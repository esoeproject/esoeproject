package com.qut.middleware.esoe.sessions.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.ConfigurationConstants;
import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.Create;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.IdentityData;
import com.qut.middleware.esoe.sessions.bean.SessionConfigData;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityDataImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.DataSourceException;
import com.qut.middleware.esoe.sessions.exception.DuplicateSessionException;
import com.qut.middleware.esoe.sessions.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.sessions.identity.IdentityResolver;
import com.qut.middleware.saml2.identifier.IdentifierGenerator;
import com.qut.middleware.saml2.schemas.assertion.AttributeType;
import com.qut.middleware.saml2.schemas.esoe.sessions.DataType;

public class CreateImpl implements Create
{
	private SessionCache sessionCache;
	private SessionConfigData sessionConfigData;
	private IdentityResolver identityResolver;
	private IdentifierGenerator identifierGenerator;
	private long sessionLengthMillis;

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(CreateImpl.class.getName());
	private Logger authnLogger = LoggerFactory.getLogger(ConfigurationConstants.authnLogger);

	/**
	 * @param sessionCache
	 *            The session cache to use as a data store.
	 * @param sessionConfigData
	 *            The session config data to use when retrieving attributes.
	 * @param identityResolver
	 *            The identity resolver to use when creating a local session.
	 * @param identifierGenerator
	 *            The identifier generator to use.
	 * @param sessionLength
	 *            The length of a new session in seconds.
	 */
	public CreateImpl(SessionCache sessionCache, SessionConfigData sessionConfigData, IdentityResolver identityResolver, IdentifierGenerator identifierGenerator, int sessionLength)
	{
		if (sessionCache == null)
		{
			throw new IllegalArgumentException("Session cache cannot be null");
		}
		if (sessionConfigData == null)
		{
			throw new IllegalArgumentException("Session config data cannot be null");
		}
		if (identityResolver == null)
		{
			throw new IllegalArgumentException("Identity resolver cannot be null");
		}
		if (identifierGenerator == null)
		{
			throw new IllegalArgumentException("Identifier generator cannot be null");
		}
		if (sessionLength < 0)
		{
			throw new IllegalArgumentException("Session length must be a positive integer");
		}

		this.sessionCache = sessionCache;
		this.sessionConfigData = sessionConfigData;
		this.identityResolver = identityResolver;
		this.identifierGenerator = identifierGenerator;
		this.sessionLengthMillis = ((long) sessionLength) * 1000L;
		
		this.logger.info("CreateImpl created with session length {} milliseconds", this.sessionLengthMillis);
	}

	public void createLocalSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass, List<AuthnIdentityAttribute> authnIdentityAttributes) throws SessionCacheUpdateException
	{
		if(principalAuthnIdentifier == null || authenticationContextClass == null || authnIdentityAttributes == null)
			throw new IllegalArgumentException("One or more null parameters recievedby createLocalSession.");
			
		this.logger.debug("Going to resolve attributes for new session {} with principal authn identifier {}. {} authnIdentityAttributes passed in.", new Object[] { sessionID, principalAuthnIdentifier, authnIdentityAttributes.size() });

		// Set up the IdentityData attribute which is used to resolve the user's attributes.
		IdentityData identityData = new IdentityDataImpl();
		identityData.setPrincipalAuthnIdentifier(principalAuthnIdentifier);
		identityData.setSessionID(sessionID);
		identityData.setIdentity(this.sessionConfigData.getIdentity());

		try
		{
			this.logger.debug("Calling identity resolver to resolve attributes for new session {}", sessionID);
			this.identityResolver.execute(identityData);

			this.logger.info("Successfully resolved attributes for new session {}", sessionID);
		}
		catch (HandlerRegistrationException e)
		{
			this.logger.debug("Handler registration exception occurred while trying to resolve attributes for session {}. Exception follows", new Object[] { sessionID }, e);
			this.logger.error("Handler registration exception occurred while trying to resolve attributes for session {}. Exception message was: {}", new Object[] { sessionID, e.getMessage() });
			throw new SessionCacheUpdateException("Handler registration exception occurred while trying to resolve attributes for session " + sessionID);
		}
		catch (DataSourceException e)
		{
			this.logger.debug("Data source exception occurred while trying to resolve attributes for session {}. Exception follows", new Object[] { sessionID }, e);
			this.logger.error("Data source exception occurred while trying to resolve attributes for session {}. Exception message was: {}", new Object[] { sessionID, e.getMessage() });
			throw new SessionCacheUpdateException("Data source exception occurred while trying to resolve attributes for session " + sessionID);
		}
		catch (DuplicateSessionException e)
		{
			this.logger.debug("Duplicate session exception occurred while trying to resolve attributes for session {}. Exception follows", new Object[] { sessionID }, e);
			this.logger.error("Duplicate session exception occurred while trying to resolve attributes for session {}. Exception message was: {}", new Object[] { sessionID, e.getMessage() });
			throw new SessionCacheUpdateException("Duplicate session exception occurred while trying to resolve attributes for session " + sessionID);
		}

		identityData.setCurrentHandler(null);

		String samlAuthnID = this.identifierGenerator.generateSAMLAuthnID();
		this.logger.info("Created SAML ID {} for new ESOE session ID {} with principal authn identifier {} and authentication context {}", new Object[] { samlAuthnID, sessionID, principalAuthnIdentifier, authenticationContextClass });

		// Create the principal object
		PrincipalImpl principal = new PrincipalImpl();
		principal.setSessionID(sessionID);
		principal.setPrincipalAuthnIdentifier(principalAuthnIdentifier);

		long time = System.currentTimeMillis();
		principal.setAuthnTimestamp(time);
		principal.setLastAccessed(time);
		principal.setSessionNotOnOrAfter(time + this.sessionLengthMillis);
		principal.setAuthenticationContextClass(authenticationContextClass);
		principal.setSAMLAuthnIdentifier(samlAuthnID);
		
		// Set resolved identity data for principal
		this.logger.debug("Adding {} resolved attributes to Principal ..",  identityData.getAttributes().size());
		
		Map<String,IdentityAttribute> attributes =  identityData.getAttributes();
		for(String attribute : attributes.keySet())
		{
			principal.putAttribute(attribute, attributes.get(attribute));
		}
		
		/*
		 * Principal is created and identity information setup from backend stores, add dynamically specified data from
		 * the authn handler to allow authz decisions to be made on authn type, provides for n-level authn and higher
		 * level security domains
		 */
		if (authnIdentityAttributes != null)
		{
			this.logger.debug("Session {} established. Adding {} authn identity attributes to attribute map.", new Object[] { sessionID, authnIdentityAttributes.size() });

			for (AuthnIdentityAttribute attrib : authnIdentityAttributes)
			{
				this.logger.debug("Adding authn processor attribute to session {}.. attribute name {} .. going to process values", new Object[] { sessionID, attrib.getName() });
				
				// Create a new attribute object to hold the values.
				IdentityAttribute idAttrib = new IdentityAttributeImpl();
				idAttrib.setType(DataType.STRING.name());
				// Add the new values.
				for (String value : attrib.getValues())
				{
					this.logger.debug("Adding authn processor attribute to session {}.. attribute name {} value {}", new Object[] { sessionID, attrib.getName(), value });
					idAttrib.getValues().add(value);
				}

				// Insert existing values for this attribute, if any.
				if (principal.getAttributes().containsKey(attrib.getName()))
				{
					this.logger.warn("Attribute {} from authn processor conflicts with same attribute from identity resolver. Session with ID {} will have merged values from both sources.", new Object[] { attrib.getName(), sessionID });
					IdentityAttribute attribute = principal.getAttributes().get(attrib.getName());
					// Only string type attributes
					if (attribute.getType().equals(DataType.STRING.name()))
					{
						for (String value : attrib.getValues())
						{
							// Only add the existing value if it's not the same as one of the new ones.
							if (!attribute.getValues().contains(value))
							{
								this.logger.debug("Adding existing attribute values to authn attribute for session {}.. attribute name {} existing value {}", new Object[] { sessionID, attrib.getName(), value });
								attribute.getValues().add(value);
							}
						}
					}
					else
					{
						this.logger.error("Attribute {} from authn processor could not be merged with existing attribute as it was not a String type. Overriding the existing value(s) for this attribute.", new Object[] { attrib.getName() });
					}
				}

				// Having populated the identity attribute object, just have to update the principal object.
				principal.putAttribute(attrib.getName(), idAttrib);
			}
		}

		this.logger.debug("About to insert session {} into the session cache.", sessionID);

		this.sessionCache.addSession(principal);

		Date sessionNotOnOrAfterDate = new Date(principal.getSessionNotOnOrAfter());
		String sessionNotOnOrAfterString = sessionNotOnOrAfterDate.toString();

		this.authnLogger.info("Authenticated new session ID {} with SAML ID {}, principal authn identifier {}, authentication context class {}, authentication timestamp {}, session not on or after {} ({})", new Object[] { sessionID, samlAuthnID, principalAuthnIdentifier, authenticationContextClass, principal.getAuthnTimestamp(), principal.getSessionNotOnOrAfter(), sessionNotOnOrAfterString });
	}

	public void createDelegatedSession(String sessionID, String principalAuthnIdentifier, String authenticationContextClass, List<AttributeType> principalAttributes) throws SessionCacheUpdateException
	{
		/*
		 * Directly create the principal object from incoming request, not there is no need to populate IdentityData
		 * directly
		 */
		PrincipalImpl principal = new PrincipalImpl();
		principal.setSessionID(sessionID);
		principal.setPrincipalAuthnIdentifier(principalAuthnIdentifier);

		long time = System.currentTimeMillis();
		principal.setAuthnTimestamp(time);
		principal.setSessionNotOnOrAfter(time + this.sessionLengthMillis);
		principal.setAuthenticationContextClass(authenticationContextClass);

		String samlAuthnID = this.identifierGenerator.generateSAMLAuthnID();
		principal.setSAMLAuthnIdentifier(samlAuthnID);
		this.logger.info("Created SAML ID {} for new delegated ESOE session ID {} with principal authn identifier {} and authentication context {}", new Object[] { samlAuthnID, sessionID, principalAuthnIdentifier, authenticationContextClass });

		for (AttributeType attrib : principalAttributes)
		{
			// The casting will take care of itself here
			IdentityAttribute localAttrib = new IdentityAttributeImpl();
			for (Object value : attrib.getAttributeValues())
				localAttrib.addValue(value);

			principal.putAttribute(attrib.getName(), localAttrib);
		}

		this.sessionCache.addSession(principal);

		Date sessionNotOnOrAfterDate = new Date(principal.getSessionNotOnOrAfter());
		String sessionNotOnOrAfterString = sessionNotOnOrAfterDate.toString();

		this.authnLogger.info("Authenticated new REMOTE session ID {} with SAML ID {}, principal authn identifier {}, authentication context class {}, authentication timestamp {}, session not on or after {} ({})", new Object[] { sessionID, samlAuthnID, principalAuthnIdentifier, authenticationContextClass, principal.getAuthnTimestamp(), principal.getSessionNotOnOrAfter(), sessionNotOnOrAfterString });
	}
}
