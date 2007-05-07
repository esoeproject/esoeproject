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
 * Author: Shaun Mangelsdorf / Bradley Beddoes
 * Creation Date: 13/11/2006 / 03/03/2007
 * 
 * Purpose: Implements the PrincipalSession interface
 */
package com.qut.middleware.spep.sessions.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qut.middleware.spep.sessions.PrincipalSession;

/** Implements the PrincipalSession interface. */
public class PrincipalSessionImpl implements PrincipalSession
{
	private String esoeSessionID;
	private Date sessionNotOnOrAfter;
	private List<String> sessionID;
	private Map<String, List<Object>> attributes;
	private Map<String, String> esoeSessionIndexMap;

	/**
	 * Default constructor
	 */
	public PrincipalSessionImpl()
	{
		this.attributes = new HashMap<String, List<Object>>();
		this.sessionID = Collections.synchronizedList( (new ArrayList<String>()) );
		this.esoeSessionIndexMap = Collections.synchronizedMap( (new HashMap<String, String>()) );
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getEsoeSessionIndex()
	 */
	public String getEsoeSessionID()
	{
		return this.esoeSessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#setEsoeSessionIndex(java.lang.String)
	 */
	public void setEsoeSessionID(String esoeSessionID)
	{
		this.esoeSessionID = esoeSessionID;
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#addEsoeSessionIndex(java.lang.String, java.lang.String)
	 */
	public void addESOESessionIndexAndLocalSessionID(String esoeSessionIndex, String localSessionID)
	{
		synchronized(this.esoeSessionIndexMap)
		{
			this.esoeSessionIndexMap.put(esoeSessionIndex, localSessionID);
		}
		
		synchronized(this.sessionID)
		{
			this.sessionID.add(localSessionID);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getEsoeSessionIndex()
	 */
	public Map<String, String> getEsoeSessionIndex()
	{
		return this.esoeSessionIndexMap;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getSessionIDList()
	 */
	public List<String> getSessionIDList()
	{
		return this.sessionID;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getSessionNotOnOrAfter()
	 */
	public Date getSessionNotOnOrAfter()
	{
		return this.sessionNotOnOrAfter;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#setSessionNotOnOrAfter(java.util.Date)
	 */
	public void setSessionNotOnOrAfter(Date sessionNotOnOrAfter)
	{
		this.sessionNotOnOrAfter = sessionNotOnOrAfter;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.spep.sessions.PrincipalSession#getAttributes()
	 */
	public Map<String, List<Object>> getAttributes()
	{
		return this.attributes;
	}

	
	/* These methods overriden to ensure that hashmaps can use this object as a key without hassle */
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.esoeSessionID.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof PrincipalSession) && ((PrincipalSession)obj).getEsoeSessionID().equals(this.esoeSessionID);
	}
}
