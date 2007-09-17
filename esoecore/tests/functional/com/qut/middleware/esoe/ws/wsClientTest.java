package com.qut.middleware.esoe.ws;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.esoe.ws.impl.WSClientImpl;

/** These tests are currently only for coverage reports and are not terribly useful
 * as the axis client cannot currently be created. Should be setup as a system test.
 *
 */
@SuppressWarnings("nls")
public class wsClientTest {

	private WSClient client;
	
	@Before
	public void setUp() throws Exception 
	{
		this.client = new WSClientImpl();		
	}
	
	@Test
	public void testAuthzCacheClear() 
	{
		byte[] authzCacheClearReq = new String("<hello></hello>").getBytes();
		String endpoint = "https://spep-linux.esoe-dev.qut.edu.au:8443/spep/services/spep/authzCacheClear";
		
		try
		{
			this.client.authzCacheClear(authzCacheClearReq, endpoint);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}

	//@Test
	public void testSingleLogout() 
	{
		byte[] logoutReq = new String("<hello></hello>").getBytes();
		String endpoint = "https://spep-linux.esoe-dev.qut.edu.au:8443/spep/services/spep/singleLogout";
		
		try
		{
			this.client.authzCacheClear(logoutReq, endpoint);
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
	}

}
