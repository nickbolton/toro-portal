/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version
 * 2 of the GPL, you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */

package net.unicon.alchemist.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Perform a digest on a string.
 * This is a simplification wrapper around the MessageDigest API.
 */
public class Digest {

    private static final String DEFAULT_DIGEST = "SHA";

    public static String digest(String input) {
        return digest(input.getBytes());
    }

    public static String digest(byte[] input) {
        return digest(input, DEFAULT_DIGEST);
    }

    public static String digest(String input, String type) {
        return digest(input.getBytes(), type);
    }

    public static String digest(byte[] input, String type) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(
                    "Unable to acquire MessageDigest instance of type: "+type,
                    ex);
        }

        md.update(input);

        return toHex(md.digest());
    }

    private static String toHex(byte[] b) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < b.length; i++) {
            char highNibble = kHexChars[(b[i] & 0xF0) >> 4];
            char lowNibble = kHexChars[b[i] & 0x0F];

            buf.append(highNibble);
            buf.append(lowNibble);
        }

        return buf.toString();
    }

    private static final char kHexChars[] =
    { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: "+Digest.class.getName()+" <string>");
            System.exit(2);
        }

        System.out.println("Input: "+args[0]);
        System.out.println("Digest: "+Digest.digest(args[0]));
    }
}
