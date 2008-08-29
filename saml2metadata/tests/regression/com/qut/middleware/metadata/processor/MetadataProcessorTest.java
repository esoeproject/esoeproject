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

package com.qut.middleware.metadata.processor;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.IAnswer;
import org.junit.Test;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.cache.MetadataCache;
import com.qut.middleware.metadata.exception.MetadataCacheUpdateException;
import com.qut.middleware.metadata.processor.impl.MetadataProcessorImpl;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;

@SuppressWarnings("all")
public class MetadataProcessorTest
{
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
	
	@Test
	public void testUpdate() throws Exception
	{
		MetadataSource source1 = createMock(MetadataSource.class);
		this.mocked.add(source1);
		MetadataSource source2 = createMock(MetadataSource.class);
		this.mocked.add(source2);
		MetadataSource source3 = createMock(MetadataSource.class);
		this.mocked.add(source3);
		MetadataSource source4 = createMock(MetadataSource.class);
		this.mocked.add(source4);
		
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		
		List<FormatHandler> handlers = new ArrayList<FormatHandler>();
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		sources.add(source1);
		sources.add(source2);
		sources.add(source3);
		sources.add(source4);
		
		expect(source1.isMandatory()).andReturn(true).anyTimes();
		expect(source2.isMandatory()).andReturn(false).anyTimes();
		expect(source3.isMandatory()).andReturn(true).anyTimes();
		expect(source4.isMandatory()).andReturn(false).anyTimes();
		
		expect(source1.getLocation()).andReturn("http://example.com/location/1").anyTimes();
		expect(source2.getLocation()).andReturn("http://example.com/location/2").anyTimes();
		expect(source3.getLocation()).andReturn("http://example.com/location/3").anyTimes();
		expect(source4.getLocation()).andReturn("http://example.com/location/4").anyTimes();
		
		expect(source1.getFormat()).andReturn("x").anyTimes();
		expect(source2.getFormat()).andReturn("x").anyTimes();
		expect(source3.getFormat()).andReturn("x").anyTimes();
		expect(source4.getFormat()).andReturn("x").anyTimes();
		
		source1.updateMetadata((MetadataProcessor)notNull());
		expectLastCall().once();
		source2.updateMetadata((MetadataProcessor)notNull());
		expectLastCall().once();
		source3.updateMetadata((MetadataProcessor)notNull());
		expectLastCall().once();
		source4.updateMetadata((MetadataProcessor)notNull());
		expectLastCall().once();
		
		startMock();
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, handlers, sources);
		processor.update();
		
		endMock();
	}
	
	@Test
	public void testUpdateFromSource1() throws Exception
	{
		MetadataSource source = createMock(MetadataSource.class);
		this.mocked.add(source);
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		FormatHandler handlerA = createMock(FormatHandler.class);
		this.mocked.add(handlerA);
		FormatHandler handlerB = createMock(FormatHandler.class);
		this.mocked.add(handlerB);
		
		expect(source.getFormat()).andReturn("x").anyTimes();
		expect(source.getLocation()).andReturn("http://example.com/location").anyTimes();
		expect(source.isMandatory()).andReturn(true).anyTimes();
		expect(source.getPriority()).andReturn(0).anyTimes();
		
		expect(handlerA.canHandle(source)).andReturn(true).anyTimes();
		expect(handlerB.canHandle(source)).andReturn(false).anyTimes();
		handlerA.updateCache(eq(source), eq(cache), (byte[])notNull());
		expectLastCall().once();
		
		startMock();
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		sources.add(source);
		List<FormatHandler> handlers = new ArrayList<FormatHandler>();
		handlers.add(handlerB);
		handlers.add(handlerA);
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, handlers, sources);

		byte[] document = new byte[]{'a', 'b', 'c'};
		processor.updateFromSource(source, document);
		
		endMock();
	}
	
	@Test
	public void testUpdateFromSource2() throws Exception
	{
		MetadataSource source = createMock(MetadataSource.class);
		this.mocked.add(source);
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		FormatHandler handlerA = createMock(FormatHandler.class);
		this.mocked.add(handlerA);
		FormatHandler handlerB = createMock(FormatHandler.class);
		this.mocked.add(handlerB);
		
		expect(source.getFormat()).andReturn("x").anyTimes();
		expect(source.getLocation()).andReturn("http://example.com/location").anyTimes();
		expect(source.isMandatory()).andReturn(true).anyTimes();
		expect(source.getPriority()).andReturn(0).anyTimes();
		
		final AtomicInteger times = new AtomicInteger(0);
		IAnswer<Object> restrictToOnce = new IAnswer<Object>(){
			public Object answer() throws Throwable
			{
				if(times.compareAndSet(0, 1))
				{
					return null;
				}
				else
				{
					fail("Updated was handled more than once");
					// This is just to appease the compiler. Above line throws
					return null;
				}
			}
		};
		expect(handlerA.canHandle(source)).andReturn(true).anyTimes();
		expect(handlerB.canHandle(source)).andReturn(true).anyTimes();
		handlerA.updateCache(eq(source), eq(cache), (byte[])notNull());
		expectLastCall().andAnswer(restrictToOnce).anyTimes();
		handlerB.updateCache(eq(source), eq(cache), (byte[])notNull());
		expectLastCall().andAnswer(restrictToOnce).anyTimes();
		
		startMock();
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		sources.add(source);
		List<FormatHandler> handlers = new ArrayList<FormatHandler>();
		handlers.add(handlerB);
		handlers.add(handlerA);
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, handlers, sources);

		byte[] document = new byte[]{'a', 'b', 'c'};
		processor.updateFromSource(source, document);
		
		assertEquals("Number of times executed was incorrect", 1, times.get());
		
		endMock();
	}
	
	@Test(expected = MetadataCacheUpdateException.class)
	public void testUpdateFromSource3() throws Exception
	{
		MetadataSource source = createMock(MetadataSource.class);
		this.mocked.add(source);
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		FormatHandler handlerA = createMock(FormatHandler.class);
		this.mocked.add(handlerA);
		FormatHandler handlerB = createMock(FormatHandler.class);
		this.mocked.add(handlerB);
		
		expect(source.getFormat()).andReturn("x").anyTimes();
		expect(source.getLocation()).andReturn("http://example.com/location").anyTimes();
		expect(source.isMandatory()).andReturn(true).anyTimes();
		expect(source.getPriority()).andReturn(0).anyTimes();
		
		expect(handlerA.canHandle(source)).andReturn(false).anyTimes();
		expect(handlerB.canHandle(source)).andReturn(false).anyTimes();
		
		startMock();
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		sources.add(source);
		List<FormatHandler> handlers = new ArrayList<FormatHandler>();
		handlers.add(handlerB);
		handlers.add(handlerA);
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, handlers, sources);

		byte[] document = new byte[]{'a', 'b', 'c'};
		processor.updateFromSource(source, document);
		
		endMock();
	}
	
	@Test
	public void testUpdateFromDynamicSource1() throws Exception
	{
		String entityID = "http://entity.example.com/";
		byte[] document = new byte[]{'a', 'b', 'c'};
		
		DynamicMetadataSource source = createMock(DynamicMetadataSource.class);
		expect(source.getFormat()).andReturn("x").anyTimes();
		this.mocked.add(source);
		
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		
		FormatHandler handler = createMock(FormatHandler.class);
		expect(handler.canHandle(eq(source))).andAnswer(new IAnswer<Boolean>(){
			public Boolean answer() throws Throwable
			{
				Object[] args = getCurrentArguments();
				DynamicMetadataSource source = (DynamicMetadataSource)args[0];
				
				return (source.getFormat().equals("x"));
			}
		}).anyTimes();
		expect(handler.dynamicUpdateCache(eq(source), eq(cache), eq(entityID), eq(document))).andReturn(null).once();
		this.mocked.add(handler);
		
		List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
		formatHandlers.add(handler);
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, formatHandlers, sources);

		startMock();
		
		assertNull(processor.updateFromDynamicSource(source, entityID, document));
		
		endMock();
	}
	
	@Test
	public void testUpdateFromDynamicSource2() throws Exception
	{
		String entityID = "http://entity.example.com/";
		byte[] document = new byte[]{'a', 'b', 'c'};
		
		EntityData entity = createMock(EntityData.class);
		expect(entity.getEntityID()).andReturn(entityID).anyTimes();
		expect(entity.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity.getMetadataSourceLocation()).andReturn("(dynamic source)").anyTimes();
		expect(entity.getPriority()).andReturn(0).anyTimes();
		expect(entity.getRoleData((Class)notNull())).andReturn(null).anyTimes();
		
		DynamicMetadataSource source = createMock(DynamicMetadataSource.class);
		expect(source.getFormat()).andReturn("x").anyTimes();
		this.mocked.add(source);
		
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		
		FormatHandler handler = createMock(FormatHandler.class);
		expect(handler.canHandle(eq(source))).andAnswer(new IAnswer<Boolean>(){
			public Boolean answer() throws Throwable
			{
				Object[] args = getCurrentArguments();
				DynamicMetadataSource source = (DynamicMetadataSource)args[0];
				
				return (source.getFormat().equals("x"));
			}
		}).anyTimes();
		expect(handler.dynamicUpdateCache(eq(source), eq(cache), eq(entityID), eq(document))).andReturn(entity).once();
		this.mocked.add(handler);
		
		List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
		formatHandlers.add(handler);
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, formatHandlers, sources);

		startMock();
		
		assertNotNull(processor.updateFromDynamicSource(source, entityID, document));
		
		endMock();
	}
	
	@Test(expected = MetadataCacheUpdateException.class)
	public void testUpdateFromDynamicSource3() throws Exception
	{
		String entityID = "http://entity.example.com/";
		byte[] document = new byte[]{'a', 'b', 'c'};
		
		EntityData entity = createMock(EntityData.class);
		expect(entity.getEntityID()).andReturn(entityID).anyTimes();
		expect(entity.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity.getMetadataSourceLocation()).andReturn("(dynamic source)").anyTimes();
		expect(entity.getPriority()).andReturn(0).anyTimes();
		expect(entity.getRoleData((Class)notNull())).andReturn(null).anyTimes();
		
		DynamicMetadataSource source = createMock(DynamicMetadataSource.class);
		expect(source.getFormat()).andReturn("x").anyTimes();
		this.mocked.add(source);
		
		MetadataCache cache = createMock(MetadataCache.class);
		this.mocked.add(cache);
		
		FormatHandler handler = createMock(FormatHandler.class);
		expect(handler.canHandle(eq(source))).andReturn(false).anyTimes();
		expect(handler.dynamicUpdateCache(eq(source), eq(cache), eq(entityID), eq(document))).andReturn(entity).anyTimes();
		this.mocked.add(handler);
		
		List<FormatHandler> formatHandlers = new ArrayList<FormatHandler>();
		formatHandlers.add(handler);
		
		List<MetadataSource> sources = new ArrayList<MetadataSource>();
		
		MetadataProcessor processor = new MetadataProcessorImpl(cache, formatHandlers, sources);

		startMock();
		
		processor.updateFromDynamicSource(source, entityID, document);
		
		endMock();
	}
}
