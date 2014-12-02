/* Copyright 2006-2007, Queensland University of Technology
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
 * Creation Date: 15/03/2007
 * 
 * Purpose: 
 */

#include "spep/metadata/proxy/MetadataDispatcher.h"

static const std::string GET_SPEP_IDENTIFIER = METADATA_getSPEPIdentifier; // this should be wstring?
static const std::string GET_ESOE_IDENTIFIER = METADATA_getESOEIdentifier; // this should be wstring?
static const std::string GET_SINGLE_SIGNON_ENDPOINT = METADATA_getSingleSignOnEndpoint;
static const std::string GET_SINGLE_LOGOUT_ENDPOINT = METADATA_getSingleLogoutEndpoint;
static const std::string GET_ATTRIBUTE_SERVICE_ENDPOINT = METADATA_getAttributeServiceEndpoint;
static const std::string GET_AUTHZ_SERVICE_ENDPOINT = METADATA_getAuthzServiceEndpoint;
static const std::string GET_SPEP_STARTUP_SERVICE_ENDPOINT = METADATA_getSPEPStartupServiceEndpoint;
static const std::string RESOLVE_KEY = METADATA_resolveKey;


spep::ipc::MetadataDispatcher::MetadataDispatcher(spep::Metadata *metadata) :
    mPrefix(METADATA),
    mMetadata(metadata)
{
}

spep::ipc::MetadataDispatcher::~MetadataDispatcher()
{
}

bool spep::ipc::MetadataDispatcher::dispatch(spep::ipc::MessageHeader &header, spep::ipc::Engine &en)
{
    const std::string dispatch = header.getDispatch();

    // Make sure the prefix matches the expected prefix for this dispatcher.
    if (dispatch.compare(0, strlen(METADATA), mPrefix) != 0)
        return false;

    if (dispatch == GET_SPEP_IDENTIFIER)
    {
        const std::wstring spepIdentifier(mMetadata->getSPEPIdentifier());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(spepIdentifier);
        }

        return true;

    }

    if (dispatch == GET_ESOE_IDENTIFIER)
    {
        const std::wstring esoeIdentifier(mMetadata->getESOEIdentifier());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(esoeIdentifier);
        }

        return true;
    }

    if (dispatch == GET_SINGLE_SIGNON_ENDPOINT)
    {
        const std::string singleSignOnEndpoint(mMetadata->getSingleSignOnEndpoint());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(singleSignOnEndpoint);
        }

        return true;
    }

    if (dispatch == GET_SINGLE_LOGOUT_ENDPOINT)
    {
        const std::string singleLogoutEndpoint(mMetadata->getSingleLogoutEndpoint());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(singleLogoutEndpoint);
        }

        return true;
    }

    if (dispatch == GET_ATTRIBUTE_SERVICE_ENDPOINT)
    {
        const std::string attributeServiceEndpoint(mMetadata->getAttributeServiceEndpoint());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(attributeServiceEndpoint);
        }

        return true;
    }

    if (dispatch == GET_AUTHZ_SERVICE_ENDPOINT)
    {
        const std::string authzServiceEndpoint(mMetadata->getAuthzServiceEndpoint());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(authzServiceEndpoint);
        }

        return true;
    }

    if (dispatch == GET_SPEP_STARTUP_SERVICE_ENDPOINT)
    {
        const std::string spepStartupServiceEndpoint(mMetadata->getSPEPStartupServiceEndpoint());

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(spepStartupServiceEndpoint);
        }

        return true;
    }

    if (dispatch == RESOLVE_KEY)
    {
        std::string keyName;
        en.getObject(keyName);

        saml2::KeyData keyData(mMetadata->resolveKey(keyName));

        if (header.getType() == SPEPIPC_REQUEST)
        {
            // Return the value
            en.sendResponseHeader();
            en.sendObject(keyData);
        }

        return true;
    }

    return false;
}
