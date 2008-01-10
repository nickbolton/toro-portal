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

import net.unicon.academus.common.XMLAbleEntity;

public class UserActivation implements XMLAbleEntity {
    public int    attemptsTaken;
    public String username;

    public UserActivation () {
        attemptsTaken = 0;
    }

    public UserActivation (int attempts, String username) {
        this.attemptsTaken = attempts;
        this.username = username;
    }
    /**
     * <p>
     * Returns the number of attemps tried for this user.
     * </p><p>
     *
     * @return a int with the number of attempts allowed for the activation.
     * </p>
     */
    public int getAttemptsTaken() {
        return this.attemptsTaken;
    }
    
    /**
     * <p>
     * Sets the number of attemps tried for this user.
     * </p><p>
     *
     * @param a int with the number of attempts allowed for the activation.
     * </p>
     */
    public void setAttemptsTaken(int attempts) {
        this.attemptsTaken = attempts;
    }
    
    public String getUsername() {
        return this.username;
    }
    
    public String toXML() {
        StringBuffer xmlBuff = new StringBuffer();
        xmlBuff.append("<user-activation");
        xmlBuff.append(" username=\"");
        xmlBuff.append(username);
        xmlBuff.append("\"");
        xmlBuff.append(">");
        xmlBuff.append("<attempts-taken>");
        xmlBuff.append(""+attemptsTaken);
        xmlBuff.append("</attempts-taken>");
        xmlBuff.append("</user-activation>");
        return xmlBuff.toString();
    }
}
