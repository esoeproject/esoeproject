package com.qut.middleware.esoe.sso.plugins.post.handler;

import com.qut.middleware.esoe.sso.bean.SSOProcessorData;
import com.qut.middleware.esoe.sso.plugins.post.bean.PostBindingData;
import com.qut.middleware.esoe.sso.plugins.post.exception.PostBindingException;

public interface PostLogic
{

	public void handlePostRequest(SSOProcessorData data, PostBindingData bindingData) throws PostBindingException;

	public void handlePostResponse(SSOProcessorData data, PostBindingData bindingData) throws PostBindingException;

}