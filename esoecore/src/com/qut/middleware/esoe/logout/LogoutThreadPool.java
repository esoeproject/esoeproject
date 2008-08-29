package com.qut.middleware.esoe.logout;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import com.qut.middleware.esoe.logout.bean.SSOLogoutState;
import com.qut.middleware.esoe.sessions.Principal;

/** A Thread executor pool providing methods to offload Logout tasks and track the status of submitted Logout
 * Threads. 
 *
 */
public interface LogoutThreadPool extends Executor
{
	
	/** Retrieve a list of logout states for the given task. The task ID is returned to the caller when
	 * a LogoutThread is submitted for processing via the createTask() method. 
	 * 
	 * @param TaskID The task ID to query for logout states.
	 * @return The list of logout states IFF the given task ID matches a successful task submission AND
	 * the task has been completed, else null.
	 */
	public List<SSOLogoutState> getLogoutStates(String TaskID);
		
	
	/** Creates a task Thread to perform the logout process for the given params.
	 * 
	 * @param principal The Principal to logout.
	 * @param storeState Whether to store logout state data from the created task. If set to true, the state
	 * information will be available via getLogoutStates() when the task is completed.
	 * @return A String identifier for the submitted task if the task is not rejected.
	 */
	public String createLogoutTask(Principal principal, boolean storeState);
	
}
