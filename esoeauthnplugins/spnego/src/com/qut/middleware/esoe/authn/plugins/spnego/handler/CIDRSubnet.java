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

package com.qut.middleware.esoe.authn.plugins.spnego.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CIDRSubnet
{
	private byte[] addressValue;
	private byte[] maskValue;

	public CIDRSubnet(String cidr)
	{
		try
		{
			// If there's no prefix length, make it 32 bit so it refers to a single host.
			if (!cidr.contains("/"))
			{
				cidr = cidr + "/32";
			}

			// Split at / character, also removing whitespace around it
			String[] value = cidr.trim().split("\\s*/\\s*", 2);

			if (value.length != 2)
			{
				throw new IllegalArgumentException("String was not valid CIDR notation (couldn't split to network/prefix length components): " + cidr);
			}

			InetAddress networkAddress = InetAddress.getByName(value[0]);
			int prefixLength = Integer.parseInt(value[1]);
			
			if (prefixLength < 0 || prefixLength > 32)
			{
				throw new IllegalArgumentException("String was not valid CIDR notation (prefix length was not in the range [0,32]): " + cidr);
			}
			
			/* This magic was "borrowed" from
			 * http://svn.apache.org/repos/asf/james/server/trunk/core-library/src/main/java/org/apache/james/util/NetMatcher.java
			 */
			final int bits = 32 - prefixLength;
	        final int mask = (bits == 32) ? 0 : 0xFFFFFFFF - ((1 << bits)-1);
			this.maskValue = new byte[4];
			this.maskValue[0] = (byte) (mask >> 24 & 0xFF);
			this.maskValue[1] = (byte) (mask >> 16 & 0xFF);
			this.maskValue[2] = (byte) (mask >> 8 & 0xFF);
			this.maskValue[3] = (byte) (mask >> 0 & 0xFF);

			// We need to mask the address value before we store it too
			this.addressValue = maskAddress(networkAddress.getAddress(), this.maskValue);
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("String was not valid CIDR notation (prefix length was not in the correct format): " + cidr);
		}
		catch (UnknownHostException e)
		{
			throw new IllegalArgumentException("String was not valid CIDR notation (network address was not in the correct format): " + cidr);
		}
	}

	private byte[] maskAddress(byte[] address, byte[] mask)
	{
		byte[] retval = new byte[4];
		// Mask each byte in sequence using bitwise AND.
		for (int i = 0; i < 4; ++i)
		{
			retval[i] = (byte) (address[i] & mask[i]);
		}
		return retval;
	}

	public boolean contains(InetAddress address)
	{
		// Mask the target address, then check for equality on each byte.
		byte[] addressBytes = maskAddress(address.getAddress(), this.maskValue);
		for (int i = 0; i < 4; ++i)
		{
			if (addressBytes[i] != this.addressValue[i]) return false;
		}
		return true;
	}
}
