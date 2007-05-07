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
 * Purpose: View service logic default implementation
 */
package com.qut.middleware.esoemanager.logic.impl;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.qut.middleware.esoemanager.Constants;
import com.qut.middleware.esoemanager.bean.ContactPersonBean;
import com.qut.middleware.esoemanager.bean.ServiceBean;
import com.qut.middleware.esoemanager.bean.ServiceNodeBean;
import com.qut.middleware.esoemanager.bean.impl.ContactPersonBeanImpl;
import com.qut.middleware.esoemanager.bean.impl.ServiceBeanImpl;
import com.qut.middleware.esoemanager.bean.impl.ServiceNodeBeanImpl;
import com.qut.middleware.esoemanager.exception.SPEPDAOException;
import com.qut.middleware.esoemanager.exception.ViewServiceException;
import com.qut.middleware.esoemanager.logic.ViewServiceLogic;
import com.qut.middleware.esoemanager.spep.sqlmap.SPEPDAO;

public class ViewServiceLogicImpl implements ViewServiceLogic
{
	private SPEPDAO spepDAO;

	/* Local logging instance */
	private Logger logger = Logger.getLogger(ViewServiceLogicImpl.class.getName());

	public ViewServiceLogicImpl(SPEPDAO spepDAO)
	{
		if (spepDAO == null)
		{
			this.logger.error("spepDAO for RetrieveKeyStoreLogicImpl was NULL");
			throw new IllegalArgumentException("spepDAO for ViewServiceLogicImpl was NULL");
		}

		this.spepDAO = spepDAO;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.logic.impl.ViewServiceLogic#execute(java.lang.String)
	 */
	public ServiceBean execute(String entityID) throws ViewServiceException
	{
		ServiceBean bean = new ServiceBeanImpl();
		Vector<ContactPersonBean> contactPersons = new Vector<ContactPersonBean>();
		Vector<ServiceNodeBean> serviceNodes = new Vector<ServiceNodeBean>();

		try
		{
			/* Get the core system data for this service */
			List<Map<String, String>> serviceDetails = this.spepDAO.queryServiceDetails(entityID);
			for (Map<String, String> service : serviceDetails)
			{
				/* There should only be one, if there is multiple results the last one returned will be displayed */
				bean.setEntityID(service.get(Constants.FIELD_ENTITY_ID));
				bean.setActiveFlag(service.get(Constants.FIELD_ACTIVE_FLAG));
			}

			/* Get SAML descriptor for this service */
			List<Map<String, String>> serviceDescriptor = this.spepDAO.queryServiceDescriptor(entityID);
			for (Map<String, String> descriptor : serviceDescriptor)
			{
				bean.setDescriptorID(descriptor.get(Constants.FIELD_DESCRIPTOR_ID));
				bean.setDescriptorXML(descriptor.get(Constants.FIELD_DESCRIPTOR_XML));
			}

			/* Get contact data */
			List<Map<String, String>> contacts = this.spepDAO.queryServiceContacts(entityID);
			for (Map<String, String> contact : contacts)
			{
				ContactPersonBean contactPerson = new ContactPersonBeanImpl();
				contactPerson.setCompany(contact.get(Constants.FIELD_CONTACT_COMPANY));
				contactPerson.setContactID(contact.get(Constants.FIELD_CONTACT_ID));
				contactPerson.setContactType(contact.get(Constants.FIELD_CONTACT_TYPE));
				contactPerson.setGivenName(contact.get(Constants.FIELD_CONTACT_GIVEN_NAME));
				contactPerson.setSurName(contact.get(Constants.FIELD_CONTACT_SURNAME));
				contactPerson.setEmailAddress(contact.get(Constants.FIELD_CONTACT_EMAIL_ADDRESS));
				contactPerson.setTelephoneNumber(contact.get(Constants.FIELD_CONTACT_TELEPHONE_NUMBER));

				contactPersons.add(contactPerson);
			}
			bean.setContacts(contactPersons);

			/* Get descriptive detail about the service */
			List<Map<String, String>> serviceDescription = this.spepDAO.queryServiceDescription(entityID);
			for (Map<String, String> description : serviceDescription)
			{
				/* There should only be one, if there is multiple results the last one returned will be displayed */
				bean.setServiceName(description.get(Constants.FIELD_SERVICE_NAME));
				bean.setServiceURL(description.get(Constants.FIELD_SERVICE_URL));
				bean.setServiceDescription(description.get(Constants.FIELD_SERVICE_DESC));
				bean.setServiceAuthzFailureMsg(description.get(Constants.FIELD_SERVICE_AUTHZ_FAIL));
			}

			/* Get the service nodes */
			List<Map<String, String>> serviceNodeData = this.spepDAO.queryServiceNodes(bean.getDescriptorID());
			for (Map<String, String> node : serviceNodeData)
			{
				ServiceNodeBean serviceNode = new ServiceNodeBeanImpl();
				serviceNode.setNodeID((String) node.get(Constants.FIELD_ENDPOINT_ID));
				serviceNode.setNodeURL((String) node.get(Constants.FIELD_ENDPOINT_NODEURL));
				serviceNode.setAssertionConsumerService((String) node.get(Constants.FIELD_ENDPOINT_ASSERTIONCONSUMER));
				serviceNode.setSingleLogoutService((String) node.get(Constants.FIELD_ENDPOINT_SINGLELOGOUT));
				serviceNode.setCacheClearService((String) node.get(Constants.FIELD_ENDPOINT_CACHECLEAR));

				serviceNodes.add(serviceNode);
			}
			bean.setServiceNodes(serviceNodes);
			
			return bean;
		}
		catch (SPEPDAOException e)
		{
			throw new ViewServiceException("Exception when attempting get service details", e);
		}
	}
}
