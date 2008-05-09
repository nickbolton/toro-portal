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
package net.unicon.sdk.authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

public class PasswordUtil {

    public static String createHash(String value)
    throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(value.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<hash.length; i++) {
            sb.append(Integer.toHexString(0xFF & hash[i]));
        }
        return sb.toString();
    }

    public static String createPassword(String rawPassword) throws NoSuchAlgorithmException {
        String encryptPass = null;
        if (rawPassword != null || rawPassword.trim().length() <= 0) {
            byte[] hash, rnd = new byte[8], fin = new byte[24];
            Long date = new Long((new Date()).getTime());
            SecureRandom r = new SecureRandom((date.toString()).getBytes());
            MessageDigest md = MessageDigest.getInstance("MD5");
            r.nextBytes(rnd);
            md.update(rnd);
            hash = md.digest(rawPassword.getBytes());
            System.arraycopy(rnd, 0, fin, 0, 8);
            System.arraycopy(hash, 0, fin, 8, 16);
            encryptPass = "(MD5)" + encode(fin);
        }
        return encryptPass;
    }
    private static String encode(byte[] raw) {
        StringBuffer encoded = new StringBuffer();
        for (int i = 0; i < raw.length; i += 3) {
            encoded.append(encodeBlock(raw, i));
        }
        return encoded.toString();
    }
    private static char[] encodeBlock(byte[] raw, int offset) {
        int block = 0;
        int slack = raw.length - offset - 1;
        int end = (slack >= 2) ? 2 : slack;
        for (int i = 0; i <= end; i++) {
            byte b = raw[offset + i];
            int neuter = (b < 0) ? b + 256 : b;
            block += neuter << (8 * (2 - i));
        }
        char[] base64 = new char[4];
        for (int i = 0; i < 4; i++) {
            int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
            base64[i] = getChar(sixbit);
        }
        if (slack < 1) base64[2] = '=';
        if (slack < 2) base64[3] = '=';
        return base64;
    }
    private static char getChar(int sixBit) {
        if (sixBit >= 0 && sixBit <= 25)
            return (char)('A' + sixBit);
        if (sixBit >= 26 && sixBit <= 51)
            return (char)('a' + (sixBit - 26));
        if (sixBit >= 52 && sixBit <= 61)
            return (char)('0' + (sixBit - 52));
        if (sixBit == 62)
            return '+';
        if (sixBit == 63)
            return '/';
        return '?';
    }
}
