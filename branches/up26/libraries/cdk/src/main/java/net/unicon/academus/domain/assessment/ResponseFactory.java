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
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/**
 * <p>
 * These are the response to the questions.  It is up to the
 * the ResponseFactory to generate and parse the data the
 * comes in from the client.  Currently , the proper form is
 * as follows:
 * <i>respId</i>|<i>response</i>
 *
 * The ident is the item OR question id.  Each question will have a
 * particular response associated with some response presentation. 
 * For example, a multiple Fill-in-the-Blank question would have
 * have more than one response id.  Lets say the question is as
 * follows.  
 * <i>Q1. The weekend days are ________ then _________? </i>
 * The first blank could have a respID of blank1, and the other blank2. 
 * So the question would answer would come into this class resembling this
 * <i>blank1|Saturday</i>
 * <i>blank2|Sunday</i>
 * </p>
 */
public final class ResponseFactory {

    public static Response getResponse(String xml) {
       Response rtnResponse = null;
       if (xml != null) {
           rtnResponse = new Response(xml);
       }
       return rtnResponse; 
    }
    
    /**
     * <p>
     * Use this method if you already know that the responses
     * are already part of one question
     */
    public static List getResponse(String [] multipleResponse) {
        if (multipleResponse == null) {
            return new ArrayList();
        }
        return __buildResponse(multipleResponse);
    }

    /*
    public static Map getRespones(List questionResponses) {
        Map rtnMap = new HashMap();
        Map questMap = new HashMap();
        
        return new HashMap();
    }*/

    /**
     * <p>
     * The question identification is already stripped
     * off.
     * </p>
     * Collections.sort(new ArrayList(Vector.copyInto(string[]))
     * TreeSet(Vector.copyInto(string[]))
     */
    protected static List __buildResponse(String [] response) {
        List rtnList = new ArrayList();

        // Removing empty respones
        String [] newArray = __removeEmptyResponse(response);
        if (newArray != null) {
            String respID     = null;
            String prevRespID = null;
            
            List respList = new ArrayList();
            
            for(int ix=0; ix < newArray.length; ++ix) {
                respID   = newArray[ix].substring(0,newArray[ix].indexOf('|'));
            
                if (!respID.equals(prevRespID) && ix != 0) {
                    rtnList.add(new Response(
                                        prevRespID, 
                                        (String []) respList.toArray(new String [0])));
                    respList = new ArrayList();
                } 
            
                respList.add(newArray[ix].substring(newArray[ix].indexOf('|')+1,
                                newArray[ix].length()));
                prevRespID = respID; 
            }
            rtnList.add(new Response(
                                respID, 
                                (String []) respList.toArray(new String [0])));
        }
        return rtnList;
    }

    private static String[] __removeEmptyResponse(String [] response) {
        List rtnArray = new ArrayList();

        if (response != null && response.length > 0) {
            String responseStr = null;
            for (int ix = 0; ix < response.length; ++ix) {
               responseStr = response[ix].substring(
                                           response[ix].indexOf('|')+1,
                                           response[ix].length() ).trim();
                if (responseStr != null && responseStr.length() > 0) {
                    rtnArray.add(response[ix]);
                }
            }
        } 

        if (!rtnArray.isEmpty()&& rtnArray.size() > 0) {
            return (String []) rtnArray.toArray(new String[0]);    
        }
        return null;
    }
    
    public static void main (String [] args) throws Exception {
        System.out.println(
            "Generating Response objects for various responses");
        
        System.out.println("Multiple Choice Single Answer");
        String [] mcsa = { "mcsaResp|" };
        List respObj = ResponseFactory.getResponse(mcsa);
        System.out.println(showResults(respObj));
        
        System.out.println("Multiple Choice Multiple Answer");
        String [] mcma =  { "mscaResp|A", "mscaResp|" };
        respObj = ResponseFactory.getResponse(mcma);
        System.out.println(showResults(respObj));
        
        System.out.println("Fill in the Blank");
        String [] fib  =  {"fib_resp|Tuna"};
        respObj = ResponseFactory.getResponse(fib);
        System.out.println(showResults(respObj));
        
        System.out.println("Multiple Blank, Fill in the Blank");
        String [] mfib  =  {"fib_resp1|Saturday", "fib_resp2|Sunday"};
        respObj = ResponseFactory.getResponse(mfib);
        System.out.println(showResults(respObj));
        
        System.exit(1);
    }
    private static String showResults(List responses) {
        StringBuffer rtnString = new StringBuffer();
        if (responses != null)
        for(int ix=0; ix < responses.size(); ++ix) {
            Response tempResp = (Response) responses.get(ix);
            rtnString.append(tempResp.toXML());
            rtnString.append("\n");
            //System.out.println("@@@@@@@@@@@@@@ REBUILDING OBJECT @@@@@@@@@@@@@@@\n");
            //System.out.println(ResponseFactory.getResponse(tempResp.toXML()).toXML());
        }
     
        return rtnString.toString();
    }
}
