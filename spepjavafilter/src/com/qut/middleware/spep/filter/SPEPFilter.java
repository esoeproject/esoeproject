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
 * Author: Shaun Manglesdorf / Bradley Beddoes
 * Creation Date: 12/12/2006 / 04/03/2007
 * 
 * Purpose: A filter to control access to resources protected by the SPEP servlet.
 */
package com.qut.middleware.spep.filter;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.qut.middleware.spep.Initializer;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.sessions.PrincipalSession;

/** A filter to control access to resources protected by the SPEP servlet. */
public class SPEPFilter implements Filter
{
	public static final String ATTRIBUTES = "attributes"; //$NON-NLS-1$

	private FilterConfig filterConfig;
	private static final String SPEP_CONTEXT_PARAM_NAME = "spep-context"; //$NON-NLS-1$
	private String spepContextName;
	
	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
		this.spepContextName = filterConfig.getInitParameter(SPEP_CONTEXT_PARAM_NAME);
		
		if(this.spepContextName == null)
			throw new ServletException(Messages.getString("SPEPFilter.8") + SPEP_CONTEXT_PARAM_NAME); //$NON-NLS-1$
	}

	public void destroy()
	{
		// no action
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
	{
		if (!(servletRequest instanceof HttpServletRequest))
		{
			throw new ServletException(Messages.getString("SPEPFilter.0"));  //$NON-NLS-1$
		}
		if (!(servletResponse instanceof HttpServletResponse))
		{
			throw new ServletException(Messages.getString("SPEPFilter.1"));  //$NON-NLS-1$
		}
		
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		HttpServletResponse response = (HttpServletResponse)servletResponse;
		String resource, decodedResource;
		
		ServletContext spepContext = this.filterConfig.getServletContext().getContext(this.spepContextName); 
		
		// Get servlet context.
		if (spepContext == null)
		{
			throw new ServletException(Messages.getString("SPEPFilter.2") + " " + this.spepContextName);  //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// Establish SPEP object.
		SPEP spep;
		try
		{
			spep = Initializer.init(spepContext);
		}
		catch (Exception e)
		{
			throw new ServletException(Messages.getString("SPEPFilter.3"), e);  //$NON-NLS-1$
		}
		
		// Ensure SPEP startup.
		if (!spep.isStarted())
		{
			// Don't allow anything to occur if SPEP hasn't started correctly.
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new ServletException(Messages.getString("SPEPFilter.4"));  //$NON-NLS-1$
		}
		
		// Get SPEP cookie.
		Cookie spepCookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				if (cookie.getName().equals(spep.getTokenName()))
				{
					spepCookie = cookie;
					break;
				}
			}
		}
		
		// value for re-determining session status after Authz request
		boolean validSession = true;
		
		// Check SPEP session is valid.
		if (spepCookie != null)
		{
			String sessionID = spepCookie.getValue();
			
			PrincipalSession PrincipalSession = spep.getAuthnProcessor().verifySession(sessionID);
			
			if (PrincipalSession != null)
			{
				// over write with new data if it exists
				WORMHashMap<String, List<Object>> attributeMap = new WORMHashMap<String, List<Object>>();
				attributeMap.putAll(PrincipalSession.getAttributes());					
				attributeMap.close();
				
				request.getSession().setAttribute(ATTRIBUTES, attributeMap);
				
				/* This section of code is critical, we must pass the PEP an exact representation of what the user is attempting to access
				 * additionally the PEP expects that the string is not in encoded form as it will do exact matching, so we decode before 
				 * passing our request to it.
				 */
				resource = request.getRequestURI();
				if(request.getQueryString() != null)
					resource = resource + "?" + request.getQueryString(); //$NON-NLS-1$
					
				decodedResource = decode(resource);
				
				decision authzDecision = spep.getPolicyEnforcementProcessor().makeAuthzDecision(sessionID, decodedResource);
				
				// the authz processor may destroy the session if the PDP determines that the client
				// session is no longer valid, so we have to check it again
				if( (PrincipalSession = spep.getAuthnProcessor().verifySession(sessionID)) == null)
					validSession = false;
				
				if(validSession)
				{
					if (authzDecision == decision.permit)
					{
						chain.doFilter(request, response);
						return;
					}
					else if (authzDecision == decision.deny)
					{
						response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					else if (authzDecision == decision.error)
					{
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						throw new ServletException(Messages.getString("SPEPFilter.6"));  //$NON-NLS-1$
					}
					else
					{
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						throw new ServletException(Messages.getString("SPEPFilter.7"));  //$NON-NLS-1$
					}
				}
			}
			
			/* Clear the local session object the supplied request is invalid */
			request.getSession().invalidate();
			
		}
		
		// If we get to this stage, the session has not been authenticated. We proceed to clear the
		// cookies configured by the SPEP to be cleared upon logout, since this is potentially the
		// first time they have come back to the SPEP since logging out.

		List<Cookie> clearCookies = new Vector<Cookie>();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				if(spep.getLogoutClearCookies() != null)
				{
					for (Cookie clearCookie : spep.getLogoutClearCookies())
					{
						if (cookie.getName().equalsIgnoreCase(clearCookie.getName()))
						{						
							Cookie clearCookieCloneInsecure = (Cookie)clearCookie.clone();
							clearCookieCloneInsecure.setMaxAge(0);
							clearCookieCloneInsecure.setSecure( false );
							
							clearCookies.add(clearCookieCloneInsecure);
							
							// Don't need to process the inner loop again for this cookie.
							break;
						}
					}
				}
			}
		}
		
		// Add the cookies to be cleared into the response object.
		for( Cookie c : clearCookies )
			response.addCookie(c);
		
		String requested = request.getRequestURI() + "?" + request.getQueryString();
		String base64RequestURI = new String(Base64.encodeBase64(requested.getBytes()));

		String redirectURL = MessageFormat.format(spep.getLoginRedirect(), new Object[]{base64RequestURI});
		response.sendRedirect(redirectURL);
	}
	
    /**
     * Transcodes %XX symbols per RFC 2369 to normalized character format
     * @param encodedStr Request string with encoded data
     * @return An unencoded representation of the request
     * @throws ServletException
     */
    private String decode(final String encodedStr) throws ServletException
    {
        if (encodedStr == null)
        {
            return null;
        }
        final StringBuffer buffer = new StringBuffer(encodedStr);
        decode(buffer, 0, buffer.length());
        return buffer.toString();
    }

    /**
     * Transcodes %XX symbols per RFC 2369 to normalized character format - adapted from apache commons
     * @param buffer The stringBuffer object containing the request data
     * @param offset How far into the string buffer we should start processing from
     * @param length Number of chars in the buffer
     * @throws ServletException
     */
    private void decode(final StringBuffer buffer, final int offset, final int length) throws ServletException
    {
        int index = offset;
        int count = length;
        int dig1, dig2;
        while(count > 0)
        {
            final char ch = buffer.charAt(index);
            if (ch != '%')
            {
                count--;
                index++;
                continue;
            }
            if (count < 3)
            {
                throw new ServletException(Messages.getString("SPEPFilter.10") + buffer.substring(index, index + count)); //$NON-NLS-1$
            }

            
            dig1 = Character.digit(buffer.charAt(index + 1), 16);
            dig2 = Character.digit(buffer.charAt(index + 2), 16);
            if (dig1 == -1 || dig2 == -1)
            {
               throw new ServletException(Messages.getString("SPEPFilter.11") + buffer.substring(index, index + count)); //$NON-NLS-1$
            }
            char value = (char) (dig1 << 4 | dig2);

            buffer.setCharAt(index, value);
            buffer.delete(index + 1, index + 3);
            count -= 3;
            index++;
        }
    }
}
