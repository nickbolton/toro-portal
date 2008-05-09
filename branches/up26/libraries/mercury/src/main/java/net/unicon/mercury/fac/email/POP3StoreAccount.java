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

package net.unicon.mercury.fac.email;

import java.util.Map;

import net.unicon.mercury.Features;
import net.unicon.mercury.MercuryException;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Element;
import org.dom4j.Attribute;

/**
 * Account information for a JavaMail Store implementing the POP3 Protocol.
 *
 * @author eandresen
 */
public class POP3StoreAccount extends EmailStoreAccount {
    boolean apop = false;

    public POP3StoreAccount() {
        super("pop3");
    }

    /**
     * Get the apop property.
     * @see #setApop(boolean)
     */
    public boolean getApop() { return apop; }
    /**
     * Set the apop property.
     *
     * APOP determines if the POP3 client should use the APOP authentication
     * method. APOP sends a digest of the password rather than clear text.
     *
     * APOP is not supported by all POP3 servers.
     *
     * Defaults to false.
     */
    public void setApop(boolean val) {
        this.apop = val;
    }

    public int getFeatures() {
        return Features.ATTACHMENTS;
    }

    public void parse(Element e) throws MercuryException {
        Attribute t = e.attribute("apop");
        if (t != null)
            setApop("true".equalsIgnoreCase(t.getValue()));
    }

    public void parse(IDecisionCollection dc) throws Exception
    {
        super.parse(dc);
        IDecision decision;

        decision = dc.getDecision("apop");
        if (decision != null) { // Optional
            setApop(((String)decision.getFirstSelectionHandle())
                    .equalsIgnoreCase("true"));
        }
    }
}
