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
 * Creation Date: 25/10/2006
 * 
 * Purpose: Handles validation events created by jaxp validator
 */
package com.qut.middleware.saml2.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Handles validation events created by jaxp validator. */
public class MarshallerValidationHandler implements ErrorHandler
{

	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(MarshallerValidationHandler.class.getName());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException spe) throws SAXException
	{
		/*
		 * Due to implementation expect to get a validation error for an uncomplete <ds:Signature> element Allow this to
		 * pass, flag everything else as an exception
		 */
		if (!spe.getMessage().contains("ds:Signature")) //$NON-NLS-1$
		{
			this.logger.error(Messages.getString("MarshallerValidationHandler.0") + spe.getLocalizedMessage()); //$NON-NLS-1$
			this.logger.debug(spe.getLocalizedMessage(), spe.getException());
			throw new SAXException(
					Messages.getString("MarshallerValidationHandler.2") + spe.getLineNumber() + Messages.getString("MarshallerValidationHandler.3") + spe.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException spe) throws SAXException
	{
		this.logger.error(Messages.getString("MarshallerValidationHandler.4") + spe.getLocalizedMessage()); //$NON-NLS-1$
		this.logger.debug(spe.getLocalizedMessage(), spe.getException());
		throw new SAXException(Messages.getString("MarshallerValidationHandler.5") + spe.getLineNumber() + Messages.getString("MarshallerValidationHandler.6") + spe.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException spe) throws SAXException
	{
		this.logger.error(Messages.getString("MarshallerValidationHandler.1") + spe.getLocalizedMessage()); //$NON-NLS-1$
		this.logger.debug(spe.getLocalizedMessage(), spe.getException());
		throw new SAXException(Messages.getString("MarshallerValidationHandler.7") + spe.getLineNumber() + Messages.getString("MarshallerValidationHandler.8") + spe.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}