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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.apache.xpath.XPathAPI;

public class GradeCondition {
    private Node __xml;
    private List __conditions;
    private int  __addValue = 0;
    
    public GradeCondition (Node xml) {
        this.__xml = xml;
        
        if (xml != null) {
            try {
                XPathAPI xpath = new XPathAPI();
                Node conditionVar = xpath.selectSingleNode(xml, "conditionvar");
                
                __conditions = ConditionFactory.getConditions(conditionVar);

                // If in the future we want to support multiple
                // or different types of actions, we would need
                // to add them here and also pass in a variable
                // or return a map with the score with a specific
                // varname - H2
                
                Node setVar = xpath.selectSingleNode(xml, "setvar");
                
                if (setVar != null) {
                    // We are assumming add at this time, in 
                    // the future we would just change this
                    // to some setvar factory that gave us
                    // the score based on the the action.  For
                    // now we will assume 'add' and only one
                    // setvar per responsecondition node. - H2
                    
                    //String action = ((Element) setVar).getAttribute("action");
                    String value = setVar.getFirstChild().getNodeValue();
                    if (value != null)
                        __addValue = Integer.parseInt(value.trim());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Node getXML() {
        return this.__xml;
    }

    public int evaluate(List respList) {
        int rtnScore = 0;
        
        Map respMap = new HashMap();

        // Build a Map from the List
        if (respList != null) {
            Response tempResp = null;
            for (int ix = 0; ix < respList.size(); ++ix) {
                tempResp = (Response) respList.get(ix);
                respMap.put(tempResp.getResponseId(), tempResp.getResponse());
            }

            boolean passed = true;
            
            // Looping through conditions
            if (__conditions != null) {
                Condition condition = null;
                for (int ix = 0; ix < __conditions.size(); ++ix) {
                    condition = (Condition) __conditions.get(ix);
                    String [] responses = (String []) respMap.get(condition.getResponseID());

                    // XXX need some work for MCMA
                    // two responses with two conditions that
                    // need to match up
                    
                    if (responses == null) {
                        // if they have no responses, they automatically
                        // fail
                        passed = false;
                    } else if (responses.length > __conditions.size()) {
                        // if they have given more responses that
                        // gradeable conditions.
                        passed = false;
                    } else {
                        // if they have more that one response we 
                        // will evaluate it.  Currently we and
                        // the conditions. - H2
                        boolean temp = false;
                        for (int iy = 0; iy < responses.length; ++iy) {
                            temp |= condition.evaluate(responses[iy]);
                        }
                        // Setting the outcome of the responses to 
                        // the bigger picture;
                        passed &= temp;
                    }
                }

                if (passed) 
                    rtnScore = __addValue;
            }
        }
        return rtnScore;
    }

    public String toString() {
        StringBuffer to = new StringBuffer();
        to.append("Grading conditions   [");

        if (__conditions != null) {
            
            //to.append("" + __conditions.size());
            for(int ix=0; ix < __conditions.size(); ++ix) {
                to.append("\t").append(((Condition) __conditions.get(ix)).toString()); 
            }
        }
        to.append("\n");
        to.append("\tadd to score : ");
        to.append("" + __addValue);
        to.append("]");
        return to.toString();
    }
}
