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

package net.unicon.academus.apps;

// Java API
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;


/**
    A utility class for manipulating download URLs for given
	resources. The downloading URLs point to a download component
	that has the ability to download/serve a file based on the
	contents of the URL.
*/

public class DownloadURLUtil {

    // In the future the following constants could
    // be retrieved from a property file
	public static final String RESOURCE_PARAMETER = "resource";	
	public static final String APPNAME_PARAMETER = "appName";
    private static final String WEBAPP_NAME = "toro-portlets-common";
	private static final String DOWNLOAD_PREFIX = "download";
    private static final String QUERY_STRING_PREFIX = "?";
    private static final String QUERY_STRING_ADDL_XML = "&amp;";
    private static final String QUERY_STRING_ADDL = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";
	private static final String URL_SEPARATOR = "/";
	private static final String CHAR_ENCODING = "UTF-8";
	
    private static String urlPrefix = null;
    private static StringBuffer appNamePrefix = new StringBuffer();

    
    // Construct URL prefix
	static {

		StringBuffer urlBuffer = new StringBuffer(128);		

        urlBuffer.append(URL_SEPARATOR);
		urlBuffer.append(WEBAPP_NAME);
		urlBuffer.append(URL_SEPARATOR);
		urlBuffer.append(DOWNLOAD_PREFIX);
		urlBuffer.append(QUERY_STRING_PREFIX);
		urlBuffer.append(RESOURCE_PARAMETER);
        urlBuffer.append(NAME_VALUE_SEPARATOR);

		urlPrefix =  urlBuffer.toString();
		
		appNamePrefix.append(QUERY_STRING_ADDL);
		appNamePrefix.append(APPNAME_PARAMETER);
		appNamePrefix.append(NAME_VALUE_SEPARATOR);
	}

	/**
     * Creates a download URL using the specified resource key.
     * The resulting URL targets a component that can download
	 * the file specified by the given resource key.
     *
     * @param resourceKey A key that uniquely identifies the resource
	 * to be downloaded.
	 * @return A URL to a download component that can serve the
	 * specified resource.
     */
	public static String createDownloadURL (String resourceKey, String appName) {

		String downloadURL = null;
		StringBuffer downloadURLBf = new StringBuffer(128);

		if(appName == null || appName.equals("")){
			throw new IllegalArgumentException("Parameter 'appName' cannot be empty or null");
		}
			
		if (resourceKey != null && !resourceKey.equals("")) {

			try {

				resourceKey = URLEncoder.encode(resourceKey, CHAR_ENCODING);

			} catch (UnsupportedEncodingException uee) {

				StringBuffer errorMsg = new StringBuffer(128);

				errorMsg.append("DownloadURLUtil:");
				errorMsg.append("createDownloadURL():");
				errorMsg.append("An error occured while encoding download URL");

				throw new RuntimeException(errorMsg.toString(), uee);
			}

			// Build the download URL using the specified resource key
			downloadURLBf.append(urlPrefix);
			downloadURLBf.append(resourceKey);
			downloadURLBf.append(appNamePrefix);
			downloadURLBf.append(appName);
 
		} else {

			StringBuffer errorMsg = new StringBuffer(128);

			errorMsg.append("DownloadURLUtil:");
			errorMsg.append("createDownloadURL():");
			errorMsg.append("The resource key cannot be null or empty.");
			
            throw new IllegalArgumentException(errorMsg.toString());
		}

		downloadURL = downloadURLBf.toString();
			
		return downloadURL;
	}

	/**
     * Extracts the resource key out of a download URL. It allows
     * downloading components to identify the resource to be
	 * downloaded without requiring them to know how the
	 * download URL was constructed.
	 *
     * @param downloadURL A URL to a downloading component.
     * @return The resource key that was embedded in the download URL.
     */
     public static String extractResourceKey (String downloadURL) {

		String resourceKey = null;

        if (downloadURL != null && !downloadURL.equals("")) {

			// Find the resource key parameter name in the download URL
			int index = downloadURL.indexOf(RESOURCE_PARAMETER);

			if (index != -1) {				
				// Calculate the position of its value
				int offset = RESOURCE_PARAMETER.length() + NAME_VALUE_SEPARATOR.length();
				int index2 = downloadURL.indexOf(QUERY_STRING_ADDL);
				// Extract resource key from download URL, and decode it.
                // Must be extracted prior to decoding to account for
                // QUERY_STRING_ADDL to be present in an encoded form within
                // the resource name.
                try {
                    resourceKey = URLDecoder.decode(
                                    downloadURL.substring(index + offset, index2),
                                    CHAR_ENCODING);;
                } catch (UnsupportedEncodingException uee) {

                    StringBuffer errorMsg = new StringBuffer(128);

                    errorMsg.append("DownloadURLUtil:");
                    errorMsg.append("extractResourceKey():");
                    errorMsg.append("An error occured while decoding download URL");

                    throw new RuntimeException(errorMsg.toString(),uee);
                }
			}

		} else {

			StringBuffer errorMsg = new StringBuffer(128);

			errorMsg.append("DownloadURLUtil:");
			errorMsg.append("extractResourceKey():");
			errorMsg.append("The download URL cannot be null or empty.");

            throw new IllegalArgumentException(errorMsg.toString());
		}

		if (resourceKey == null || resourceKey.equals("")) {
  
			StringBuffer errorMsg = new StringBuffer(128);

			errorMsg.append("DownloadURLUtil:");
			errorMsg.append("extractResourceKey():");
			errorMsg.append("No resource key was found within provided URL:");
			errorMsg.append(downloadURL);

			throw new RuntimeException(errorMsg.toString());
		}

        return resourceKey;
	}
     
 	/**
      * Extracts the Application Name out of a download URL. It allows
      * downloading components to identify the application that is 
      * downloading the resource. This application name is used to 
      * retrieve the encryption service to encrypt and decrypt the 
      * resources.  
 	 *
 	 *
      * @param downloadURL A URL to a downloading component.
      * @return The application name that was embedded in the download URL.
      */
      public static String extractAppName (String downloadURL) {

 		String appName = null;
 		
 		try {

 			downloadURL = URLDecoder.decode(downloadURL, CHAR_ENCODING);

 		} catch (UnsupportedEncodingException uee) {

 			StringBuffer errorMsg = new StringBuffer(128);

 			errorMsg.append("DownloadURLUtil:");
 			errorMsg.append("extractResourceKey():");
 			errorMsg.append("An error occured while decoding download URL");

             throw new RuntimeException(errorMsg.toString(),uee);
 		}
 		
         if (downloadURL != null && !downloadURL.equals("")) {
 			// Find the resource key parameter name in the download URL
 			int index = downloadURL.indexOf(APPNAME_PARAMETER);

 			if (index != -1) {				
 				// Calculate the position of its value
 				int offset = APPNAME_PARAMETER.length() + NAME_VALUE_SEPARATOR.length();

 				// Extract resource key from download URL
 				appName = downloadURL.substring(index + offset);
 			}

 		} else {

 			StringBuffer errorMsg = new StringBuffer(128);

 			errorMsg.append("DownloadURLUtil:");
 			errorMsg.append("extractResourceKey():");
 			errorMsg.append("The download URL cannot be null or empty.");

             throw new IllegalArgumentException(errorMsg.toString());
 		}

 		if (appName == null || appName.equals("")) {
   
 			StringBuffer errorMsg = new StringBuffer(128);

 			errorMsg.append("DownloadURLUtil:");
 			errorMsg.append("extractAppName():");
 			errorMsg.append("No application name was found within provided URL:");
 			errorMsg.append(downloadURL);

 			throw new RuntimeException(errorMsg.toString());
 		}

         return appName;
 	}
}
