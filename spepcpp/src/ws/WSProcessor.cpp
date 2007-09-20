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

#include "ws/WSProcessor.h"
#include "Util.h"
#include "UnicodeStringConversion.h"

#include "axis2_http_transport.h"

spep::WSProcessor::WSProcessor( spep::ReportingProcessor *reportingProcessor, spep::AuthnProcessor *authnProcessor, spep::PolicyEnforcementProcessor *policyEnforcementProcessor )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor("spep::WSProcessor") ),
_authnProcessor( authnProcessor ),
_policyEnforcementProcessor( policyEnforcementProcessor ),
_contentType( AXIS2_HTTP_HEADER_ACCEPT_APPL_SOAP )
{
}

void spep::WSProcessor::authzCacheClear( spep::WSProcessorData& data )
{
	this->_localReportingProcessor.log( spep::DEBUG, UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString( data.getSOAPRequestDocument() ) ) );
	this->processSOAPRequest( data );
	
	PolicyEnforcementProcessorData policyEnforcementProcessorData;
	policyEnforcementProcessorData.setRequestDocument( data.getSAMLRequestDocument() );
	
	this->_policyEnforcementProcessor->authzCacheClear( policyEnforcementProcessorData );
	
	data.setSAMLResponseDocument( policyEnforcementProcessorData.getResponseDocument() );
	
	this->createSOAPResponse( data );
	this->_localReportingProcessor.log( spep::DEBUG, UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString( data.getSOAPResponseDocument() ) ) );
}

void spep::WSProcessor::singleLogout( spep::WSProcessorData& data )
{
	this->_localReportingProcessor.log( spep::DEBUG, UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString( data.getSOAPRequestDocument() ) ) );
	this->processSOAPRequest( data );
	
	AuthnProcessorData authnProcessorData;
	authnProcessorData.setRequestDocument( data.getSAMLRequestDocument() );
	
	this->_authnProcessor->logoutPrincipal( authnProcessorData );
	
	data.setSAMLResponseDocument( authnProcessorData.getResponseDocument() );
	
	this->createSOAPResponse( data );
	this->_localReportingProcessor.log( spep::DEBUG, UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString( data.getSOAPResponseDocument() ) ) );
}

void spep::WSProcessor::processSOAPRequest( spep::WSProcessorData& data )
{
	saml2::SAMLDocument document( SOAPUtil::unwrapDocumentFromSOAP( data.getSOAPRequestDocument(), data.getCharacterEncoding(), data.getSOAPVersion() ) );
	
	data.setSAMLRequestDocument( document );
}

void spep::WSProcessor::createSOAPResponse( spep::WSProcessorData& data )
{
	SOAPDocument document( SOAPUtil::wrapDocumentInSOAP( data.getSAMLResponseDocument(), data.getCharacterEncoding(), data.getSOAPVersion() ) );
	
	data.setSOAPResponseDocument( document );
}
