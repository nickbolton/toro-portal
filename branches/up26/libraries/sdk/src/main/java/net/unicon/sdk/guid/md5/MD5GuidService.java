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
package net.unicon.sdk.guid.md5;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import net.unicon.sdk.guid.Context;
import net.unicon.sdk.guid.Guid;
import net.unicon.sdk.guid.GuidException;
import net.unicon.sdk.guid.IGuidService;

/**
 * Provides Globally Unique Identifiers based on MD5.
 */
public class MD5GuidService implements IGuidService {

    private static Random myRand;
    private static String s_id;

    static {
        SecureRandom mySecureRand = new SecureRandom();
        long secureInitializer = mySecureRand.nextLong();
        myRand = new Random(secureInitializer);
        try {
            s_id = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    } // end static block

    /** constructor */
    public MD5GuidService() {}

    /**
     * Provides a Globally Unique Identifier (GUID) based on MD5.
     *
     * @return Guid object that represents a globally unique identifier. 
     * @throws <code>GuidException</code>
     * @see <{Guid}>
     */
    public synchronized Guid generate() throws GuidException {

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            long time = System.currentTimeMillis();
            long rand = 0;
            rand = myRand.nextLong();

            StringBuffer sbValueBeforeMD5 = new StringBuffer();
            sbValueBeforeMD5.append(s_id);
            sbValueBeforeMD5.append(":");
            sbValueBeforeMD5.append(Long.toString(time));
            sbValueBeforeMD5.append(":");
            sbValueBeforeMD5.append(Long.toString(rand));

            String valueBeforeMD5 = sbValueBeforeMD5.toString();
            md5.update(valueBeforeMD5.getBytes());

            byte[] array = md5.digest();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < array.length; ++j) {
                int b = array[j] & 0xFF;
                if (b < 0x10) sb.append('0');
                sb.append(Integer.toHexString(b));
            }

            String valueAfterMD5 = sb.toString();
            Guid guid = new Guid(valueAfterMD5);

            return guid;
        } catch (Throwable t) {
            throw new GuidException(t.getMessage(), t);
        }

    } // end generate 

    /**
     * Provides a Globally Unique Identifier (GUID) based on MD5.
     *
     * @param ctx Context that the GUID is to be created for.
     * @return Guid object that represents a globally unique identifier. 
     * @throws <code>GuidException</code>
     * @see <{Guid}>
     * @see <{Context}>
     */
    public synchronized Guid generate(Context ctx) throws GuidException {
        Guid guid = this.generate();

        return guid;
    } // end generate(ctx)

} // end MD5GuidService
