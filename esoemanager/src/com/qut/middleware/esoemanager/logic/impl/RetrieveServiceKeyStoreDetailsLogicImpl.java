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
 * Purpose: Retrieve service keystore details logic default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.bean.KeyStoreDetailsBean;
import com.qut.middleware.esoemanager.bean.impl.KeyStoreDetailsBeanImpl;
import com.qut.middleware.esoemanager.exception.RetrieveServiceKeyStoreException;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.logic.RetrieveServiceKeyStoreDetailsLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;

public class RetrieveServiceKeyStoreDetailsLogicImpl implements RetrieveServiceKeyStoreDetailsLogic
{
	private SPEPDAO spepDAO;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(RetrieveServiceNodesLogicImpl.class.getName());

	public RetrieveServiceKeyStoreDetailsLogicImpl(SPEPDAO spepDAO)
	{
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for RetrieveServiceKeystoreLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for RetrieveServiceKeystoreLogicImpl was NULL");
		}

		this.spepDAO = spepDAO;
	}

	public KeyStoreDetailsBean execute(Integer descID) throws RetrieveServiceKeyStoreException
	{
		KeyStoreDetailsBean keyStoreDetails = new KeyStoreDetailsBeanImpl();
		
		List<Map<String, Object>> keyStoreDetailsMap;
		try
		{
			keyStoreDetailsMap = this.spepDAO.queryKeyStoreDetails(descID);
		}
		catch (SPEPDAOException e)
		{
			throw new RetrieveServiceKeyStoreException("Exception when attempting to retrieve keystore", e);
		}
		
		if(keyStoreDetailsMap == null)
		{
			this.logger.info("No keystore details available for " + descID.toString());
			throw new RetrieveServiceKeyStoreException("No keystore details available for " + descID);
		}
		
		/* If more then one keystore was present for this descriptorID the last one returned in the map will have its details returned */
		for(Map<String, Object> keyStoreDetail : keyStoreDetailsMap)
		{
			keyStoreDetails.setExpiryDate((Date)keyStoreDetail.get(Constants.FIELD_PKI_EXPIRY_DATE));
			keyStoreDetails.setKeyPairName((String)keyStoreDetail.get(Constants.FIELD_PKI_KEYPAIRNAME));
			keyStoreDetails.setKeyStorePassphrase((String)keyStoreDetail.get(Constants.FIELD_PKI_KEYSTORE_PASSPHRASE));
			keyStoreDetails.setKeyPairPassphrase((String)keyStoreDetail.get(Constants.FIELD_PKI_KEYPAIR_PASSPHRASE));
		}
		
		return keyStoreDetails;
	}
}
