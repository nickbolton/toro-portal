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
import java.util.Properties;

import net.unicon.mercury.MercuryException;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Account information for a JavaMail Transport implementing the SMTP Protocol.
 *
 * @author eandresen
 */
public class SMTPTransportAccount extends EmailTransportAccount {
    boolean quitwait = true;
    boolean ehlo = true;

    /**
     * Construct an SMTPTransportAccount with all defaults.
     */
    public SMTPTransportAccount() {
        super("smtp");
    }

    /**
     * Get the quitwait property.
     * @see #setQuitWait(boolean)
     */
    public boolean getQuitWait() { return quitwait; }
    /**
     * Set the quitwait property.
     *
     * QuitWait determines if the SMTP client should wait for a response from
     * the server after issuing a QUIT command.
     *
     * Defaults to true.
     */
    public void setQuitWait(boolean val) {
        this.quitwait = val;
    }

    /**
     * Get the Ehlo property.
     * @see #setEhlo(boolean)
     */
    public boolean getEhlo() { return ehlo; }
    /**
     * Set the Ehlo property.
     *
     * Ehlo determines in the SMTP client should use the extended HELO sequence
     * during server connection. This enables additional functionality such as
     * improved authentication support.
     *
     * Defaults to true.
     */
    public void setEhlo(boolean val) {
        this.ehlo = val;
    }

    public Properties getProperties() {
        Properties props = super.getProperties();

        props.put("mail."+getProtocol()+".quitwait",
                    String.valueOf(getQuitWait()));
        props.put("mail."+getProtocol()+".ehlo",
                    String.valueOf(getEhlo()));

        // Turn on SMTP authentication if a username and password have been
        // specified.
        if (!getUsername().equals("") && !getPassword().equals(""))
            props.put("mail."+getProtocol()+".auth", "true");

        return props;
    }

    public void parse(Element e) throws MercuryException {
        super.parse(e);

        Attribute t = e.attribute("quitwait");
        if (t != null) {
            setQuitWait("true".equalsIgnoreCase(t.getValue()));
        }

        t = e.attribute("ehlo");
        if (t != null) {
            setEhlo("true".equalsIgnoreCase(t.getValue()));
        }
    }

    public void parse(IDecisionCollection dc) throws Exception
    {
        super.parse(dc);
        IDecision decision = null;

        decision = dc.getDecision("quitwait");
        if (decision != null) { // Optional
            setQuitWait(((String)decision.getFirstSelectionHandle())
                    .equalsIgnoreCase("true"));
        }

        decision = dc.getDecision("ehlo");
        if (decision != null) { // Optional
            setEhlo(((String)decision.getFirstSelectionHandle())
                    .equalsIgnoreCase("true"));
        }
    }
}
