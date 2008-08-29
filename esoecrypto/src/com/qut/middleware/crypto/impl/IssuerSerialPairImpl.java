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
 * Creation Date: 06/08/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.crypto.impl;

import java.math.BigInteger;

import com.qut.middleware.crypto.IssuerSerialPair;

public class IssuerSerialPairImpl implements IssuerSerialPair
{
	private String issuerDN;
	private BigInteger serialNumber;

	public IssuerSerialPairImpl(String issuerDN, BigInteger serialNumber)
	{
		this.issuerDN = issuerDN;
		this.serialNumber = serialNumber;
	}

	public String getIssuerDN()
	{
		return issuerDN;
	}

	public BigInteger getSerialNumber()
	{
		return serialNumber;
	}
	
	@Override
	public int hashCode()
	{
		return (this.issuerDN.hashCode() / 2) + (this.serialNumber.hashCode() / 2);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof IssuerSerialPair)) return false;
		IssuerSerialPair other = (IssuerSerialPair) obj;
		
		return (issuerDN.equals(other.getIssuerDN()) && serialNumber.equals(other.getSerialNumber()));
	}
	
	public int compareTo(IssuerSerialPair o)
	{
		int result;
		result = this.issuerDN.compareTo(o.getIssuerDN());
		if (result == 0) result = this.serialNumber.compareTo(o.getSerialNumber());
		
		return result;
	}
}
