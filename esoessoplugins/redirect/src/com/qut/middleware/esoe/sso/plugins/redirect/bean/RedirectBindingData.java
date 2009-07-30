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
 * Creation Date: 26/08/2008
 *
 * Purpose:
 */

package com.qut.middleware.esoe.sso.plugins.redirect.bean;

public class RedirectBindingData
{
	private String signature;
	private String sigAlg;
	private String encoding;
	private String relayState;
	private String samlRequestString;

	public String getRelayState()
	{
		return this.relayState;
	}

	public void setRelayState(String relayState)
	{
		this.relayState = relayState;
	}

	public void setRequestEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public String getRequestEncoding()
	{
		return encoding;
	}

	public void setSignatureAlgorithm(String sigAlg)
	{
		this.sigAlg = sigAlg;
	}

	public String getSignatureAlgorithm()
	{
		return sigAlg;
	}

	public String getSignature()
	{
		return signature;
	}

	public void setSignature(String signature)
	{
		this.signature = signature;
	}

	public void setSAMLRequestString(String samlRequestString) {
		this.samlRequestString = samlRequestString;
	}

	public String getSAMLRequestString()
	{
		return samlRequestString;
	}
}
