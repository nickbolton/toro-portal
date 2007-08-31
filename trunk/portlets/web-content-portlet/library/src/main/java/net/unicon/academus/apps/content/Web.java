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

package net.unicon.academus.apps.content;

import net.unicon.academus.apps.XHTMLFilter;
import net.unicon.academus.apps.XHTMLFilter.XHTMLFilterConfig;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFile;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Web {

    // Static Members.
    private static final Map cache = Collections.synchronizedMap(new HashMap());
    private static final String[][] replace = new String[][] {
                    new String[] { "&quot;", "&#034;" },
                    new String[] { "&lt;", "&#060;" },
                    new String[] { "&gt;", "&#062;" },
                    new String[] { "&nbsp;", "&#160;" },
                    new String[] { "&copy;", "&#169;" },
                    new String[] { "&amp;", "&#038;" }  // MUST BE LAST!!
                };


    // Instance Members.
    private final String dfltDoc;
    private final IResourceFactory fac;
    
    /*
     * Public API.
     */

    public static Web fromUrl(String url) {

        // Assertions.
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Check the cache first.
        if (cache.containsKey(url)) {
            return (Web) cache.get(url);
        }

        Web rslt = null;
        synchronized (Web.class) {

            // Double-check.
            if (cache.containsKey(url)) {
                return (Web) cache.get(url);
            }

            /* We must obtain it from the url.
             * Layout of the Web URL is as follows:
             *
             *  0   Protocol (i.e. WEB:)
             *  1   [Empty... "//" after protocol]
             *  2   Class Name
             *  3   Default Document
             *  4+  Resource Factory
             */

            String[] tokens = url.split("/", 5);

            // Default Document.
            String dfltDoc = tokens[3];

            // Resouce Factory.
            String facUrl = tokens[4];
            IResourceFactory fac;
            try {
                fac = AbstractResourceFactory.fromUrl(facUrl);

                rslt = new Web(dfltDoc, fac);

                // Add to the cache.
                cache.put(url, rslt);
            } catch (DemetriusException e) {
                throw new RuntimeException("Resource with the given url was not found. " + facUrl);
            }
        }

        return rslt;

    }

    public String getDefaultDocument() {
        return dfltDoc;
    }
    
    public class DocumentData {
       private Map attributes;
       private String body;
       private DocumentData(String body, Map attributes) {
          this.body = body;
          this.attributes = attributes;
       }
      public Map getAttributes() {
         return attributes;
      }
      public String getBody() {
         return body;
      }
    }
    
    public DocumentData getDocument(String path, String bodyXpath,
             String inputTags, XHTMLFilterConfig filterConfig) {
        // Assertions.
        if (path == null) {
            String msg = "Argument 'path' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (bodyXpath == null) {
            String msg = "Argument 'bodyXpath' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (inputTags == null) {
           String msg = "Argument 'inputTags' cannot be null.";
           throw new IllegalArgumentException(msg);
        }

        Map attrs = new HashMap();
        StringBuffer body = new StringBuffer();
        try {
            IFile f = (IFile) fac.getResource(path);
            Reader r = new BufferedReader(new InputStreamReader(f.getInputStream()));
            
            String xml = null;
            if (filterConfig != null) {
               // TagSoup it!
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
               InputSource is = new InputSource(r);
               XHTMLFilter.filterHTML(is, bos, filterConfig);
               xml = bos.toString();
            } else { // Straight through
               // Read everything into a StringBuffer.
               StringBuffer chars = new StringBuffer();
               char[] buff = new char[512];
               int wasRead = 0;
               do {
                   chars.append(buff, 0, wasRead); // NB:  No-op the first time...
                   wasRead = r.read(buff, 0, buff.length);
               } while (wasRead != -1);
               
               // Perform entity replacement.
               xml = replaceUncleanEntities(chars.toString());
            }

            Document doc = new SAXReader().read(new StringReader(xml));

            Iterator it = doc.selectNodes(bodyXpath).iterator();
            body.append("<body>");
            while (it.hasNext()) {
                Node n = (Node) it.next();
                body.append(n.asXML());
            }
            body.append("</body>");
            
            String name = null;
            String value = null;
            it = doc.selectNodes(inputTags).iterator();
            while (it.hasNext()) {
                Element n = (Element) it.next();
                name = n.attribute("name").getValue();
                value = n.attribute("value").getValue();
                attrs.put(name, value);
            }
        } catch (Throwable t) {
            String msg = "Unable to obtain the specified document body:  "
                                                                + path;
            throw new RuntimeException(msg, t);
        }
        
        return new DocumentData(body.toString(), attrs);

    }

    /*
     * Implementation.
     */

    private Web(String dfltDoc, IResourceFactory fac) {

        // Assertions.
        if (dfltDoc == null) {
            String msg = "Argument 'dfltDoc' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (fac == null) {
            String msg = "Argument 'fac' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.dfltDoc = dfltDoc;
        this.fac = fac;

    }

    private static String replaceUncleanEntities(String inpt) {

        // Assertions.
        if (inpt == null) {
            String msg = "Argument 'inpt' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        String rslt = inpt;

        for (int i=0; i < replace.length; i++) {
            String[] tokens = replace[i];
            rslt = rslt.replaceAll(tokens[0], tokens[1]);
        }

        return rslt;

    }

}