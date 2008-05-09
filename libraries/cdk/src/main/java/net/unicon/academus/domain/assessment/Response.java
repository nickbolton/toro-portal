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

package net.unicon.academus.domain.assessment;

import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;


import net.unicon.academus.common.XMLAbleEntity;

/**
 * <p>
 * 
 * </p>
 */
public class Response implements XMLAbleEntity {

    private String [] response; 
    private String responseID;
    
    public Response() {} 
    
    public Response(String xml) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            XPathAPI xpath = new XPathAPI();
            Node node = xpath.selectSingleNode(document, "response");
            
            if (node != null) {
                this.responseID = ((Element) node).getAttribute("ident_ref");
                
                NodeList nodeList = document.getElementsByTagName("response_value");
                String [] setResp = new String[nodeList.getLength()];
                
                for (int ix = 0; ix < nodeList.getLength(); ++ix) {
                    setResp[ix] = ((Element) nodeList.item(ix)).getFirstChild().getNodeValue(); 
                }
                this.response = setResp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public Response(String responseID, String [] response) {
        this.response   = response;
        this.responseID = responseID;
    }

    /**
     * <p>
     * Does ...
     * </p><p>
     * 
     * @return a String with ...
     * </p>
     */
    public String[] getResponse() {        
        // your code here
        return this.response;
    } // end getResponse        

    /**
     * <p>
     * Does ...
     * </p><p>
     * 
     * @param response ...
     * </p>
     */
    public void setResponse(String [] response) {        
        this.response = response;
    } // end setResponse        

    public String getResponseId() {
        return this.responseID;
    }
    
    public void setResponseId(String respId) {
        this.responseID = respId;
    }

    public String toXML() {
        StringBuffer xmlBuffer = new StringBuffer();
        xmlBuffer.append("<response ");
        xmlBuffer.append(" ident_ref=\"");
        xmlBuffer.append(responseID);
        xmlBuffer.append("\"");
        xmlBuffer.append(">");

        //Responses
        if (response != null) {
            for (int ix =0; ix < response.length; ++ix ) {
                xmlBuffer.append("<response_value>");
                xmlBuffer.append(response[ix]);
                xmlBuffer.append("</response_value>");
            }
        } else {
            xmlBuffer.append("<response_value/>");    
        }
        xmlBuffer.append("</response>");
        return xmlBuffer.toString();
    }
} // end Response
