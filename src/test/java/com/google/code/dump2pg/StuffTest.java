/**
 * This file is part of dump2pg.
 * 
 * dump2pg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * dump2pg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with dump2pg. If not, see <http://www.gnu.org/licenses/>.
 */
package com.google.code.dump2pg;

import static com.google.code.dump2pg.Stuff.*;

import static org.junit.Assert.*;

import org.junit.Test;

public class StuffTest {

	@Test
	public void testToLong() {
		assertEquals(0L, toLong((byte) 0));
		assertEquals(128L, toLong(Byte.MIN_VALUE));
		assertEquals(127L, toLong(Byte.MAX_VALUE));
	}

	@Test
	public void testToOctalString() {
		assertEquals("000", toOctalString(0));
		assertEquals("001", toOctalString(1));
		assertEquals("002", toOctalString(2));
		assertEquals("011", toOctalString(9));
		assertEquals("012", toOctalString(10));
		assertEquals("144", toOctalString(100));
		assertEquals("310", toOctalString(200));
		assertEquals("454", toOctalString(300));
		assertEquals("620", toOctalString(400));
		assertEquals("100", toOctalString(Integer.highestOneBit(127)));
		assertEquals("001", toOctalString(Integer.lowestOneBit(127)));
	}

	@Test
	public void testEscapeString() {
		assertEquals("a\nb", "a\nb");
	}

	@Test
	public void testEscapeBytes() {
		assertEquals("\\\\000", escapeBytes(new byte[] {0}));
		assertEquals("\\\\001", escapeBytes(new byte[] {1}));
		assertEquals("\\\\011", escapeBytes(new byte[] {9}));
		assertEquals("\\\\012", escapeBytes(new byte[] {10}));
		assertEquals("\\\\001\\\\002\\\\003", escapeBytes(new byte[] {1, 2, 3}));
		assertEquals("\\\\177\\\\176\\\\175", escapeBytes(new byte[] {127, 126, 125}));
	}

	@Test
	public void testEscapeByte() {
		assertEquals("\\\\000", escapeByte((byte) 0));
		assertEquals("\\\\001", escapeByte((byte) 1));
		assertEquals("\\\\011", escapeByte((byte) 9));
		assertEquals("\\\\012", escapeByte((byte) 10));
		assertEquals("\\\\200", escapeByte(Byte.MIN_VALUE));
		assertEquals("\\\\177", escapeByte(Byte.MAX_VALUE));
	}

}
