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

package com.qut.middleware.metadata.bean;

public interface EntityData
{
	/**
	 * @return The identifier of this entity, as given by the metadata source.
	 */
	public String getEntityID();

	/**
	 * @return The location that this Entity was created from.
	 */
	public String getMetadataSourceLocation();

	/**
	 * @return The priority given to this entity. This is used internally to
	 *         prevent entities from important metadata sources from being
	 *         overwritten when duplicate EntityID values exist across networks.
	 */
	public int getPriority();

	/**
	 * Returns role data corresponding to the presented class.
	 * 
	 * @param <T> Return type.
	 * @param t Class object to match against.
	 * @return The requested role data, or null if not found.
	 */
	public <T extends Role> T getRoleData(Class<T> t);

	/**
	 * Adds a piece of role data to the entity.
	 * 
	 * @param data The piece of role data to add.
	 * @throws IllegalArgumentException If a role with the given (exact match)
	 *         class has already been added.
	 */
	public void addRoleData(Role data);

	/**
	 * @return The expiry time of this entity, expressed in milliseconds since
	 *         the epoch, as defined by System.currentTimeMillis(). After this
	 *         time, the entry will not be cached any longer. If no value has
	 *         been assigned, the default value 0 will be returned.
	 */
	public long getExpiryTimeMillis();

	/**
	 * @param expiryTime The updated expiry time to give to this entity.
	 */
	public void setExpiryTimeMillis(long expiryTime);
	
	/**
	 * @return The boolean value indicating whether this entity was discovered
	 * 		   from a trusted metadata source, or not.
	 */
	public boolean isTrusted();
}
