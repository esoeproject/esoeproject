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
 * Author: Shaun Mangelsdorf
 * Creation Date: 03/11/06
 * 
 * Purpose: Implements the SPEPRegistrationCache interface.
 */
package com.qut.middleware.esoe.spep.impl;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.spep.Messages;
import com.qut.middleware.esoe.spep.SPEPRegistrationCache;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.InvalidRequestException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.spep.sqlmap.SPEPRegistrationDao;
import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationData;
import com.qut.middleware.esoe.spep.sqlmap.impl.SPEPRegistrationQueryData;
import com.qut.middleware.saml2.schemas.assertion.NameIDType;
import com.qut.middleware.saml2.schemas.esoe.protocol.ValidateInitializationRequest;

/** Implements the SPEPRegistrationCache interface.*/
public class SPEPRegistrationCacheImpl implements SPEPRegistrationCache
{
	private SPEPRegistrationDao spepRegistrationDao;
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(SPEPRegistrationCacheImpl.class.getName());
	
	/**
	 * Default constructor.
	 * @param spepRegistrationDao The SPEP Registration DAO
	 */
	public SPEPRegistrationCacheImpl(SPEPRegistrationDao spepRegistrationDao)
	{
		if(spepRegistrationDao == null)
		{
			throw new IllegalArgumentException(Messages.getString("SPEPRegistrationCacheImpl.23")); //$NON-NLS-1$
		}
		
		this.spepRegistrationDao = spepRegistrationDao;
		
		this.logger.info(Messages.getString("SPEPRegistrationCacheImpl.7")); //$NON-NLS-1$
	}

	/** Register the SPEP details in the data source. The following fields are required in the given
	 * ValidateInitializationRequest.
	 * 
	 * issuer, ipAddressList, compileDate, compileSystem, version, environment, nodeID
	 * 
	 * 
	 */
	public void registerSPEP(ValidateInitializationRequest data) throws InvalidRequestException, DatabaseFailureNoSuchSPEPException, DatabaseFailureException, SPEPCacheUpdateException
	{
		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.8"), data.getID())); //$NON-NLS-1$
		
		// Check input to ensure valid data.
		NameIDType issuer = data.getIssuer();
		if(issuer == null)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.0")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.0")); //$NON-NLS-1$
		}
		String issuerID = issuer.getValue();
		if(issuerID == null || issuerID.length() == 0)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.1")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.1")); //$NON-NLS-1$
		}
		List<String> ipAddressList = data.getIpAddress();
		if(ipAddressList == null || ipAddressList.size() == 0)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.2")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.2")); //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		for(String address : ipAddressList)
		{
			buffer.append(address);
			buffer.append(" "); //$NON-NLS-1$
		}
		String ipAddress = new String(buffer.substring(0, buffer.length() - 1));
		
		String compileDate = data.getCompileDate();
		if(compileDate == null)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.3")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.3")); //$NON-NLS-1$
		}
		String compileSystem = data.getCompileSystem();
		if(compileSystem == null)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.4")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.4")); //$NON-NLS-1$
		}
		String environment = data.getEnvironment();
		if(environment == null)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.5")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.5")); //$NON-NLS-1$
		}
		String version = data.getSwVersion();
		if(version == null)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.6")); //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.6")); //$NON-NLS-1$
		}
	
		String nodeID = data.getNodeId();
		if(nodeID == null)
		{
			this.logger.error(Messages.getString("SPEPRegistrationCacheImpl.24"));  //$NON-NLS-1$
			throw new InvalidRequestException(Messages.getString("SPEPRegistrationCacheImpl.25")); //$NON-NLS-1$
		}
		
		// Retrieve database mapping for current entity
		Integer entID = this.spepRegistrationDao.getEntID(issuerID);

		// Build the query data
		SPEPRegistrationQueryData queryData = new SPEPRegistrationQueryData();
		queryData.setEntID(entID);

		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.11"), issuerID)); //$NON-NLS-1$
		
		// Get the SPEP registration data
		SPEPRegistrationData record = this.spepRegistrationDao.getSPEPRegistration(queryData);
		
		this.logger.info(Messages.getString("SPEPRegistrationCacheImpl.13") ); //$NON-NLS-1$
		
		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.26"), ipAddress, compileDate, compileSystem, environment, version, nodeID) ); //$NON-NLS-1$
				
		if(record == null)
		{
			this.logger.debug(Messages.getString("SPEPRegistrationCacheImpl.27")); //$NON-NLS-1$
			
			// Record does not exist, make a new one..
			Date date = new Date();
			record = new SPEPRegistrationData();
			record.setEntID(entID);
			record.setIpAddress(ipAddress);
			record.setCompileDate(compileDate);
			record.setCompileSystem(compileSystem);
			record.setEnvironment(environment);
			record.setVersion(version);
			record.setDateAdded(date);
			record.setDateUpdated(date);
			record.setNodeID(nodeID);
			
			// .. and insert it
			this.spepRegistrationDao.insertSPEPRegistration(record);
			
			this.logger.info(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.20"), issuerID)); //$NON-NLS-1$
		}
		else
		{
			this.logger.debug(Messages.getString("SPEPRegistrationCacheImpl.28"));  //$NON-NLS-1$
			
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.29"), record.getIpAddress(), record.getCompileDate(), record.getCompileSystem(), record.getEnvironment(), record.getVersion(), record.getNodeID()) ); //$NON-NLS-1$
			
			// Record exists. Check if we need to change anything
			if(		record.getIpAddress().equals(ipAddress)
				&&	record.getCompileDate().equals(compileDate)
				&&	record.getCompileSystem().equals(compileSystem)
				&&	record.getEnvironment().equals(environment)
				&&	record.getVersion().equals(version)
				&&	record.getNodeID().equals(nodeID))
			{
				this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.21"), issuerID)); //$NON-NLS-1$
				return;
			}
			
			this.logger.debug(Messages.getString("SPEPRegistrationCacheImpl.30")); //$NON-NLS-1$
			
			record.setEntID(entID);
			record.setIpAddress(ipAddress);
			record.setCompileDate(compileDate);
			record.setCompileSystem(compileSystem);
			record.setEnvironment(environment);
			record.setVersion(version);
			record.setDateAdded(new Date());
			record.setDateUpdated(new Date());
			record.setNodeID(nodeID);
			
			// Update as necessary
			this.spepRegistrationDao.updateSPEPRegistration(record);
			
			this.logger.info(MessageFormat.format(Messages.getString("SPEPRegistrationCacheImpl.22"), issuerID)); //$NON-NLS-1$
		}
	}
}
