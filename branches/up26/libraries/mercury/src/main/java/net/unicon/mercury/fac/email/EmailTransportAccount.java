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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.unicon.mercury.MercuryException;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Element;

/**
 * Account information for a JavaMail Transport.
 *
 * @author eandresen
 */
class EmailTransportAccount extends JavaMailAccount {
    private String from = "";

    public EmailTransportAccount(String proto) {
        super("transport", proto);
    }

    /**
     * Get the "From:" address for the transport.
     * @return the "From:" address for the transport.
     * @see #setFrom(String)
     */
    public String getFrom() { return from; }
    /**
     * Set the "From:" address for the transport.
     *
     * This property must be set for outgoing mail to be correctly identified.
     * Failure to set this property may result in failure to send mail.
     *
     * There is no default.
     */
    public void setFrom(String from) {
        this.from = from;
        if (this.from == null)
            this.from = "";
    }

    public Properties getProperties() {
        Properties props = super.getProperties();
        props.put("mail.from", getFrom());

        return props;
    }

    public void parse(Element e) throws MercuryException {
        super.parse(e);

        Element el = (Element)e.selectSingleNode("from");
        if (el != null)
            setFrom(el.getText());
    }

    public void parse(IDecisionCollection dc) throws Exception
    {
        super.parse(dc);

        // Required.
        setFrom((String)dc.getDecision("from").getFirstSelectionValue());
    }
}

