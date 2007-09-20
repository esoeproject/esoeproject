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
 * Purpose: 
 */

#include "pep/PolicyEnforcementProcessor.h"

#include "constants/VersionConstants.h"
#include "constants/StatusCodeConstants.h"

#include "handlers/impl/UnmarshallerImpl.h"
#include "handlers/impl/MarshallerImpl.h"
#include "exceptions/InvalidSAMLRequestException.h"
#include "exceptions/InvalidSAMLResponseException.h"
#include "exceptions/InvalidSAMLAssertionException.h"

#include "exceptions/InvalidStateException.h"
#include "exceptions/InvalidRequestException.h"
#include "exceptions/InvalidResponseException.h"
#include "exceptions/PEPException.h"

#include "UnicodeStringConversion.h"

spep::PolicyEnforcementProcessor::PolicyEnforcementProcessor( spep::ReportingProcessor *reportingProcessor, spep::WSClient *wsClient, spep::SessionGroupCache *sessionGroupCache, spep::SessionCache *sessionCache, spep::Metadata *metadata, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, spep::KeyResolver *keyResolver, std::string schemaPath )
:
_localReportingProcessor( reportingProcessor->localReportingProcessor( "spep::PolicyEnforcementProcessor" ) ),
_sessionGroupCache( sessionGroupCache ),
_sessionCache( sessionCache ),
_metadata( metadata ),
_identifierGenerator( identifierGenerator ),
_samlValidator( samlValidator ),
_wsClient( wsClient )
{
	std::vector<std::string> lxacmlSchemaList;
	
	lxacmlSchemaList.push_back( ConfigurationConstants::lxacmlSAMLProtocol );
	lxacmlSchemaList.push_back( ConfigurationConstants::lxacmlSAMLAssertion );
	lxacmlSchemaList.push_back( ConfigurationConstants::lxacmlContext );
	lxacmlSchemaList.push_back( ConfigurationConstants::samlAssertion );
	
	std::vector<std::string> lxacmlGroupTargetSchemaList;
	lxacmlGroupTargetSchemaList.push_back( ConfigurationConstants::lxacmlGroupTarget );
	
	_lxacmlAuthzDecisionQueryMarshaller = new saml2::MarshallerImpl<middleware::lxacmlSAMLProtocolSchema::LXACMLAuthzDecisionQueryType>( schemaPath, lxacmlSchemaList, "LXACMLAuthzDecisionQuery", "http://www.qut.com/middleware/lxacmlSAMLProtocolSchema", keyResolver->getSPEPKeyName(), keyResolver->getSPEPPrivateKey() );
	_responseUnmarshaller = new saml2::UnmarshallerImpl<saml2::protocol::ResponseType>( schemaPath, lxacmlSchemaList, metadata );
	_lxacmlAuthzDecisionStatementUnmarshaller = new saml2::UnmarshallerImpl<middleware::lxacmlSAMLAssertionSchema::LXACMLAuthzDecisionStatementType>( schemaPath, lxacmlSchemaList, metadata );
	_groupTargetUnmarshaller = new saml2::UnmarshallerImpl<middleware::lxacmlGroupTargetSchema::GroupTargetType>( schemaPath, lxacmlGroupTargetSchemaList );
	
	std::vector<std::string> cacheClearSchemaList;
	
	cacheClearSchemaList.push_back( ConfigurationConstants::esoeProtocol );
	cacheClearSchemaList.push_back( ConfigurationConstants::samlAssertion );
	cacheClearSchemaList.push_back( ConfigurationConstants::samlProtocol );
	
	_clearAuthzCacheRequestUnmarshaller = new saml2::UnmarshallerImpl<middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType>( schemaPath, cacheClearSchemaList, metadata );
	_clearAuthzCacheResponseMarshaller = new saml2::MarshallerImpl<middleware::ESOEProtocolSchema::ClearAuthzCacheResponseType>( schemaPath, cacheClearSchemaList, "ClearAuthzCacheResponse", "http://www.qut.com/middleware/ESOEProtocolSchema", keyResolver->getSPEPKeyName(), keyResolver->getSPEPPrivateKey() );
}

spep::PolicyEnforcementProcessor::~PolicyEnforcementProcessor()
{
	delete _lxacmlAuthzDecisionQueryMarshaller;
	delete _responseUnmarshaller;
	delete _lxacmlAuthzDecisionStatementUnmarshaller;
	delete _groupTargetUnmarshaller;
	delete _clearAuthzCacheRequestUnmarshaller;
	delete _clearAuthzCacheResponseMarshaller;
}

void spep::PolicyEnforcementProcessor::makeAuthzDecision( spep::PolicyEnforcementProcessorData &data )
{
	this->_localReportingProcessor.log( DEBUG, "Going to make authz decision from group cache." );
	
	// Evaluate from the local cache
	Decision policyDecision = _sessionGroupCache->makeCachedAuthzDecision( data.getESOESessionID(), data.getResource() );
	
	// If the cache gave an authoritative answer, return it.
	if ( policyDecision == spep::Decision::PERMIT || policyDecision == spep::Decision::DENY )
	{
		this->_localReportingProcessor.log( DEBUG, "Cached authz decision was either permit or deny. Returning result." );
		
		data.setDecision( policyDecision );
		return;
	}
	
	// No value is cached, so we need to query the PDP.
	if ( policyDecision == spep::Decision::CACHE )
	{
		this->_localReportingProcessor.log( DEBUG, "No cached authz decision was found. Generating an authz decision query." );
		
		this->generateAuthzDecisionQuery( data );
		
		this->_localReportingProcessor.log( DEBUG, "Generated a query. Making web service call." );
		
		WSProcessorData wsData;
		wsData.setSAMLRequestDocument( data.getRequestDocument() );
		this->_wsClient->policyDecisionPoint( wsData, this->_metadata->getAuthzServiceEndpoint() );
		data.setResponseDocument( wsData.getSAMLResponseDocument() );
		
		this->_localReportingProcessor.log( DEBUG, "Got web service response. Processing authz decision statement." );
		
		this->processAuthzDecisionStatement( data );
		
		if( data.getDecision() == spep::Decision::PERMIT || data.getDecision() == spep::Decision::DENY )
		{
			this->_localReportingProcessor.log( DEBUG, "PDP authz decision was either permit or deny. Returning result." );
			return;
		}
	}
	
	if ( policyDecision != spep::Decision::ERROR )
	{
		this->_localReportingProcessor.log( ERROR, "An error condition was encountered after the PDP WS query but the policy decision was not set to ERROR" ); 
		// If there is no error at this stage, there is something very strange going on.
		throw InvalidStateException( "Invalid policy decision was encountered, but error was not set" );
	}
	
	// decision is ERROR already
	// data.setDecision( spep::Decision::ERROR );
	
	this->_localReportingProcessor.log( ERROR, "An error occurred during authz processing. Returning error condition" );
}

void spep::PolicyEnforcementProcessor::authzCacheClear( spep::PolicyEnforcementProcessorData &data )
{
	saml2::SAMLDocument requestDocument( data.getRequestDocument() );
	if ( requestDocument.getData() == NULL || requestDocument.getLength() <= 0 )
	{
		this->_localReportingProcessor.log( ERROR, "A NULL or empty Authz cache clear request was received." );
		throw PEPException( "Authz cache clear request was null or empty." );
	}
	
	this->_localReportingProcessor.log( DEBUG, "Going to unmarshal authz cache clear request." );
	
	std::auto_ptr<middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType> request;
	try
	{
		request.reset( _clearAuthzCacheRequestUnmarshaller->unMarshallSigned( requestDocument, true ) );
	}
	catch( saml2::UnmarshallerException &ex )
	{
		this->_localReportingProcessor.log( ERROR, "Couldn't unmarshal authz cache clear request. Message was: " + ex.getMessage() + ". Cause was: " + ex.getCause() );
		throw PEPException( "Couldn't unmarshal authz cache clear request" );
	}
		
	std::wstring statusCodeValue;
	std::wstring statusMessage;
	
	/*
	 * If the ESOE has sent a Subject, it refers to the principal we need to terminate. Otherwise we
	 * are terminating all
	 */
	if (request->Subject().present())
	{
		
		if( !request->Subject()->NameID().present() )
		{
			statusCodeValue = saml2::statuscode::REQUESTOR;
			statusMessage = L"Subject with no NameID present in request. The request is invalid.";
			
			this->_localReportingProcessor.log( ERROR, "The authz cache clear request Subject had no NameID. The request is invalid." );
		}
		else
		{
			// The subject's NameID would be the ESOE session ID
			std::wstring esoeSessionID( request->Subject()->NameID()->c_str() );
			// TODO Clear a single principal
			//_sessionGroupCache->clearPrincipalSession( esoeSessionID );
			
			statusCodeValue = saml2::statuscode::SUCCESS;
			// TODO status message
		}
		
		this->generateClearAuthzCacheResponse( data, request->ID(), statusMessage, statusCodeValue );
		
		return;
	}
	
	std::map< UnicodeString, std::vector<UnicodeString> > groupTargets;
	
	// Extensions element is expected to contain GroupTarget elements.
	if (request->Extensions().present())
	{
		this->_localReportingProcessor.log( DEBUG, "Got an Extensions element in this Authz cache clear request. Processing group targets." );
		// Loop through the Extensions to find <GroupTarget> elements.
		xercesc::DOMNode *node = request->Extensions()->_node();
		xercesc::DOMNodeList *childNodes = node->getChildNodes();
		for( XMLSize_t i = 0; i < childNodes->getLength(); ++i )
		{
			
			xercesc::DOMElement *element = (xercesc::DOMElement*)childNodes->item(i);
			const XMLCh* localNameXMLString = element->getLocalName();
			
			std::auto_ptr<XercesCharStringAdapter> localName( new XercesCharStringAdapter( XMLString::transcode( localNameXMLString ) ) );
			this->_localReportingProcessor.log( DEBUG, std::string("Got local name: ") + localName->get() );
			
			if ( std::string("GroupTarget").compare( 0, XMLString::stringLen( localNameXMLString ), localName->get() ) == 0 )
			{
				
				std::auto_ptr<middleware::lxacmlGroupTargetSchema::GroupTargetType> groupTarget
					( _groupTargetUnmarshaller->unMarshallUnSignedElement( element ) );
				
				UnicodeString groupTargetID( UnicodeStringConversion::toUnicodeString( groupTarget->GroupTargetID() ) );
				std::vector<UnicodeString>& authzTargets = groupTargets[groupTargetID];
				
				// Add all the <AuthzTarget> elements to the list.
				middleware::lxacmlGroupTargetSchema::GroupTargetType::AuthzTarget::iterator authzTargetIterator;
				for( authzTargetIterator = groupTarget->AuthzTarget().begin();
					authzTargetIterator != groupTarget->AuthzTarget().end();
					++authzTargetIterator )
				{
					UnicodeString authzTarget( UnicodeStringConversion::toUnicodeString( std::wstring( authzTargetIterator->c_str(), authzTargetIterator->length() ) ) );
					authzTargets.push_back( authzTarget );
				}
				
				this->_localReportingProcessor.log( DEBUG, "Added " + boost::lexical_cast<std::string>( authzTargets.size() ) + " authz targets for group target " + UnicodeStringConversion::toString( groupTargetID ) );
				
			}
			
		}
	}
		
	try
	{
		this->_sessionGroupCache->clearCache( groupTargets );
		
		this->_localReportingProcessor.log( INFO, "Authorization cache clear succeeded. Flushed all cached authz decisions and created new cache with " + boost::lexical_cast<std::string>( groupTargets.size() ) + " group targets." );

		statusCodeValue = saml2::statuscode::SUCCESS;
		statusMessage = L"The authorization cache was cleared successfully.";
	}
	catch( std::exception &ex )
	{
		this->_localReportingProcessor.log( ERROR, std::string("An exception was thrown when trying to clear the authz cache. Exception was: ") + ex.what() );
		
		statusCodeValue = saml2::statuscode::RESPONDER;
		statusMessage = L"An exception occurred while trying to clear the authz cache.";
	}
	
	// Finished, one way or the other generate a response.
	this->generateClearAuthzCacheResponse( data, request->ID(), statusMessage, statusCodeValue );
	return;
}

void spep::PolicyEnforcementProcessor::generateClearAuthzCacheResponse( spep::PolicyEnforcementProcessorData &data, std::wstring &inResponseTo, std::wstring &statusMessage, std::wstring &statusCodeValue )
{
	
	// Build the XML object.
	saml2::assertion::NameIDType issuer( _metadata->getSPEPIdentifier() );

	std::wstring samlID( _identifierGenerator->generateSAMLID() );
	std::vector<std::string> idList;
	idList.push_back( UnicodeStringConversion::toString(samlID) );
	
	saml2::protocol::StatusType status;
	saml2::protocol::StatusCodeType statusCode( statusCodeValue );
	status.StatusCode( statusCode );
	status.StatusMessage( statusMessage );
		
	middleware::ESOEProtocolSchema::ClearAuthzCacheResponseType response;
	response.ID( samlID );
	response.InResponseTo( inResponseTo );
	response.IssueInstant( xml_schema::date_time() );
	response.Version( saml2::versions::SAML_20 );

	response.Issuer( issuer );
	response.Status( status );
	
	// Marshal and set in data object.
	data.setResponseDocument( _clearAuthzCacheResponseMarshaller->marshallSigned( &response, idList ) );
	
	this->_localReportingProcessor.log( DEBUG, "Generated authz cache clear response with status code: " + UnicodeStringConversion::toString( statusCodeValue ) + " and status message: " + UnicodeStringConversion::toString( statusMessage ) );
}

void spep::PolicyEnforcementProcessor::generateAuthzDecisionQuery( spep::PolicyEnforcementProcessorData &data )
{
	this->_localReportingProcessor.log( DEBUG, "About to generate authz decision query." );
	
	std::vector<std::string> idList;
	
	// <Resource> tag to describe the resource being accessed
	middleware::lxacmlContextSchema::ResourceType resource;
	middleware::lxacmlContextSchema::AttributeType resourceAttribute;
	middleware::lxacmlContextSchema::AttributeValueType resourceAttributeValue;
	resourceAttributeValue.Value( UnicodeStringConversion::toWString( data.getResource() ) );
	resourceAttribute.AttributeValue( resourceAttributeValue );
	resource.Attribute( resourceAttribute );
	
	// <Subject> tag to describe the principal requesting the resource
	middleware::lxacmlContextSchema::SubjectType subject;
	middleware::lxacmlContextSchema::AttributeType subjectAttribute;
	middleware::lxacmlContextSchema::AttributeValueType subjectAttributeValue;
	subjectAttributeValue.Value(data.getESOESessionID());
	subjectAttribute.AttributeValue( subjectAttributeValue );
	subject.Attribute( subjectAttribute );
	
	// Put them into a <lxacml:Request>
	middleware::lxacmlContextSchema::RequestType request;
	request.Resource( resource );
	request.Subject( subject );
	
	saml2::assertion::NameIDType issuer( _metadata->getSPEPIdentifier() );
	
	std::wstring samlID = _identifierGenerator->generateSAMLID();
	idList.push_back( UnicodeStringConversion::toString(samlID) );
	
	// Create the SAML request.
	middleware::lxacmlSAMLProtocolSchema::LXACMLAuthzDecisionQueryType authzQuery;
	authzQuery.Request( request );
	authzQuery.ID( samlID );
	authzQuery.IssueInstant( xml_schema::date_time() );
	authzQuery.Version( saml2::versions::SAML_20 );
	authzQuery.Issuer( issuer );
	
	this->_localReportingProcessor.log( DEBUG, "Going to marshal authz decision query." );
	
	// Marshal and return.
	try
	{
		data.setRequestDocument( _lxacmlAuthzDecisionQueryMarshaller->marshallSigned( &authzQuery, idList ) );
	}
	catch( saml2::MarshallerException &ex )
	{
		this->_localReportingProcessor.log( ERROR, "Failed to marshal authz decision query. Message was: " + ex.getMessage() + ". Cause was: " + ex.getCause() );
		data.setRequestDocument( saml2::SAMLDocument() );
	}
}

void spep::PolicyEnforcementProcessor::processAuthzDecisionStatement( spep::PolicyEnforcementProcessorData &data )
{
	saml2::SAMLDocument responseDocument( data.getResponseDocument() );
	if ( responseDocument.getData() == NULL || responseDocument.getLength() <= 0 )
	{
		throw PEPException( "LXACML response document was NULL or empty. Unable to perform authorization." );
	}
	
	this->_localReportingProcessor.log( DEBUG, UnicodeStringConversion::toString( UnicodeStringConversion::toUnicodeString(responseDocument) ) );
	
	// Keep the DOM so we can process Obligations correctly.
	std::auto_ptr<saml2::protocol::ResponseType> response( _responseUnmarshaller->unMarshallSigned( responseDocument, true ) );
	
	if( saml2::statuscode::AUTHN_FAILED.compare( response->Status().StatusCode().Value().c_str() ) == 0 )
	{
		this->_localReportingProcessor.log( ERROR, "PDP rejected session identifier with Authn Fail status. Terminating principal session." );
		data.setDecision( spep::Decision::DENY );
		this->_sessionCache->terminatePrincipalSession( data.getESOESessionID() );
		return;
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
		throw PEPException( "SAML response was rejected by SAML Validator." );
	}
	catch( std::exception &ex )
	{
		// Error occurred validating the response. Reject it anyway.
		this->_localReportingProcessor.log( ERROR, "Error occurred in the SAML Validator. Message: " + std::string(ex.what()) );
		throw PEPException( "Error occurred in the SAML Validator." );
	}
	
	// Loop through <Assertion> elements.
	saml2::protocol::ResponseType::Assertion::iterator assertionIterator;
	for( assertionIterator = response->Assertion().begin();
		assertionIterator != response->Assertion().end();
		++assertionIterator )
	{
		
		try
		{
			// Validate the SAML Assertion.
			this->_samlValidator->getAssertionValidator().validate( &(*assertionIterator) );
		}
		catch( saml2::InvalidSAMLAssertionException &ex )
		{
			// Assertion was rejected explicitly.
			this->_localReportingProcessor.log( ERROR, "SAML assertion was rejected by SAML Validator. Reason: " + ex.getMessage() );
			throw PEPException( "SAML assertion was rejected by SAML Validator." );
		}
		catch( std::exception &ex )
		{
			// Error occurred validating the assertion. Reject it anyway.
			this->_localReportingProcessor.log( ERROR, "Error occurred in the SAML Validator. Message: " + std::string(ex.what()) );
			throw PEPException( "Error occurred in the SAML Validator." );
		}
		
		// TODO Validate subject confirmation data
		
		saml2::assertion::AssertionType::Statement::iterator statementIterator;
		for( statementIterator = assertionIterator->Statement().begin();
			statementIterator != assertionIterator->Statement().end();
			++statementIterator )
		{
			
			// Get the DOMElement from the object and retrieve its local name
			xercesc::DOMElement *domElement = (xercesc::DOMElement*)statementIterator->_node();
			
			// The attribute name for the "any" element type, and the expected value of that element.
			std::auto_ptr<XercesXMLChStringAdapter> attributeName( new XercesXMLChStringAdapter( XMLString::transcode("xsi:type") ) );
			std::auto_ptr<XercesXMLChStringAdapter> expectedXSITypeValue( new XercesXMLChStringAdapter( XMLString::transcode("lxacmla:LXACMLAuthzDecisionStatementType") ) );
			
			// Check if we have the right type of element
			const XMLCh *xsiTypeValue = domElement->getAttribute( attributeName->get() );
			if( xsiTypeValue != NULL && XMLString::compareString( xsiTypeValue, expectedXSITypeValue->get() ) == 0 )
			{
				
				// Using not_root as a flag in unMarshallUnSignedElement means that the domElement is not "owned" by the XSD
				// object even though it is still kept.
				std::auto_ptr<middleware::lxacmlSAMLAssertionSchema::LXACMLAuthzDecisionStatementType> lxacmlAuthzDecisionStatement
					( this->_lxacmlAuthzDecisionStatementUnmarshaller->unMarshallUnSignedElement( domElement, true ) );
				
				// If this XSD object was created, it must be schema valid. So, the Response and Result elements 
				// must be present. The same can be said for Decision
				middleware::lxacmlContextSchema::ResultType &result = lxacmlAuthzDecisionStatement->Response().Result();
				// operator _xsd_DecisionType() is defined, so we can just cast.
				middleware::lxacmlContextSchema::DecisionType::_xsd_DecisionType decision = 
					(middleware::lxacmlContextSchema::DecisionType::_xsd_DecisionType)result.Decision();
					
				// Check the decision from the document and set it in the data object.					
				if ( decision == middleware::lxacmlContextSchema::DecisionType::Permit )
				{
					this->_localReportingProcessor.log( AUTHZ, UnicodeStringConversion::toString( data.getResource() ) + " <- ESOE Session[" + UnicodeStringConversion::toString( data.getESOESessionID() ) + "] result from PDP is PERMIT" );  
					data.setDecision( spep::Decision::PERMIT );
				}
				else if ( decision == middleware::lxacmlContextSchema::DecisionType::Deny )
				{
					this->_localReportingProcessor.log( AUTHZ, UnicodeStringConversion::toString( data.getResource() ) + " <- ESOE Session[" + UnicodeStringConversion::toString( data.getESOESessionID() ) + "] result from PDP is DENY" );  
					data.setDecision( spep::Decision::DENY );
				}
				else
				{
					this->_localReportingProcessor.log( ERROR, UnicodeStringConversion::toString( data.getResource() ) + " <- ESOE Session[" + UnicodeStringConversion::toString( data.getESOESessionID() ) + "] Erroneous result from PDP. Failing" );
					// No known decision. Error condition.
					data.setDecision( spep::Decision::ERROR );
				}
				
				this->_localReportingProcessor.log( DEBUG, "Going to process obligations for authz decision statement." );
				this->processObligations( data, result.Obligations() );
				
			}
		}
		
	}
}

void spep::PolicyEnforcementProcessor::processObligations( spep::PolicyEnforcementProcessorData &data, middleware::lxacmlSchema::ObligationsType &obligations )
{
	
	// Loop through the <Obligation> elements.
	middleware::lxacmlSchema::ObligationsType::Obligation::iterator obligationIterator;
	for( obligationIterator = obligations.Obligation().begin();
		obligationIterator != obligations.Obligation().end();
		++obligationIterator )
	{
		
		this->_localReportingProcessor.log( DEBUG, "Got obligation with ObligationId=" + UnicodeStringConversion::toString(obligationIterator->ObligationId().c_str()) );
		
		// Check if the obligation ID is equal to the one we are to process.
		if ( obligationIterator->ObligationId().compare( 0, wcslen(OBLIGATION_ID), OBLIGATION_ID ) == 0 )
		{
			
			this->_localReportingProcessor.log( DEBUG, "ObligationId matched. Checking FulfillOn value." );
			
			// operator _xsd_EffectType() is defined, so we can just cast.
			middleware::lxacmlSchema::EffectType::_xsd_EffectType effect = 
				(middleware::lxacmlSchema::EffectType::_xsd_EffectType)obligationIterator->FulfillOn();
			
			// Check that the decision matches the FulfillOn value.
			if ( ( data.getDecision() == spep::Decision::PERMIT && effect == middleware::lxacmlSchema::EffectType::Permit )
				|| ( data.getDecision() == spep::Decision::DENY && effect == middleware::lxacmlSchema::EffectType::Deny ) )
			{
				
				this->_localReportingProcessor.log( DEBUG, "FulfillOn value matched current decision. Processing attribute assignments." );

				// Process the attribute assignments.
				middleware::lxacmlSchema::ObligationType::AttributeAssignment::iterator attributeAssignmentIterator;
				for( attributeAssignmentIterator = obligationIterator->AttributeAssignment().begin();
					attributeAssignmentIterator != obligationIterator->AttributeAssignment().end();
					++attributeAssignmentIterator )
				{
					
					// Check the AttributeId and if it is not the same go to the next loop iteration.
					std::wstring attributeID = attributeAssignmentIterator->AttributeId();
					if ( attributeID.compare( 0, wcslen(ATTRIBUTE_ID), ATTRIBUTE_ID ) != 0 )
						continue;
					
					this->_localReportingProcessor.log( DEBUG, "Matched attribute ID for current attribute. Processing group targets." );

					// For each child node of this <AttributeAssignment> element.
					xercesc::DOMNode *node = attributeAssignmentIterator->_node();
					xercesc::DOMNodeList *childNodes = node->getChildNodes();
					for( XMLSize_t i = 0; i < childNodes->getLength(); ++i )
					{
						
						// Find only the <GroupTarget> elements.
						xercesc::DOMElement *element = (xercesc::DOMElement*)childNodes->item(i);
						const XMLCh* localNameXMLString = element->getLocalName();
						std::auto_ptr<XercesCharStringAdapter> localName( new XercesCharStringAdapter( XMLString::transcode( localNameXMLString ) ) );
						
						if ( std::string("GroupTarget").compare( 0, XMLString::stringLen( localNameXMLString ), localName->get() ) == 0 )
						{
							std::auto_ptr<middleware::lxacmlGroupTargetSchema::GroupTargetType> groupTarget
								( _groupTargetUnmarshaller->unMarshallUnSignedElement( element ) );
							
							UnicodeString groupTargetID( UnicodeStringConversion::toUnicodeString( std::wstring(groupTarget->GroupTargetID()) ) );
							std::vector<UnicodeString> authzTargets;
							
							this->_localReportingProcessor.log( DEBUG, "Current element is a group target. Group target ID: " + UnicodeStringConversion::toString( groupTargetID ) );
							
							// Loop through the <AuthzTarget> elements.
							middleware::lxacmlGroupTargetSchema::GroupTargetType::AuthzTarget::iterator authzTargetIterator;
							for( authzTargetIterator = groupTarget->AuthzTarget().begin();
								authzTargetIterator != groupTarget->AuthzTarget().end();
								++authzTargetIterator )
							{
								
								UnicodeString authzTarget( UnicodeStringConversion::toUnicodeString( std::wstring(authzTargetIterator->c_str()) ) );
								authzTargets.push_back( authzTarget );
								this->_localReportingProcessor.log( DEBUG, "Adding to group target " + UnicodeStringConversion::toString( groupTargetID ) + " authz target: " + UnicodeStringConversion::toString( authzTarget ) );
								
							}
							
							std::wstring esoeSessionID( data.getESOESessionID() );
							// TODO Check this for possible concurrency issues.
							_sessionGroupCache->updateCache( esoeSessionID, groupTargetID, authzTargets, data.getDecision() );
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
}
