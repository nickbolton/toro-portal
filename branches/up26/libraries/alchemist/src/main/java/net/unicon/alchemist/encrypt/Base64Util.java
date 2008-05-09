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


public class Base64Util {

	private static String byteToCharMap = null;

	private static final char PADDING = '=';
    private static final char FIRST_DIGIT = '0';
    private static final char LAST_DIGIT = '9';
    private static final char FIRST_UPPER_CASE = 'A';
    private static final char LAST_UPPER_CASE = 'Z';
    private static final char FIRST_LOWER_CASE = 'a';
    private static final char LAST_LOWER_CASE = 'z';

    private static final int BYTE_SET_SIZE = 3;
	private static final int CHAR_SET_SIZE = 4;

    // Build base64 encoding map
    static {

		StringBuffer buffer = new StringBuffer(64);
        int index = 0;

        // Insert letters A to Z
		char currentChar = FIRST_UPPER_CASE;
        while (currentChar <= LAST_UPPER_CASE) {
            buffer.append(currentChar++);
		}

		// Insert letters a to z
		currentChar = FIRST_LOWER_CASE;
        while (currentChar <= LAST_LOWER_CASE) {
			buffer.append(currentChar++);
		}

        // Insert digits 0 to 9 in the map
		currentChar = FIRST_DIGIT;
        while (currentChar <= LAST_DIGIT) {
			buffer.append(currentChar++);
		}

		buffer.append('+');
		buffer.append('-');

		byteToCharMap = buffer.toString();
	}

    public static String encode (byte[] input) {

		int byteLength = input.length;

		// Divide byte array to 3-byte sets
		int byteSets = byteLength / BYTE_SET_SIZE ;

		// Remaining bytes in byte array after sectioning it to 3-byte sets
		int remaining = byteLength % BYTE_SET_SIZE;

        int byteIndex = 0;
        int charIndex;
        int setCount = 0;

        StringBuffer result = new StringBuffer(byteLength);

        byte[] target = new byte[CHAR_SET_SIZE];

		// Convert byte array to unsigned integers
		int[] unsignedInt = new int[input.length];
		for (int index = 0; index < unsignedInt.length; index++) {
			unsignedInt[index] = getUnsignedInt(input[index]);
		}

        while (setCount < byteSets) {

			// Extract 3 bytes and convert them to 4 bytes with 6 meaningful bits each
            target[0] = (byte) (unsignedInt[byteIndex] >>> 2);
            target[1] = (byte) ( ((unsignedInt[byteIndex] & 0x03) << 4) | (unsignedInt[byteIndex+1] >> 4) );
            target[2] = (byte) ( ((unsignedInt[byteIndex+1] & 0x0f) << 2) | (unsignedInt[byteIndex+2] >> 6) );
            target[3] = (byte) ( unsignedInt[byteIndex+2] & 0x3f);

			// Generate 4 base64 encoded characters
            for (int index = 0; index < CHAR_SET_SIZE; index++) {
				result.append(byteToCharMap.charAt(target[index]));
			}

            byteIndex = byteIndex + BYTE_SET_SIZE;
            setCount++;
        }

        // One byte remaining to be encoded
        // Create 2 base64 characters and pad twice
        if (remaining == 1) {
			target[0] = (byte) (unsignedInt[byteIndex] >>> 2);
            target[1] = (byte) ( (unsignedInt[byteIndex] & 0x03) << 4);
            result.append(byteToCharMap.charAt(target[0]));
            result.append(byteToCharMap.charAt(target[1]));
			result.append(PADDING);
			result.append(PADDING);
        }

        // One byte remaining to be encoded
        // Create 3 base64 characters and pad once
        if (remaining == 2) {
			target[0] = (byte) (unsignedInt[byteIndex] >>> 2);
            target[1] = (byte) ( ((unsignedInt[byteIndex] & 0x03) << 4) | (unsignedInt[byteIndex+1] >> 4) );
            target[2] = (byte) ( ((unsignedInt[byteIndex+1] & 0x0f) << 2));
            result.append(byteToCharMap.charAt(target[0]));
            result.append(byteToCharMap.charAt(target[1]));
            result.append(byteToCharMap.charAt(target[2]));
            result.append(PADDING);
        }

		return result.toString();
	}

	// Java bytes are signed but for Base64 encoding purposes they
	// should be treated as unsigned. The method converts a byte
	// to an integer that is always positive and has the same bit
	// sequence as the original byte.
	private static int getUnsignedInt(byte b) {

		int unsigned;

	    if (b >= 0) {
	    	unsigned = (int) b;
	    } else {
			unsigned = 256 + b;
		}

	    return unsigned;
  	}

	public static byte[] decode (String encoded) {

		char[] encodedChars = encoded.toCharArray();
		int charLength = encodedChars.length;

		// Divide character array to 4-character sets
		// The length of valid base64 strings is always divisible by 4
		int charSets = charLength / CHAR_SET_SIZE ;

		// Calculate the number of resulting bytes
		// 4 characters give 3 bytes
		int numBytes = charSets * BYTE_SET_SIZE;

		byte[] source = new byte[CHAR_SET_SIZE];
		byte[] result = new byte[numBytes];

        int byteIndex = 0;
        int charIndex = 0;
        int setCount = 0;

        while (setCount < charSets) {

			// Extract 4 source bytes from the encoded string
			// The 4 bytes are represented by 4 characters
			for (int index = 0; index < CHAR_SET_SIZE; index++) {
				source[index] = charToByte(encodedChars[charIndex++]);
			}

			// Construct 3 bytes from the 4 source bytes extracted
			result[byteIndex++] = (byte) (( source[0] << 2) | (source[1] >>> 4));
			result[byteIndex++] = (byte) (((source[1] & 0x0f) << 4) | (source[2] >>> 2));
			result[byteIndex++] = (byte) (((source[2] & 0x03) << 6) | (source[3]));

            setCount++;
        }

        // Remove bytes caused by the padding characters
        if ( encoded.endsWith("==") ) {
			numBytes = numBytes - 2;
		} else if (encoded.endsWith("=") ) {
			numBytes--;
		}

        byte[] temp = result;
        result = new byte[numBytes];

        for (int index = 0; index < numBytes; index++) {
			result[index] = temp[index];
		}

		return result;
	}

	private static byte charToByte (char encodedChar) {

		byte result = 0;

        // All indexes are less than the size of a byte
		if (encodedChar != PADDING)
			result = (byte) (byteToCharMap.indexOf(encodedChar));

		return result;
    }

	public static void main (String[] args) {

		String encodeParam = null;

		if (args.length > 0 && args[0] != null && !args.equals("") ){

			encodeParam = args[0];

		} else {

			System.out.println("Aborting...Please provide a word/phrase to encode and decode.");
			System.exit(1);
		}

		byte[] input = encodeParam.getBytes();

		System.out.println("Input size:" + input.length);

		String encoded = encode(input);
		System.out.println("Encoded string:" + encoded);

		byte[] decoded = decode(encoded);

		System.out.println("Decoded size:" + decoded.length);

		String decodedStr = new String(decoded);

		System.out.println("Decoded string:" +decodedStr);

	}
}
