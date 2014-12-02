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
 * Purpose: Interprets attributes passed between IDP and SPEP
 */

#include "spep/attribute/AttributeProcessor.h"
#include "spep/UnicodeStringConversion.h"
#include "spep/exceptions/AttributeException.h"
#include "spep/exceptions/InvalidRequestException.h"
#include "spep/exceptions/InvalidResponseException.h"

#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMNode.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMTypeInfo.hpp>

#include "saml2/constants/VersionConstants.h"
#include "saml2/exceptions/InvalidSAMLResponseException.h"
#include "saml2/exceptions/InvalidSAMLAssertionException.h"
#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"

XERCES_CPP_NAMESPACE_USE

spep::AttributeProcessor::AttributeProcessor( saml2::Logger *logger, spep::Metadata *metadata, spep::KeyResolver *keyResolver, saml2::IdentifierGenerator *identifierGenerator, spep::WSClient *wsClient, saml2::SAMLValidator *samlValidator, const std::string& schemaPath, const std::map<std::string,std::string>& attributeRenameMap )
:
mLocalLogger( logger, "spep::AttributeProcessor" ),
mMetadata( metadata ),
mKeyResolver( keyResolver ),
mIdentifierGenerator( identifierGenerator ),
mWSClient( wsClient ),
mSamlValidator( samlValidator )
{
    std::vector<std::string> schemaList{ ConfigurationConstants::samlAssertion, ConfigurationConstants::samlProtocol };
		
	mAttributeQueryMarshaller = std::make_unique<saml2::MarshallerImpl<saml2::protocol::AttributeQueryType>>( 
		logger, schemaPath, schemaList, "AttributeQuery", "urn:oasis:names:tc:SAML:2.0:protocol", 
		keyResolver->getSPEPKeyAlias(), keyResolver->getSPEPPrivateKey()
	);
	mResponseUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<saml2::protocol::ResponseType>>(logger, schemaPath, schemaList, mMetadata);
	
// 	for (std::map<std::string,std::string>::const_iterator iter = attributeRenameMap.begin();
// 		iter != attributeRenameMap.end(); ++iter )
// 	{
// 		this->mAttributeRenameMap[ UnicodeStringConversion::toUnicodeString( iter->first ) ] = UnicodeStringConversion::toUnicodeString( iter->second );
// 	}

    for (const auto& iter: attributeRenameMap)
    {
        mAttributeRenameMap[UnicodeStringConversion::toUnicodeString(iter.first)] = UnicodeStringConversion::toUnicodeString(iter.second);
    }
}

spep::AttributeProcessor::~AttributeProcessor()
{
}

void spep::AttributeProcessor::doAttributeProcessing(spep::PrincipalSession &principalSession)
{
	// Validate the ESOE session identifier
	if (principalSession.getESOESessionID().empty())
	{
		mLocalLogger.error() << "Rejecting attribute processing request for principal session with empty ESOE session identifier.";
		SAML2LIB_INVPARAM_EX("Principal session had an empty ESOE session identifier. Unable to get attributes.");
	}
	
	mLocalLogger.debug() << "Building attribute query for new principal session. ESOE Session ID: " << UnicodeStringConversion::toString( principalSession.getESOESessionID() );

	// Build the attribute query
	std::wstring samlID = mIdentifierGenerator->generateSAMLID();
	
	xml_schema::dom::auto_ptr<DOMDocument> requestDocument(buildAttributeQuery(principalSession, samlID));
	
	if (requestDocument.get() == NULL)
	{
		throw AttributeException("The request document was null. Aborting attribute query.");
	}
	
	std::auto_ptr<saml2::protocol::ResponseType> response;
	try
	{
		std::string endpoint(mMetadata->getAttributeServiceEndpoint());
		mLocalLogger.debug() << "Doing attribute query to endpoint: " << endpoint;
		response.reset(mWSClient->doWSCall(endpoint, requestDocument.get(), mResponseUnmarshaller.get()));
	}
	catch (std::exception& ex)
	{
		mLocalLogger.error() << "An exception occurred during the attribute web service call. Unable to continue. "
			"Exception was: " << ex.what();
		throw AttributeException("An exception occurred during the attribute web service call. Unable to continue.");
	}
	
	mLocalLogger.debug() << "Got attribute response document. Going to process attribute statements.";
	
	// Process the response
	try
	{
		mLocalLogger.debug() << "Processing attribute response.";
		processAttributeResponse(response.get(), principalSession, samlID);
	}
	catch (std::exception& ex)
	{
		mLocalLogger.error() << "An exception occurred while processing the attribute response. Exception was: " << ex.what();
		throw AttributeException("An exception occurred while processing the attribute response.");
	}
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::AttributeProcessor::buildAttributeQuery(spep::PrincipalSession &principalSession, const std::wstring &samlID)
{	
	// Subject is ESOE session identifier.
	saml2::assertion::SubjectType subject;
	saml2::assertion::NameIDType subjectNameID(principalSession.getESOESessionID());
	subject.NameID(subjectNameID);
	
	// Set the issuer to be "this" SPEP
	saml2::assertion::NameIDType issuer(mMetadata->getSPEPIdentifier());
	
	saml2::protocol::AttributeQueryType attributeQuery;
	// Use the ID given to us, because that's what will be expected in the InReponseTo field of the response.
	attributeQuery.ID(samlID);
    std::vector<std::string> identifierList(1, UnicodeStringConversion::toString(samlID));
	attributeQuery.Version(saml2::versions::SAML_20);
	// Set to the current time.
	attributeQuery.IssueInstant(xml_schema::date_time());
	attributeQuery.Subject(subject);
	attributeQuery.Issuer(issuer);
	
	try
	{
		std::string samlIDString(UnicodeStringConversion::toString(samlID));
		mLocalLogger.debug() << "Marshalling attribute query document with SAML ID: " << samlIDString;
		
		// Marshal the request.
		DOMDocument *requestDocument = mAttributeQueryMarshaller->generateDOMDocument(&attributeQuery);
		requestDocument = mAttributeQueryMarshaller->validate(requestDocument);
        mAttributeQueryMarshaller->sign(requestDocument, identifierList);
		
		mLocalLogger.debug() << "Marshalled attribute query document successfully. SAML ID: " << samlIDString;
		
		return requestDocument;
	}
	catch (saml2::MarshallerException &ex)
	{
		mLocalLogger.error() << "Failed to marshal attribute query document. Exception was: " << ex.getMessage() << ". Cause was: " << ex.getCause();
		throw AttributeException("Unable to marshal attribute query document.");
	}
}

void spep::AttributeProcessor::processAttributeResponse(saml2::protocol::ResponseType *response, spep::PrincipalSession &principalSession, const std::wstring &samlID)
{
	if (response == nullptr)
	{
		mLocalLogger.error() << "Attribute Response is NULL or empty. Failing attribute processing.";
		throw AttributeException("Attribute Response is NULL or empty. Failing attribute processing.");
	}
	
	try
	{
		// Validate the SAML Response.
		mSamlValidator->getResponseValidator().validate(response);
	}
	catch (saml2::InvalidSAMLResponseException &ex)
	{
		// Response was rejected explicitly.
		mLocalLogger.error() << "SAML response was rejected by SAML Validator. Reason: " << ex.getMessage();
		throw AttributeException("SAML response was rejected by SAML Validator.");
	}
	catch (std::exception &ex)
	{
		// Error occurred validating the response. Reject it anyway.
		mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << ex.what();
		throw AttributeException("Error occurred in the SAML Validator.");
	}
	
	// TODO Verify the issuer
	
	std::vector<saml2::assertion::AttributeStatementType*> attributeStatements;
	
	// Loop through all the Assertions in the response
	saml2::protocol::ResponseType::Assertion_iterator assertionIterator;
	for (assertionIterator = response->Assertion().begin();
		assertionIterator != response->Assertion().end();
		++assertionIterator)
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
			throw AttributeException("SAML assertion was rejected by SAML Validator.");
		}
		catch (std::exception &ex)
		{
			// Error occurred validating the assertion. Reject it anyway.
			mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << ex.what();
			throw AttributeException("Error occurred in the SAML Validator.");
		}
		
		// TODO Validate recipient
		// Validate subject confirmation data
		
		saml2::assertion::AssertionType::AttributeStatement_iterator attributeStatementIterator;
		for (attributeStatementIterator = assertionIterator->AttributeStatement().begin();
			attributeStatementIterator != assertionIterator->AttributeStatement().end();
			++attributeStatementIterator)
		{
			
			// Take a pointer of the attribute statement and put it in the list to be processed
			attributeStatements.push_back(&(*attributeStatementIterator));
		}
		
	}
	
	mLocalLogger.debug() << "Attribute statements found: " << attributeStatements.size() << ". Going to process.";
	// Process the attribute statements
	processAttributeStatements(attributeStatements, principalSession);
}

void spep::AttributeProcessor::processAttributeStatements(AttributeStatementPointerList &attributeStatements, spep::PrincipalSession &principalSession)
{
	// Loop through all the AttributeStatement elements in the list.
	AttributeStatementPointerList::iterator attributeStatementIterator;
	for (attributeStatementIterator = attributeStatements.begin(); 
		attributeStatementIterator != attributeStatements.end(); 
		++attributeStatementIterator)
	{
		saml2::assertion::AttributeStatementType *attributeStatement = *attributeStatementIterator;
		
		// Loop through the attributes in the statement.
		saml2::assertion::AttributeStatementType::Attribute_iterator attributeIterator;
		for (attributeIterator = attributeStatement->Attribute().begin();
			attributeIterator != attributeStatement->Attribute().end();
			++attributeIterator)
		{
			UnicodeString attributeNameText(UnicodeStringConversion::toUnicodeString(attributeIterator->Name().c_str()));
			
			std::map<UnicodeString,UnicodeString>::iterator renameIterator = mAttributeRenameMap.find(attributeNameText);
			if (renameIterator != mAttributeRenameMap.end())
			{
				mLocalLogger.debug() << "Found rename for attribute " << UnicodeStringConversion::toString(attributeNameText) << "... renaming to " << UnicodeStringConversion::toString(renameIterator->second);
				attributeNameText = renameIterator->second;
			}
			
			// This will either a) create an empty list, or b) let us append to the existing list of values
			std::vector<UnicodeString>& attributeValueList = principalSession.getAttributeMap()[attributeNameText];

			mLocalLogger.debug() << "Current attribute: " << UnicodeStringConversion::toString(attributeNameText) << ". " << attributeValueList.size() << " value(s) already populated.";

			// Loop through the attribute values.
			saml2::assertion::AttributeType::AttributeValue_iterator attributeValueIterator;
			for (attributeValueIterator = attributeIterator->AttributeValue().begin();
				attributeValueIterator != attributeIterator->AttributeValue().end();
				++attributeValueIterator)
			{
				
				// Add the attribute value into the list.
				DOMNode *attributeValueNode = attributeValueIterator->_node();
				if (attributeValueNode->getNodeType() == DOMNode::ELEMENT_NODE)
				{
					
					DOMNode *attributeValueChildNode = attributeValueNode->getFirstChild();
					while (attributeValueChildNode != NULL)
					{
						if (attributeValueChildNode->getNodeType() == DOMNode::TEXT_NODE)
						{
							DOMText *attributeValueTextNode = static_cast<DOMText*>(attributeValueChildNode);
							UnicodeString attributeValueText(UnicodeStringConversion::toUnicodeString(attributeValueTextNode->getData()));
							
							mLocalLogger.debug() << "Adding value: " << UnicodeStringConversion::toString(attributeNameText) << " = " << UnicodeStringConversion::toString(attributeValueText);
							attributeValueList.push_back(attributeValueText);
							
							break;
						}
						else
						{
							attributeValueChildNode = attributeValueChildNode->getNextSibling();
						}
					}
				}
				
			}
			
			mLocalLogger.debug() << "Finished processing attributes for " << UnicodeStringConversion::toString(attributeNameText) << ". " << attributeValueList.size() << " attributes in value list now.";
		}
		
	}
	
}
