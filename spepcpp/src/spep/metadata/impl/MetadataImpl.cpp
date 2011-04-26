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

#include "spep/metadata/impl/MetadataImpl.h"
#include "spep/metadata/MetadataThread.h"
#include "spep/exceptions/InvalidStateException.h"
#include "spep/exceptions/MetadataException.h"

#include "spep/Util.h"
#include "spep/UnicodeStringConversion.h"

#include "unicode/utypes.h"
#include "unicode/unistr.h"

#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMNode.hpp>
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMTypeInfo.hpp>
#include <xercesc/util/XMLString.hpp>

#include <xsec/dsig/DSIGKeyInfo.hpp>
#include <xsec/dsig/DSIGKeyInfoName.hpp>

#include <boost/thread.hpp>
#include <boost/lexical_cast.hpp>

#include "saml2/handlers/impl/UnmarshallerImpl.h"
#include "saml2/handlers/impl/MarshallerImpl.h"

#define SPEPSTARTUPSERVICE_ELEMENTNAME "SPEPStartupService"
#define XSI_TYPE_ATTRIBUTENAME "xsi:type"
#define LXACMLPDPDESCRIPTOR_TYPENAME "lxacml-md:LXACMLPDPDescriptorType"

// Define a "short sleep" to be 20 milliseconds
#define SHORT_SLEEP_LENGTH_NANOSECONDS ( 20 * 1000 * 1000 ) 
#define ONE_SECOND_LENGTH_NANOSECONDS ( 1000 * 1000 * 1000 )

spep::MetadataImpl::MetadataImpl( saml2::Logger *logger, std::string schemaPath, std::wstring spepIdentifier, std::wstring esoeIdentifier, std::string metadataURL, std::string caBundle, spep::KeyResolver *keyResolver, int assertionConsumerIndex, int interval )
:
_metadataMutex(),
_logger(logger),
_localLogger(logger, "spep::MetadataImpl"),
_spepIdentifier(spepIdentifier), 
_esoeIdentifier(esoeIdentifier),
_metadataURL(metadataURL),
_schemaPath(schemaPath),
_keyResolver(keyResolver),
_assertionConsumerIndex(assertionConsumerIndex),
_interval(interval),
_balancingIndex(0),
_indexMutex(),
_spepStartupServiceUnmarshaller( NULL ),
_currentRevision(),
_error( false ),
_hasData( false ),
_cache(NULL),
_caBundle( caBundle ),
_thread(NULL),
_threadGroup()
{
	std::vector<std::string> spepStartupSchemas;
	spepStartupSchemas.push_back( ConfigurationConstants::spepStartupService );
	_spepStartupServiceUnmarshaller = new saml2::UnmarshallerImpl<middleware::spepStartupServiceSchema::SPEPStartupServiceType>
		( logger, schemaPath, spepStartupSchemas );
	
	init();
}

spep::MetadataImpl::~MetadataImpl()
{
	this->_thread->_die = true;
	
	// Now we wait for the thread to die before we delete it.
	this->_threadGroup.join_all();
	delete this->_thread;
}

void spep::MetadataImpl::init()
{
	_localLogger.debug() << "Spawning metadata thread.";
	this->_thread = new MetadataThread( this->_logger, this, this->_caBundle, this->_schemaPath, this->_interval, this->_keyResolver );
	this->_threadGroup.create_thread( this->_thread->getThreadHandler() );
}

void spep::MetadataImpl::rebuildCache( saml2::metadata::EntitiesDescriptorType &entitiesDescriptor, std::string hashValue, spep::MetadataImpl::KeyMap &keyMap )
{
	_localLogger.debug() << "About to rebuild metadata cache from document with hash value: " << hashValue;
	
	MetadataCache *newCache = new MetadataCache();
	int errors = 0;
	
	buildCacheRecurse( entitiesDescriptor, *newCache );
	if (newCache->singleSignOnEndpoints.size() == 0)
	{
		_localLogger.info() << "No single sign-on endpoints for ESOE were found in metadata";
		++errors;
	}
	if (newCache->attributeServiceEndpoints.size() == 0)
	{
		_localLogger.info() << "No attribute service endpoints for ESOE were found in metadata";
		++errors;
	}
	if (newCache->authzServiceEndpoints.size() == 0)
	{
		_localLogger.info() << "No authorization service endpoints for ESOE were found in metadata";
		++errors;
	}
	if (newCache->spepStartupServiceEndpoints.size() == 0)
	{
		_localLogger.info() << "No startup service endpoints for ESOE were found in metadata";
		++errors;
	}
	
	if (errors > 0)
	{
		_localLogger.error() << "An error occurred while processing the metadata document update. Aborting.";
		throw MetadataException( "Errors prevented the metadata cache update from occurring." );
	}
	
	newCache->keyMap = keyMap;
	
	{
		ScopedLock lock( this->_metadataMutex );
		// Make an auto_ptr with the metadata cache in it, so that it is deleted properly when we leave this scope.
		// Don't need to care about nulls here.. std::auto_ptr won't try to delete NULL.
		std::auto_ptr<MetadataCache> autoPtrMetadataCache( this->_cache );
		
		this->_currentRevision = hashValue;
		this->_cache = newCache;
		
		this->_hasData = true;
		_localLogger.info() << "New metadata document was processed successfully and is now active.";
	}
}

void spep::MetadataImpl::buildCacheRecurse( saml2::metadata::EntitiesDescriptorType &entitiesDescriptor, spep::MetadataImpl::MetadataCache &cache )
{
	saml2::metadata::EntitiesDescriptorType::EntitiesDescriptor_iterator entitiesDescriptorIterator;
	// Recurse into <EntitiesDescriptor> sub-elements of the root element.
	for( entitiesDescriptorIterator = entitiesDescriptor.EntitiesDescriptor().begin(); entitiesDescriptorIterator != entitiesDescriptor.EntitiesDescriptor().end(); ++entitiesDescriptorIterator )
	{
		this->buildCacheRecurse( *entitiesDescriptorIterator, cache );
	}
	
	// Iterate through all the <EntityDescriptor> elements
	saml2::metadata::EntitiesDescriptorType::EntityDescriptor_iterator entityDescriptorIterator;
	for( entityDescriptorIterator = entitiesDescriptor.EntityDescriptor().begin(); 
		entityDescriptorIterator != entitiesDescriptor.EntityDescriptor().end(); 
		++entityDescriptorIterator )
	{
		
		// Check this entity descriptor to see if it's the one we are after.
		// Need to compare to c_str() here because the object is a xsd::cxx::xml::string
		if ( this->_esoeIdentifier.compare( entityDescriptorIterator->entityID().c_str() ) == 0 )
		{
			
			_localLogger.debug() << "Found ESOE entity descriptor (" << UnicodeStringConversion::toString(this->_esoeIdentifier) << ")";
			// Loop through the <IDPSSODescriptor> elements and add all single sign-on
			// and single logout endpoints.
			saml2::metadata::EntityDescriptorType::IDPSSODescriptor_iterator idpSSODescriptorIterator;
			for( idpSSODescriptorIterator = entityDescriptorIterator->IDPSSODescriptor().begin(); 
				idpSSODescriptorIterator != entityDescriptorIterator->IDPSSODescriptor().end(); 
				++idpSSODescriptorIterator )
			{
				
				// Loop through <SingleSignOnService> elements.
				saml2::metadata::IDPSSODescriptorType::SingleSignOnService_iterator singleSignOnServiceIterator;
				for( singleSignOnServiceIterator = idpSSODescriptorIterator->SingleSignOnService().begin();
					singleSignOnServiceIterator != idpSSODescriptorIterator->SingleSignOnService().end();
					++singleSignOnServiceIterator )
				{
					
					// Add the endpoint to the list.
					cache.singleSignOnEndpoints.push_back( UnicodeStringConversion::toString( singleSignOnServiceIterator->Location() ) );
					
				}
				
				// Loop through <SingleLogoutService> elements
				saml2::metadata::IDPSSODescriptorType::SingleLogoutService_iterator singleLogoutServiceIterator;
				for( singleLogoutServiceIterator = idpSSODescriptorIterator->SingleLogoutService().begin();
					singleLogoutServiceIterator != idpSSODescriptorIterator->SingleLogoutService().end();
					++singleLogoutServiceIterator )
				{
					
					// Add the endpoint to the list
					cache.singleLogoutEndpoints.push_back( UnicodeStringConversion::toString( singleLogoutServiceIterator->Location() ) );
					
				}
				
				// Check in <Extensions> for SPEP startup service elements
				if ( idpSSODescriptorIterator->Extensions().present() )
				{
					
					xercesc::DOMNode *node = idpSSODescriptorIterator->Extensions()->_node();
					xercesc::DOMNodeList *childNodes = node->getChildNodes();
					for( XMLSize_t i = 0; i < childNodes->getLength(); ++i )
					{
						
						if( node->getNodeType() != DOMNode::ELEMENT_NODE ) continue;
						
						xercesc::DOMElement *element = (xercesc::DOMElement*)childNodes->item(i);
						const XMLCh* localNameXMLCh = element->getLocalName();
						std::auto_ptr<XercesCharStringAdapter> localName( new XercesCharStringAdapter( XMLString::transcode( localNameXMLCh ) ) );
						 
						if ( std::string(SPEPSTARTUPSERVICE_ELEMENTNAME).compare( 0, XMLString::stringLen( localNameXMLCh ), localName->get() ) == 0 )
						{
							
							/* Get SAML2lib-cpp to generate XSD representation of SPEPStartupServiceType from ##anyType node */
							std::auto_ptr<middleware::spepStartupServiceSchema::SPEPStartupServiceType> spepStartupService ( this->_spepStartupServiceUnmarshaller->unMarshallUnSignedElement( element ) );
							cache.spepStartupServiceEndpoints.push_back( UnicodeStringConversion::toString( spepStartupService->Location() ) );
							
						}
						
					}
					
				}
				
			}
			
			// Loop through all <AttributeAuthorityDescriptor> elements and add all attribute
			// service endpoints to the cache.
			saml2::metadata::EntityDescriptorType::AttributeAuthorityDescriptor_iterator attributeAuthorityDescriptorIterator;
			for( attributeAuthorityDescriptorIterator = entityDescriptorIterator->AttributeAuthorityDescriptor().begin(); 
				attributeAuthorityDescriptorIterator != entityDescriptorIterator->AttributeAuthorityDescriptor().end(); 
				++attributeAuthorityDescriptorIterator )
			{
				
				// Loop through the <AttributeService> elements 
				saml2::metadata::AttributeAuthorityDescriptorType::AttributeService_iterator attributeServiceIterator;
				for( attributeServiceIterator = attributeAuthorityDescriptorIterator->AttributeService().begin();
					attributeServiceIterator != attributeAuthorityDescriptorIterator->AttributeService().end();
					++attributeServiceIterator )
				{
					
					// Add the endpoint to the list.
					cache.attributeServiceEndpoints.push_back( UnicodeStringConversion::toString( attributeServiceIterator->Location() ) );
					
				}
				
			}
			

			// Loop through all the <RoleDescriptor> elements.
			// This will loop through all extensions to the RoleDescriptor element which are not
			// defined directly in the SAML metadata schema.
			// In our case, we need <LXACMLPDPDescriptor> elements here.
			saml2::metadata::EntityDescriptorType::RoleDescriptor_iterator roleDescriptorIterator;
			for( roleDescriptorIterator = entityDescriptorIterator->RoleDescriptor().begin(); 
				roleDescriptorIterator != entityDescriptorIterator->RoleDescriptor().end(); 
				++roleDescriptorIterator )
			{
				
				// Get the DOMElement.. if this isn't an element node something really bizarre is going on..
				DOMNode *domNode = roleDescriptorIterator->_node();
				if ( domNode->getNodeType() != DOMNode::ELEMENT_NODE )
				{ continue; }
				DOMElement *domElement = (DOMElement*)domNode;
				
				std::auto_ptr<XercesXMLChStringAdapter> attributeName( new XercesXMLChStringAdapter( XMLString::transcode(XSI_TYPE_ATTRIBUTENAME) ) );
				std::auto_ptr<XercesXMLChStringAdapter> expectedXSITypeValue( new XercesXMLChStringAdapter( XMLString::transcode(LXACMLPDPDESCRIPTOR_TYPENAME) ) );
				
				// Check if we have the right type of element
				const XMLCh *xsiTypeValue = domElement->getAttribute( attributeName->get() );
				if( xsiTypeValue != NULL && XMLString::compareString( xsiTypeValue, expectedXSITypeValue->get() ) == 0 )
				{
					
					// Cast to LXACMLPDPDescriptor to get the location of the AuthzService
					middleware::lxacmlPDPSchema::LXACMLPDPDescriptorType *lxacmlPDPDescriptor = (middleware::lxacmlPDPSchema::LXACMLPDPDescriptorType*)(&(*roleDescriptorIterator));
					
					// Add all the authz service locations to the cache.
					middleware::lxacmlPDPSchema::LXACMLPDPDescriptorType::AuthzService_iterator authzServiceIterator;
					for( authzServiceIterator = lxacmlPDPDescriptor->AuthzService().begin();
						authzServiceIterator != lxacmlPDPDescriptor->AuthzService().end();
						++authzServiceIterator )
					{
						
						cache.authzServiceEndpoints.push_back( UnicodeStringConversion::toString( authzServiceIterator->Location() ) );
						
					}
					
				}
				
			}
			
			_localLogger.info() << boost::lexical_cast<std::string>(cache.singleSignOnEndpoints.size()) << " single sign-on endpoint(s) found.";
			_localLogger.info() << boost::lexical_cast<std::string>(cache.singleLogoutEndpoints.size()) << " single logout endpoint(s) found.";
			_localLogger.info() << boost::lexical_cast<std::string>(cache.spepStartupServiceEndpoints.size()) << " spep startup endpoint(s) found.";
			_localLogger.info() << boost::lexical_cast<std::string>(cache.attributeServiceEndpoints.size()) << " attribute service endpoint(s) found.";
			_localLogger.info() << boost::lexical_cast<std::string>(cache.authzServiceEndpoints.size()) << " authorization service endpoint(s) found.";
			
		}
	}
}

// This will mask the index to be less than 2^20
#define INDEX_MASK 0xFFFFF
int spep::MetadataImpl::nextBalancingIndex( int modulus ) const
{
	// Pretty crude "random" number generation.
	// Doesn't need to be a secure random, just enough for load balancing will suffice.
	ScopedLock indexLock( _indexMutex );
	
	++_balancingIndex;
	_balancingIndex &= INDEX_MASK;
	
	// Return a number less than 'modulus'
	return _balancingIndex % modulus;
}

const std::wstring spep::MetadataImpl::getSPEPIdentifier() const
{
	return this->_spepIdentifier;
}

const std::wstring spep::MetadataImpl::getESOEIdentifier() const
{
	return this->_esoeIdentifier;
}

void spep::MetadataImpl::waitForData() const
{
	while(!this->_hasData && !this->_error)
	{
		boost::xtime shortSleep;
		boost::xtime_get( &shortSleep, boost::TIME_UTC );
		
		shortSleep.nsec += SHORT_SLEEP_LENGTH_NANOSECONDS;
		// xtime.nsec > 10^9 is undefined behaviour, so if we have gone too far
		// increase seconds and decrease nanoseconds accordingly
		while( shortSleep.nsec >= ONE_SECOND_LENGTH_NANOSECONDS )
		{
			shortSleep.nsec -= ONE_SECOND_LENGTH_NANOSECONDS;
			shortSleep.sec++;
		}
		
		boost::thread::sleep( shortSleep );	
	}
	
	if( this->_error )
	{
		throw InvalidStateException( "The metadata was not successfully loaded, and is preventing normal operation." );
	}
}

const std::string spep::MetadataImpl::getSingleSignOnEndpoint() const
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );

	const std::vector<std::string>& data = this->_cache->singleSignOnEndpoints;
	
	return data.at( nextBalancingIndex( data.size() ) );
}

const std::string spep::MetadataImpl::getSingleLogoutEndpoint() const
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );

	const std::vector<std::string>& data = this->_cache->singleLogoutEndpoints;
	
	return data.at( nextBalancingIndex( data.size() ) );
}

const std::string spep::MetadataImpl::getAttributeServiceEndpoint() const
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );

	const std::vector<std::string>& data = this->_cache->attributeServiceEndpoints;
	
	return data.at( nextBalancingIndex( data.size() ) );
}

const std::string spep::MetadataImpl::getAuthzServiceEndpoint() const
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );

	const std::vector<std::string>& data = this->_cache->authzServiceEndpoints;
	
	return data.at( nextBalancingIndex( data.size() ) );
}

const std::string spep::MetadataImpl::getSPEPStartupServiceEndpoint() const
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );
	
	const std::vector<std::string>& data = this->_cache->spepStartupServiceEndpoints;
	
	return data.at( nextBalancingIndex( data.size() ) );
}

XSECCryptoKey *spep::MetadataImpl::resolveKey (DSIGKeyInfoList *list)
{
	if( list->isEmpty() )
		return NULL;
	
	_localLogger.debug() << "About to resolve XSECCryptoKey from DSIGKeyInfoList.";
	
	// Loop through the key info list and look for a name.
	for( DSIGKeyInfoList::size_type i = 0; i < list->getSize(); ++i )
	{
		DSIGKeyInfo *keyInfo = list->item(i);
		// If this DSIGKeyInfo isn't a key name, skip it.
		if( keyInfo->getKeyInfoType() != DSIGKeyInfo::KEYINFO_NAME ) continue;
		
		// Reinterpret the pointer as a DSIGKeyInfoName
		DSIGKeyInfoName* keyInfoName = reinterpret_cast<DSIGKeyInfoName*>( keyInfo );
		std::auto_ptr<XercesCharStringAdapter> keyNameChars( new XercesCharStringAdapter( XMLString::transcode( keyInfoName->getKeyName() ) ) );
		
		// Grab the keyname as a std::string
		std::string keyName( keyNameChars->get() );
		_localLogger.debug() << "Found keyname " << keyName << ", attempting to find key data";
		
		try
		{
			// Resolve the key locally
			saml2::KeyData keyData( this->resolveKey( keyName ) );
			_localLogger.debug() << "Got key data. Returning";
			
			// Create a XSECCryptoKey
			return keyData.createXSECCryptoKey();
		}
		catch( std::exception& e )
		{
		}
	}
	
	_localLogger.error() << "No key data found. Returning NULL";
	// No key data found/returned. Return null now.
	return NULL;
}

saml2::KeyData spep::MetadataImpl::resolveKey (std::string keyName)
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );

	KeyMap::iterator iter = _cache->keyMap.find(keyName);
	if (iter != _cache->keyMap.end())
		return iter->second;
	
	throw MetadataException( "Key could not be resolved - no key by that name exists" );
}

XSECKeyInfoResolver* spep::MetadataImpl::clone() const
{
	this->waitForData();
	ScopedLock lock( this->_metadataMutex );

	_localLogger.debug() << "Cloning metadata key resolver.";

	return new MetadataKeyResolver( this->_logger, this->_cache->keyMap );
}

spep::MetadataImpl::MetadataKeyResolver::MetadataKeyResolver( saml2::Logger *logger, const spep::MetadataImpl::KeyMap& map )
:
_logger( logger ),
_localLogger( logger, "spep::MetadataImpl::MetadataKeyResolver" ),
_map( map )
{
}

spep::MetadataImpl::MetadataKeyResolver::~MetadataKeyResolver()
{
}

XSECCryptoKey *spep::MetadataImpl::MetadataKeyResolver::resolveKey (DSIGKeyInfoList *list)
{
	if( list->isEmpty() )
		return NULL;
	
	_localLogger.debug() << "About to resolve XSECCryptoKey from DSIGKeyInfoList.";
	
	// Loop through the key info list and look for a name.
	for( DSIGKeyInfoList::size_type i = 0; i < list->getSize(); ++i )
	{
		DSIGKeyInfo *keyInfo = list->item(i);
		if( keyInfo->getKeyInfoType() != DSIGKeyInfo::KEYINFO_NAME ) continue;
		
		// This keyInfo is a key name, so cast it and grab the name as a Xerces char*
		DSIGKeyInfoName* keyInfoName = reinterpret_cast<DSIGKeyInfoName*>( keyInfo );
		std::auto_ptr<XercesCharStringAdapter> keyNameChars( new XercesCharStringAdapter( XMLString::transcode( keyInfoName->getKeyName() ) ) );
		
		std::string keyName( keyNameChars->get() );
		_localLogger.debug() << "Found keyname " << keyName << ", attempting to find key data";
		
		try
		{
			saml2::KeyData keyData( this->resolveKey( keyName ) );
			_localLogger.debug() << "Got key data. Returning";
			
			return keyData.createXSECCryptoKey();
		}
		catch( std::exception& e )
		{
		}
	}
	
	_localLogger.error() << "No key data found. Returning NULL";
	// No key data found/returned. Return null now.
	return NULL;
}

saml2::KeyData spep::MetadataImpl::MetadataKeyResolver::resolveKey (std::string keyName)
{
	KeyMap::iterator iter = this->_map.find(keyName);
	if (iter != this->_map.end())
		return iter->second;
	
	throw MetadataException( "Key could not be resolved - no key by that name exists" );
}

XSECKeyInfoResolver* spep::MetadataImpl::MetadataKeyResolver::clone() const
{
	return new MetadataKeyResolver( this->_logger, this->_map );
}
