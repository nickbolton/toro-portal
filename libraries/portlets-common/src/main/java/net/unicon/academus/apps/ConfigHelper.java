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

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import net.unicon.academus.api.AcademusDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ConfigHelper {

    private static final Log LOG = LogFactory.getLog(ConfigHelper.class);
    
	public static Method getParser(Element e, Class defaultClass) {

		// Assertions.
		if (e == null) {
			String msg = "Argument 'e [Element]' cannot be null.";
			throw new IllegalArgumentException(msg);
		}
		if (defaultClass == null) {
			String msg = "Argument 'defaultClass' cannot be null.";
			throw new IllegalArgumentException(msg);
		}

		Class implClass = defaultClass;	// duh...
		Attribute impl = e.attribute("impl");
		if (impl != null) {
            String implementationClassName = "unknown class";
			try {
                implementationClassName = impl.getValue();
                
                // exception for backwards compatibility of SsoEntry impl declaration
                // in configuration files.
                String ssoEntryClassName = SsoEntry.class.getName();
                String ssoEntrySimpleClassName = SsoEntrySimple.class.getName();
                if (ssoEntryClassName.equals(implementationClassName)) {
                    LOG.warn("impl=\"" + ssoEntryClassName + "\" appeared in Gateway configuration.  " +
                            "This declaration is deprecated and instead should be impl=\"" + ssoEntrySimpleClassName + "\"");
                    implementationClassName = ssoEntrySimpleClassName;
                }
                
				implClass = Class.forName(implementationClassName);
			} catch (Throwable t) {
				String msg = "Unable to create a parser for the specified implementation:  " 
																		+ impl.getValue();
				throw new RuntimeException(msg, t);
			}
		}

		Method rslt = null;
		try {
			rslt = implClass.getDeclaredMethod("parse", new Class[] {Element.class});
		} catch (Throwable t) {
			String msg = "Unable to create a parser for the specified implementation:  " 
																+ implClass.getName();
			throw new RuntimeException(msg, t);
		}
		
		return rslt;
		
	}
	
	public static Element handle(Element configElement) {
        return handle(configElement, true);
    }

    public static Element handle(Element configElement, boolean handleDataSource) {
        // Resolve imports
        List elist = configElement.selectNodes("//import");
        if (!elist.isEmpty()) {
            for (Iterator it = elist.iterator(); it.hasNext();) {
                Element e = (Element)it.next();

                String fname = null;
                Attribute t = e.attribute("src");
                if (t == null) {
                    throw new IllegalArgumentException(
                            "Element <import> must contain an attribute 'src'.");
                }
                fname = t.getValue();

                String xpath = null;
                t = e.attribute("select");
                if (t == null) {
                    throw new IllegalArgumentException(
                            "Element <import> must contain an attribute 'select'.");
                }
                xpath = t.getValue();

                Element e2 = null;
                try {
                    
                    // first try the classloader
                    URL url = ConfigHelper.class.getResource(fname);
                    
                    // next try an url
                    if (url == null) {
                        try {
                            url = new URL(fname);
                        } catch (MalformedURLException mue) {
                            // try a file
                            File f = new File(fname);
                            if (!f.exists()) {
                                throw new Exception(); // will get caught below
                            }
                            url = new URL("file", null, fname);
                        }
                    }
                    e2 = (Element)ConfigHelper.handle(
                            (new SAXReader())
                                .read(url.toString())
                                    .getRootElement()
                                ).selectSingleNode(xpath);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to import requested document ("+fname+") and xpath ("+xpath+")", ex);
                }

                if (e2 == null)
                    throw new IllegalArgumentException(
                            "XPath expression ["+xpath+"] failed to resolve into an element.");

                // Replace the import with the selected node definition
                e.getParent().add(e2.detach());
                e.detach();
            }
        }

        elist = configElement.selectNodes("//copy-of");
        if (!elist.isEmpty()) {
            for (Iterator it = elist.iterator(); it.hasNext();) {
                Element e = (Element)it.next();

                String xpath = null;
                Attribute t = e.attribute("select");
                if (t == null) {
                    throw new IllegalArgumentException(
                            "Element <copy-of> must contain an attribute 'select'.");
                }
                xpath = t.getValue();

                Element e2 = (Element)configElement.selectSingleNode(xpath);
                if (e2 == null)
                    throw new IllegalArgumentException(
                            "The given xpath expression did not resolve into a node: "+xpath);

                // Replace the copy-of with the selected node definition
                e.getParent().add(e2.createCopy());
                e.detach();
            }
        }

        elist = configElement.selectNodes("//value-of");
        if (!elist.isEmpty()) {
            for (Iterator it = elist.iterator(); it.hasNext();) {
                Element e = (Element)it.next();

                String xpath = null;
                Attribute t = e.attribute("select");
                if (t == null) {
                    throw new IllegalArgumentException(
                            "Element <value-of> must contain an attribute 'select'.");
                }
                xpath = t.getValue();

                Element e2 = (Element)configElement.selectSingleNode(xpath);
                if (e2 == null)
                    throw new IllegalArgumentException(
                            "The given xpath expression did not resolve into a node: "+xpath);

                // Replace the value-of with the text of the selected node definition
                e.getParent().setText(e2.getText());
                e.detach();
            }
        }

        if (handleDataSource) {
            String jndiName = null;
            Element el = (Element)configElement.selectSingleNode("jndi-ref");
            if (el != null)
                jndiName = el.getText();

            // Bootstrap datasources...
            elist = configElement.selectNodes("//*[@needsDataSource='true']");
            if (jndiName != null && !elist.isEmpty()) {
                try {
                    DataSource ds = new AcademusDataSource();

                    for (Iterator it = elist.iterator(); it.hasNext();) {
                        Element e = (Element) it.next();

                        Attribute impl = e.attribute("impl");
                        if (impl == null)
                            throw new IllegalArgumentException(
                                    "Elements with the 'needsDataSource' attribute "
                                    + " must have an 'impl' attribute.");

                        Class.forName(impl.getValue())
                            .getDeclaredMethod("bootstrap",
                                    new Class[] { DataSource.class })
                            .invoke(null, new Object[] { ds });
                    }
                } catch (Throwable e) {
                    throw new RuntimeException("Failed the DataSource bootstrapping", e);
                }
            }
        }

        return configElement;
    }

//    private static DataSource getDataSource(String jndiName) {
//        try {
//            return (DataSource)(new InitialContext()).lookup(jndiName);
//        } catch(NamingException ne) {
//            throw new RuntimeException("JNDI lookup for JDBC DataSource failed: "+jndiName, ne);
//        }
//    }
}
