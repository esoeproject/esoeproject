/*
 * Copyright 2007, Queensland University of Technology
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
 * Creation Date: 14/12/2007
 * 
 * Purpose: 
 */

/*
 * This implementation was created using the information publically available at
 * http://www.metastatic.org/source/JKS.html
 * http://java.sun.com/j2se/1.4.2/docs/api/javax/crypto/EncryptedPrivateKeyInfo.html
 * 
 * No reference to any Sun or other source code was made while writing this.
 */

#include "spep/metadata/JKSKeystore.h"
#include "spep/exceptions/KeystoreException.h"

#include <fstream>
#include <cstring>

#ifndef WIN32

#include <netinet/in.h>
#include <arpa/inet.h>

#endif //!WIN32

#include <openssl/evp.h>
#include <openssl/asn1.h>
#include <openssl/asn1t.h>
#include <openssl/opensslv.h>

#define KEYSTORE_MAGIC_NUMBER 0xFEEDFEED
#define KEYSTORE_MAGIC_STRING "Mighty Aphrodite"
#define KEYSTORE_UNEXPECTED_VERSION(x) (x != 2)

#define KEYSTORE_ENTRY_PRIVATE_KEY 1
#define KEYSTORE_ENTRY_TRUSTED_CERT 2

#define KEYSTORE_ERROR_NO_FILE "Unable to open Keystore. File does not exist or is not readable."
#define KEYSTORE_ERROR_NO_CERT_CHAIN "No entry in the keystore for the specified key alias."
#define KEYSTORE_ERROR_CORRUPT_MAGIC "Keystore magic number does not match. Possibly corrupt?"
#define KEYSTORE_ERROR_BAD_VERSION "Keystore version does not match the expected version number."
#define KEYSTORE_ERROR_UNEXPECTED_EOF "Unexpected end of file while trying to parse the keystore."
#define KEYSTORE_ERROR_UNKNOWN_ENTRY_TYPE "Unknown entry type was encountered. Unable to continue parsing."
#define KEYSTORE_ERROR_CORRUPT_SIGNATURE "Hash value does not match the signed value in the keystore. Possibly corrupt or wrong keystore password?"
#define KEYSTORE_ERROR_CORRUPT_KEY "Private key data could not be validated. Wrong password?"

namespace spep
{

    struct JKSKeystoreHeaderFormat
    {
        unsigned char magic[4];
        unsigned char version[4];
        unsigned char count[4];
        unsigned char remain[0];
    };

    struct JKSEntry
    {
        unsigned char type[4];
        unsigned char alias[0];
    };

    struct JKSAliasEntry
    {
        unsigned char length[2];
        unsigned char data[0];
    };

    struct JKSDataEntry
    {
        unsigned char length[4];
        unsigned char data[0];
    };

    struct JKSSignature
    {
        unsigned char value[20];
    };

    /**
     * Defines the ASN.1 structure used for the JKS key
     * algorithm identifier.
     */
    typedef struct JKSPrivateKeyAlgorithmIdentifier
    {
        ASN1_OBJECT *algorithm;
        /* JKS private keys have a null parameter to the algorithm */
        ASN1_NULL *nullParameter;
    } JKS_ALGORITHM;

    /**
     * Defines the ASN.1 structure used for the JKS private
     * key information record
     */
    typedef struct JKSPrivateKeyInfo
    {
        JKS_ALGORITHM *encryptionAlgorithm;
        ASN1_OCTET_STRING *encryptedData;
    } JKS_PKEYINFO;

    const JKSPrivateKeyData* JKSKeystore::getKeyData(const std::string& alias)
    {
        auto iter = mPKeyDataMap.find(alias);
        if (iter != mPKeyDataMap.end())
        {
            return &(iter->second);
        }

        return nullptr;
    }

    const JKSTrustedCertData* JKSKeystore::getCertificateData(const std::string& alias)
    {
        auto iter = mCertDataMap.find(alias);
        if (iter != mCertDataMap.end())
        {
            return &(iter->second);
        }

        return nullptr;
    }

    const std::vector<JKSTrustedCertData>& JKSKeystore::getCertificateChain(const std::string& alias)
    {
        auto iter = mCertChainMap.find(alias);
        if (iter != mCertChainMap.end())
        {
            return iter->second;
        }

        throw KeystoreException(KEYSTORE_ERROR_NO_CERT_CHAIN);
    }

    std::vector<std::string> JKSKeystore::getCertificateAliases()
    {
        std::vector<std::string> retval;
        for (auto iter = mCertDataMap.begin(); iter != mCertDataMap.end(); ++iter)
        {
            retval.push_back(iter->first);
        }

        return retval;
    }

    /**
     * OpenSSL ASN.1 definitions to be used when parsing the private
     * key from the JKS keystore
     */
    /** @{ */
    ASN1_SEQUENCE(JKS_ALGORITHM) = {
        ASN1_SIMPLE(JKS_ALGORITHM, algorithm, ASN1_OBJECT),
        ASN1_SIMPLE(JKS_ALGORITHM, nullParameter, ASN1_NULL)
    } ASN1_SEQUENCE_END(JKS_ALGORITHM)

    IMPLEMENT_ASN1_FUNCTIONS(JKS_ALGORITHM)

    ASN1_SEQUENCE(JKS_PKEYINFO) = {
            ASN1_SIMPLE(JKS_PKEYINFO, encryptionAlgorithm, JKS_ALGORITHM),
            ASN1_SIMPLE(JKS_PKEYINFO, encryptedData, ASN1_OCTET_STRING)
        } ASN1_SEQUENCE_END(JKS_PKEYINFO)

        IMPLEMENT_ASN1_FUNCTIONS(JKS_PKEYINFO)
        /** @} */

        uint16_t JKSKeystore::twoByteToUnsignedInt(const unsigned char* data)
            {
                uint16_t retval = *(reinterpret_cast<const uint16_t*>(data));

                return ntohs(retval);
            }

            uint32_t JKSKeystore::fourByteToUnsignedInt(const unsigned char* data)
            {
                uint32_t retval = *(reinterpret_cast<const uint32_t*>(data));

                return ntohl(retval);
            }

            uint64_t JKSKeystore::eightByteToUnsignedInt(const unsigned char* data)
            {
                uint64_t retval;
                retval = fourByteToUnsignedInt(data);

                // Shift 4 bytes. Compiler will undoubtedly optimize this. It's just easier to read this way.
                for (int i = 0; i < 4; ++i)
                    retval *= 0x100;

                retval = fourByteToUnsignedInt(&data[4]);
                return retval;
            }

            JKSKeystore::~JKSKeystore()
            {
                delete[] mKeystorePassword;
                mKeystorePassword = nullptr;

                delete mPasswordResolver;
                mPasswordResolver = nullptr;

                for (auto iter = mFreeList.begin(); iter != mFreeList.end(); ++iter)
                {
                    delete[] *iter;
                }
            }

            JKSKeystore::JKSKeystore(const std::string& filename, const std::string& keystorePassword)
            {
                this->initWithPassword(filename, keystorePassword);
            }

            JKSKeystore::JKSKeystore(const std::string& filename, const std::string& keystorePassword, const std::string& privateKeyPassword)
            {
                this->initWithPassword(filename, keystorePassword, &privateKeyPassword);
            }

            void JKSKeystore::initWithPassword(const std::string& filename, const std::string& keystorePassword, const std::string* privateKeyPassword)
            {
                // Open the keystore file.
                std::ifstream keystoreInput(filename.c_str(), std::ios::in | std::ios::binary);
                if (!keystoreInput.good()) throw KeystoreException(KEYSTORE_ERROR_NO_FILE);

                // Seek to end and store the length of the file
                keystoreInput.seekg(0, std::ios::end);
                std::size_t keystoreLength = keystoreInput.tellg();
                keystoreInput.seekg(0, std::ios::beg);

                // Allocate room to read the entire file into memory.
                unsigned char *buffer = new unsigned char[keystoreLength];
                class DeleteBuffer { public: DeleteBuffer(unsigned char *buf) : _buf(buf){} ~DeleteBuffer() { delete[] _buf; } private: unsigned char *_buf; }
                deleteBuffer(buffer);

                // Read the keystore from the file.
                keystoreInput.read(reinterpret_cast<char*>(buffer), keystoreLength);

                // Store the keystore password as a member variable
                mKeystorePasswordLength = keystorePassword.length() * 2;
                unsigned char *keystorePasswordUChars = new unsigned char[mKeystorePasswordLength];
                for (int i = 0; i < mKeystorePasswordLength; i += 2)
                {
                    keystorePasswordUChars[i] = 0;
                    keystorePasswordUChars[i + 1] = keystorePassword.at(i / 2);
                }
                mKeystorePassword = keystorePasswordUChars;

                // FIXME: does any of this here leak?
                if (privateKeyPassword == NULL)
                {
                    mPasswordResolver = new SinglePasswordResolver(keystorePassword);
                }
                else
                {
                    mPasswordResolver = new SinglePasswordResolver(*privateKeyPassword);
                }

                // Parse the keystore
                parse(buffer, keystoreLength);
            }

            void JKSKeystore::parse(const unsigned char *data, std::size_t length)
            {
                // Parse the start of the data in memory as the keystore header
                const JKSKeystoreHeaderFormat *header = reinterpret_cast<const JKSKeystoreHeaderFormat*>(data);

                // Grab values from the header to be checked.
                uint32_t magic = fourByteToUnsignedInt(header->magic);
                uint32_t version = fourByteToUnsignedInt(header->version);
                uint32_t count = fourByteToUnsignedInt(header->count);

                // Check the magic and version numbers
                if (magic != KEYSTORE_MAGIC_NUMBER)
                {
                    throw KeystoreException(KEYSTORE_ERROR_CORRUPT_MAGIC);
                }
                if (KEYSTORE_UNEXPECTED_VERSION(version))
                {
                    throw KeystoreException(KEYSTORE_ERROR_BAD_VERSION);
                }

                // For 'count' entries, parse the entry.
                const unsigned char *ptr = &(header->remain[0]);
                for (uint32_t i = 0; i < count; ++i)
                {
                    std::size_t remainingLength = length - (ptr - data);
                    ptr = parseKeyEntry(ptr, remainingLength);
                }

                // Interpret the last part of the file as the signature.
                const JKSSignature *sig = reinterpret_cast<const JKSSignature*>(ptr);

                // Calculate the signature based on the data that was processed.
                unsigned int hashLength;
                unsigned char hashValue[20];

                const EVP_MD *hashType = EVP_sha1();
                EVP_MD_CTX *hashContext = EVP_MD_CTX_create();
                EVP_MD_CTX_init(hashContext);
                EVP_DigestInit_ex(hashContext, hashType, NULL);
                // Hash password + magic + keystore data to get signature
                EVP_DigestUpdate(hashContext, mKeystorePassword, mKeystorePasswordLength);
                EVP_DigestUpdate(hashContext, KEYSTORE_MAGIC_STRING, strlen(KEYSTORE_MAGIC_STRING));
                EVP_DigestUpdate(hashContext, data, (ptr - data));
                EVP_DigestFinal_ex(hashContext, hashValue, &hashLength);
                EVP_MD_CTX_destroy(hashContext);

                // Check the computed hash against the signature
                for (std::size_t i = 0; i < 20; ++i)
                {
                    if (hashValue[i] != sig->value[i])
                    {
                        throw KeystoreException(KEYSTORE_ERROR_CORRUPT_SIGNATURE);
                    }
                }
            }

            const unsigned char* JKSKeystore::parseKeyEntry(const unsigned char *data, std::size_t length)
            {
                if (length < sizeof(JKSEntry)) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                // Interpret the start of this entry as an "entry" and grab the type
                const JKSEntry *entry = reinterpret_cast<const JKSEntry*>(data);
                uint32_t entryType = fourByteToUnsignedInt(entry->type);

                std::size_t remainingLength = length - (entry->alias - data);
                if (remainingLength < sizeof(JKSAliasEntry)) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                // All entries have an alias, so parse and store the alias
                const JKSAliasEntry *alias = reinterpret_cast<const JKSAliasEntry*>(entry->alias);
                uint16_t aliasLength = twoByteToUnsignedInt(alias->length);

                remainingLength = length - (alias->data - data);
                if (remainingLength < aliasLength) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                std::string keyAlias(reinterpret_cast<const char*>(alias->data), static_cast<uint32_t>(aliasLength));

                const unsigned char *next = &(alias->data[aliasLength]);
                remainingLength = length - (next - data);

                // All entries have a creation time. Parse that
                uint64_t creationTime = eightByteToUnsignedInt(next);
                next += sizeof(creationTime);
                remainingLength -= sizeof(creationTime);

                // Check the type of the entry
                if (entryType == KEYSTORE_ENTRY_PRIVATE_KEY)
                {
                    if (remainingLength < sizeof(JKSDataEntry)) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);
                    // Grab the data entry
                    const JKSDataEntry *pkeyEntry = reinterpret_cast<const JKSDataEntry*>(next);

                    // Get the length of the data entry
                    uint32_t pkeyLength = fourByteToUnsignedInt(pkeyEntry->length);

                    remainingLength = length - (pkeyEntry->data - data);
                    if (remainingLength < pkeyLength) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                    // And parse it as a private key.
                    parsePrivateKey(keyAlias, creationTime, pkeyEntry->data, pkeyLength);

                    next = &(pkeyEntry->data[pkeyLength]);

                    remainingLength = length - (next - data);
                    if (remainingLength < sizeof(uint32_t)) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                    // Get the number of certificates that follow this private key
                    uint32_t certCount = fourByteToUnsignedInt(next);

                    next += sizeof(uint32_t);
                    for (int i = 0; i < certCount; ++i)
                    {
                        // Parse each cert in the chain.
                        next = parseTrustedCertEntry(keyAlias, creationTime, next, remainingLength, true);
                    }

                    // Return the pointer to the first byte after everything that was processed.
                    return next;
                }
                else if (entryType == KEYSTORE_ENTRY_TRUSTED_CERT)
                {
                    // Parse the current as a trusted cert entry
                    return parseTrustedCertEntry(keyAlias, creationTime, next, remainingLength);
                }
                else
                {
                    // Unknown entry type is an error condition. We can't continue.
                    throw KeystoreException(KEYSTORE_ERROR_UNKNOWN_ENTRY_TYPE);
                }
            }

            void JKSKeystore::parsePrivateKey(const std::string& alias, uint64_t creationTime, const unsigned char *data, uint32_t length)
            {
                JKS_PKEYINFO *pkeyInfo = NULL;
                // Parse the ASN.1 structure of the JKS private key.
                // Early versions of OpenSSL ASN.1 parsing need a const workaround.
#if OPENSSL_VERSION_NUMBER < 0x000908000
                d2i_JKS_PKEYINFO( &pkeyInfo, const_cast<unsigned char**>(&data), length );
#else //OPENSSL_VERSION_NUMBER < 0.9.8
                d2i_JKS_PKEYINFO(&pkeyInfo, &data, length);
#endif //OPENSSL_VERSION_NUMBER < 0.9.8

                struct pkeyInfoCleanupStruct
                {
                public:
                    pkeyInfoCleanupStruct(JKS_PKEYINFO *pkeyInfo) : _pkeyInfo(pkeyInfo) {}
                    ~pkeyInfoCleanupStruct() { if (_pkeyInfo != NULL) JKS_PKEYINFO_free(_pkeyInfo); }
                private:
                    JKS_PKEYINFO *_pkeyInfo;
                } pkeyInfoCleanup(pkeyInfo);

                // Find the key length and allocate a space for it.
                std::size_t encryptedLength = ASN1_STRING_length(pkeyInfo->encryptedData);
                std::size_t keyLength = encryptedLength - 40;
                unsigned char *key = new unsigned char[keyLength];
                mFreeList.push_back(key);

                // Get the crypto values out of the octet string
                const unsigned char *seed = ASN1_STRING_data(pkeyInfo->encryptedData);
                const unsigned char *ekey = &(seed[20]);
                const unsigned char *checksum = &(seed[20 + keyLength]);
                unsigned char hashValue[20];
                unsigned int hashLength;

                // Initialize the vector and hash function
                std::memcpy(hashValue, seed, sizeof(hashValue));

                const EVP_MD *hashType = EVP_sha1();
                EVP_MD_CTX *hashContext = EVP_MD_CTX_create();
                EVP_MD_CTX_init(hashContext);

                const unsigned char *password;
                std::size_t passwordLength;

                if (!mPasswordResolver->getPrivateKeyPassword(alias, &passwordLength, &password))
                {
                    // No password given. Don't try to use the private key.
                    return;
                }

                for (std::size_t i = 0; i < keyLength;)
                {
                    // Update the hash for this step
                    EVP_DigestInit_ex(hashContext, hashType, NULL);
                    EVP_DigestUpdate(hashContext, password, passwordLength);
                    EVP_DigestUpdate(hashContext, hashValue, sizeof(hashValue));
                    EVP_DigestFinal_ex(hashContext, hashValue, &hashLength);

                    // XOR it against the key
                    for (std::size_t j = 0; j < sizeof(hashValue) && i < keyLength; ++j)
                    {
                        key[i] = (hashValue[j] ^ ekey[i]);
                        ++i;
                    }
                }

                // Hash the key to validate that it was decrypted correctly.
                EVP_DigestInit_ex(hashContext, hashType, NULL);
                EVP_DigestUpdate(hashContext, password, passwordLength);
                EVP_DigestUpdate(hashContext, key, keyLength);
                EVP_DigestFinal_ex(hashContext, hashValue, &hashLength);
                EVP_MD_CTX_destroy(hashContext);

                for (std::size_t i = 0; i < sizeof(hashValue); ++i)
                {
                    if (hashValue[i] != checksum[i])
                    {
                        // Hash didn't match. Wrong password / corrupt
                        throw KeystoreException(KEYSTORE_ERROR_CORRUPT_KEY);
                    }
                }

                JKSPrivateKeyData &pkeyData(mPKeyDataMap[alias]);
                pkeyData.creationTimeMillis = creationTime;
                pkeyData.alias = alias;
                pkeyData.len = keyLength;
                pkeyData.data = key;
            }

            const unsigned char* JKSKeystore::parseTrustedCertEntry(const std::string& alias, uint64_t creationTime, const unsigned char *data, uint32_t length, bool chain)
            {
                if (length < sizeof(JKSAliasEntry)) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);
                // Trusted cert has an encoding name, stored the same way as an alias. Parse that.
                const JKSAliasEntry *encodingEntry = reinterpret_cast<const JKSAliasEntry*>(data);

                uint16_t encodingLength = twoByteToUnsignedInt(encodingEntry->length);

                uint32_t remainingLength = length - (encodingEntry->data - data);
                if (remainingLength < encodingLength) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                std::string encoding(reinterpret_cast<const char*>(encodingEntry->data), static_cast<std::size_t>(encodingLength));
                const unsigned char *next = &(encodingEntry->data[encodingLength]);

                remainingLength = length - (next - data);
                if (remainingLength < sizeof(JKSDataEntry)) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                // The remainder of the entry is the certificate data.
                const JKSDataEntry *certEntry = reinterpret_cast<const JKSDataEntry*>(next);

                uint32_t certLength = fourByteToUnsignedInt(certEntry->length);

                remainingLength = length - (certEntry->data - data);
                if (remainingLength < certLength) throw KeystoreException(KEYSTORE_ERROR_UNEXPECTED_EOF);

                // Parse the certificate data.
                parseTrustedCert(alias, creationTime, encoding, certEntry->data, certLength, chain);

                return &(certEntry->data[certLength]);
            }

            void JKSKeystore::parseTrustedCert(const std::string& alias, uint64_t creationTime, const std::string& algorithm, const unsigned char *data, uint32_t length, bool chain)
            {
                unsigned char *cert = new unsigned char[length];
                mFreeList.push_back(cert);
                std::memcpy(cert, data, length);

                JKSTrustedCertData certData;
                certData.creationTimeMillis = creationTime;
                certData.alias = alias;
                certData.algorithm = algorithm;
                certData.len = length;
                certData.data = cert;

                if (chain)
                {
                    mCertChainMap[alias].push_back(certData);
                }
                else
                {
                    mCertDataMap[alias] = certData;
                }
            }

            JKSKeystore::PrivateKeyPasswordResolver::~PrivateKeyPasswordResolver()
            {
            }

            JKSKeystore::SinglePasswordResolver::SinglePasswordResolver(const std::string& password) :
                mData(nullptr),
                mLength(2 * password.length())
            {
                mData = new unsigned char[mLength];
                // UTF-16BE encoding of an ASCII string is simple.
                for (int i = 0; i < mLength; i += 2)
                {
                    mData[i] = 0;
                    mData[i + 1] = password.at(i / 2);
                }
            }

            JKSKeystore::SinglePasswordResolver::~SinglePasswordResolver()
            {
                delete[] mData;
                mData = nullptr;
            }

            bool JKSKeystore::SinglePasswordResolver::getPrivateKeyPassword(const std::string& alias, std::size_t* length, const unsigned char** data)
            {
                *length = mLength;
                *data = mData;
                return true;
            }

            JKSPrivateKeyData::JKSPrivateKeyData() :
                data(nullptr)
            {
            }

            JKSPrivateKeyData::JKSPrivateKeyData(const JKSPrivateKeyData& other) :
                creationTimeMillis(other.creationTimeMillis),
                alias(other.alias),
                len(other.len),
                data(other.data)
            {
            }

            JKSPrivateKeyData& JKSPrivateKeyData::operator=(const JKSPrivateKeyData& other)
            {
                creationTimeMillis = other.creationTimeMillis;
                alias = other.alias;
                len = other.len;
                data = other.data;

                return *this;
            }

            JKSPrivateKeyData::~JKSPrivateKeyData()
            {
            }

            JKSTrustedCertData::JKSTrustedCertData() :
                creationTimeMillis(0),
                alias(),
                algorithm(),
                len(0),
                data(nullptr)
            {
            }

            JKSTrustedCertData::JKSTrustedCertData(const JKSTrustedCertData& other) :
                creationTimeMillis(other.creationTimeMillis),
                alias(other.alias),
                algorithm(other.algorithm),
                len(other.len),
                data(other.data)
            {
            }

            JKSTrustedCertData& JKSTrustedCertData::operator=(const JKSTrustedCertData& other)
            {
                creationTimeMillis = other.creationTimeMillis;
                alias = other.alias;
                algorithm = other.algorithm;
                len = other.len;
                data = other.data;

                return *this;
            }

            JKSTrustedCertData::~JKSTrustedCertData()
            {
            }

}
