/* 
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 13/06/2007
 * 
 * Purpose: Implementation of bean to hold shib attributes
 */
package com.qut.middleware.delegator.shib.authn.bean.impl;

import com.qut.middleware.delegator.shib.authn.bean.ShibAttribute;

public class ShibAttributeImpl implements ShibAttribute
{
	private String esoeAttributeName;
	private String label;
	private String schema;
	private String valuePrepend;
	private String value;
	private boolean required;

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#getLabel()
	 */
	public String getLabel()
	{
		return this.label;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#getSchema()
	 */
	public String getSchema()
	{
		return this.schema;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#isRequired()
	 */
	public boolean isRequired()
	{
		return this.required;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#setLabel(java.lang.String)
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#setRequired(boolean)
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#setSchema(java.lang.String)
	 */
	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#getESOEAttributeName()
	 */
	public String getEsoeAttributeName()
	{
		return this.esoeAttributeName;
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#setEsoeAttributeName(java.lang.String)
	 */
	public void setEsoeAttributeName(String esoeAttributeName)
	{
		this.esoeAttributeName = esoeAttributeName;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#getValue()
	 */
	public String getValue()
	{
		return value;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#setValue(java.lang.String)
	 */
	public void setValue(String value)
	{
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#getValuePrepend()
	 */
	public String getValuePrepend()
	{
		return valuePrepend;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.delegator.openid.authn.bean.OpenIDAttribute#setValuePrepend(java.lang.String)
	 */
	public void setValuePrepend(String valuePrepend)
	{
		this.valuePrepend = valuePrepend;
	}

}
