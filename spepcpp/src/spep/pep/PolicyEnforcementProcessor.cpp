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

#include "spep/pep/PolicyEnforcementProcessor.h"

#include "saml2/constants/VersionConstants.h"
#include "saml2/constants/StatusCodeConstants.h"

#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"
#include "saml2/exceptions/InvalidSAMLRequestException.h"
#include "saml2/exceptions/InvalidSAMLResponseException.h"
#include "saml2/exceptions/InvalidSAMLAssertionException.h"

#include "spep/exceptions/InvalidStateException.h"
#include "spep/exceptions/InvalidRequestException.h"
#include "spep/exceptions/InvalidResponseException.h"
#include "spep/exceptions/PEPException.h"

#include "spep/UnicodeStringConversion.h"

spep::PolicyEnforcementProcessor::PolicyEnforcementProcessor(saml2::Logger *logger, spep::WSClient *wsClient, spep::SessionGroupCache *sessionGroupCache, spep::SessionCache *sessionCache, spep::Metadata *metadata, saml2::IdentifierGenerator *identifierGenerator, saml2::SAMLValidator *samlValidator, spep::KeyResolver *keyResolver, const std::string& schemaPath) :
    mLocalLogger(logger, "spep::PolicyEnforcementProcessor"),
    mSessionGroupCache(sessionGroupCache),
    mSessionCache(sessionCache),
    mMetadata(metadata),
    mIdentifierGenerator(identifierGenerator),
    mSamlValidator(samlValidator),
    mWSClient(wsClient)
{
    std::vector<std::string> lxacmlSchemaList{ ConfigurationConstants::lxacmlSAMLProtocol,
        ConfigurationConstants::lxacmlSAMLAssertion,
        ConfigurationConstants::lxacmlContext,
        ConfigurationConstants::samlAssertion };

    std::vector<std::string> lxacmlGroupTargetSchemaList{ ConfigurationConstants::lxacmlGroupTarget };

    mLxacmlAuthzDecisionQueryMarshaller = std::make_unique<saml2::MarshallerImpl<middleware::lxacmlSAMLProtocolSchema::LXACMLAuthzDecisionQueryType>>(
        logger, schemaPath, lxacmlSchemaList, "LXACMLAuthzDecisionQuery", "http://www.qut.com/middleware/lxacmlSAMLProtocolSchema",
        keyResolver->getSPEPKeyAlias(), keyResolver->getSPEPPrivateKey()
        );

    mResponseUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<saml2::protocol::ResponseType>>
        (logger, schemaPath, lxacmlSchemaList, metadata);

    mLxacmlAuthzDecisionStatementUnmarshaller = std::make_unique<saml2::UnmarshallerImpl
        <middleware::lxacmlSAMLAssertionSchema::LXACMLAuthzDecisionStatementType>>
        (logger, schemaPath, lxacmlSchemaList, metadata);

    mGroupTargetUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<middleware::lxacmlGroupTargetSchema::GroupTargetType>>
        (logger, schemaPath, lxacmlGroupTargetSchemaList);

    std::vector<std::string> cacheClearSchemaList{
        ConfigurationConstants::esoeProtocol,
        ConfigurationConstants::samlAssertion,
        ConfigurationConstants::samlProtocol };

    mClearAuthzCacheRequestUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType>>
        (logger, schemaPath, cacheClearSchemaList, metadata);
    mClearAuthzCacheResponseMarshaller = std::make_unique<saml2::MarshallerImpl<middleware::ESOEProtocolSchema::ClearAuthzCacheResponseType>>
        (logger, schemaPath, cacheClearSchemaList, "ClearAuthzCacheResponse", "http://www.qut.com/middleware/ESOEProtocolSchema",
        keyResolver->getSPEPKeyAlias(), keyResolver->getSPEPPrivateKey());
}

spep::PolicyEnforcementProcessor::~PolicyEnforcementProcessor()
{
}

void spep::PolicyEnforcementProcessor::makeAuthzDecision(spep::PolicyEnforcementProcessorData &data)
{
    mLocalLogger.debug() << "Going to make authz decision from group cache.";

    // Evaluate from the local cache
    Decision policyDecision = mSessionGroupCache->makeCachedAuthzDecision(data.getESOESessionID(), data.getResource());

    // If the cache gave an authoritative answer, return it.
    if (policyDecision == spep::Decision::PERMIT || policyDecision == spep::Decision::DENY)
    {
        mLocalLogger.debug() << "Cached authz decision was either permit or deny. Returning result.";

        data.setDecision(policyDecision);
        return;
    }

    // No value is cached, so we need to query the PDP.
    if (policyDecision == spep::Decision::CACHE)
    {
        mLocalLogger.debug() << "No cached authz decision was found. Generating an authz decision query.";

        xml_schema::dom::auto_ptr<DOMDocument> requestDocument(generateAuthzDecisionQuery(data));

        mLocalLogger.debug() << "Generated a query. Making web service call.";

        std::auto_ptr<saml2::protocol::ResponseType> response;
        try
        {
            response.reset(mWSClient->doWSCall(mMetadata->getAuthzServiceEndpoint(), requestDocument.get(), mResponseUnmarshaller.get()));
        }
        catch (saml2::UnmarshallerException &ex)
        {
            throw PEPException("Failed to unmarshal the response. Message was: " + ex.getMessage());
        }

        mLocalLogger.debug() << "Got web service response. Processing authz decision statement.";

        this->processAuthzDecisionStatement(data, response.get());

        if (data.getDecision() == spep::Decision::PERMIT || data.getDecision() == spep::Decision::DENY)
        {
            mLocalLogger.debug() << "PDP authz decision was either permit or deny. Returning result.";
            return;
        }
    }

    if (policyDecision != spep::Decision::ERROR)
    {
        mLocalLogger.error() << "An error condition was encountered after the PDP WS query but the policy decision was not set to ERROR";
        // If there is no error at this stage, there is something very strange going on.
        throw InvalidStateException("Invalid policy decision was encountered, but error was not set");
    }

    // decision is ERROR already
    // data.setDecision( spep::Decision::ERROR );

    mLocalLogger.error() << "An error occurred during authz processing. Returning error condition";
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::PolicyEnforcementProcessor::authzCacheClear(middleware::ESOEProtocolSchema::ClearAuthzCacheRequestType *request)
{
    std::wstring statusCodeValue;
    std::wstring statusMessage;

    // TODO Should this be validated? I think so.

    /*
     * If the ESOE has sent a Subject, it refers to the principal we need to terminate. Otherwise we
     * are terminating all
     */
    if (request->Subject().present())
    {
        if (!request->Subject()->NameID().present())
        {
            statusCodeValue = saml2::statuscode::REQUESTOR;
            statusMessage = L"Subject with no NameID present in request. The request is invalid.";

            mLocalLogger.error() << "The authz cache clear request Subject had no NameID. The request is invalid.";
        }
        else
        {
            // The subject's NameID would be the ESOE session ID
            std::wstring esoeSessionID(request->Subject()->NameID()->c_str());
            // TODO Clear a single principal
            //_sessionGroupCache->clearPrincipalSession( esoeSessionID );

            statusCodeValue = saml2::statuscode::SUCCESS;
            // TODO status message
        }

        return generateClearAuthzCacheResponse(request->ID(), statusMessage, statusCodeValue);
    }

    std::map< UnicodeString, std::vector<UnicodeString> > groupTargets;

    // Extensions element is expected to contain GroupTarget elements.
    if (request->Extensions().present())
    {
        mLocalLogger.debug() << "Got an Extensions element in this Authz cache clear request. Processing group targets.";
        // Loop through the Extensions to find <GroupTarget> elements.
        xercesc::DOMNode *node = request->Extensions()->_node();
        xercesc::DOMNodeList *childNodes = node->getChildNodes();
        for (XMLSize_t i = 0; i < childNodes->getLength(); ++i)
        {
            xercesc::DOMElement *element = (xercesc::DOMElement*)childNodes->item(i);
            const XMLCh* localNameXMLString = element->getLocalName();

            std::auto_ptr<XercesCharStringAdapter> localName(new XercesCharStringAdapter(XMLString::transcode(localNameXMLString)));
            mLocalLogger.debug() << "Got local name: " << localName->get();

            if (std::string("GroupTarget").compare(0, XMLString::stringLen(localNameXMLString), localName->get()) == 0)
            {

                std::auto_ptr<middleware::lxacmlGroupTargetSchema::GroupTargetType> groupTarget
                    (mGroupTargetUnmarshaller->unMarshallUnSignedElement(element));

                UnicodeString groupTargetID(UnicodeStringConversion::toUnicodeString(groupTarget->GroupTargetID()));
                std::vector<UnicodeString>& authzTargets = groupTargets[groupTargetID];

                // Add all the <AuthzTarget> elements to the list.
                middleware::lxacmlGroupTargetSchema::GroupTargetType::AuthzTarget_iterator authzTargetIterator;
                for (authzTargetIterator = groupTarget->AuthzTarget().begin();
                    authzTargetIterator != groupTarget->AuthzTarget().end();
                    ++authzTargetIterator)
                {
                    UnicodeString authzTarget(UnicodeStringConversion::toUnicodeString(std::wstring(authzTargetIterator->c_str(), authzTargetIterator->length())));
                    authzTargets.push_back(authzTarget);
                }

                mLocalLogger.debug() << "Added " << boost::lexical_cast<std::string>(authzTargets.size()) << " authz targets for group target " << UnicodeStringConversion::toString(groupTargetID);
            }

        }
    }

    try
    {
        mSessionGroupCache->clearCache(groupTargets);

        mLocalLogger.info() << "Authorization cache clear succeeded. Flushed all cached authz decisions and created new cache with " << boost::lexical_cast<std::string>(groupTargets.size()) << " group targets.";

        statusCodeValue = saml2::statuscode::SUCCESS;
        statusMessage = L"The authorization cache was cleared successfully.";
    }
    catch (std::exception &ex)
    {
        mLocalLogger.error() << "An exception was thrown when trying to clear the authz cache. Exception was: " << ex.what();

        statusCodeValue = saml2::statuscode::RESPONDER;
        statusMessage = L"An exception occurred while trying to clear the authz cache.";
    }

    // Finished, one way or the other generate a response.
    return generateClearAuthzCacheResponse(request->ID(), statusMessage, statusCodeValue);
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::PolicyEnforcementProcessor::generateClearAuthzCacheResponse(const std::wstring &inResponseTo, const std::wstring &statusMessage, const std::wstring &statusCodeValue)
{
    // Build the XML object.
    saml2::assertion::NameIDType issuer(mMetadata->getSPEPIdentifier());
    std::wstring samlID(mIdentifierGenerator->generateSAMLID());
    
    saml2::protocol::StatusType status;
    saml2::protocol::StatusCodeType statusCode(statusCodeValue);
    status.StatusCode(statusCode);
    status.StatusMessage(statusMessage);

    middleware::ESOEProtocolSchema::ClearAuthzCacheResponseType response;
    response.ID(samlID);
    response.InResponseTo(inResponseTo);
    response.IssueInstant(xml_schema::date_time());
    response.Version(saml2::versions::SAML_20);

    response.Issuer(issuer);
    response.Status(status);

    // Marshal and set in data object.
    DOMDocument *responseDocument = mClearAuthzCacheResponseMarshaller->generateDOMDocument(&response);
    responseDocument = mClearAuthzCacheResponseMarshaller->validate(responseDocument);
    std::vector<std::string> idList{ UnicodeStringConversion::toString(samlID) };
    mClearAuthzCacheResponseMarshaller->sign(responseDocument, idList);

    mLocalLogger.debug() << "Generated authz cache clear response with status code: " << UnicodeStringConversion::toString(statusCodeValue) << " and status message: " << UnicodeStringConversion::toString(statusMessage);

    return responseDocument;
}

XERCES_CPP_NAMESPACE::DOMDocument* spep::PolicyEnforcementProcessor::generateAuthzDecisionQuery(spep::PolicyEnforcementProcessorData &data)
{
    mLocalLogger.debug() << "About to generate authz decision query.";

    // <Resource> tag to describe the resource being accessed
    middleware::lxacmlContextSchema::ResourceType resource;
    middleware::lxacmlContextSchema::AttributeType resourceAttribute;
    middleware::lxacmlContextSchema::AttributeValueType resourceAttributeValue;
    resourceAttributeValue.Value(UnicodeStringConversion::toWString(data.getResource()));
    resourceAttribute.AttributeValue(resourceAttributeValue);
    resource.Attribute(resourceAttribute);

    // <Subject> tag to describe the principal requesting the resource
    middleware::lxacmlContextSchema::SubjectType subject;
    middleware::lxacmlContextSchema::AttributeType subjectAttribute;
    middleware::lxacmlContextSchema::AttributeValueType subjectAttributeValue;
    subjectAttributeValue.Value(data.getESOESessionID());
    subjectAttribute.AttributeValue(subjectAttributeValue);
    subject.Attribute(subjectAttribute);

    // Put them into a <lxacml:Request>
    middleware::lxacmlContextSchema::RequestType request;
    request.Resource(resource);
    request.Subject(subject);

    saml2::assertion::NameIDType issuer(mMetadata->getSPEPIdentifier());
    std::wstring samlID = mIdentifierGenerator->generateSAMLID();
    
    // Create the SAML request.
    middleware::lxacmlSAMLProtocolSchema::LXACMLAuthzDecisionQueryType authzQuery;
    authzQuery.Request(request);
    authzQuery.ID(samlID);
    authzQuery.IssueInstant(xml_schema::date_time());
    authzQuery.Version(saml2::versions::SAML_20);
    authzQuery.Issuer(issuer);

    mLocalLogger.debug() << "Going to marshal authz decision query.";

    // Marshal and return.
    try
    {
        DOMDocument *requestDocument = mLxacmlAuthzDecisionQueryMarshaller->generateDOMDocument(&authzQuery);
        requestDocument = mLxacmlAuthzDecisionQueryMarshaller->validate(requestDocument);
        std::vector<std::string> idList{ UnicodeStringConversion::toString(samlID) };
        mLxacmlAuthzDecisionQueryMarshaller->sign(requestDocument, idList);

        return requestDocument;
    }
    catch (saml2::MarshallerException &ex)
    {
        mLocalLogger.error() << "Failed to marshal authz decision query. Message was: " << ex.getMessage() << ". Cause was: " << ex.getCause();
        return nullptr;
    }
}

void spep::PolicyEnforcementProcessor::processAuthzDecisionStatement(spep::PolicyEnforcementProcessorData &data, saml2::protocol::ResponseType *response)
{
    if (response == nullptr)
    {
        throw PEPException("LXACML response document was NULL. Unable to perform authorization.");
    }

    if (saml2::statuscode::AUTHN_FAILED.compare(response->Status().StatusCode().Value().c_str()) == 0)
    {
        mLocalLogger.error() << "PDP rejected session identifier with Authn Fail status. Terminating principal session.";
        data.setDecision(spep::Decision::DENY);
        mSessionCache->terminatePrincipalSession(data.getESOESessionID());
        return;
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
        throw PEPException("SAML response was rejected by SAML Validator.");
    }
    catch (std::exception &ex)
    {
        // Error occurred validating the response. Reject it anyway.
        mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << ex.what();
        throw PEPException("Error occurred in the SAML Validator.");
    }

    // Loop through <Assertion> elements.
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
            throw PEPException("SAML assertion was rejected by SAML Validator.");
        }
        catch (std::exception &ex)
        {
            // Error occurred validating the assertion. Reject it anyway.
            mLocalLogger.error() << "Error occurred in the SAML Validator. Message: " << ex.what();
            throw PEPException("Error occurred in the SAML Validator.");
        }

        // TODO Validate subject confirmation data
        saml2::assertion::AssertionType::Statement_iterator statementIterator;
        for (statementIterator = assertionIterator->Statement().begin();
            statementIterator != assertionIterator->Statement().end();
            ++statementIterator)
        {
            // Get the DOMElement from the object and retrieve its local name
            xercesc::DOMElement *domElement = (xercesc::DOMElement*)statementIterator->_node();

            // The attribute name for the "any" element type, and the expected value of that element.
            std::auto_ptr<XercesXMLChStringAdapter> attributeName(new XercesXMLChStringAdapter(XMLString::transcode("xsi:type")));
            std::auto_ptr<XercesXMLChStringAdapter> expectedXSITypeValue(new XercesXMLChStringAdapter(XMLString::transcode("lxacmla:LXACMLAuthzDecisionStatementType")));

            // Check if we have the right type of element
            const XMLCh *xsiTypeValue = domElement->getAttribute(attributeName->get());
            if (xsiTypeValue != NULL && XMLString::compareString(xsiTypeValue, expectedXSITypeValue->get()) == 0)
            {
                // Using not_root as a flag in unMarshallUnSignedElement means that the domElement is not "owned" by the XSD
                // object even though it is still kept.
                std::auto_ptr<middleware::lxacmlSAMLAssertionSchema::LXACMLAuthzDecisionStatementType> lxacmlAuthzDecisionStatement
                    (mLxacmlAuthzDecisionStatementUnmarshaller->unMarshallUnSignedElement(domElement, true));

                // If this XSD object was created, it must be schema valid. So, the Response and Result elements 
                // must be present. The same can be said for Decision
                middleware::lxacmlContextSchema::ResultType &result = lxacmlAuthzDecisionStatement->Response().Result();
                // operator _xsd_DecisionType() is defined, so we can just cast.
                middleware::lxacmlContextSchema::DecisionType::value decision =
                    (middleware::lxacmlContextSchema::DecisionType::value)result.Decision();

                // Check the decision from the document and set it in the data object.					
                if (decision == middleware::lxacmlContextSchema::DecisionType::Permit)
                {
                    mLocalLogger.info() << UnicodeStringConversion::toString(data.getResource()) << " <- ESOE Session[" << UnicodeStringConversion::toString(data.getESOESessionID()) << "] result from PDP is PERMIT";
                    data.setDecision(spep::Decision::PERMIT);
                }
                else if (decision == middleware::lxacmlContextSchema::DecisionType::Deny)
                {
                    mLocalLogger.info() << UnicodeStringConversion::toString(data.getResource()) << " <- ESOE Session[" << UnicodeStringConversion::toString(data.getESOESessionID()) << "] result from PDP is DENY";
                    data.setDecision(spep::Decision::DENY);
                }
                else
                {
                    mLocalLogger.error() << UnicodeStringConversion::toString(data.getResource()) << " <- ESOE Session[" << UnicodeStringConversion::toString(data.getESOESessionID()) << "] Erroneous result from PDP. Failing";
                    // No known decision. Error condition.
                    data.setDecision(spep::Decision::ERROR);
                }

                mLocalLogger.debug() << "Going to process obligations for authz decision statement.";
                processObligations(data, result.Obligations());

            }
        }
    }
}

void spep::PolicyEnforcementProcessor::processObligations(spep::PolicyEnforcementProcessorData &data, middleware::lxacmlSchema::ObligationsType &obligations)
{
    // Loop through the <Obligation> elements.
    middleware::lxacmlSchema::ObligationsType::Obligation_iterator obligationIterator;
    for (obligationIterator = obligations.Obligation().begin();
        obligationIterator != obligations.Obligation().end();
        ++obligationIterator)
    {
        mLocalLogger.debug() << "Got obligation with ObligationId=" << UnicodeStringConversion::toString(obligationIterator->ObligationId().c_str());

        // Check if the obligation ID is equal to the one we are to process.
        if (obligationIterator->ObligationId().compare(0, wcslen(OBLIGATION_ID), OBLIGATION_ID) == 0)
        {
            mLocalLogger.debug() << "ObligationId matched. Checking FulfillOn value.";

            // operator _xsd_EffectType() is defined, so we can just cast.
            middleware::lxacmlSchema::EffectType::value effect =
                (middleware::lxacmlSchema::EffectType::value)obligationIterator->FulfillOn();

            // Check that the decision matches the FulfillOn value.
            if ((data.getDecision() == spep::Decision::PERMIT && effect == middleware::lxacmlSchema::EffectType::Permit)
                || (data.getDecision() == spep::Decision::DENY && effect == middleware::lxacmlSchema::EffectType::Deny))
            {
                mLocalLogger.debug() << "FulfillOn value matched current decision. Processing attribute assignments.";

                // Process the attribute assignments.
                middleware::lxacmlSchema::ObligationType::AttributeAssignment_iterator attributeAssignmentIterator;
                for (attributeAssignmentIterator = obligationIterator->AttributeAssignment().begin();
                    attributeAssignmentIterator != obligationIterator->AttributeAssignment().end();
                    ++attributeAssignmentIterator)
                {
                    // Check the AttributeId and if it is not the same go to the next loop iteration.
                    std::wstring attributeID = attributeAssignmentIterator->AttributeId();
                    if (attributeID.compare(0, wcslen(ATTRIBUTE_ID), ATTRIBUTE_ID) != 0)
                        continue;

                    mLocalLogger.debug() << "Matched attribute ID for current attribute. Processing group targets.";

                    // For each child node of this <AttributeAssignment> element.
                    xercesc::DOMNode *node = attributeAssignmentIterator->_node();
                    xercesc::DOMNodeList *childNodes = node->getChildNodes();
                    for (XMLSize_t i = 0; i < childNodes->getLength(); ++i)
                    {
                        // Find only the <GroupTarget> elements.
                        xercesc::DOMElement *element = (xercesc::DOMElement*)childNodes->item(i);
                        const XMLCh* localNameXMLString = element->getLocalName();
                        std::auto_ptr<XercesCharStringAdapter> localName(new XercesCharStringAdapter(XMLString::transcode(localNameXMLString)));

                        if (std::string("GroupTarget").compare(0, XMLString::stringLen(localNameXMLString), localName->get()) == 0)
                        {
                            std::auto_ptr<middleware::lxacmlGroupTargetSchema::GroupTargetType> groupTarget
                                (mGroupTargetUnmarshaller->unMarshallUnSignedElement(element));

                            UnicodeString groupTargetID(UnicodeStringConversion::toUnicodeString(std::wstring(groupTarget->GroupTargetID())));
                            std::vector<UnicodeString> authzTargets;

                            mLocalLogger.debug() << "Current element is a group target. Group target ID: " << UnicodeStringConversion::toString(groupTargetID);

                            // Loop through the <AuthzTarget> elements.
                            middleware::lxacmlGroupTargetSchema::GroupTargetType::AuthzTarget_iterator authzTargetIterator;
                            for (authzTargetIterator = groupTarget->AuthzTarget().begin();
                                authzTargetIterator != groupTarget->AuthzTarget().end();
                                ++authzTargetIterator)
                            {
                                UnicodeString authzTarget(UnicodeStringConversion::toUnicodeString(std::wstring(authzTargetIterator->c_str())));
                                authzTargets.push_back(authzTarget);
                                mLocalLogger.debug() << "Adding to group target " << UnicodeStringConversion::toString(groupTargetID) << " authz target: " << UnicodeStringConversion::toString(authzTarget);
                            }

                            std::wstring esoeSessionID(data.getESOESessionID());
                            // TODO Check this for possible concurrency issues.
                            mSessionGroupCache->updateCache(esoeSessionID, groupTargetID, authzTargets, data.getDecision());
                        }

                    }

                }

            }

        }

    }
}
