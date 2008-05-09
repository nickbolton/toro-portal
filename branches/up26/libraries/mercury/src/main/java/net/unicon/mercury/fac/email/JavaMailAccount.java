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

import javax.mail.PasswordAuthentication;
import javax.mail.URLName;

import net.unicon.mercury.MercuryException;
import net.unicon.mercury.XmlFormatException;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Base class to provide common settings accross both Store and Transport
 * JavaMail accounts.
 *
 * @author eandresen
 */
public abstract class JavaMailAccount {
    private String type = null;
    private String proto = null;
    private int port = 0;
    private String host = "";
    private String path = "";
    private String user = "";
    private String password = "";

    // Default timesout to 30 seconds.
    private int connectionTimeout = 30000;
    private int timeout = 30000;

    /**
     * Construct a JavaMailAccount of the given type and protocol.
     * @param type Either "store" or "transport".
     * @param proto Protocol implemented.
     */
    protected JavaMailAccount(String type, String proto) {
        assert type != null : "Argument 'type' cannot be null.";
        assert proto != null : "Argument 'proto' cannot be null";
        if (type.equals(""))
            throw new IllegalArgumentException("Argument 'type' cannot be empty");
        if (proto.equals(""))
            throw new IllegalArgumentException("Argument 'proto' cannot be empty");

        this.proto = proto;
        this.type = type;
    }

    /**
     * Get the type of this account.
     * @return Type of this account. One of: "store", "transport".
     */
    public String getType() {
        return this.type;
    }

    /**
     * Get the username.
     * @return Username for this account.
     */
    public String getUsername() { return user; }
    /**
     * Set the username.
     * @param user Username for this account, or null for none.
     */
    public void setUsername(String user) {
        this.user = user;
        if (this.user == null)
            this.user = "";
    }

    /**
     * Get the password.
     * @return Password for this account.
     */
    public String getPassword() { return password; }
    /**
     * Set the password.
     * @param password Password for this account, or null for none.
     */
    public void setPassword(String password) {
        this.password = password;
        if (this.password == null)
            this.password = "";
    }

    /**
     * Initial path on remote host.
     * @return Initial path on remote host.
     */
    public String getPath() { return path; }
    /**
     * Set the initial path on the remote host.
     * You won't need to set this in most cases.
     * @param path Initial path on the remote host.
     */
    public void setPath(String path) {
        this.path = path;
        if (this.path == null)
            this.path = "";
    }

    /**
     * Get the server hostname.
     * @return Hostname of remote host.
     */
    public String getHostname() { return host; }
    /**
     * Set the remote server hostname.
     * @param host Hostname of remote server.
     */
    public void setHostname(String host) {
        this.host = host;
        if (this.host == null)
            this.host = "";
    }

    /**
     * Get the port.
     * @return port to connect to, or 0 if protocol default.
     */
    public int getPort() { return port; }
    /**
     * Set the port.
     * @param port Port to connect to, or 0 to use the protocol default.
     */
    public void setPort(int port) {
        this.port = port;
        if (this.port < 0 || this.port > 65535)
            this.port = 0;
    }
    private void setPort(Integer port) {
        if (port != null)
            setPort(port.intValue());
    }

    /**
     * Get the connection timeout value.
     * @return Connection timeout value.
     */
    public int getTimeout() { return timeout; }
    /**
     * Set the connection timeout value.
     * Defaults to 0.
     * @param timeout Connection timeout value, or 0 for indefinite.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
        if (this.timeout < 0)
            this.timeout = 0;
    }
    private void setTimeout(Integer timeout) {
        if (timeout != null)
            setTimeout(timeout.intValue());
    }

    /**
     * Get the protocol this account is for.
     * @return Protocol represented by this account.
     */
    public String getProtocol() { return proto; }

    /**
     * Retrieve a JavaMail PasswordAuthentication object using the account's
     * username and password.
     * @return new PasswordAuthentication object
     */
    public PasswordAuthentication getAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    /**
     * Get the URL of the service to connect to.
     * @return URL of the remote service.
     */
    public URLName getURLName() {
        return new URLName(proto, host, port, path, user, password);
    }

    /**
     * Parse a service URL into account settings.
     * @param url URL to parse
     */
    public void fromURLName(URLName url) {
        if (!url.getProtocol().equals(this.getProtocol()))
            throw new IllegalArgumentException("Argument 'url' is not of the "
                                             + "matching protocol type");
        setUsername(url.getUsername());
        setPort( url.getPort() == -1 ? 0 : url.getPort() );
        setHostname(url.getHost());
        setPassword(url.getPassword());
    }

    /**
     * Retrive the Properties of this account.
     * @return a Properties object suitable for use in a JavaMail Session
     *         instance.
     */
    public Properties getProperties() {
        Properties props = new Properties();
        String buf = "mail."+proto;

        props.put("mail."+getType()+".protocol", getProtocol());
        props.put(buf+".host", getHostname());
        if (getPort() != 0) {
            props.put(buf+".port", String.valueOf(getPort()));
        }
        props.put(buf+".user", getUsername());

        return props;
    }

    /**
     * Configure the account based on an XML fragment.
     * @param e Element representing the top node of the XML fragment.
     */
    public void parse(Element e) throws MercuryException {
        Element e2 = (Element)e.selectSingleNode("host");
        if (e2 == null)
            throw new XmlFormatException(
                    "Element <host> required.");
        setHostname(e2.getText());

        e2 = (Element)e.selectSingleNode("port");
        if (e2 != null)
            setPort(Integer.parseInt(e2.getText()));

        e2 = (Element)e.selectSingleNode("username");
        if (e2 != null)
            setUsername(e2.getText());

        e2 = (Element)e.selectSingleNode("password");
        if (e2 != null)
            setPassword(e2.getText());

        Attribute t = e.attribute("timeout");
        if (t != null)
            setTimeout(Integer.parseInt(t.getValue()));
    }

    /**
     * Configure the account based on a series of user input choices.
     * @param dc User chosen decisions
     */
    public void parse(IDecisionCollection dc) throws Exception
    {
        IDecision decision;

        setHostname((String)dc.getDecision("hostname").getFirstSelectionValue());
        setUsername((String)dc.getDecision("username").getFirstSelectionValue());
        setPassword((String)dc.getDecision("password").getFirstSelectionValue());
        setPort((Integer)dc.getDecision("port").getFirstSelectionValue());

        decision = dc.getDecision("timeout");
        if (decision != null) { // Optional
            setTimeout((Integer)decision.getFirstSelectionValue());
        }
    }
}

