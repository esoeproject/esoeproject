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
 * Creation Date: 23/01/2007
 * 
 * Purpose: 
 */

#ifndef UTIL_H_
#define UTIL_H_

// For wstring workaround.
#include "saml2/SAML2Defs.h"
#include <iostream>

#include <boost/thread/recursive_mutex.hpp>
#include <boost/thread/thread.hpp>
#include <boost/thread/xtime.hpp>

#include <xercesc/util/XMLString.hpp>
#include <xercesc/dom/DOMDocument.hpp>

XERCES_CPP_NAMESPACE_USE

#define NANOSECONDS_PER_MILLISECOND 1000*1000
#define MILLISECONDS_PER_SECOND 1000
#define NANOSECONDS_PER_SECOND MILLISECONDS_PER_SECOND*NANOSECONDS_PER_MILLISECOND

namespace spep
{

	namespace ConfigurationConstants
	{
		/** SAML Protocol schema */
		static const std::string samlProtocol = "saml-schema-protocol-2.0.xsd";
		/** SAML Assertion schema */
		static const std::string samlAssertion = "saml-schema-assertion-2.0.xsd";
		/** SAML Metadata schema */
		static const std::string samlMetadata = "saml-schema-metadata-2.0.xsd";
		/** LXACML schema */
		static const std::string lxacml = "lxacml-schema.xsd";
		/** LXACML SAML Protocol schema */
		static const std::string lxacmlSAMLProtocol = "lxacml-schema-saml-protocol.xsd";
		/** LXACML SAML Assertion schema */
		static const std::string lxacmlSAMLAssertion  = "lxacml-schema-saml-assertion.xsd";
		/** LXACML Group Target schema */
		static const std::string lxacmlGroupTarget = "lxacml-schema-grouptarget.xsd";
		/** LXACML Context schema */
		static const std::string lxacmlContext = "lxacml-schema-context.xsd";
		/** LXACML Metadata schema */
		static const std::string lxacmlMetadata = "lxacml-schema-metadata.xsd";
		/** ESOE Protocol schema */
		static const std::string esoeProtocol = "esoe-schema-saml-protocol.xsd";
		/** Cache Clear Service schema */
		static const std::string cacheClearService = "cacheclear-schema-saml-metadata.xsd";
		/** SPEP Startup Service schema */
		static const std::string spepStartupService = "spepstartup-schema-saml-metadata.xsd";
		/** Session Data schema */
		static const std::string sessionData = "sessiondata-schema.xsd";
		/** Attribute Config schema */
		static const std::string attributeConfig = "attributeconfig-schema.xsd";
		/** SOAP/1.1 schema */
		static const std::string soap11 = "soap-1.1-envelope.xsd";
		/** SOAP/1.2 schema */
		static const std::string soap12 = "soap-1.2-envelope.xsd";
		
		/** Timezone in use for the SPEP */
		static const std::string timeZone = "UTC";
	}
	
	typedef boost::recursive_mutex Mutex;
	typedef Mutex::scoped_lock ScopedLock;
	
	/**
	 * Provides an automatic array allocation/deallocation method.
	 */
	template <class T>
	class AutoArray
	{
		
		private:
		T *_ptr;
		AutoArray( const AutoArray& other );
		AutoArray& operator=( const AutoArray& other );
		
		public:
		AutoArray( std::size_t n ) : _ptr( new T[n] ) {}
		AutoArray( T* ptr ) : _ptr( ptr ) {}
		~AutoArray() { delete[] _ptr; }
		
		T* operator*() { return _ptr; }
		T& operator[]( std::size_t index ) { return _ptr[index]; }
		
		T* get() { return _ptr; }
	};
	
	/**
	 * Provides an adapter so that an XMLCh string from Xerces will be released when
	 * this object is deleted.
	 */
	class XercesXMLChStringAdapter
	{
		private:
		XMLCh *_ptr;
		
		public:
		XercesXMLChStringAdapter():_ptr(NULL){}
		XercesXMLChStringAdapter( XMLCh *ptr ):_ptr(ptr){}
		explicit XercesXMLChStringAdapter( XercesXMLChStringAdapter &copy ){ _ptr = copy._ptr; copy._ptr = NULL; }
		~XercesXMLChStringAdapter() { if (_ptr != NULL) XMLString::release( &_ptr ); }
		
		XMLCh* get() { return _ptr; };
		
	};
	
	/**
	 * Provides an adapter so that a char string from Xerces will be released when
	 * this object is deleted.
	 */
	class XercesCharStringAdapter
	{
		private:
		char *_ptr;
		
		public:
		XercesCharStringAdapter():_ptr(NULL){}
		XercesCharStringAdapter( char *ptr ):_ptr(ptr){}
		explicit XercesCharStringAdapter( XercesCharStringAdapter &copy ){ _ptr = copy._ptr; copy._ptr = NULL; }
		~XercesCharStringAdapter() { if (_ptr != NULL) XMLString::release( &_ptr ); }
		
		char* get() { return _ptr; };
		
	};
	
	/**
	 * Contains a C array that can be realloc()'d when needed, and is automatically
	 * freed.
	 */
	template <typename T>
	class CArray
	{
		private:
		T* _ptr;
		
		public:
		/* Default value so we can construct an array of them */
		CArray( std::size_t n = 1 ):_ptr( static_cast<T*>(malloc(n)) ){}
		~CArray(){ free( _ptr ); }
		void resize( std::size_t n ){ _ptr = static_cast<T*>(realloc( _ptr, n )); }
		
		T* get() { return _ptr; }
		T& operator[]( std::size_t index ) { return _ptr[index]; }
		T* operator*(){ return _ptr; }
		
	};
	
	class InterruptibleSleeper
	{
		private:
		boost::xtime _targetTime;
		int _interval;
		bool *_die;
		
		public:
		/**
		 * Creates an interruptible sleeper, to sleep for the given 
		 * number of seconds or until the "die" value is true.
		 */
		InterruptibleSleeper( int seconds, int milliseconds, int pollIntervalMilliseconds, bool* die )
		:
		_targetTime(),
		_interval( pollIntervalMilliseconds * NANOSECONDS_PER_MILLISECOND ),
		_die( die )
		{
			boost::xtime_get( &_targetTime, boost::TIME_UTC ); 
			milliseconds += _targetTime.nsec / NANOSECONDS_PER_MILLISECOND;
			seconds += milliseconds / MILLISECONDS_PER_SECOND;
			_targetTime.nsec = (milliseconds % MILLISECONDS_PER_SECOND) * NANOSECONDS_PER_MILLISECOND;
			_targetTime.sec += seconds;
		}
		
		inline void sleep()
		{
			for(;;)
			{
				// Time to die?
				if( _die != NULL )
				{
					if( *_die )
					{
						// Yes, die.
						return;
					}
				}
				// No, continue sleeping.
				
				// Get the current time.
				boost::xtime nextUpdate;
				boost::xtime_get( &nextUpdate, boost::TIME_UTC );
				
				// Calculate when the next update should occur.
				nextUpdate.nsec += _interval;
				while( nextUpdate.nsec >= NANOSECONDS_PER_SECOND )
				{
					nextUpdate.nsec -= NANOSECONDS_PER_SECOND;
					nextUpdate.sec++;
				}
				
				// If the target time isn't within our grasp yet, sleep until the next update
				if( _targetTime.sec > nextUpdate.sec ||
					_targetTime.sec == nextUpdate.sec && _targetTime.nsec > nextUpdate.nsec )
				{
					boost::thread::sleep( nextUpdate );
				}
				// Otherwise sleep until the target time and return.
				else
				{
					boost::thread::sleep( _targetTime );
					return;
				}
			}
		}
	};
	
	inline void delay(int secs, int msecs) {
		bool die = false;
		InterruptibleSleeper sleeper(secs, msecs, secs*1000+msecs, &die);

		sleeper.sleep();
	}
}


#if defined(WIN32) && defined(_MSC_VER)

#ifdef BUILDING_SPEP
#define SPEPEXPORT __declspec(dllexport)
#define SPEPCONSTANT __declspec(dllexport) extern
#else
#define SPEPEXPORT __declspec(dllimport)
#define SPEPCONSTANT __declspec(dllimport) extern
#endif /*BUILDING_SPEP*/

// VC++ (or more accurately, the windows platform SDK) doesn't define snprintf.. 
// instead it defines _snprintf
#define snprintf _snprintf

// Also, it complains about half the functions not being safe... so
// we flag them not to be deprecated.
#define _CRT_SECURE_NO_DEPRECATE 1

// Include this ASAP so other libraries including windows.h can't break us.
#include <winsock2.h>

// Bring this in for the class definition
#include <xercesc/dom/DOMDocument.hpp>

// Windows defines a DOMDocument type, so we need to get around that..
namespace spep
{
	typedef XERCES_CPP_NAMESPACE::DOMDocument DOMDocument;
}
#endif /*WIN32 && _MSC_VER */

#ifdef __GNUC__

#ifdef BUILDING_SPEP
#define SPEPEXPORT __attribute((visibility("default")))
#define SPEPCONSTANT
#else
#define SPEPEXPORT
#define SPEPCONSTANT
#endif /*BUILDING_SPEP*/

#endif

#ifndef SPEPEXPORT
// If nothing has been defined, we don't need any special flags to export symbols
#define SPEPEXPORT 
#define SPEPCONSTANT 
#endif /*SPEPEXPORT*/

#endif /*UTIL_H_*/
