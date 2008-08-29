/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 18/01/2008
 * 
 * Purpose: 
 */

#include "RequestParameters.h"

namespace spep { namespace isapi {

RequestParameters::RequestParameters( ISAPIRequest *request )
{
	std::string paramString;
	
	if( request->getRequestMethod().compare( "POST" ) == 0 )
	{
		DWORD contentLength = 128;
		spep::CArray<char> buf( contentLength );
		
		std::memset( buf.get(), 0, contentLength );
		request->readRequestDocument( buf, contentLength );
		
		paramString = std::string( buf.get(), contentLength );
	}
	else
	{
		paramString = request->getQueryString();
	}
	
	// Start at the beginning and keep going til we fall off the string..
	// Not exactly rocket science.
	for( std::size_t pos = 0; pos != std::string::npos; )
	{
		// Split before next param
		std::size_t end = paramString.find_first_of( '&', pos );
		
		std::string current;
		// If we've hit the end
		if( end == std::string::npos )
		{
			// Consume the rest of the string
			current = paramString.substr( pos );
			pos = end;
		}
		else
		{
			// otherwise grab the part that is this parameter
			current = paramString.substr( pos, end-pos );
			pos = end+1;
		}
		
		// Split at equals sign, if any.. otherwise ignore this parameter
		end = current.find_first_of( '=' );
		if( end != std::string::npos )
		{
			// Separate the values and store them
			std::string name( current.substr( 0, end ) );
			std::string value( current.substr( end+1 ) );
			
			request->urlDecode( name );
			request->urlDecode( value );
			
			_params[name] = value;
		}
	}
}

const std::string& RequestParameters::operator[]( const std::string& name )
{
	std::map<std::string,std::string>::iterator iter = _params.find( name );
	
	if( iter != _params.end() )
	{
		return iter->second;
	}
	
	static std::string empty;
	return empty;
}

}}