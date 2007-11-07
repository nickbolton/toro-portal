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

package net.unicon.academus.apps.gateway.engine;

import java.util.Map;

import net.unicon.academus.apps.SsoEntry;
import net.unicon.academus.apps.SsoHandler;
import net.unicon.academus.apps.gateway.GatewayUserContext;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;
import net.unicon.alchemist.EntityEncoder;

/**
 * State Query to be used as the base type for all Gateway Portlet queries.
 * InitialQuery is also the state query for all current actions.
 *
 * @see #commonQueries(StringBuffer)
 */
public class InitialQuery implements IStateQuery
{
   private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(InitialQuery.class);
    /** The user context associated with this state query instance. */
    protected final GatewayUserContext guc;

    /**
     * Construct an InitialQuery for the given user context.
     * @param guc User context to associate with the query.
     */
    public InitialQuery(GatewayUserContext guc) {
        // Assertions
        if(guc == null){
            throw new IllegalArgumentException(
                    "Arguement 'guc [GatewayUserContext]' cannot be null.");
        }

        this.guc = guc;
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
        
        if (log.isDebugEnabled()) {
           log.debug("State Query: "+rslt);
        }

        guc.setLoggedIn();
 
        return rslt.toString();
    }

    /**
     * Append a common (across Gateway Portlet) set of queries to the
     * provided StringBuffer.
     *
     * The following elements and methods are added:
     * <ul>
     * <li>&lt;navigation&gt; - {@link #queryNavigation(StringBuffer) queryNavigation}
     * </ul>
     *
     * @see InitialQuery
     */
    protected void commonQueries(StringBuffer rslt) throws WarlockException {
        rslt.append("<title>");
        rslt.append(EntityEncoder.encodeEntities(guc.getAppContext().getTitle()));
        rslt.append("</title>");

        String eid = guc.getEntryId();
        Map userattribs = guc.getUserAttributes();
        SsoHandler sh = guc.getAppContext().getSsoHandler();
        boolean useAjax = guc.getAppContext().isAjaxFormPopulation();

        rslt.append("<navigation");

        if (guc.getChangeCreds())
            rslt.append(" changecreds=\"true\"");
        
        StringBuffer ajaxSb = new StringBuffer();
        if (useAjax) {
            ajaxSb.append("<ajax-callback-url>");
            ajaxSb.append(guc.getAppContext().getAjaxCallbackUrl()).append('?');
            ajaxSb.append("gatewayUserContextKey");
            ajaxSb.append('=').append(guc.getCacheKey());
            ajaxSb.append("</ajax-callback-url>");
        }

        if (eid == null) {
            rslt.append(">");
            
            // ajax callback
            rslt.append(ajaxSb);

            // All.
            rslt.append(guc.evaluateAllEntries(!useAjax));
        } else {
            rslt.append(" has-back=\"").append(guc.getAppContext().getSsoEntries().length > 1).append("\">");
            
            // ajax callback
            rslt.append(ajaxSb);
            
            // One.
            SsoEntry entry = guc.getAppContext().getSsoEntry(eid);
            rslt.append(sh.evaluate(entry, userattribs, guc.getCurrentSeq(entry),
                !useAjax));
        }
        rslt.append("</navigation>");
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
