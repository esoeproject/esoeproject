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
 * Creation Date: 27/02/2007
 * 
 * Purpose: 
 */

#include "metadata/MetadataThread.h"
#include "metadata/impl/MetadataImpl.h"

#include "exceptions/InvalidStateException.h"
#include "exceptions/MetadataException.h"

#include <boost/thread.hpp>

spep::MetadataThread::RawMetadata::RawMetadata( const spep::MetadataThread::RawMetadata &rhs )
:
hashContext( rhs.hashContext ),
data( new char[rhs.len] ),
len( rhs.len ),
hashValue( rhs.hashValue )
{
	memcpy(this->data, rhs.data, this->len);
}

spep::MetadataThread::RawMetadata::RawMetadata( EVP_MD_CTX *ctx )
:
hashContext( ctx ),
data( NULL ),
len( 0 ),
hashValue("")
{
}

spep::MetadataThread::RawMetadata::~RawMetadata()
{
	if( this->data != NULL ) delete[] this->data;
}

spep::MetadataThread::RawMetadata& spep::MetadataThread::RawMetadata::operator=( const spep::MetadataThread::RawMetadata &rhs )
{
	// Handle self-assignment.
	if( &rhs == this ) return *this;
	
	// Copy the RawMetadata given into 'this' object.
	this->len = rhs.len;
	this->data = new char[this->len];
	memcpy(this->data, rhs.data, this->len);
	this->hashValue = rhs.hashValue;
	this->hashContext = rhs.hashContext;
	
	return *this;
}

spep::MetadataThread::ThreadHandler::ThreadHandler( MetadataThread *metadataThread, ReportingProcessor *reportingProcessor )
:
_metadataThread( metadataThread ),
_localReportingProcessor( reportingProcessor->localReportingProcessor("spep::MetadataThread::ThreadHandler"))
{
}

spep::MetadataThread::ThreadHandler::ThreadHandler( const ThreadHandler& other )
:
_metadataThread( other._metadataThread ),
_localReportingProcessor( other._localReportingProcessor )
{
}

void spep::MetadataThread::ThreadHandler::operator()()
{
	this->_localReportingProcessor.log( DEBUG, "Metadata background thread invoked. Doing initial metadata retrieval." );
	
	// Struct to hold timestamp for doing a thread sleep.
	boost::xtime nextUpdate;
	try
	{
		// Perform the initial metadata retrieve operation.
		this->_metadataThread->doGetMetadata();
		
		// If we get here, the metadata operation succeeded.
		this->_metadataThread->_metadata->_error = false;
	}
	catch (std::exception &ex)
	{
		std::stringstream error;
		error << "Exception thrown during metadata retrieval process. Error was: " << ex.what() << std::ends;
		this->_localReportingProcessor.log( ERROR, error.str() );
		this->_metadataThread->_metadata->_error = true;
	}
	catch (...)
	{
		this->_localReportingProcessor.log( ERROR, "Unexpected throw during metadata retrieval process. Initial retrieve failed." );
		this->_metadataThread->_metadata->_error = true;
	}
	
	// Enter the "main" loop for the metadata processor.
	this->_localReportingProcessor.log( DEBUG, "Metadata background thread entering loop." );
	while(!this->_metadataThread->_die)
	{
		try
		{
			// Get the current timestamp.
			if( boost::xtime_get( &nextUpdate, boost::TIME_UTC ) == 0 )
			{
				throw InvalidStateException( "Couldn't get UTC time from boost::xtime_get()" );
			}
			
			// Add the update interval to the timestamp and sleep until that time.
			nextUpdate.sec += this->_metadataThread->_interval;
			
			// TODO Valgrind complains about this call.. why? 
			/*
			 * ==10383==    at 0x40050FF: free (vg_replace_malloc.c:233)
			 * ==10383==    by 0x543BE3D: free_mem (in /lib/libc-2.6.so)
			 * ==10383==    by 0x543B986: __libc_freeres (in /lib/libc-2.6.so)
			 * ==10383==    by 0x40011E6: _vgnU_freeres (vg_preloaded.c:60)
			 * ==10383==    by 0x53BC115: (within /lib/libc-2.6.so)
			 * ==10383==    by 0x434185D: spep::MetadataThread::ThreadHandler::operator()() (MetadataThread.cpp:123)
			 * ...
			 * Address 0x4433D58 is not stack'd, malloc'd or (recently) free'd
			 * 
			 * (line 123 was the location of the boost::thread::sleep() call at the time.)
			 */
			boost::thread::sleep( nextUpdate );
			
			// Perform the metadata retrieve operation.
			this->_localReportingProcessor.log( DEBUG, "Metadata background thread woke up. Doing metadata retrieval." );
			this->_metadataThread->doGetMetadata();
			this->_metadataThread->_metadata->_error = false;
		}
		catch (std::exception &ex)
		{
			// An error occurred. Log it, but don't set the error flag because we can continue operation
			// using the old metadata we have cached.
			std::stringstream error;
			error << "Exception thrown during metadata retrieval process. Error was: " << ex.what() << std::ends;
			this->_localReportingProcessor.log( ERROR, error.str() );
		}
		catch (...)
		{
			this->_localReportingProcessor.log( ERROR, "Unexpected throw during metadata retrieval process. Continuing." );
		}
	}
}

spep::MetadataThread::ThreadHandler spep::MetadataThread::getThreadHandler()
{
	return ThreadHandler( this, this->_reportingProcessor );
}

std::size_t spep::MetadataThread::curlCallback( void *buffer, std::size_t size, std::size_t nmemb, void *userp )
{
	// This will only be invoked from an internal call to cURL, so we 
	// will always have the right kind of object being passed in here.
	RawMetadata *data = (RawMetadata*)userp;
	
	// Number of bytes is size*nmemb (see man curl_easy_setopt(3))
	std::size_t bytes = ( size * nmemb );
	std::size_t newSize = data->len + bytes;
	// Reallocate the buffer and copy the data
	char *newBuffer = new char[newSize];
	
	if( data->data != NULL )
		memcpy( newBuffer, data->data, data->len );
	
	memcpy( &newBuffer[data->len], buffer, bytes );
	
	// Update the digest 
	EVP_DigestUpdate( data->hashContext, buffer, bytes );
	
	delete[] data->data;
	data->data = newBuffer;
	data->len = newSize;
	
	return bytes;
}

void spep::MetadataThread::doGetMetadata()
{
	RawMetadata data = this->getRawMetadata( this->_metadata->_metadataURL );
	
	// The retrieve failed. Can't continue.
	if( data.failed )
		throw MetadataException( "Metadata document download failed. Couldn't perform metadata update." );

	this->_localReportingProcessor.log( DEBUG, "Metadata retrieve succeeded. Going to process. Hash code is " + data.hashValue + " (current: " + this->_metadata->_currentRevision + ")" );
			
	// Check if the metadata has been modified since the last time the cache was updated.
	if ( data.hashValue.compare( this->_metadata->_currentRevision ) != 0 )
	{
		this->_localReportingProcessor.log( DEBUG, "Hash code has changed. Rebuilding metadata cache." ); 
		
		try
		{
			saml2::SAMLDocument metadataDocument( (SAMLByte*)data.data, data.len );
			data.data = NULL;
			
			// Unmarshal the metadata..
			std::auto_ptr<saml2::MetadataOutput<saml2::metadata::EntitiesDescriptorType> > mdo( 
				this->_metadataUnmarshaller->unMarshallMetadata( metadataDocument, this->_metadata->_keyResolver->getMetadataKey(), true )
			);
			
			// .. and use it to rebuild the metadata cache.
			this->_metadata->rebuildCache( *(mdo->xmlObj), data.hashValue, mdo->keyList );
			
			this->_localReportingProcessor.log( DEBUG, "Finished rebuilding metadata cache." );
		}
		catch (saml2::InvalidParameterException ex)
		{
			this->_localReportingProcessor.log( ERROR, "Invalid parameter passed to unmarshaller. Unmarshalling failed." ); 
			throw;
		}
		catch (saml2::UnmarshallerException ex)
		{
			this->_localReportingProcessor.log( ERROR, "Invalid metadata document. Unmarshalling failed: " + ex.getMessage() );
			throw;
		}
	}
	else
	{
		this->_localReportingProcessor.log( DEBUG, "Hash code has not changed. Ignoring new metadata." ); 
	}
}

spep::MetadataThread::RawMetadata spep::MetadataThread::getRawMetadata( std::string metadataURL )
{
	EVP_MD_CTX *hashContext = &(this->_hashContext);
	// Initialize the hash function for the new metadata.
	if ( ! EVP_DigestInit_ex( hashContext, this->_hashType, NULL ) )
	{
		throw InvalidStateException( "Unable to initialize the digest function for metadata hashing" );
	}

	RawMetadata data( hashContext );
	
	CArray<char> errorBuffer( CURL_ERROR_SIZE );
	std::memset( errorBuffer.get(), 0, CURL_ERROR_SIZE );
	
	this->_localReportingProcessor.log( DEBUG, "Calling cURL to retrieve metadata from " + metadataURL );

	// Set the URL for curl to retrieve from
	curl_easy_setopt(this->_curl, CURLOPT_URL, metadataURL.c_str());
	// Give curl something to call with its data
	curl_easy_setopt(this->_curl, CURLOPT_WRITEFUNCTION, spep::MetadataThread::curlCallback);
	curl_easy_setopt(this->_curl, CURLOPT_WRITEDATA, (void*)&data);
	// Buffer to output an error message if the call fails
	curl_easy_setopt(this->_curl, CURLOPT_ERRORBUFFER, errorBuffer.get());
	// Don't give us any content on a HTTP >=400 response
	curl_easy_setopt(this->_curl, CURLOPT_FAILONERROR, 1);
	// Perform the retrieve operation. This will block until complete.
	CURLcode result = curl_easy_perform(this->_curl);
	
	// If the request didn't succeed, handle the error condition.
	if (result != CURLE_OK)
	{
		this->_localReportingProcessor.log( ERROR, std::string("Metadata retrieve failed. Error message was: ") + errorBuffer.get() );
		data.failed = true;
		return data;
	}
	
	data.failed = false;
	
	// Finalise the hash
	unsigned int hashLength = 0;
	unsigned char hashValue[EVP_MAX_MD_SIZE];
	
	EVP_DigestFinal_ex( hashContext, hashValue, &hashLength );
	
	// .. and build it into a string to be returned in the object.
	AutoArray<char> hashChars( hashLength*2 + 1 );
	for (unsigned int i=0; i<hashLength; ++i)
	{
		// Output each byte as 2 hex chars.
		snprintf( &(hashChars[2*i]), 3, "%02x", hashValue[i] );
	}
	
	data.hashValue = std::string( hashChars.get(), hashLength*2 );
	return data;
}

spep::MetadataThread::MetadataThread( spep::ReportingProcessor *reportingProcessor, spep::MetadataImpl *metadata, std::string schemaPath, int interval )
:
_reportingProcessor(reportingProcessor),
_localReportingProcessor(reportingProcessor->localReportingProcessor("spep::MetadataThread")),
_metadata(metadata),
_interval(interval),
_hashType( EVP_sha1() ), // Initialize to SHA1 hash
_curl( curl_easy_init() ), // Init the cURL handle in the constructor.
_metadataUnmarshaller( NULL ),
_die(false)
{
	EVP_MD_CTX_init( &(this->_hashContext) );
	std::vector<std::string> metadataSchemas;
	metadataSchemas.push_back( ConfigurationConstants::samlProtocol );
	metadataSchemas.push_back( ConfigurationConstants::lxacmlMetadata );
	metadataSchemas.push_back( ConfigurationConstants::cacheClearService );
	metadataSchemas.push_back( ConfigurationConstants::spepStartupService );
	this->_metadataUnmarshaller = new saml2::UnmarshallerImpl<saml2::metadata::EntitiesDescriptorType>( schemaPath, metadataSchemas );
}
