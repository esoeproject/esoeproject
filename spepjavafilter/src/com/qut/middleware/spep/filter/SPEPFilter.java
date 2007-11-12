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
 * Author: Shaun Manglesdorf and Bradley Beddoes
 * Creation Date: 12/12/2006
 * 
 * Purpose: A filter to control access to resources protected by the SPEP servlet, provides for both hard init and lazy init states.
 */
package com.qut.middleware.spep.filter;

import java.io.IOException;
import java.net.URL;
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
import org.apache.log4j.Logger;

import com.qut.middleware.spep.SPEPProxy;
import com.qut.middleware.spep.sessions.PrincipalSession;

/** A filter to control access to resources protected by the SPEP servlet. */
public class SPEPFilter implements Filter
{
	public static final String ATTRIBUTES = "com.qut.middleware.spep.filter.attributes"; //$NON-NLS-1$

	private FilterConfig filterConfig;
	private static final String SPEP_CONTEXT_PARAM_NAME = "spep-context"; //$NON-NLS-1$
	private String spepContextName;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(SPEPFilter.class.getName());

	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
		this.spepContextName = filterConfig.getInitParameter(SPEP_CONTEXT_PARAM_NAME);

		if (this.spepContextName == null)
			throw new ServletException(Messages.getString("SPEPFilter.8") + SPEP_CONTEXT_PARAM_NAME); //$NON-NLS-1$
	}

	public void destroy()
	{
		// no action
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
	{
		if (!(servletRequest instanceof HttpServletRequest))
		{
			throw new ServletException(Messages.getString("SPEPFilter.0")); //$NON-NLS-1$
		}
		if (!(servletResponse instanceof HttpServletResponse))
		{
			throw new ServletException(Messages.getString("SPEPFilter.1")); //$NON-NLS-1$
		}

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		String resource, decodedResource, requested, redirectURL;
		URL serviceHost;

		ServletContext spepContext = this.filterConfig.getServletContext().getContext(this.spepContextName);

		// Get servlet context.
		if (spepContext == null)
		{
			throw new ServletException(Messages.getString("SPEPFilter.2") + " " + this.spepContextName); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Establish SPEPProxy object.
		SPEPProxy spep;
		try
		{
			spep = Initializer.init(spepContext);
		}
		catch (Exception e)
		{
			this.logger.fatal("Unable to process request to acces resource, SPEP is not responding, check cross context configuration is enabled \n" + e.getLocalizedMessage());
			throw new ServletException(Messages.getString("SPEPFilter.3"), e); //$NON-NLS-1$
		}

		// Ensure SPEP startup.
		if (!spep.isStarted())
		{
			// Don't allow anything to occur if SPEP hasn't started correctly.
			this.logger.fatal("Unable to process request to acces resource, SPEP is not initialized correcty ");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw new ServletException(Messages.getString("SPEPFilter.4")); //$NON-NLS-1$
		}

		// Get SPEP cookie.
		Cookie spepCookie = null;
		Cookie globalESOECookie = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				if (cookie.getName().equals(spep.getTokenName()))
				{
					spepCookie = cookie;
					this.logger.debug("Located spep cookie with value of " + spepCookie.getValue());
				}
				if (cookie.getName().equals(spep.getEsoeGlobalTokenName()))
				{
					globalESOECookie = cookie;
					this.logger.debug("Located globalESOECookie cookie with value of " + globalESOECookie.getValue());
				}
			}
		}

		// value for re-determining session status after Authz request
		boolean validSession = false;

		// Check SPEP session is valid.
		if (spepCookie != null)
		{
			String sessionID = spepCookie.getValue();

			this.logger.info("Attempting to retrieve data for session with ID of " + sessionID);
			PrincipalSession PrincipalSession = spep.verifySession(sessionID);

			if (PrincipalSession != null)
			{
				this.logger.info("Located session with ID of " + sessionID);

				if (request.getSession().getAttribute(ATTRIBUTES) == null)
				{
					// over write with new data if it exists
					WORMHashMap<String, List<Object>> attributeMap = new WORMHashMap<String, List<Object>>();
					attributeMap.putAll(PrincipalSession.getAttributes());
					attributeMap.close();

					request.getSession().setAttribute(ATTRIBUTES, attributeMap);
				}

				/*
				 * This section of code is critical, we must pass the PEP an exact representation of what the user is
				 * attempting to access additionally the PEP expects that the string is not in encoded form as it will
				 * do exact matching, so we decode before passing our request to it.
				 */
				resource = request.getRequestURI();
				if (request.getQueryString() != null)
					resource = resource + "?" + request.getQueryString(); //$NON-NLS-1$

				decodedResource = decode(resource);

				SPEPProxy.decision authzDecision = spep.makeAuthzDecision(sessionID, decodedResource);

				// the authz processor may destroy the session if the PDP determines that the client
				// session is no longer valid, so we have to check it again
				if ((PrincipalSession = spep.verifySession(sessionID)) != null)
					validSession = true;

				if (validSession)
				{
					if (authzDecision == SPEPProxy.decision.permit)
					{
						this.logger.info("PDP advised for session ID of " + sessionID + " that access to resource " + decodedResource + " was permissable");
						chain.doFilter(request, response);
						return;
					}
					else
						if (authzDecision == SPEPProxy.decision.deny)
						{
							this.logger.info("PDP advised for session ID of " + sessionID + " that access to resource " + decodedResource + " was denied, forcing response of" + HttpServletResponse.SC_FORBIDDEN);
							response.setStatus(javax.servlet.http.HttpServletResponse.SC_FORBIDDEN);
							response.sendError(HttpServletResponse.SC_FORBIDDEN);
							return;
						}
						else
							if (authzDecision == SPEPProxy.decision.error)
							{
								this.logger.info("PDP advised for session ID of " + sessionID + " that access to resource " + decodedResource + " was in error, forcing response of" + HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
								response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
								throw new ServletException(Messages.getString("SPEPFilter.6")); //$NON-NLS-1$
							}
							else
							{
								this.logger.info("PDP advised for session ID of " + sessionID + " that access to resource " + decodedResource + " was undetermined, forcing response of" + HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
								response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
								throw new ServletException(Messages.getString("SPEPFilter.7")); //$NON-NLS-1$
							}
				}
			}

			/* Clear the local session object the supplied request is invalid */
			this.logger.debug("Invalidating session for ID of " + sessionID);
			request.getSession().invalidate();
		}

		/*
		 * If we get to this stage, the user has not got a session established with this SPEP. We proceed to clear the
		 * cookies configured by the SPEP to be cleared upon logout, since this is potentially the first time they have
		 * come back to the SPEP since logging out.
		 */
		List<Cookie> clearCookies = new Vector<Cookie>();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				if (spep.getLogoutClearCookies() != null)
				{
					for (Cookie clearCookie : spep.getLogoutClearCookies())
					{
						if (cookie.getName().equalsIgnoreCase(clearCookie.getName()))
						{
							Cookie clearCookieCloneInsecure = (Cookie) clearCookie.clone();
							clearCookieCloneInsecure.setMaxAge(0);
							clearCookieCloneInsecure.setSecure(false);

							clearCookies.add(clearCookieCloneInsecure);

							// Don't need to process the inner loop again for this cookie.
							break;
						}
					}
				}
			}
		}

		/* Add the cookies to be cleared into the response object. */
		for (Cookie c : clearCookies)
			response.addCookie(c);

		/*
		 * Remove any principal object details which may be in the session, this state can occur if the user has removed
		 * their spepSession cookie but retained their jsessionid cookie
		 */
		request.getSession().removeAttribute(ATTRIBUTES);

		/*
		 * At this stage a determination needs to be made about allowing the request to pass SPEP without being hindered
		 * due to lazy session initialization being configured if it isn't or we won't allow the request to pass for the
		 * logical reasons below they will be forced to authenticate.
		 */
		if (spep.isLazyInit())
		{
			this.logger.info("Lazy init is enabled on this SPEP instance, determining if request should be interrogated by SPEP");

			/*
			 * We are being lazy in starting sessions, determine if user has already authenticated with an IDP (the
			 * ESOE), if so we enforce a session (value is not important just that the cookie exists), if not figure out
			 * if user is accessing something that has been configured to force a session to be established before it is
			 * accessible
			 */
			if (globalESOECookie == null)
			{
				this.logger.debug("globalESOECookie was not set for this request");

				boolean matchedLazyInitResource = false;
				resource = request.getRequestURI();
				if (request.getQueryString() != null)
					resource = resource + "?" + request.getQueryString(); //$NON-NLS-1$

				decodedResource = decode(resource);

				for (String lazyInitResource : spep.getLazyInitResources())
				{
					if (decodedResource.matches(lazyInitResource))
					{
						matchedLazyInitResource = true;
						this.logger.info("Lazy session init attempt matched initialization query of " + lazyInitResource + " from request of " + decodedResource);
					}
					else
						this.logger.debug("Lazy session init attempt failed to match initialization query of " + lazyInitResource + " from request of " + decodedResource);
				}

				// If we still have no reason to engage spep functionality for this request let the request pass
				if (matchedLazyInitResource)
				{
					if (spep.getLazyInitDefaultAction().equals(SPEPProxy.defaultAction.deny))
					{
						this.logger.info("No reason to invoke SPEP for access to resource " + decodedResource + " could be determined due to lazyInit, forwarding request to application");
						chain.doFilter(request, response);
						return;
					}
				}
				else
				{
					if (spep.getLazyInitDefaultAction().equals(SPEPProxy.defaultAction.permit))
					{
						this.logger.info("No reason to invoke SPEP for access to resource " + decodedResource + " could be determined due to lazyInit, forwarding request to application");
						chain.doFilter(request, response);
						return;
					}
				}
			}
		}

		/*
		 * All attempts to provide resource access have failed, invoke SPEP to provide secure session establishment
		 * Current request is B64 encoded and appended to request for SPEP to redirect users back to content dynamically
		 */
		this.logger.debug("Failed all avenues to provide access to content");
		if (request.getQueryString() != null)
			requested = request.getRequestURI() + "?" + request.getQueryString();
		else
			requested = request.getRequestURI();

		/*
		 * Determine if the request was directed to the service URL, if so redirect to that point. If not redirect to
		 * the local node.
		 */
		serviceHost = new URL(spep.getServiceHost());

		if (request.getServerName().equals(serviceHost.getHost()))
		{
			/* Ensures that SSL offloading in Layer 7 environments is correctly handled */
			requested = spep.getServiceHost() + requested;
			String base64RequestURI = new String(Base64.encodeBase64(requested.getBytes()));
			redirectURL = MessageFormat.format(spep.getServiceHost() + spep.getSsoRedirect(), new Object[] { base64RequestURI });
		}
		else
		{
			String base64RequestURI = new String(Base64.encodeBase64(requested.getBytes()));
			redirectURL = MessageFormat.format(spep.getSsoRedirect(), new Object[] { base64RequestURI });
		}

		this.logger.debug("Redirecting to " + redirectURL + " to establish secure session");
		response.sendRedirect(redirectURL);
	}

	/**
	 * Transcodes %XX symbols per RFC 2369 to normalized character format
	 * 
	 * @param encodedStr
	 *            Request string with encoded data
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
	 * 
	 * @param buffer
	 *            The stringBuffer object containing the request data
	 * @param offset
	 *            How far into the string buffer we should start processing from
	 * @param length
	 *            Number of chars in the buffer
	 * @throws ServletException
	 */
	private void decode(final StringBuffer buffer, final int offset, final int length) throws ServletException
	{
		int index = offset;
		int count = length;
		int dig1, dig2;
		while (count > 0)
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
