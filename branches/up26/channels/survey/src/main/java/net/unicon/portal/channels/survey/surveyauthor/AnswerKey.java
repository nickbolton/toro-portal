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
package net.unicon.portal.channels.survey.surveyauthor;


public final class AnswerKey {

    // Instance Members.
    private Integer pId;
    private Integer qId;
    private Integer iId;


    public AnswerKey(Integer pId, Integer qId, Integer iId) {

        // Assertions.
        if (pId == null) {
            String msg = "Argument 'pId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (qId == null) {
            String msg = "Argument 'qId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (iId == null) {
            String msg = "Argument 'iId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.pId = pId;
        this.qId = qId;
        this.iId = iId;

    }

    public boolean equals(Object o) {

        // Handle null.
        if (o == null) {
            return false;
        }

        boolean rslt = false;   // default...

        if (o instanceof AnswerKey) {
            AnswerKey k = (AnswerKey) o;
            rslt = k.pId.equals(pId) && k.qId.equals(qId) && k.iId.equals(iId);
        }

        return rslt;

    }

    public int hashCode() {
        return qId.hashCode();
    }

    public String toString() {

        StringBuffer rslt = new StringBuffer();
        rslt.append(pId.toString()).append(".").append(qId.toString())
            .append(".").append(iId.toString());
        return rslt.toString();

    }

}
