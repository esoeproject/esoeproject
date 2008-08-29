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
 * Creation Date: 12/03/2007
 * 
 * Purpose: 
 */

#include "regression/AttributeProcessorTest.h"

#include "reporting/ReportingProcessor.h"
#include "identifier/IdentifierCache.h"
#include "identifier/IdentifierGenerator.h"
#include "validator/SAMLValidator.h"
#include "metadata/KeyResolver.h"
#include "metadata/impl/MetadataImpl.h"
#include "attribute/AttributeProcessor.h"
#include "attribute/AttributeProcessorData.h"
#include "UnicodeStringConversion.h"

#include "regression/Common.h"

#include "handlers/impl/UnmarshallerImpl.h"
#include "handlers/impl/MarshallerImpl.h"
#include "constants/StatusCodeConstants.h"
#include "constants/VersionConstants.h"
#include "SAML2Defs.h"

#include <vector>
#include <string>

#include <xercesc/internal/MemoryManagerImpl.hpp>

SUITE( AttributeProcessorTest )
{
	
	saml2::SAMLDocument generateAttributeResponse( std::wstring esoeSessionID, std::wstring samlID, std::string schemaPath, spep::KeyResolver &keyResolver );
	
	class TestWSClient : public spep::WSClient
	{
		private:
		saml2::Unmarshaller<saml2::protocol::AttributeQueryType> *_unmarshaller;
		std::string _schemaPath;
		spep::KeyResolver *_keyResolver;
		
		public:
		TestWSClient( spep::KeyResolver *keyResolver, std::string schemaPath )
		:
		WSClient( NULL ),
		_unmarshaller( NULL ),
		_schemaPath( schemaPath ),
		_keyResolver( keyResolver )
		{
			std::vector<std::string> schemaList;
			schemaList.push_back( spep::ConfigurationConstants::samlAssertion );
			schemaList.push_back( spep::ConfigurationConstants::samlProtocol );
			
			_unmarshaller = new saml2::UnmarshallerImpl<saml2::protocol::AttributeQueryType>( schemaPath, schemaList );
		}
		
		virtual ~TestWSClient()
		{
			delete _unmarshaller;
		}
		
		virtual void attributeAuthority( spep::WSProcessorData& data, std::string endpoint )
		{
			std::auto_ptr<saml2::protocol::AttributeQueryType> attributeQuery( _unmarshaller->unMarshallUnSigned( data.getSAMLRequestDocument() )  );
			data.setSAMLResponseDocument( generateAttributeResponse( attributeQuery->Subject().NameID()->c_str(), attributeQuery->ID().c_str(), _schemaPath, *_keyResolver ) );
		}
		
		virtual void policyDecisionPoint( spep::WSProcessorData& data, std::string endpoint )
		{
			throw std::exception();
		}
		
		virtual void spepStartup( spep::WSProcessorData& data, std::string endpoint )
		{
			throw std::exception();
		}
	};
	
	class AttributeProcessorTest_TestObjects
	{
		public:
		spep::ReportingProcessor reportingProcessor;
		saml2::IdentifierCache identifierCache;
		saml2::IdentifierGenerator identifierGenerator;
		saml2::SAMLValidator samlValidator;
		spep::KeyResolver keyResolver;
		KeyResolverInitializer keyResolverInit;
		std::string schemaPath;
		spep::MetadataImpl metadata;
		std::vector<std::string> attributeSchemaList;
		TestWSClient wsClient;
		spep::AttributeProcessor attributeProcessor;
		
		AttributeProcessorTest_TestObjects()
		:
		reportingProcessor(),
		identifierCache(),
		identifierGenerator( &identifierCache ),
		samlValidator( &identifierCache, 30 ),
		keyResolver(TESTDATA_PATH, "myrsakey"),
		keyResolverInit( keyResolver ),
		schemaPath(SCHEMA_PATH),
		metadata(&reportingProcessor, schemaPath, L"spep", L"esoe", "http://www.metadata.com", &keyResolver, 0, 1),
		attributeSchemaList(),
		wsClient(&keyResolver, schemaPath),
		attributeProcessor( &reportingProcessor, &metadata, &keyResolver, &identifierGenerator, &wsClient, schemaPath )
		{
			attributeSchemaList.push_back( spep::ConfigurationConstants::samlProtocol );
			attributeSchemaList.push_back( spep::ConfigurationConstants::samlAssertion );
		}
	};
	
	TEST( AttributeProcessorTest_testBuildAttributeQuery )
	{
		AttributeProcessorTest_TestObjects testObjects;
		spep::AttributeProcessorData data;
		
		const wchar_t* samlID = L"_foijweiruq98u59817203945-81203957908q7wifjalksdf";
		spep::PrincipalSession principalSession;
		std::wstring esoeSessionID( L"_12345678" );
		principalSession.setESOESessionID( esoeSessionID );
		
		testObjects.attributeProcessor.buildAttributeQuery( data, principalSession, samlID );
		
		saml2::UnmarshallerImpl<saml2::protocol::AttributeQueryType> attributeQueryUnmarshaller( testObjects.schemaPath, testObjects.attributeSchemaList );
		
		CHECK( data.getRequestDocument().getData() != NULL );
		
		saml2::protocol::AttributeQueryType *attributeQuery =
			attributeQueryUnmarshaller.unMarshallSigned( data.getRequestDocument(), testObjects.keyResolver.getSPEPPublicKey() );
		
		// Make sure the ID was set correctly.
		CHECK( wcscmp( attributeQuery->ID().c_str(), samlID ) == 0 );
		
		CHECK( esoeSessionID.compare( attributeQuery->Subject().NameID()->c_str() ) == 0 );
		
		// TODO More checks to make sure subject etc was set correctly.
	}
	
	saml2::SAMLDocument generateAttributeResponse( std::wstring esoeSessionID, std::wstring samlID, std::string schemaPath, spep::KeyResolver &keyResolver )
	{
		std::vector<std::string> schemaList;
		schemaList.push_back( spep::ConfigurationConstants::samlProtocol );
		schemaList.push_back( spep::ConfigurationConstants::samlAssertion );
		
		std::vector<std::string> idList;
		
		saml2::UnmarshallerImpl<saml2::protocol::ResponseType> responseUnmarshaller
			( schemaPath, schemaList );
		saml2::MarshallerImpl<saml2::protocol::ResponseType> responseMarshaller
			( schemaPath, schemaList, "Response", "urn:oasis:names:tc:SAML:2.0:protocol", keyResolver.getSPEPKeyName(), keyResolver.getSPEPPrivateKey() );
		
		SAMLByte* doc = NULL;
		long arrayLen = 0;
		long charLen = 0;
		
		std::ifstream input( TESTDATA_PATH "/attributeResponse.xml");
		input.seekg( 0, std::ios::end );
		charLen = input.tellg();
		arrayLen = charLen * sizeof(char) / sizeof(SAMLByte);
		doc = new SAMLByte[arrayLen + 1];
		
		input.seekg( 0, std::ios::beg );
		input.read( (char*)doc, charLen );
		input.close();
		doc[arrayLen] = (SAMLByte)0;
		
		// doc will be delete[]'ed after this line
		saml2::protocol::ResponseType *response = responseUnmarshaller.unMarshallUnSigned( saml2::SAMLDocument( doc, arrayLen ), true );
		
		
		std::wstring esoeIdentifier( L"esoe" );
		std::wstring responseID( L"_fiajsoiejroiqjweprijp-85091230948019238409" );
		std::wstring assertionID( L"_09tui0qi0w9eti09i2019itt-0i3t09009itpoasjdglkjlasdkmn" );
		
		saml2::protocol::StatusType status;
		saml2::protocol::StatusCodeType statusCode( saml2::statuscode::SUCCESS );
		status.StatusCode( statusCode );
	
		saml2::assertion::NameIDType responseIssuer( esoeIdentifier );
		response->Issuer( responseIssuer );
		response->Status( status );
		response->InResponseTo( samlID );
		//response->ID( responseID );
		response->Version( saml2::versions::SAML_20 );
		response->IssueInstant( xml_schema::date_time() );
		
		saml2::assertion::SubjectType subject;
		saml2::assertion::NameIDType subjectNameID( esoeSessionID );
		subject.NameID( subjectNameID );
		
		saml2::assertion::AssertionType *assertion = &(*response->Assertion().begin());
	
		saml2::assertion::NameIDType assertionIssuer( esoeIdentifier );
		assertion->Issuer( assertionIssuer );
		assertion->Subject( subject );
		assertion->Version( saml2::versions::SAML_20 );
		//assertion->ID( assertionID );
		assertion->IssueInstant( xml_schema::date_time() );
		
		idList.push_back( spep::UnicodeStringConversion::toString(responseID) );
		
		return responseMarshaller.marshallSigned( response, idList );
	}
	
	TEST( AttributeProcessorTest_testProcessAttributeResponse )
	{
		AttributeProcessorTest_TestObjects testObjects;
		spep::AttributeProcessorData data;
		
		const wchar_t* samlID = L"_81203957908q7wifjalksdf-foijweiruq98u59817203945";
		spep::PrincipalSession principalSession;
		
		data.setResponseDocument( generateAttributeResponse( principalSession.getESOESessionID(), samlID, testObjects.schemaPath, testObjects.keyResolver ) );
		
		// TODO this call broken due to <AttributeValue/> elements generated above not having any content. Why?
		//testObjects.attributeProcessor.processAttributeResponse( data, principalSession, samlID );
		
		//UnicodeString testAttribute( UNICODE_STRING_SIMPLE( "uid" ) );
		//UnicodeString testAttributeValue( UNICODE_STRING_SIMPLE( "beddoes" ) );
		
		//spep::PrincipalSession::AttributeMapType attributeMap = principalSession.getAttributeMap();
		
		//spep::PrincipalSession::AttributeMapType::mapped_type attributeValueVector = attributeMap[testAttribute];
		//CHECK( attributeValueVector.size() == 0 );
		
		//CHECK( attributeValueVector[0].compare( testAttributeValue ) == 0 );
	}

}
