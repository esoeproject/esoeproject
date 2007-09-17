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
 * Author: Shaun Mangelsdorf
 * Creation Date: 09/11/2006
 * 
 * Purpose: Interface for the data contained as part of a client session.
 */
package com.qut.middleware.spep.sessions;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** Interface for the data contained as part of a client session. */
public interface PrincipalSession 
{
	/** 
	 * Returns list of sessionID's that this principal has acquired locally
	 * 
	 * @return the sessionID
	 */
	public List<String> getSessionIDList();
	
	/** Authenticated ESOE Session identifier of client. The SessionID is a global identifier
	 * set by the ESOE.
	 * 
	 * @param esoeSessionID the sessionID to set
	 */
	public String getEsoeSessionID();
	
	/** Authenticated ESOE Session identifier of client. The SessionID is a global identifier
	 * set by the ESOE.
	 * 
	 * @param sessionID the sessionID to set
	 */
	public void setEsoeSessionID(String esoeSessionID);
	
	/** String representation of a timestamp limiting the duration of a client session, must be UTC.
	 * 
	 * @return the sessionNotOnOrAfter
	 */
	public Date getSessionNotOnOrAfter();
	
	/** String representation of a timestamp limiting the duration of a client session, must be UTC.
	 * 
	 * @param sessionNotOnOrAfter the sessionNotOnOrAfter to set
	 */
	public void setSessionNotOnOrAfter(Date sessionNotOnOrAfter);
	
	/** The list of client attributes.
	 * 
	 * @return A map of AttributeName -> List of AttributeValues.
	 */
	public Map<String, List<Object>> getAttributes();
	
	/** 
	 * Sets a mapping up between the local sessionID of the principal and the remove session index on 
	 * the ESOE which makes this session unique, also adds the sessionID to interal list of all sessionID's
	 * for this principal as convenience
	 * 
	 * @param index The session index identifier.
	 */
	public void addESOESessionIndexAndLocalSessionID(String esoeSessionIndex, String localSessionID);
	
	/** Set the value used to identify the particular session on the esoe for this client principal session.
	 * The session index is used to differentiate between different sessions held by the same principal.
	 * 
	 * @return The session index identifier.
	 */
	public Map<String, String> getEsoeSessionIndex();
	
}
