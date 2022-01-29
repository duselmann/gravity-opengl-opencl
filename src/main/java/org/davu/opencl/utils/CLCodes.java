// Copyright (c) 2022 David Uselmann
package org.davu.opencl.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opencl.CL10;

/**
 * This class loads the constants file, ignores all but the definitions, and loads
 * them as integer lookups. It is be used to display a string for the OpenCL codes.
 *
 * The way this is designed, the constants definitions in CL10 can be copied into
 * the src/main/resources/cl/const.txt file without any more tedious manipulation.
 *
 * When codes share the same number all string values are returned in sorted order.
 * This way the structure does not have to perform extra grouping logic or know what
 * the cause was for the lookup. It will be up to the user to find the relevant string.
 * This is not as onerous as one might think. Typically, there should be few errors
 * while writing new code and additionally the context of the error will be the call
 * that created it. For example, a call to a device info lookup might return -1, but
 * so could could a call to build a kernel program. The return will include both
 * CL_BUILD_NONE and CL_DEVICE_NOT_FOUND but it is clear here the error context is in
 * the string. The values are sorted to improve the discovery for the few shared codes.
 *
 *
 * @author davu
 */
public class CLCodes {
    static final Logger LOGGER = LogManager.getLogger(CLCodes.class);

    static final HashMap<Integer,List<String>> codesDictionary;

    static final String SEPARATOR_DEFAULT = ", ";
    static final String SEPARATOR_NEWLINE = "\n";
    // at this time there is no OpenCL code -99
    static final int BAD_CODE_NUMBER = -99;

    static String separator = SEPARATOR_DEFAULT;


    public static String getString(int code) {
        if (codesDictionary.containsKey(code)) {
            return codesDictionary.get(code)
                    .stream()
                    .collect(Collectors.joining(separator));
        }
        return "UNKNOWN:"+code;
    }

    public static void useComma() {
        setSeparator(SEPARATOR_DEFAULT);
    }
    public static void useNewLine() {
        setSeparator(SEPARATOR_NEWLINE);
    }
    public static void setSeparator(String joining) {
        separator = joining;
    }


    static {
        codesDictionary = new HashMap<>();

        try (InputStream is = CLCodes.class.getClassLoader().getResourceAsStream("cl/const.txt")) {

            Reader reader = new InputStreamReader(is);
            BufferedReader buf = new BufferedReader(reader);

            String line;
            while ((line =buf.readLine()) != null) {
                line = clean(line);
                if (excludeLine(line)) {
                    continue;
                }
                Map.Entry<String,String> entry = makeEntry(line);
                int key = parseNumber(entry.getKey());
                if (key == BAD_CODE_NUMBER) {
                    LOGGER.warn("OpenCL constants code cannot be parsed '{}'", line);
                }
                putCode(key, entry.getValue());
            }

        } catch (IOException e) {

            throw new RuntimeException("Cannot find constants string file.", e);
        }


    }

    static void putCode(int key, String value) {
        List<String> values = codesDictionary.get(key);
        if (values == null) {
            values = new ArrayList<>();
            codesDictionary.put(key, values);
        }
        values.add(value);
        // sort for readability when there are many, like -1 or 0
        if (values.size()>1) {
            Collections.sort(values);
        }
    }

    static boolean excludeLine(String line) {
        // this does not handle multiple line comments greater than 2
        return line.length() == 0
                || line.startsWith("//")
                || line.startsWith("/*")
                || line.contains("*/")
                || line.startsWith("public");
    }
    static String clean(String line) {
        line = line.replace(',', ' ');
        line = line.replace(';', ' ');
        line = line.trim();
        return line;
    }
    static Map.Entry<String,String> makeEntry(String line) {
        String[] parts = line.split("=");
        Map<String,String> entry = new HashMap<>();
        entry.put(parts[1].trim(), parts[0].trim());
        return entry.entrySet().iterator().next();
    }

    static int parseNumber(String number) {
        try {
            if (number.contains("<<")) {
                return shift(number);
            }
            if (number.startsWith("0x")) {
                return hex(number);
            }
            return Integer.parseInt(number);
        } catch(Exception e) {
            return BAD_CODE_NUMBER;
        }
    }

    static int hex(String number) {
        if (number.equals("0xFFFFFFFF")) {
            return -1;
        }
        return Integer.parseInt(number.substring(2), 16);
    }

    static int shift(String number) {
        String[] parts = number.split("<<");
        int base = parseNumber(clean(parts[0]));
        int shift = parseNumber(clean(parts[1]));
        return base << shift;
    }


    public static String getEventStatusString(int status) {
        switch (status) {
        case CL10.CL_QUEUED:
            return "CL_QUEUED";
        case CL10.CL_SUBMITTED:
            return "CL_SUBMITTED";
        case CL10.CL_RUNNING:
            return "CL_RUNNING";
        case CL10.CL_COMPLETE:
            return "CL_COMPLETE";
        default:
            throw new IllegalArgumentException(String.format("Unknown event status: 0x%X", status));
        }
    }
}
