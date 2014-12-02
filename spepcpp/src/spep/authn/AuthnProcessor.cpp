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
 * Creation Date: 25/09/2006
 * 
 * Purpose: Implements the web browser SSO and single logout SAML profiles
 */

#include "spep/Util.h"
#include "spep/authn/AuthnProcessor.h"

#include "saml2/exceptions/InvalidSAMLResponseException.h"
#include "saml2/exceptions/InvalidSAMLAssertionException.h"
#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"
#include "saml2/handlers/SAMLDocument.h"

#include "spep/UnicodeStringConversion.h"

#include "saml2/constants/NameIDFormatConstants.h"
#include "saml2/constants/VersionConstants.h"
#include "saml2/constants/StatusCodeConstants.h"

#include "spep/exceptions/AuthnException.h"
#include "spep/exceptions/InvalidResponseException.h"

#include <openssl/rand.h>

spep::AuthnProcessor::AuthnProcessor(saml2::Logger *logger, spep::AttributeProcessor *attributeProcessor, spep::Metadata *metadata, spep::SessionCache *sessionCache, saml2::SAMLValidator *samlValidator, saml2::IdentifierGenerator *identifierGenerator, KeyResolver *keyResolver, const std::wstring& spepIdentifier, const std::string& ssoRedirect, const std::string& serviceHost, const std::string& schemaPath, int attributeConsumingServiceIndex, int assertionConsumerServiceIndex)
:
mLocalLogger(logger, "spep::AuthnProcessor"),
mAttributeProcessor(attributeProcessor),
mMetadata(metadata),
mSessionCache(sessionCache),
mSamlValidator(samlValidator),
mIdentifierGenerator(identifierGenerator),
mKeyResolver(keyResolver),
mSpepIdentifier(spepIdentifier),
mSSORedirect(ssoRedirect),
mServiceHost(hostnameFromURL(serviceHost)),
mAttributeConsumingServiceIndex(attributeConsumingServiceIndex),
mAssertionConsumerServiceIndex(assertionConsumerServiceIndex)
{
    std::vector<std::string> authnSchemaList{ ConfigurationConstants::samlProtocol, ConfigurationConstants::samlAssertion };
	mAuthnRequestMarshaller = std::make_unique<saml2::MarshallerImpl<saml2::protocol::AuthnRequestType>>(logger, schemaPath, 
		authnSchemaList, "AuthnRequest", "urn:oasis:names:tc:SAML:2.0:protocol", this->mKeyResolver->getSPEPKeyAlias(),
		this->mKeyResolver->getSPEPPrivateKey()
	);
	mResponseUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<saml2::protocol::ResponseType>>(logger, schemaPath, authnSchemaList, metadata);
	
    std::vector<std::string> logoutSchemaList{ ConfigurationConstants::samlProtocol };
	mLogoutResponseMarshaller = std::make_unique<saml2::MarshallerImpl<saml2::protocol::ResponseType>>(logger, schemaPath, 
		logoutSchemaList, "LogoutResponse", "urn:oasis:names:tc:SAML:2.0:protocol", this->mKeyResolver->getSPEPKeyAlias(),
		this->mKeyResolver->getSPEPPrivateKey()
	);
	mLogoutRequestUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<saml2::protocol::LogoutRequestType>>(logger, schemaPath, logoutSchemaList, metadata);
	
	std::size_t pos = mSSORedirect.find('?', 0);
	if (pos != 0)
	{
		mSSORedirect = mSSORedirect.substr(0, pos);
	}
}

spep::AuthnProcessor::~AuthnProcessor()
{
}

std::string spep::AuthnProcessor::hostnameFromURL(const std::string& url)
{
	std::size_t start = url.find( "://", 0 );
	if (start != std::string::npos)
	{
		start += 3;

		std::size_t end = url.find(":", start);
		if (end != std::string::npos)
		{
			return url.substr(start, (end-start));
		}
		else
		{
			return url.substr(start);
		}
	}
	else
	{
		throw saml2::InvalidParameterException(__FILE__, __LINE__, "The URL parameter was invalid.. It did not meet the format requirements of a URL");
	}
}

void spep::AuthnProcessor::processAuthnResponse(spep::AuthnProcessorData &data)
{
	saml2::SAMLDocument responseDocument(data.getResponseDocument());
	
	mLocalLogger.debug() << "About to process Authn response.";
	
	if (responseDocument.getData() == NULL || responseDocument.getLength() == 0)
	{
		mLocalLogger.error() << "NULL Authn Response was received. Unable to process.";
		throw AuthnException( "NULL Authn Response was received. Unable to process." );
	}
	
	std::auto_ptr<saml2::protocol::ResponseType> response;
	
	try
	{
		// Unmarshal the document.
		response.reset(mResponseUnmarshaller->unMarshallSigned(responseDocument));
	}
	catch (saml2::UnmarshallerException &ex)
	{
		// Couldn't unmarshal.
		mLocalLogger.error() << "Failed to unmarshal Authn Response. Error was: " << ex.getMessage() << ". Cause was: " << ex.getCause();
		throw AuthnException("Unable to unmarshal Authn Response. Couldn't authenticate session.");
	}
	catch (std::exception &ex)
	{
		// Couldn't unmarshal.
		mLocalLogger.error() << std::string("Unknown exception while trying unmarshal Authn Response. Message was: ") << ex.what();
		throw AuthnException("Unknown error occurred trying to unmarshal Authn Response. Couldn't authenticate session.");
	}
	
	mLocalLogger.info() << "Unmarshalled Authn Response with SAML ID: " << UnicodeStringConversion::toString(response->ID());
	
	// Get the request ID from the "InResponseTo" value.
	// We need to match this against an UnauthenticatedSession in the session cache, otherwise the request
	// didn't actually originate from us (either that, or the unauthenticated session expired)
	std::wstring requestID(response->InResponseTo()->c_str());
	UnauthenticatedSession unauthenticatedSession;
	try
	{
		// Make sure that the UnauthenticatedSession exists in the cache, otherwise this response does not correspond to an AuthnRequest
		mSessionCache->getUnauthenticatedSession(unauthenticatedSession, requestID);
	}
	catch (std::exception &ex)
	{
		mLocalLogger.error() << "Couldn't find unauthenticated session for request ID: " << UnicodeStringConversion::toString(requestID) << ". Rejecting Authn Response.";
		throw AuthnException("Failed to find an unauthenticated session for the given response. Couldn't authenticate session.");
	}
	
	mSessionCache->terminateUnauthenticatedSession(requestID);
	mLocalLogger.info() << "Terminated unauthenticated session for request ID: " << UnicodeStringConversion::toString(requestID) << ". Going to process Authn Response";

	try
	{
		// Validate the SAML Response.
		mSamlValidator->getResponseValidator().validate(response.get());
	}
	catch( saml2::InvalidSAMLResponseException &ex )
	{
		// Response was rejected explicitly.
		mLocalLogger.error() << "SAML response was rejected by SAML Validator. Reason: " << ex.getMessage();
		throw AuthnException("SAML response was rejected by SAML Validator.");
	}
	catch( std::exception &ex )
	{
		// Error occurred validating the response. Reject it anyway.
		mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << std::string(ex.what());
		throw AuthnException("Error occurred in the SAML Validator.");
	}
	
	// TODO Check destination
	
	// Set the originally requested URL in the data object
	data.setRequestURL(unauthenticatedSession.getRequestURL());
	
	saml2::protocol::ResponseType::Assertion_iterator assertionIterator;
	// For each <Assertion>
	for (assertionIterator = response->Assertion().begin(); assertionIterator != response->Assertion().end(); assertionIterator++)
	{
		try
		{
			// Validate the SAML Assertion.
			mSamlValidator->getAssertionValidator().validate(&(*assertionIterator));
		}
		catch (saml2::InvalidSAMLAssertionException &ex)
		{
			// Assertion was rejected explicitly.
			mLocalLogger.error() << "SAML assertion was rejected by SAML Validator. Reason: " << ex.getMessage();
			throw AuthnException("SAML assertion was rejected by SAML Validator.");
		}
		catch (std::exception &ex)
		{
			// Error occurred validating the assertion. Reject it anyway.
			mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << std::string(ex.what());
			throw AuthnException("Error occurred in the SAML Validator.");
		}
		
		// TODO Validate subject confirmation and audience restriction
		
		// For each <AuthnStatement>
		saml2::assertion::AssertionType::AuthnStatement_const_iterator authnStatementIterator;
		for (authnStatementIterator = assertionIterator->AuthnStatement().begin(); authnStatementIterator != assertionIterator->AuthnStatement().end(); authnStatementIterator++)
		{
			// Process it
			std::pair<bool,std::string> resultPair = processAuthnStatement(*authnStatementIterator, *assertionIterator, data.getRemoteIpAddress(), data.getDisableAttributeQuery());
			
			// The first of the pair is a "success" value. If it's false, something failed.
			if (!resultPair.first)
			{
				throw AuthnException("Failure occurred processing AuthnStatement. Couldn't authenticate session.");
			}
			
			mLocalLogger.info() << "Authenticated new session. " << "REMOTE_ADDR: " << data.getRemoteIpAddress() << ". SPEP Session ID: " << resultPair.second << ". SAML Request ID: " << UnicodeStringConversion::toString(requestID);
			
			data.setSessionID(resultPair.second);
			
			// Don't process any more statements.
			return;
		}
	}
	
	mLocalLogger.error() << "No AuthnStatements found. Rejecting Authn Response.";
	
	throw AuthnException("No AuthnStatements were found in the response document. Couldn't authenticate session.");
}

void spep::AuthnProcessor::generateAuthnRequest(spep::AuthnProcessorData &data)
{	
	std::wstring authnRequestSAMLID(mIdentifierGenerator->generateSAMLAuthnID());
	
	// AMCG. Was debug - changed to info
	mLocalLogger.info() << "Going to create a new AuthnRequest - REMOTE_ADDR: " << data.getRemoteIpAddress() << ". Generated SAML Request AuthnID: " << UnicodeStringConversion::toString(authnRequestSAMLID);

	// Create the unauthenticated session
	UnauthenticatedSession unauthenticatedSession;
	unauthenticatedSession.setRequestURL(data.getRequestURL());
	unauthenticatedSession.setAuthnRequestSAMLID(authnRequestSAMLID);
	
	// Build the XML object.
	saml2::protocol::NameIDPolicyType nameIDPolicy;	
	nameIDPolicy.Format(saml2::nameidformat::TRANS);
	nameIDPolicy.AllowCreate(true);

    saml2::protocol::AuthnRequestType authnRequest;
    authnRequest.IssueInstant(xml_schema::date_time());
	authnRequest.NameIDPolicy(nameIDPolicy);
	authnRequest.ForceAuthn(false);
	authnRequest.IsPassive(false);
	authnRequest.Version(saml2::versions::SAML_20);
	authnRequest.ID(authnRequestSAMLID);
	authnRequest.AttributeConsumingServiceIndex(mAttributeConsumingServiceIndex);
	
	if (this->mServiceHost.compare(hostnameFromURL(data.getBaseRequestURL())) == 0)
	{
		authnRequest.AssertionConsumerServiceIndex(mAssertionConsumerServiceIndex);
	}
	else
	{
		authnRequest.AssertionConsumerServiceURL(spep::UnicodeStringConversion::toWString(data.getBaseRequestURL() + mSSORedirect));
	}
	
	saml2::assertion::NameIDType issuer(mSpepIdentifier);
	authnRequest.Issuer(issuer);
	
	try
	{
		// Need to build a list of IDs of elements that are to be signed, to be passed to SAML2lib
        std::vector<std::string> idList{ UnicodeStringConversion::toString(authnRequestSAMLID) };
		
		// Perform the unmarshal and set in the AuthnRequestData.
		data.setRequestDocument(mAuthnRequestMarshaller->marshallSigned(&authnRequest, idList));
	}
	catch (saml2::MarshallerException &ex)
	{
		mLocalLogger.error() << "Couldn't marshal new AuthnRequest. Message was: " << ex.getMessage() << ". Cause was: " << ex.getCause();
		throw AuthnException("Unable to marshal a new AuthnRequest. Unable to initiate authentication.");
	}
	catch (std::exception &ex)
	{
		mLocalLogger.error() << std::string("Unknown error while trying to marshal new AuthnRequest. Message was: ") << ex.what();
		throw AuthnException("Unknown error occurred while trying to marshal a new AuthnRequest. Unable to initiate authentication.");
	}
	
	// Success! Insert the unauthenticated session in the cache.
	mSessionCache->insertUnauthenticatedSession(unauthenticatedSession);
	
	mLocalLogger.info() << "Created unauthenticated session for new AuthnRequest. REMOTE_ADDR: " << data.getRemoteIpAddress() << " SAML ID: " << UnicodeStringConversion::toString(authnRequestSAMLID);
}

std::pair<bool, std::string> spep::AuthnProcessor::processAuthnStatement(const saml2::assertion::AuthnStatementType& authnStatement, const saml2::assertion::AssertionType& assertion, const std::string& remoteAddress, bool disableAttributeQuery)
{
    std::string sessionID = mIdentifierGenerator->generateSessionID();

	// Get the ESOE session ID and session index out of the document.
	saml2::assertion::SubjectType subject = assertion.Subject().get();
	saml2::assertion::NameIDType subjectNameID = subject.NameID().get();
	std::wstring esoeSessionID(subjectNameID.c_str());
	std::wstring esoeSessionIndex(authnStatement.SessionIndex()->c_str());
	
    mLocalLogger.info() << "Going to process authn statement for new session. REMOTE_ADDR: " << remoteAddress << ". Session ID: " << sessionID;

    PrincipalSession principalSession;

	if (authnStatement.SessionNotOnOrAfter().present())
	{
		mLocalLogger.info() << "Session expiry time from ESOE is: " << boost::posix_time::to_iso_extended_string(authnStatement.SessionNotOnOrAfter().get()) << " for ESOE session: " << spep::UnicodeStringConversion::toString(esoeSessionID);
		principalSession.setSessionNotOnOrAfter(authnStatement.SessionNotOnOrAfter().get());
	}
	else
	{
		mLocalLogger.error() << "Session expiry value was not presented for ESOE session: " << spep::UnicodeStringConversion::toString(esoeSessionID);
	}
	
	principalSession.setESOESessionID(esoeSessionID);
	principalSession.addESOESessionIndexAndLocalSessionID(esoeSessionIndex, sessionID);
	
    bool result = false;

	if (disableAttributeQuery)
	{
		mLocalLogger.debug() << "Skipping attribute processing because it is disabled for session: " << sessionID;
	}
	else
	{
		try
		{
			mLocalLogger.debug() << "Doing attribute processing for session: " << sessionID;
			mAttributeProcessor->doAttributeProcessing(principalSession);
		}
		catch (std::exception &e)
		{
			mLocalLogger.error() << "Failed attribute processing for session: " << sessionID << ". Can't continue authentication.";
			return std::make_pair(result, sessionID);
		}
	}
	
	try
	{
		// Insert the principal session into the session cache.
		this->mSessionCache->insertPrincipalSession(sessionID, principalSession);
		result = true;
		
		mLocalLogger.info() << "Successfully inserted authenticated session (" << sessionID << ") into session cache." << "ESOE session ID: " << spep::UnicodeStringConversion::toString(esoeSessionID);
	}
	catch (std::exception &ex)
	{
		mLocalLogger.error() << std::string("Failed to insert authenticated session into session cache. Message was: ") << ex.what();
	}
	
	// We need to return the result (whether or not the session was added)
	// as well as the local session identifier.
	return std::make_pair(result, sessionID);
}

spep::PrincipalSession spep::AuthnProcessor::verifySession(const std::string& sessionID )
{
	spep::PrincipalSession clientSession;
	
	try
	{
		// Try to get the principal session from the session cache.
		mSessionCache->getPrincipalSession(clientSession, sessionID);
		
		mLocalLogger.debug() << "Verified existing session: " << sessionID;
	}
	catch (std::exception &ex)
	{
		// Can't return null because it's not a pointer type.
		mLocalLogger.error() << "Couldn't verify existing session: " << sessionID << ". Failing.";
		throw AuthnException("Unable to verify an existing session. Failing.");
	}
	
	return clientSession;
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::AuthnProcessor::logoutPrincipal(saml2::protocol::LogoutRequestType *logoutRequest)
{
	PrincipalSession principalSession;
	std::string esoeSessionId = spep::UnicodeStringConversion::toString(logoutRequest->NameID()->c_str());
    std::wstring requestSAMLID(logoutRequest->ID().c_str());

    try
	{
        mLocalLogger.debug() << "Going to log out an authenticated session. Unmarshalling request document";
	
        if (logoutRequest->NameID().present())
		{
			mSessionCache->getPrincipalSessionByEsoeSessionID(principalSession, logoutRequest->NameID()->c_str());
		}
		else
		{
			// TODO Is this correct?
			return generateLogoutResponse(saml2::statuscode::REQUESTOR, L"No NameID was specified to be terminated.", requestSAMLID);
		}
	
	}
	catch (std::exception &ex)
	{
		mLocalLogger.error() << "Error trying to retrieve session from the session cache for logout. ESOE Session ID: " << esoeSessionId << ". Message was: " << ex.what();
		
		// Can't return null, no pointer type
		return generateLogoutResponse(saml2::statuscode::UNKNOWN_PRINCIPAL, L"The principal specified in the logout request is not known at this node.", requestSAMLID);
	}
	
	// TODO Check if only specific sessions should be terminated.
	mSessionCache->terminatePrincipalSession(principalSession.getESOESessionID());
	
	mLocalLogger.info() << "Successfully logged out the ESOE Session '" << esoeSessionId << "'. Returning a success response document";
	
	// Generate a response to the document.
	return generateLogoutResponse(saml2::statuscode::SUCCESS, L"Logout succeeded", requestSAMLID);
	
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::AuthnProcessor::generateLogoutResponse(const std::wstring &statusCodeValue, const std::wstring &statusMessage, const std::wstring &inResponseTo)
{
	mLocalLogger.debug() << "Generating Logout Response document with status code: " << UnicodeStringConversion::toString(statusCodeValue) << " and status message: " << UnicodeStringConversion::toString(statusMessage);
	
	std::wstring logoutResponseSAMLID(mIdentifierGenerator->generateSAMLID()); 
	saml2::assertion::NameIDType issuer(mMetadata->getSPEPIdentifier());
	saml2::protocol::StatusType status;
	saml2::protocol::StatusCodeType statusCode(statusCodeValue);
	status.StatusCode(statusCode);
	status.StatusMessage(statusMessage);
	
	saml2::protocol::ResponseType response;
	response.ID(logoutResponseSAMLID);
	if (!inResponseTo.empty())
	{
		response.InResponseTo(inResponseTo);
	}
	response.IssueInstant(xml_schema::date_time());
	response.Issuer(issuer);
	response.Status(status);
	response.Version(saml2::versions::SAML_20);
	
	try
	{
        std::vector<std::string> idList{ UnicodeStringConversion::toString(logoutResponseSAMLID) };

		DOMDocument* responseDocument = mLogoutResponseMarshaller->generateDOMDocument(&response);
		responseDocument = mLogoutResponseMarshaller->validate(responseDocument);
		mLogoutResponseMarshaller->sign(responseDocument, idList);
		
		return responseDocument;
	}
	catch (saml2::MarshallerException &ex)
	{
		mLocalLogger.error() << "Failed to marshal Logout Response document. Message was: " << ex.getMessage() << ". Cause was: " << ex.getCause();
	}
	catch (std::exception &ex)
	{
		mLocalLogger.error() << std::string("Unknown error trying to marshal Logout Response document. Message was: ") << ex.what();
	}
	
	return NULL;
}
