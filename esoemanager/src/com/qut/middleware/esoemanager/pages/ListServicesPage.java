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

import java.util.List;

import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.exception.RetrieveServiceListException;
import com.qut.middleware.esoemanager.logic.RetrieveServiceListLogic;

public class ListServicesPage extends BorderPage
{
	private RetrieveServiceListLogic logic;
	
	public List<ServiceBean> services;
	
	public void onGet()
	{
		try
		{
			this.services = this.logic.execute();
		}
		catch (RetrieveServiceListException e)
		{
			this.services = null;
		}
	}

	public RetrieveServiceListLogic getRetrieveServiceListLogic()
	{
		return this.logic;
	}

	public void setRetrieveServiceListLogic(RetrieveServiceListLogic logic)
	{
		this.logic = logic;
	}
}
