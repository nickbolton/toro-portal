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

import java.net.URL;

import java.security.Key;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Simple symmetrical encryption service.
 *
 * <p>This class depends on the presence of the configuration file
 * "/config/encryption.xml", which is relative to the top-level classpath.</p>
 *
 * <p>This file should contain entries similar to:</p>
 * <pre>
 * &lt;encryption-services&gt;
 *    &lt;encryption-instance encrypt="true"&gt;
 *        &lt;name&gt;myApp&lt;/name&gt;
 *        &lt;key&gt;somekey1&lt;/key&gt;
 *    &lt;/encryption-instance&gt;
 * &lt;/encryption-services&gt;
 * </pre>
 *
 * <p>The key must be 8 characters or more, but only the first 8 characters
 * will be used.</p>
 */
public class EncryptionService {

    private static final String CONFIGURATION_FILE = "/config/encryption.xml";
    private static final String ALGORITHM_SETTINGS = "DES/ECB/PKCS5Padding";
    private static final String ALGORITHM = "DES";

    private static final Map serviceInstances = createInstances();

    private Key key = null;  
    private boolean encryption = false;

    /**
     * Returns a singleton instance of the encryption service.
     *
     * @return A singleton encryption service instance.
     */
    public synchronized static EncryptionService getInstance(String appName) {
		return (EncryptionService)serviceInstances.get(appName);
	}

	/**
	 * Encrypts the specified string.
	 *
	 * @param The <code>String</code> to be encrypted.
	 * @return The encrypted <code>String</code>.
     */
	public String encrypt (String value)  {

		if(!encryption){
			return value;
		}
		String encryptedValue = null;
        Cipher cipher = null;
        byte[] encryptedBytes = null;        

		if (value != null && !value.equals("")) {

			try {

				// Convert string to bytes based on specified char encoding
				byte[] input = value.getBytes();
				
				cipher = Cipher.getInstance(ALGORITHM_SETTINGS);

				// Encrypt bytes
				cipher.init(Cipher.ENCRYPT_MODE, key);
                encryptedBytes = cipher.doFinal(input);

			} catch (Exception e) {

				StringBuffer errorMsg = new StringBuffer(128);

				errorMsg.append("EncryptionService:");
				errorMsg.append("encrypt():");
				errorMsg.append("Cipher with algorithm:");
				errorMsg.append(ALGORITHM);
                errorMsg.append(" could not be created.");

                throw new RuntimeException(errorMsg.toString(), e);
			}

			// Convert encrypted bytes back to a string using Base64 encoding
            encryptedValue = Base64Util.encode(encryptedBytes);

		} else {

			StringBuffer errorMsg = new StringBuffer(128);

			errorMsg.append("EncryptionService:");
			errorMsg.append("encrypt():");
			errorMsg.append("The value to be encrypted cannot be null or empty.");

            throw new IllegalArgumentException(errorMsg.toString());
		}

		return encryptedValue;
	}

	/**
	 * Decrypts the specified string.
	 *
	 * @param The <code>String</code> to be decrypted.
	 * @return The decrypted <code>String</code>.
     */
	public String decrypt (String encryptedValue) {

		if(!encryption){
			return encryptedValue;
		}
		String decryptedValue = null;

		if (encryptedValue != null && !encryptedValue.equals("")) {

			try {

				// Decode the string using Base64 decoding
				byte[] input = Base64Util.decode(encryptedValue);				

				Cipher cipher = Cipher.getInstance(ALGORITHM_SETTINGS);

				// Encrypt bytes
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] decodedBytes = cipher.doFinal(input);

				// Convert encrypted bytes back to a string
				decryptedValue = new String(decodedBytes);

            } catch (Exception e) {

				StringBuffer errorMsg = new StringBuffer(128);

				errorMsg.append("EncryptionService:");
				errorMsg.append("decrypt():");
				errorMsg.append("An error occured while decrypting:");
                errorMsg.append(encryptedValue);

				throw new RuntimeException (errorMsg.toString(), e);
			}

		} else {

			StringBuffer errorMsg = new StringBuffer(128);

			errorMsg.append("EncryptionService:");
			errorMsg.append("decrypt():");
			errorMsg.append("The value to be decrypted cannot be null or empty.");

            throw new IllegalArgumentException(errorMsg.toString());
		}

		return decryptedValue;
	}

	public void setEncryption(boolean value){
		encryption = value;
	}

    /**
     * Private Encryption service constructor.
     */
    private EncryptionService(boolean encrypt, Key key) {
        this.key = key;
        this.encryption = encrypt;
	}

    /**
     * Initialize the service instances from the configuration file.
     */
    private static Map createInstances() {
        Map serviceInstances = new HashMap();

        try {
            URL configUrl = EncryptionService.class.getResource(CONFIGURATION_FILE);

            if (configUrl == null)
                throw new IllegalArgumentException("Unable to locate encryption service configuration.");

            Element configElement
                = (Element)(new SAXReader()).read(configUrl.toString()).selectSingleNode("encryption-services");;

            SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");

            List eList = configElement.selectNodes("encryption-instance");
            Iterator it = eList.iterator();
            while (it.hasNext()) {
                Element e = (Element)it.next();
                Attribute attr = e.attribute("encrypt");
                boolean encrypt = (attr == null || !attr.getValue().equalsIgnoreCase("false"));
                String appName = e.selectSingleNode("name").getText();
                String keyStr = e.selectSingleNode("key").getText();

                if (keyStr == null || keyStr.length() < 8)
                    throw new IllegalArgumentException("<key> must be at least 8 characters.");

                Key key = skf.generateSecret(new DESKeySpec(keyStr.getBytes()));

                serviceInstances.put(appName, new EncryptionService(encrypt, key));
            }

        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize EncryptionService", ex);
        }

        return Collections.unmodifiableMap(serviceInstances);
    }
}
