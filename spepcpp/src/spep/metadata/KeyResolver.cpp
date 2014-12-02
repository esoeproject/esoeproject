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
 * Creation Date: 26/02/2007
 * 
 * Purpose: 
 */

 
#include "spep/metadata/KeyResolver.h"
#include "spep/metadata/JKSKeystore.h"
#include "spep/exceptions/KeyResolverException.h"

#include "spep/Util.h"
#include "spep/Base64.h"

#include <openssl/x509.h>

#include <xsec/dsig/DSIGKeyInfo.hpp>
#include <xsec/dsig/DSIGKeyInfoName.hpp>

spep::KeyResolver::KeyResolver() :
    mSpepPublicKey(nullptr),
    mSpepPublicKeyB64(),
    mSpepPrivateKey(nullptr),
    mSpepPrivateKeyB64(),
    mTrustedCerts()
{
}

spep::KeyResolver::~KeyResolver()
{
    deleteKeys();
}

spep::KeyResolver::KeyResolver(const spep::KeyResolver &other) :
    mSpepPublicKey(nullptr),
    mSpepPublicKeyB64(other.mSpepPublicKeyB64),
    mSpepPrivateKey(nullptr),
    mSpepPrivateKeyB64(other.mSpepPrivateKeyB64),
    mSpepKeyAlias(other.mSpepKeyAlias),
    mTrustedCerts(other.mTrustedCerts)
{
    if (other.mSpepPublicKey != nullptr)
    {
        mSpepPublicKey = other.mSpepPublicKey->clone();
    }

    if (other.mSpepPrivateKey != nullptr)
    {
        mSpepPrivateKey = other.mSpepPrivateKey->clone();
    }
}

spep::KeyResolver& spep::KeyResolver::operator=(const spep::KeyResolver &other)
{
    deleteKeys();

    if (other.mSpepPublicKey != nullptr)
    {
        mSpepPublicKey = other.mSpepPublicKey->clone();
    }

    mSpepPublicKeyB64 = other.mSpepPublicKeyB64;

    if (other.mSpepPrivateKey != nullptr)
    {
        mSpepPrivateKey = other.mSpepPrivateKey->clone();
    }

    mSpepPrivateKeyB64 = other.mSpepPrivateKeyB64;

    if (other.mSpepPublicKey != nullptr)
    {
        mSpepPublicKey = other.mSpepPublicKey->clone();
    }
    else
    {
        mSpepPublicKey = nullptr;
    }

    if (other.mSpepPrivateKey != nullptr)
    {
        mSpepPrivateKey = other.mSpepPrivateKey->clone();
    }
    else
    {
        mSpepPrivateKey = nullptr;
    }

    mSpepKeyAlias = other.mSpepKeyAlias;
    mTrustedCerts = other.mTrustedCerts;

    return *this;
}

spep::KeyResolver::KeyResolver(const std::string& keystorePath, const std::string& keystorePassword, const std::string& spepKeyAlias, const std::string& spepKeyPassword) :
    mSpepPublicKey(nullptr),
    mSpepPublicKeyB64(),
    mSpepPrivateKey(nullptr),
    mSpepPrivateKeyB64(),
    mSpepKeyAlias(spepKeyAlias),
    mTrustedCerts()
{
    JKSKeystore keystore(keystorePath, keystorePassword, spepKeyPassword);

    const JKSPrivateKeyData *spepPkeyData = keystore.getKeyData(spepKeyAlias);
    const JKSTrustedCertData *spepCertData = &(keystore.getCertificateChain(spepKeyAlias).at(0));

    {
        Base64Encoder encoder;
        encoder.push(reinterpret_cast<const char*>(spepCertData->data), spepCertData->len);
        encoder.close();
        Base64Document cert = encoder.getResult();

        mSpepPublicKeyB64.assign(cert.getData(), cert.getLength());
    }

    {
        Base64Encoder encoder;
        encoder.push(reinterpret_cast<const char*>(spepPkeyData->data), spepPkeyData->len);
        encoder.close();
        Base64Document key = encoder.getResult();

        mSpepPrivateKeyB64.assign(key.getData(), key.getLength());
    }

    std::vector<std::string> trustedCertAliases(keystore.getCertificateAliases());
    
    for (auto iter = trustedCertAliases.begin(); iter != trustedCertAliases.end(); ++iter)
    {
        std::string keyAlias(*iter);
        const JKSTrustedCertData *certData = keystore.getCertificateData(keyAlias);

        Base64Encoder encoder;
        encoder.push(reinterpret_cast<const char*>(certData->data), certData->len);
        encoder.close();
        Base64Document cert = encoder.getResult();

        std::string encodedCert(cert.getData(), cert.getLength());

        mTrustedCerts[keyAlias] = encodedCert;
    }
}

void spep::KeyResolver::deleteKeys()
{
    delete mSpepPublicKey;
    mSpepPublicKey = nullptr;
    delete mSpepPrivateKey;
    mSpepPrivateKey = nullptr;
}

void spep::KeyResolver::loadSPEPPublicKey()
{
    delete mSpepPublicKey;
    mSpepPublicKey = nullptr;

    // OpenSSLCryptoX509 object to hold the key until it is cloned.
    // FIXME: should this be a unique_ptr?
    std::auto_ptr<OpenSSLCryptoX509> x509(new OpenSSLCryptoX509());
    x509->loadX509Base64Bin(mSpepPublicKeyB64.c_str(), mSpepPublicKeyB64.length());

    mSpepPublicKey = x509->clonePublicKey();
}

void spep::KeyResolver::loadSPEPPrivateKey()
{
    delete mSpepPrivateKey;
    mSpepPrivateKey = nullptr;

    Base64Decoder decoder;
    decoder.push(mSpepPrivateKeyB64.c_str(), mSpepPrivateKeyB64.length());
    decoder.close();
    Base64Document pkey = decoder.getResult();

    // Read the key data into an OpenSSL RSA key.
    BIO* bioMem = BIO_new_mem_buf(const_cast<char*>(pkey.getData()), pkey.getLength());

    PKCS8_PRIV_KEY_INFO *pkeyInfo = NULL;
    // TODO This line leaks 4 blocks. (2918 bytes)
    d2i_PKCS8_PRIV_KEY_INFO_bio(bioMem, &pkeyInfo);

    if (pkeyInfo == NULL)
    {
        throw std::exception();
    }

    EVP_PKEY* rawkey = EVP_PKCS82PKEY(pkeyInfo);
    mSpepPrivateKey = new OpenSSLCryptoKeyRSA(rawkey);

    BIO_free(bioMem);
}

XSECCryptoKey* spep::KeyResolver::getSPEPPublicKey()
{
    if (!mSpepPublicKey)
        loadSPEPPublicKey();
    return mSpepPublicKey;
}

XSECCryptoKey* spep::KeyResolver::getSPEPPrivateKey()
{
    if (!mSpepPrivateKey)
        loadSPEPPrivateKey();
    return mSpepPrivateKey;
}

std::string spep::KeyResolver::getSPEPKeyAlias() const
{
    return mSpepKeyAlias;
}

XSECCryptoKey *spep::KeyResolver::resolveKey(DSIGKeyInfoList *list)
{
    if (list->isEmpty())
        return NULL;

    // Loop through the key info list and look for a name.
    for (DSIGKeyInfoList::size_type i = 0; i < list->getSize(); ++i)
    {
        DSIGKeyInfo *keyInfo = list->item(i);
        if (keyInfo->getKeyInfoType() != DSIGKeyInfo::KEYINFO_NAME) continue;

        // This keyInfo is a key name, so cast it and grab the name as a Xerces char*
        DSIGKeyInfoName* keyInfoName = reinterpret_cast<DSIGKeyInfoName*>(keyInfo);
        std::auto_ptr<XercesCharStringAdapter> keyNameChars(new XercesCharStringAdapter(XMLString::transcode(keyInfoName->getKeyName())));

        std::string keyName(keyNameChars->get());

        try
        {
            XSECCryptoKey* key = this->resolveKey(keyName);
            return key;
        }
        catch (std::exception& e)
        {
        }
    }

    // No key data found/returned. Return null now.
    return NULL;
}

XSECCryptoKey* spep::KeyResolver::resolveKey(const std::string& keyName)
{
    auto iter = mTrustedCerts.find(keyName);
    if (iter == mTrustedCerts.end()) return NULL;

    std::auto_ptr<OpenSSLCryptoX509> x509(new OpenSSLCryptoX509());
    x509->loadX509Base64Bin(reinterpret_cast<const char*>(iter->second.c_str()), iter->second.length());

    XSECCryptoKey *key = x509->clonePublicKey();
    return key;
}

XSECKeyInfoResolver* spep::KeyResolver::clone() const
{
    return new spep::KeyResolver(*this);
}

