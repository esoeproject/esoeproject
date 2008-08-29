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
package com.qut.middleware.esoemanager.util;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.saml2.schemas.metadata.AttributeAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.AuthnAuthorityDescriptor;
import com.qut.middleware.saml2.schemas.metadata.IDPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.PDPDescriptor;
import com.qut.middleware.saml2.schemas.metadata.SPSSODescriptor;
import com.qut.middleware.saml2.schemas.metadata.lxacml.LXACMLPDPDescriptor;

public class UtilFunctions
{
	public void prettyPrintXML(byte[] document, OutputStream out) throws Exception
	{
		DocumentBuilderFactory docBuildFac = DocumentBuilderFactory.newInstance();
		docBuildFac.setNamespaceAware(true);
		docBuildFac.setValidating(false);
		docBuildFac.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.FALSE); //$NON-NLS-1$

		DocumentBuilder docBuilder = docBuildFac.newDocumentBuilder();
		Document doc = docBuilder.parse(new ByteArrayInputStream(document));
		
		OutputFormat format = new OutputFormat(doc);
		format.setLineWidth(81);
		format.setIndenting(true);
		format.setIndent(2);
		format.setEncoding("UTF-16");
		format.setOmitDocumentType(true);
		format.setOmitXMLDeclaration(true);
		XMLSerializer serializer = new XMLSerializer(out, format);
		serializer.serialize(doc);
	}
	
	/**
	 * Returns RoleDescriptorType class name for corresponding database id
	 * 
	 * @param idNumber
	 * @return
	 */
	public String getRoleDescriptorType(String idNumber)
	{
		if (idNumber.equals(Constants.IDP_DESCRIPTOR)) //$NON-NLS-1$
		{
			return IDPSSODescriptor.class.getCanonicalName();
		}
		else
			if (idNumber.equals(Constants.SP_DESCRIPTOR)) //$NON-NLS-1$
			{
				return SPSSODescriptor.class.getCanonicalName();
			}
			else
				if (idNumber.equals(Constants.LXACML_PDP_DESCRIPTOR)) //$NON-NLS-1$
				{
					return PDPDescriptor.class.getCanonicalName();
				}
				else
					if (idNumber.equals(Constants.AUTHN_AUTHORITY_DESCRIPTOR)) //$NON-NLS-1$
					{
						return AuthnAuthorityDescriptor.class.getCanonicalName();
					}
					else
						if (idNumber.equals(Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR)) //$NON-NLS-1$
						{
							return AttributeAuthorityDescriptor.class.getCanonicalName();
						}
						else
						{
							return null;
						}
	}

	/**
	 * Returns the database id number for any RoleDescriptorType
	 * 
	 * @param className
	 *            of the descriptor being used
	 * @return Value of the descriptor object, or null on error
	 */
	public String getRoleDescriptorTypeId(String className)
	{
		if (className.equalsIgnoreCase(IDPSSODescriptor.class.getCanonicalName()))
		{
			return Constants.IDP_DESCRIPTOR;
		}
		else
			if (className.equalsIgnoreCase(SPSSODescriptor.class.getCanonicalName()))
			{
				return Constants.SP_DESCRIPTOR;
			}
			else
				if (className.equalsIgnoreCase(PDPDescriptor.class.getCanonicalName()))
				{
					return Constants.LXACML_PDP_DESCRIPTOR;
				}
				else
					if (className.equalsIgnoreCase(LXACMLPDPDescriptor.class.getCanonicalName()))
					{
						return Constants.LXACML_PDP_DESCRIPTOR;
					}
					else
						if (className.equalsIgnoreCase(AuthnAuthorityDescriptor.class.getCanonicalName()))
						{
							return Constants.AUTHN_AUTHORITY_DESCRIPTOR;
						}
						else
							if (className.equalsIgnoreCase(AttributeAuthorityDescriptor.class.getCanonicalName()))
							{
								return Constants.ATTRIBUTE_AUTHORITY_DESCRIPTOR;
							}
		return null;
	}
}
