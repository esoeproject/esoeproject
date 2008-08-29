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
 * Creation Date: Jan 24, 2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.authn.plugins.statgen.test;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Test;

import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.plugins.statgen.handler.StatsGenerationHandler;

public class StatsGenerationHandlerTest
{

	@Test
	public void testExecute() throws SessionCreationException
	{
		StatsGenerationHandler handler = new StatsGenerationHandler();
		
		AuthnProcessorData data = createMock( AuthnProcessorData.class );
		
		HttpServletRequest req = createMock( HttpServletRequest.class );
		
		HttpSession sess = createMock( HttpSession.class );
		
		Locale locale = new Locale( "en" );

		expect( data.getHttpRequest() ).andReturn( req ).anyTimes();
		expect( req.getSession() ).andReturn( sess ).anyTimes();
		expect( data.getPrincipalName() ).andReturn( "principal" ).anyTimes();
		expect( data.getSessionID() ).andReturn( "sessionID" ).anyTimes();
		expect( req.getRemoteAddr() ).andReturn( "remoteAddr" ).anyTimes();
		expect( req.getRemoteHost() ).andReturn( "remoteHost" ).anyTimes();
		expect( req.getRemotePort() ).andReturn( Integer.valueOf( 15 ) ).anyTimes();
		expect( req.getHeader("User-Agent") ).andReturn( "User-Agent" ).anyTimes();
		expect( req.getHeader("Referer") ).andReturn( "referer" ).anyTimes();
		expect( req.isRequestedSessionIdFromCookie() ).andReturn( Boolean.TRUE ).anyTimes();
		expect( req.isRequestedSessionIdFromURL() ).andReturn( Boolean.FALSE ).anyTimes();
		expect( req.getCharacterEncoding() ).andReturn( "encoding" ).anyTimes();
		expect( req.getContentType() ).andReturn( "contentType" ).anyTimes();
		expect( req.getLocale() ).andReturn( locale ).anyTimes();
		expect( req.getProtocol() ).andReturn( "protocol" ).anyTimes();
		
		replay( data );
		replay( req );
		replay( sess );
		
		handler.execute( data );
		
		verify( data );
		verify( req );
		verify( sess );
	}

	@Test
	public void testGetHandlerName()
	{
		StatsGenerationHandler handler = new StatsGenerationHandler();
		
		assertNotNull( handler.getHandlerName() );
	}

}
