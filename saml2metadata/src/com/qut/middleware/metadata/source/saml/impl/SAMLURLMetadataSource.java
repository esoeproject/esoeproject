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
 * Creation Date: 16/06/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.source.saml.impl;

import java.net.URL;

import com.qut.middleware.metadata.constants.FormatConstants;
import com.qut.middleware.metadata.source.impl.URLMetadataSource;

public class SAMLURLMetadataSource extends URLMetadataSource
{
	public SAMLURLMetadataSource(URL url)
	{
		super(url);
	}
	
	public String getFormat()
	{
		return FormatConstants.SAML2;
	}

}
