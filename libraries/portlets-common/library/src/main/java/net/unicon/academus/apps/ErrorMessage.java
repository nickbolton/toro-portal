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

package net.unicon.academus.apps;

import net.unicon.alchemist.EntityEncoder;

public final class ErrorMessage {

    // Instance Members.
    private String type;
    private String problem;
    private String solution;

    /*
     * Public API.
     */

    public ErrorMessage(String type, String problem, String solution) {

        // Assertions.
        if (type == null) {
            String msg = "Argument 'type' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (problem == null) {
            String msg = "Argument 'problem' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'solution' may be null.

        // Instance Members.
        this.type = type;
        this.problem = problem;
        this.solution = solution;

    }

    public String toXml() {

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<error ");

        // Type.
        rslt.append("type=\"").append(EntityEncoder.encodeEntities(type))
                                                    .append("\">");

        // Problem.
        rslt.append("<problem>").append(EntityEncoder.encodeEntities(problem))
                                                    .append("</problem>");

        // Solution.
        if (solution != null) {
            rslt.append("<solution>")
                        .append(EntityEncoder.encodeEntities(solution))
                        .append("</solution>");
        }

        // End.
        rslt.append("</error>");
        return rslt.toString();

    }

}