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

package net.unicon.academus.apps.permissions.engine;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.permissions.PermissionsUserContext;
import net.unicon.academus.apps.permissions.PortletHelper;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

/**
 * State Query to be used as the base type for all Permissions Portlet queries.
 * InitialQuery is also the state query for the permissions_welcome screen.
 *
 * <p>Provided in this base implementation are a set a common queries that can
 * be referenced by the subclass using the following pattern:</p>
 * <pre>
 * public String query() throws WarlockException {
 *    StringBuffer rslt = new StringBuffer();
 *    rslt.append("&lt;state&gt;");
 *    super.commonQueries(rslt);
 *
 *    // ...Add any additional state information here.
 *
 *    rslt.append("&lt;/state&gt;");
 *    return rslt.toString();
 * }
 * </pre>
 *
 * <p>If any of the queries called by {@link #commonQueries(StringBuffer) commonQueries} need
 * extra information appended to them, use the following pattern:</p>
 * <pre>
 * protected void queryStatus(StringBuffer rslt) throws WarlockException {
 *     super.queryStatus(rslt);
 *
 *     // ...Append any other state information that belongs within
 *     // &lt;status&gt;&lt;/status&gt; elements.
 * }
 * </pre>
 *
 * @see #commonQueries(StringBuffer)
 */
public class InitialQuery implements IStateQuery
{
    /** The user context associated with this state query instance. */
    protected final PermissionsUserContext puc;
    /** The errors reported from the calling Action. */
    protected final ErrorMessage[] errors;

    /**
     * Construct an InitialQuery for the given user context.
     * @param muc User context to associate with the query.
     */
    public InitialQuery(PermissionsUserContext puc) {
        this(puc, null);
    }

    /**
     * Construct an InitialQuery for the given user context.
     * @param muc User context to associate with the query.
     * @param errors Error messages to include in the state query
     */
    public InitialQuery(PermissionsUserContext puc, ErrorMessage[] errors) {
        // Assertions
        if(puc == null){
            throw new IllegalArgumentException(
                    "Argument 'puc [PermissionsUserContext]' cannot be null.");
        }

        this.puc = puc;        
        this.errors = (errors != null ? errors : new ErrorMessage[0]);
    }

    /**
     * Retrieve the state information.
     *
     * <p>This default implementation returns the results of
     * {@link #commonQueries(StringBuffer) commonQueries} wrapped in
     * &lt;state&gt; elements.</p>
     * 
     * @see net.unicon.warlock.IStateQuery#query()
     * @see #commonQueries(StringBuffer)
     */
    public String query() throws WarlockException {
        StringBuffer rslt = new StringBuffer();

        rslt.append("<state>");
        commonQueries(rslt);
        rslt.append("</state>");
        return rslt.toString();
    }

    /**
     * Append a common (across Permissions Portlet) set of queries to the
     * provided StringBuffer.
     *
     * The following elements and methods are added:
     * <ul>
     * <li>&lt;status&gt;     - {@link #queryStatus(StringBuffer) queryStatus}
     * <li>&lt;navigation&gt; - {@link #queryNavigation(StringBuffer) queryNavigation}
     * <li>&lt;current&gt;    - {@link #querySelections(StringBuffer) querySelections}
     * </ul>
     *
     * @see InitialQuery
     */
    protected void commonQueries(StringBuffer rslt) throws WarlockException {
        rslt.append("<status>");
        queryStatus(rslt);
        rslt.append("</status>");

        rslt.append("<navigation>");
        queryNavigation(rslt);
        rslt.append("</navigation>");

        rslt.append("<current>");
        querySelections(rslt);
        rslt.append("</current>");
    }

    /**
     * Append any status information to the provided StringBuffer.
     *
     * The following elements are added:
     * <ul>
     * <li>&lt;errors&gt;
     * </ul>
     *
     * @see ErrorMessage#toXml()
     */
    protected void queryStatus(StringBuffer rslt) throws WarlockException {
        // Errors.
        for (int i = 0; i < errors.length; i++)
            rslt.append(errors[i].toXml());
    }

    /**
     * Append any current selections to the provided StringBuffer.
     */
    protected void querySelections(StringBuffer rslt) throws WarlockException {
        if (puc.hasPortletSelection()) {
            PortletHelper pInfo = puc.getPortletSelection();
            rslt.append(pInfo.toXml());
        }
    }

    /**
     * Append navigation information to the provided StringBuffer.
     */
    protected void queryNavigation(StringBuffer rslt) throws WarlockException {
        PortletHelper[] portlets = puc.getAppContext().listPortlets();

        for (int i = 0; i < portlets.length; i++) {
            rslt.append(portlets[i].toXml());
        }
    }

    /**
     * Retrieve any decisions to supply to the screens.
     *
     * This implementation returns an array of length zero.
     *
     * @see net.unicon.warlock.IStateQuery#getDecisions()
     */
    public IDecisionCollection[] getDecisions() throws WarlockException {
    	return new IDecisionCollection[0];
    }
}
