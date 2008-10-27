package com.qut.middleware.esoe.sessions.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.authn.bean.AuthnIdentityAttribute;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.Update;
import com.qut.middleware.esoe.sessions.bean.IdentityAttribute;
import com.qut.middleware.esoe.sessions.bean.impl.IdentityAttributeImpl;
import com.qut.middleware.esoe.sessions.cache.SessionCache;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.saml2.schemas.esoe.sessions.DataType;

public class UpdateImpl implements Update
{
	private SessionCache sessionCache;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public UpdateImpl(SessionCache sessionCache)
	{
		if (sessionCache == null)
		{
			throw new IllegalArgumentException("Session cache cannot be null");
		}

		this.sessionCache = sessionCache;

		this.logger.info("Created UpdateImpl");
	}

	public void addEntitySessionIndex(Principal principal, String entityID, String sessionIndex) throws SessionCacheUpdateException
	{
		// Update the principal and then push the changes to the underlying data store.

		principal.addEntitySessionIndex(entityID, sessionIndex);

		this.sessionCache.addEntitySessionIndex(principal, entityID, sessionIndex);

		this.logger.info("Added entity ID {} session index {} to principal with session ID {} SAML ID {}", new Object[] { entityID, sessionIndex, principal.getSessionID(), principal.getSAMLAuthnIdentifier() });
	}

	public void addPrincipalAttributes(Principal principal, List<AuthnIdentityAttribute> authnIdentityAttributes) throws SessionCacheUpdateException
	{
		// This is the same logic that is used to insert authn identity attributes into a new session in CreateImpl,
		// just with a session cache operation added below..
		// .. so if you change it, update this comment.

		String sessionID = principal.getSessionID(); // For logging.

		if (authnIdentityAttributes != null)
		{
			this.logger.debug("Adding {} authn identity attributes to attribute map for session with ID {}.", new Object[] { authnIdentityAttributes.size(), sessionID });

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
	

		this.logger.info("Added {} authn identity attributes to attribute map for session with ID {}.", new Object[] { authnIdentityAttributes.size(), sessionID });
	}

}
