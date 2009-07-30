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
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: 03/12/2008
 * 
 * Purpose: 
 */

package com.qut.middleware.esoe.sso.plugins.artifact;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.qut.middleware.esoe.sso.plugins.artifact.bean.Artifact;
import com.qut.middleware.esoe.sso.plugins.artifact.exception.ArtifactBindingException;

public class ArtifactTest
{
	// 00 04 00[42 times] in base64
	private String blankArtifact = "AAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
	
	/* 00 04 13 37 
	 * 00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 12 13
	 * 20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F 30 31 32 33
	 * in base64
	 * 
	 * generated with:
	 * perl -e 'my @x = (0x00, 0x04, 0x13, 0x37,
	 *  0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13,
	 *  0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33);
	 *  foreach $i (@x) { print chr($i); }' | base64
	 */
	private String goodArtifact = "AAQTNwABAgMEBQYHCAkKCwwNDg8QERITICEiIyQlJicoKSorLC0uLzAxMjM=";
	private byte[] goodSourceID = new byte[]{0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 0x10, 0x11, 0x12, 0x13};
	private byte[] goodMessageHandle = new byte[]{0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30, 0x31, 0x32, 0x33};
	private int goodIndex = 0x1337;
	
	// type id = 0x0410
	private String badArtifact1 = "BBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
	// too short for an artifact
	private String badArtifact2 = "AAQAAAAA";
	// too short for type id 0x0004
	private String badArtifact3 = "AAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	// too long for type id 0x0004
	private String badArtifact4 = "AAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	private byte[] badSourceID = new byte[]{};
	private byte[] badMessageHandle = new byte[]{};
	
	private String audience = "http://spep.example.com";
	private byte[] document = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	@Test
	public void testBlankArtifact1() throws Exception
	{
		Artifact blank = new Artifact(blankArtifact);
		byte[] twentyZeroes = new byte[]
		                               {
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0
		                               };
		
		assertEquals(4, blank.getType());
		assertEquals(0, blank.getIndex());
		assertArrayEquals("invalid message handle", twentyZeroes, blank.getMessageHandle());
		assertArrayEquals("invalid source id", twentyZeroes, blank.getSourceID());
	}
	
	@Test
	public void testBlankArtifact2() throws Exception
	{
		byte[] twentyZeroes = new byte[]
		                               {
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0
		                               };
		Artifact blank = new Artifact(0, twentyZeroes, twentyZeroes, audience, document);
		
		assertEquals(blankArtifact, blank.toBase64Artifact());
	}
	
	@Test
	public void testGoodArtifact1() throws Exception
	{
		Artifact good = new Artifact(goodArtifact);
		
		assertEquals(4, good.getType());
		assertEquals(goodIndex, good.getIndex());
		assertArrayEquals("invalid message handle", goodMessageHandle, good.getMessageHandle());
		assertArrayEquals("invalid source id", goodSourceID, good.getSourceID());
	}
	
	@Test
	public void testGoodArtifact2() throws Exception
	{
		Artifact good = new Artifact(goodIndex, goodSourceID, goodMessageHandle, audience, document);
		
		assertEquals(goodArtifact, good.toBase64Artifact());
	}
	
	@Test(expected = ArtifactBindingException.class)
	public void testBadArtifact1() throws Exception
	{
		new Artifact(badArtifact1);
	}

	@Test(expected = ArtifactBindingException.class)
	public void testBadArtifact2() throws Exception
	{
		new Artifact(badArtifact2);
	}

	@Test(expected = ArtifactBindingException.class)
	public void testBadArtifact3() throws Exception
	{
		new Artifact(badArtifact3);
	}

	@Test(expected = ArtifactBindingException.class)
	public void testBadArtifact4() throws Exception
	{
		new Artifact(badArtifact4);
	}

	@Test(expected = ArtifactBindingException.class)
	public void testBadArtifact5() throws Exception
	{
		new Artifact(goodIndex, badSourceID, goodMessageHandle, audience, document);
	}

	@Test(expected = ArtifactBindingException.class)
	public void testBadArtifact6() throws Exception
	{
		new Artifact(goodIndex, goodSourceID, badMessageHandle, audience, document);
	}

}
