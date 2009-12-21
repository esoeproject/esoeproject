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

#include <unicode/regex.h>
#include <unicode/parseerr.h>

#include "spep/UnicodeStringConversion.h"
#include "spep/Base64.h"
#include "spep/exceptions/AuthnException.h"

#include "RequestHandler.h"
#include "Cookies.h"

#include "spep/pep/PolicyEnforcementProcessorData.h"

#include <boost/date_time/local_time/local_time.hpp>

#include <cstring>

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
	
	char *properURI = apr_pstrdup( req->pool, req->parsed_uri.path );
	if( req->parsed_uri.query != NULL )
	{
		properURI = apr_psprintf( req->pool, "%s?%s", properURI, req->parsed_uri.query );
	}
	
	ap_unescape_url( properURI );
	
	Cookies cookies( req );
	std::vector<std::string> cookieValues;
	cookies.getCookieValuesByName( cookieValues, spepConfigData->getTokenName() );
	if( !cookieValues.empty() )
	{
		std::string sessionID;
		spep::PrincipalSession principalSession;
		bool validSession = false;

		// SPEP cookie was found, validate using one of the values and use that to proceed.
		for (std::vector<std::string>::iterator cookieValueIterator = cookieValues.begin();
			cookieValueIterator != cookieValues.end(); ++cookieValueIterator) {

			sessionID = *cookieValueIterator;
			try {
				principalSession = this->_spep->getAuthnProcessor()->verifySession( sessionID );
				validSession = true;
				break;
			} catch( std::exception& e ) {
			}
		}
		
		if( validSession )
		{
			// If attribute querying is not disabled...
			if( !this->_spep->getSPEPConfigData()->disableAttributeQuery() )
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
			}
			
			if( this->_spep->getSPEPConfigData()->disablePolicyEnforcement() )
			{
				// No need to perform authorization, just let them in.
				return DECLINED;
			}
			
			// Perform authorization on the URI requested.
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
			catch( std::exception& e )
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
	
	bool requireSend = false;
	const std::vector<std::string>& logoutClearCookies = this->_spep->getSPEPConfigData()->getLogoutClearCookies();
	for( std::vector<std::string>::const_iterator logoutClearCookieIterator = logoutClearCookies.begin();
		logoutClearCookieIterator != logoutClearCookies.end();
		++logoutClearCookieIterator )
	{
		// Throw the configured string into a stringstream
		std::stringstream ss( *logoutClearCookieIterator );
		
		// Split into name, domain, path. Doc says that stringstream operator>> won't throw
		std::string cookieNameString, cookieDomainString, cookiePathString;
		ss >> cookieNameString >> cookieDomainString >> cookiePathString;

		// Default to NULL, and then check if they were specified
		const char *cookieName = NULL, *cookieDomain = NULL, *cookiePath = NULL;
		// No cookie name, no clear.
		if( cookieNameString.length() == 0 )
		{
			continue;
		}
		
		// If the user sent this cookie.
		Cookies cookies( req );
		std::vector<std::string> cookieValues;
		cookies.getCookieValuesByName( cookieValues, spepConfigData->getTokenName() );
		if( !cookieValues.empty() ) {
			cookieName = cookieNameString.c_str();
			
			if( cookieDomainString.length() > 0 )
			{
				cookieDomain = cookieDomainString.c_str();
			}
			
			if( cookiePathString.length() > 0 )
			{
				cookiePath = cookiePathString.c_str();
			}
			
			// Set the cookie to an empty value.
			cookies.addCookie( req, cookieName, "", cookiePath, cookieDomain, false );
			
			// Flag that we need to send the cookies, because we have set at least one.
			requireSend = true;
		}
	}
	
	if( requireSend )
	{
		cookies.sendCookies( req );
	}
	
	// Lazy init code.
	if( spepConfigData->isLazyInit() )
	{
		
		std::string globalESOECookieName( spepConfigData->getGlobalESOECookieName() );
		Cookies cookies( req );
		std::vector<std::string> cookieValues;
		cookies.getCookieValuesByName( cookieValues, globalESOECookieName );
		if( cookieValues.empty() ) {
			bool matchedLazyInitResource = false;
			UnicodeString properURIUnicode( spep::UnicodeStringConversion::toUnicodeString( properURI ) );
			
			std::vector<UnicodeString>::const_iterator lazyInitResourceIterator;
			for( lazyInitResourceIterator = spepConfigData->getLazyInitResources().begin();
				lazyInitResourceIterator != spepConfigData->getLazyInitResources().end();
				++lazyInitResourceIterator )
			{
				// TODO Opportunity for caching of compiled regex patterns is here.
				UParseError parseError;
				UErrorCode errorCode = U_ZERO_ERROR;
				// Perform the regular expression matching here.
				UBool result = RegexPattern::matches( *lazyInitResourceIterator, properURIUnicode, parseError, errorCode );
				
				if ( U_FAILURE( errorCode ) )
				{
					// TODO throw u_errorName( errorCode )
					return HTTP_INTERNAL_SERVER_ERROR;
				}
				
				// FALSE is defined by ICU. This line for portability.
				if (result != FALSE)
				{
					matchedLazyInitResource = true;
					break;
				}
			}
			
			if( matchedLazyInitResource )
			{
				if( !spepConfigData->isLazyInitDefaultPermit() )
				{
					return DECLINED;
				}
			}
			else
			{
				if( spepConfigData->isLazyInitDefaultPermit() )
				{
					return DECLINED;
				}
			}
		}
	}
	
	boost::posix_time::ptime epoch( boost::gregorian::date( 1970, 1, 1 ) );
	boost::posix_time::time_duration timestamp = boost::posix_time::microsec_clock::local_time() - epoch;
	boost::posix_time::time_duration::tick_type currentTimeMillis = timestamp.total_milliseconds();
	
	apr_uri_t *uri = static_cast<apr_uri_t*>( apr_pcalloc( req->pool, sizeof(apr_uri_t) ) );
	apr_uri_parse( req->pool, this->_spep->getSPEPConfigData()->getServiceHost().c_str(), uri );
	
	const char *hostname = apr_table_get( req->headers_in, "Host" );
	if( hostname == NULL )
	{
		hostname = req->server->server_hostname;
	}
	
	const char *format = NULL;
	const char *base64RequestURI = NULL;
	// If we can't determine our own hostname, just fall through to the service host.
	// If the service host was requested obviously we want that.
	if( hostname == NULL || std::strcmp( uri->hostinfo, hostname ) == 0 )
	{
		// Join the service hostname and requested URI to form the return URL
		char *returnURL = apr_psprintf( req->pool, "%s%s", 
				this->_spep->getSPEPConfigData()->getServiceHost().c_str(), req->unparsed_uri );
		
		// Base64 encode this so that the HTTP redirect doesn't corrupt it.
		base64RequestURI = ap_pbase64encode( req->pool, returnURL );
		
		// Create the format string for building the redirect URL.
		format = apr_psprintf( req->pool, "%s%s", this->_spep->getSPEPConfigData()->getServiceHost().c_str(), 
					this->_spep->getSPEPConfigData()->getSSORedirect().c_str() );
	}
	else
	{
		base64RequestURI = ap_pbase64encode( req->pool, req->unparsed_uri );
		// getSSORedirect() will only give us a temporary.. dup it into the pool so we don't lose it when we leave this scope.
		format = apr_pstrdup( req->pool, this->_spep->getSPEPConfigData()->getSSORedirect().c_str() );
	}
	
	char *redirectURL = apr_psprintf( req->pool, format, base64RequestURI );
	
	std::stringstream timestampParameter;
	if( strchr( redirectURL, '?' ) != NULL )
	{
		// Query string already exists.. append the timestamp as another parameter
		timestampParameter << "&ts=" << currentTimeMillis;
		redirectURL = apr_psprintf( req->pool, "%s%s", redirectURL, timestampParameter.str().c_str() );
	}
	else
	{
		// No query string. Add one with the timestamp as a parameter.
		timestampParameter << "?ts=" << currentTimeMillis;
		redirectURL = apr_psprintf( req->pool, "%s%s", redirectURL, timestampParameter.str().c_str() );
	}
	
	apr_table_setn( req->headers_out, "Location", redirectURL );
	return HTTP_MOVED_TEMPORARILY;
	
}
