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
 * Creation Date: 01/07/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.metadata.bean;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.*;

import com.qut.middleware.crypto.impl.KeystoreResolverImpl;

public class KeystoreResolverTest
{
	private static String keystorePath = "tests/testdata/testKeystore.ks";
	private static String keystorePassword = "kspass";
	private static String keyPassword = "keypass";
	private static String keyAlias = "testpriv";

	@Test
	public void testLoadKeystore() throws Exception
	{
		new KeystoreResolverImpl(new File(keystorePath), keystorePassword, keyAlias, keyPassword);
	}
}
