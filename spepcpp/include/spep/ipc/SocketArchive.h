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
 * Creation Date: 31/01/2007
 * 
 * Purpose: 
 */

#ifndef SOCKETARCHIVE_H_
#define SOCKETARCHIVE_H_

#include <iostream>
#include <vector>
#include <map>
#include <string>

#include <unicode/unistr.h>
#include <unicode/ustring.h>

#include <boost/function.hpp>
#include <boost/date_time/posix_time/posix_time.hpp>

/* for saml2::KeyData */
#include "saml2/handlers/MetadataOutput.h"

/* for saml2::LogLevel */
#include "saml2/logging/LogLevel.h"

#include "spep/Util.h"
#include "spep/Base64.h"
#include "spep/ipc/Exceptions.h"

namespace spep
{
	namespace ipc
	{
		
		/**
		 * This object, when serialized, puts no data into the stream. Also, when
		 * deserialized it reads no data from the stream. Its purpose is for 0
		 * argument methods, since a request object must be presented.
		 */
		struct NoData{};
		
		class SPEPEXPORT SocketArchive
		{
			public:
			
			SocketArchive(boost::function<void(const std::vector<char>&)> writeCallback, boost::function<void(std::vector<char>&)> readCallback) : 
			_writeCallback(writeCallback),
			_readCallback(readCallback),
			_in(this),
			_out(this)
			{
			}
			
			/**
			 * The input half of a socket archive. This is a generic deserializer
			 */
			class SocketArchiveInput
			{
				public:
				SocketArchiveInput(SocketArchive *sa) :
				_sa(sa) {}

				/**
				 * The deserialization methods. 
				 */
				/*@{*/
				
				void load(bool &b)
				{ int i; _sa->loadLexical(i); b = (i != 0); }
				
				void load(char &c)
				{ _sa->loadLexical(c); }
				
				void load(unsigned char &c)
				{ _sa->loadLexical(c); }
				
				void load(short &s)
				{ _sa->loadLexical(s); }
				
				void load(unsigned short &s)
				{ _sa->loadLexical(s); }
				
				void load(int &i)
				{ _sa->loadLexical(i); }
				
				void load(unsigned int &i)
				{ _sa->loadLexical(i); }
				
				void load(long &l)
				{ _sa->loadLexical(l); }
				
				void load(unsigned long &l)
				{ _sa->loadLexical(l); }
				
				void load(float &f)
				{ _sa->loadLexical(f); }
				
				void load(double &d)
				{ _sa->loadLexical(d); }
				
				void load(long double &d)
				{ _sa->loadLexical(d); }

#ifdef _WIN64
                void load(size_t &st)
                { _sa->loadLexical(st); }
#endif

				void load(saml2::LogLevel &level)
				{
					int val;
					load( val );
					level = static_cast<saml2::LogLevel>(val);
				}
				
				void load(boost::posix_time::ptime &ptime)
				{
					std::string timeString;
					load( timeString );
					
					ptime = boost::posix_time::from_iso_string( timeString );
				}
				
				template <class CharT>
				void load(std::basic_string<CharT> &s)
				{
					_sa->loadString(s);
				}
				
				void load(UnicodeString &string)
				{
					std::basic_string<UChar> ucharString;

					load(ucharString);
					string.setTo(ucharString.c_str(), ucharString.length());
				}
				
				void load( NoData& )
				{
				}
				
				void load( saml2::KeyData &keyData )
				{
					load( keyData.p );
					load( keyData.y );
					load( keyData.j );
					load( keyData.g );
					load( keyData.q );
					load( keyData.modulus );
					load( keyData.exponent );
					load( keyData.keyName );
					
					unsigned int type;
					load( type );
					keyData.type = static_cast<saml2::KeyData::KeyType>(type);
				}
				
				template <class K, class V>
				void load( std::map<K,V> &map )
				{
					typename std::map<K,V>::size_type size;
					// First, load the number of elements in the map.
					load( size );
					
					// Then for every element..
					for( typename std::map<K,V>::size_type i = 0; i < size; ++i )
					{
						K k; 
						V v;
						// .. load the key..
						load(k);
						// .. load the value..
						load(v);
						
						// .. and insert the pair into the map.
						map.insert( std::make_pair( k, v ) );
					}
				}
				
				template <class E>
				void load( std::vector<E> &vec )
				{
					typename std::vector<E>::size_type size;
					// First, load the number of elements in the list.
					load( size );
					
					// Then for every element..
					for( typename std::vector<E>::size_type i = 0; i < size; ++i )
					{
						E e;
						// .. load it..
						load(e);
						
						// .. and insert it into the list.
						vec.push_back( e );
					}
				}
				
				template <class T>
				void load( T &t )
				{
					// The 0 is hard coded since the version is just a placeholder at the moment.
					spep::ipc::access::serialize( t, *this, 0 );
				}
				
				/*@}*/
				
				
				/**
				 * Operators for deserializing with a stream-like syntax
				 */
				/*@{*/
				template <class T>
				SocketArchiveInput &operator &(T &t)
				{ load(t); return *this; }
				
				template <class T>
				SocketArchiveInput &operator >>(T &t)
				{ load(t); return *this; }
				/*@}*/
				
				private:
				SocketArchive *_sa;
			};

			/**
			 * The output half of a socket archive. This is a generic serializer 
			 */
			class SocketArchiveOutput
			{
				public:
				SocketArchiveOutput(SocketArchive *sa) :
				_sa(sa) {}
				
				/**
				 * The serialization methods.
				 */
				/*@{*/
				void save(bool b)
				{ _sa->saveLexical(b); }
				
				void save(char c)
				{ _sa->saveLexical(c); }
				
				void save(unsigned char c)
				{ _sa->saveLexical(c); }
				
				void save(short s)
				{ _sa->saveLexical(s); }
				
				void save(unsigned short s)
				{ _sa->saveLexical(s); }
				
				void save(int i)
				{ _sa->saveLexical(i); }
				
				void save(unsigned int i)
				{ _sa->saveLexical(i); }
				
				void save(long l)
				{ _sa->saveLexical(l); }
				
				void save(unsigned long l)
				{ _sa->saveLexical(l); }
				
				void save(float f)
				{ _sa->saveLexical(f); }
				
				void save(double d)
				{ _sa->saveLexical(d); }
				
				void save(long double d)
				{ _sa->saveLexical(d); }

#ifdef _WIN64
                void save(size_t st)
                { _sa->saveLexical(st); }
#endif
				
				void save(saml2::LogLevel &level)
				{
					int val = level;
					save( val );
				}
				
				void save(boost::posix_time::ptime &ptime)
				{
					std::string timeString( boost::posix_time::to_iso_string(ptime) );
					save( timeString );
				}
				
				template <class CharT>
				void save(std::basic_string<CharT> &s)
				{
					_sa->saveString(s);
				}

                template <class CharT>
                void save(const std::basic_string<CharT> &s)
                {
                    _sa->saveString(s);
                }
				
				void save(UnicodeString &string)
				{
					std::basic_string<UChar> ucharString(string.getBuffer(), string.length());
					save(ucharString);
				}
				
				void save( NoData& )
				{
				}
				
				void save( saml2::KeyData &keyData )
				{
					// Save all possible KeyData fields.
					save( keyData.p );
					save( keyData.y );
					save( keyData.j );
					save( keyData.g );
					save( keyData.q );
					save( keyData.modulus );
					save( keyData.exponent );
					save( keyData.keyName );
					
					unsigned int type = static_cast<unsigned int>(keyData.type);
					save( type );
				}
				
				template <class K, class V>
				void save( std::map<K,V> &map )
				{
					// Save the number of elements
					save( map.size() );
					typename std::map<K,V>::iterator iter;
					// For each element..
					for( iter = map.begin(); iter != map.end(); ++iter )
					{
						// ..save the key..
						K k = iter->first;
						// ..then save the value.
						V v = iter->second;
						save( k );
						save( v );
					}
				}
				
				template <class E>
				void save( std::vector<E> &vec )
				{
					// Save the number of elements
					save( vec.size() );
					typename std::vector<E>::iterator iter;
					// Save each element.
					for( iter = vec.begin(); iter != vec.end(); ++iter )
					{
						E e = *iter;
						save( e );
					}
				}
				
				template <class T>
				void save(T &t)
				{
					// Generic type serializer. Calls T::serialize(*this,0);
					spep::ipc::access::serialize( t, *this, 0 );
				}

                template <class T>
                void save(const T &t)
                {
                    // Generic type serializer. Calls T::serialize(*this,0);
                    spep::ipc::access::serialize(t, *this, 0);
                }
				/*@}*/
				
				/**
				 * Operators for serializing with a stream-like syntax
				 */
				/*@{*/
				template <class T>
				SocketArchiveOutput &operator &(T &t)
				{ save(t); return *this; }

                template <class T>
                SocketArchiveOutput &operator &(const T &t)
                {
                    save(t); return *this;
                }
				
				template <class T>
				SocketArchiveOutput &operator <<(T &t)
				{ save(t); return *this; }

                template <class T>
                SocketArchiveOutput &operator <<(const T &t)
                {
                    save(t); return *this;
                }
				/*@}*/
			
				private:
				SocketArchive *_sa;
			};
			
			/**
			 * Accessor method for the input(deserialization) side of the socket archive
			 */
			SocketArchiveInput &in()
			{ return _in; }
			/**
			 * Accessor method for the output(serialization) side of the socket archive
			 */
			SocketArchiveOutput &out()
			{ return _out; }

			template<class T>
			void loadLexical(T &t)
			{
				std::string value;
				loadString(value);

				if (value.length() == 0) {
					t = T();
				} else {
					t = boost::lexical_cast<T>(value);
				}
			}
			
			template<class T>
			void saveLexical(T &t)
			{
				std::string value(boost::lexical_cast<std::string>(t));
				saveString(value);
			}

			template<class CharT>
			void loadString(std::basic_string<CharT> &str) {
				std::vector<char> buffer;
				
				read(buffer);
				
				if (buffer.size() > 0)
				{
					CharT *buf = reinterpret_cast<CharT*>(&buffer.front());
					std::size_t len = (buffer.size() * sizeof(char)) / sizeof(CharT);
					str = std::basic_string<CharT>(buf, len);
				}
				else 
				{
					str = std::basic_string<CharT>();
				}
			}

			template<class CharT>
			void saveString(const std::basic_string<CharT> &str) {
				
				std::size_t len = (str.length() * sizeof(CharT)) / sizeof(char);
				
				if (len > 0)
				{
					std::vector<char> buffer(len);
					std::memcpy(&buffer.front(), str.c_str(), len);
					write(buffer);
				}
				else
				{
					write(std::vector<char>(1, '\0'));
				}
			}

			/**
			 * Loads binary data using the callback method.
			 * @param address Location to set the buffer address.
			 * @param size Location to set the size of the buffer.
			 */
			void read(std::vector<char>& buffer) {
				_readCallback(buffer);
			}
			
			void write(const std::vector<char>& buffer) {
				_writeCallback(buffer);
			}

			private:
			boost::function<void(const std::vector<char>&)> _writeCallback;
			boost::function<void(std::vector<char>&)> _readCallback;
			SocketArchiveInput _in;
			SocketArchiveOutput _out;
		};
		
	}
}

#endif /*SOCKETARCHIVE_H_*/
