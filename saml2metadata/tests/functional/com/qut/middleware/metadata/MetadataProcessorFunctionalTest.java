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
 * Creation Date: 15/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.KeyEntry;
import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.cache.impl.MetadataCacheImpl;
import com.qut.middleware.metadata.exception.InvalidMetadataException;
import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.exception.MetadataStateException;
import com.qut.middleware.metadata.processor.DynamicMetadataUpdater;
import com.qut.middleware.metadata.processor.FormatHandler;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.processor.impl.MetadataProcessorImpl;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;


public class MetadataProcessorFunctionalTest
{
	private final String testFormat = "test";
	private List<Object> mocked = new ArrayList<Object>();
	private void startMock()
	{
		for(Object o : this.mocked)
			replay(o);
	}
	private void endMock()
	{
		for(Object o : this.mocked)
			verify(o);
		
		this.mocked.clear();
	}
	
	public String getEntityID(int seq)
	{
		return "http://entity" + Integer.toString(seq) + ".example.com/" + Integer.toString(seq);
	}
	
	@Test
	public void testMetadataProcessorNormal() throws Exception
	{
		DynamicMetadataUpdater dynamicMetadataUpdater = createMock(DynamicMetadataUpdater.class);
		this.mocked.add(dynamicMetadataUpdater);
		
		MetadataCache cache = new MetadataCacheImpl(dynamicMetadataUpdater);
		List<FormatHandler> handlers = new ArrayList<FormatHandler>();
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		
		expect(dynamicMetadataUpdater.dynamicUpdate((MetadataProcessor)notNull(), (String)notNull())).andReturn(null).anyTimes();
		
		TestHandler handler = new TestHandler();
		FormatHandler handler1 = createMock(FormatHandler.class);
		this.mocked.add(handler1);
		FormatHandler handler2 = createMock(FormatHandler.class);
		this.mocked.add(handler2);
		
		expect(handler1.canHandle((MetadataSource)notNull())).andReturn(false).anyTimes();
		expect(handler2.canHandle((MetadataSource)notNull())).andReturn(false).anyTimes();
		
		handlers.add(handler1);
		handlers.add(handler);
		handlers.add(handler2);
		
		String location1 = "http://md1.example.com/location";
		String location2 = "http://md2.example.com/location";
		String location3 = "http://md3.example.com/location";
		
		int priority1 = 1;
		int priority2 = 0;
		int priority3 = 2;
		
		TestSource source1 = new TestSource(this.testFormat, location1, priority1, true, true);
		sources.add(source1);
		TestSource source2 = new TestSource(this.testFormat, location2, priority2, false, true);
		sources.add(source2);
		TestSource source3 = new TestSource(this.testFormat, location3, priority3, false, true);
		sources.add(source3);
		
		// Scaling parameter for testing speed at high numbers of iterations.
		int scale = 20;
		
		List<EntityData> entities1 = new ArrayList<EntityData>();
		for (int i = 0; i<20*scale; ++i)
		{
			EntityDataImpl entityData = new EntityDataImpl(location1, priority1);
			entityData.setEntityID(getEntityID(i));
			entities1.add(entityData);
		}

		List<EntityData> entities2 = new ArrayList<EntityData>();
		for (int i = 18*scale; i<30*scale; ++i)
		{
			EntityDataImpl entityData = new EntityDataImpl(location2, priority2);
			entityData.setEntityID(getEntityID(i));
			entities2.add(entityData);
		}

		List<EntityData> entities3 = new ArrayList<EntityData>();
		for (int i = 15*scale; i<24*scale; ++i)
		{
			EntityDataImpl entityData = new EntityDataImpl(location3, priority3);
			entityData.setEntityID(getEntityID(i));
			entities3.add(entityData);
		}
		
		startMock();

		MetadataProcessor processor = new MetadataProcessorImpl(cache, handlers, sources);
		
		// Update, there will be no entities.
		processor.update();
		
		for (int i = 0; i < 30*scale; ++i)
		{
			EntityData entityData = null;
			try
			{
			entityData = processor.getEntityData(getEntityID(i));
			}
			catch (MetadataStateException e)
			{//Ignore. entityData will still be null
			}
			assertNull(entityData);
		}

		handler.setEntityList(location1, entities1);
		source1.markUpdated();
		
		// Update, will bring in entities from source 1.
		processor.update();
		for (int i = 0; i < 20*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location1, entityData.getMetadataSourceLocation());
		}
		for (int i = 20*scale; i < 30*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNull(entityData);
		}
		
		handler.setEntityList(location2, entities2);
		source2.markUpdated();
		
		// Update, will bring in entities from source 2. Will not overwrite source 1.
		processor.update();
		for (int i = 0; i < 20*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location1, entityData.getMetadataSourceLocation());
		}
		for (int i = 20*scale; i < 30*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location2, entityData.getMetadataSourceLocation());
		}

		handler.setEntityList(location3, entities3);
		source3.markUpdated();
		
		// Update, will bring in entities from source 3, overwriting some from 1 and 2.
		processor.update();
		for (int i = 0; i < 15*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location1, entityData.getMetadataSourceLocation());
		}
		for (int i = 15*scale; i < 24*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location3, entityData.getMetadataSourceLocation());
		}
		for (int i = 24*scale; i < 30*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location2, entityData.getMetadataSourceLocation());
		}
		
		source1.markUpdated();
		source2.markUpdated();
		
		// Update again with the same data from source 1 and 2 - should appear not to have changed.
		processor.update();
		for (int i = 0; i < 15*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location1, entityData.getMetadataSourceLocation());
		}
		for (int i = 15*scale; i < 24*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location3, entityData.getMetadataSourceLocation());
		}
		for (int i = 24*scale; i < 30*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location2, entityData.getMetadataSourceLocation());
		}
		
		handler.setEntityList(location1, null);
		source1.markUpdated();
		// Update, will remove all from metadata source 1.
		processor.update();
		
		for (int i = 0; i < 15*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNull(entityData);
		}
		for (int i = 15*scale; i < 24*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location3, entityData.getMetadataSourceLocation());
		}
		for (int i = 24*scale; i < 30*scale; ++i)
		{
			EntityData entityData = processor.getEntityData(getEntityID(i));
			assertNotNull(entityData);
			assertEquals(location2, entityData.getMetadataSourceLocation());
		}

		endMock();
	}
	
	
	class TestHandler implements FormatHandler
	{
		private Map<String, List<EntityData>> sourceEntityData = new TreeMap<String, List<EntityData>>();
		public boolean canHandle(MetadataSource source)
		{ return (source.getFormat().equals(MetadataProcessorFunctionalTest.this.testFormat)); }
		public void updateCache(MetadataSource source, MetadataCache cache, byte[] document)
		{
			List<EntityData> entities = this.sourceEntityData.get(source.getLocation());
			cache.update(source, entities, new ArrayList<KeyEntry>());
		}
		public void setEntityList(String sourceLocation, List<EntityData> entities)
		{ this.sourceEntityData.put(sourceLocation, entities); }
		public boolean canHandle(DynamicMetadataSource source)
		{
			return false;
		}
		public EntityData dynamicUpdateCache(DynamicMetadataSource source, MetadataCache cache, String entityID, byte[] document) throws InvalidMetadataException
		{
			return null;
		}
	}
	
	class TestSource implements MetadataSource
	{
		private String format;
		private String location;
		private int priority;
		private boolean mandatory;
		private boolean updated;
		private boolean trusted;
		
		public TestSource(String format, String location, int priority, boolean mandatory, boolean trusted)
		{
			this.format = format;
			this.location = location;
			this.priority = priority;
			this.mandatory = mandatory;
			this.trusted = trusted;
		}
		
		public String getFormat()
		{ return this.format; }
		public String getLocation()
		{ return this.location; }
		public int getPriority()
		{ return this.priority; }
		public boolean isMandatory()
		{ return this.mandatory; }
		public void markUpdated()
		{ this.updated = true; }
		public boolean isTrusted()
		{ return this.trusted; }
		public void updateMetadata(MetadataProcessor processor) throws MetadataSourceException
		{
			if (!this.updated) return;
			
			byte[] document = new byte[]{};
			try
			{
				processor.updateFromSource(this, document);
				this.updated = false;
			}
			catch (MetadataCacheUpdateException e)
			{
				fail(e.getMessage());
			}
		}
	}
}
