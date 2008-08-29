package com.qut.middleware.esoe.authn.plugins.statgen.handler;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Handler;

/**
 * This is a really simple handler to spit out details about a users initial authentication and the environment they are doing that within
 */
public class StatsGenerationHandler implements Handler
{
	private String handlerName = "ESOE Statistics Generator";
	private Logger logger = LoggerFactory.getLogger(StatsGenerationHandler.class.getName());
	private Logger statsLogger = LoggerFactory.getLogger("esoe.stats");

	private String statFormat = "{0} - {1} - {2} - {3} - {4} - {5} - {6} - {7} - {8} - {9} - {10} - {11} - {12}";

	public StatsGenerationHandler()
	{
		this.statsLogger.info("Format for stats logger is: <principalName> - <esoeSessionID> - <remoteAddr> - <remoteHost> - <remotePort> - <userAgent> - <referer> - <cookieSessionMgt> - <urlSessionMgt> - <charEnc> - <contType> - <locale> - <protocol>");
	}

	public result execute(AuthnProcessorData data) throws SessionCreationException
	{

		HttpServletRequest req = data.getHttpRequest();
		HttpSession sess = req.getSession();
		
		String details = MessageFormat.format(this.statFormat,
		data.getPrincipalName() ,
		data.getSessionID(),
		req.getRemoteAddr(),
		req.getRemoteHost(),
		req.getRemotePort(),
		req.getHeader("User-Agent"),
		req.getHeader("Referer"),
		req.isRequestedSessionIdFromCookie(),
		req.isRequestedSessionIdFromURL(),
		req.getCharacterEncoding(),
		req.getContentType(),
		req.getLocale(),
		req.getProtocol()
		);

		this.statsLogger.info(details);
		
		return result.SuccessfulNonPrincipal;
	}

	public String getHandlerName()
	{
		return this.handlerName;
	}

}
