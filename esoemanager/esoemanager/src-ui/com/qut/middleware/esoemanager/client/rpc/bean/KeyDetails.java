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
package com.qut.middleware.esoemanager.client.rpc.bean;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class KeyDetails implements IsSerializable
{
	String keypairName;
	String keypairPassphrase;
	Date expiryDate;
	String keystorePassphrase;
	
	boolean expireWarn;
	boolean expireError;
	
	/**
	 * @return the expireWarn
	 */
	public boolean isExpireWarn()
	{
		return expireWarn;
	}
	/**
	 * @param expireWarn the expireWarn to set
	 */
	public void setExpireWarn(boolean expireWarn)
	{
		this.expireWarn = expireWarn;
	}
	/**
	 * @return the expireError
	 */
	public boolean isExpireError()
	{
		return expireError;
	}
	/**
	 * @param expireError the expireError to set
	 */
	public void setExpireError(boolean expireError)
	{
		this.expireError = expireError;
	}
	/**
	 * @return the keypairName
	 */
	public String getKeypairName()
	{
		return keypairName;
	}
	/**
	 * @param keypairName the keypairName to set
	 */
	public void setKeypairName(String keypairName)
	{
		this.keypairName = keypairName;
	}
	/**
	 * @return the keypairPassphrase
	 */
	public String getKeypairPassphrase()
	{
		return keypairPassphrase;
	}
	/**
	 * @param keypairPassphrase the keypairPassphrase to set
	 */
	public void setKeypairPassphrase(String keypairPassphrase)
	{
		this.keypairPassphrase = keypairPassphrase;
	}
	/**
	 * @return the expiryDate
	 */
	public Date getExpiryDate()
	{
		return expiryDate;
	}
	/**
	 * @param expiryDate the expiryDate to set
	 */
	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}
	/**
	 * @return the keystorePassphrase
	 */
	public String getKeystorePassphrase()
	{
		return keystorePassphrase;
	}
	/**
	 * @param keystorePassphrase the keystorePassphrase to set
	 */
	public void setKeystorePassphrase(String keystorePassphrase)
	{
		this.keystorePassphrase = keystorePassphrase;
	}
}
