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
 * Creation Date: 11/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.source;

import java.net.URL;

import static org.easymock.EasyMock.*;
import org.junit.Test;

import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.impl.URLMetadataSource;

public class URLMetadataSourceTest
{
	@Test(expected = MetadataSourceException.class)
	public void testInvalidSource() throws Exception
	{
		URL url = new URL("http://255.255.255.256/invalid");
		URLMetadataSource source = new URLMetadataSource(url){
			public String getFormat()
			{
				return "X";
			}
		};
		
		MetadataProcessor processor = createMock(MetadataProcessor.class);
		replay(processor);
		
		source.updateMetadata(processor);
		
		verify(processor);
	}
}
