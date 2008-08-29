/*
 * Copyright 2008, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 25/06/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.processor.MetadataProcessor;

public class MetadataUpdateThread extends Thread
{
	private int interval;
	private volatile boolean running;
	private MetadataProcessor metadataProcessor;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public MetadataUpdateThread(MetadataProcessor metadataProcessor, int intervalSeconds)
	{
		super("Metadata update thread");
		this.metadataProcessor = metadataProcessor;
		this.interval = intervalSeconds * 1000;
		this.running = false;
		
		// Need to start the thread here. Spring doesn't do it by itself.
		this.start();
	}
	
	@Override
	public void run()
	{
		this.logger.info("Thread: " + this.getName() + " starting");
		this.running = true;
		for(;;)
		{
			if (!this.running) break;
			
			try
			{
				this.logger.info("Performing metadata retrieval.");
				this.metadataProcessor.update();
			}
			catch (Exception e)
			{
				this.logger.error("Uncaught runtime exception occurred while updating metadata processor.", e);
			}
			
			if (!this.running) break;
			
			try
			{
				Thread.sleep(this.interval);
			}
			catch (InterruptedException e)
			{
				if (!this.running) break;
			}
		}
		
		this.logger.info("Thread: " + this.getName() + " terminated");
	}
	
	public void shutdown()
	{
		this.logger.info("Thread: " + this.getName() + " was requested to shut down.");
		this.running = false;
		this.interrupt();
	}
}
