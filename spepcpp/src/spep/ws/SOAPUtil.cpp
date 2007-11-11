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
 * Creation Date: Aug 14, 2007
 * 
 * Purpose: 
 */

#include "spep/ws/SOAPUtil.h"
#include "spep/Util.h"

#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"
#include "saml2/SAML2Defs.h"


spep::SOAPUtil::SOAPUtil( spep::ReportingProcessor *reportingProcessor, std::string schemaPath )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::SOAPUtil" ) ),
_soap11Handler( new SOAP11Handler( reportingProcessor, schemaPath ) ),
_soap12Handler( new SOAP12Handler( reportingProcessor, schemaPath ) )
{
}

spep::SOAPUtil::~SOAPUtil()
{
	delete this->_soap11Handler;
	delete this->_soap12Handler;
}

/**
 * Wraps a given SAML object and marshalls into a SOAP document ready for a web service request/response.
 */
spep::SOAPDocument spep::SOAPUtil::wrapObjectInSOAP( DOMElement *objectElement, const std::string& characterEncoding, spep::SOAPUtil::SOAPVersion soapVersion )
{
	switch( soapVersion )
	{
		case SOAP12:
		this->_localReportingProcessor.log( DEBUG, "Going to wrap SOAP/1.2 envelope." );
		return this->_soap12Handler->wrap( objectElement, characterEncoding );

		// Default to SOAP/1.1 processing
		case SOAP11:
		default:
		this->_localReportingProcessor.log( DEBUG, "Going to wrap SOAP/1.1 envelope." );
		return this->_soap11Handler->wrap( objectElement, characterEncoding );
	}
}

spep::SOAPUtil::SOAP11Handler::SOAP11Handler( spep::ReportingProcessor *reportingProcessor, std::string schemaPath )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::SOAPUtil::SOAP11Handler" ) ),
_envelopeUnmarshaller( NULL ),
_envelopeMarshaller( NULL )
{
	std::vector<std::string> schemas;
	schemas.push_back( spep::ConfigurationConstants::soap11 );
	schemas.push_back( spep::ConfigurationConstants::samlProtocol );
	schemas.push_back( spep::ConfigurationConstants::samlAssertion );
	schemas.push_back( spep::ConfigurationConstants::samlMetadata );
	schemas.push_back( spep::ConfigurationConstants::lxacml );
	schemas.push_back( spep::ConfigurationConstants::lxacmlSAMLProtocol );
	schemas.push_back( spep::ConfigurationConstants::lxacmlSAMLAssertion );
	schemas.push_back( spep::ConfigurationConstants::lxacmlGroupTarget );
	schemas.push_back( spep::ConfigurationConstants::lxacmlContext );
	schemas.push_back( spep::ConfigurationConstants::lxacmlMetadata );
	schemas.push_back( spep::ConfigurationConstants::esoeProtocol );
	schemas.push_back( spep::ConfigurationConstants::cacheClearService );
	schemas.push_back( spep::ConfigurationConstants::spepStartupService );
	schemas.push_back( spep::ConfigurationConstants::sessionData );
	schemas.push_back( spep::ConfigurationConstants::attributeConfig );
	
	_envelopeUnmarshaller = new saml2::UnmarshallerImpl< soap::v11::Envelope >( schemaPath, schemas );
	_envelopeMarshaller = new saml2::MarshallerImpl< soap::v11::Envelope >( schemaPath, schemas, "Envelope", "http://schemas.xmlsoap.org/soap/envelope/" );
	_bodyUnmarshaller = new saml2::UnmarshallerImpl< soap::v11::Body >( schemaPath, schemas );
	_bodyMarshaller = new saml2::MarshallerImpl< soap::v11::Body >( schemaPath, schemas, "Body", "http://schemas.xmlsoap.org/soap/envelope/" );
	
	_implFlags = XMLString::transcode( IMPL_FLAGS );
	_domImpl = DOMImplementationRegistry::getDOMImplementation(this->_implFlags);
}

spep::SOAPUtil::SOAP11Handler::~SOAP11Handler()
{
	if( _envelopeUnmarshaller != NULL )
	{
		delete _envelopeUnmarshaller;
	}

	if( _envelopeMarshaller != NULL )
	{
		delete _envelopeMarshaller;
	}
	
	if( _implFlags != NULL )
	{
		XMLString::release( &_implFlags );
	}
}

spep::SOAPDocument spep::SOAPUtil::SOAP11Handler::wrap( DOMElement *objectElement, std::string characterEncoding )
{
	/*soap::v11::Body body;
	body.any().push_back( *objectElement );
	
	DOMDocument* bodyDocument = this->_bodyMarshaller->generateDOMDocument( &body );
	bodyDocument = this->_bodyMarshaller->validate( bodyDocument );
	
	soap::v11::Envelope envelope;
	DOMElement* importedNode = static_cast<DOMElement*>( envelope.dom_document().importNode( bodyDocument->getDocumentElement(), true ) );
	envelope.any().push_back( importedNode );
	
	bodyDocument->release();*/
	
	soap::v11::Body body;
	body.any().push_back( *objectElement );
	
	soap::v11::Envelope envelope;
	envelope.Body( body );
	
	return this->_envelopeMarshaller->marshallUnSigned( &envelope, false );
}

spep::SOAPUtil::SOAP12Handler::SOAP12Handler( spep::ReportingProcessor *reportingProcessor, std::string schemaPath )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::SOAPUtil::SOAP12Handler" ) ),
_envelopeUnmarshaller( NULL ),
_envelopeMarshaller( NULL )
{
	std::vector<std::string> schemas;
	schemas.push_back( spep::ConfigurationConstants::soap12 );
	schemas.push_back( spep::ConfigurationConstants::samlProtocol );
	schemas.push_back( spep::ConfigurationConstants::samlAssertion );
	schemas.push_back( spep::ConfigurationConstants::samlMetadata );
	schemas.push_back( spep::ConfigurationConstants::lxacml );
	schemas.push_back( spep::ConfigurationConstants::lxacmlSAMLProtocol );
	schemas.push_back( spep::ConfigurationConstants::lxacmlSAMLAssertion );
	schemas.push_back( spep::ConfigurationConstants::lxacmlGroupTarget );
	schemas.push_back( spep::ConfigurationConstants::lxacmlContext );
	schemas.push_back( spep::ConfigurationConstants::lxacmlMetadata );
	schemas.push_back( spep::ConfigurationConstants::esoeProtocol );
	schemas.push_back( spep::ConfigurationConstants::cacheClearService );
	schemas.push_back( spep::ConfigurationConstants::spepStartupService );
	schemas.push_back( spep::ConfigurationConstants::sessionData );
	schemas.push_back( spep::ConfigurationConstants::attributeConfig );
	
	_envelopeUnmarshaller = new saml2::UnmarshallerImpl< soap::v12::Envelope >( schemaPath, schemas );
	_envelopeMarshaller = new saml2::MarshallerImpl< soap::v12::Envelope >( schemaPath, schemas, "Envelope", "http://www.w3.org/2003/05/soap-envelope" );
	
	_implFlags = XMLString::transcode( IMPL_FLAGS );
	_domImpl = DOMImplementationRegistry::getDOMImplementation(this->_implFlags);
}

spep::SOAPUtil::SOAP12Handler::~SOAP12Handler()
{
	if( _envelopeUnmarshaller != NULL )
	{
		delete _envelopeUnmarshaller;
	}

	if( _envelopeMarshaller != NULL )
	{
		delete _envelopeMarshaller;
	}
	
	if( _implFlags != NULL )
	{
		XMLString::release( &_implFlags );
	}
}

spep::SOAPDocument spep::SOAPUtil::SOAP12Handler::wrap( DOMElement *objectElement, std::string characterEncoding )
{
	
	soap::v12::Body body;
	body.any().push_back( *objectElement );
	
	soap::v12::Envelope envelope;
	envelope.Body( body );
	
	return this->_envelopeMarshaller->marshallUnSigned( &envelope, false );
}
