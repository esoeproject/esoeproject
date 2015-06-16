/*
 * Copyright 2008, Queensland University of Technology
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
 * Creation Date: 04/01/2008
 * 
 * Purpose: 
 */

#include "WSHandler.h"
#include "FilterConstants.h"
#include "SPEPExtension.h"

#include "spep/exceptions/InvalidStateException.h"

namespace spep {
    namespace isapi {

        WSHandler::WSHandler(SPEP *spep, SPEPExtension *extension)
            :
            mSpep(spep),
            mSpepExtension(extension)
        {
        }

        spep::SOAPDocument WSHandler::readRequestDocument(HttpRequest *request, spep::SOAPUtil::SOAPVersion *soapVersion, std::string &characterEncoding)
        {
            std::string contentType(request->getContentType());
            if (contentType.find(SOAP12_DOCUMENT_CONTENT_TYPE) != std::string::npos)
            {
                *soapVersion = SOAPUtil::SOAP12;
            }
            else if (contentType.find(SOAP11_DOCUMENT_CONTENT_TYPE) != std::string::npos)
            {
                *soapVersion = SOAPUtil::SOAP11;
            }
            else
            {
                // TODO Throw something more useful
                throw spep::InvalidStateException();
            }

            // This is the way it's done everywhere I can see... Seems kinda dodgy but what can you do.
            std::size_t i;
            if ((i = contentType.find(";")) != std::string::npos)
            {
                if ((i = contentType.find("=", i)) != std::string::npos)
                {
                    characterEncoding = contentType.substr(i + 1);
                }
            }

            DWORD contentLength(128);

            spep::CArray<char> requestDocument(contentLength);

            request->readRequestDocument(requestDocument, contentLength);

            if (contentLength > 0)
            {
                char *documentArray = new char[contentLength];
                std::memcpy(documentArray, requestDocument.get(), contentLength);
                return spep::SOAPDocument(reinterpret_cast<SAMLByte*>(documentArray), contentLength);
            }

            throw spep::InvalidStateException();
        }

        DWORD WSHandler::sendResponseDocument(HttpRequest *request, spep::SOAPDocument soapResponse, spep::SOAPUtil::SOAPVersion soapVersion, const std::string &characterEncoding)
        {
            std::string contentType(request->getContentType());

            return request->sendResponseDocument(HTTP_OK, HTTP_OK_STATUS_LINE, reinterpret_cast<const char*>(soapResponse.getData()), soapResponse.getLength(), contentType);
        }

		DWORD WSHandler::authzCacheClear(HttpRequest* request)
        {
            std::string requestXML;
            try
            {
                std::string characterEncoding;
                spep::SOAPUtil::SOAPVersion soapVersion;
                SOAPDocument requestDocument = readRequestDocument(request, &soapVersion, characterEncoding);

                spep::WSProcessor *wsProcessor = mSpep->getWSProcessor();

                SOAPDocument responseDocument = wsProcessor->authzCacheClear(requestDocument, soapVersion, characterEncoding);

                if (responseDocument.getData() == NULL || responseDocument.getLength() == 0)
                {
                    return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
                }

                return sendResponseDocument(request, responseDocument, soapVersion, characterEncoding);
            }
            catch (std::exception& ex)
            {
                return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
            }
        }

		DWORD WSHandler::singleLogout(HttpRequest* request)
        {
            std::string requestXML;
            try
            {
                std::string characterEncoding;
                spep::SOAPUtil::SOAPVersion soapVersion;
                SOAPDocument requestDocument = readRequestDocument(request, &soapVersion, characterEncoding);

                spep::WSProcessor *wsProcessor = mSpep->getWSProcessor();

                SOAPDocument responseDocument = wsProcessor->singleLogout(requestDocument, soapVersion, characterEncoding);

                if (responseDocument.getData() == NULL || responseDocument.getLength() == 0)
                {
                    return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
                }

                return this->sendResponseDocument(request, responseDocument, soapVersion, characterEncoding);
            }
            catch (std::exception& ex)
            {
                return request->sendErrorDocument(HTTP_INTERNAL_SERVER_ERROR);
            }
        }

		DWORD WSHandler::processRequest(HttpRequest* request)
        {
            // We don't check if the SPEP is started here, because we want the initial authz cache clear request to succeed.
            try
            {
                const std::string path(request->getRequestURL());
                if (path == mSpepExtension->mSpepAuthzCacheClearURL)
                {
                    // This request is bound for /spep/services/spep/authzCacheClear - handle it
                    return authzCacheClear(request);
                }
                else if (path == mSpepExtension->mSpepSingleLogoutURL)
                {
                    // This request is bound for /spep/services/spep/singleLogout - handle it.
                    return singleLogout(request);
                }

                SetLastError(ERROR_FILE_NOT_FOUND);
                return HSE_STATUS_ERROR;
            }
            catch (std::exception& ex)
            {
                SetLastError(ERROR_FILE_NOT_FOUND);
                return HSE_STATUS_ERROR;
            }
            return 0;
        }

    }
}