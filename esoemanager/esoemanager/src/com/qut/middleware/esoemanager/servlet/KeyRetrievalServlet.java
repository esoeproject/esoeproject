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
package com.qut.middleware.esoemanager.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qut.middleware.esoemanager.exception.RetrieveServiceCryptoException;
import com.qut.middleware.esoemanager.manager.bean.KeyStoreBean;
import com.qut.middleware.esoemanager.manager.logic.ServiceCrypto;

public class KeyRetrievalServlet extends HttpServlet
{
	private static final long serialVersionUID = -809876850994287425L;

	private final String SERVICE_CRYPTO_IMPL = "serviceCrypto";
	private final String SERVICE_ID = "sid";
	private final String KEYPAIR_NAME = "n";

	private ServiceCrypto serviceCrypto;

	private Logger logger = LoggerFactory.getLogger(KeyRetrievalServlet.class);

	@Override
	public void init() throws ServletException
	{
		super.init();

		/* Spring integration to make our servlet aware of IoC */
		WebApplicationContext webAppContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this
				.getServletContext());

		this.serviceCrypto = (ServiceCrypto) webAppContext.getBean(SERVICE_CRYPTO_IMPL,
				com.qut.middleware.esoemanager.manager.logic.ServiceCrypto.class);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		OutputStream out = null;
		String sidValue = req.getParameter(this.SERVICE_ID);
		String nameValue = req.getParameter(this.KEYPAIR_NAME);

		try
		{
			KeyStoreBean keystoreBean = this.serviceCrypto.retrieveKeystore(sidValue, nameValue);
			
			resp.setContentType("application/octet-stream");
			resp.addHeader("Content-disposition", "attachment; filename=spepKeystore.ks"); //$NON-NLS-1$ //$NON-NLS-2$

			out = resp.getOutputStream();
			keystoreBean.getKeyStore().store(out, keystoreBean.getPassphrase().toCharArray());
		}
		catch (IOException e)
        {
             this.logger.error("IOException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
        }
        catch (KeyStoreException e)
        {
             this.logger.error("KeyStoreException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
        }
        catch (NoSuchAlgorithmException e)
        {
             this.logger.error("NoSuchAlgorithmException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
        }
        catch (CertificateException e)
        {
             this.logger.error("CertificateException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
        }
		catch (RetrieveServiceCryptoException e)
		{
			 this.logger.error("RetrieveServiceCryptoException thrown, " + e.getLocalizedMessage());
             this.logger.debug(e.toString());
		}


	}
}
