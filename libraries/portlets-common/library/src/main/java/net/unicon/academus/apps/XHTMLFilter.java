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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XHTMLFilter implements ContentHandler
{
    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(XHTMLFilter.class);

    private int ignorelvl;
    private boolean needclose;
    private final Writer writer;
    private final XHTMLFilterConfig config;

    public XHTMLFilter(OutputStream os, XHTMLFilterConfig config) {
        this(new OutputStreamWriter(os), config);
    }

    public XHTMLFilter(Writer w, XHTMLFilterConfig config) {
        this.writer = w;
        this.config = config;
    }

    public void startDocument() throws SAXException {
        this.ignorelvl = 0;
        this.needclose = false;
    }
    public void endDocument() throws SAXException {
        try {
            write('\n');
            writer.flush();
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {}
    public void endPrefixMapping(String prefix) throws SAXException {}

    public void skippedEntity(String name) throws SAXException {}
    public void setDocumentLocator(Locator loc) {}
    public void processingInstruction(String target, String data) throws SAXException {}

    public void startElement(String nsuri, String lname, String qname, Attributes atts) throws SAXException {
        lname = lname.toLowerCase();
        if (config.allowTags.contains(lname)) {
            if (needclose) {
                write('>');
                needclose = false;
            }

            write('<');
            write(lname);

            int attLength = atts.getLength();
            if (attLength > 0) {
                for (int i = 0; i < attLength; i++) {
                    String aname = atts.getLocalName(i).toLowerCase();
                    String aval = atts.getValue(i);
                    if (!config.ignoreAttrs.contains(aname) && !filterAttrValue(aval)) {
                        write(' ');
                        write(aname);
                        write("=\"");
                        writeEsc(aval, true);
                        write('"');
                    }
                }
            }

            needclose = true;
        } else
            ignorelvl++;
    }

    public void endElement(String nsuri, String lname, String qname) throws SAXException {
        lname = lname.toLowerCase();
        if (config.allowTags.contains(lname)) {
            if (needclose) {
                write(" />");
                needclose = false;
            } else {
                write("</");
                write(lname);
                write('>');
            }
        } else
            ignorelvl--;
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
    public void characters (char ch[], int start, int len) throws SAXException
    {
        if (needclose) {
            write('>');
            needclose = false;
        }
        if (ignorelvl == 0)
            writeEsc(ch, start, len, false);
    }

    private void write (String s) throws SAXException
    {
        try {
            writer.write(s);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    private void write (char c) throws SAXException
    {
        try {
            writer.write(c);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    private void writeEsc (String s, boolean isAttVal)
                            throws SAXException {
        writeEsc(s.toCharArray(), 0, s.length(), isAttVal);
    }

    private void writeEsc (char ch[], int start, int length, boolean isAttVal)
                            throws SAXException {
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
                case '&':
                    write("&amp;");
                    break;
                case '<':
                    write("&lt;");
                    break;
                case '>':
                    write("&gt;");
                    break;
                case '\"':
                    if (isAttVal) {
                        write("&quot;");
                    } else {
                        write('\"');
                    }
                    break;
                default:
                    if (ch[i] > '\u007f') {
                        write("&#");
                        write(Integer.toString(ch[i]));
                        write(';');
                    } else {
                        write(ch[i]);
                    }
            }
        }
    }

    private boolean filterAttrValue(String aval) {
        boolean rslt = false;
        Iterator it = config.attrValueFilters.iterator();
        while (!rslt && it.hasNext()) {
            Pattern p = (Pattern)it.next();
            rslt = p.matcher(aval).matches();
        }
        return rslt;
    }

    public static class XHTMLFilterConfig {
        private final Set allowTags;
        private final Set ignoreAttrs;
        private final Set attrValueFilters;
        private XHTMLFilterConfig(Set allowTags, Set ignoreAttrs, Set attrValueFilters) {
            this.allowTags = Collections.unmodifiableSet(allowTags);
            this.ignoreAttrs = Collections.unmodifiableSet(ignoreAttrs);
            this.attrValueFilters = Collections.unmodifiableSet(attrValueFilters);
        }
    }

    public static XHTMLFilterConfig getConfiguration(String resourceName) {
        Properties p = new Properties();
        try {
            p.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to load specified properties file: "+resourceName,
                    e);
        }
        return getConfiguration(p);
    }

    public static XHTMLFilterConfig getConfiguration(Properties p) {
        Set allowTags = new HashSet();
        Set ignoreAttrs = new HashSet();
        Set attrValueFilters = new HashSet();
        String tmp[] = null;

        String atags = p.getProperty("tags.allow");
        tmp = atags.split(",\\s*");
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].length() > 0)
                allowTags.add(tmp[i]);
        }

        String iattrs = p.getProperty("attributes.ignore");
        tmp = iattrs.split(",\\s*");
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].length() > 0)
                ignoreAttrs.add(tmp[i]);
        }

        String valFilters = p.getProperty("attributes.valueFilters");
        tmp = valFilters.split(",\\s*");
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].length() > 0)
                attrValueFilters.add(Pattern.compile(tmp[i]));
        }

        return new XHTMLFilterConfig(allowTags, ignoreAttrs, attrValueFilters);
    }

    public static String filterHTML(String in, XHTMLFilterConfig config) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputSource s = new InputSource(new StringReader(in));
        filterHTML(s, bos, config);
        return bos.toString();
    }

    public static void filterHTML(InputSource in, OutputStream os, XHTMLFilterConfig config) {
        try {
            XMLReader r = new Parser();
            Writer w = new OutputStreamWriter(os, "UTF-8");
            r.setFeature(Parser.ignoreBogonsFeature, true);
            r.setContentHandler(new XHTMLFilter(w, config));
            r.parse(in);
        } catch (Exception e) {
            log.error("Unable to parse the given HTML: "+e.getMessage(), e);
            throw new RuntimeException("Unable to parse the given HTML: "+e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        XHTMLFilterConfig config = getConfiguration(args[1]);
        InputSource s = new InputSource();
        s.setSystemId(args[0]);
        filterHTML(s, System.out, config);
    }
}

