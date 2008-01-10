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

import javax.xml.transform.TransformerException;

import net.unicon.sdk.SDKException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.CachedXPathAPI;

/**
 * Class for parsing XML documents using the Apache XPath utilities.
 *
 * @author      Alex Bragg
 */

public class XPathUtil
{
        // This is an XPath context. By making a static generic context, 
        // we save a lot of time

    public static final CachedXPathAPI xp = new CachedXPathAPI();

   /**
    * This method accepts an XML <code>Node</code> and an XPath query and
    * returns a string value of the first found node or null if there 
    * was no node.
    *
    * @param  n     An XML <code>Node</code>
    * @param  xpath The xpath query to execute on the <code>Node</code>
    *
    * @return The <code>String</code> value of the <code>Node</code> matching
    *         XPath query
    *
    * @throws SDKException
    */

    public static String XPathSingleNodeValue(Node n, String xpath)
        throws SDKException
    {
        String str = null;
        Node nn;
        try
        {
            nn = xp.selectSingleNode(n, xpath);

            if (nn != null && nn.hasChildNodes() )
                str = nn.getFirstChild().getNodeValue();
        } 
        catch(TransformerException e)
        {
            throw new SDKException(
                "A parse exception occurred executing: " + xpath + "\n" + e);
        }

        return str;
    }

   /**
    * This method accepts an XML <code>Node</code> and an XPath query and
    * returns a string array of the values for the found nodes or null if there 
    * were no nodes found.
    *
    * @param  n     An XML <code>Node</code>
    * @param  xpath The xpath query to execute on the <code>Node</code>
    *
    * @return The <code>String</code> array of values of the <code>Node</code>s 
    *         matching XPath query
    *
    * @throws SDKException
    */

    public static String[] XPathMultiNodeValues(Node n, String xpath)
        throws SDKException
    {
        String[] str = new String[0];
        NodeList nl;
        try
        {
            nl = xp.selectNodeList(n, xpath);
            str = new String[nl.getLength()];

            for(int i = 0; i < nl.getLength(); i++)
                str[i] = nl.item(i).getFirstChild().getNodeValue();
        } 
        catch(TransformerException e)
        {
            throw new SDKException(
                "A parse exception occurred executing: " + xpath + "\n" + e);
        }

        return str;
    }

   /**
    * This method accepts an XML <code>Node</code> and an XPath query and
    * returns a <code>NodeList</code> of the <code>Node</code>s matching
    * the query. The method returns null if it doesn't find any 
    * <code>Node</code>s.
    *
    * @param  n     An XML <code>Node</code>
    * @param  xpath The xpath query to execute on the <code>Node</code>
    *
    * @return The <code>NodeList</code> of <code>Node</code>s 
    *         matching XPath query
    *
    * @throws SDKException
    */

    public static NodeList XPathSelectNodes(Node n, String xpath)
        throws SDKException
    {
        NodeList nn;

        try
        {
            nn = xp.selectNodeList(n, xpath);
        } 
        catch(TransformerException e)
        {
            throw new SDKException(
                "A parse exception occurred executing: " + xpath + "\n" + e);
        }

        return nn;
    }

   /**
    * This method accepts an XML <code>Node</code> and returns a 
    * <code>String</code> containing a representation of the 
    * <code>Node</code> in XML file format.
    *
    * @param  n     An XML <code>Node</code>
    *
    * @return Returns a <code>String</code> with <code>Node</code> in XML
    *         file format. 
    *
    * @throws SDKException
    */

    public static String NodetoXML(Node n)
    {
        NamedNodeMap nm = null;
        StringBuffer sb = new StringBuffer();

        if(n.hasChildNodes())
        {
            sb.append("<" + ((Element) n).getTagName());

            nm = n.getAttributes();
            for (int i = 0; i < nm.getLength(); i++)
                sb.append(" " + nm.item(i).getNodeName() + "=\"" 
                          + nm.item(i).getNodeValue() + "\"");

            sb.append(">");

            NodeList nl = n.getChildNodes();

            for (int i = 0; i < nl.getLength(); i++)
                sb.append(NodetoXML(nl.item(i)));

            sb.append("</" + ((Element) n).getTagName() + ">");
        }
        else
            sb.append(n.getNodeValue());

        return sb.toString();
    }

}
