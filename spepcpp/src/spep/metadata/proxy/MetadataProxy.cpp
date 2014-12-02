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

#include "spep/metadata/proxy/MetadataProxy.h"
#include "spep/metadata/proxy/MetadataDispatcher.h"

#include "saml2/exceptions/InvalidParameterException.h"

static const std::string GET_SPEP_IDENTIFIER = METADATA_getSPEPIdentifier; // this should be wstring?
static const std::string GET_ESOE_IDENTIFIER = METADATA_getESOEIdentifier; // this should be wstring?
static const std::string GET_SINGLE_SIGNON_ENDPOINT = METADATA_getSingleSignOnEndpoint;
static const std::string GET_SINGLE_LOGOUT_ENDPOINT = METADATA_getSingleLogoutEndpoint;
static const std::string GET_ATTRIBUTE_SERVICE_ENDPOINT = METADATA_getAttributeServiceEndpoint;
static const std::string GET_AUTHZ_SERVICE_ENDPOINT = METADATA_getAuthzServiceEndpoint;
static const std::string GET_SPEP_STARTUP_SERVICE_ENDPOINT = METADATA_getSPEPStartupServiceEndpoint;
static const std::string RESOLVE_KEY = METADATA_resolveKey;

spep::ipc::MetadataProxy::MetadataProxy(spep::ipc::ClientSocketPool *socketPool) :
    mSocketPool(socketPool)
{
}

spep::ipc::MetadataProxy::~MetadataProxy()
{
    CryptoKeyPointerList::iterator iter;
    for (iter = mCryptoKeyList.begin(); iter != mCryptoKeyList.end(); ++iter)
    {
        // TODO Investigate if we need to delete keys here.
        //XSECCryptoKey *key = *iter;
        // They shouldn't be in the list if they're NULL.
        //if( key != NULL )
        //	delete key;
    }
}

const std::wstring spep::ipc::MetadataProxy::getSPEPIdentifier() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::wstring>(GET_SPEP_IDENTIFIER, noData);
}

const std::wstring spep::ipc::MetadataProxy::getESOEIdentifier() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::wstring>(GET_ESOE_IDENTIFIER, noData);
}

const std::string spep::ipc::MetadataProxy::getSingleSignOnEndpoint() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::string>(GET_SINGLE_SIGNON_ENDPOINT, noData);
}

const std::string spep::ipc::MetadataProxy::getSingleLogoutEndpoint() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::string>(GET_SINGLE_LOGOUT_ENDPOINT, noData);
}

const std::string spep::ipc::MetadataProxy::getAttributeServiceEndpoint() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::string>(GET_ATTRIBUTE_SERVICE_ENDPOINT, noData);
}

const std::string spep::ipc::MetadataProxy::getAuthzServiceEndpoint() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::string>(GET_AUTHZ_SERVICE_ENDPOINT, noData);
}

const std::string spep::ipc::MetadataProxy::getSPEPStartupServiceEndpoint() const
{
    spep::ipc::NoData noData;

    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest<std::string>(GET_SPEP_STARTUP_SERVICE_ENDPOINT, noData);
}

XSECCryptoKey *spep::ipc::MetadataProxy::resolveKey(DSIGKeyInfoList *lst)
{
    if (lst->getSize() < 1)
    {
        SAML2LIB_INVPARAM_EX("List did not have at least one keyinfo");
    }

    DSIGKeyInfo *keyInfo = lst->item(0);
    const XMLCh *keyNameXMLString = keyInfo->getKeyName();
    std::auto_ptr< XercesCharStringAdapter > keyNameChars(new XercesCharStringAdapter(XMLString::transcode(keyNameXMLString)));

    std::string keyName(keyNameChars->get(), XMLString::stringLen(keyNameXMLString));

    saml2::KeyData keyData(resolveKey(keyName));

    // TODO Memory leak here?
    // This XSECCryptoKey must be deleted in this class, since xml-security-c won't.
    XSECCryptoKey *cryptoKey = keyData.createXSECCryptoKey();
    // Store the pointer in a list to be destructed when this class goes out of scope.

    if (cryptoKey != NULL)
    {
        mCryptoKeyList.push_back(cryptoKey);
    }

    return cryptoKey;

}

saml2::KeyData spep::ipc::MetadataProxy::resolveKey(const std::string& keyName)
{
    ClientSocketLease clientSocket(mSocketPool);
    return clientSocket->makeRequest< saml2::KeyData >(RESOLVE_KEY, keyName);
}

XSECKeyInfoResolver* spep::ipc::MetadataProxy::clone() const
{
    return new MetadataProxy(mSocketPool);
}
