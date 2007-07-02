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
 * Author: Bradley Beddoes
 * Creation Date: 25/10/2006
 * 
 * Purpose: Ensures marshalled XML documents are created with namespaces that have specific meaning to humans instead of generic ns1, ns2...
 */

package com.qut.middleware.saml2.namespace;

import org.apache.log4j.Logger;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/** Ensures marshalled XML documents are created with namespaces that have specific meaning to humans instead of generic ns1, ns2... */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper
{
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(NamespacePrefixMapperImpl.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String, java.lang.String,
	 *      boolean)
	 */
	@Override
	public String getPreferredPrefix(String namespaceURI, String suggested, boolean requirePrefix)
	{
		if (namespaceURI == null)
		{
			throw new IllegalArgumentException(Messages.getString("NamespacePrefixMapperImpl.0")); //$NON-NLS-1$
		}

		/* Define all of our custom mappings */
		if (namespaceURI.equals("http://www.w3.org/2001/XMLSchema")) //$NON-NLS-1$
		{
			return "xs"; //$NON-NLS-1$
		}
			
		if (namespaceURI.equals("http://www.w3.org/2001/XMLSchema-instance")) //$NON-NLS-1$
		{
			return "xsi"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.w3.org/2000/09/xmldsig#")) //$NON-NLS-1$
		{
			return "ds"; //$NON-NLS-1$
		}
		
		if (namespaceURI.equals("http://www.w3.org/2001/04/xmlenc#")) //$NON-NLS-1$
		{
			return "xenc"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("urn:oasis:names:tc:SAML:2.0:protocol")) //$NON-NLS-1$
		{
			return "samlp"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("urn:oasis:names:tc:SAML:2.0:assertion")) //$NON-NLS-1$
		{
			return "saml"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("urn:oasis:names:tc:SAML:2.0:metadata")) //$NON-NLS-1$
		{
			return "md"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/SessionDataSchema")) //$NON-NLS-1$
		{
			return "session"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/lxacmlSchema")) //$NON-NLS-1$
		{
			return "lxacml"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/lxacmlSAMLProtocolSchema")) //$NON-NLS-1$
		{
			return "lxacmlp"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/lxacmlSAMLAssertionSchema")) //$NON-NLS-1$
		{
			return "lxacmla"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/lxacmlGroupTargetSchema")) //$NON-NLS-1$
		{
			return "group"; //$NON-NLS-1$
		}
		
		if (namespaceURI.equals("http://www.qut.com/middleware/lxacmlPDPSchema")) //$NON-NLS-1$
		{
			return "lxacml-md"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/lxacmlContextSchema")) //$NON-NLS-1$
		{
			return "lxacml-context"; //$NON-NLS-1$
		}

		if (namespaceURI.equals("http://www.qut.com/middleware/ESOEProtocolSchema")) //$NON-NLS-1$
		{
			return "esoe"; //$NON-NLS-1$
		}
		
		if (namespaceURI.equals("http://www.qut.com/middleware/cacheClearServiceSchema")) //$NON-NLS-1$
		{
			return "clear"; //$NON-NLS-1$
		}
		
		if (namespaceURI.equals("http://www.qut.com/middleware/spepStartupServiceSchema")) //$NON-NLS-1$
		{
			return "spep"; //$NON-NLS-1$
		}
		
		this.logger.debug("Prefix mapper has no mapping for the presented namespaceURI:" + namespaceURI); //$NON-NLS-1$

		/* No preference for this namespace, use default */
		return suggested;

	}
}
