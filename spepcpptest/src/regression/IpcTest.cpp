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
 * Creation Date: 30/01/2007
 * 
 * Purpose: 
 */

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>

#include "regression/IpcTest.h"
#include "ipc/Platform.h"
#include "ipc/SocketArchive.h"
#include "ipc/MessageHeader.h"
#include "ipc/Engine.h"
#include "ipc/Socket.h"
#include "Util.h"

#include <iostream>
#include <unicode/unistr.h>
#include <unicode/ustring.h>

SUITE( IpcTest )
{
	
	//#define _exit(a) exit(a)
	static int port = 50000;
	
	TEST( IpcTest_testEncodeDecode1 )
	{
		char *str = "this is a sample string.";
		std::size_t len = spep::ipc::platform::textEncoding::encodedSize( strlen(str) );
		spep::AutoArray<char> buf( len );
		
		spep::ipc::platform::textEncoding::encode( buf.get(), str, len );
		
		std::size_t decodelen = strlen(str);
		
		spep::AutoArray<char> decodebuf( decodelen );
		
		spep::ipc::platform::textEncoding::decode( decodebuf.get(), buf.get(), decodelen );
		
		CHECK( strncmp(decodebuf.get(), str, decodelen) == 0 );
	}
	
	TEST( IpcTest_testEncodeDecode2 )
	{
		UnicodeString unistr( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
		std::size_t len = spep::ipc::platform::textEncoding::encodedSize( sizeof(UChar) * unistr.length() );
		spep::AutoArray<char> buf( len );
		
		const UChar *unibuf = unistr.getBuffer();
		spep::ipc::platform::textEncoding::encode( buf.get(), reinterpret_cast<const char*>(unibuf), len );
		
		std::size_t decodelen = sizeof(UChar) * unistr.length();
		
		spep::AutoArray<UChar> decodebuf( decodelen );
		
		spep::ipc::platform::textEncoding::decode( reinterpret_cast<char*>(decodebuf.get()), buf.get(), decodelen );
		
		UnicodeString decodedstring( decodebuf.get(), unistr.length() );
		
		CHECK( decodedstring.compare(unistr) == 0 );
	}
	
	TEST( IpcTest_testSocket )
	{
		static const char* string = "this is a test message.";
		int testport = port++;
	
		spep::ipc::platform::socket_t sock;
		try
		{
			sock = spep::ipc::platform::openSocket();
			spep::ipc::platform::bindLoopbackSocket(sock, testport);
			spep::ipc::platform::listenSocket(sock);
		}
		catch( spep::ipc::SocketException e )
		{
			CHECK( false );
		}
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			char buf[1024] = {0};
			spep::ipc::platform::socket_t clientSock;
			try
			{
				clientSock = spep::ipc::platform::acceptSocket(sock);
				spep::ipc::platform::readSocket( clientSock, buf, strlen(string), 0 );
				CHECK( strncmp( buf, string, strlen(string) ) == 0 );
	
				spep::ipc::platform::closeSocket(sock);
				spep::ipc::platform::closeSocket(clientSock);
			}
			catch( spep::ipc::SocketException e )
			{
				CHECK( false );
			}
			waitpid(pid,NULL,0);
		}
		else
		{
			
			try
			{
				spep::ipc::platform::closeSocket(sock);
				
				spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
				spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
	
				CHECK( spep::ipc::platform::writeSocket( clientSock, string, strlen(string), 0 ) >= (int)strlen(string) );
				spep::ipc::platform::closeSocket(clientSock);
			}
			catch( spep::ipc::SocketException e )
			{
				CHECK( false );
			}
			
			_exit(0);
		}
	}
	
	TEST( IpcTest_testSocket2Way )
	{
		static const char* string = "this is a test message.";
	
		int testport = port++;
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			char buf[1024] = {0};
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
	
			CHECK( spep::ipc::platform::writeSocket( clientSock, string, strlen(string), 0 ) >= (int)strlen(string) );
	
			waitpid(pid,NULL,0);
			
			spep::ipc::platform::readSocket( clientSock, buf, strlen(string), 0 );
			CHECK( strncmp( buf, string, strlen(string) ) == 0 );
	
			spep::ipc::platform::closeSocket(sock);
			spep::ipc::platform::closeSocket(clientSock);
		}
		else
		{
			char buf[1024] = {0};
	
			spep::ipc::platform::closeSocket(sock);
			
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			spep::ipc::platform::readSocket( clientSock, buf, strlen(string), 0 );
			CHECK( strncmp( buf, string, strlen(string) ) == 0 );
			
			spep::ipc::platform::writeSocket( clientSock, buf, strlen(buf), 0 );
	
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	class Serializable
	{
		
		public:
		Serializable(){}
		Serializable(int val1, int val2, long val3, double val4, std::string val5, UnicodeString val6) :
		_val1(val1), _val2(val2), _val3(val3), _val4(val4), _val5(val5), _val6(val6) {}
		
		bool operator==( Serializable &s )
		{ return (_val1 == s._val1) 
				&& (_val2 == s._val2) 
				&& (_val3 == s._val3) 
				&& (_val4 == s._val4) 
				&& (_val5 == s._val5) 
				&& (_val6 == s._val6); }
		
		template <class Archive>
		void serialize(Archive &a, const unsigned int version)
		{ a & _val1 & _val2 & _val3 & _val4 & _val5 & _val6; }
	
		int _val1, _val2;
		long _val3;
		double _val4;
		std::string _val5;
		UnicodeString _val6;
	};
	
	class LongSerializable
	{
		public:
		LongSerializable(){}
		LongSerializable(UnicodeString val1, UnicodeString val2, UnicodeString val3, UnicodeString val4, UnicodeString val5, UnicodeString val6) :
		_val1(val1), _val2(val2), _val3(val3), _val4(val4), _val5(val5), _val6(val6) {}
		
		bool operator==( LongSerializable &s )
		{ return (_val1 == s._val1) 
				&& (_val2 == s._val2) 
				&& (_val3 == s._val3) 
				&& (_val4 == s._val4) 
				&& (_val5 == s._val5) 
				&& (_val6 == s._val6); }
		
		template <class Archive>
		void serialize(Archive &a, const unsigned int version)
		{ a & _val1 & _val2 & _val3 & _val4 & _val5 & _val6; }
	
		UnicodeString _val1;
		UnicodeString _val2;
		UnicodeString _val3;
		UnicodeString _val4;
		UnicodeString _val5;
		UnicodeString _val6;
	};
	
	
	TEST( IpcTest_testSocketArchiveIn )
	{
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("12345678123456781234567812345678123456781234567812345678123456789");
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a simple unicode string." ) );
		
		int testport = port++;
		char *string = "1 -45 12345678 5.01e-06 66 _313233343536373831323334353637383132333435363738313233343536373831323334353637383132333435363738313233343536373831323334353637383900 66 _5400680069007300200069007300200061002000730069006d0070006c006500200075006e00690063006f0064006500200073007400720069006e0067002e000000 ";
	
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			
			CHECK( spep::ipc::platform::validSocket( clientSock ) );
			
			spep::ipc::SocketArchive socketArchive( clientSock );
			
			Serializable s;
			
			socketArchive.in() >> s;
			
			CHECK( s._val1 == val1 );
			CHECK( s._val2 == val2 );
			CHECK( s._val3 == val3 );
			CHECK( (s._val4 - val4)/val4 < 1e-5 );
			CHECK( s._val5 == val5 );
			CHECK( s._val6 == val6 );
			
			spep::ipc::platform::closeSocket(sock);
			spep::ipc::platform::closeSocket(clientSock);
			
			waitpid(pid,NULL,0);
		}
		else
		{
			spep::ipc::platform::closeSocket(sock);
			
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			CHECK( spep::ipc::platform::writeSocket( clientSock, string, strlen(string), 0 ) >= (int)strlen(string) );
			
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	TEST( IpcTest_testSocketArchiveOut )
	{
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("12345678123456781234567812345678123456781234567812345678123456789");
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a simple unicode string." ) );
		
		int testport = port++;
		char *string = "1 -45 12345678 5.01e-06 66 _313233343536373831323334353637383132333435363738313233343536373831323334353637383132333435363738313233343536373831323334353637383900 66 _5400680069007300200069007300200061002000730069006d0070006c006500200075006e00690063006f0064006500200073007400720069006e0067002e000000 ";
	
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			char buf[10240] = {0};
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			
			waitpid(pid,NULL,0);
			
			spep::ipc::platform::readSocket( clientSock, buf, 10240, 0 );
			CHECK( strncmp( buf, string, strlen(string) ) == 0 );
			
			spep::ipc::platform::closeSocket(sock);
			spep::ipc::platform::closeSocket(clientSock);
		}
		else
		{
			spep::ipc::platform::closeSocket(sock);
			
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			Serializable s(val1, val2, val3, val4, val5, val6);
			spep::ipc::SocketArchive socketArchive( clientSock );
			socketArchive.out() << s;
			
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	TEST( IpcTest_testSocketArchive1 )
	{
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("12345678123456781234567812345678123456781234567812345678123456");
		//UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a simple unicode string." ) );
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
	
		int testport = port++;
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			
			CHECK( spep::ipc::platform::validSocket( clientSock ) );
			
			spep::ipc::SocketArchive socketArchive( clientSock );
			
			Serializable s;
			
			socketArchive.in() >> s;
			
			CHECK( s._val1 == 1 );
			CHECK( s._val2 == val2 );
			CHECK( s._val3 == val3 );
			CHECK( (s._val4 - val4)/val4 < 1e-5 );
			CHECK( s._val5 == val5 );
			CHECK( s._val6 == val6 );
			
			spep::ipc::platform::closeSocket(sock);
			spep::ipc::platform::closeSocket(clientSock);
			
			waitpid(pid,NULL,0);
		}
		else
		{
			spep::ipc::platform::closeSocket(sock);
			
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			Serializable s(val1, val2, val3, val4, val5, val6);
			spep::ipc::SocketArchive socketArchive( clientSock );
			socketArchive.out() << s;
			
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	
	TEST( IpcTest_testSocketArchive2 )
	{
		UnicodeString val1( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
		UnicodeString val2( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
		UnicodeString val3( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
		UnicodeString val4( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
		UnicodeString val5( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
	
		int testport = port++;
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			CHECK( spep::ipc::platform::validSocket( clientSock ) );
			
			spep::ipc::SocketArchive socketArchive( clientSock );
			
			LongSerializable s;
			
			socketArchive.in() >> s;
			
			CHECK( s._val1 == val1 );
			CHECK( s._val2 == val2 );
			CHECK( s._val3 == val3 );
			CHECK( s._val4 == val4 );
			CHECK( s._val5 == val5 );
			CHECK( s._val6 == val6 );
			
			spep::ipc::platform::closeSocket(sock);
			spep::ipc::platform::closeSocket(clientSock);
			
			waitpid(pid,NULL,0);
		}
		else
		{
			spep::ipc::platform::closeSocket(sock);
			
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			LongSerializable s(val1, val2, val3, val4, val5, val6);
			spep::ipc::SocketArchive socketArchive( clientSock );
			socketArchive.out() << s;
			
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	
	TEST( IpcTest_testSocketArchive2Way )
	{
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("12345678123456781234567812345678123456781234567812345678123456");
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full. This is a very long unicode string used for testing the handling of longer strings in the serialization step. If we overflow the buffer we should be able to see how it behaves when it gets too full." ) );
	
		int testport = port++;
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
	
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			CHECK( spep::ipc::platform::validSocket( clientSock ) );
			
			spep::ipc::SocketArchive socketArchive( clientSock );
			
			{
				Serializable tmp(val1,val2,val3,val4,val5,val6);
				socketArchive.out() << tmp;
			}
			
			waitpid(pid,NULL,0);
			
			Serializable s;
			
			socketArchive.in() >> s;
			
			//CHECK( s._val1 == val1 );
			//CHECK( s._val2 == val2 );
			//CHECK( s._val3 == val3 );
			//CHECK( s._val4 == val4 );
			//CHECK( s._val5 == val5 );
			//CHECK( s._val6 == val6 );
			
			spep::ipc::platform::closeSocket(sock);
			spep::ipc::platform::closeSocket(clientSock);
		}
		else
		{
			spep::ipc::platform::closeSocket(sock);
			
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			Serializable s;
			spep::ipc::SocketArchive socketArchive( clientSock );
			
			socketArchive.in() >> s;
			socketArchive.out() << s;
			
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	
	class Success {};
	class Failure {};
	
	template <class T>
	class MockDispatcher
	{
		
		spep::ipc::MessageHeader _header;
		T _t;
		
		public:
		
		MockDispatcher(spep::ipc::MessageHeader header, T t) :
		_header(header), _t(t) {}
		
		bool dispatch( spep::ipc::MessageHeader &header, spep::ipc::Engine &engine )
		{
			T t; engine.getObject(t);
			
			if (header.getDispatch() == _header.getDispatch() && _t == t)
			{ throw Success(); } // please don't hurt me... i can explain
			else
			{ throw Failure(); }
		}
		
		spep::ipc::MessageHeader header()
		{ return _header; }
		T object()
		{ return _t; }
		
	};
	
	TEST( IpcTest_testServerSocket )
	{
		
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("this is a test string.");
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a simple unicode string." ) );
	
		//spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		//spep::ipc::platform::bindSocket(sock, "127.0.0.1", 13828);
		//spep::ipc::platform::listenSocket(sock);
		
		int testport = port++;
		std::string dispatch("dispatch");
		spep::ipc::MessageHeader header( spep::ipc::SPEPIPC_REQUEST, dispatch );
		Serializable s(val1, val2, val3, val4, val5, val6);
		
		MockDispatcher<Serializable> dispatcher( header, s );
		spep::ipc::ServerSocket< MockDispatcher<Serializable> > serverSocket = spep::ipc::ServerSocket< MockDispatcher<Serializable> >( dispatcher, port++ );
		
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
		
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			
			CHECK( spep::ipc::platform::validSocket( clientSock ) );
			
			waitpid(pid, NULL, 0);
			
			bool caught = false;
			bool success = false;
			try
			{
				serverSocket.run( clientSock );
			}
			catch (Success s) // see above
			{
				caught = true;
				success = true;
			}
			catch (Failure f)
			{
				caught = true;
			}
			
			CHECK( caught );
			CHECK( success );
			
			spep::ipc::platform::closeSocket(clientSock);
			spep::ipc::platform::closeSocket(sock);
		}
		else
		{
			spep::ipc::platform::closeSocket(sock);
	
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::openSocket();
			spep::ipc::platform::connectLoopbackSocket( clientSock, testport );
			
			spep::ipc::SocketArchive socketArchive( clientSock );
			socketArchive.out() << header;
			
			socketArchive.out() << s;
			
			spep::ipc::platform::closeSocket(clientSock);
			
			_exit(0);
		}
	}
	
	
	TEST( IpcTest_testClientSocket1 )
	{
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("this is a test string.");
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a simple unicode string." ) );
		
		int testport = port++;
		std::string dispatch("dispatch");
		Serializable s(val1, val2, val3, val4, val5, val6);
		
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
		
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		if (pid > 0)
		{
			spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
			
			CHECK( spep::ipc::platform::validSocket( clientSock ) );
			
			waitpid(pid, NULL, 0);
			
			spep::ipc::MessageHeader header;
			Serializable object;
			
			spep::ipc::SocketArchive socketArchive(clientSock);
			
			socketArchive.in() >> header >> object;
			
			CHECK( header.getDispatch() == dispatch );
			CHECK( object == s );
			
			spep::ipc::platform::closeSocket(clientSock);
			spep::ipc::platform::closeSocket(sock);
		}
		else
		{
			spep::ipc::ClientSocket clientSocket( testport );
			
			clientSocket.makeNonBlockingRequest( dispatch, s );
			
			spep::ipc::platform::closeSocket(sock);
			_exit(0);
		}
	}
	
	
	TEST( IpcTest_testClientSocket2 )
	{
		int val1 = 1, val2 = -45;
		long val3 = 12345678L;
		double val4 = 5.01e-6;
		std::string val5("this is a test string.");
		UnicodeString val6( UNICODE_STRING_SIMPLE( "This is a simple unicode string." ) );
		
		int testport = port++;
		std::string dispatch("dispatch");
		Serializable s(val1, val2, val3, val4, val5, val6);
		
		spep::ipc::platform::socket_t sock = spep::ipc::platform::openSocket();
		spep::ipc::platform::bindLoopbackSocket(sock, testport);
		spep::ipc::platform::listenSocket(sock);
		
		pid_t pid = fork();
		
		CHECK( pid >= 0 );
		
		std::string replyObject("reply");
		
		if (pid > 0)
		{
			try
			{
					
				spep::ipc::platform::socket_t clientSock = spep::ipc::platform::acceptSocket(sock);
				
				CHECK( spep::ipc::platform::validSocket( clientSock ) );
				
				spep::ipc::MessageHeader header;
				Serializable object;
				
				spep::ipc::SocketArchive socketArchive(clientSock);
				
				socketArchive.in() >> header >> object;
				
				spep::ipc::MessageHeader replyHeader( spep::ipc::SPEPIPC_RESPONSE, "" );
				socketArchive.out() << replyHeader << replyObject;
				
				spep::ipc::platform::closeSocket(clientSock);
				spep::ipc::platform::closeSocket(sock);
				
				waitpid(pid, NULL, 0);
			}
			catch (spep::ipc::SocketException e)
			{
				CHECK(false);
			}
			catch (spep::ipc::InvalidArchiveStateException e)
			{
				CHECK(false);
			}
		}
		else
		{
			try
			{
				spep::ipc::ClientSocket clientSocket( testport );
				
				std::string reply;
				reply = clientSocket.makeRequest<std::string>( dispatch, s );
				
				CHECK( reply == replyObject );
				
				spep::ipc::platform::closeSocket(sock);
				_exit(0);
			}
			catch (spep::ipc::SocketException e)
			{
				CHECK(false);
			}
			catch (spep::ipc::InvalidArchiveStateException e)
			{
				CHECK(false);
			}
		}
	}

}
