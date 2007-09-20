/* Copyright 2006-2007, Queensland University of Technology
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
 * Creation Date: 18/06/2007
 * 
 * Purpose: 
 */
 
#include "Cookies.h"
#include "RequestHandler.h"

#include "pep/PolicyEnforcementProcessorData.h"
#include "UnicodeStringConversion.h"

spep::apache::RequestHandler::RequestHandler( spep::SPEP *spep )
:
_spep( spep )
{
}

int spep::apache::RequestHandler::handleRequest( request_rec *req )
{
	
	try
	{
		return this->handleRequestInner( req );
	}
	catch(...)
	{
		return HTTP_INTERNAL_SERVER_ERROR;
	}
}

int spep::apache::RequestHandler::handleRequestInner( request_rec *req )
{
	if( !this->_spep->isStarted() )
	{
		return HTTP_SERVICE_UNAVAILABLE;
	}
	
	spep::SPEPConfigData *spepConfigData = this->_spep->getSPEPConfigData();
	
	Cookies cookies( req );
	const char *cookieValue = cookies[ spepConfigData->getTokenName() ];
	if( cookieValue != NULL )
	{
		
		// No SPEP cookie was found. Need to create a new session.
		std::string sessionID = std::string( cookieValue );
		
		spep::PrincipalSession principalSession;
		bool validSession = false;
		try
		{
			principalSession = this->_spep->getAuthnProcessor()->verifySession( sessionID );
			validSession = true;
		}
		catch( spep::ipc::IPCException e )
		{
		}
		
		if( validSession )
		{
			
			// Put attributes into the environment.
			
			std::string usernameAttribute = spepConfigData->getUsernameAttribute();
			
			std::string attributeValueSeparator = spepConfigData->getAttributeValueSeparator();
			std::string attributeNamePrefix = spepConfigData->getAttributeNamePrefix();
			for( spep::PrincipalSession::AttributeMapType::iterator attributeIterator = principalSession.getAttributeMap().begin();
				attributeIterator != principalSession.getAttributeMap().end();
				++attributeIterator )
			{
				
				std::string name = spep::UnicodeStringConversion::toString( attributeIterator->first );
				std::string envName = attributeNamePrefix + name;
				
				std::stringstream valueStream;
				bool first = true;
				for( std::vector<UnicodeString>::iterator attributeValueIterator = attributeIterator->second.begin(); 
					attributeValueIterator != attributeIterator->second.end(); 
					++attributeValueIterator )
				{
					std::string value = spep::UnicodeStringConversion::toString( *attributeValueIterator );
					
					if( first )
					{
						valueStream << value;
						first = false;
					}
					else
					{
						valueStream << attributeValueSeparator << value;
					}
				}
				
				std::string envValue = valueStream.str();
				
				// Insert the attribute name/value pair into the subprocess environment.
				apr_table_set( req->subprocess_env, envName.c_str(), envValue.c_str() );
				
				if( name.compare( usernameAttribute ) == 0 )
				{
#ifndef APACHE1
					req->user = apr_pstrdup( req->pool, envValue.c_str() );
#else
					req->connection->user = apr_pstrdup( req->pool, envValue.c_str() );
#endif
				}
			}
			
			// Perform authorization on the URI requested.
			char *properURI = apr_pstrdup( req->pool, req->parsed_uri.path );
			if( req->parsed_uri.query != NULL )
			{
				properURI = apr_psprintf( req->pool, "%s?%s", properURI, req->parsed_uri.query );
			}
			
			ap_unescape_url( properURI );
			
			spep::PolicyEnforcementProcessorData pepData;
			pepData.setESOESessionID( principalSession.getESOESessionID() );
			pepData.setResource( properURI );
			
			this->_spep->getPolicyEnforcementProcessor()->makeAuthzDecision( pepData );
			spep::Decision authzDecision( pepData.getDecision() );
			
			validSession = false;
			try
			{
				principalSession = this->_spep->getAuthnProcessor()->verifySession( sessionID );
				validSession = true;
			}
			catch( spep::ipc::IPCException e )
			{
			}
			
			if( validSession )
			{
				if( authzDecision == spep::Decision::PERMIT )
				{
					return DECLINED;
				}
				else if( authzDecision == spep::Decision::DENY )
				{
					return HTTP_FORBIDDEN;
				}
				else if( authzDecision == spep::Decision::ERROR )
				{
					return HTTP_INTERNAL_SERVER_ERROR;
				}
				else
				{
					return HTTP_INTERNAL_SERVER_ERROR;
				}
			}
		}
	}
	
	// If we get to this stage, the session has not been authenticated. We proceed to clear the
	// cookies configured by the SPEP to be cleared upon logout, since this is potentially the
	// first time they have come back to the SPEP since logging out.
	
	// TODO Cookie clear.
	
	const char *base64RequestURI = ap_pbase64encode( req->pool, req->unparsed_uri );
	char *redirectURL = apr_psprintf( req->pool, this->_spep->getSPEPConfigData()->getLoginRedirect().c_str(), base64RequestURI );
	
	apr_table_setn( req->headers_out, "Location", redirectURL );
	return HTTP_MOVED_TEMPORARILY;
	
}
