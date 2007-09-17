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
 * Author: Shaun Mangelsdorf
 * Creation Date: 23/03/2007
 * 
 * Purpose: Tests the SPEPFilter for correct behaviour.
 */
package com.qut.middleware.spep.filter;

import static com.qut.middleware.spep.Capture.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.spep.Capture;
import com.qut.middleware.spep.ConfigurationConstants;
import com.qut.middleware.spep.SPEP;
import com.qut.middleware.spep.authn.AuthnProcessor;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor;
import com.qut.middleware.spep.pep.PolicyEnforcementProcessor.decision;
import com.qut.middleware.spep.sessions.PrincipalSession;

/**
 * @author Shaun
 *
 */
public class SPEPFilterTest
{
	private String spepContextName = "spep-context";
	private String attributesName = "attributes";
	
	private String spepContext = "/spep";
	private String requestURI = "/secure/admin.jsp";
	private String loginRedirect = "https://spep.imaginarycorp.com/spep/sso";
	private String spepTokenName = "spep-session";
	private String sessionID = "_9587198273948qoierjoiqwjeroiuqwer-uqopwiejfiajsdlkgalskjfdalsdfj";
	private Map<String, List<Object>> attributes;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		this.attributes = new HashMap<String, List<Object>>();
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#init(javax.servlet.FilterConfig)}.
	 */
	@Test( expected = ServletException.class )
	public void testInit() throws Exception
	{
		// Verify that it throws if there is no spep context. The normal behaviour will be tested below.
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( null ).once();
		replay( filterConfig );
		
		try
		{
			SPEPFilter spepFilter = new SPEPFilter();
			spepFilter.init( filterConfig );
		}
		finally
		{
			verify( filterConfig );
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test( expected = ServletException.class )
	public void testDoFilterNotStarted() throws Exception
	{
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		expectLastCall().atLeastOnce();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.FALSE ).once();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
		}
		finally
		{/*
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
		//	verify( response );
			verify( spep );
			verify( chain );
			*/
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test
	public void testDoFilterUnauthenticated() 
	{
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( this.requestURI ).anyTimes();
		
		try
		{
		
			response.sendRedirect( this.loginRedirect );
			expectLastCall().atLeastOnce();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).once();
		expect( spep.getServiceHost() ).andReturn( this.loginRedirect ).atLeastOnce();
		expect( spep.getLogoutClearCookies() ).andReturn( new Vector<Cookie>() ).anyTimes();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
		}
		catch(Exception e)
		{
			e.getCause().printStackTrace();
		}
		finally
		{
			/*
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
			verify( response );
			verify( spep );
			verify( chain );
			*/
			
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test
	public void testDoFilterUnauthenticatedCookieClear1() throws Exception
	{
		List<Cookie> clearCookies = new Vector<Cookie>();
		Cookie cookie1 = new Cookie( "APPSESSION", "" );
		cookie1.setDomain( "imaginarycorp.com" );
		Cookie cookie2 = new Cookie( "ANOTHERSESSION", "" );
		cookie2.setDomain( "spep.imaginarycorp.com" );
		cookie2.setPath( "/application" );
		Cookie cookie3 = new Cookie( "SECURESESSION", "" );
		cookie3.setDomain( "spep.imaginarycorp.com" );
		cookie3.setPath( "/secure" );
		cookie3.setSecure( true );
		clearCookies.add( cookie1 );
		clearCookies.add( cookie2 );
		clearCookies.add( cookie3 );

		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{(Cookie)cookie1.clone(), (Cookie)cookie3.clone()} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( this.requestURI ).anyTimes();
		
		Capture<Cookie> captureCookies = new Capture<Cookie>(); 
		response.addCookie( capture( captureCookies ) );
		expectLastCall().anyTimes();
		
		response.sendRedirect( this.loginRedirect );
		expectLastCall().atLeastOnce();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).once();
		expect( spep.getServiceHost() ).andReturn( this.loginRedirect ).atLeastOnce();
		expect( spep.getLogoutClearCookies() ).andReturn( clearCookies ).anyTimes();
		expect( spep.getTokenName() ).andReturn( this.spepTokenName ).anyTimes();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
			
			boolean cookie1Cleared = false, cookie3Cleared = false;
			
			for ( Cookie c : captureCookies.getCaptured() )
			{
				if ( c.getName().equals( cookie1.getName() ) )
				{
					assertEquals( c.getDomain(), cookie1.getDomain() );
					assertEquals( c.getPath(), cookie1.getPath() );
					assertEquals( c.getMaxAge(), 0 );
					
					cookie1Cleared = true;
				}
				else if ( c.getName().equals( cookie3.getName() ) )
				{
					assertEquals( c.getDomain(), cookie3.getDomain() );
					assertEquals( c.getPath(), cookie3.getPath() );
					assertEquals( c.getMaxAge(), 0 );
					
					cookie3Cleared = true;
				}
				else
				{
					fail( "Unexpected cookie cleared by filter - " + c.getName() );
				}
			}
			
			assertTrue( "Cookie1 was not cleared", cookie1Cleared );
			assertTrue( "Cookie3 was not cleared", cookie3Cleared );
		}
		finally
		{
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
		//	verify( response );
			verify( spep );
			verify( chain );
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test
	public void testDoFilterUnauthenticatedCookieClear2() throws Exception
	{
		List<Cookie> clearCookies = new Vector<Cookie>();
		Cookie cookie1 = new Cookie( "APPSESSION", "" );
		cookie1.setDomain( "imaginarycorp.com" );
		Cookie cookie2 = new Cookie( "ANOTHERSESSION", "" );
		cookie2.setDomain( "spep.imaginarycorp.com" );
		cookie2.setPath( "/application" );
		Cookie cookie3 = new Cookie( "SECURESESSION", "" );
		cookie3.setDomain( "spep.imaginarycorp.com" );
		cookie3.setPath( "/secure" );
		cookie3.setSecure( true );
		clearCookies.add( cookie1 );
		clearCookies.add( cookie2 );

		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{(Cookie)cookie1.clone(), (Cookie)cookie3.clone()} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( this.requestURI ).anyTimes();
		
		Capture<Cookie> captureCookies = new Capture<Cookie>(); 
		response.addCookie( capture( captureCookies ) );
		expectLastCall().anyTimes();
		
		response.sendRedirect( this.loginRedirect );
		expectLastCall().atLeastOnce();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).once();
		expect( spep.getServiceHost() ).andReturn( this.loginRedirect ).atLeastOnce();
		expect( spep.getLogoutClearCookies() ).andReturn( clearCookies ).anyTimes();
		expect( spep.getTokenName() ).andReturn( this.spepTokenName ).anyTimes();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
			
			boolean cookie1Cleared = false, cookie3Cleared = false;
			
			for ( Cookie c : captureCookies.getCaptured() )
			{
				if ( c.getName().equals( cookie1.getName() ) )
				{
					assertEquals( c.getDomain(), cookie1.getDomain() );
					assertEquals( c.getPath(), cookie1.getPath() );
					assertEquals( c.getMaxAge(), 0 );
					
					cookie1Cleared = true;
				}
				else if ( c.getName().equals( cookie3.getName() ) )
				{
					assertEquals( c.getDomain(), cookie3.getDomain() );
					assertEquals( c.getPath(), cookie3.getPath() );
					assertEquals( c.getMaxAge(), 0 );
					
					cookie3Cleared = true;
				}
				else
				{
					fail( "Unexpected cookie cleared by filter - " + c.getName() );
				}
			}
			
			assertTrue( "Cookie1 was not cleared", cookie1Cleared );
			assertFalse( "Cookie3 was cleared", cookie3Cleared );
		}
		finally
		{
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
			verify( response );
			verify( spep );
			verify( chain );
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test
	public void testDoFilterAuthenticatedPermit() throws Exception
	{
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		Cookie sessionCookie = new Cookie( this.spepTokenName, this.sessionID );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{sessionCookie} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( this.requestURI ).anyTimes();
		expect( request.getQueryString() ).andReturn( null ).anyTimes();
		
		HttpSession session = createMock( HttpSession.class );
		expect( request.getSession() ).andReturn( session ).anyTimes();
		
		Capture<Object> captureAttributes = new Capture<Object>();
		session.setAttribute( eq(this.attributesName), capture( captureAttributes ) );
		expectLastCall().once();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).anyTimes();
		expect( spep.getTokenName() ).andReturn( this.spepTokenName ).anyTimes();
		expect( spep.getLogoutClearCookies() ).andReturn( new Vector<Cookie>() ).anyTimes();
		
		AuthnProcessor authnProcessor = createMock( AuthnProcessor.class );
		expect( spep.getAuthnProcessor() ).andReturn( authnProcessor ).anyTimes();
		
		PrincipalSession principalSession = createMock( PrincipalSession.class );
		expect( authnProcessor.verifySession( this.sessionID ) ).andReturn( principalSession ).anyTimes();
		
		expect( principalSession.getAttributes() ).andReturn( this.attributes );
		
		PolicyEnforcementProcessor policyEnforcementProcessor = createMock( PolicyEnforcementProcessor.class );
		expect( spep.getPolicyEnforcementProcessor() ).andReturn( policyEnforcementProcessor ).anyTimes();
		
		expect( policyEnforcementProcessor.makeAuthzDecision( eq( sessionID ), eq( this.requestURI ) ) ).andReturn( decision.permit ).once();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		chain.doFilter( eq( request ), eq( response ) );
		expectLastCall().once();
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		replay( authnProcessor );
		replay( principalSession );
		replay( policyEnforcementProcessor );
		replay( session );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
			
			assertEquals( 1, captureAttributes.getCaptured().size() );
			Object o = captureAttributes.getCaptured().get( 0 );
			assertTrue( o instanceof Map );
			Map<String,List<Object>> attributeMap = (Map)o;
			
			for ( Map.Entry<String,List<Object>> entry : this.attributes.entrySet() )
			{
				assertTrue( attributeMap.containsKey(entry.getKey()) );
				assertTrue( attributeMap.get( entry.getKey()).containsAll( entry.getValue() ) );
				assertTrue( entry.getValue().containsAll( attributeMap.get( entry.getKey()) ) );
			}
			
			for( Map.Entry<String,List<Object>> entry : attributeMap.entrySet() )
			{
				assertTrue( this.attributes.containsKey( entry.getKey() ) );
			}
		}
		finally
		{
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
			verify( response );
			verify( spep );
			verify( chain );
			verify( authnProcessor );
			verify( principalSession );
			verify( policyEnforcementProcessor );
			verify( session );
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test
	public void testDoFilterAuthzURL() throws Exception
	{
		String requestURI = "/spep/test%20page.jsp";
		String queryString = "test=moo&asdf%20lol=%2fadmin%2fx";
		String decodedResource = "/spep/test page.jsp?test=moo&asdf lol=/admin/x";
		
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		Cookie sessionCookie = new Cookie( this.spepTokenName, this.sessionID );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{sessionCookie} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( requestURI ).anyTimes();
		expect( request.getQueryString() ).andReturn( queryString ).anyTimes();
		
		HttpSession session = createMock( HttpSession.class );
		expect( request.getSession() ).andReturn( session ).anyTimes();
		
		Capture<Object> captureAttributes = new Capture<Object>();
		session.setAttribute( eq(this.attributesName), capture( captureAttributes ) );
		expectLastCall().once();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).anyTimes();
		expect( spep.getTokenName() ).andReturn( this.spepTokenName ).anyTimes();
		expect( spep.getLogoutClearCookies() ).andReturn( new Vector<Cookie>() ).anyTimes();
		
		AuthnProcessor authnProcessor = createMock( AuthnProcessor.class );
		expect( spep.getAuthnProcessor() ).andReturn( authnProcessor ).anyTimes();
		
		PrincipalSession principalSession = createMock( PrincipalSession.class );
		expect( authnProcessor.verifySession( this.sessionID ) ).andReturn( principalSession ).anyTimes();
		
		expect( principalSession.getAttributes() ).andReturn( this.attributes );
		
		PolicyEnforcementProcessor policyEnforcementProcessor = createMock( PolicyEnforcementProcessor.class );
		expect( spep.getPolicyEnforcementProcessor() ).andReturn( policyEnforcementProcessor ).anyTimes();
		
		expect( policyEnforcementProcessor.makeAuthzDecision( eq( sessionID ), eq( decodedResource ) ) ).andReturn( decision.permit ).once();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		chain.doFilter( eq( request ), eq( response ) );
		expectLastCall().once();
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		replay( authnProcessor );
		replay( principalSession );
		replay( policyEnforcementProcessor );
		replay( session );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
			
			assertEquals( 1, captureAttributes.getCaptured().size() );
			Object o = captureAttributes.getCaptured().get( 0 );
			assertTrue( o instanceof Map );
			Map<String,List<Object>> attributeMap = (Map)o;
			
			for ( Map.Entry<String,List<Object>> entry : this.attributes.entrySet() )
			{
				assertTrue( attributeMap.containsKey(entry.getKey()) );
				assertTrue( attributeMap.get( entry.getKey()).containsAll( entry.getValue() ) );
				assertTrue( entry.getValue().containsAll( attributeMap.get( entry.getKey()) ) );
			}
			
			for( Map.Entry<String,List<Object>> entry : attributeMap.entrySet() )
			{
				assertTrue( this.attributes.containsKey( entry.getKey() ) );
			}
		}
		finally
		{
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
			verify( response );
			verify( spep );
			verify( chain );
			verify( authnProcessor );
			verify( principalSession );
			verify( policyEnforcementProcessor );
			verify( session );
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test
	public void testDoFilterAuthenticatedDeny() throws Exception
	{
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		Cookie sessionCookie = new Cookie( this.spepTokenName, this.sessionID );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{sessionCookie} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( this.requestURI ).anyTimes();
		expect( request.getQueryString() ).andReturn( null ).anyTimes();
		
		response.setStatus( eq( HttpServletResponse.SC_FORBIDDEN) );
		expectLastCall().atLeastOnce();
		response.sendError( eq( HttpServletResponse.SC_FORBIDDEN) );
		expectLastCall().atLeastOnce();
		
		HttpSession session = createMock( HttpSession.class );
		expect( request.getSession() ).andReturn( session ).anyTimes();
		
		Capture<Object> captureAttributes = new Capture<Object>();
		session.setAttribute( eq(this.attributesName), capture( captureAttributes ) );
		expectLastCall().once();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).anyTimes();
		expect( spep.getTokenName() ).andReturn( this.spepTokenName ).anyTimes();
		expect( spep.getLogoutClearCookies() ).andReturn( new Vector<Cookie>() ).anyTimes();
		
		AuthnProcessor authnProcessor = createMock( AuthnProcessor.class );
		expect( spep.getAuthnProcessor() ).andReturn( authnProcessor ).anyTimes();
		
		PrincipalSession principalSession = createMock( PrincipalSession.class );
		expect( authnProcessor.verifySession( this.sessionID ) ).andReturn( principalSession ).anyTimes();
		
		expect( principalSession.getAttributes() ).andReturn( this.attributes );
		
		PolicyEnforcementProcessor policyEnforcementProcessor = createMock( PolicyEnforcementProcessor.class );
		expect( spep.getPolicyEnforcementProcessor() ).andReturn( policyEnforcementProcessor ).anyTimes();
		
		expect( policyEnforcementProcessor.makeAuthzDecision( eq( sessionID ), eq( this.requestURI ) ) ).andReturn( decision.deny ).once();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		replay( authnProcessor );
		replay( principalSession );
		replay( policyEnforcementProcessor );
		replay( session );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
			
			assertEquals( 1, captureAttributes.getCaptured().size() );
			Object o = captureAttributes.getCaptured().get( 0 );
			assertTrue( o instanceof Map );
			Map<String,List<Object>> attributeMap = (Map)o;
			
			for ( Map.Entry<String,List<Object>> entry : this.attributes.entrySet() )
			{
				assertTrue( attributeMap.containsKey(entry.getKey()) );
				assertTrue( attributeMap.get( entry.getKey()).containsAll( entry.getValue() ) );
				assertTrue( entry.getValue().containsAll( attributeMap.get( entry.getKey()) ) );
			}
			
			for( Map.Entry<String,List<Object>> entry : attributeMap.entrySet() )
			{
				assertTrue( this.attributes.containsKey( entry.getKey() ) );
			}
		}
		finally
		{
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
			verify( response );
			verify( spep );
			verify( chain );
			verify( authnProcessor );
			verify( principalSession );
			verify( policyEnforcementProcessor );
			verify( session );
		}
	}

	/**
	 * Test method for {@link com.qut.middleware.spep.filter.SPEPFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)}.
	 */
	@Test( expected = ServletException.class )
	public void testDoFilterAuthenticatedError() throws Exception
	{
		FilterConfig filterConfig = createMock( FilterConfig.class );
		expect( filterConfig.getInitParameter( this.spepContextName ) ).andReturn( this.spepContext ).once();
		
		ServletContext servletContext = createMock( ServletContext.class );
		expect( filterConfig.getServletContext() ).andReturn( servletContext ).anyTimes();
		
		ServletContext spepServletContext = createMock( ServletContext.class );
		expect( servletContext.getContext( this.spepContext ) ).andReturn( spepServletContext ).anyTimes();

		HttpServletRequest request = createMock( HttpServletRequest.class );
		HttpServletResponse response = createMock( HttpServletResponse.class );
		
		Cookie sessionCookie = new Cookie( this.spepTokenName, this.sessionID );
		
		expect( request.getCookies() ).andReturn( new Cookie[]{sessionCookie} ).anyTimes();
		expect( request.getRequestURI() ).andReturn( this.requestURI ).anyTimes();
		expect( request.getQueryString() ).andReturn( null ).anyTimes();
		
		response.setStatus( eq( HttpServletResponse.SC_INTERNAL_SERVER_ERROR) );
		expectLastCall().atLeastOnce();
		
		HttpSession session = createMock( HttpSession.class );
		expect( request.getSession() ).andReturn( session ).anyTimes();
		
		Capture<Object> captureAttributes = new Capture<Object>();
		session.setAttribute( eq(this.attributesName), capture( captureAttributes ) );
		expectLastCall().once();
		
		SPEP spep = createMock( SPEP.class );
		expect( spepServletContext.getAttribute( eq(ConfigurationConstants.SERVLET_CONTEXT_NAME) ) ).andReturn( spep ).anyTimes();
		
		expect( spep.isStarted() ).andReturn( Boolean.TRUE ).anyTimes();
		expect( spep.getTokenName() ).andReturn( this.spepTokenName ).anyTimes();
		expect( spep.getLogoutClearCookies() ).andReturn( new Vector<Cookie>() ).anyTimes();
		
		AuthnProcessor authnProcessor = createMock( AuthnProcessor.class );
		expect( spep.getAuthnProcessor() ).andReturn( authnProcessor ).anyTimes();
		
		PrincipalSession principalSession = createMock( PrincipalSession.class );
		expect( authnProcessor.verifySession( this.sessionID ) ).andReturn( principalSession ).anyTimes();
		
		expect( principalSession.getAttributes() ).andReturn( this.attributes );
		
		PolicyEnforcementProcessor policyEnforcementProcessor = createMock( PolicyEnforcementProcessor.class );
		expect( spep.getPolicyEnforcementProcessor() ).andReturn( policyEnforcementProcessor ).anyTimes();
		
		expect( policyEnforcementProcessor.makeAuthzDecision( eq( sessionID ), eq( this.requestURI ) ) ).andReturn( decision.error ).once();
		
		SPEPFilter spepFilter = new SPEPFilter();
		
		FilterChain chain = createMock( FilterChain.class );
		
		replay( filterConfig );
		replay( servletContext );
		replay( spepServletContext );
		replay( request );
		replay( response );
		replay( spep );
		replay( chain );
		replay( authnProcessor );
		replay( principalSession );
		replay( policyEnforcementProcessor );
		replay( session );
		
		try
		{
			spepFilter.init( filterConfig );
			
			spepFilter.doFilter( request, response, chain );
			
			assertEquals( 1, captureAttributes.getCaptured().size() );
			Object o = captureAttributes.getCaptured().get( 0 );
			assertTrue( o instanceof Map );
			Map<String,List<Object>> attributeMap = (Map)o;
			
			for ( Map.Entry<String,List<Object>> entry : this.attributes.entrySet() )
			{
				assertTrue( attributeMap.containsKey(entry.getKey()) );
				assertTrue( attributeMap.get( entry.getKey()).containsAll( entry.getValue() ) );
				assertTrue( entry.getValue().containsAll( attributeMap.get( entry.getKey()) ) );
			}
			
			for( Map.Entry<String,List<Object>> entry : attributeMap.entrySet() )
			{
				assertTrue( this.attributes.containsKey( entry.getKey() ) );
			}
		}
		finally
		{
			verify( filterConfig );
			verify( servletContext );
			verify( spepServletContext );
			verify( request );
			verify( response );
			verify( spep );
			verify( chain );
			verify( authnProcessor );
			verify( principalSession );
			verify( policyEnforcementProcessor );
			verify( session );
		}
	}

}
