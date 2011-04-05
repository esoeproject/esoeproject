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
 * Author: Shaun Mangelsdorf
 * Creation Date: Oct 26, 2007
 * 
 * Purpose: 
 */

#include <unicode/regex.h>
#include <unicode/parseerr.h>

#include "spep/UnicodeStringConversion.h"
#include "spep/Base64.h"
#include "spep/exceptions/AuthnException.h"

#include "SPEPExtension.h"
#include "Cookies.h"
#include "FilterConstants.h"
#include "WSHandler.h"
#include "SSOHandler.h"

namespace spep { namespace isapi {
} }

spep::isapi::SPEPExtension::SPEPExtension( spep::ConfigurationReader &configReader, const std::string& log )
:
_spep(NULL),
_stream( log.c_str() ),
_spepWebappURL( DEFAULT_URL_SPEP_WEBAPP ),
_spepSSOURL( DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_SSO ),
_spepWebServicesURL( DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES ),
_spepAuthzCacheClearURL( DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES DEFAULT_URL_SPEP_AUTHZCACHECLEAR ),
_spepSingleLogoutURL( DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES DEFAULT_URL_SPEP_SINGLELOGOUT ),
_wsHandler(NULL),
_ssoHandler(NULL)
{
	int port = configReader.getIntegerValue( CONFIGURATION_SPEPDAEMONPORT );
	_spepWebappURL = std::string(DEFAULT_URL_SPEP_WEBAPP);

	_spep = spep::SPEP::initializeClient( port );
	// Trigger a startup request.
	_spep->isStarted();
	
	_wsHandler = new WSHandler( _spep, this );
	_ssoHandler = new SSOHandler( _spep, this );

	m_localLogger = LocalLoggerPtr(new saml2::LocalLogger(_spep->getLogger(), "spep::isapi::SPEPExtension"));
}

spep::isapi::SPEPExtension::~SPEPExtension()
{
	delete _wsHandler;
	delete _ssoHandler;
}

DWORD spep::isapi::SPEPExtension::processRequest( spep::isapi::ISAPIRequest* request )
{
	std::string requestedPath = request->getRequestURL();
	if( requestedPath.compare( 0, _spepWebappURL.length(), _spepWebappURL ) == 0 )
	{
		if( requestedPath.compare( 0, _spepWebServicesURL.length(), _spepWebServicesURL ) == 0 )
		{
			return _wsHandler->processRequest( request );
		}
		else if( requestedPath.compare( 0, _spepSSOURL.length(), _spepSSOURL ) == 0 )
		{
			return _ssoHandler->handleRequest( request );
		}
	}

	if( !this->_spep->isStarted() )
	{
		return request->sendErrorDocument( HTTP_SERVICE_UNAVAILABLE );
	}
	
	spep::SPEPConfigData *spepConfigData = this->_spep->getSPEPConfigData();
	
	// Perform authorization on the URI requested.
	std::string properURI = requestedPath;
	
	std::string queryString = request->getQueryString();
	if( queryString.length() > 0 )
	{
		properURI = properURI + "?" + queryString;
	}
	
	request->urlDecode( properURI );
	
	Cookies cookies( request );
	std::string tokenName(spepConfigData->getTokenName());
	const std::string& cookieValue = cookies[ tokenName ];
	if( cookieValue.length() != 0 )
	{
		
		// No SPEP cookie was found. Need to create a new session.
		std::string sessionID( cookieValue );
		
		spep::PrincipalSession principalSession;
		bool validSession = false;
		try
		{
			m_localLogger->info() << "Attempting to retrieve data for session with ID of " << sessionID << " REMOTE_ADDR: " << request->getRemoteAddress();

			principalSession = this->_spep->getAuthnProcessor()->verifySession( sessionID );
			validSession = true;

			m_localLogger->info() << "Verified existing session with Session ID: " << sessionID << " REMOTE_ADDR: " << request->getRemoteAddress();
		}
		catch( std::exception &e )
		{
		}
		
		if( validSession )
		{
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
					request->addRequestHeader( envName, envValue );
					//apr_table_set( req->subprocess_env, envName.c_str(), envValue.c_str() );
					
					if( name.compare( usernameAttribute ) == 0 )
					{
						// Set the REMOTE_USER
						request->setRemoteUser( envValue );
					}

					m_localLogger->debug() << "Attribute inserted into Request Header - Name: " << envName << " Value: " << envValue;
				}
			}

			if( this->_spep->getSPEPConfigData()->disablePolicyEnforcement() )
			{
				m_localLogger->debug() << "Policy enforcement disabled. Continuing request.";

				// No need to perform authorization, just let them in.
				return request->continueRequest();
			}
			
			// Perform authorization on the URI requested.
			spep::PolicyEnforcementProcessorData pepData;
			pepData.setESOESessionID( principalSession.getESOESessionID() );
			pepData.setResource( spep::UnicodeStringConversion::toUnicodeString( properURI ) );
			
			this->_spep->getPolicyEnforcementProcessor()->makeAuthzDecision( pepData );
			spep::Decision authzDecision( pepData.getDecision() );
			
			validSession = false;
			try
			{
				principalSession = this->_spep->getAuthnProcessor()->verifySession( sessionID );
				validSession = true;
			}
			catch( std::exception& ex )
			{
				m_localLogger->info() << "An error occurred when attempting to verify a session after performing authz, with Session ID: " << sessionID << ". Error: " << ex.what();
			}
			
			if( validSession )
			{
				// TODO The response documents here all need to be configurable.
				if( authzDecision == spep::Decision::PERMIT )
				{
					return request->continueRequest();
				}
				else if( authzDecision == spep::Decision::DENY )
				{
					return request->sendErrorDocument( HTTP_FORBIDDEN_READ );
				}
				else if( authzDecision == spep::Decision::ERROR )
				{
					return request->sendErrorDocument( HTTP_INTERNAL_SERVER_ERROR );
				}
				else
				{
					return request->sendErrorDocument( HTTP_INTERNAL_SERVER_ERROR );
				}
			}
		}
	}
	
	// If we get to this stage, the session has not been authenticated. We proceed to clear the
	// cookies configured by the SPEP to be cleared upon logout, since this is potentially the
	// first time they have come back to the SPEP since logging out.
	
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
		if( cookies[ cookieNameString ].length() != 0 )
		{
			cookieName = cookieNameString.c_str();
			
			if( cookieDomainString.length() > 0 )
			{
				cookieDomain = cookieDomainString.c_str();
			}
			
			if( cookiePathString.length() > 0 )
			{
				cookiePath = cookiePathString.c_str();
			}
			
			m_localLogger->info() << "Clearing cookie - Name: " << cookieNameString << " Domain: " << cookieDomainString << " Path: " << cookiePathString << " Value: " << cookies[cookieNameString];

			// Set the cookie to an empty value.
			cookies.addCookie( request, cookieName, "", cookiePath, cookieDomain, false );
		}
	}
	
	// Lazy init code.
	if( spepConfigData->isLazyInit() )
	{
		m_localLogger->debug() << "Lazy init is enabled. Continuing.";
		
		std::string globalESOECookieName( spepConfigData->getGlobalESOECookieName() );
		if( cookies[ globalESOECookieName ].length() == 0 )
		{
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
					request->sendResponseHeader( HTTP_INTERNAL_SERVER_ERROR_STATUS_LINE );
					return HSE_STATUS_SUCCESS;
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
					return request->continueRequest();
				}
			}
			else
			{
				if( spepConfigData->isLazyInitDefaultPermit() )
				{
					return request->continueRequest();
				}
			}
		}
	}
	
	boost::posix_time::ptime epoch( boost::gregorian::date( 1970, 1, 1 ) );
	boost::posix_time::time_duration timestamp = boost::posix_time::microsec_clock::local_time() - epoch;
	boost::posix_time::time_duration::tick_type currentTimeMillis = timestamp.total_milliseconds();
	
	/*std::size_t length = this->_spep->getSPEPConfigData()->getServiceHost().length();
	spep::CArray<char> serviceHostURL( length );
	std::memcpy( serviceHostURL.get(), this->_spep->getSPEPConfigData()->getServiceHost().c_str(), length );
	
	const char *serviceHost = NULL;
	
	for( std::size_t i = 0; i < length; ++i )
	{
		if( serviceHostURL[i] == ':' )
		{
			if( i+2 >= length )
				break;
			
			if( serviceHostURL[i+1] == '/' && serviceHostURL[i+2] == '/' )
			{
				serviceHost = &serviceHostURL[i+3];
				
				for( std::size_t j=i; j < length; ++j )
				{
					if( serviceHostURL[j] == ':' )
					{
						serviceHostURL[j] = '\0';
					}
				}
			}
		}
	}*/
	const char *serviceHost = NULL;
	std::string serviceHostURL = _spep->getSPEPConfigData()->getServiceHost();
	size_t found = serviceHostURL.find("://");
	if (found != std::string::npos)
	{
		serviceHostURL.erase(0, found + 3);
	}
	
	if( serviceHost == NULL || std::strlen( serviceHost ) == 0 )
	{
		//serviceHost = serviceHostURL.get();
		serviceHost = serviceHostURL.c_str();
	}
	
	std::string host( request->getHeader( "Host" ) );
	
	const char *hostname = host.c_str();
	if( host.length() == 0 )
	{
		hostname = NULL;
	}
	
	if( hostname == NULL )
	{
		//hostname = req->server->server_hostname;
	}
	
	//std::string url( request->getRequestURL() );
	//std::string queryString( request->getQueryString() );
	//if( queryString.length() > 0 )
	//	url = url + std::string("?") + queryString;
	
	const char *format = NULL;
	const char *base64RequestURI = NULL;
	// If we can't determine our own hostname, just fall through to the service host.
	// If the service host was requested obviously we want that.
	if( hostname == NULL || std::strcmp( serviceHost, hostname ) == 0 )
	{
		// Join the service hostname and requested URI to form the return URL
		std::string returnURL = this->_spep->getSPEPConfigData()->getServiceHost() + properURI.c_str();
		
		Base64Encoder encoder;
		encoder.push( returnURL.c_str(), returnURL.length() );
		encoder.close();
		Base64Document document( encoder.getResult() );
		
		// Base64 encode this so that the HTTP redirect doesn't corrupt it.
		base64RequestURI = request->istrndup( document.getData(), document.getLength() );
		
		// Create the format string for building the redirect URL.
		format = request->isprintf( "%s%s", this->_spep->getSPEPConfigData()->getServiceHost().c_str(), 
					this->_spep->getSPEPConfigData()->getSSORedirect().c_str() );
	}
	else
	{
		Base64Encoder encoder;
		encoder.push( properURI.c_str(), properURI.length() );
		encoder.close();
		Base64Document document( encoder.getResult() );
		
		// Base64 encode this so that the HTTP redirect doesn't corrupt it.
		base64RequestURI = request->istrndup( document.getData(), document.getLength() );
		
		// getSSORedirect() will only give us a temporary.. dup it into the pool so we don't lose it when we leave this scope.
		format = request->istrndup( this->_spep->getSPEPConfigData()->getSSORedirect().c_str(), this->_spep->getSPEPConfigData()->getSSORedirect().length() );
	}
	
	std::string redirectURL( request->isprintf( format, base64RequestURI ) );
	
	std::stringstream timestampParameter;
	if( redirectURL.find_first_of( '?' ) != std::string::npos )
	{
		// Query string already exists.. append the timestamp as another parameter
		timestampParameter << "&ts=" << currentTimeMillis;
		redirectURL = redirectURL + timestampParameter.str();
	}
	else
	{
		// No query string. Add one with the timestamp as a parameter.
		timestampParameter << "?ts=" << currentTimeMillis;
		redirectURL = redirectURL + timestampParameter.str();
	}
	
	/*
	 * 	std::string url( request->getHeader( ISAPI_HEADER_URL ) );
	
	spep::Base64Encoder encoder;
	encoder.push( url.c_str(), url.size() );
	encoder.close();
	spep::Base64Document document( encoder.getResult() );
	
	x << 4;
	std::string base64RequestURI( document.getData(), document.getLength() );
	
	char *redirectURL = request->isprintf( this->_spep->getSPEPConfigData()->getLoginRedirect().c_str(), base64RequestURI.c_str() );
	 */
	
	//std::string redirectHeader( std::string(REDIRECT_HEADER) + redirectURL );
	//request->setHeader( redirectHeader );
	//request->sendResponseHeader( HTTP_REDIRECT_STATUS_LINE );

	return request->sendRedirectResponse( redirectURL );
}
