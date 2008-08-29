package com.qut.middleware.esoe.logout.bean.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qut.middleware.esoe.logout.bean.LogoutProcessorData;
import com.qut.middleware.esoe.logout.bean.SSOLogoutState;

public class LogoutProcessorDataImpl implements LogoutProcessorData
{
	private HttpServletRequest httpRequest;
	private HttpServletResponse httpResponse;
	
	private String samlBinding;
	private String sessionID;
	private String requestCharsetName;
	
	private List<SSOLogoutState> logoutStates;

	public HttpServletRequest getHttpRequest()
	{
		return httpRequest;
	}

	public void setHttpRequest(HttpServletRequest httpRequest)
	{
		this.httpRequest = httpRequest;
	}

	public HttpServletResponse getHttpResponse()
	{
		return httpResponse;
	}

	public void setHttpResponse(HttpServletResponse httpResponse)
	{
		this.httpResponse = httpResponse;
	}

	public String getSamlBinding()
	{
		return samlBinding;
	}

	public void setSamlBinding(String samlBinding)
	{
		this.samlBinding = samlBinding;
	}

	public List<SSOLogoutState> getLogoutStates()
	{
		return logoutStates;
	}

	public void setLogoutStates(List<SSOLogoutState> logoutStates)
	{
		this.logoutStates = logoutStates;
	}

	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public String getRequestCharsetName()
	{
		return requestCharsetName;
	}

	public void setRequestCharsetName(String requestCharsetName)
	{
		this.requestCharsetName = requestCharsetName;
	}
}
