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
package com.qut.middleware.esoemanager.manager.logic;

import java.util.List;

import com.qut.middleware.esoemanager.client.rpc.bean.KeyDetails;
import com.qut.middleware.esoemanager.exception.RetrieveServiceCryptoException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoCreationException;
import com.qut.middleware.esoemanager.exception.ServiceCryptoDeletionException;
import com.qut.middleware.esoemanager.manager.bean.KeyStoreBean;

public interface ServiceCrypto
{
	public List<KeyDetails> retrieveServiceKeys(String serviceID) throws RetrieveServiceCryptoException;
	
	public KeyStoreBean retireveKeystore(String serviceID, String keypairName)  throws RetrieveServiceCryptoException;
	
	public void createServiceKey(String serviceID) throws ServiceCryptoCreationException;
	
	public void deleteServiceKey(String serviceID, String keypairName) throws ServiceCryptoDeletionException;
}
