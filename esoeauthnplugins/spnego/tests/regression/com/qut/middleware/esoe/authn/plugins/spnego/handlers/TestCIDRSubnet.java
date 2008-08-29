/* Copyright 2006, Queensland University of Technology
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
 * Creation Date: 25/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.authn.plugins.spnego.handlers;

import java.net.InetAddress;

import org.junit.Test;

import static org.junit.Assert.*;

import com.qut.middleware.esoe.authn.plugins.spnego.handler.CIDRSubnet;


public class TestCIDRSubnet
{
	@Test
	public void testCIDRSubnet1() throws Exception
	{
		CIDRSubnet subnet = new CIDRSubnet("1.2.3.4/16");
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.4")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.16.4")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.255.4")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.43")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.255")));
		assertFalse(subnet.contains(InetAddress.getByName("2.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("255.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("2.128.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("2.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("2.255.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("0.0.3.4")));
	}
	
	@Test
	public void testCIDRSubnet2() throws Exception
	{
		CIDRSubnet subnet = new CIDRSubnet("1.2.3.4/1");
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.4")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.16.4")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.255.4")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.43")));
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.255")));
		assertTrue(subnet.contains(InetAddress.getByName("2.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("255.1.3.4")));
		assertTrue(subnet.contains(InetAddress.getByName("2.128.3.4")));
		assertTrue(subnet.contains(InetAddress.getByName("2.1.3.4")));
		assertTrue(subnet.contains(InetAddress.getByName("2.255.3.4")));
		assertTrue(subnet.contains(InetAddress.getByName("0.0.3.4")));
	}
	
	@Test
	public void testCIDRSubnet3() throws Exception
	{
		CIDRSubnet subnet = new CIDRSubnet("1.2.3.4/32");
		assertTrue(subnet.contains(InetAddress.getByName("1.2.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("1.2.16.4")));
		assertFalse(subnet.contains(InetAddress.getByName("1.2.255.4")));
		assertFalse(subnet.contains(InetAddress.getByName("1.2.3.43")));
		assertFalse(subnet.contains(InetAddress.getByName("1.2.3.255")));
		assertFalse(subnet.contains(InetAddress.getByName("2.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("255.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("2.128.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("2.1.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("2.255.3.4")));
		assertFalse(subnet.contains(InetAddress.getByName("0.0.3.4")));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCIDR1() throws Exception
	{
		new CIDRSubnet("1.2.3.4/");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCIDR2() throws Exception
	{
		new CIDRSubnet("1.2.3.4/33");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCIDR3() throws Exception
	{
		new CIDRSubnet("1.2.3.4/-1");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCIDR4() throws Exception
	{
		new CIDRSubnet("x.2.3.4/14");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCIDR5() throws Exception
	{
		new CIDRSubnet("1.2.3.4/x");
	}
}
