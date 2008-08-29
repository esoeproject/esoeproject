/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.metadata.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.saml2.exception.UnmarshallerException;

public class MetadataUpdateMonitor extends Thread implements MonitorThread
{
	private volatile boolean running;

	private MetadataCache metadataCache;
	private MetadataGenerator metadataGenerator;

	private int interval;

	private Logger logger = LoggerFactory.getLogger(MetadataUpdateMonitor.class.getName());

	/**
	 * @param metadataCache
	 *            The metadata cache to manipulate with this thread
	 * @param historyDirectory
	 *            Directory to store historical version of all metadata document changes
	 * @param historicalFileName
	 *            Name to use when writing historical metadata archives to disk
	 * @param interval
	 *            The interval at which to refresh the metadata and update if it has changed, in seconds
	 */
	public MetadataUpdateMonitor(MetadataCache metadataCache, MetadataGenerator metadataGenerator, int interval) throws UnmarshallerException
	{
		super("ESOEManager Metadata update monitor"); //$NON-NLS-1$

		if (metadataCache == null)
		{
			this.logger.error("Supplied metadataCache was NULL for MetadataUpdateMonitor");
			throw new IllegalArgumentException("Supplied metadataCache was NULL for MetadataUpdateMonitor");
		}
		if (metadataGenerator == null)
		{
			this.logger.error("Supplied metadataGenerator was NULL for MetadataUpdateMonitor");
			throw new IllegalArgumentException("Supplied metadataGenerator was NULL for MetadataUpdateMonitor");
		}
		if (interval <= 0 || (interval > Integer.MAX_VALUE / 1000))
		{
			throw new IllegalArgumentException("Supplied value for interval was invalid"); //$NON-NLS-1$
		}

		this.metadataCache = metadataCache;
		this.metadataGenerator = metadataGenerator;
		this.interval = interval * 1000;

		this.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		this.setRunning(true);

		try
		{
			generateMetadata();
		}
		catch (Exception e)
		{
			this.logger.error("Creating metadata caused exception network will not be initialized \n" + e.getLocalizedMessage());
			this.logger.debug(e.toString());
		}

		while (this.isRunning())
		{
			try
			{
				Thread.sleep(this.interval);

				this.logger.debug("Woke up about to do metadata update");

				// retrieve metadata and update if required
				generateMetadata();
			}
			catch (InterruptedException e)
			{
				if (!this.isRunning())
					break;
			}
			catch (Exception e)
			{
				this.logger.error("Creating metadata caused exception metadata will not be updated \n" + e.getLocalizedMessage());
				this.logger.debug(e.toString());
			}
		}

		this.logger.info("Terminating thread for class " + this.getName());

		return;
	}

	private void generateMetadata() throws Exception
	{
		byte[] completeMD = null;	// Includes ESOE specific SAML MD exstenstions
		byte[] samlMD = null;		// SAML 2.0 spec only

		/* Generate new metadata instance */
		completeMD = this.metadataGenerator.generateMetadata(true);
		samlMD = this.metadataGenerator.generateMetadata(false);

		if (completeMD != null)
		{
			this.logger.debug("Successfully updated metadata");
			this.metadataCache.setCompleteMD(completeMD);
			this.metadataCache.setSamlMD(samlMD);

			this.logger.debug("Metadata updated successfully");
			return;
		}
		this.logger.debug("Metadata update failed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qut.middleware.esoe.MonitorThread#shutdown()
	 */
	public void shutdown()
	{
		this.setRunning(false);

		this.interrupt();
	}

	/**
	 * @return
	 */
	protected synchronized boolean isRunning()
	{
		return this.running;
	}

	/**
	 * @param running
	 */
	protected synchronized void setRunning(boolean running)
	{
		this.running = running;
	}
}