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
 * Creation Date: 03/06/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.processor.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.metadata.bean.EntityData;
import com.qut.middleware.metadata.exception.MetadataSourceException;
import com.qut.middleware.metadata.processor.DynamicMetadataUpdater;
import com.qut.middleware.metadata.processor.MetadataProcessor;
import com.qut.middleware.metadata.source.DynamicMetadataSource;

public class DynamicMetadataUpdaterImpl implements DynamicMetadataUpdater
{
	private List<DynamicMetadataSource> dynamicSources;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public DynamicMetadataUpdaterImpl(List<DynamicMetadataSource> dynamicSources)
	{
		this.dynamicSources = dynamicSources;
	}
	
	public EntityData dynamicUpdate(MetadataProcessor processor, String entityID)
	{
		for (DynamicMetadataSource source : this.dynamicSources)
		{
			try
			{
				EntityData data = source.updateDynamicMetadata(processor, entityID);
				if (data != null)
				{
					this.logger.info("Resolved entity from dynamic metadata source. Entity ID: " + entityID + "  Source: ");
					return data;
				}
			}
			catch (MetadataSourceException e)
			{
				this.logger.warn("Dynamic metadata source reported an error with entity ID: " + entityID + " .. Message was: " + e.getMessage());
				this.logger.debug("Dynamic metadata source reported an error with entity ID: " + entityID, e);
			}
		}
		
		return null;
	}
}
