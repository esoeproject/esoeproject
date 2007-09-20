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

#include "startup/impl/StartupProcessorImpl.h"

#include "constants/VersionConstants.h"
#include "constants/StatusCodeConstants.h"

#include "exceptions/SPEPStartupException.h"
#include "exceptions/InvalidStateException.h"

#include "handlers/impl/UnmarshallerImpl.h"
#include "handlers/impl/MarshallerImpl.h"
#include "exceptions/InvalidSAMLResponseException.h"

#include <boost/lexical_cast.hpp>

//#define COMPILE_DATE __DATE__

#ifndef COMPILE_SYSTEM

#ifdef __GNUC__
#define COMPILE_SYSTEM __VERSION__
#endif /* __GNUC__ */

#endif /* COMPILE_SYSTEM */

spep::StartupProcessorImpl::StartupProcessorImpl( spep::ReportingProcessor *reportingProcessor, spep::WSClient *wsClient, spep::Metadata *metadata, spep::KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, std::string schemaPath, std::wstring spepIdentifier, const std::vector<std::wstring>& ipAddresses, std::string nodeID, int authzCacheIndex, int startupRetryInterval )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::StartupProcessorImpl" ) ),
_reportingProcessor( reportingProcessor ),
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
	
	this->_validateInitializationRequestMarshaller = new saml2::MarshallerImpl<middleware::ESOEProtocolSchema::ValidateInitializationRequestType>( schemaPath, spepStartupSchemas, "ValidateInitializationRequest", "http://www.qut.com/middleware/ESOEProtocolSchema", this->_keyResolver->getSPEPKeyName(), this->_keyResolver->getSPEPPrivateKey() );
	this->_validateInitializationResponseUnmarshaller = new saml2::UnmarshallerImpl<middleware::ESOEProtocolSchema::ValidateInitializationResponseType>( schemaPath, spepStartupSchemas, this->_metadata );
}

spep::StartupProcessorImpl::~StartupProcessorImpl()
{
}
	
saml2::SAMLDocument spep::StartupProcessorImpl::buildRequest( const std::wstring &samlID )
{
	this->_localReportingProcessor.log( spep::DEBUG, "Going to build SPEP startup request." );
	
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
	validateInitializationRequest.sw_version( L"C++ SPEP v0.0" );
	validateInitializationRequest.environment( L"Linux" );
	for( std::vector<std::wstring>::iterator iter = this->_ipAddresses.begin(); iter != this->_ipAddresses.end(); ++iter )
	{
		validateInitializationRequest.ipAddress().push_back( *iter );
	}
	
	std::vector<std::string> idList;
	idList.push_back( UnicodeStringConversion::toString(samlID) );
	
	return this->_validateInitializationRequestMarshaller->marshallSigned( &validateInitializationRequest, idList );
	
}

void spep::StartupProcessorImpl::processResponse( const saml2::SAMLDocument& responseDocument, const std::wstring &expectedSAMLID )
{
	std::auto_ptr<middleware::ESOEProtocolSchema::ValidateInitializationResponseType> response( NULL );
	
	try
	{
		response.reset( this->_validateInitializationResponseUnmarshaller->unMarshallSigned( responseDocument ) );
	}
	catch( saml2::UnmarshallerException &ex )
	{
		std::stringstream ss;
		ss << "SPEP startup ERROR. Exception when unmarshalling startup response. Exception was: " << ex.getMessage() << ". Cause was: " << ex.getCause() << std::ends;
		this->_localReportingProcessor.log( spep::ERROR, ss.str() );
		throw SPEPStartupException( "Exception occurred while unmarshalling SPEP startup response." );
	}
	catch( std::exception &ex )
	{
		std::stringstream ss;
		ss << "SPEP startup ERROR. Exception when unmarshalling startup response. Exception was: " << ex.what() << std::ends;
		this->_localReportingProcessor.log( spep::ERROR, ss.str() );
		throw SPEPStartupException( "Exception occurred while unmarshalling SPEP startup response." );
	}
	
	try
	{
		// Validate the SAML Response.
		this->_samlValidator->getResponseValidator().validate( response.get() );
	}
	catch( saml2::InvalidSAMLResponseException &ex )
	{
		// Response was rejected explicitly.
		this->_localReportingProcessor.log( ERROR, "SAML response was rejected by SAML Validator. Reason: " + ex.getMessage() );
		throw SPEPStartupException( "SAML response was rejected by SAML Validator." );
	}
	catch( std::exception &ex )
	{
		// Error occurred validating the response. Reject it anyway.
		this->_localReportingProcessor.log( ERROR, "Error occurred in the SAML Validator. Message: " + std::string(ex.what()) );
		throw SPEPStartupException( "Error occurred in the SAML Validator." );
	}
	
	// TODO Check issuer
	
	xml_schema::uri &statusCodeValue = response->Status().StatusCode().Value();
	if( saml2::statuscode::SUCCESS.compare( 0, statusCodeValue.length(), statusCodeValue.c_str() ) == 0 )
	{
		// Success. Permit the SPEP startup.
		this->_localReportingProcessor.log( spep::INFO, "SPEP startup SUCCESS. Beginning normal operation." );
		return;
	}
	
	if( response->Status().StatusMessage().present() )
	{
		this->_localReportingProcessor.log( spep::ERROR, "SPEP startup FAILED. Retrying later. Message from ESOE was: " + UnicodeStringConversion::toString( response->Status().StatusMessage().get().c_str() ) );
	}
	else
	{
		this->_localReportingProcessor.log( spep::ERROR, "SPEP startup FAILED. Retrying later. No message from ESOE to explain failure." );
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
	
	StartupProcessorThread threadObject( this->_reportingProcessor, this, this->_startupRetryInterval );
	this->_threadGroup.create_thread( threadObject );
}

void spep::StartupProcessorImpl::doStartup()
{
	
	try
	{
		// Locking here might improve efficiency, because a startup request in progress will block all requests
		// for "started" status until it is finished - saves returning a value and having them sleep to retry.
		ScopedLock lock( this->_startupResultMutex );
		
		// Generate the request document
		std::wstring samlID( this->_identifierGenerator->generateSAMLID() );
		
		WSProcessorData wsData;
		wsData.setSAMLRequestDocument( this->buildRequest( samlID ) );
		
		std::string endpoint( this->_metadata->getSPEPStartupServiceEndpoint() );
		
		{
			std::stringstream ss;
			ss << "About to send SPEP startup WS query to ESOE endpoint: " << endpoint << std::ends;
			this->_localReportingProcessor.log( spep::DEBUG, ss.str() );
		}
		
		// Perform the web service call.
		this->_wsClient->spepStartup( wsData, endpoint );
		
		this->_localReportingProcessor.log( spep::DEBUG, "Received response from web service endpoint. Going to process." );
		
		// Process the response.
		this->processResponse( wsData.getSAMLResponseDocument(), samlID );
		
		// If we made it here, startup was successful..
		this->setStartupResult( STARTUP_ALLOW );
		
	}
	catch( saml2::MarshallerException &ex )
	{
		std::stringstream ss;
		ss << "Failed to marshal request document. Error was: " << ex.getMessage() << " .. cause: " << ex.getCause() << std::ends;
		
		// .. otherwise it failed for some reason.
		this->_localReportingProcessor.log( spep::ERROR, ss.str() );
		this->setStartupResult( STARTUP_FAIL );
		
	}
	catch( std::exception &ex )
	{
		
		std::stringstream ss;
		ss << "Failed SPEP startup. Exception message was: " << ex.what() << std::ends;
		
		// .. otherwise it failed for some reason.
		this->_localReportingProcessor.log( spep::ERROR, ss.str() );
		this->setStartupResult( STARTUP_FAIL );
		
	}
}

spep::StartupProcessorImpl::StartupProcessorThread::StartupProcessorThread( spep::ReportingProcessor *reportingProcessor, spep::StartupProcessorImpl *startupProcessor, int startupRetryInterval )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::StartupProcessor" ) ),
_startupProcessor( startupProcessor ),
_startupRetryInterval( startupRetryInterval )
{
}

spep::StartupProcessorImpl::StartupProcessorThread::StartupProcessorThread( const spep::StartupProcessorImpl::StartupProcessorThread& other )
:
_localReportingProcessor( other._localReportingProcessor ),
_startupProcessor( other._startupProcessor ),
_startupRetryInterval( other._startupRetryInterval )
{
}

void spep::StartupProcessorImpl::StartupProcessorThread::operator()()
{

	boost::xtime nextUpdate;

	this->_localReportingProcessor.log( spep::INFO, "SPEP startup handler begins." );
	// Loop until we're allowed to start.
	while( this->_startupProcessor->allowProcessing() != STARTUP_ALLOW )
	{

		try
		{
			this->_startupProcessor->doStartup();
		}
		catch (...)
		{
			this->_localReportingProcessor.log( spep::DEBUG, "Unexpected throw from doStartup() .. ignoring and continuing loop." );
		}
		
		if( this->_startupProcessor->allowProcessing() == STARTUP_ALLOW ) break;
		
		if( boost::xtime_get( &nextUpdate, boost::TIME_UTC ) == 0 )
		{
			this->_localReportingProcessor.log( spep::ERROR, "Couldn't get UTC time from boost::xtime_get" );
		}
		
		nextUpdate.sec += _startupRetryInterval;
		
		boost::thread::sleep( nextUpdate );
		
	}
	this->_localReportingProcessor.log( spep::DEBUG, "SPEP startup handler exiting loop." );
}

spep::StartupProcessorImpl::StartupProcessorThread& spep::StartupProcessorImpl::StartupProcessorThread::operator=( const spep::StartupProcessorImpl::StartupProcessorThread& other )
{
	this->_localReportingProcessor = other._localReportingProcessor;
	this->_startupProcessor = other._startupProcessor;
	this->_startupRetryInterval = other._startupRetryInterval;
	
	return *this;
}

