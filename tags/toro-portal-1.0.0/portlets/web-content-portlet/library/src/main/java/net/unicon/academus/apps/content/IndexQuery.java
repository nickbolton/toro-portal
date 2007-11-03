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

package net.unicon.academus.apps.content;

import net.unicon.academus.apps.SsoHandler;
import net.unicon.academus.apps.SsoHandlerXML;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;
import org.dom4j.DocumentException;

public class IndexQuery implements IStateQuery {
    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory
         .getLog(IndexQuery.class);

    // Instance members.
    private final WebContentApplicationContext app;
    private final WebContentUserContext user;

    /*
     * Public API.
     */

    public IndexQuery(WebContentApplicationContext app, WebContentUserContext user) {

        // Assertions.
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (user == null) {
            String msg = "Argument 'user' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.app = app;
        this.user = user;

    }

    public String query() throws WarlockException {

        Web web = user.getCurrentWeb();
        String nextDoc = user.getCurrentPath();
        boolean useAjax = app.isAjaxFormPopulation();

        // Begin.
        StringBuffer rslt = new StringBuffer();
        rslt.append("<state>");

        if (useAjax) {
            rslt.append("<ajax-callback-url>");
            rslt.append(app.getAjaxCallbackUrl()).append('?');
            rslt.append("gatewayUserContextKey");
            rslt.append('=').append(user.getCacheKey());
            rslt.append("</ajax-callback-url>");
        }


        // AccessType.
        rslt.append("<accesstype>Edit</accesstype>");

        // SsoTarget(s).
        SsoHandler handler = new SsoHandlerXML();

        Web.DocumentData doc = null;
        try {
           doc = web.getDocument(nextDoc, app.getBodyXpath(), app.getInputTags(), app.getFilterConfig());
        } catch (Exception ex) {
           log.error("Error occurred during document retrieval: "+ex.getMessage(), ex);
           rslt.append("<status><error type=\"other\">")
              .append("<problem>There was an error handling your request: ");
           if (ex.getCause() instanceof DocumentException) {
              rslt.append(EntityEncoder.encodeEntities(ex.getCause().getMessage()));
           } else {
              rslt.append(EntityEncoder.encodeEntities(ex.getMessage()));
           }
           rslt.append("</problem><solution>")
              .append("Please contact a systems administrator for assistance.</solution>")
              .append("</error></status>");
           rslt.append("<body><a href=\"")
           .append(user.getCurrentWeb().getDefaultDocument())
           .append("\">Return to index page.</a></body>");
           // rethrowing exception with a meaningful message
           throw new WarlockException("IndexQuery.query(): doc returned null. ", ex);
        }

        user.addAttributes(doc.getAttributes());
        
        if (doc != null) {
            rslt.append(user.evaluateAllEntries(!useAjax));

	        // UrlRewriting.
	        UrlRewritingRule[] rules = app.getUrlRewritingRules();
	        for (int i=0; i < rules.length; i++) {
	            rules[i].toXml(rslt);
	        }
	
	        // Body.
	        rslt.append(doc.getBody());
        }

        // End.
        rslt.append("</state>");

        if (log.isDebugEnabled()) {
           log.debug("State Query: "+rslt);
        }

        return rslt.toString();
    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        return new IDecisionCollection[0];
    }

}
