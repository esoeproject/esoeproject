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
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 18/09/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.saml2.handler;

import java.util.Collection;

import org.w3c.dom.Element;

import com.qut.middleware.saml2.exception.SOAPException;

public interface SOAPHandler
{
	public enum FaultCode
	{
		Receiver,
		Sender,
		VersionMismatch,
		MustUnderstand,
		DataEncodingUnknown;
		
		private String oldName;
		
		private FaultCode()
		{
			this.oldName = null;
		}
		/**
		 * Constructor specifying the name of the fault code in SOAP 1.1
		 */
		private FaultCode(String oldName)
		{
			this.oldName = oldName;
		}
		
		public String getOldName()
		{
			if (this.oldName != null) return oldName;
			
			return this.name();
		}
	}
	
	public boolean canHandle(String contentType);
	public byte[] wrapDocument(Element samlDocument) throws SOAPException;
	public byte[] wrapDocument(Element samlDocument, String encoding) throws SOAPException;
	public byte[] generateFaultResponse(String reason, FaultCode faultCode, String subCode, Collection<Element> detailElements, String encoding) throws SOAPException;
	public Element unwrapDocument(byte[] soapDocument) throws SOAPException;
	public String getContentType(String encoding);
	public String getDefaultEncoding();
}
