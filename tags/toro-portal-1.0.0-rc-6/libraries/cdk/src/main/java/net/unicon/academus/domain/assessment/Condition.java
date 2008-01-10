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

import org.w3c.dom.Node;

/**
 * <p>
 * 
 * </p>
 */
public abstract class Condition {

/**
 * <p>
 * Represents ...
 * </p>
 */
    protected String __responseId; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    protected Node __xml; 

/**
 * <p>
 * Represents ...
 * </p>
 */
    protected String __answer; 

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @param response ...
 * </p><p>
 * @return a boolean with ...
 * </p>
 */
    public Condition (Node xml, String respId, String answer) {
        this.__xml = xml;
        this.__responseId = respId;
        this.__answer = answer;
    }
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * @param response ...
 * </p><p>
 * @return a boolean with ...
 * </p>
 */
    public abstract boolean evaluate(String response);
    
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * @return a boolean with ...
 * </p>
 */
    public String getResponseID() {
        return this.__responseId;
    }
    
/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * @return a boolean with ...
 * </p>
 */
    public String getAnswer() {
        return this.__answer;
    }

/**
 * <p>
 * Does ...
 * </p><p>
 * 
 * </p><p>
 * @return a boolean with ...
 * </p>
 */
    public Node getXML() {
        return this.__xml;
    }

    public String toString() {
        StringBuffer to = new StringBuffer();
        to.append("\n\tCondition : [");
        to.append("\n\t\trespID : ").append(__responseId);
        to.append("\n\t\tanswer : ").append(__answer);
        to.append("\t]\n");

        return to.toString();
    }
} // end Condition




