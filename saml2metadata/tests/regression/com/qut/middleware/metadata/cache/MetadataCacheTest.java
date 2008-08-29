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
 * Creation Date: 21/05/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.cache;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.KeyEntry;
import com.qut.middleware.metadata.bean.Role;
import com.qut.middleware.metadata.bean.impl.EntityDataImpl;
import com.qut.middleware.metadata.cache.impl.MetadataCacheImpl;
import com.qut.middleware.metadata.processor.DynamicMetadataUpdater;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;
import com.qut.middleware.metadata.source.MetadataSource;

@SuppressWarnings("all")
public class MetadataCacheTest
{
	private class A implements Role { public A() {} }
	private class B implements Role { public B() {} }
	private class C implements Role { public C() {} }
	private String testMetadataFormat = "TestMetadata";
	private String testMetadataSourceName1 = "test://metadata/1";
	private String testMetadataSourceName2 = "test://metadata/2";
	private String testMetadataSourceName3 = "test://metadata/3";
	private int testMetadataSourcePriority1 = 0;
	private int testMetadataSourcePriority2 = 3;
	private int testMetadataSourcePriority3 = 5;
	private String entityID1 = "http://entity.example.com/1";
	private String entityID2 = "http://entity.example.com/2";
	private String entityID3 = "http://entity.example.com/3";
	private String entityID4 = "http://entity.example.com/4";
	private List<Object> mocked;
	private DynamicMetadataUpdater dynamicMetadataUpdater;
	private MetadataCache metadataCache;
	private MetadataSource source1;
	private MetadataSource source2;
	private MetadataSource source3;
	private long dynamicEntityExpiryInterval = 1;
	private DynamicMetadataSource dynamicSource1;
	private DynamicMetadataSource dynamicSource2;
	private DynamicMetadataSource dynamicSource3;
	private MetadataProcessor processor;
	private List<KeyEntry> keyList;
	
	@Before
	public void setup()
	{
		this.mocked = new ArrayList<Object>();
		
		this.dynamicMetadataUpdater = createMock(DynamicMetadataUpdater.class);
		expect(this.dynamicMetadataUpdater.dynamicUpdate((MetadataProcessor)notNull(), (String)notNull())).andReturn(null).anyTimes();
		this.mocked.add(this.dynamicMetadataUpdater);
		
		this.metadataCache = new MetadataCacheImpl(this.dynamicMetadataUpdater);
		
		this.source1 = createMock(MetadataSource.class);
		expect(this.source1.getFormat()).andReturn(this.testMetadataFormat).anyTimes();
		expect(this.source1.getLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(this.source1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		this.mocked.add(this.source1);
		
		this.source2 = createMock(MetadataSource.class);
		expect(this.source2.getFormat()).andReturn(this.testMetadataFormat).anyTimes();
		expect(this.source2.getLocation()).andReturn(this.testMetadataSourceName2).anyTimes();
		expect(this.source2.getPriority()).andReturn(this.testMetadataSourcePriority2).anyTimes();
		this.mocked.add(this.source2);
		
		this.source3 = createMock(MetadataSource.class);
		expect(this.source3.getFormat()).andReturn(this.testMetadataFormat).anyTimes();
		expect(this.source3.getLocation()).andReturn(this.testMetadataSourceName3).anyTimes();
		expect(this.source3.getPriority()).andReturn(this.testMetadataSourcePriority3).anyTimes();
		this.mocked.add(this.source3);
		
		this.dynamicSource1 = createMock(DynamicMetadataSource.class);
		expect(this.dynamicSource1.getFormat()).andReturn(this.testMetadataFormat).anyTimes();
		this.mocked.add(this.dynamicSource1);
		
		this.dynamicSource2 = createMock(DynamicMetadataSource.class);
		expect(this.dynamicSource2.getFormat()).andReturn(this.testMetadataFormat).anyTimes();
		this.mocked.add(this.dynamicSource2);
		
		this.dynamicSource3 = createMock(DynamicMetadataSource.class);
		expect(this.dynamicSource3.getFormat()).andReturn(this.testMetadataFormat).anyTimes();
		this.mocked.add(this.dynamicSource3);

		this.processor = createMock(MetadataProcessor.class);
		this.mocked.add(processor);
		
		this.keyList = new ArrayList<KeyEntry>();
	}
	
	public void startMock()
	{
		for (Object o : this.mocked)
		{
			replay(o);
		}
	}

	public void endMock()
	{
		for (Object o : this.mocked)
		{
			verify(o);
		}
	}

	@Test
	public void testUpdate1()
	{
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity1);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.update(source1, entities, this.keyList);
		
		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		endMock();
	}

	@Test
	public void testUpdate2()
	{
		// Same entity ID, different source / priority
		
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName2).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority2).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.update(source1, entities, this.keyList);

		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entities.clear();
		entities.add(entity2);
		
		this.metadataCache.update(source2, entities, this.keyList);

		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}

	@Test
	public void testUpdate3()
	{
		// Same entity ID, different source / priority
		
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName2).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority2).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity2);
		
		startMock();
		
		this.metadataCache.update(source2, entities, this.keyList);

		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		entities.clear();
		entities.add(entity1);
		
		this.metadataCache.update(source1, entities, this.keyList);

		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}

	@Test
	public void testUpdate4()
	{
		// Different entity ID, different source / priority
		
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID2).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName2).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority2).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity2);
		
		EntityData entity3 = createMock(EntityData.class);
		expect(entity3.getEntityID()).andReturn(this.entityID3).anyTimes();
		expect(entity3.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity3.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName3).anyTimes();
		expect(entity3.getPriority()).andReturn(this.testMetadataSourcePriority3).anyTimes();
		expect(entity3.getRoleData(eq(C.class))).andReturn(new C()).anyTimes();
		expect(entity3.getRoleData(not(eq(C.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity3);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.update(source1, entities, this.keyList);

		entities.clear();
		entities.add(entity2);
		
		this.metadataCache.update(source2, entities, this.keyList);

		entities.clear();
		entities.add(entity3);
		
		this.metadataCache.update(source3, entities, this.keyList);

		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entity = this.metadataCache.getEntityData(this.entityID2, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		entity = this.metadataCache.getEntityData(this.entityID3, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(C.class));
		
		endMock();
	}

	@Test
	public void testUpdate5()
	{
		// Same entity ID, same source / priority
		
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity2);
		
		startMock();
		
		this.metadataCache.update(source1, entities, this.keyList);

		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		entities.clear();
		entities.add(entity1);
		
		this.metadataCache.update(source1, entities, this.keyList);

		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		endMock();
	}

	@Test
	public void testUpdate6()
	{
		// Different entity ID, same source / priority
		
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID2).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.update(source1, entities, this.keyList);

		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entities.clear();
		entities.add(entity2);
		
		this.metadataCache.update(source1, entities, this.keyList);

		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNull(entity);
		
		entity = this.metadataCache.getEntityData(this.entityID2, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}
	
	@Test
	public void testDynamicUpdate1()
	{
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		entity1.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity1);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.setDynamicEntityExpiryInterval(this.dynamicEntityExpiryInterval);
		
		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		endMock();
	}
	
	@Test
	public void testDynamicUpdate2()
	{
		// Same entity ID, different source / priority
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		entity1.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName2).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority2).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		entity2.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.setDynamicEntityExpiryInterval(this.dynamicEntityExpiryInterval);
		
		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entities.clear();
		entities.add(entity2);

		this.metadataCache.dynamicUpdate(dynamicSource2, entities, this.keyList);
		
		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}
	
	@Test
	public void testDynamicUpdate3()
	{
		// Same entity ID, different source / priority
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		entity1.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().anyTimes();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName2).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority2).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		entity2.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity2);
		
		startMock();
		
		this.metadataCache.setDynamicEntityExpiryInterval(this.dynamicEntityExpiryInterval);
		
		this.metadataCache.dynamicUpdate(dynamicSource2, entities, this.keyList);
		
		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		entities.clear();
		entities.add(entity1);

		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}
	
	@Test
	public void testDynamicUpdate4()
	{
		// Same entity ID, same source / priority
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		entity1.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		entity2.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.setDynamicEntityExpiryInterval(this.dynamicEntityExpiryInterval);
		
		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entities.clear();
		entities.add(entity2);

		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}
	
	@Test
	public void testDynamicUpdate5()
	{
		// Different entity ID, same source / priority
		EntityData entity1 = createMock(EntityData.class);
		expect(entity1.getEntityID()).andReturn(this.entityID1).anyTimes();
		expect(entity1.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity1.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity1.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity1.getRoleData(eq(A.class))).andReturn(new A()).anyTimes();
		expect(entity1.getRoleData(not(eq(A.class)))).andReturn(null).anyTimes();
		entity1.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity1);
		
		EntityData entity2 = createMock(EntityData.class);
		expect(entity2.getEntityID()).andReturn(this.entityID2).anyTimes();
		expect(entity2.getExpiryTimeMillis()).andReturn(0L).anyTimes();
		expect(entity2.getMetadataSourceLocation()).andReturn(this.testMetadataSourceName1).anyTimes();
		expect(entity2.getPriority()).andReturn(this.testMetadataSourcePriority1).anyTimes();
		expect(entity2.getRoleData(eq(B.class))).andReturn(new B()).anyTimes();
		expect(entity2.getRoleData(not(eq(B.class)))).andReturn(null).anyTimes();
		entity2.setExpiryTimeMillis(geq(System.currentTimeMillis())); expectLastCall().atLeastOnce();
		this.mocked.add(entity2);
		
		List<EntityData> entities = new ArrayList<EntityData>();
		entities.add(entity1);
		
		startMock();
		
		this.metadataCache.setDynamicEntityExpiryInterval(this.dynamicEntityExpiryInterval);
		
		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		EntityData entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entities.clear();
		entities.add(entity2);

		this.metadataCache.dynamicUpdate(dynamicSource1, entities, this.keyList);
		
		entity = this.metadataCache.getEntityData(this.entityID1, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(A.class));
		
		entity = this.metadataCache.getEntityData(this.entityID2, this.processor);
		assertNotNull(entity);
		assertNotNull(entity.getRoleData(B.class));
		
		endMock();
	}
}
