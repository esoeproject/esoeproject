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
 */
package com.qut.middleware.esoestartup.pages;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.pages.BorderPage;
import com.qut.middleware.esoestartup.Constants;
import com.qut.middleware.esoestartup.exception.TestDatabaseException;
import com.qut.middleware.esoestartup.logic.TestDatabaseLogic;
import com.qut.middleware.esoestartup.pages.forms.impl.DataRepositoryForm;

public class RegisterESOEDatabasePage extends BorderPage
{
	public String headInclude = "ajax/ajax-registerdb-head.htm";
	public String bodyOnload = "registerAjax();";

	/* Tester for supplied database details */
	TestDatabaseLogic logic;

	/* Data Repository form */
	public DataRepositoryForm dataRepositoryForm;

	private String defaultMySQL;
	private String defaultOracle;

	public RegisterESOEDatabasePage()
	{
		this.dataRepositoryForm = new DataRepositoryForm();
	}

	public void onInit()
	{
		Constants.DatabaseDrivers driver;
		this.dataRepositoryForm.init();

		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		this.dataRepositoryForm.add(nextButton);
		this.dataRepositoryForm.setButtonAlign(Form.ALIGN_RIGHT);
		
		this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_DRIVER).setAttribute("onChange", "changeDatabase(this);");

		driver = (Constants.DatabaseDrivers) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_DRIVER);

		if (driver != null)
		{
			if (driver == Constants.DatabaseDrivers.mysql)
			{
				this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_DRIVER).setValue(
						PageConstants.DATA_REPOSITORY_DRIVER_MYSQL);
			}
			else
			{
				this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_DRIVER).setValue(
						PageConstants.DATA_REPOSITORY_DRIVER_ORACLE);
			}
			
			this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_URL).setValue(
					(String) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_URL));
			this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_USERNAME).setValue(
					(String) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_USERNAME));
			this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_PASSWORD).setValue(
					(String) this.retrieveSession(PageConstants.STORED_DATA_REPOSITORY_PASSWORD));
		}
		else
		{
			this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_DRIVER).setValue(PageConstants.DATA_REPOSITORY_DRIVER_MYSQL);
			this.dataRepositoryForm.getField(PageConstants.DATA_REPOSITORY_URL).setValue(defaultMySQL);
		}

		
	}

	public TestDatabaseLogic getTestDatabaseLogic()
	{
		return this.logic;
	}

	public void setTestDatabaseLogic(TestDatabaseLogic logic)
	{
		this.logic = logic;
	}

	public boolean nextClick()
	{
		Constants.DatabaseDrivers driver;
		String url, username, password, redirectPath;

		if (this.dataRepositoryForm.isValid())
		{
			if (this.dataRepositoryForm.getFieldValue(PageConstants.DATA_REPOSITORY_DRIVER).equals(
					PageConstants.DATA_REPOSITORY_DRIVER_MYSQL))
			{
				driver = Constants.DatabaseDrivers.mysql;
			}
			else
			{
				if (this.dataRepositoryForm.getFieldValue(PageConstants.DATA_REPOSITORY_DRIVER).equals(
						PageConstants.DATA_REPOSITORY_DRIVER_ORACLE))
				{
					driver = Constants.DatabaseDrivers.oracle;
				}
				else
				{
					this.dataRepositoryForm.setError(PageConstants.DATA_REPOSITORY_UNKNOWN_DRIVER);
					return true;
				}
			}

			try
			{
				url = this.dataRepositoryForm.getFieldValue(PageConstants.DATA_REPOSITORY_URL);
				username = this.dataRepositoryForm.getFieldValue(PageConstants.DATA_REPOSITORY_USERNAME);
				password = this.dataRepositoryForm.getFieldValue(PageConstants.DATA_REPOSITORY_PASSWORD);
				this.logic.testDatabase(driver, url, username, password);
			}
			catch (TestDatabaseException e)
			{
				/* If test logic throws an exception have the user re-enter details */
				this.dataRepositoryForm.setError(PageConstants.DATA_REPOSITORY_TEST_ERROR + e.getLocalizedMessage());
				return true;
			}

			/* Database is ready to go, store details in the session */
			this.storeSession(PageConstants.STORED_DATA_REPOSITORY_DRIVER, driver);
			this.storeSession(PageConstants.STORED_DATA_REPOSITORY_URL, url);
			this.storeSession(PageConstants.STORED_DATA_REPOSITORY_USERNAME, username);
			this.storeSession(PageConstants.STORED_DATA_REPOSITORY_PASSWORD, password);

			this.storeSession(PageConstants.STAGE1_RES, new Boolean(true));

			/* Move users to second stage, registration of ESOE service endpoints */
			redirectPath = getContext().getPagePath(RegisterESOEPage.class);
			setRedirect(redirectPath);

			return false;
		}
		return true;
	}

	public String getDefaultMySQL()
	{
		return defaultMySQL;
	}

	public void setDefaultMySQL(String defaultMySQL)
	{
		this.defaultMySQL = defaultMySQL;
	}

	public String getDefaultOracle()
	{
		return defaultOracle;
	}

	public void setDefaultOracle(String defaultOracle)
	{
		this.defaultOracle = defaultOracle;
	}
}
