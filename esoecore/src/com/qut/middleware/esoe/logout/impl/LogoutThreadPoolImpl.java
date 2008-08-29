package com.qut.middleware.esoe.logout.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.logout.LogoutMechanism;
import com.qut.middleware.esoe.logout.LogoutThreadPool;
import com.qut.middleware.esoe.logout.bean.SSOLogoutState;
import com.qut.middleware.esoe.sessions.Principal;

public class LogoutThreadPoolImpl extends ThreadPoolExecutor implements LogoutThreadPool{

	private static long defaultThreadIdleTimeout = 60L;

	private LogoutMechanism logoutMechanism;
	private Map<String, Future<List<SSOLogoutState>>> taskFutures;	
	// this queue is used to control the bounds of the Future map, as maps cannot be bounded (yet we want fast access).
	private LinkedBlockingQueue<String> taskList;
	
	/* Local logging instance */
	Logger logger = LoggerFactory.getLogger(LogoutThreadPoolImpl.class.getName());
	
	/** 
	 * 
	 * @param logoutMechanism The Logout mechanism to use for logging out of principals.
	 * @param minThreads Minumum number of threads to keep active in the thread pool.
	 * @param maxThreads Maximum number of threads to have active in the thread pool.
	 * @param maxQueueSize The maximum size of the task queue. The task queue is used to store Logout
	 * state data for submitted tasks, which can later be retrieved via getLogoutStates(). This data is stored in a FIFO 
	 * queue, whereby the oldest entries are purged if the queue reaches this maximum size.
	 */
	public LogoutThreadPoolImpl(LogoutMechanism logoutMechanism, int minThreads, int maxThreads, int maxQueueSize)
	{
		// super constructor will take care of  param checking
		super(minThreads, maxThreads,  defaultThreadIdleTimeout, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		
		if(logoutMechanism == null)
			throw new IllegalArgumentException("Param logoutMechanism MUST NOT be null.");
			
		this.logoutMechanism = logoutMechanism;
		this.taskList = new LinkedBlockingQueue<String>(maxQueueSize);
		this.taskFutures = new HashMap<String, Future<List<SSOLogoutState>>>();
		
		this.logger.info(MessageFormat.format("Successfully created LogoutThreadPool. Min thread count {0}. Max thread count {1}. Task queue size max is {2}.", minThreads, maxThreads, maxQueueSize) );
	}
	
	
	/* NOTE: The implementation of this method ensures that the task is complete for the given taskID. This being
	 *  the case, calls to this method will block on the given task thread, if found, until it is complete.
	 * 
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.logout.LogoutThreadPool#getLogoutStates(java.lang.String)
	 */
	public List<SSOLogoutState> getLogoutStates(String taskID)
	{
		this.logger.debug("Task info for " + taskID + " ...");
		
		if(this.taskList.contains(taskID))
		{
			Future<List<SSOLogoutState>> task = this.taskFutures.remove(taskID);
			if(task != null)
			{
				try
				{					
					// blocks until task is complete
					return task.get();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return null;
				}
			}
			else
				this.logger.debug("No Future (state information) available for given task. Possibly, an error occurred when scheduling the task. ");
			
			this.taskList.remove(taskID);
		}
		else
			this.logger.debug(MessageFormat.format("Unable to find any record for task {0}. Either the given taskID is incorrect, or the record has aged and been deleted.",  taskID));
		
		return null;
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.qut.middleware.esoe.logout.LogoutThreadPool#createLogoutTask(com.qut.middleware.esoe.sessions.Principal)
	 */	
	public String createLogoutTask(Principal principal, boolean storeState) 
	{
		String taskID = null;
		
		int activeThreads = this.getActiveCount();
		int totalThreads = this.getPoolSize();
		
		this.logger.debug(MessageFormat.format("Logout Thread pool size is {0}. Currently active threads is {1}.", totalThreads, activeThreads) );
				
		try
		{			
			// Block until threads are available to minimize Rejected Tasks.
			while(activeThreads >= this.getMaximumPoolSize() - 1)
			{
				activeThreads = this.getActiveCount();
			}
		
			LogoutTask lTask = new LogoutTask(this.logoutMechanism, principal);
			Future<List<SSOLogoutState>> futureTask =  super.submit(lTask);
			taskID = new Integer(futureTask.hashCode()).toString();
			
			if(storeState)
			{	
				this.taskFutures.put(taskID, futureTask);
				
				// if the queue is full, adjust size of the future list
				if( ! this.taskList.offer(taskID) )
				{
					// remove head of queue, then add new to tail, then removed corresponding future
					String removed = this.taskList.poll();
					this.taskList.add(taskID);
					this.taskFutures.remove(removed);			
				}
					
				this.logger.debug(MessageFormat.format("Added task {0} to Thread pool. Currently {1} task Futures stored.", taskID, this.taskList.size()) );
			}			
		}
		catch(RejectedExecutionException e)
		{
			// an error occured schedluing the task
			this.logger.error("LogoutTask Rejected - " + e.fillInStackTrace());
			this.logger.trace("Trace: - ", e);
		}
		
		return taskID;
	}	
	
}
