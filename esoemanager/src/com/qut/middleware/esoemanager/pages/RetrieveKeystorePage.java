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
package com.qut.middleware.esoemanager.pages;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import net.sf.click.Page;
import net.sf.click.util.ClickUtils;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.bean.KeyStoreBean;
import com.qut.middleware.esoemanager.exception.RetrieveKeystoreException;
import com.qut.middleware.esoemanager.logic.RetrieveKeyStoreLogic;

public class RetrieveKeystorePage extends Page
{
	RetrieveKeyStoreLogic logic;

	public String id;
	public KeyStoreBean keyStoreBean;

	private ClickUtils clickUtils;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(RetrieveKeystorePage.class.getName());

	public void setRetrieveKeyStoreLogic(RetrieveKeyStoreLogic logic)
	{
		this.logic = logic;
		clickUtils = new ClickUtils();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.click.Page#getPath()
	 */
	@Override
	public String getPath()
	{
		/* We return null here to ensure we have exclusive rights to the response output stream */
		return null;
	}

	public void onGet()
	{
		if (id == null)
		{
			// TODO: Redirect client to error page
		}

		OutputStream out = null;
		try
		{
			this.keyStoreBean = this.logic.execute(id);
			this.getContext().getResponse().setContentType("application/octet-stream");
			this.getContext().getResponse().addHeader("Content-disposition", "attachment; filename=spepKeystore.ks"); //$NON-NLS-1$ //$NON-NLS-2$
			
			out = this.getContext().getResponse().getOutputStream();
			keyStoreBean.getKeyStore().store(out, keyStoreBean.getKeyStorePassphrase().toCharArray());
		}
		catch (RetrieveKeystoreException e)
		{
			this.logger.error("SPEPDAOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		catch (IOException e)
		{
			this.logger.error("IOException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		catch (KeyStoreException e)
		{
			this.logger.error("KeyStoreException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		catch (NoSuchAlgorithmException e)
		{
			this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		catch (CertificateException e)
		{
			this.logger.error("CertificateException thrown, " + e.getLocalizedMessage());
			this.logger.debug(e);
			// TODO: Redirect to error page
		}
		finally
		{

			if (out != null)
				this.clickUtils.close(out);
		}
	}

}
