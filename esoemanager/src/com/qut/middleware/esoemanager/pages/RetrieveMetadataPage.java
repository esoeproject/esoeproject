/* 
 * Copyright 2006, Queensland University of Technology
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
 * Author: Bradley Beddoes
 * Creation Date: 1/5/07
 */
package com.qut.middleware.esoemanager.pages;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import net.sf.click.Page;
import net.sf.click.util.ClickUtils;

import com.qut.middleware.esoemanager.metadata.MetadataCache;

public class RetrieveMetadataPage extends Page
{
	public String metadata;
	private MetadataCache metadataCache;
	
	private ClickUtils clickUtils;
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(RetrieveMetadataPage.class.getName());
	
	public RetrieveMetadataPage()
	{
		clickUtils = new ClickUtils();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.click.Page#getPath()
	 */
	@Override
	public String getPath()
	{
		/* We return null here to ensure we have exclusive rights to the response output stream */
		return null;
	}
	
	@Override
	public void onGet()
	{
		OutputStream out = null;
		
		/* Retrieve metadata from cache and serve to caller */
		this.metadata = this.metadataCache.getCacheData();
		
		/* Return empty string if underlying system has not created MD yet */
		if(metadata == null)
			metadata = "";
		
		/* Set headers ready for output of xml, send as a attachment with filename */
		this.getContext().getResponse().setContentType("text/xml");
		this.getContext().getResponse().addHeader("Content-disposition", "attachment; filename=metadata.xml");
		
		try
		{
			out = this.getContext().getResponse().getOutputStream();
			this.getContext().getResponse().getOutputStream().write(metadata.getBytes("UTF-16"));
			this.getContext().getResponse().getOutputStream().flush();
		}
		catch (UnsupportedEncodingException e)
		{
			this.logger.error("UnsupportedEncodingException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		catch (IOException e)
		{
			this.logger.error("IOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		finally
		{

			if (out != null)
				this.clickUtils.close(out);
		}
	}
	
	public void setMetadataCache(MetadataCache metadataCache)
	{
		this.metadataCache = metadataCache;
	}
	
	public MetadataCache getMetadataCache()
	{
		return this.metadataCache;
	}
}
