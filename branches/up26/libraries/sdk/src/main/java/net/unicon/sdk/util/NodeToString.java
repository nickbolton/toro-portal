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
package net.unicon.sdk.util;

import org.w3c.dom.*;
import java.util.*;
import java.io.*;

public class NodeToString {

    protected static ArrayList empty = new ArrayList();

    protected Node doc;
    protected HashMap stopTags;
    protected HashMap emptyTags;
    protected HashMap blankLineTags;
    protected int startIndent;
    protected int indent;
    protected int tabStop;

    protected Writer out;
    /**
    * Canonical refers to Canonical XML, a standard within the W3C.
    * For more information see http://www.w3.org/TR/xml-c14n
    **/
    protected boolean canonical;
    protected boolean unwrapCDATA;

    protected boolean printTopLevelTag;
    protected boolean printNewLines;

    public NodeToString( Node doc, 
                         boolean printTopLevelTag) {

        this(doc, empty,empty,empty, printTopLevelTag);

    } /* end NodeToString constructor */

    public NodeToString( Node doc, List stopTags, 
                         List emptyTags, List blankLines, 
                         boolean printTopLevelTag) {
        this (doc);
        this.printTopLevelTag = printTopLevelTag;
        printNewLines = true;
        setStopTags(stopTags);
        setEmptyTags(emptyTags);
        setBlankLineTags(blankLines);
    } /* end NodeToString constructor */

    public NodeToString(Node doc) {
        this.doc = doc;
        startIndent = 0;
        tabStop = 2;
        canonical = true;
        stopTags = new HashMap();
        emptyTags = new HashMap();
        blankLineTags = new HashMap();
        printTopLevelTag = true;
        printNewLines = true;
    }

    
    public void setWriter(Writer out){ this.out = out; }
    public void setIndent(int indent){ startIndent = indent; }
    public void setTabStop(int tabStop){ this.tabStop = tabStop; }
    public void setCanonical(boolean canonical){ this.canonical = canonical; }
    public void setPrintTopLevelTag(boolean print){ printTopLevelTag = print; }
    public void setPrintNewLines(boolean newlines) { printNewLines = newlines; }
    public void setUnwrapCDATA(boolean unwrap) { unwrapCDATA = unwrap; }


    /**
     * This sets the list of tags that should not be
     * indented. this includes all tags under the included tags.
     *
     * @param list just the tag names
     */
    public void setStopTags(List list) {
        stopTags = new HashMap();
        for (int i=0; i < list.size(); i++) {
            stopTags.put(list.get(i), "x");
        }
    }

    /**
     * This sets the tags that are allowed to be
     * printed as <... /> if they have no children or value.
     * @param list
     */
    public void setEmptyTags(List list) {
        emptyTags = new HashMap();
        for (int i=0; i < list.size(); i++) {
            emptyTags.put(list.get(i), "x");
        }
    }
    
    /**
     * This sets the tags that should have an extra blank line after the end tag.
     * @param list
     */
    public void setBlankLineTags(List list) {
        blankLineTags = new HashMap();
        for (int i=0; i < list.size(); i++) {
            blankLineTags.put(list.get(i), "x");
        }
    }

     public String toString() {
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         indent = startIndent;
         try {
             out = new OutputStreamWriter(bout, "UTF-8");
             print(doc, indent, tabStop, false, true);
         } catch (Exception e) {e.printStackTrace();}
         return bout.toString();
     }

    //////////////// stolen from xerces sample code

    private static String MIME2JAVA_ENCODINGS[] =
    { "Default", "UTF-8", "US-ASCII", "ISO-8859-1", "ISO-8859-2", "ISO-8859-3", "ISO-8859-4", 
      "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "ISO-8859-9", "ISO-2022-JP",
      "SHIFT_JIS", "EUC-JP","GB2312", "BIG5", "EUC-KR", "ISO-2022-KR", "KOI8-R", "EBCDIC-CP-US", 
      "EBCDIC-CP-CA", "EBCDIC-CP-NL", "EBCDIC-CP-DK", "EBCDIC-CP-NO", "EBCDIC-CP-FI", "EBCDIC-CP-SE",
      "EBCDIC-CP-IT", "EBCDIC-CP-ES", "EBCDIC-CP-GB", "EBCDIC-CP-FR", "EBCDIC-CP-AR1", 
      "EBCDIC-CP-HE", "EBCDIC-CP-CH", "EBCDIC-CP-ROECE","EBCDIC-CP-YU",  
      "EBCDIC-CP-IS", "EBCDIC-CP-AR2", "UTF-16"
    };


    /** Prints the specified node, recursively. */
    public void print(Node node, int indent, int tabStop, 
                      boolean inStop, boolean topLevel) throws IOException{

        // is there anything to do?
        if ( node == null ) {
            return;
        }
        
        boolean blankLine = false;
        if (blankLineTags.get(node.getNodeName()) != null) {
            blankLine = true;
        }
        
        int type = node.getNodeType();
        switch ( type ) {
            // print document
        case Node.DOCUMENT_NODE: {
            if ( !canonical ) {
                if (printTopLevelTag) {
                    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                }
            }

            print(((Document)node).getDocumentElement(), indent, tabStop,
                  inStop, topLevel);
            out.flush();
            break;
        }

        // print element with attributes
        case Node.ELEMENT_NODE: {

            NodeList children = node.getChildNodes();

            for (int i = 0; i < indent; i++) {
                out.write(' ');
            }
            if ((topLevel && printTopLevelTag)||(!topLevel)) {
                out.write('<');
                out.write(node.getNodeName());
                Attr attrs[] = sortAttributes(node.getAttributes());
                for ( int i = 0; i < attrs.length; i++ ) {
                    Attr attr = attrs[i];
                    out.write(' ');
                    out.write(attr.getNodeName());
                    out.write("=\"");
                    out.write(normalize(attr.getNodeValue()));
                    out.write('"');
                }
            }

            boolean empty = false;

            if ((children.getLength()==0)&&
                (emptyTags.get(node.getNodeName())!=null)) {
                
                if ((topLevel && printTopLevelTag)||(!topLevel)) {
                    out.write(" />");
                }
                empty = true;

            } else {
                if ((topLevel && printTopLevelTag)||(!topLevel)) {
                    out.write('>');
                }
            }


            if (empty) {
                if (printNewLines) {
                    if (!inStop) {
                        out.write('\n');
                    }
                    if (blankLine) {
                        out.write('\n');
                    }
                }
                return;
            }

            // check to see if it has a value/text if not indent and newline
                
            boolean text = false;
            boolean thisIsStop = false;
            if (stopTags.get(node.getNodeName()) != null) {
                text = true;
                thisIsStop = true;
            }

            if ( children != null ) {
                int len = children.getLength();
                
                
                for ( int i = 0; i < len; i++ ) {
                    if (children.item(i).getNodeType() == Node.TEXT_NODE) {
                        text = true;
                    }
                }

                if (!(text||inStop)) {
                    if (!thisIsStop && printNewLines) {
                        out.write('\n');
                    }
                    for ( int i = 0; i < len; i++ ) {
                        print(children.item(i), indent+tabStop, tabStop, 
                              thisIsStop, false);
                    }
                } else {
                    for ( int i = 0; i < len; i++ ) {
                        print(children.item(i), 0, 0, 
                              thisIsStop||inStop, false);
                    }
                }
            }

            // print out the end tag
            if (!(text||inStop)) {
                for (int i = 0; i < indent; i++) {
                    out.write(' ');
                }
            }
            if ((topLevel && printTopLevelTag)||(!topLevel)) {
                out.write("</");
                out.write(node.getNodeName());
                out.write(">");
                if (!inStop && printNewLines) {
                    out.write('\n');
                }
            }
            if (blankLine && printNewLines) {
                out.write('\n');
            }
        
            break;
        }

        // handle entity reference nodes
        case Node.ENTITY_REFERENCE_NODE: {
            if ( canonical ) {
                NodeList children = node.getChildNodes();
                if ( children != null ) {
                    int len = children.getLength();
                    for ( int i = 0; i < len; i++ ) {
                        print(children.item(i), indent, tabStop, 
                              inStop, false);
                    }
                }
            } else {
                out.write('&');
                out.write(node.getNodeName());
                out.write(';');
            }
            break;
        }

        // print cdata sections
        case Node.CDATA_SECTION_NODE: {
            if ( canonical || unwrapCDATA) {
                out.write(normalize(node.getNodeValue()));
            } else {
                out.write("<![CDATA[");
                out.write(node.getNodeValue());
                out.write("]]>");
            }
            break;
        }

        // print text
        case Node.TEXT_NODE: {
            out.write(normalize(node.getNodeValue()));
            break;
        }

        // print processing instruction
        case Node.PROCESSING_INSTRUCTION_NODE: {
            out.write("<?");
            out.write(node.getNodeName());
            String data = node.getNodeValue();
            if ( data != null && data.length() > 0 ) {
                out.write(' ');
                out.write(data);
            }
            out.write("?>");
            break;
        }
        }

        out.flush();

    } // print(Node)

    /** Returns a sorted list of attributes. */
    protected Attr[] sortAttributes(NamedNodeMap attrs) {
        
        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr array[] = new Attr[len];
        for ( int i = 0; i < len; i++ ) {
            array[i] = (Attr)attrs.item(i);
        }
        for ( int i = 0; i < len - 1; i++ ) {
            String name  = array[i].getNodeName();
            int    index = i;
            for ( int j = i + 1; j < len; j++ ) {
                String curName = array[j].getNodeName();
                if ( curName.compareTo(name) < 0 ) {
                    name  = curName;
                    index = j;
                }
            }
            if ( index != i ) {
                Attr temp    = array[i];
                array[i]     = array[index];
                array[index] = temp;
            }
        }

        return (array);

    } // sortAttributes(NamedNodeMap):Attr[]

    /** Normalizes the given string. */
    protected String normalize(String s) {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for ( int i = 0; i < len; i++ ) {
            char ch = s.charAt(i);
            switch ( ch ) {
            case '<': {
                str.append("&lt;");
                break;
            }
            case '>': {
                str.append("&gt;");
                break;
            }
            case '&': {
                str.append("&amp;");
                break;
            }
            case '"': {
                str.append("&quot;");
                break;
            }
            case '\r':
            case '\n': {
                /*
                * true canonical would convert the new line char
                * however, this puts the entity values in the titles
                * which go into the db. A trim might work but then
                * I would have to change everywhere we use NodeToString.
                */
                /*
                if ( canonical ) {
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                    break;
                }
                */
                // else, default append char
            }
            default: {
                str.append(ch);
            }
            }
        }

        return (str.toString());

    } // normalize(String):String



}
