package com.qut.middleware.esoe.logout.impl;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.bean.SSOLogoutState;
import com.qut.middleware.esoe.logout.bean.impl.SSOLogoutStateImpl;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.exception.InvalidDescriptorIdentifierException;

/** A {@link LogoutTask} will attempt to Log the given {@link Principal} out of all active SPEP sessions, using the given
 * {@link LogoutMechanism}.  If a Logout fails, the attempt will be added to the LogoutFailure repository and the Task will 
 * return. For use with thread pools to increase logout throughtput. On completion, the call method will return a list
 * of logouts states for each active SPEP.
 * 
 */
public class LogoutTask implements Callable<List<SSOLogoutState>>
{
	private String name;
	private Principal principal;
	private LogoutMechanism logoutMechanism ;
	
	/* Local logging instance */
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	
	/** Default constructor.
	 * 
	 * @param logoutMechanism The logout mechanism to use for logging out principal sessions.
	 * @param principal The principal to logout.
	 */
	public LogoutTask(LogoutMechanism logoutMechanism, Principal principal)
	{
		this.logoutMechanism = logoutMechanism;
		this.principal = principal;
		this.name = new Integer(principal.hashCode()).toString();
	}
	
	public List<SSOLogoutState> call() throws Exception 
	{
		this.logger.trace(this.getName() + " calling logout mechanism ...");
		
		return this.logoutPrincipal(this.principal);				
	}
	
							
	/* Log the given Principal out of any active sessions. NOTE: This method will clear the active descriptors list
	 * for the given Principal once active sessions have been logged out.
	 * 
	 * @return A List of logout states for each SPEP that the principal has an active session on.
	 */
	private List<SSOLogoutState> logoutPrincipal(Principal principal)
	{				
		long startTime = System.currentTimeMillis();
		
		List<SSOLogoutState> logoutStates = new Vector<SSOLogoutState>();
		
		this.logger.debug(MessageFormat.format("{0} Logging out active user sessions for {1}." ,this.getName(), principal.getPrincipalAuthnIdentifier()) );
		
		// obtain active entities (SPEPS logged into) for user, iterate through and send logout request to each SPEP
		List<String> activeDescriptors = principal.getActiveDescriptors();
		if(activeDescriptors != null)
		{
			Iterator<String> entitiesIterator = activeDescriptors.iterator();
			while(entitiesIterator.hasNext())
			{
				String entity = entitiesIterator.next();
							
				// resolve all endpoints for the given entity and send logout request
				List<String> endPoints = null;
				
				endPoints = this.logoutMechanism.getEndPoints(entity);
				
				Iterator<String> endpointIter = endPoints.iterator();
				while (endpointIter.hasNext())
				{
					String endPoint = endpointIter.next();
				
					List<String> indicies = null;
					try
					{
						indicies = principal.getDescriptorSessionIdentifiers(entity);
					}
					catch(InvalidDescriptorIdentifierException e)
					{
						this.logger.warn("Unable to retrieve session indicies from principal"); //$NON-NLS-1$
					}
				
					// call the logout code and add the result
					logoutStates.add(this.performAndRecordLogout(endPoint , indicies, principal.getSAMLAuthnIdentifier()) );						
				}
			}
			
			// Reset active descriptors once they've been logged out
			principal.getActiveDescriptors().clear();
		}		
		else
			this.logger.debug("User has no active SPEP sessions ?");
		
		long endTime = System.currentTimeMillis();
		
		this.logger.debug(MessageFormat.format("{0} completed in {1} milliseconds.", this.getName(), (endTime - startTime)) );
		
		return logoutStates;
	}
		

	private SSOLogoutState performAndRecordLogout(String endPoint , List<String> indicies, String principalAuthnIdentifier)
	{
		// store the state of the logout request for reporting if required
		SSOLogoutState logoutState = new SSOLogoutStateImpl();
		
		LogoutMechanism.result result = this.logoutMechanism.performSingleLogout(principalAuthnIdentifier , indicies, endPoint , true);
		
		logoutState.setSPEPURL(endPoint);
				
		if(result == LogoutMechanism.result.LogoutSuccessful)
		{
			logoutState.setLogoutState(true);
			logoutState.setLogoutStateDescription("Logout Successful");
			this.logger.debug(MessageFormat.format("{0} successfully logged {1} out of {2}.", this.getName(), principalAuthnIdentifier, endPoint) ) ;
		}
		else
		{							
			logoutState.setLogoutState(false);
			logoutState.setLogoutStateDescription("Logout Failed");										
			this.logger.warn(MessageFormat.format("{0} Failed to log {1} out of {2}.", this.getName(), principalAuthnIdentifier, endPoint) ) ;
		}
	
		return logoutState;
	}
	
	/* Set the list of Logout states to use.
	 * 
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/* Returns the List of logouts states as returned from logout of principal sessions.
	 * 
	 */
	public String getName()
	{
		return this.name;
	}
	
}
