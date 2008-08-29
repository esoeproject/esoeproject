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
 * Creation Date: 14/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.source;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.qut.middleware.metadata.constants.FormatConstants;
import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.impl.MetadataSourceBase;

public class MetadataSourceBaseTest
{
	private final String sourceLocation = "http://example.com/metadata/location";
	
	@Test
	public void testDefaultValues1()
	{
		// Test default constructor (through test impl class)
		byte[] buf = new byte[]{};
		MetadataSourceBaseTestImpl source = new MetadataSourceBaseTestImpl(buf);
		
		assertEquals(MetadataSource.DEFAULT_PRIORITY, source.getPriority());
		assertEquals(MetadataSource.DEFAULT_MANDATORY, source.isMandatory());
	}
	
	@Test
	public void testDefaultValues2()
	{
		// Test non-default constructor
		MetadataSourceBase source = new MetadataSourceBase("SHA1"){
			public String getFormat()
			{
				return null;
			}
			public String getLocation()
			{
				return null;
			}
			public void updateMetadata(MetadataProcessor processor) throws MetadataSourceException
			{
				// Nothing required.
			}
		};
		
		assertEquals(MetadataSource.DEFAULT_PRIORITY, source.getPriority());
		assertEquals(MetadataSource.DEFAULT_MANDATORY, source.isMandatory());
		assertEquals(MetadataSource.DEFAULT_TRUSTED, source.isTrusted());
	}
	
	@Test
	public void testReadMetadata1() throws Exception
	{
		byte[] buf = new byte[]{'a','b','c','d'};
		
		MetadataSourceBaseTestImpl source = new MetadataSourceBaseTestImpl(buf);
		
		MetadataProcessor processor = createMock(MetadataProcessor.class);
		
		processor.updateFromSource(eq(source), (byte[])notNull());
		expectLastCall().once();
		
		replay(processor);
		
		// Test - update metadata causes an update.
		source.updateMetadata(processor);
		
		verify(processor);
	}
	
	@Test
	public void testReadMetadata2() throws Exception
	{
		byte[] buf = new byte[]{'a','b','c','d'};
		
		MetadataSourceBaseTestImpl source = new MetadataSourceBaseTestImpl(buf);
		
		MetadataProcessor processor = createMock(MetadataProcessor.class);
		
		processor.updateFromSource(eq(source), (byte[])notNull());
		expectLastCall().once();
		
		replay(processor);
		
		// Test - update metadata multiple times (with same data) causes only one update.
		source.updateMetadata(processor);
		source.updateMetadata(processor);
		source.updateMetadata(processor);
		
		verify(processor);
	}
	
	@Test
	public void testReadMetadata3() throws Exception
	{
		byte[] buf = new byte[]{'a','b','c','d'};
		byte[] buf2 = new byte[]{'a','b','c','d','e','f','g'};
		
		MetadataSourceBaseTestImpl source = new MetadataSourceBaseTestImpl(buf);
		
		MetadataProcessor processor = createMock(MetadataProcessor.class);
		
		processor.updateFromSource(eq(source), (byte[])notNull());
		expectLastCall().times(2);
		
		replay(processor);
		
		// Test - update metadata multiple times (with different data) causes another update.
		source.updateMetadata(processor);
		
		source.setDoc(buf2);
		
		source.updateMetadata(processor);
		source.updateMetadata(processor);
		
		verify(processor);
	}
	
	
	
	
	
	/**
	 * Used to test the behaviour of the super class
	 */
	class MetadataSourceBaseTestImpl extends MetadataSourceBase
	{
		private byte[] doc;

		public MetadataSourceBaseTestImpl(byte[] doc)
		{
			this.doc = doc;
		}
		
		public void setDoc(byte[] doc)
		{
			this.doc = doc;
		}
		
		public String getFormat()
		{
			return FormatConstants.SAML2;
		}

		public String getLocation()
		{
			return MetadataSourceBaseTest.this.sourceLocation;
		}

		public void updateMetadata(MetadataProcessor processor) throws MetadataSourceException
		{
			try
			{
				InputStream input = new ByteArrayInputStream(this.doc);
				this.readMetadata(input, processor);
			}
			catch(IOException e)
			{
				throw new MetadataSourceException(e);
			}
		}
	}
}
