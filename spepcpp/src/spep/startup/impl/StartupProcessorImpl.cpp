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
 * Creation Date: 08/01/2007
 * 
 * Purpose: 
 */

#include "spep/startup/impl/StartupProcessorImpl.h"
#include "spep/UnicodeStringConversion.h"

#include "saml2/constants/VersionConstants.h"
#include "saml2/constants/StatusCodeConstants.h"

#include "spep/exceptions/SPEPStartupException.h"
#include "spep/exceptions/InvalidStateException.h"

#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"
#include "saml2/exceptions/InvalidSAMLResponseException.h"

#include <boost/lexical_cast.hpp>

//#define COMPILE_DATE __DATE__

#ifndef COMPILE_SYSTEM

#ifdef __GNUC__
#define COMPILE_SYSTEM __VERSION__
#endif /* __GNUC__ */

#ifdef WIN32
#define COMPILE_OS "Win32 "

#ifdef _MSC_VER
#define COMPILE_CXX COMPILE_OS "Visual C++ " #_MSC_VER
#else /*_MSC_VER*/
#define COMPILE_SYSTEM COMPILE_OS "Unknown compiler"
#endif /*_MSC_VER*/

#endif /*WIN32*/

#ifndef COMPILE_SYSTEM/*(2)*/
#define COMPILE_SYSTEM "Unknown compile system"
#endif /*COMPILE_SYSTEM(2)*/

#endif /* COMPILE_SYSTEM */

#ifdef PACKAGE_STRING
#define COMPILE_VERSION PACKAGE_STRING
#else /*PACKAGE_STRING*/
#define COMPILE_VERSION "Unsupported version"
#endif /*PACKAGE_STRING*/

#define ENVIRONMENT L"Unspecified environment"

spep::StartupProcessorImpl::StartupProcessorImpl( saml2::Logger *logger, spep::WSClient *wsClient, spep::Metadata *metadata, spep::KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, std::string schemaPath, std::wstring spepIdentifier, const std::vector<std::wstring>& ipAddresses, std::string nodeID, int authzCacheIndex, int startupRetryInterval )
:
_localLogger( logger, "spep::StartupProcessorImpl" ),
_logger( logger ),
_wsClient( wsClient ),
_metadata( metadata ),
_keyResolver( keyResolver ),
_samlValidator( samlValidator ),
_startupResult( STARTUP_NONE ),
_spepIdentifier( spepIdentifier ),
_ipAddresses( ipAddresses ),
_nodeID( nodeID ),
_authzCacheIndex( authzCacheIndex ),
_startupRetryInterval( startupRetryInterval ),
_validateInitializationRequestMarshaller( NULL ),
_validateInitializationResponseUnmarshaller( NULL ),
_identifierGenerator( identifierGenerator )
{
	std::vector<std::string> spepStartupSchemas;
	spepStartupSchemas.push_back( ConfigurationConstants::esoeProtocol );
	
	this->_validateInitializationRequestMarshaller = new saml2::MarshallerImpl
		<middleware::ESOEProtocolSchema::ValidateInitializationRequestType>(
			logger, schemaPath, spepStartupSchemas, "ValidateInitializationRequest", "http://www.qut.com/middleware/ESOEProtocolSchema", 
			this->_keyResolver->getSPEPKeyAlias(), this->_keyResolver->getSPEPPrivateKey()
		);
	
	this->_validateInitializationResponseUnmarshaller = new saml2::UnmarshallerImpl
		<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>
			( logger, schemaPath, spepStartupSchemas, this->_metadata );
}

spep::StartupProcessorImpl::~StartupProcessorImpl()
{
}
	
XERCES_CPP_NAMESPACE::DOMDocument* spep::StartupProcessorImpl::buildRequest( const std::wstring &samlID )
{
	_localLogger.debug() << "Going to build SPEP startup request.";
	
	saml2::assertion::NameIDType issuer( this->_spepIdentifier );
	
	middleware::ESOEProtocolSchema::ValidateInitializationRequestType validateInitializationRequest;
	validateInitializationRequest.ID( samlID );
	validateInitializationRequest.Version( saml2::versions::SAML_20 );
	validateInitializationRequest.IssueInstant( xml_schema::date_time() );
	validateInitializationRequest.Issuer( issuer );
	validateInitializationRequest.nodeId( UnicodeStringConversion::toWString( boost::lexical_cast<std::string>( this->_nodeID ) ) );
	validateInitializationRequest.authzCacheIndex( this->_authzCacheIndex );
	
	validateInitializationRequest.compileDate( UnicodeStringConversion::toWString( std::string(__DATE__) ) );
	validateInitializationRequest.compileSystem( UnicodeStringConversion::toWString( std::string( COMPILE_SYSTEM ) ) );
	validateInitializationRequest.sw_version( UnicodeStringConversion::toWString( COMPILE_VERSION ) );
	validateInitializationRequest.environment( ENVIRONMENT );
	for( std::vector<std::wstring>::iterator iter = this->_ipAddresses.begin(); iter != this->_ipAddresses.end(); ++iter )
	{
		validateInitializationRequest.ipAddress().push_back( *iter );
	}
	
	std::vector<std::string> idList;
	idList.push_back( UnicodeStringConversion::toString(samlID) );
	
	DOMDocument *requestDocument = this->_validateInitializationRequestMarshaller->generateDOMDocument( &validateInitializationRequest );
	requestDocument = this->_validateInitializationRequestMarshaller->validate( requestDocument );
	this->_validateInitializationRequestMarshaller->sign( requestDocument, idList );
	
	return requestDocument;
}

void spep::StartupProcessorImpl::processResponse( middleware::ESOEProtocolSchema::ValidateInitializationResponseType* response, const std::wstring &expectedSAMLID )
{
	try
	{
		// Validate the SAML Response.
		this->_samlValidator->getResponseValidator().validate( response );
	}
	catch( saml2::InvalidSAMLResponseException &ex )
	{
		// Response was rejected explicitly.
		_localLogger.error() << "SAML response was rejected by SAML Validator. Reason: " << ex.getMessage();
		throw SPEPStartupException( "SAML response was rejected by SAML Validator." );
	}
	catch( std::exception &ex )
	{
		// Error occurred validating the response. Reject it anyway.
		_localLogger.error() << "Error occurred in the SAML Validator. Message: " << std::string(ex.what());
		throw SPEPStartupException( "Error occurred in the SAML Validator." );
	}
	
	// TODO Check issuer
	
	xml_schema::uri &statusCodeValue = response->Status().StatusCode().Value();
	if( saml2::statuscode::SUCCESS.compare( 0, statusCodeValue.length(), statusCodeValue.c_str() ) == 0 )
	{
		// Success. Permit the SPEP startup.
		_localLogger.info() << "SPEP startup SUCCESS. Beginning normal operation.";
		return;
	}
	
	if( response->Status().StatusMessage().present() )
	{
		_localLogger.error() << "SPEP startup FAILED. Retrying later. Message from ESOE was: " << UnicodeStringConversion::toString( response->Status().StatusMessage().get().c_str() );
	}
	else
	{
		_localLogger.error() << "SPEP startup FAILED. Retrying later. No message from ESOE to explain failure.";
	}
	
	throw SPEPStartupException( "Response from ESOE did not indicate successful SPEP startup." );
}

spep::StartupResult spep::StartupProcessorImpl::allowProcessing()
{
	ScopedLock lock( this->_startupResultMutex );
	return this->_startupResult;
}

void spep::StartupProcessorImpl::setStartupResult( StartupResult startupResult )
{
	ScopedLock lock( this->_startupResultMutex );
	this->_startupResult = startupResult;
}

void spep::StartupProcessorImpl::beginSPEPStart()
{
	// Make sure we only start 1 thread..
	{
		ScopedLock lock( this->_startupResultMutex );
		
		if( this->allowProcessing() != STARTUP_NONE )
			return;
		
		// Set the startup result to 'wait' so that everything else will block.
		this->setStartupResult( STARTUP_WAIT );
	}
	
	StartupProcessorThread threadObject( this->_logger, this, this->_startupRetryInterval );
	this->_threadGroup.create_thread( threadObject );
}

void spep::StartupProcessorImpl::doStartup()
{
	
	try
	{
		// Generate the request document
		std::wstring samlID( this->_identifierGenerator->generateSAMLID() );
		
		xml_schema::dom::auto_ptr<DOMDocument> requestDocument( this->buildRequest( samlID ) );
		
		std::string endpoint( this->_metadata->getSPEPStartupServiceEndpoint() );
		
		{
			std::stringstream ss;
			ss << "About to send SPEP startup WS query to ESOE endpoint: " << endpoint << std::ends;
			_localLogger.debug() << ss.str();
		}
		
		// Perform the web service call.
		try
		{
			std::auto_ptr<middleware::ESOEProtocolSchema::ValidateInitializationResponseType> response(
				this->_wsClient->doWSCall( endpoint, requestDocument.get(), this->_validateInitializationResponseUnmarshaller )
			);
			
			_localLogger.debug() << "Received response from web service endpoint. Going to process.";
			
			// Process the response.
			this->processResponse( response.get(), samlID );
		}
		catch( saml2::UnmarshallerException &ex )
		{
			std::stringstream ss;
			ss << "SPEP startup ERROR. Exception when unmarshalling startup response. Exception was: " << ex.getMessage() << ". Cause was: " << ex.getCause() << std::ends;
			_localLogger.debug() << ss.str();
			throw SPEPStartupException( "Exception occurred while unmarshalling SPEP startup response." );
		}
		catch( std::exception &ex )
		{
			std::stringstream ss;
			ss << "SPEP startup ERROR. Exception when unmarshalling startup response. Exception was: " << ex.what() << std::ends;
			_localLogger.debug() << ss.str();
			throw SPEPStartupException( "Exception occurred while unmarshalling SPEP startup response." );
		}
		
		// If we made it here, startup was successful..
		this->setStartupResult( STARTUP_ALLOW );
		
	}
	catch( saml2::MarshallerException &ex )
	{
		std::stringstream ss;
		ss << "Failed to marshal request document. Error was: " << ex.getMessage() << " .. cause: " << ex.getCause() << std::ends;
		
		// .. otherwise it failed for some reason.
		_localLogger.debug() << ss.str();
		this->setStartupResult( STARTUP_FAIL );
		
	}
	catch( std::exception &ex )
	{
		
		std::stringstream ss;
		ss << "Failed SPEP startup. Exception message was: " << ex.what() << std::ends;
		
		// .. otherwise it failed for some reason.
		_localLogger.debug() << ss.str();
		this->setStartupResult( STARTUP_FAIL );
		
	}
}

spep::StartupProcessorImpl::StartupProcessorThread::StartupProcessorThread( saml2::Logger *logger, spep::StartupProcessorImpl *startupProcessor, int startupRetryInterval )
:
_logger( logger ),
_localLogger( logger, "spep::StartupProcessor" ),
_startupProcessor( startupProcessor ),
_startupRetryInterval( startupRetryInterval )
{
}

spep::StartupProcessorImpl::StartupProcessorThread::StartupProcessorThread( const spep::StartupProcessorImpl::StartupProcessorThread& other )
:
_logger( other._logger ),
_localLogger( other._logger, "spep::StartupProcessor" ),
_startupProcessor( other._startupProcessor ),
_startupRetryInterval( other._startupRetryInterval )
{
}

void spep::StartupProcessorImpl::StartupProcessorThread::operator()()
{

	boost::xtime nextUpdate;

	_localLogger.debug() << "SPEP startup handler begins.";
	// Loop until we're allowed to start.
	while( this->_startupProcessor->allowProcessing() != STARTUP_ALLOW )
	{

		try
		{
			this->_startupProcessor->doStartup();
		}
		catch (...)
		{
			_localLogger.error() << "Unexpected throw from doStartup() .. ignoring and continuing loop.";
		}
		
		if( this->_startupProcessor->allowProcessing() == STARTUP_ALLOW ) break;
		
		if( boost::xtime_get( &nextUpdate, boost::TIME_UTC ) == 0 )
		{
			_localLogger.error() << "Couldn't get UTC time from boost::xtime_get";
		}
		
		nextUpdate.sec += _startupRetryInterval;
		
		boost::thread::sleep( nextUpdate );
		
	}
	_localLogger.debug() << "SPEP startup handler exiting loop.";
}

