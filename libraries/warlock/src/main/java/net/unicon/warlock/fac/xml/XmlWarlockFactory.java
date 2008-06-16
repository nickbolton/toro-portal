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

package net.unicon.warlock.fac.xml;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IChoiceCollectionParser;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.PenelopeException;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IRenderingEngine;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.AbstractWarlockFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.serialize.Serializer;
import org.apache.xml.serializer.Method;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMNodeHelper;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXEventRecorder;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

public final class XmlWarlockFactory extends AbstractWarlockFactory {
    
    private static final Object systemLock = new Object();
    private static final Log log =
        LogFactory.getLog(XmlWarlockFactory.class);
    
    private static final String xsltDefaultTransformer =
        TransformerFactory.newInstance().getClass().getName();
    
    private static Properties serializerProperties = null;
    private static Properties xmlSerializerProperties = null;
    
    // Instance Members.
    private final Source transSource;
    private final IRenderingEngine engine;
    private final IEntityStore store;
    private SAXTransformerFactory transformerFactory = null;
    private Templates templates = null;
    
    
    private boolean xsltcDebug = false;
    private String xsltcPackageName = "translets";
    private boolean xsltcGenerateTranslet = false;
    private boolean xsltcAutoTranslet = false;
    private boolean xsltcUseClasspath = false;

    /*
     * Public API.
     */

    public XmlWarlockFactory(Source transSource) {
        // Assertions.
        if (transSource == null) {
            String msg = "Arg:wument 'transSource' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // Instance Members.
        this.transSource = transSource;
        this.engine = new RenderingEngineImpl(this);
        this.store = new JvmEntityStore();
        
        initializeDefaultTransformerFactory();
        initializeSerializer();
    }
    
    public XmlWarlockFactory(Source transSource,
        String transformerImplemention, boolean debug, String packageName,
        boolean generateTranslet, boolean autoTranslet,
        boolean useClasspath) {
        
        // Assertions.
        if (transSource == null) {
            String msg = "Arg:wument 'transSource' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        /*
        try {
            Transformer emptyt=TransformerFactory.newInstance().newTransformer();
            StringWriter sw = new StringWriter();
            emptyt.transform(transSource, new StreamResult(sw));
            System.out.println("ZZZ XmlWarlockFactory XSL SOURCE:\n" + sw.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        

        // Instance Members.
        this.transSource = transSource;
        this.engine = new RenderingEngineImpl(this);
        this.store = new JvmEntityStore();
        
        initializeXsltcTransformerFactory(transformerImplemention,
            debug, packageName, generateTranslet, autoTranslet, useClasspath);
        initializeSerializer();
    }
    
    private void initializeSerializer() {
        if (serializerProperties == null) {
            synchronized (systemLock) {
                if (serializerProperties == null) {
                    serializerProperties = new Properties();
                    serializerProperties.put(OutputKeys.METHOD, Method.HTML);
                }
                if (xmlSerializerProperties == null) {
                    xmlSerializerProperties = new Properties();
                    xmlSerializerProperties.put(OutputKeys.METHOD, Method.XML);
                }
            }
        }
    }
    
    private void cacheTemplates() {
        try {
	        // cache the precompiled templates object
            transformerFactory.setAttribute("debug",
          Boolean.toString(xsltcDebug));
      transformerFactory.setAttribute("package-name",
          xsltcPackageName);
      transformerFactory.setAttribute("generate-translet",
          Boolean.toString(xsltcGenerateTranslet));
      transformerFactory.setAttribute("auto-translet",
          Boolean.toString(xsltcAutoTranslet));
      transformerFactory.setAttribute("use-classpath",
          Boolean.toString(xsltcUseClasspath));
	        templates = transformerFactory.newTemplates(transSource);
        } catch (TransformerConfigurationException tce) {
            throw new RuntimeException("Failed caching templates.", tce);
        }
    }
    
    private void initializeDefaultTransformerFactory() {
        synchronized (systemLock) {
            this.transformerFactory =
                (SAXTransformerFactory)TransformerFactory.newInstance();
	        log.info("Warlock using transformer - "
	            + transformerFactory.getClass().getName());
        }
        cacheTemplates();
    }
    
    private void initializeXsltcTransformerFactory(String transformerImplemention,
        boolean debug, String packageName, boolean generateTranslet,
        boolean autoTranslet, boolean useClasspath) {
        
        synchronized (systemLock) {
            System.setProperty("javax.xml.transform.TransformerFactory",
                transformerImplemention);
            this.transformerFactory =
                (SAXTransformerFactory)TransformerFactory.newInstance();
            this.xsltcDebug = debug;
            this.xsltcPackageName = packageName;
            this.xsltcGenerateTranslet = generateTranslet;
            this.xsltcAutoTranslet = autoTranslet;
            this.xsltcUseClasspath = useClasspath;
            
            log.info("Warlock using transformer - "
	            + transformerFactory.getClass().getName());
            log.info("Setting auto-translet to " + autoTranslet);
            log.info("Setting generate-translet to " +
                generateTranslet);
            log.info("Setting use-classpath to " + useClasspath);
	        
	
	        // restore the default implementation
	        System.setProperty("javax.xml.transform.TransformerFactory",
	            xsltDefaultTransformer);
	
	        log.info("Warlock restored transformer - "
	            + TransformerFactory.newInstance().getClass().getName());
            
            // cache the source as a precompile templates object 
        }
        cacheTemplates();
    }

    public IRenderingEngine getRenderingEngine() {
        return engine;
    }

    public IScreen parseScreen(Element e) throws WarlockException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return parseScreen(e, new Element[0]);

    }

    public IScreen parseScreen(Element e, Element[] reference)
                                throws WarlockException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (reference == null) {
            String msg = "Argument 'reference' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Resolve the References.
        List ref = e.selectNodes("//include");
        Iterator it = ref.iterator();
        while (it.hasNext()) {

            Element incl = (Element) it.next();

            // Read the library handle.
            Attribute h = incl.attribute("handle");
            if (h == null) {
                String msg = "Element <include> is missing required attribute "
                                                            + "'handle'.";
                throw new XmlFormatException(msg);
            }
            String handle = h.getValue();

            // Read the xpath expression.
            Attribute x = incl.attribute("xpath");
            if (x == null) {
                String msg = "Element <include> is missing required attribute "
                                                            + "'xpath'.";
                throw new XmlFormatException(msg);
            }
            String xpath = x.getValue();

            // Find the relevent library.
            Element lib = null;
            for (int i=0; i < reference.length; i++) {
                if (reference[i].valueOf("@handle").equals(handle)) {
                    lib = reference[i];
                    break;
                }
            }
            if (lib == null) {
                String msg = "Unable to locate the specified reference "
                                            + "library:  " + handle;
                throw new WarlockException(msg);
            }

            // Replace the node(s).
            org.w3c.dom.Element dIncl = DOMNodeHelper.asDOMElement(incl);
            Iterator targets = lib.selectNodes(xpath).iterator();
            while (targets.hasNext()) {
                Element elm = (Element) targets.next();
                org.w3c.dom.Element dElm = DOMNodeHelper.asDOMElement(elm);
                org.w3c.dom.Element dImp = (org.w3c.dom.Element) dIncl
                        .getOwnerDocument().importNode(dElm, true);
                dIncl.getParentNode().insertBefore(dImp, dIncl);
            }
            dIncl.getParentNode().removeChild(dIncl);

        }

        return AbstractWarlockFactory.ScreenImpl.parse(e, this);

    }

    /*
     * Implementation.
     */

    private synchronized TransformerHandler getTransformerHandler()
                            throws WarlockException {
        TransformerHandler rslt = null;

        // Create a new one.
        synchronized(systemLock) {
            try {
                transformerFactory.setAttribute("debug",
                    Boolean.toString(xsltcDebug));
                transformerFactory.setAttribute("package-name",
                    xsltcPackageName);
                transformerFactory.setAttribute("generate-translet",
                    Boolean.toString(xsltcGenerateTranslet));
                transformerFactory.setAttribute("auto-translet",
                    Boolean.toString(xsltcAutoTranslet));
                transformerFactory.setAttribute("use-classpath",
                    Boolean.toString(xsltcUseClasspath));
                rslt = transformerFactory.newTransformerHandler(templates);
            } catch (Throwable t) {
                String msg = "Unable to create a new transformer instance.";
                throw new WarlockException(msg, t);
            }
        }

        return rslt;
    }

    private IEntityStore getEntityStore() {
        return store;
    }

    /*
     * Nested Types.
     */

     private static final class RenderingEngineImpl
                    implements IRenderingEngine {

        // Instance Members.
        private final XmlWarlockFactory owner;

        /*
         * Public API.
         */

        public RenderingEngineImpl(XmlWarlockFactory owner) {

            // Assertions.
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.owner = owner;

        }

        public IChoiceCollection[] render(IScreen screen, IStateQuery query,
                                Map params, Writer writer) throws WarlockException {

            // Assertions.
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (query == null) {
                String msg = "Argument 'query' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (params == null) {
                String msg = "Argument 'params' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            
            final String screenText = "screen";
            final String decisionsText = "decisions";
            final String emptyText = "";
            
            IChoiceCollection[] choices = null;
            
            try {
                // Evaluate the screen.
                
                AttributesImpl attributes = new AttributesImpl();
                
                // Transform to browser markup.
                TransformerHandler th = owner.getTransformerHandler();
                Transformer trans = th.getTransformer();
                th.setResult(new StreamResult(writer));
                
                
                Iterator it = params.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    trans.setParameter(key, params.get(key));
                }
                if (log.isDebugEnabled()) {
                    log.debug("Transforming for evaluated screen: " +
                        screen.getHandle().getValue());
                }
                
                // Prepare choice collection(s).
                IChoiceCollectionParser ccParser = owner.getEntityStore().getChoiceCollectionParser(th);
                
                ccParser.startDocument();
                
                ccParser.startElement(emptyText, screenText, screenText, attributes);
                if (log.isDebugEnabled()) {
                    log.debug("Evaluating IStateQuery: "+query.getClass().getName());
                }
                screen.evaluate(query, ccParser);
                
                ccParser.startElement(emptyText, decisionsText, decisionsText, attributes);
                it = Arrays.asList(query.getDecisions()).iterator();
                while (it.hasNext()) {
                    IDecisionCollection c = (IDecisionCollection) it.next();
                    c.sendXmlEvents(ccParser);
                }
                ccParser.endElement(emptyText, decisionsText, decisionsText);
                ccParser.endElement(emptyText, screenText, screenText);
                ccParser.endDocument();
                
                choices = ccParser.getChoiceCollections();

            } catch (SAXException se) {
                throw new WarlockException(
                    "Rendering engine failed to build the specified screen: " +
                    screen.getHandle().getValue(), se);
            }

            return choices;
        }

     
     
     public class TestHandler implements ContentHandler {
         
         private ContentHandler handler = null;
         
         public TestHandler(ContentHandler handler) {
             this.handler = handler;
         }

        public void endDocument() throws SAXException {
            System.out.println("endDocument");
            handler.endDocument();
        }

        public void startDocument() throws SAXException {
            System.out.println("startDocument");
            handler.startDocument();
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            System.out.println("characters: " + new String(ch, start, length));
            handler.characters(ch, start, length);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            System.out.println("ignorableWhitespace: " + new String(ch, start, length));
            handler.ignorableWhitespace(ch, start, length);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            System.out.println("endPrefixMapping: " + prefix);
            handler.endPrefixMapping(prefix);
        }

        public void skippedEntity(String name) throws SAXException {
            System.out.println("skippedEntity: " + name);
            handler.skippedEntity(name);
        }

        public void setDocumentLocator(Locator locator) {
            System.out.println("setDocumentLocator: " + locator);
            System.out.println("columnNumber: " + locator.getColumnNumber());
            System.out.println("lineNumber: " + locator.getLineNumber());
            System.out.println("publicId: " + locator.getPublicId());
            System.out.println("systemId: " + locator.getSystemId());
            handler.setDocumentLocator(locator);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            System.out.println("processingInstruction: " + target + ", " + data);
            handler.processingInstruction(target, data);
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            System.out.println("startPrefixMapping: " + prefix + ", " + uri);
            handler.startPrefixMapping(prefix, uri);
        }

        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            System.out.println("endElement: " + namespaceURI + ", " + localName + ", " + qName);
            handler.endElement(namespaceURI, localName, qName);
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
            System.out.println("startElement: " + namespaceURI + ", " + localName + ", " + qName + ", " + atts);
            handler.startElement(namespaceURI, localName, qName, atts);
        }
         
     }
     }
     
     public class TransformerObjects {
         private TransformerHandler th = null;
         private Serializer ser = null;
         
         public TransformerObjects(TransformerHandler th, Serializer ser) {
             this.th = th;
             this.ser = ser;
         }
         
         public TransformerHandler getTransformerHandler() {
             return th;
         }
         
         public Serializer getSerializer() {
             return ser;
         }
     }
}
