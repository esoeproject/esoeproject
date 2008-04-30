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

#include <boost/date_time/posix_time/posix_time.hpp>

/* for saml2::KeyData */
#include "saml2/handlers/MetadataOutput.h"

#include "spep/ipc/Platform.h"
#include "spep/Util.h"

#define DELIMITER_STRING " "
#define DELIMITER_CHAR ' '
#define ISDELIMITER(x) ( x == DELIMITER_CHAR ) 

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
		
#		define SOCKET_ARCHIVE_BUFFER_SIZE 1024

		class SPEPEXPORT SocketArchive
		{
			public:
			
			SocketArchive( platform::socket_t socket ) : 
			_socket(socket), 
			_pos(0),
			_size(0),
			_in(this),
			_out(this),
			_closed(false),
			_eof(false)
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

/// Debug define so we can get output from the socket archive if we need it.
#define DEBUG_LOADING_TYPE(x)  {/*std::cout << std::endl << "load: " << (#x) << ' ' << std::endl;*/}

				/**
				 * The deserialization methods. 
				 */
				/*@{*/
				
				void load(bool &b)
				{ DEBUG_LOADING_TYPE(bool); int i; _sa->loadPrimitiveWholeNumber(i); b = (i != 0); }
				
				void load(char &c)
				{ DEBUG_LOADING_TYPE(char); _sa->loadPrimitiveWholeNumber(c); }
				
				void load(unsigned char &c)
				{ DEBUG_LOADING_TYPE(unsigned char); _sa->loadPrimitiveWholeNumber(c); }
				
				void load(short &s)
				{ DEBUG_LOADING_TYPE(short); _sa->loadPrimitiveWholeNumber(s); }
				
				void load(unsigned short &s)
				{ DEBUG_LOADING_TYPE(unsigned short); _sa->loadPrimitiveWholeNumber(s); }
				
				void load(int &i)
				{ DEBUG_LOADING_TYPE(int); _sa->loadPrimitiveWholeNumber(i); }
				
				void load(unsigned int &i)
				{ DEBUG_LOADING_TYPE(unsigned int); _sa->loadPrimitiveWholeNumber(i); }
				
				void load(long &l)
				{ DEBUG_LOADING_TYPE(long); _sa->loadPrimitiveWholeNumber(l); }
				
				void load(unsigned long &l)
				{ DEBUG_LOADING_TYPE(unsigned long); _sa->loadPrimitiveWholeNumber(l); }
				
				void load(float &f)
				{ DEBUG_LOADING_TYPE(float); _sa->loadPrimitiveFloatingPoint(f); }
				
				void load(double &d)
				{ DEBUG_LOADING_TYPE(double); _sa->loadPrimitiveFloatingPoint(d); }
				
				void load(long double &d)
				{ DEBUG_LOADING_TYPE(long double); _sa->loadPrimitiveFloatingPoint(d); }
				
				void load(void*& buf, std::size_t& len)
				{
					DEBUG_LOADING_TYPE(void[]);
					load( len );
					buf = new char[len];
					_sa->load_binary( buf, len );
				}
				
				void load(boost::posix_time::ptime &ptime)
				{
					DEBUG_LOADING_TYPE(boost::posix_time::ptime);
					std::string timeString;
					load( timeString );
					
					ptime = boost::posix_time::from_iso_string( timeString );
				}
				
				template <class CharT>
				void load(std::basic_string<CharT> &s)
				{
					DEBUG_LOADING_TYPE(std::basic_string<CharT>); 
					CharT *sbuf;
					std::size_t size;
					
					// First load the length of the string
					load(size);
					
					if (size > 0)
					{
						// Allocate a buffer and load into it
						sbuf = new CharT[size];
						_sa->load_binary( sbuf, size*sizeof(CharT) );
						
						// Construct the string.
						s = std::basic_string<CharT>(sbuf);
						
						delete[] sbuf;
					}
					else
					{
						// Size is 0, we don't need to load any more data.
						s = std::basic_string<CharT>();
					}
				}
				
				void load(std::string &s)
				{
					DEBUG_LOADING_TYPE(std::string); 
					char *sbuf;
					std::size_t size;
					
					// First load the length of the string
					load(size);
					
					if (size > 0)
					{
						// Allocate a buffer and load into it
						sbuf = new char[size];
						_sa->load_binary( sbuf, size );
						
						// Construct the string.
						s = std::string(sbuf);
						
						delete[] sbuf;
					}
					else
					{
						// Size is 0, we don't need to load any more data.
						s = std::string();
					}
				}
				
				void load(UnicodeString &string)
				{
					DEBUG_LOADING_TYPE(UnicodeString); 
					char *sbuf;
					std::size_t size = 0;
					
					// First load the length of the string
					load(size);
					
					if (size > 0)
					{
						// Allocate a buffer and load into it
						sbuf = new char[size];
						_sa->load_binary( sbuf, size );
						
						// Construct the string.
						string.setTo( UnicodeString( reinterpret_cast<UChar*>(sbuf) ) );
						
						delete[] sbuf;
					}
					else
					{
						// Size is 0, we don't need to load any more data.
						string = UnicodeString();
					}
				}
				
				void load( NoData& )
				{
				}
				
				void load( saml2::KeyData &keyData )
				{
					DEBUG_LOADING_TYPE( saml2::KeyData );
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
				
				template <class T, class U>
				SocketArchiveInput &operator ()(T &t, U &u)
				{ load((void*&)t,(std::size_t&)u); return *this; }
				/*@}*/
				
				private:
				SocketArchive *_sa;
			};

/// snprintf() format to serialize integer types as
#define SOCKET_ARCHIVE_INT_FORMAT "%d" DELIMITER_STRING
/// snprintf() format to serialize unsigned integer types as
#define SOCKET_ARCHIVE_UNSIGNED_INT_FORMAT "%u" DELIMITER_STRING
/// snprintf() format to serialize long integer types as
#define SOCKET_ARCHIVE_LONG_FORMAT "%ld" DELIMITER_STRING
/// snprintf() format to serialize unsigned long integer types as
#define SOCKET_ARCHIVE_UNSIGNED_LONG_FORMAT "%lu" DELIMITER_STRING
/// snprintf() format to serialize floating point types as
#define SOCKET_ARCHIVE_FLOAT_FORMAT "%.*g" DELIMITER_STRING
/// snprintf() format to serialize double floating point types as
#define SOCKET_ARCHIVE_DOUBLE_FORMAT "%.*g" DELIMITER_STRING


			/**
			 * The output half of a socket archive. This is a generic serializer 
			 */
			class SocketArchiveOutput
			{
				public:
				SocketArchiveOutput(SocketArchive *sa) :
				_sa(sa) {}
				
/// Debug define so we can get output from the socket archive if we need it.
#define DEBUG_SAVING_TYPE(x)  {/*std::cout << std::endl << "save: " << (#x) << ' ' << std::endl;*/}

				/**
				 * The serialization methods.
				 */
				/*@{*/
				void save(bool b)
				{ DEBUG_SAVING_TYPE( bool ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_INT_FORMAT, b); }
				
				void save(char c)
				{ DEBUG_SAVING_TYPE( char ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_INT_FORMAT, c); }
				
				void save(unsigned char c)
				{ DEBUG_SAVING_TYPE( unsigned char ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_UNSIGNED_INT_FORMAT, c); }
				
				void save(short s)
				{ DEBUG_SAVING_TYPE( short ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_INT_FORMAT, s); }
				
				void save(unsigned short s)
				{ DEBUG_SAVING_TYPE( unsigned short ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_UNSIGNED_INT_FORMAT, s); }
				
				void save(int i)
				{ DEBUG_SAVING_TYPE( int ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_INT_FORMAT, i); }
				
				void save(unsigned int i)
				{ DEBUG_SAVING_TYPE( unsigned int ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_UNSIGNED_INT_FORMAT, i); }
				
				void save(long l)
				{ DEBUG_SAVING_TYPE( long ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_LONG_FORMAT, l); }
				
				void save(unsigned long l)
				{ DEBUG_SAVING_TYPE( unsigned long ); _sa->savePrimitiveWholeNumber(SOCKET_ARCHIVE_UNSIGNED_LONG_FORMAT, l); }
				
				void save(float f)
				{ DEBUG_SAVING_TYPE( float ); _sa->savePrimitiveFloatingPoint(SOCKET_ARCHIVE_FLOAT_FORMAT, f); }
				
				void save(double d)
				{ DEBUG_SAVING_TYPE( double ); _sa->savePrimitiveFloatingPoint(SOCKET_ARCHIVE_DOUBLE_FORMAT, d); }
				
				void save(long double d)
				{ DEBUG_SAVING_TYPE( long double ); _sa->savePrimitiveFloatingPoint(SOCKET_ARCHIVE_DOUBLE_FORMAT, d); }
				
				void save(void* buf, std::size_t len)
				{
					DEBUG_SAVING_TYPE( void[] );
					save( len );
					_sa->save_binary( buf, len );
				}
				
				void save(boost::posix_time::ptime &ptime)
				{
					DEBUG_SAVING_TYPE(boost::posix_time::ptime);
					std::string timeString( boost::posix_time::to_iso_string(ptime) );
					save( timeString );
				}
				
				template <class CharT>
				void save(std::basic_string<CharT> &s)
				{
					DEBUG_SAVING_TYPE( std::basic_string<CharT> );
					std::size_t size = sizeof(CharT) * (s.size() + 1);
					// Save the length
					save(size);
					if (size > 0)
					{
						// Serialize the string as plain bytes, but only if size is greater than 0
						_sa->save_binary( s.c_str(), size ); 
					}
				}
				
				void save(std::string &s)
				{
					DEBUG_SAVING_TYPE( std::string );
					std::size_t size = sizeof(std::string::value_type) * (s.size() + 1);
					// Save the length
					save(size);
					if (size > 0) 
					{
						// Serialize the string, but only if size is greater than 0
						_sa->save_binary( s.c_str(), size );
					}
				}
				
				void save(UnicodeString &string)
				{
					DEBUG_SAVING_TYPE( UnicodeString );
					std::size_t size = sizeof(UChar) * (string.length() + 1);
					// Save the length
					save(size);
					if (size > 0)
					{
						// Serialize the string as plain bytes, but only if size is greater than 0
						_sa->save_binary( (const char*)const_cast<UnicodeString&>(string).getTerminatedBuffer(), size );
					}
				}
				
				void save( NoData& )
				{
				}
				
				void save( saml2::KeyData &keyData )
				{
					DEBUG_SAVING_TYPE( saml2::KeyData );
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
				/*@}*/
				
				/**
				 * Operators for serializing with a stream-like syntax
				 */
				/*@{*/
				template <class T>
				SocketArchiveOutput &operator &(T &t)
				{ save(t); return *this; }
				
				template <class T>
				SocketArchiveOutput &operator <<(T &t)
				{ save(t); return *this; }
				
				template <class T, class U>
				SocketArchiveOutput &operator ()(T *&t, U &u)
				{ save((void*&)t,(std::size_t&)u); return *this; }
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
			
			/**
			 * Loads binary data from the socket into a buffer.
			 * @param address The base address of the buffer
			 * @param size The number of char elements that will fit in the allocated space
			 */
			void load_binary(void *address, std::size_t size)
			{
				// Fill the socket buffer
				fill();
				
				// Zero all the memory in the buffer
				memset( address, 0, size );
				// Decide how many bytes need to be read.
				std::size_t len = platform::textEncoding::encodedSize( size );
				if (len > 0)
				{
					// Allocate a temporary buffer to read into.
					AutoArray<char> buf( len+1 );
					
					char n = DELIMITER_CHAR; // if we never enter the loop, don't skip the entry.
					for (std::size_t pos = 0; pos < len; pos++)
					{
						// Consume a byte..
						n = consume();
						if ( ISDELIMITER(n) ) 
						{
							// We have hit the end of the serialized data for this object. Finish.
							buf[pos] = '\0';
							break;
						}
						
						// ..and place it in the buffer.
						buf[pos] = n;
					}
					
					// Truncate the object and skip to the next one.
					while ( !ISDELIMITER(n) ) n = consume();
					
					buf[len] = '\0';
					// Decode the serialized data into the buffer
					platform::textEncoding::decode( (char*)address, buf.get(), size );
				}
			}
			
			// TODO Shouldn't need to overload for void* and const void*
			//void save_binary(void *address, std::size_t size)
			//{ save_binary( address, size ); }
			
			void save_binary(const void *address, std::size_t size)
			{
				if (size > 0)
				{
					// Allocate a buffer for the length of the encoded data
					std::size_t len = platform::textEncoding::encodedSize( size ) + 1;
					AutoArray<char> buf( len );
					
					// Encode into the buffer.
					platform::textEncoding::encode( buf.get(), (char*)address, len );
					
					// Append a delimiter character to the encoded data and write it to the socket.
					buf[len-1] = DELIMITER_CHAR;
					write( buf.get(), len );
				}
			}
			
			template<class T>
			void loadPrimitiveWholeNumber(T &t)
			{
				fill();

				t = 0;
				int multiplier = 1;
				
				// Consume first character, check if it's a negative sign.
				char n = consume();
				if ( n == '-' )
				{
					multiplier = -1;
					// Consume the next character after the '-'
					n = consume();
				}
				
				while (_size >= 0)
				{
					if( ISDELIMITER(n) ) 
					{
						// Finished reading the number.. multiply by -1 if needed and return  
						t = t * multiplier;
						return;
					}
					if( !isdigit(n) )
					{
						// Number data is invalid. Skip the object and return -1.
						t = -1;
						for( ; !ISDELIMITER(n); n=consume() );
						return;
					}
					
					// Digits are stored base 10 in big endian
					t = 10*t + (int)( n - '0' );
					n = consume();
				}
			}
			
			template<class T>
			void savePrimitiveWholeNumber(const char *format, T &t)
			{
				char outbuf[SOCKET_ARCHIVE_BUFFER_SIZE];
				
				// Write the number out to a buffer.
				int len = snprintf( outbuf, SOCKET_ARCHIVE_BUFFER_SIZE, format, t );
				
				// If the number was too big, truncate and append a delimiter
				if (len > SOCKET_ARCHIVE_BUFFER_SIZE)
				{
					len = SOCKET_ARCHIVE_BUFFER_SIZE;
					outbuf[len - 1] = DELIMITER_CHAR;
				}
				
				// Write the number data out.
				write( outbuf, len );
			}
			
			template<class T>
			void loadPrimitiveFloatingPoint(T &t)
			{
				// Fill the socket buffer
				fill();

				// A buffer for the floating point number.
				char fpbuf[SOCKET_ARCHIVE_BUFFER_SIZE];
				int fppos = 0;
				char n = consume();
				while (ISDELIMITER(n)) n = consume();
				// Make sure we don't go outside the buffer.
				while (_size > 0 && fppos < ( SOCKET_ARCHIVE_BUFFER_SIZE - 1 ))
				{
					if (ISDELIMITER(n)) break;
					// Store the character and load the next.
					fpbuf[fppos++] = n;
					n = consume();
				}
				
				// Skip to the next element if we aren't already there
				while (!ISDELIMITER(n)) n = consume();
				
				fpbuf[fppos] = '\0';
				
				t = (T)strtod(fpbuf, NULL);
			}
			
			template<class T>
			void savePrimitiveFloatingPoint(const char *format, T &t)
			{
				char outbuf[SOCKET_ARCHIVE_BUFFER_SIZE];
				
				// Print the number into the floating point buffer.
				int len = snprintf( outbuf, SOCKET_ARCHIVE_BUFFER_SIZE, format, std::numeric_limits<T>::digits10, t );
				
				// If the number was too big, truncate and append a delimiter.
				if (len > SOCKET_ARCHIVE_BUFFER_SIZE)
				{
					len = SOCKET_ARCHIVE_BUFFER_SIZE;
					outbuf[len - 1] = DELIMITER_CHAR;
				}
				
				write( outbuf, len );
			}
			
			private:
			
			void write( char *buf, std::size_t len )
			{
				if (_closed) throw SocketException("The socket for this archive was closed by a previous error");
				
				std::size_t pos = 0;
				// Until all the data has been written..
				while( pos < len ) 
				{
					std::size_t bytes = SOCKET_ARCHIVE_BUFFER_SIZE;
					if (bytes > (len - pos)) bytes = (len - pos);
					std::size_t written;
					try
					{
						// .. try to write it..
						written = platform::writeSocket( _socket, &buf[pos], bytes, 0 );
					}
					catch (SocketException e)
					{
						_closed = true;
					}

					// .. and update the position by how many bytes were written.
					pos += written;
				}
			}

#ifndef MIN
#define MIN(a,b) ( (a) < (b) ? (a) : (b) )
#endif /* MIN */

			void fill(bool block = false)
			{
				// Reposition the data to the beginning of the buffer.
				memmove(_buff, &_buff[_pos], _size);
				_pos = 0;
				
				// Determine the position and length to be written.
				char *writebuf = _buff + _size;
				std::size_t writelen = SOCKET_ARCHIVE_BUFFER_SIZE - _size;
				
				int flags = 0;
				// If non blocking, add the non block flag
				if (!block)
				{
					return;
					flags |= platform::flags::nonBlocking;
				}
				
				// Read into the buffer, and increase the size by the number of bytes read.
				std::size_t written = platform::readSocket( _socket, writebuf, writelen, 0 );
				_size += written;
			}
			
			inline char consume()
			{
				if (_closed) throw SocketException("The socket for this archive was closed by a previous error");
				if (_eof) throw SocketException("The socket for this archive reached end-of-file");
				
				// If we don't have a character, block until we do
				if (_size <= 0) fill(true);
				// If we still don't, it's EOF.
				if (_size <= 0)
				{
					_eof = true;
					return '\0';
				}
				
				// Consume a character from the buffer.
				--_size; return _buff[_pos++];
			}
			
			platform::socket_t _socket;
			char _buff[SOCKET_ARCHIVE_BUFFER_SIZE];
			std::size_t _pos, _size;
			SocketArchiveInput _in;
			SocketArchiveOutput _out;
			bool _closed, _eof;
		};
		
	}
}

#endif /*SOCKETARCHIVE_H_*/
