/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.metadata.logic;


public interface MetadataCache
{		
	/** Check the cache for data and return bool value.
	 * 
	 * @return Returns true if the cache contains any cache data, else false.
	 */
	public boolean hasData();
	
	/** Get the internal view of ESOE metadata including all custom extenstions used by SPEP
	 * 
	 */
	public byte[] getCompleteMD();
	
	/** Set the internal view of ESOE metadata including all custom extenstions used by SPEP
	 * 
	 */
	public void setCompleteMD(byte[] cachedata);
	
	/** Get the external view of ESOE metadata only includes SAML 2.0 compliant data
	 * 
	 */
	public byte[] getSamlMD();
	
	/** Set the external view of ESOE metadata only includes SAML 2.0 compliant data
	 * 
	 */
	public void setSamlMD(byte[] cachedata);
	
}
