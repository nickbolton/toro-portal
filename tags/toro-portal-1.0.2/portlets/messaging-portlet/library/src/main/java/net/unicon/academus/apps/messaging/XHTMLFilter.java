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

package net.unicon.academus.apps.messaging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ccil.cowan.tagsoup.Parser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XHTMLFilter implements ContentHandler
{
    private int ignorelvl;
    private boolean needclose;
    private final Writer writer;
    private static final Set allowable = genAllowable();
    private static final Set ignoreAttrs = genIgnoreAttrs();

    public XHTMLFilter(OutputStream os) {
        this(new OutputStreamWriter(os));
    }

    public XHTMLFilter(Writer w) {
        this.writer = w;
    }

    public void startDocument() throws SAXException {
        this.ignorelvl = 0;
        this.needclose = false;
//System.out.println("[I: "+ignorelvl+"] Doc start");
    }
    public void endDocument() throws SAXException {
//System.out.println("[I: "+ignorelvl+"] Doc end");
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
//System.out.println("[I: "+ignorelvl+"] Element start: "+lname);
        lname = lname.toLowerCase();
        if (allowable.contains(lname)) {
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
                    if (!ignoreAttrs.contains(aname) && !filterAttrValue(aval)) {
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
//System.out.println("[I: "+ignorelvl+"] Element end: "+lname);
        lname = lname.toLowerCase();
        if (allowable.contains(lname)) {
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
//System.out.println("[I: "+ignorelvl+"] Chars: "+new String(ch, start, len));
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
        return aval.toLowerCase().startsWith("javascript:");
    }

    private static Set genIgnoreAttrs() {
        Set rslt = new HashSet();

        rslt.add("style");
        rslt.add("clear");
        rslt.add("version");

        rslt.add("onabort");
        rslt.add("onblur");
        rslt.add("onchange");
        rslt.add("onclick");
        rslt.add("ondblclick");
        rslt.add("onerror");
        rslt.add("onfocus");
        rslt.add("onkeydown");
        rslt.add("onkeypress");
        rslt.add("onkeyup");
        rslt.add("onload");
        rslt.add("onmousedown");
        rslt.add("onmouseout");
        rslt.add("onmouseover");
        rslt.add("onmouseup");
        rslt.add("onreset");
        rslt.add("onresize");
        rslt.add("onsubmit");
        rslt.add("onunload");

        return Collections.unmodifiableSet(rslt);
    }

    private static Set genAllowable() {
        Set rslt = new HashSet();

        rslt.add("html");
        rslt.add("body");

        rslt.add("a");
        rslt.add("br");
        rslt.add("center");
        rslt.add("div");
        rslt.add("hr");
        rslt.add("img");
        rslt.add("label");
        rslt.add("legend");
        rslt.add("p");
        rslt.add("pre");

        rslt.add("b");
        rslt.add("big");
        rslt.add("font");
        rslt.add("h1");
        rslt.add("h2");
        rslt.add("h3");
        rslt.add("h4");
        rslt.add("i");
        rslt.add("small");
        rslt.add("span");
        rslt.add("strike");
        rslt.add("sub");
        rslt.add("sup");
        rslt.add("u");

        rslt.add("abbr");
        rslt.add("acronym");
        rslt.add("address");
        rslt.add("bdo");
        rslt.add("blockquote");
        rslt.add("cite");
        rslt.add("code");
        rslt.add("dfn");
        rslt.add("em");
        rslt.add("kbd");
        rslt.add("samp");
        rslt.add("strong");
        rslt.add("tt");
        rslt.add("var");

        rslt.add("caption");
        rslt.add("col");
        rslt.add("colgroup");
        rslt.add("table");
        rslt.add("tbody");
        rslt.add("td");
        rslt.add("tfoot");
        rslt.add("th");
        rslt.add("thead");
        rslt.add("tr");

        rslt.add("dd");
        rslt.add("dl");
        rslt.add("dt");
        rslt.add("li");
        rslt.add("ol");
        rslt.add("ul");

        return Collections.unmodifiableSet(rslt);
    }

    public static String filterHTML(String in) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            XMLReader r = new Parser();
            Writer w = new OutputStreamWriter(bos, "UTF-8");
            InputSource s = new InputSource(new StringReader(in));

            r.setFeature(Parser.ignoreBogonsFeature, true);
            r.setContentHandler(new XHTMLFilter(w));
            r.parse(s);

        } catch (Exception e) {
            throw new RuntimeException("Unable to parse the given HTML", e);
        }

        return bos.toString();
    }

    public static void main(String[] args) throws Exception {
        XMLReader r = new Parser();
        r.setFeature(Parser.ignoreBogonsFeature, true);
        Writer w = new OutputStreamWriter(System.out, "UTF-8");
        r.setContentHandler(new XHTMLFilter(w));
        InputSource s = new InputSource();
        s.setSystemId(args[0]);
        r.parse(s);
    }
}

