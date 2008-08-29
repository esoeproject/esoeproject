/* Copyright 2008, Queensland University of Technology
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
 */
package com.qut.middleware.esoemanager.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qut.middleware.saml2.identifier.impl.Messages;

public class PolicyIDGenerator
{
	private ReentrantLock lock;
	private SecureRandom random;
	private final String RNG = "SHA1PRNG";
	
	/* Local logging instance */
	private Logger logger = LoggerFactory.getLogger(PolicyIDGenerator.class.getName());
	
	public PolicyIDGenerator()
	{
		this.lock = new ReentrantLock();

		try
		{
			/* Attempt to get the specified RNG instance */
			this.random = SecureRandom.getInstance(this.RNG);
		}
		catch (NoSuchAlgorithmException nsae)
		{
			this.logger.error(Messages.getString("IdentifierGeneratorImpl.13")); //$NON-NLS-1$
			this.logger.debug(nsae.getLocalizedMessage(), nsae);
			this.random = new SecureRandom();
		}
		
		this.random.setSeed(System.currentTimeMillis());
	}
	
	public String generatePolicyID()
	{
		String id = "lxacmlpolicy:access:" + generate(6) + ":" + System.currentTimeMillis();
		return id;
	}

	/**
	 * Generates the specified number of random bytes using SecureRandom
	 * 
	 * @param length
	 *            The number of random bytes to generate
	 * @return The generated random string
	 */
	private String generate(int length)
	{
		String id;
		byte[] buf;
		buf = new byte[length];

		this.lock.lock();
		try
		{
			/* Seed the specified RNG instance and get bytes */
			this.random.setSeed(Thread.currentThread().getName().getBytes());
			this.random.setSeed(System.currentTimeMillis());
			this.random.nextBytes(buf);
		}
		finally
		{
			this.lock.unlock();
		}

		id = new String(Hex.encodeHex(buf));

		return id;
	}

}
