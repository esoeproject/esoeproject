/* Copyright 2007, Queensland University of Technology
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
 * Creation Date: Nov 1, 2007
 * 
 * Purpose: 
 */

#include "Cookies.h"

#include <boost/date_time/posix_time/posix_time.hpp>

namespace spep { namespace isapi {
Cookies::Cookies( spep::isapi::ISAPIRequest *isapiRequest )
{
	std::string cookieHeader( isapiRequest->getHeader( "Cookie" ) );
	for( std::size_t pos = 0; pos != std::string::npos; )
	{
		std::size_t start = cookieHeader.find_first_not_of( ' ', pos );
		std::size_t end = cookieHeader.find_first_of(';', start);
		
		if( start == std::string::npos )
		{
			break;
		}
		
		std::string current;
		if( end == std::string::npos )
		{
			current = cookieHeader.substr( start );
			pos = end;
		}
		else
		{
			current = cookieHeader.substr( start, end-start );
			pos = end+1;
		}
		
		std::size_t nameStart = current.find_first_not_of( ' ' );
		std::size_t nameEnd = current.find_first_of( " =", nameStart );
		if( nameEnd != std::string::npos )
		{
			std::string name( current.substr( nameStart, nameEnd-nameStart ) );
			std::size_t equals = current.find_first_of( '=', nameEnd );
			std::size_t valueStart = current.find_first_not_of( ' ', equals+1 );
			if( valueStart != std::string::npos )
			{
				std::size_t valueEnd = current.find_first_of( ' ', valueStart );
				if( valueEnd != std::string::npos ) valueEnd -= valueStart;

				std::string value( current.substr( valueStart, valueEnd ) );
				
				_values[name] = value;
			}
		}
	}
}

const std::string& Cookies::operator[]( const std::string& name )
{
	std::map<std::string,std::string>::iterator iter = _values.find(name);
	if( iter != _values.end() )
		return iter->second;
	
	static std::string noValue;
	
	return noValue;
}

//Thu, 01-Jan-1970 00:00:00 GMT
void Cookies::addCookie( ISAPIRequest *isapiRequest, const char *name, const char *value, const char *path, const char *domain, bool secureOnly, int expires )
{
	std::stringstream ss;
	
	ss << "Set-Cookie: ";
	
	if( name == NULL || value == NULL ) return;
	
	ss << name << "=" << value;
	
	if( path != NULL )
	{
		ss << "; path=" << path;
	}
	else
	{
		ss << "; path=/";
	}
	
	if( domain != NULL && expires > 0 )
	{
		ss << "; domain=" << domain;
	}
	
	if( secureOnly )
	{
		ss << "; secure";
	}
	
	if( expires > 0 )
	{
		boost::posix_time::ptime expiry = 
			boost::posix_time::second_clock::universal_time() 
			+ boost::posix_time::seconds( expires );
		
		boost::posix_time::time_facet *facet = new boost::posix_time::time_facet( COOKIES_EXPIRES_TIME_STRING_FORMAT );
		
		std::stringstream time;
		time.imbue( std::locale( time.getloc(), facet ) );
		
		time << expiry << std::ends;
		
		std::string expiryString( time.str() );
		ss << "; expires=" << expiryString;
	}
	
	ss << "\r\n";
	
	isapiRequest->setHeader( ss.str() );
}

}}
