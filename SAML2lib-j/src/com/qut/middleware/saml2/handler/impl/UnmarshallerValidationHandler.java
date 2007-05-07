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

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Handles validation events created by jaxp validator. */
public class UnmarshallerValidationHandler implements ErrorHandler
{
	
	/* Local logging instance */
	private Logger logger = Logger.getLogger(UnmarshallerValidationHandler.class.getName());
	
	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException spe) throws SAXException
	{
		this.logger.error(Messages.getString("UnmarshallerValidationHandler.6") + spe.getLocalizedMessage()); //$NON-NLS-1$
		this.logger.debug(spe.getLocalizedMessage(), spe.getException());
		throw new SAXException(Messages.getString("UnmarshallerValidationHandler.0") + spe.getLineNumber() + Messages.getString("UnmarshallerValidationHandler.1") + spe.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException spe) throws SAXException
	{
		this.logger.error(Messages.getString("UnmarshallerValidationHandler.7") + spe.getLocalizedMessage()); //$NON-NLS-1$
		this.logger.debug(spe.getLocalizedMessage(), spe.getException());
		throw new SAXException(Messages.getString("UnmarshallerValidationHandler.2") + spe.getLineNumber() + Messages.getString("UnmarshallerValidationHandler.3") + spe.getLocalizedMessage());		 //$NON-NLS-1$ //$NON-NLS-2$
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException spe) throws SAXException
	{
		this.logger.error(Messages.getString("UnmarshallerValidationHandler.8") + spe.getLocalizedMessage()); //$NON-NLS-1$
		this.logger.debug(spe.getLocalizedMessage(), spe.getException());
		throw new SAXException(Messages.getString("UnmarshallerValidationHandler.4") + spe.getLineNumber() + Messages.getString("UnmarshallerValidationHandler.5") + spe.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
