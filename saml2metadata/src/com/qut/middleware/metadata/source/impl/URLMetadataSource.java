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
 * Creation Date: 10/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.source.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.MetadataProcessor;

public abstract class URLMetadataSource extends MetadataSourceBase
{
	private URL url;

	public URLMetadataSource(URL url)
	{
		this.url = url;
	}

	public String getLocation()
	{
		return this.url.toExternalForm();
	}

	public void updateMetadata(MetadataProcessor processor) throws MetadataSourceException
	{
		try
		{
			this.logger.debug("Metadata source {} - going to open URL as input stream", this.getLocation());
			URLConnection connection = this.url.openConnection();
			InputStream input = connection.getInputStream();
			this.readMetadata(input, processor);
			this.logger.debug("Metadata source {} - finished reading metadata.", this.getLocation());
		}
		catch (IOException e)
		{
			String message = MessageFormat.format(
					"An I/O error occurred trying to process the metadata source.  Location: {0}  Format: {1}  Error: {2}",
					this.getLocation(), this.getFormat(), e.getMessage()
			);
			this.logger.error(message, e);
			throw new MetadataSourceException(message, e);
		}
	}
}
