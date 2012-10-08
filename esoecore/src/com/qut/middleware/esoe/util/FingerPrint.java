package com.qut.middleware.esoe.util;

import com.qut.middleware.esoe.sessions.exception.SessionCacheUpdateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * Able to generate and store fingerprints in a redis data store for comparison.
 */

public class FingerPrint {


    JedisPool pool = new JedisPool("localhost");
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());



    public FingerPrint() {

    }

    public FingerPrint(String username, String password) {


    }

    public FingerPrint(String hostname, String port, String username, String password) {


    }

    /**
     * Performs all the required tasks to generate and check a fingerprint from the given session ID and fingerprint
     * material. Ie:
     * <p/>
     * It generates a fingerprint, checks the fingerprint against stored (if it is already stored) and saves the
     * fingerprint if not stored.
     *
     * @param sessionId       The ESOE session ID, whoich will be used as the key to store the generated fingerprint.
     * @param fingerprintData The string material to be used to generate a fingerprint.
     */
    public boolean assertFingerprintCheck(String sessionId, String fingerprintData) throws SessionCacheUpdateException {

        String fingerprint = generateFingerprint(fingerprintData);

        if (fingerprint != null) {

            Jedis printstore = new Jedis("localhost");
            try {

                printstore = pool.getResource();
                if (!printstore.exists(sessionId)) {

                    logger.debug("No key found for {}", sessionId);
                    if (!saveFingerprint(sessionId, fingerprint)) {
                        throw new SessionCacheUpdateException("Unable to save fingerprint to fingerprint store");
                    }
                }

                return checkFingerprint(sessionId, fingerprint);

            } catch (Exception e) {

                logger.error("Error accessing fingerprint store {}", e.getMessage());
                throw new SessionCacheUpdateException("Unable to access fingerprint store");

            } finally {

                pool.returnResource(printstore);
            }
        }

        return false;
    }


    /**
     * Generates a fingerprint for the given string.
     *
     * @return A fingerprint string if data is not null, else null.
     */
    public String generateFingerprint(String data) {

        String value = null;

        if (data != null) {

            try {

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(data.getBytes());
                value = bytesToHexString(hash);
                logger.info("Generated fingerprint of {} from {}", value, data);

            } catch (NoSuchAlgorithmException e) {

                logger.error(e.getMessage());
            }
        }

        return value;
    }


    /**
     * Checks the given fingerprint against fingerprint stored against the given session key (if any)
     *
     * @param key         The ESOE session ID used as a key to store the given fingerprint
     * @param fingerprint The fingerprint string to check
     * @return True if the key exists and the corresponding value equals the given fingerprint, else false.
     */
    public boolean checkFingerprint(String key, String fingerprint) {

        // redis will die if given null values
        if (key == null || fingerprint == null) {
            throw new InvalidParameterException("Key and fingerprint cannot be null");
        }

        Jedis printStore = pool.getResource();
        try {

            String value = printStore.get(key);
            if (value != null) {
                logger.debug("Fingerprint found. Checking {} against {}", value, fingerprint);
                return value.equals(fingerprint);
            } else {
                logger.debug("No fingerprint found for session ID {}", key);
            }

        } catch (Exception e) {

            logger.error("Error accessing fingerprint store {}", e.getMessage());

        } finally {

            pool.returnResource(printStore);
        }

        return false;
    }


    /**
     * Save the given key/fingerprint pair.
     *
     * @param key
     * @param fingerprint
     * @return True if the given key/fingerprint pair is successfully saved, else false.
     */
    public boolean saveFingerprint(String key, String fingerprint) {

        // redis will die if given null values
        if (key == null || fingerprint == null) {
            throw new InvalidParameterException("Key and fingerprint cannot be null");
        }

        Jedis printStore = pool.getResource();
        try {

            if (!printStore.exists(key)) {

                logger.debug("Saving fingerprint {} under key {}", fingerprint, key);
                printStore.set(key, fingerprint);

            } else {

                logger.error("Session key {} already exists in fingerprint Store. Save failed", key);
                return false;
            }
        } catch (Exception e) {

            logger.error("Error accessing fingerprint store {}", e.getMessage());
            return false;

        } finally {

            pool.returnResource(printStore);
        }

        return true;
    }


    /**
     * Delete the given key.
     *
     * @param key
     * @return True if the key does not exists, or exists and was successfully deleted.
     */
    public boolean deleteFingerprint(String key) {

        // redis will die if given null values
        if (key == null) {
            throw new InvalidParameterException("Fingerprint key cannot be null");
        }

        Jedis printStore = pool.getResource();
        try {

            logger.info("Removing key {} from fingerprint store", key);
            printStore.del(key);

        } catch (Exception e) {

            logger.error("Error accessing fingerprint store {}", e.getMessage());
            return false;

        } finally {

            pool.returnResource(printStore);
        }

        return true;
    }


    private String bytesToHexString(byte[] bytes) {

        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString();
    }
}