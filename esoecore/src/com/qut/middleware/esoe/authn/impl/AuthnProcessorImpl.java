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
 * Creation Date: 06/10/2006
 * 
 * Purpose: Default implementation of AuthnProcessor
 */
package com.qut.middleware.esoe.authn.impl;

import java.text.MessageFormat;
import java.util.List;

import com.qut.middleware.esoe.util.FingerPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.esoe.authn.AuthnProcessor;
import com.qut.middleware.esoe.authn.bean.AuthnProcessorData;
import com.qut.middleware.esoe.authn.exception.AuthnFailureException;
import com.qut.middleware.esoe.authn.exception.HandlerRegistrationException;
import com.qut.middleware.esoe.authn.exception.SessionCreationException;
import com.qut.middleware.esoe.authn.pipeline.Handler;
import com.qut.middleware.esoe.sessions.Principal;
import com.qut.middleware.esoe.sessions.SessionsProcessor;
import com.qut.middleware.esoe.sessions.exception.InvalidSessionIdentifierException;
import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import com.qut.middleware.esoe.spep.SPEPProcessor;

import javax.servlet.http.HttpServletRequest;

public class AuthnProcessorImpl implements AuthnProcessor {
    SPEPProcessor spepProcessor;
    SessionsProcessor sessionsProcessor;
    private final List<Handler> registeredHandlers;

    private String hostname;
    private int port;
    private String password;
    private int expireInterval;

    private Logger logger = LoggerFactory.getLogger(AuthnProcessorImpl.class.getName());

    /**
     * @param sessionTokenName
     * @param sessionDomain
     * @param registeredHandlers Vector of registered handlers for authn in the esoe
     * @throws HandlerRegistrationException Thrown when no handlers have been supplied
     */
    public AuthnProcessorImpl(SPEPProcessor spepProcessor, SessionsProcessor sessionsProcessor, List<Handler> registeredHandlers, String hostname, int port, String password, int expireInterval) throws HandlerRegistrationException {
        /* Ensure that a stable base is created when this Processor is setup */
        if (spepProcessor == null) {
            this.logger.error(Messages.getString("AuthnProcessorImpl.14")); //$NON-NLS-1$
            throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.15")); //$NON-NLS-1$
        }
        if (sessionsProcessor == null) {
            this.logger.error(Messages.getString("AuthnProcessorImpl.16")); //$NON-NLS-1$
            throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.17")); //$NON-NLS-1$
        }
        if (registeredHandlers == null || registeredHandlers.size() == 0) {
            this.logger.error(Messages.getString("AuthnProcessorImpl.0")); //$NON-NLS-1$
            throw new IllegalArgumentException(Messages.getString("AuthnProcessorImpl.0")); //$NON-NLS-1$
        }

        this.spepProcessor = spepProcessor;
        this.sessionsProcessor = sessionsProcessor;
        this.registeredHandlers = registeredHandlers;

        if (registeredHandlers.size() == 0) {
            this.logger.error(Messages.getString("AuthnProcessorImpl.5")); //$NON-NLS-1$
            throw new HandlerRegistrationException(Messages.getString("AuthnProcessorImpl.6")); //$NON-NLS-1$
        }

        this.hostname = hostname;
        this.port = port;
        this.password = password;
        this.expireInterval = expireInterval;

        this.logger.info(MessageFormat.format("Redis connection details - Host: {0} Port: {1} Password: ******* ExpireInterval: {2}", this.hostname, this.port, this.expireInterval));
    }

    /*
      * (non-Javadoc)
      *
      * @see com.qut.middleware.esoe.authn.AuthnProcessor#execute(com.qut.middleware.esoe.authn.bean.AuthnProcessorData)
      */
    public result execute(AuthnProcessorData data) throws AuthnFailureException {
        Handler.result result;

        /* Determine if a sessionID is set and if so if its valid in our system */
        if (data.getSessionID() != null && data.getSessionID().length() > 0) {
            this.sessionsProcessor.getQuery().validAuthnSession(data.getSessionID());
        }

        /* Even with iterators no sync required here as don't reasonably expect the structure of the underlying list to be modified */
        for (Handler handler : this.registeredHandlers) {
            /* For continuing session don't re-evaluate handlers we have already passed */
            if (data.getCurrentHandler() != null && data.getCurrentHandler().length() > 0 && !data.getCurrentHandler().equals(handler.getHandlerName()))
                continue;

            data.setCurrentHandler(handler.getHandlerName());
            this.logger.debug(Messages.getString("AuthnProcessorImpl.7") + handler.getHandlerName()); //$NON-NLS-1$
            try {
                result = handler.execute(data);
                this.logger
                        .info(Messages.getString("AuthnProcessorImpl.8") + handler.getHandlerName() + Messages.getString("AuthnProcessorImpl.9") + result.toString()); //$NON-NLS-1$ //$NON-NLS-2$
                switch (result) {
                    /* Ensure that multiple handlers aren't competing to identify the principal in the stack */
                    case Successful:
                        if (!data.getSuccessfulAuthn())
                            data.setSuccessfulAuthn(true);
                        else
                            throw new AuthnFailureException(Messages.getString("AuthnProcessorImpl.3")); //$NON-NLS-1$

                        /* Handler completed reset current handler value */
                        data.setCurrentHandler(null);

                        // new principal session created, save the fingerprint
                        saveFingerprint(data);

                        break;
                    case SuccessfulNonPrincipal:
                        /* Handler completed reset current handler value */
                        data.setCurrentHandler(null);
                        break;
                    case NoAction:
                        /* Handler completed reset current handler value */
                        data.setCurrentHandler(null);
                        break;
                    case UserAgent:
                        return AuthnProcessor.result.UserAgent;
                    case Failure:
                        this.purgeFailedSession(data);
                        return AuthnProcessor.result.Failure;
                    case Invalid:
                        this.purgeFailedSession(data);
                        return AuthnProcessor.result.Invalid;
                }
            } catch (SessionCreationException sce) {
                this.logger.warn(Messages.getString("AuthnProcessorImpl.10")); //$NON-NLS-1$
                return AuthnProcessor.result.Failure;
            }
        }

        if (!data.getSuccessfulAuthn()) {
            this.logger.warn(Messages.getString("AuthnProcessorImpl.4")); //$NON-NLS-1$
            throw new AuthnFailureException(Messages.getString("AuthnProcessorImpl.4")); //$NON-NLS-1$
        }

        this.logger.debug(Messages.getString("AuthnProcessorImpl.13") + data.getSessionID()); //$NON-NLS-1$

        Principal principal = this.sessionsProcessor.getQuery().queryAuthnSession(data.getSessionID());
        if (principal != null)
            this.spepProcessor.clearPrincipalSPEPCaches(principal);

        return AuthnProcessor.result.Completed;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.qut.middleware.esoe.authn.AuthnProcessor#getRegisteredHandlers()
      */
    public List<Handler> getRegisteredHandlers() {
        return this.registeredHandlers;
    }

    /**
     * Purges a session which has been established by one handler but then revoked by another
     *
     * @param data Populated AuthnProcessorData instance
     */
    private void purgeFailedSession(AuthnProcessorData data) {
        try {
            if (data.getSuccessfulAuthn()) {
                data.setSuccessfulAuthn(false);
                this.sessionsProcessor.getTerminate().terminateSession(data.getSessionID());
            }
        } catch (SessionCacheUpdateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void saveFingerprint(AuthnProcessorData data) {

        // Insert fingerprint check to eliminate the possibility of swapped user sessions
        HttpServletRequest userRequest = data.getHttpRequest();
        String userAgentData = userRequest.getRemoteAddr() + userRequest.getHeader("User-Agent") + userRequest.getHeader("Accept-Encoding");
        FingerPrint printChecker = new FingerPrint(hostname, port, password, expireInterval);
        String fingerprint = printChecker.generateFingerprint(userAgentData);

        if (printChecker.saveFingerprint(data.getSessionID(), fingerprint)) {
            logger.info("{} - Saved fingerprint for newly authenticated session {}", userRequest.getRemoteAddr(), data.getSessionID());

        } else {
            logger.warn("{} - Attempt to save fingerprint for new session ID {} failed", userRequest.getRemoteAddr(), data.getSessionID());
        }

    }
}
