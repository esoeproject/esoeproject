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

#include "spep/metadata/MetadataThread.h"
#include "spep/metadata/impl/MetadataImpl.h"

#include "spep/exceptions/InvalidStateException.h"
#include "spep/exceptions/MetadataException.h"

#include <boost/thread.hpp>

spep::MetadataThread::RawMetadata::RawMetadata(const spep::MetadataThread::RawMetadata &rhs) :
    hashContext(rhs.hashContext),
    data(new char[rhs.len]),
    len(rhs.len),
    hashValue(rhs.hashValue),
    failed(rhs.failed)
{
	memcpy(data, rhs.data, len);
}

spep::MetadataThread::RawMetadata::RawMetadata(EVP_MD_CTX *ctx) :
    hashContext(ctx),
    data(nullptr),
    len(0),
    hashValue(""),
    failed(false)
{
}

spep::MetadataThread::RawMetadata::~RawMetadata()
{
	delete[] data;
    data = nullptr;
}

spep::MetadataThread::RawMetadata& spep::MetadataThread::RawMetadata::operator=(const spep::MetadataThread::RawMetadata &rhs)
{
	// Handle self-assignment.
	if (&rhs == this) return *this;
	
	// Copy the RawMetadata given into 'this' object.
	len = rhs.len;
	data = new char[len];
	memcpy(data, rhs.data, len);
	hashValue = rhs.hashValue;
	hashContext = rhs.hashContext;
	failed = rhs.failed;
	
	return *this;
}

spep::MetadataThread::ThreadHandler::ThreadHandler(MetadataThread *metadataThread, saml2::Logger *logger) :
    mMetadataThread(metadataThread),
    mLocalLogger(logger, "spep::MetadataThread::ThreadHandler")
{
}

spep::MetadataThread::ThreadHandler::ThreadHandler(const ThreadHandler& other) :
    mMetadataThread(other.mMetadataThread),
    mLocalLogger(mMetadataThread->mLogger, "spep::MetadataThread::ThreadHandler")
{
}

void spep::MetadataThread::ThreadHandler::operator()()
{
	mLocalLogger.debug() << "Metadata background thread invoked. Doing initial metadata retrieval.";
	
	// Struct to hold timestamp for doing a thread sleep.
	boost::xtime nextUpdate;
	try
	{
		// Perform the initial metadata retrieve operation.
		mMetadataThread->doGetMetadata();
		
		// If we get here, the metadata operation succeeded.
		mMetadataThread->mMetadata->mError = false;
	}
	catch (std::exception& ex)
	{
		std::stringstream error;
		error << "Exception thrown during metadata retrieval process. Error was: " << ex.what() << std::ends;
		mLocalLogger.error() << error.str();
		mMetadataThread->mMetadata->mError = true;
	}
	catch (...)
	{
		mLocalLogger.error() << "Unexpected throw during metadata retrieval process. Initial retrieve failed.";
		mMetadataThread->mMetadata->mError = true;
	}
	
	// Enter the "main" loop for the metadata processor.
	mLocalLogger.debug() << "Metadata background thread entering loop.";
	while (!mMetadataThread->mDie)
	{
		try
		{
			// Get the current timestamp.
			if (boost::xtime_get(&nextUpdate, boost::TIME_UTC_) == 0)
			{
				throw InvalidStateException("Couldn't get UTC time from boost::xtime_get()");
			}
			
			// Add the update interval to the timestamp and sleep until that time.
			nextUpdate.sec += mMetadataThread->mInterval;
			
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
			boost::thread::sleep(nextUpdate);
			
			// Perform the metadata retrieve operation.
			mLocalLogger.debug() << "Metadata background thread woke up. Doing metadata retrieval.";
			mMetadataThread->doGetMetadata();
			mMetadataThread->mMetadata->mError = false;
		}
		catch (std::exception& ex)
		{
			// An error occurred. Log it, but don't set the error flag because we can continue operation
			// using the old metadata we have cached.
			std::stringstream error;
			error << "Exception thrown during metadata retrieval process. Error was: " << ex.what() << std::ends;
			mLocalLogger.error() << error.str();
		}
		catch (...)
		{
			mLocalLogger.error() << "Unexpected throw during metadata retrieval process. Continuing.";
		}
	}
}

spep::MetadataThread::MetadataThread(saml2::Logger *logger, spep::MetadataImpl *metadata, const std::string& caBundle, const std::string& schemaPath, int interval, saml2::ExternalKeyResolver* extKeyResolver) :
    mLogger(logger),
    mLocalLogger(logger, "spep::MetadataThread"),
    mMetadata(metadata),
    mInterval(interval),
    mHashType(EVP_sha1()), // Initialize to SHA1 hash
    mCABundle(caBundle),
    mMetadataUnmarshaller(nullptr),
    mDie(false)
{
    EVP_MD_CTX_init(&(this->mHashContext));
    std::vector<std::string> metadataSchemas{
        ConfigurationConstants::samlProtocol,
        ConfigurationConstants::lxacmlMetadata,
        ConfigurationConstants::cacheClearService,
        ConfigurationConstants::spepStartupService };

    mMetadataUnmarshaller = std::make_unique<saml2::UnmarshallerImpl<saml2::metadata::EntitiesDescriptorType>>(logger, schemaPath, metadataSchemas, extKeyResolver);
}

spep::MetadataThread::ThreadHandler spep::MetadataThread::getThreadHandler()
{
	return ThreadHandler(this, mLogger);
}

std::size_t spep::MetadataThread::curlCallback(void *buffer, std::size_t size, std::size_t nmemb, void *userp)
{
	// This will only be invoked from an internal call to cURL, so we 
	// will always have the right kind of object being passed in here.
	RawMetadata *data = (RawMetadata*)userp;
	
	// Number of bytes is size*nmemb (see man curl_easy_setopt(3))
	std::size_t bytes = (size * nmemb);
	std::size_t newSize = data->len + bytes;
	// Reallocate the buffer and copy the data
	char *newBuffer = new char[newSize];
	
	if (data->data != nullptr)
		memcpy(newBuffer, data->data, data->len);
	
	memcpy(&newBuffer[data->len], buffer, bytes);
	
	// Update the digest 
	EVP_DigestUpdate(data->hashContext, buffer, bytes);
	
	delete[] data->data;
	data->data = newBuffer;
	data->len = newSize;
	
	return bytes;
}

int spep::MetadataThread::debugCallback(CURL *curl, curl_infotype info, char *msg, std::size_t len, void *userp)
{
	MetadataThread *metadataThread = static_cast<MetadataThread*>(userp);
	std::stringstream ss;
	
    switch (info)
    {
    case CURLINFO_TEXT:
        //The data is informational text.
        ss << "curl-info: " << std::string(msg, len);
        metadataThread->mLocalLogger.debug() << ss.str();
        break;

    case CURLINFO_HEADER_IN:
        //The data is header (or header-like) data received from the peer.
        //ss << "curl-header-in: " << std::string( msg, len );
        //metadataThread->_localLogger.debug() << ss.str();
        break;

    case CURLINFO_HEADER_OUT:
        //The data is header (or header-like) data sent to the peer.
        //ss << "curl-header-out: " << std::string( msg, len );
        //metadataThread->_localLogger.debug() << ss.str();
        break;

    case CURLINFO_DATA_IN:
        //The data is protocol data received from the peer.
        //ss << "curl-data-in: len=" << len << std::ends;
        //metadataThread->_localLogger.debug() << ss.str();
        break;

    case CURLINFO_DATA_OUT:
        //The data is protocol data sent to the peer.
        //ss << "curl-data-out: len=" << len << std::ends;
        //metadataThread->_localLogger.debug() << ss.str();
        break;

    default:
        //ss << "curl-unknown-message: [suppressing output]";
        //metadataThread->_localLogger.debug() << ss.str();
        break;
    }
	
	return 0;
}

void spep::MetadataThread::doGetMetadata()
{
	RawMetadata data = getRawMetadata(mMetadata->mMetadataURL);
	
	// The retrieve failed. Can't continue.
	if (data.failed)
		throw MetadataException("Metadata document download failed. Couldn't perform metadata update.");

	mLocalLogger.info() << "Metadata retrieve succeeded. Going to process. Hash code is " << data.hashValue << " (current: " << mMetadata->mCurrentRevision << ")";
			
	// Check if the metadata has been modified since the last time the cache was updated.
	if (data.hashValue.compare(mMetadata->mCurrentRevision) != 0)
	{
		mLocalLogger.info() << "Hash code has changed. Rebuilding metadata cache."; 
		
		try
		{
			saml2::SAMLDocument metadataDocument((SAMLByte*)data.data, data.len);
			data.data = nullptr;
			
			// Unmarshal the metadata..
			std::auto_ptr<saml2::MetadataOutput<saml2::metadata::EntitiesDescriptorType> > mdo( 
				mMetadataUnmarshaller->unMarshallMetadata(metadataDocument, true)
			);
			
			// .. and use it to rebuild the metadata cache.
			mMetadata->rebuildCache(*(mdo->xmlObj), data.hashValue, mdo->keyList);
			
			mLocalLogger.info() << "Finished rebuilding metadata cache.";
		}
		catch (saml2::InvalidParameterException& ex)
		{
			mLocalLogger.error() << "Invalid parameter passed to unmarshaller. Unmarshalling failed."; 
			throw;
		}
		catch (saml2::UnmarshallerException& ex)
		{
			mLocalLogger.error() << "Invalid metadata document. Unmarshalling failed: " << ex.getMessage();
			throw;
		}
	}
	else
	{
		mLocalLogger.debug() << "Hash code has not changed. Ignoring new metadata."; 
	}
}

spep::MetadataThread::RawMetadata spep::MetadataThread::getRawMetadata(const std::string& metadataURL)
{
	EVP_MD_CTX *hashContext = &(mHashContext);

	// Initialize the hash function for the new metadata.
	if (!EVP_DigestInit_ex(hashContext, mHashType, NULL))
	{
		throw InvalidStateException("Unable to initialize the digest function for metadata hashing");
	}

	RawMetadata data(hashContext);
	
	CArray<char> errorBuffer(CURL_ERROR_SIZE);
	std::memset(errorBuffer.get(), 0, CURL_ERROR_SIZE);
	
	mLocalLogger.debug() << "Calling cURL to retrieve metadata from " << metadataURL;

	CURL *pCurlHandle = curl_easy_init();

	// Set the URL for curl to retrieve from
	curl_easy_setopt(pCurlHandle, CURLOPT_URL, metadataURL.c_str());
	// Give curl something to call with its data
	curl_easy_setopt(pCurlHandle, CURLOPT_WRITEFUNCTION, spep::MetadataThread::curlCallback);
	curl_easy_setopt(pCurlHandle, CURLOPT_WRITEDATA, (void*)&data);
	// Buffer to output an error message if the call fails
	curl_easy_setopt(pCurlHandle, CURLOPT_ERRORBUFFER, errorBuffer.get());
	// Don't give us any content on a HTTP >=400 response
	curl_easy_setopt(pCurlHandle, CURLOPT_FAILONERROR, 1);
	// Ignore signals
	curl_easy_setopt(pCurlHandle, CURLOPT_NOSIGNAL, 1L);
	// Debugging code
	curl_easy_setopt(pCurlHandle, CURLOPT_DEBUGFUNCTION, spep::MetadataThread::debugCallback);
	curl_easy_setopt(pCurlHandle, CURLOPT_DEBUGDATA, (void*)this);
	curl_easy_setopt(pCurlHandle, CURLOPT_VERBOSE, 1);

	// Set the CA bundle, if we were given one
	if (!mCABundle.empty())
	{
		curl_easy_setopt(pCurlHandle, CURLOPT_CAINFO, mCABundle.c_str());
	}

	// Perform the retrieve operation. This will block until complete.
	CURLcode result = curl_easy_perform(pCurlHandle);
	curl_easy_cleanup(pCurlHandle);
	
	mLocalLogger.debug() << boost::lexical_cast<std::string>(result);

	// If the request didn't succeed, handle the error condition.
	if (result != CURLE_OK)
	{
		mLocalLogger.error() << std::string("Metadata retrieve failed. Error message was: ") << errorBuffer.get();
		data.failed = true;
		return data;
	}
	
	data.failed = false;
	
	// Finalise the hash
	unsigned int hashLength = 0;
	unsigned char hashValue[EVP_MAX_MD_SIZE];
	
	EVP_DigestFinal_ex(hashContext, hashValue, &hashLength);
	
	// .. and build it into a string to be returned in the object.
	AutoArray<char> hashChars(hashLength * 2 + 1);
	for (unsigned int i = 0; i < hashLength; ++i)
	{
		// Output each byte as 2 hex chars.
		snprintf(&(hashChars[2*i]), 3, "%02x", hashValue[i]);
	}
	
	data.hashValue = std::string(hashChars.get(), hashLength * 2);
	return data;
}


