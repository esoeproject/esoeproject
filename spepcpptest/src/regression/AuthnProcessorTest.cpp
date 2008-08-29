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
 * Creation Date: 20/02/2007
 * 
 * Purpose: 
 */
#if 0
#include "regression/AuthnProcessorTest.h"
#include "authn/AuthnProcessor.h"
#include "authn/AuthnProcessorData.h"

#include "metadata/impl/MetadataImpl.h"

#include "sessions/impl/SessionCacheImpl.h"

#include "identifier/IdentifierGenerator.h"

#include "handlers/impl/UnmarshallerImpl.h"
#include "handlers/impl/MarshallerImpl.h"

#include "saml-schema-protocol-2.0.hxx"
#include "saml-schema-assertion-2.0.hxx"
#include "constants/VersionConstants.h"
#include "constants/StatusCodeConstants.h"

#include "Util.h"

#include "regression/Common.h"

#include <inttypes.h>

SUITE( AuthnProcessorTest )
{
	
	class AuthnProcessorTest_TestObjects
	{
		
		public:
		spep::ReportingProcessor reportingProcessor;
		saml2::IdentifierCache identifierCache;
		saml2::IdentifierGenerator identifierGenerator;
		saml2::SAMLValidator samlValidator;
		spep::SessionCacheImpl sessionCache;
		spep::KeyResolver keyResolver;
		KeyResolverInitializer keyResolverInit;
		int attributeConsumingServiceIndex;
		int assertionConsumerServiceIndex;
		std::string schemaPath;
		spep::MetadataImpl metadata;
		std::vector<std::string> authnSchemaList;
		spep::AttributeProcessor attributeProcessor;
		spep::AuthnProcessor authnProcessor;
		
		AuthnProcessorTest_TestObjects()
		:
		reportingProcessor(),
		identifierCache(),
		identifierGenerator( &identifierCache ),
		samlValidator( &identifierCache, 30 ),
		keyResolver(TESTDATA_PATH, "myrsakey"),
		keyResolverInit( keyResolver ),
		attributeConsumingServiceIndex(0),
		assertionConsumerServiceIndex(0),
		schemaPath(SCHEMA_PATH),
		metadata(&reportingProcessor, schemaPath, L"spep", L"esoe", "http://www.metadata.com", &keyResolver, 0, 0),
		authnSchemaList(),
		attributeProcessor( &metadata, &keyResolver, &identifierGenerator, schemaPath ),
		authnProcessor( &reportingProcessor, &attributeProcessor, &metadata, &sessionCache, &samlValidator, &identifierGenerator, &keyResolver, schemaPath, attributeConsumingServiceIndex, assertionConsumerServiceIndex )
		{
			authnSchemaList.push_back( spep::ConfigurationConstants::samlProtocol );
			authnSchemaList.push_back( spep::ConfigurationConstants::samlAssertion );
		}
		
	};
	
	void generateSuccessAuthnResponse( AuthnProcessorTest_TestObjects &testObjects, saml2::protocol::ResponseType &response, std::wstring &inResponseTo, std::wstring &subjectNameIDValue, std::wstring &esoeSessionIndex )
	{
		std::wstring samlID1( testObjects.identifierGenerator.generateSAMLID() );
		std::wstring samlID2( testObjects.identifierGenerator.generateSAMLID() );
		
		std::wstring esoeIdentifier( L"esoe.url" );
		saml2::assertion::NameIDType issuer( esoeIdentifier );
		
		saml2::assertion::NameIDType subjectNameID( subjectNameIDValue );
		saml2::assertion::SubjectType subject;
		subject.NameID( subjectNameID );
		
		saml2::assertion::AuthnContextType authnContext;
		authnContext.AuthnContextClassRef( L"ASDfasdfaksjgfhkj" );
		
		saml2::protocol::StatusType status;
		saml2::protocol::StatusCodeType statusCode( saml2::statuscode::SUCCESS );
		status.StatusCode( statusCode );
		
		saml2::assertion::AuthnStatementType authnStatement;
		authnStatement.AuthnContext( authnContext );
		authnStatement.AuthnInstant( xml_schema::date_time() );
		authnStatement.SessionIndex( esoeSessionIndex );
		authnStatement.SessionNotOnOrAfter( xml_schema::date_time() + boost::posix_time::hours(2) );
		
		saml2::assertion::AssertionType assertion;
		assertion.ID( samlID2 );
		assertion.IssueInstant( xml_schema::date_time() );
		assertion.Issuer( issuer );
		assertion.Subject( subject );
		assertion.Version( saml2::versions::SAML_20 );
		assertion.AuthnStatement().push_back( authnStatement );
		
		response.ID( samlID1 );
		response.Issuer( issuer );
		response.IssueInstant( xml_schema::date_time() );
		response.InResponseTo( inResponseTo );
		response.Version( saml2::versions::SAML_20 );
		response.Assertion().push_back( assertion );
		response.Status( status );
	}
	
	TEST( AuthnProcessorTest_testGenerateAuthnRequest )
	{
		AuthnProcessorTest_TestObjects testObjects;
		spep::AuthnProcessorData data;
		std::string requestURL( "http://LOL.com" );
		data.setRequestURL( requestURL );
		
		testObjects.authnProcessor.generateAuthnRequest( data );
		
		/*int16_t *p = (int16_t*)data.getRequestDocument();
		for (std::size_t i=0; i<(data.getRequestDocumentLength()/sizeof(int16_t)); ++i)
		{
			std::cout << (char)(p[i]&0xFF);
		}
		std::cout << std::endl;*/
		
// TODO Why doesn't this work on cygwin?
#ifndef __CYGWIN__
		saml2::UnmarshallerImpl<saml2::protocol::AuthnRequestType> unmarshaller( testObjects.schemaPath, testObjects.authnSchemaList );
		
		spep::UnauthenticatedSession unauthenticatedSession;
		try
		{
			// has a bad_cast error in the unmarshaller, but only on cygwin
			saml2::protocol::AuthnRequestType *authnRequest = unmarshaller.unMarshallSigned( data.getRequestDocument(), data.getRequestDocumentLength(), testObjects.keyResolver.getSPEPPublicKey(), false );
			testObjects.sessionCache.getUnauthenticatedSession( unauthenticatedSession, authnRequest->ID().c_str() );
		}
		catch ( saml2::UnmarshallerException &ex )
		{
			ex.printStackTrace();
			CHECK( false );
		}
		catch ( std::exception &ex )
		{
			CHECK( false );
		}
		
		CHECK( unauthenticatedSession.getRequestURL() == requestURL );
#endif /*__CYGWIN__*/
	}
	
	TEST( AuthnProcessorTest_testProcessAuthnResponse1 )
	{
		AuthnProcessorTest_TestObjects testObjects;
		
		std::string requestURL( "http://LOL.com" );
		std::wstring samlID( L"_iaojf09j10t9230r910239r01293r0912k30r9k12039rk123-9091u2309ri09ir09qi2039iagl" );
		std::wstring subjectNameIDValue( L"subject" );
		std::wstring esoeSessionIndex( L"01244" );
		spep::UnauthenticatedSession unauthenticatedSession;
		unauthenticatedSession.setRequestURL( requestURL );
		unauthenticatedSession.setAuthnRequestSAMLID( samlID );
		
		testObjects.sessionCache.insertUnauthenticatedSession( unauthenticatedSession );
		
		saml2::protocol::ResponseType response;
		generateSuccessAuthnResponse( testObjects, response, samlID, subjectNameIDValue, esoeSessionIndex );
		
		saml2::MarshallerImpl<saml2::protocol::ResponseType> marshaller( testObjects.schemaPath, testObjects.authnSchemaList, "Response", "urn:oasis:names:tc:SAML:2.0:protocol" );
		std::auto_ptr<saml2::MarshallerOutput> output( marshaller.marshallUnSigned( &response ) );
		
		spep::AuthnProcessorData data;
		data.setResponseDocument( output->data, output->length );
		testObjects.authnProcessor.processAuthnResponse( data );
		
		std::string sessionID = data.getSessionID();
		spep::PrincipalSession clientSession;
		try
		{
			testObjects.sessionCache.getPrincipalSession( clientSession, sessionID );
		}
		catch (std::exception &ex)
		{
			CHECK( false );
		}
		
		CHECK( clientSession.getESOESessionID() == subjectNameIDValue );
		
		try
		{
			clientSession = testObjects.authnProcessor.verifySession( sessionID );
		}
		catch (std::exception &ex)
		{
			CHECK( false );
		}
		
		CHECK( clientSession.getESOESessionID() == subjectNameIDValue );
		CHECK( clientSession.getESOESessionIndexMap()[ esoeSessionIndex ] == sessionID );
	}

}
#endif
