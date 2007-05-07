/* 
 * Copyright 2006, Queensland University of Technology
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
 * Author: Bradley Beddoes
 * Creation Date: 1/5/07
 * 
 * Purpose: Metadata Logic default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.bean.MetadataBean;
import com.qut.middleware.esoemanager.logic.MetadataLogic;
import com.qut.middleware.esoemanager.metadata.MetadataCache;

public class MetadataLogicImpl implements MetadataLogic
{
        private MetadataCache metadataCache;
        
        private Logger logger = Logger.getLogger(MetadataLogicImpl.class.getName());
        
        public MetadataLogicImpl(MetadataCache metadataCache)
        {
                if(metadataCache == null)
                {
                        this.logger.error("Null metadataCache specified on creation of MetadataLogicImpl");
                        throw new InvalidParameterException("metadataCache must not be null");
                }
                
                this.metadataCache = metadataCache;
        }
        
        /* (non-Javadoc)
         * @see com.qut.middleware.esoemanager.logic.MetadataLogic#loadMetadata(MetadataBean)
         * 
         * Metadata is loaded from the current in memory representation, this is updated by a thread which is spawned to handle database interaction
         */
        public String loadMetadata(MetadataBean bean)
        {
                return this.metadataCache.getCacheData();
        }
}