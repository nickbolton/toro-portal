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

import net.unicon.academus.apps.SsoEntry;
import net.unicon.academus.apps.SsoHandler;
import net.unicon.academus.apps.XHTMLFilter.XHTMLFilterConfig;
import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Principal;
import net.unicon.warlock.IApplicationContext;

import java.util.HashSet;

public class WebContentApplicationContext implements IApplicationContext {

    // Instance Members.
    private final AccessBroker ssoBroker;
    private final AccessBroker contentBroker;
    private final String bodyXpath;
    private final String inputTags;
    private final UrlRewritingRule[] rules;
    private final XHTMLFilterConfig xhtmlFilterConfig;
    private String ajaxCallbackUrl = null;
    private SsoHandler handler;

    /*
     * Public API.
     */

    public WebContentApplicationContext(AccessBroker ssoBroker,
                        AccessBroker contentBroker, String bodyXpath
                        , String inputTags 
                        , XHTMLFilterConfig xhtmlFilterConfig
                        , UrlRewritingRule[] rules
                        , SsoHandler handler
                        , String ajaxCallbackUrl) {

        // Assertions.
        if (ssoBroker == null) {
            String msg = "Argument 'ssoBroker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (contentBroker == null) {
            String msg = "Argument 'contentBroker' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (bodyXpath == null) {
            String msg = "Argument 'bodyXpath' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.ssoBroker = ssoBroker;
        this.contentBroker = contentBroker;
        this.bodyXpath = bodyXpath;
        this.inputTags = inputTags;
        this.xhtmlFilterConfig = xhtmlFilterConfig;
        this.rules = new UrlRewritingRule[rules.length];
        this.ajaxCallbackUrl = ajaxCallbackUrl;
        this.handler = handler;
        System.arraycopy(rules, 0, this.rules, 0, rules.length);
    }

    public SsoEntry[] getSsoEntries(Principal p) {
        IAccessEntry[] entries = ssoBroker.getEntries(p
                , new AccessRule[] {new AccessRule(WebContentAccessType.VIEW, true)});
        HashSet set = new HashSet();
        for (int i=0; i < entries.length; i++) {
            set.add(entries[i].getTarget());
        }
        return (SsoEntry[]) set.toArray(new SsoEntry[0]);
    }

    public AccessBroker getBroker() {
        return contentBroker;
    }

    public String getBodyXpath() {
        return bodyXpath;
    }
    
    public String getInputTags() {
        return this.inputTags;
    }
    
    public XHTMLFilterConfig getFilterConfig() {
       return this.xhtmlFilterConfig;
    }

    public UrlRewritingRule[] getUrlRewritingRules() {
        UrlRewritingRule[] rslt = new UrlRewritingRule[rules.length];
        System.arraycopy(rules, 0, rslt, 0, rules.length);
        return rslt;
    }

    public boolean isAjaxFormPopulation() {
        return this.ajaxCallbackUrl != null;
    }

    public String getAjaxCallbackUrl() {
        return this.ajaxCallbackUrl;
    }

    public SsoHandler getSsoHandler() {
        return handler;
    }

}
