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
package com.qut.middleware.esoemanager.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoemanager.metadata.logic.MetadataCache;

public class MetadataServlet extends HttpServlet
{
	private static final long serialVersionUID = -809876850994287425L;

	private final String METADATA_CACHE_IMPL = "metadataCache";

	private MetadataCache metadataCache;

	private Logger logger = LoggerFactory.getLogger(MetadataServlet.class);

	@Override
	public void init() throws ServletException
	{
		super.init();

		/* Spring integration to make our servlet aware of IoC */
		WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this
				.getServletContext());

		this.metadataCache = (MetadataCache) webAppContext.getBean(METADATA_CACHE_IMPL,
				com.qut.middleware.esoemanager.metadata.logic.MetadataCache.class);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String path = req.getRequestURI();
		
		if(path.contains("retrieve.htm") || path.endsWith("internal"))
		{
			this.generateMetadata(req, resp, this.metadataCache.getCompleteMD());
			return;
		}
		
		this.generateMetadata(req, resp, this.metadataCache.getSamlMD());
		return;
	}

	private void generateMetadata(HttpServletRequest req, HttpServletResponse resp, byte[] md)
	{
		OutputStream out = null;
        
        /* Set headers ready for output of xml, send as a attachment with filename */
        resp.setContentType("application/samlmetadata+xml");
        resp.addHeader("Content-disposition", "attachment; filename=metadata.xml");
        
        try
        {
             out = resp.getOutputStream();
             resp.getOutputStream().write(md);
             resp.getOutputStream().flush();
        }
        catch (UnsupportedEncodingException e)
        {
             this.logger.error("UnsupportedEncodingException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
        }
        catch (IOException e)
        {
             this.logger.error("IOException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
        }
	}
}
