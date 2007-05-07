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
 * Purpose: Loads metadata from repository
 */
package com.qut.middleware.esoemanager.logic;

import com.qut.middleware.esoemanager.bean.MetadataBean;

public interface MetadataLogic
{
        /*
         * Loads metadata from some backend source
         * @param MetadataBean Bean containing various data to support request for Metadata
         * @return String value of the metadata document or NULL when an error state is encountered
         */
        public String loadMetadata(MetadataBean bean);
}