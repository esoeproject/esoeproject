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
 * Creation Date: 12/01/2007
 * 
 * Purpose: Implements the reporting processor unit test.
 */

#include "regression/ReportingTest.h"

#include "reporting/ReportingLevels.h"
#include "reporting/ReportingProcessor.h"
#include "reporting/LocalReportingProcessor.h"
#include "reporting/Handler.h"

#define THROW_IF_FALSE(x) if(!(x)) throw std::exception();

SUITE( ReportingTest )
{
	
	
	class NullHandler : public spep::Handler
	{
		
		public:
		NullHandler(){}
		virtual ~NullHandler(){}
		virtual void log(const std::string&, const spep::Level, const std::string&){}
		
	};
	class TestHandler : public spep::Handler
	{
		public:
		std::string expectedName;
		spep::Level expectedLevel;
		std::string expectedMessage;
		bool *called;
		
		TestHandler(std::string name, spep::Level level, std::string message, bool *called)
		:
		expectedName(name),
		expectedLevel(level),
		expectedMessage(message)
		{
			this->called = called;
		}
		
		virtual ~TestHandler()
		{}
		
		virtual void log(const std::string &name, const spep::Level level, const std::string &message)
		{
			THROW_IF_FALSE( expectedName == name );
			THROW_IF_FALSE( expectedLevel == level );
			THROW_IF_FALSE( expectedMessage == message );
			*(this->called) = true;
		}
	};
	
	spep::ReportingProcessor *createTestProcessor( std::string expectedName, spep::Level level, std::string expectedMessage, bool *called )
	{
		spep::ReportingProcessor *reportingProcessor( new spep::ReportingProcessor() );
		TestHandler *handler( new TestHandler(expectedName, level, expectedMessage, called) );
		
		reportingProcessor->registerHandler(handler);
		
		return reportingProcessor;
	}
	
	TEST( ReportingTest_testReportingProcessor )
	{
		{
			std::auto_ptr<spep::ReportingProcessor> processor(new spep::ReportingProcessor());
			std::auto_ptr<NullHandler> handler(new NullHandler());
			
			processor->registerHandler(handler.get());
			bool found = false;
			const std::vector<spep::Handler*> *vec = processor->registeredHandlers();
			std::vector<spep::Handler*>::const_iterator iter = vec->begin();
			for (; iter != vec->end(); ++iter)
			{
				if (*iter == handler.get())
					found = true;
			}
			
			CHECK( found );
		}
		{
			std::string name("name.lol");
			spep::Level level = spep::DEBUG;
			std::string message("This is the message");
			bool called = false;
			
			std::auto_ptr<spep::ReportingProcessor> processor( createTestProcessor(name, level, message, &called) );
			
			/* cppunit assertions are in this call */
			processor->log(name, level, message);
			
			CHECK(called);
		}
		{
			std::string name("another.name");
			spep::Level level = spep::WARN;
			std::string message("This is another message");
			bool called = false;
			
			std::auto_ptr<spep::ReportingProcessor> processor( createTestProcessor(name, level, message, &called) );
			
			/* cppunit assertions are in this call */
			processor->log(name, level, message);
			
			CHECK(called);
		}
	}
	
	TEST( ReportingTest_testLocalReportingProcessor )
	{
		{
			std::string name("name.lol");
			spep::Level level = spep::DEBUG;
			std::string message("This is the message");
			bool called = false;
			
			std::auto_ptr<spep::ReportingProcessor> processor( createTestProcessor(name, level, message, &called) );
			
			spep::LocalReportingProcessor localProcessor( processor->localReportingProcessor(name) );
			
			localProcessor.log(level, message);
			
			CHECK(called);
		}
		{
			std::string name("another.name");
			spep::Level level = spep::WARN;
			std::string message("This is another message");
			bool called = false;
			
			std::auto_ptr<spep::ReportingProcessor> processor( createTestProcessor(name, level, message, &called) );
			
			spep::LocalReportingProcessor localProcessor( processor->localReportingProcessor(name) );
			
			localProcessor.log(level, message);
					
			CHECK(called);
		}
	}

}
