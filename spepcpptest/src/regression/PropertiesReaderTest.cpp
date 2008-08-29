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
 * Creation Date: May 31, 2007
 * 
 * Purpose: 
 */

#if 0
#include "UnitTest++.h"

#include <iostream>
#include <fstream>

#include "PropertiesReader.h"

SUITE( PropertiesReaderTest )
{
	
	TEST( PropertiesReaderTest_testReadPropertiesFile )
	{
		
		std::cerr << "Properties reader test" << std::endl;
		// When run from the /Debug directory when built.
		spep::PropertiesReader propertiesReader( "../testdata/test.properties" );
		CHECK( std::string("value1").compare( propertiesReader.getProperty( "key1" ) ) == 0 );
		CHECK( std::string("value2").compare( propertiesReader.getProperty( "key2" ) ) == 0 );
		bool caught = false;
		try
		{
			propertiesReader.getProperty( "key3" );
		}
		catch( std::exception e )
		{
			caught = true;
		}
		CHECK( caught );
		
	}
	
}
#endif
