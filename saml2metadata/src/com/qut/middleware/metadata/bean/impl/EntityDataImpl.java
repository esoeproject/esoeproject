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
 * Creation Date: 09/04/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.bean.Role;

public class EntityDataImpl implements EntityData
{
	private String entityID;
	private String metadataSourceLocation;
	private int priority;
	private Set<Object> roleData;
	private Random random;
	private long expiryTime;
	private boolean trusted;

	public EntityDataImpl(String metadataSourceLocation, int priority)
	{
		this.roleData = Collections.synchronizedSet(new HashSet<Object>());
		this.metadataSourceLocation = metadataSourceLocation;
		this.priority = priority;
		this.expiryTime = 0;
		this.trusted = false;
	}

	public void setEntityID(String entityID)
	{
		this.entityID = entityID;
	}

	public String getEntityID()
	{
		return this.entityID;
	}
	
	public String getMetadataSourceLocation()
	{
		return this.metadataSourceLocation;
	}
	
	public int getPriority()
	{
		return this.priority;
	}

	public void addRoleData(Role data)
	{
		// Make sure we don't add a role of the *exact* same type. That would cause undefined behaviour.
		Object existing = this.getRoleData(data.getClass());
		if (existing != null && existing.getClass() == data.getClass())
		{
			throw new IllegalArgumentException("A role of the specified type has already been added.");
		}
		this.roleData.add(data);
	}

	public <T extends Role> T getRoleData(Class<T> t)
	{
		Class<?> clazz = null;
		Object found = null;
		for (Object o : this.roleData)
		{
			/* If t is assignable from o's type, that means
			 * o is either:
			 * - the type that we want; or
			 * - a subclass of that type
			 */
			if (t.isAssignableFrom(o.getClass()))
			{
				/* If we don't already have an object, assign this one.
				 */
				if (found == null)
				{
					clazz = o.getClass();
					found = o;
				}
				else
				{
					/* Otherwise, we check if we have a more
					 * specific class than the one already found.
					 */
					if (o.getClass().isAssignableFrom(clazz))
					{
						clazz = o.getClass();
						found = o;
					}
				}
			}
		}

		return found == null ? null : t.cast(found);
	}

	public void setRandom(Random random)
	{
		this.random = random;
	}
	
	public Random getRandom()
	{
		return this.random;
	}

	public long getExpiryTimeMillis()
	{
		return this.expiryTime;
	}

	public void setExpiryTimeMillis(long expiryTime)
	{
		this.expiryTime = expiryTime;
	}

	public boolean isTrusted()
	{
		return this.trusted;
	}
	
	public void setTrusted(boolean trusted)
	{
		this.trusted = trusted;
	}
}
