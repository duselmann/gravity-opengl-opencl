// Copyright (c) 2022 David Uselmann
package org.davu.opencl.utils;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.junit.Test;

/**
 * Purpose: I created an OpenCL code translations class, CLCodes.java
 * Its purpose is to translate the numbers GPU returns into somewhat
 * human readable information.
 *
 * Of course, this tests that translations are working.
 *
 * @author davu
 */
public class CLCodesTest {

    @Test
    public void shift() {
        int value = CLCodes.shift("1 << 0");
        assertEquals("left shift by 0", 1, value);
        value = CLCodes.shift("1 << 2");
        assertEquals("left shift by 2", 4, value);
    }
    @Test
    public void hex() {
        int value = CLCodes.hex("0x0F");
        assertEquals("upper case test", 15, value);
        value = CLCodes.hex("0x0f");
        assertEquals("lower case test", 15, value);
        value = CLCodes.hex("0xFFFFFFFF");
        assertEquals("Special HUGE value", -1, value);
    }
    @Test
    public void parseNumber() {
        int value = CLCodes.parseNumber("1 << 0");
        assertEquals("left shift by 0", 1, value);
        value = CLCodes.parseNumber("1 << 2");
        assertEquals("left shift by 2", 4, value);
        value = CLCodes.parseNumber("0x0F");
        assertEquals("upper case test", 15, value);
        value = CLCodes.parseNumber("0x0f");
        assertEquals("lower case test", 15, value);
        value = CLCodes.parseNumber("0xFFFFFFFF");
        assertEquals("Special HUGE value", -1, value);
        value = CLCodes.parseNumber("1");
        assertEquals(1, value);
        value = CLCodes.parseNumber("-1");
        assertEquals(-1, value);
    }
    @Test
    public void parseNumber_BAD_CODE_NUMBER() {
        int value = CLCodes.parseNumber("1sdf1");
        assertEquals("bad code number", CLCodes.BAD_CODE_NUMBER, value);
    }

    @Test
    public void excludeLine() {
        boolean excluded = CLCodes.excludeLine("// comment lines are excluded.");
        assertTrue(excluded);
        excluded = CLCodes.excludeLine("/* comment lines are excluded. */");
        assertTrue(excluded);
        excluded = CLCodes.excludeLine("close comment lines are excluded. */");
        assertTrue(excluded);
        excluded = CLCodes.excludeLine(CLCodes.clean("         "));
        assertTrue("blank lines are excluded - via clean", excluded);
        excluded = CLCodes.excludeLine("");
        assertTrue("empty lines are excluded", excluded);
        excluded = CLCodes.excludeLine("public lines are excluded.");
        assertTrue(excluded);
    }
    @Test
    public void clean() {
        String expect = "leading and trailing whitespace is removed";
        String cleaned = CLCodes.clean("\t "+expect+" \t  ");
        assertEquals(expect, cleaned);
        cleaned = CLCodes.clean(expect+",");
        assertEquals("commas are removed", expect, cleaned);
        cleaned = CLCodes.clean(expect+";");
        assertEquals("semicolons are removed", expect, cleaned);
    }
    @Test
    public void makeEntry() {
        String line = "STRING = 1";
        Entry<String, String> entry = CLCodes.makeEntry(line);
        assertEquals("1", entry.getKey());
        assertEquals("STRING", entry.getValue());
    }
    @Test
    public void getString() {
        String entry = CLCodes.getString(-9999);
        assertEquals("UNKNOWN:-9999", entry);
        entry = CLCodes.getString(-37);
        assertEquals("CL_INVALID_HOST_PTR", entry);
    }
    @Test
    public void putCode() {
        int code = -99999;

        String entry = CLCodes.getString(code);
        assertEquals("UNKNOWN:"+code, entry);

        String newCode = "TEST_NEW_CODE";
        CLCodes.putCode(code, newCode);
        entry = CLCodes.getString(code);
        assertEquals(newCode, entry);

        String anotherCode = "TEST_ANOTHER_CODE";
        CLCodes.putCode(code, anotherCode);
        entry = CLCodes.getString(code);
        assertTrue(entry.contains(anotherCode));
        assertTrue(entry.contains(newCode));

        assertEquals(anotherCode+", "+newCode, entry);
    }
    @Test
    public void separator() {
        int code = -88888;

        String entry = CLCodes.getString(code);
        assertEquals("UNKNOWN:-88888", entry);

        String newCode = "TEST_NEW_CODE";
        CLCodes.putCode(code, newCode);

        String anotherCode = "TEST_ANOTHER_CODE";
        CLCodes.putCode(code, anotherCode);

        CLCodes.useNewLine();
        entry = CLCodes.getString(code);
        assertEquals(anotherCode+"\n"+newCode, entry);

        CLCodes.setSeparator(":");
        entry = CLCodes.getString(code);
        assertEquals(anotherCode+":"+newCode, entry);

        CLCodes.useComma();
        entry = CLCodes.getString(code);
        assertEquals(anotherCode+", "+newCode, entry);
    }

}
