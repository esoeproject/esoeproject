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

public class RegisterServiceSPEPPageAjax extends RegisterServiceSPEPPage
{
	public String assertionConsumerService;
	public String singleLogoutService;
	public String cacheClearService;

	public String endType;
	
	@Override
	public String getContentType()
	{
		return "text/xml";
	}
	
	public String getTemplate()
	{
		return "/ajax/ajax-template.htm";
	}

	@Override
	public void onGet()
	{
		if (this.endType != null)
		{
			if (this.endType.equals(PageConstants.SERVICE_TYPE_JAVA))
			{
				this.assertionConsumerService = this.defaultAssertionConsumerService.get(0);
				this.singleLogoutService = this.defaultSingleLogoutService.get(0);
				this.cacheClearService = this.defaultCacheClearService.get(0);
			}
			if (this.endType.equals(PageConstants.SERVICE_TYPE_APACHE))
			{
				this.assertionConsumerService = this.defaultAssertionConsumerService.get(1);
				this.singleLogoutService = this.defaultSingleLogoutService.get(1);
				this.cacheClearService = this.defaultCacheClearService.get(1);
			}
			if (this.endType.equals(PageConstants.SERVICE_TYPE_IIS))
			{
				this.assertionConsumerService = this.defaultAssertionConsumerService.get(2);
				this.singleLogoutService = this.defaultSingleLogoutService.get(2);
				this.cacheClearService = this.defaultCacheClearService.get(2);
			}
		}
	}
}
