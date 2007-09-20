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

#include "ws/SOAPUtil.h"
#include "Util.h"

#define XML_VERSION "1.0"

#include "axiom.h"
//#include "axutil_error_default.h"
#include "axiom_soap.h"

void spep::SOAPUtil::axiomInit()
{
	static bool inited = false;
	static Mutex mutex;
	
	if( !inited )
	{
		ScopedLock lock( mutex );
		if( !inited )
		{
			axiom_xml_reader_init();
			inited = true;
		}
	}
}

spep::SOAPDocument spep::SOAPUtil::wrapDocumentInSOAP( const saml2::SAMLDocument& samlDocument, std::string characterEncoding, spep::SOAPUtil::SOAPVersion soapVersion )
{
	class Axis2Cleanup
	{
		public:
		axiom_document_t *samlDocument;
		axiom_output_t *output;
		axutil_env_t *env;
		axiom_soap_body_t *soapBody;
		
		Axis2Cleanup()
		:
		samlDocument( NULL ),
		output( NULL ),
		env( NULL ),
		soapBody( NULL )
		{}
		
		~Axis2Cleanup()
		{
			if( soapBody != NULL )
			{
				axiom_soap_body_free( soapBody, env );
				soapBody = NULL;
			}
			
			if( samlDocument != NULL )
			{
				axiom_document_free( samlDocument, env );
				samlDocument = NULL;	
			}
			
			if( output != NULL )
			{
				axiom_output_free( output, env );
				output = NULL;
			}
			
			if( env != NULL )
			{
				axutil_env_free( env );
				env = NULL;
			}
		}
	} axis2Objects;
	
	axiomInit();
	
	axis2Objects.env = axutil_env_create_all( "", AXIS2_LOG_LEVEL_DEBUG );
	axutil_env_enable_log( axis2Objects.env, AXIS2_FALSE );
	
	int readerType = AXIS2_XML_PARSER_TYPE_BUFFER;

	// We pass NULL as character encoding here to let the xml parser detect it itself.
	axiom_xml_reader_t *xmlReader = axiom_xml_reader_create_for_memory( axis2Objects.env, const_cast<SAMLByte*>( samlDocument.getData() ), samlDocument.getLength(), NULL, readerType );
	axiom_stax_builder_t *staxBuilder = axiom_stax_builder_create( axis2Objects.env, xmlReader );
	
	axis2Objects.samlDocument = axiom_document_create( axis2Objects.env, NULL, staxBuilder );
	axiom_node_t *samlDocumentRoot = axiom_document_get_root_element( axis2Objects.samlDocument, axis2Objects.env );
	
	int axiomSOAPVersion = 0;
	switch( soapVersion )
	{
		case SOAP11:
		axiomSOAPVersion = AXIOM_SOAP11;
		break;
		
		case SOAP12:
		axiomSOAPVersion = AXIOM_SOAP12;
		break;
		
		default:
		axiomSOAPVersion = AXIOM_SOAP_VERSION_NOT_SET;
		break;
	}
	
	axiom_soap_envelope_t *soapEnvelope = axiom_soap_envelope_create_with_soap_version_prefix( axis2Objects.env, axiomSOAPVersion, NULL );
	
	axiom_soap_body_t *soapBody = axiom_soap_body_create_with_parent( axis2Objects.env, soapEnvelope );
	
	axiom_soap_body_add_child( soapBody, axis2Objects.env, samlDocumentRoot );
	
	AutoArray<axis2_char_t> encoding( characterEncoding.length() + 1 );
	std::memcpy( encoding.get(), characterEncoding.c_str(), characterEncoding.length() );
	encoding[characterEncoding.length()] = '\0';

	AutoArray<axis2_char_t> xmlVersion( strlen(XML_VERSION) + 1 );
	std::memcpy( xmlVersion.get(), XML_VERSION, strlen(XML_VERSION) );
	xmlVersion[strlen(XML_VERSION)] = '\0';
	
	int writerType = AXIS2_XML_PARSER_TYPE_BUFFER;
	int isPrefixDefault = AXIS2_TRUE;
	int compression = 0;
	
	axiom_xml_writer_t *xmlWriter = axiom_xml_writer_create_for_memory( axis2Objects.env, NULL, isPrefixDefault, compression, writerType );
	axis2Objects.output = axiom_output_create( axis2Objects.env, xmlWriter );
	
	xmlWriter->ops->write_start_document_with_version_encoding( xmlWriter, axis2Objects.env, xmlVersion.get(), encoding.get() );
	
	axis2_status_t serializeStatus = axiom_soap_envelope_serialize( soapEnvelope, axis2Objects.env, axis2Objects.output, false );
	if( serializeStatus == AXIS2_SUCCESS )
	{
		char* cDocument = (char*)axiom_xml_writer_get_xml( xmlWriter, axis2Objects.env );
		std::size_t length = axiom_xml_writer_get_xml_size( xmlWriter, axis2Objects.env );
		
		char* document = new char[length];
		std::memcpy( document, cDocument, length );
		
		return SOAPDocument( document, length );
	}
	
	// TODO Throw different exception here.
	throw std::exception();
	
}

saml2::SAMLDocument spep::SOAPUtil::unwrapDocumentFromSOAP( const spep::SOAPDocument& soapDocument, std::string characterEncoding, spep::SOAPUtil::SOAPVersion soapVersion )
{
	axiomInit();
	
	class Axis2Cleanup
	{
		public:
		axiom_document_t *samlDocument;
		axiom_output_t *output;
		axutil_env_t *env;
		
		Axis2Cleanup()
		:
		samlDocument( NULL ),
		output( NULL ),
		env( NULL )
		{}
		
		~Axis2Cleanup()
		{
			if( samlDocument != NULL )
			{
				axiom_document_free( samlDocument, env );
				samlDocument = NULL;	
			}
			
			if( output != NULL )
			{
				axiom_output_free( output, env );
				output = NULL;
			}
			
			if( env != NULL )
			{
				axutil_env_free( env );
				env = NULL;
			}
		}
	} axis2Objects;
	
	axis2Objects.env = axutil_env_create_all( "", AXIS2_LOG_LEVEL_DEBUG );
	axutil_env_enable_log( axis2Objects.env, AXIS2_FALSE );
	
	int readerType = AXIS2_XML_PARSER_TYPE_BUFFER;

	// We pass NULL as character encoding here to let the xml parser detect it itself.
	axiom_xml_reader_t *xmlReader = axiom_xml_reader_create_for_memory( axis2Objects.env, const_cast<char*>( soapDocument.getData() ), soapDocument.getLength(), NULL, readerType );
	
	axiom_stax_builder_t *staxBuilder = axiom_stax_builder_create( axis2Objects.env, xmlReader );
	
	const axis2_char_t *soapNamespace = NULL;
	switch( soapVersion )
	{
		case SOAP11:
		soapNamespace = AXIOM_SOAP11_SOAP_ENVELOPE_NAMESPACE_URI;
		break;
		
		case SOAP12:
		soapNamespace = AXIOM_SOAP12_SOAP_ENVELOPE_NAMESPACE_URI;
		break;
		
		default:
		// TODO Handle differently.
		throw std::exception();
	}
	
	axiom_soap_builder_t *soapBuilder = axiom_soap_builder_create( axis2Objects.env, staxBuilder, soapNamespace );
	
	
	axiom_soap_envelope_t *soapEnvelope = axiom_soap_builder_get_soap_envelope( soapBuilder, axis2Objects.env );
	
	axiom_soap_body_t *soapBody = axiom_soap_envelope_get_body( soapEnvelope, axis2Objects.env );
	
	axiom_node_t *baseNode = axiom_soap_body_get_base_node( soapBody, axis2Objects.env );
	axiom_node_t *samlDocumentNode = axiom_node_get_first_element( baseNode, axis2Objects.env );
	
	axis2Objects.samlDocument = axiom_document_create( axis2Objects.env, samlDocumentNode, NULL );
	
	int writerType = AXIS2_XML_PARSER_TYPE_BUFFER;
	int isPrefixDefault = AXIS2_TRUE;
	int compression = 0;
	
	AutoArray<axis2_char_t> encoding( characterEncoding.length() + 1 );
	std::memcpy( encoding.get(), characterEncoding.c_str(), characterEncoding.length() );
	encoding[characterEncoding.length()] = '\0';

	AutoArray<axis2_char_t> xmlVersion( strlen(XML_VERSION) + 1 );
	std::memcpy( xmlVersion.get(), XML_VERSION, strlen(XML_VERSION) );
	xmlVersion[strlen(XML_VERSION)] = '\0';
	
	axiom_xml_writer_t *xmlWriter = axiom_xml_writer_create_for_memory( axis2Objects.env, NULL, isPrefixDefault, compression, writerType );
	axis2Objects.output = axiom_output_create( axis2Objects.env, xmlWriter );
	//axiom_output_set_char_set_encoding( output, env, encoding->get() );
	//axiom_output_set_xml_version( output, env, xmlVersion->get());
	
	xmlWriter->ops->write_start_document_with_version_encoding( xmlWriter, axis2Objects.env, xmlVersion.get(), encoding.get() );
	
	axis2_status_t serializeStatus = axiom_document_serialize( axis2Objects.samlDocument, axis2Objects.env, axis2Objects.output );
	if( serializeStatus == AXIS2_SUCCESS )
	{
		SAMLByte* cDocument = (SAMLByte*)axiom_xml_writer_get_xml( xmlWriter, axis2Objects.env );
		long length = (long)axiom_xml_writer_get_xml_size( xmlWriter, axis2Objects.env );
		
		SAMLByte* document = new SAMLByte[length];
		std::memcpy( document, cDocument, length );
		
		return saml2::SAMLDocument( document, length );
	}
	
	// TODO Throw different exception here.
	throw std::exception();
}
