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

import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.apache.xpath.XPathAPI;

import org.xml.sax.InputSource;

public final class ConditionFactory {

    public static final String VAREQUAL = "varequal";
    
    public static List getConditions(Node xml) {
        List rtnConditions = new ArrayList();
        if (xml != null) {
                /* in the future we'lll have to loop through
                 * each variable type in IMS.  For the
                 * first requirements, there are only one
                 * variable type needed */
                try {
                XPathAPI xpath = new XPathAPI();
                NodeList nodeList = xpath.selectNodeList(xml, VAREQUAL);
                if (nodeList != null) {
                    String respId = null;
                    String answer = null;
                    for (int ix = 0; ix < nodeList.getLength(); ++ix ) {
                        answer = ((Element) nodeList.item(ix)).getFirstChild().getNodeValue(); 
                        respId = ((Element) nodeList.item(ix)).getAttribute("respident");

                        rtnConditions.add(new EqualCondition(
                                ((Node) nodeList.item(ix)) /*xml*/, 
                                respId, 
                                answer));
                    }
                }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return rtnConditions;
    }

    public static void main (String [] args) throws Exception {
        StringBuffer xml = new StringBuffer();
        xml.append("<conditionvar>");
        xml.append("<varequal respident=\"FIB01\" case=\"Yes\">Autumn</varequal>");
        xml.append("<varequal respident=\"FIB01\" case=\"Yes\">Summer</varequal>");
        xml.append("</conditionvar>");
    
        String xmlRep = xml.toString();
        System.out.println(xmlRep);
        System.out.println("\n");
        
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlRep)));

        List conditions = getConditions((Node) document);
        Condition condition = null;
        System.out.println("Number of <conditions> : " + conditions.size());
        for (int ix = 0; ix < conditions.size(); ++ix) {
            condition = (Condition) conditions.get(ix);
            System.out.println("respId : " + condition.getResponseID());
            System.out.println("answer : " + condition.getAnswer() );
        }
    }
}
