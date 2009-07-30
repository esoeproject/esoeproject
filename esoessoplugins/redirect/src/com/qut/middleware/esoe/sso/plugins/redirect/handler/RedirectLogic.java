package com.qut.middleware.esoe.sso.plugins.redirect.handler;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.plugins.redirect.bean.RedirectBindingData;
import com.qut.middleware.esoe.sso.plugins.redirect.exception.RedirectBindingException;

public interface RedirectLogic
{

	public abstract void handleRedirectRequest(SSOProcessorData data, RedirectBindingData bindingData) throws RedirectBindingException;

	public abstract void handleRedirectResponse(SSOProcessorData data, RedirectBindingData bindingData) throws RedirectBindingException;

}