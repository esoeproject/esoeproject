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
 * Creation Date: 17/11/2006
 * 
 * Purpose: Creates and maintains an instance of SqlMapClient
 */
package com.qut.middleware.esoe.spep.sqlmap.impl;

import java.sql.SQLException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.qut.middleware.esoe.spep.exception.DatabaseFailureException;
import com.qut.middleware.esoe.spep.exception.DatabaseFailureNoSuchSPEPException;
import com.qut.middleware.esoe.spep.exception.SPEPCacheUpdateException;
import com.qut.middleware.esoe.spep.sqlmap.SPEPRegistrationDao;

/** Creates and maintains an instance of SqlMapClient. */
public class SPEPRegistrationDaoImpl extends SqlMapClientDaoSupport implements SPEPRegistrationDao
{
	private Logger logger = Logger.getLogger(SPEPRegistrationDaoImpl.class);
	
	public Integer getEntID(String entityID) throws DatabaseFailureNoSuchSPEPException
	{
		try
		{
			Integer result = (Integer) this.getSqlMapClient().queryForObject("getEntID", entityID);
			if (result != null)
			{
				return result;
			}
			else
			{
				throw new DatabaseFailureNoSuchSPEPException("No value for ENT_ID mapping for supplied entityID of " + entityID + " could be established");
			}

		}
		catch (SQLException e)
		{
			this.logger.error("SQLException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			throw new DatabaseFailureNoSuchSPEPException(e.getLocalizedMessage(), e);
		}
	}
	
	/** Query the underlying data source to see if an SPEP registration exists.
	 * 
	 * @param queryData The query data to be used to find the SPEP
	 * @return Integer number of SPEPs found. Should be 1 or 0 indicating success/failure
	 * @throws DatabaseFailureException
	 */
	public Integer querySPEPExists (SPEPRegistrationQueryData queryData) throws DatabaseFailureException
	{
		if (queryData == null || queryData.getEntID() == null)
		{
			String message = MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.0"), queryData.getEntID()); //$NON-NLS-1$
			this.logger.error(message);
			throw new DatabaseFailureException(message);
		}
		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.1"), queryData.getEntID())); //$NON-NLS-1$
		Integer spepExists = null;
		try
		{
			spepExists = (Integer)this.getSqlMapClient().queryForObject("spepConfirmExists", queryData); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.2"), queryData.getEntID(), spepExists)); //$NON-NLS-1$
		}
		catch (SQLException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.3"), queryData.getEntID(), e.getMessage())); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.4"), queryData.getEntID()), e); //$NON-NLS-1$
			throw new DatabaseFailureException(Messages.getString("SPEPRegistrationDaoImpl.5"), e); //$NON-NLS-1$
		}
		
		return spepExists;
	}

	/** Retrieve the data for an SPEP and map into returned data type.
	 * 
	 * @param queryData The query data to be used to find the SPEP registration data
	 * @return The SPEP registration data
	 * @throws DatabaseFailureException
	 */
	public SPEPRegistrationData getSPEPRegistration(SPEPRegistrationQueryData queryData) throws DatabaseFailureException
	{
		SPEPRegistrationData record;		
		
		if (queryData == null || queryData.getEntID() == null)
		{
			String message = MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.6"), queryData.getEntID()); //$NON-NLS-1$
			this.logger.error(message);
			throw new DatabaseFailureException(message);
		}
		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.7"), queryData.getEntID())); //$NON-NLS-1$
		try
		{
			record = (SPEPRegistrationData)this.getSqlMapClient().queryForObject("getSPEPRegistration", queryData); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.8"), queryData.getEntID())); //$NON-NLS-1$
		}
		catch (SQLException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.9"), queryData.getEntID(), e.getMessage())); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.10"), queryData.getEntID()), e); //$NON-NLS-1$
			throw new DatabaseFailureException(Messages.getString("SPEPRegistrationDaoImpl.11"), e); //$NON-NLS-1$
		}
		
		return record;
	}

	/** Insert an SPEP registration record.
	 * 
	 * @param record The record to insert in the database
	 * @throws SPEPCacheUpdateException
	 */
	public void insertSPEPRegistration(SPEPRegistrationData record) throws SPEPCacheUpdateException
	{
		if (record == null || record.getEntID() == null)
		{
			String message = MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.12"), record.getEntID()); //$NON-NLS-1$
			this.logger.error(message);
			throw new SPEPCacheUpdateException(message);
		}
		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.13"), record.getEntID())); //$NON-NLS-1$
		try
		{
			this.getSqlMapClient().insert("insertSPEPRegistration", record); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.14"), record.getEntID())); //$NON-NLS-1$
		}
		catch (SQLException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.15"), record.getEntID(), e.getMessage())); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.16"), record.getEntID()), e); //$NON-NLS-1$
			throw new SPEPCacheUpdateException(Messages.getString("SPEPRegistrationDaoImpl.17"), e); //$NON-NLS-1$
		}
	}

	/** Update an  exisiting SPEP registration record.
	 * 
	 * @param record The record to update in the database
	 * @throws SPEPCacheUpdateException
	 */
	public void updateSPEPRegistration(SPEPRegistrationData record) throws SPEPCacheUpdateException
	{
		if (record == null || record.getEntID() == null)
		{
			String message = MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.18"), record.getEntID()); //$NON-NLS-1$
			this.logger.error(message);
			throw new SPEPCacheUpdateException(message);
		}
		this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.19"), record.getEntID())); //$NON-NLS-1$
		try
		{
			this.getSqlMapClient().update("updateSPEPRegistration", record); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.20"), record.getEntID())); //$NON-NLS-1$
		}
		catch (SQLException e)
		{
			this.logger.error(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.21"), record.getEntID(), e.getMessage())); //$NON-NLS-1$
			this.logger.debug(MessageFormat.format(Messages.getString("SPEPRegistrationDaoImpl.22"), record.getEntID()), e); //$NON-NLS-1$
			throw new SPEPCacheUpdateException(Messages.getString("SPEPRegistrationDaoImpl.23"), e); //$NON-NLS-1$
		}
	}
}
