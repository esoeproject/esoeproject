/* 
 * Copyright 2006, Queensland University of Technology
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
 * Creation Date: 17/11/2006
 * 
 * Purpose: Resolves schema resources locally for the SAML2lib-j library
 */
package com.qut.middleware.saml2.resolver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.qut.middleware.saml2.exception.ResourceException;

/** Resolves schema resources locally for the SAML2lib-j library. */
public class ResourceResolver implements LSResourceResolver
{
	/* Filenames to load upon creation and retrieve for resolver */
	private final String XMLDSIG = "xmldsig-core-schema.xsd"; //$NON-NLS-1$
	private final String XMLENC = "xenc-schema.xsd"; //$NON-NLS-1$
	private final String XML = "xml.xsd"; //$NON-NLS-1$
	private final String SAML = "saml-schema-assertion-2.0.xsd"; //$NON-NLS-1$
	private final String SAMLP = "saml-schema-protocol-2.0.xsd"; //$NON-NLS-1$
	private final String SAMLM = "saml-schema-metadata-2.0.xsd"; //$NON-NLS-1$
	private final String LXACML = "lxacml-schema.xsd"; //$NON-NLS-1$
	private final String LXACMLM = "lxacml-schema-metadata.xsd"; //$NON-NLS-1$
	private final String LXACMLC = "lxacml-schema-context.xsd"; //$NON-NLS-1$
	private final String LXACMLG = "lxacml-schema-grouptarget.xsd"; //$NON-NLS-1$
	private final String LXACMLA = "lxacml-schema-saml-assertion.xsd"; //$NON-NLS-1$
	private final String LXACMLP = "lxacml-schema-saml-protocol.xsd"; //$NON-NLS-1$
	private final String SESSION = "sessiondata-schema.xsd"; //$NON-NLS-1$
	private final String ESOE = "esoe-schema-saml-protocol.xsd"; //$NON-NLS-1$
	private final String CACHECLEAR = "cacheclear-schema-saml-metadata.xsd"; //$NON-NLS-1$

	/* Local logging instance */
	private Logger logger = Logger.getLogger(ResourceResolver.class.getName());

	private Map<String, LSInput> schemas;

	public ResourceResolver() throws ResourceException
	{
		this.schemas = new HashMap<String, LSInput>();

		/* Pre load all supported schema files */
		loadSchema(this.XMLDSIG);
		loadSchema(this.XMLENC);
		loadSchema(this.XML);
		loadSchema(this.SAML);
		loadSchema(this.SAMLP);
		loadSchema(this.SAMLM);
		loadSchema(this.LXACML);
		loadSchema(this.LXACMLM);
		loadSchema(this.LXACMLC);
		loadSchema(this.LXACMLG);
		loadSchema(this.LXACMLA);
		loadSchema(this.LXACMLP);
		loadSchema(this.SESSION);
		loadSchema(this.ESOE);
		loadSchema(this.CACHECLEAR);
	}

	/**
	 * @param resource
	 *            The name of the schema file to load incl extenstion
	 */
	private void loadSchema(String resource) throws ResourceException
	{
		String str;
		StringBuffer xml;
		Reader reader = null;
		BufferedReader in = null;
		InputStream fileStream = null;

		try
		{
			URL location = SchemaResolver.class.getResource(resource);
			LSInput input = new LSInputImpl();
			if (location == null)
				throw new IllegalArgumentException(
						Messages.getString("ResourceResolver.12") + resource + Messages.getString("ResourceResolver.13") + SchemaResolver.class.getPackage().getName()); //$NON-NLS-1$ //$NON-NLS-2$
			input.setBaseURI(location.getFile());

			xml = new StringBuffer();
			fileStream = location.openStream();
			reader = new InputStreamReader(fileStream);
			in = new BufferedReader(reader);

			while ((str = in.readLine()) != null)
			{
				xml.append(str);
			}

			input.setStringData(xml.toString());

			this.schemas.put(resource, input);
		}
		catch (IOException e)
		{
			this.logger.fatal(Messages.getString("ResourceResolver.9") + resource); //$NON-NLS-1$
			throw new ResourceException(e.getLocalizedMessage(), e);
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();

				if (reader != null)
					reader.close();

				if (fileStream != null)
					fileStream.close();
			}
			catch (IOException e)
			{
				this.logger.fatal("Unable to close file streams when loading schema");
				this.logger.error(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
	{
		if (namespaceURI != null && namespaceURI.equals("http://www.w3.org/2000/09/xmldsig#")) //$NON-NLS-1$
		{
			return this.schemas.get(this.XMLDSIG);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.w3.org/2001/04/xmlenc#")) //$NON-NLS-1$
		{
			return this.schemas.get(this.XMLENC);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.w3.org/XML/1998/namespace")) //$NON-NLS-1$
		{
			return this.schemas.get(this.XML);
		}

		if (namespaceURI != null && namespaceURI.equals("urn:oasis:names:tc:SAML:2.0:assertion")) //$NON-NLS-1$
		{
			return this.schemas.get(this.SAML);
		}

		if (namespaceURI != null && namespaceURI.equals("urn:oasis:names:tc:SAML:2.0:protocol")) //$NON-NLS-1$
		{
			return this.schemas.get(this.SAMLP);
		}

		if (namespaceURI != null && namespaceURI.equals("urn:oasis:names:tc:SAML:2.0:metadata")) //$NON-NLS-1$
		{
			return this.schemas.get(this.SAMLM);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/lxacmlSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.LXACML);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/lxacmlPDPSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.LXACMLM);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/lxacmlContextSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.LXACMLC);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/lxacmlGroupTargetSchema")) //$NON-NLS-1$
		{
			//$NON-NLS-1$
			return this.schemas.get(this.LXACMLG);

		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/lxacmlSAMLAssertionSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.LXACMLA);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/lxacmlSAMLProtocolSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.LXACMLP);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/SessionDataSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.SESSION);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/ESOEProtocolSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.ESOE);
		}

		if (namespaceURI != null && namespaceURI.equals("http://www.qut.com/middleware/cacheClearServiceSchema")) //$NON-NLS-1$
		{
			return this.schemas.get(this.CACHECLEAR);
		}

		this.logger
				.warn(Messages.getString("ResourceResolver.11") + namespaceURI + Messages.getString("ResourceResolver.10")); //$NON-NLS-1$ //$NON-NLS-2$

		/* Resolver doesn't know about this schema so pass back default object */
		LSInput input = new LSInputImpl();
		input.setSystemId(systemId);
		input.setPublicId(publicId);
		input.setBaseURI(baseURI);
		return input;
	}
}
