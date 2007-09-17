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
import com.qut.middleware.esoestartup.bean.ESOEBean;
import com.qut.middleware.esoestartup.pages.forms.impl.CryptoForm;

public class RegisterESOECryptoPage extends BorderPage
{
	public CryptoForm cryptoForm;

	private ESOEBean esoeBean;

	public RegisterESOECryptoPage()
	{
		this.cryptoForm = new CryptoForm();
	}

	public void onInit()
	{
		this.esoeBean = (ESOEBean) this.retrieveSession(ESOEBean.class.getName());

		this.cryptoForm.init();

		Submit nextButton = new Submit(PageConstants.NAV_NEXT_LABEL, this, PageConstants.NAV_NEXT_FUNC);
		Submit backButton = new Submit(PageConstants.NAV_PREV_LABEL, this, PageConstants.NAV_PREV_FUNC);

		this.cryptoForm.add(backButton);
		this.cryptoForm.add(nextButton);
		this.cryptoForm.setButtonAlign(Form.ALIGN_RIGHT);

		if (esoeBean != null)
		{
			this.cryptoForm.getField(PageConstants.CRYPTO_ISSUER_DN).setValue(esoeBean.getCertIssuerDN());
			this.cryptoForm.getField(PageConstants.CRYPTO_ISSUER_EMAIL).setValue(esoeBean.getCertIssuerEmailAddress());
		}
	}

	@Override
	public void onGet()
	{
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return;
		}
		
		/* Check if previous registration stage completed */
		Boolean status = (Boolean) this.retrieveSession(PageConstants.STAGE7_RES);
		if (status == null || status.booleanValue() != true)
		{
			previousClick();
		}
	}

	public boolean nextClick()
	{
		/* Ensure session data is correctly available */
		if(this.esoeBean == null)
		{
			previousClick();
			return false;
		}
		
		if (this.cryptoForm.isValid())
		{
			String redirectPath;
			String issuerDN = this.cryptoForm.getFieldValue(PageConstants.CRYPTO_ISSUER_DN);
			String issuerEmail = this.cryptoForm.getFieldValue(PageConstants.CRYPTO_ISSUER_EMAIL);
			// TODO: Add check to ensure DN is actually a valid DN

			esoeBean.setCertIssuerDN(issuerDN);
			esoeBean.setCertIssuerEmailAddress(issuerEmail);

			this.storeSession(PageConstants.STAGE8_RES, new Boolean(true));

			/* Move users to final stage */
			redirectPath = getContext().getPagePath(RegisterESOEFinalizePage.class);
			setRedirect(redirectPath);

			return false;

		}

		return true;
	}

	public boolean previousClick()
	{
		/* Move client to register service page */
		String path = getContext().getPagePath(RegisterESOEManagerServiceNodesPage.class);
		setRedirect(path);

		return false;
	}
}
