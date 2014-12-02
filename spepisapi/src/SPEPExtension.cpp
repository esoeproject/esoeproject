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

namespace spep {
    namespace isapi {

        SPEPExtension::SPEPExtension(spep::ConfigurationReader &configReader, const std::string& log) :
            mSpep(nullptr),
            mStream(log.c_str()),
            mSpepWebappURL(DEFAULT_URL_SPEP_WEBAPP),
            mSpepSSOURL(DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_SSO),
            mSpepWebServicesURL(DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES),
            mSpepAuthzCacheClearURL(DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES DEFAULT_URL_SPEP_AUTHZCACHECLEAR),
            mSpepSingleLogoutURL(DEFAULT_URL_SPEP_WEBAPP DEFAULT_URL_SPEP_WEBSERVICES DEFAULT_URL_SPEP_SINGLELOGOUT),
            mWSHandler(nullptr),
            mSSOHandler(nullptr)
        {
            int port = configReader.getIntegerValue(CONFIGURATION_SPEPDAEMONPORT);
            mSpepWebappURL = std::string(DEFAULT_URL_SPEP_WEBAPP);

            mSpep = spep::SPEP::initializeClient(port);
            // Trigger a startup request.
            mSpep->isStarted();

            mWSHandler = new WSHandler(mSpep, this);
            mSSOHandler = new SSOHandler(mSpep, this);

            mLocalLogger = std::make_shared<saml2::LocalLogger>(mSpep->getLogger(), "spep::isapi::SPEPExtension");
        }

        SPEPExtension::~SPEPExtension()
        {
            delete mWSHandler;
            delete mSSOHandler;
        }

        DWORD SPEPExtension::processRequest(ISAPIRequest* request)
        {
            // Handle web service and SSO requests
            const std::string requestedPath = request->getRequestURL();
            if (requestedPath.compare(0, mSpepWebappURL.length(), mSpepWebappURL) == 0)
            {
                if (requestedPath.compare(0, mSpepWebServicesURL.length(), mSpepWebServicesURL) == 0)
                {
                    return mWSHandler->processRequest(request);
                }
                else if (requestedPath.compare(0, mSpepSSOURL.length(), mSpepSSOURL) == 0)
                {
                    return mSSOHandler->handleRequest(request);
                }
            }

            if (!mSpep->isStarted())
            {
                return request->sendErrorDocument(HTTP_SERVICE_UNAVAILABLE);
            }

            // Handle all other page requests

            spep::SPEPConfigData *spepConfigData = mSpep->getSPEPConfigData();

            // Perform authorization on the URI requested.
            std::string properURI = requestedPath;

            const std::string queryString = request->getQueryString();
            if (!queryString.empty())
            {
                properURI = properURI + "?" + queryString;
            }

            request->urlDecode(properURI);

            // Find the SPEP session cookie
            Cookies cookies(request);
            const std::string cookieValue = cookies[spepConfigData->getTokenName()]; // tokenName is the spepSession cookie name
            if (!cookieValue.empty())
            {
                // No SPEP cookie was found. Need to create a new session.
                std::string sessionID(cookieValue);
                spep::PrincipalSession principalSession;
                bool validSession = false;

                try
                {
                    mLocalLogger->info() << "Attempting to retrieve data for session with ID of " << sessionID << " REMOTE_ADDR: " << request->getRemoteAddress();

                    principalSession = mSpep->getAuthnProcessor()->verifySession(sessionID);
                    validSession = true;

                    // This nasty bit of code tries to pull out the username 
                    std::string uidAttributeValue("No UID found in attribute map.");
                    const spep::PrincipalSession::AttributeMapType& attributeMap = principalSession.getAttributeMap();
                    const auto attrib = attributeMap.find(spep::UnicodeStringConversion::toUnicodeString(spepConfigData->getUsernameAttribute()));
                    if (attrib != attributeMap.end())
                    {
                        uidAttributeValue = "UID attribute found but value was NULL";
                        if (attrib->second.size() > 0)
                        {
                            std::string convertedAttributeString = spep::UnicodeStringConversion::toString(attrib->second.front());
                            if (convertedAttributeString.size() > 0)
                                uidAttributeValue = convertedAttributeString;
                        }
                    }

                    mLocalLogger->info() << "Verified existing session with Session ID: " << sessionID << " REMOTE_ADDR: " << request->getRemoteAddress() << " ESOE Session ID: " << spep::UnicodeStringConversion::toString(principalSession.getESOESessionID()) << " UID: " << uidAttributeValue;
                }
                catch (std::exception& e)
                {
                    // log an error here?
                }

                if (validSession)
                {
                    if (!this->mSpep->getSPEPConfigData()->disableAttributeQuery())
                    {
                        // Put attributes into the environment.

                        for (auto attributeIterator = principalSession.getAttributeMap().begin();
                            attributeIterator != principalSession.getAttributeMap().end();
                            ++attributeIterator)
                        {
                            const std::string name = spep::UnicodeStringConversion::toString(attributeIterator->first);
                            const std::string envName = spepConfigData->getAttributeNamePrefix() + name;

                            std::stringstream valueStream;
                            bool first = true;

                            for (std::vector<UnicodeString>::iterator attributeValueIterator = attributeIterator->second.begin();
                                attributeValueIterator != attributeIterator->second.end();
                                ++attributeValueIterator)
                            {
                                std::string value = spep::UnicodeStringConversion::toString(*attributeValueIterator);

                                if (first)
                                {
                                    valueStream << value;
                                    first = false;
                                }
                                else
                                {
                                    valueStream << spepConfigData->getAttributeValueSeparator() << value;
                                }
                            }

                            const std::string envValue = valueStream.str();

                            // Insert the attribute name/value pair into the subprocess environment.
                            request->addRequestHeader(envName, envValue);
                            //apr_table_set( req->subprocess_env, envName.c_str(), envValue.c_str() );

                            if (name == spepConfigData->getUsernameAttribute())
                            {
                                // Set the REMOTE_USER
                                request->setRemoteUser(envValue);
                            }

                            mLocalLogger->debug() << "Attribute inserted into Request Header - Name: " << envName << " Value: " << envValue;
                        }
                    }

                    if (mSpep->getSPEPConfigData()->disablePolicyEnforcement())
                    {
                        mLocalLogger->debug() << "Policy enforcement disabled. Continuing request.";

                        // No need to perform authorization, just let them in.
                        return request->continueRequest();
                    }

                    // Perform authorization on the URI requested.
                    spep::Decision authzDecision;
                    try
                    {
                        spep::PolicyEnforcementProcessorData pepData;
                        pepData.setESOESessionID(principalSession.getESOESessionID());
                        pepData.setResource(spep::UnicodeStringConversion::toUnicodeString(properURI));

                        mSpep->getPolicyEnforcementProcessor()->makeAuthzDecision(pepData);
                        authzDecision = pepData.getDecision();

                    }
                    catch (std::exception& e)
                    {
                        mLocalLogger->error() << "An error occurred when making authz decision with Session ID: " << sessionID << ". Error: " << e.what();
                        return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
                    }


                    validSession = false;
                    try
                    {
                        principalSession = mSpep->getAuthnProcessor()->verifySession(sessionID);
                        validSession = true;
                    }
                    catch (std::exception& ex)
                    {
                        mLocalLogger->error() << "An error occurred when attempting to verify a session after performing authz, with Session ID: " << sessionID << ". Error: " << ex.what();
                    }

                    if (validSession)
                    {
                        // TODO The response documents here all need to be configurable.
                        if (authzDecision == spep::Decision::PERMIT)
                        {
                            return request->continueRequest();
                        }
                        else if (authzDecision == spep::Decision::DENY)
                        {
                            return request->sendErrorDocument(HTTP_FORBIDDEN_READ);
                        }
                        else if (authzDecision == spep::Decision::ERROR)
                        {
                            return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
                        }
                        else
                        {
                            return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }

            // If we get to this stage, the session has not been authenticated. We proceed to clear the
            // cookies configured by the SPEP to be cleared upon logout, since this is potentially the
            // first time they have come back to the SPEP since logging out.

            const std::vector<std::string>& logoutClearCookies = mSpep->getSPEPConfigData()->getLogoutClearCookies();
            for (const auto& logoutCookie: logoutClearCookies)
            {
                // Throw the configured string into a stringstream
                std::stringstream ss(logoutCookie);

                // Split into name, domain, path. Doc says that stringstream operator>> won't throw
                std::string cookieNameString, cookieDomainString, cookiePathString;
                ss >> cookieNameString >> cookieDomainString >> cookiePathString;

                // Default to NULL, and then check if they were specified
                const char *cookieName = nullptr, *cookieDomain = nullptr, *cookiePath = nullptr;
                // No cookie name, no clear.
                if (cookieNameString.empty())
                {
                    continue;
                }

                // If the user sent this cookie.
                if (!cookies[cookieNameString].empty())
                {
                    cookieName = cookieNameString.c_str();

                    if (!cookieDomainString.empty())
                    {
                        cookieDomain = cookieDomainString.c_str();
                    }

                    if (!cookiePathString.empty())
                    {
                        cookiePath = cookiePathString.c_str();
                    }

                    mLocalLogger->info() << "Clearing cookie - Name: " << cookieNameString << " Domain: " << cookieDomainString << " Path: " << cookiePathString << " Value: " << cookies[cookieNameString];

                    // Set the cookie to an empty value.
                    cookies.addCookie(request, cookieName, "", cookiePath, cookieDomain, false);
                }
            }

            // Lazy init code.
            if (spepConfigData->isLazyInit())
            {
                mLocalLogger->debug() << "Lazy init is enabled. Continuing.";

                // See if the _saml_idp cookie has been set
                if (cookies[spepConfigData->getGlobalESOECookieName()].empty())
                {
                    bool matchedLazyInitResource = false;
                    UnicodeString properURIUnicode(spep::UnicodeStringConversion::toUnicodeString(properURI));

                    std::vector<UnicodeString>::const_iterator lazyInitResourceIterator;
                    for (lazyInitResourceIterator = spepConfigData->getLazyInitResources().begin();
                        lazyInitResourceIterator != spepConfigData->getLazyInitResources().end();
                        ++lazyInitResourceIterator)
                    {
                        // TODO Opportunity for caching of compiled regex patterns is here.
                        UParseError parseError;
                        UErrorCode errorCode = U_ZERO_ERROR;
                        // Perform the regular expression matching here.
                        UBool result = RegexPattern::matches(*lazyInitResourceIterator, properURIUnicode, parseError, errorCode);

                        if (U_FAILURE(errorCode))
                        {
                            request->sendResponseHeader(HTTP_INTERNAL_SERVER_ERROR_STATUS_LINE);
                            return HSE_STATUS_SUCCESS;
                        }

                        // FALSE is defined by ICU. This line for portability.
                        if (result != FALSE)
                        {
                            matchedLazyInitResource = true;
                            break;
                        }
                    }

                    if (matchedLazyInitResource)
                    {
                        if (!spepConfigData->isLazyInitDefaultPermit())
                        {
                            return request->continueRequest();
                        }
                    }
                    else
                    {
                        if (spepConfigData->isLazyInitDefaultPermit())
                        {
                            return request->continueRequest();
                        }
                    }
                }
            }

            boost::posix_time::ptime epoch(boost::gregorian::date(1970, 1, 1));
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
            const char *serviceHost = nullptr;
            std::string serviceHostURL = mSpep->getSPEPConfigData()->getServiceHost();
            size_t found = serviceHostURL.find("://");
            if (found != std::string::npos)
            {
                serviceHostURL.erase(0, found + 3);
            }

            if (serviceHost == nullptr || std::strlen(serviceHost) == 0)
            {
                //serviceHost = serviceHostURL.get();
                serviceHost = serviceHostURL.c_str();
            }

            std::string host(request->getHeader("Host"));

            const char *hostname = host.c_str();
            if (host.empty())
            {
                hostname = nullptr;
            }

            if (hostname == nullptr)
            {
                //hostname = req->server->server_hostname;
            }

            //std::string url( request->getRequestURL() );
            //std::string queryString( request->getQueryString() );
            //if( queryString.length() > 0 )
            //	url = url + std::string("?") + queryString;

            const char *format = nullptr;
            const char *base64RequestURI = nullptr;
            // If we can't determine our own hostname, just fall through to the service host.
            // If the service host was requested obviously we want that.
            if (hostname == nullptr || std::strcmp(serviceHost, hostname) == 0)
            {
                // Join the service hostname and requested URI to form the return URL
                std::string returnURL = mSpep->getSPEPConfigData()->getServiceHost() + properURI.c_str();

                Base64Encoder encoder;
                encoder.push(returnURL.c_str(), returnURL.length());
                encoder.close();
                Base64Document document(encoder.getResult());

                // Base64 encode this so that the HTTP redirect doesn't corrupt it.
                base64RequestURI = request->istrndup(document.getData(), document.getLength());

                // Create the format string for building the redirect URL.
                format = request->isprintf("%s%s", mSpep->getSPEPConfigData()->getServiceHost().c_str(),
                    mSpep->getSPEPConfigData()->getSSORedirect().c_str());
            }
            else
            {
                Base64Encoder encoder;
                encoder.push(properURI.c_str(), properURI.length());
                encoder.close();
                Base64Document document(encoder.getResult());

                // Base64 encode this so that the HTTP redirect doesn't corrupt it.
                base64RequestURI = request->istrndup(document.getData(), document.getLength());

                // getSSORedirect() will only give us a temporary.. dup it into the pool so we don't lose it when we leave this scope.
                format = request->istrndup(mSpep->getSPEPConfigData()->getSSORedirect().c_str(), mSpep->getSPEPConfigData()->getSSORedirect().length());
            }

            std::string redirectURL(request->isprintf(format, base64RequestURI));

            std::stringstream timestampParameter;
            if (redirectURL.find_first_of('?') != std::string::npos)
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

            return request->sendRedirectResponse(redirectURL);
        }
    }
}