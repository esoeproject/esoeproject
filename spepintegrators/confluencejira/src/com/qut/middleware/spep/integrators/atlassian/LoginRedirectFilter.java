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
 * Creation Date: 6/9/207
 * 
 * Purpose: Determines if a user has authenticated to confluence wiki and redirects them to their content
 */
package com.qut.middleware.spep.integrators.atlassian;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class LoginRedirectFilter implements Filter 
{
	/* Local logging instance */
	private Logger logger = Logger.getLogger(LoginRedirectFilter.class.getName());

	public void destroy() 
	{
		return;
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain filterChain) throws IOException, ServletException 
	{
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String redirTarget = servletRequest.getParameter("os_redirect");
		if( redirTarget != null)
		{
			this.logger.debug("Sending user to os_redirect target of: " + redirTarget );
			response.sendRedirect( redirTarget );
			return;
		}
		
		filterChain.doFilter(servletRequest, servletResponse);
	}

	public void init(FilterConfig filterConfig) throws ServletException 
	{
		return;
	}
}
