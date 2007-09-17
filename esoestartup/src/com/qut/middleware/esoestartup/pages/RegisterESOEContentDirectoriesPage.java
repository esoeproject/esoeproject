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

import java.io.File;

import net.sf.click.control.Form;
import net.sf.click.control.Submit;

import com.qut.middleware.esoemanager.pages.BorderPage;
import com.qut.middleware.esoestartup.bean.ESOEBean;
import com.qut.middleware.esoestartup.pages.forms.impl.DataContentForm;

public class RegisterESOEContentDirectoriesPage extends BorderPage
{
	/* Data Repository form */
	public DataContentForm dataContentForm;
	
	private ESOEBean esoeBean;

	public RegisterESOEContentDirectoriesPage()
	{
		this.dataContentForm = new DataContentForm();
	}

	public void onInit()
	{
		String esoeData, esoemanagerData, spepData;
		
		this.esoeBean = (ESOEBean) this.retrieveSession(ESOEBean.class.getName());

		this.dataContentForm.init();

		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		this.dataContentForm.add(nextButton);
		this.dataContentForm.setButtonAlign(Form.ALIGN_RIGHT);

		esoeData = System.getProperty(PageConstants.ESOE_CONTENT_DATA);
		esoemanagerData = System.getProperty(PageConstants.ESOEMANAGER_CONTENT_DATA);
		spepData = System.getProperty(PageConstants.SPEP_CONTENT_DATA);

		if (esoeData != null)
			this.dataContentForm.getField(PageConstants.ESOE_CONTENT_DATA).setValue(esoeData);

		if (esoemanagerData != null)
			this.dataContentForm.getField(PageConstants.ESOEMANAGER_CONTENT_DATA).setValue(esoemanagerData);

		if (spepData != null)
			this.dataContentForm.getField(PageConstants.SPEP_CONTENT_DATA).setValue(spepData);
	}
	
	public void onGet()
	{
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return;
		}
	}

	public boolean nextClick()
	{
		String esoeData, esoemanagerData, spepData;
		String redirectPath;
		File tmp;
		
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return false;
		}

		if (this.dataContentForm.isValid())
		{
			esoeData = this.dataContentForm.getFieldValue(PageConstants.ESOE_CONTENT_DATA);
			esoemanagerData = this.dataContentForm.getFieldValue(PageConstants.ESOEMANAGER_CONTENT_DATA);
			spepData = this.dataContentForm.getFieldValue(PageConstants.SPEP_CONTENT_DATA);
			
			tmp = new File(esoeData);
			if(tmp == null || !tmp.canWrite())
			{
				this.dataContentForm.setError(PageConstants.ESOE_CONTENT_ERROR);
				return true;
			}
			
			tmp = new File(esoemanagerData);
			if(tmp == null || !tmp.canWrite())
			{
				this.dataContentForm.setError(PageConstants.ESOEMANAGER_CONTENT_ERROR);
				return true;
			}
			
			tmp = new File(spepData);
			if(tmp == null || !tmp.canWrite())
			{
				this.dataContentForm.setError(PageConstants.SPEP_CONTENT_ERROR);
				return true;
			}

			/* Directories are ready to go, store details in the session */
			this.esoeBean.setEsoeDataDirectory(esoeData);
			this.esoeBean.setEsoemanagerDataDirectory(esoemanagerData);
			this.esoeBean.setSpepDataDirectory(spepData);

			this.storeSession(PageConstants.STAGE1_RES, new Boolean(true));

			/* Move users to second stage, registration of ESOE service endpoints */
			redirectPath = getContext().getPagePath(RegisterESOEDatabasePage.class);
			setRedirect(redirectPath);

			return false;
		}
		return true;
	}
	
	public boolean previousClick()
	{
		String path = getContext().getPagePath(IndexPage.class);
		setRedirect(path);

		return false;
	}
}
