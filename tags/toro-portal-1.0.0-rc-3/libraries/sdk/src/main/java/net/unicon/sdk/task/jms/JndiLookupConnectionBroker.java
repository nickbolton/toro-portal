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

package net.unicon.sdk.task.jms;

import java.util.Hashtable;
import java.util.Iterator;
import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicConnection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.dom4j.Element;

import net.unicon.sdk.task.WorkflowException;

/**
 * Brokers JMS <code>Connection</code> instances by obtaining a reference to a
 * <code>TopicConnectionFactory</code> from JNDI and using it to create new
 * connections.
 */
final class JndiLookupConnectionBroker implements IConnectionBroker {

    private TopicConnectionFactory fac;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>JndiLookupConnectionBroker</code> from parameters
     * specified in an XML element.  This element uses the following content
     * model:<p>
     *
     * <blockquote><code>
     * &lt;!ELEMENT connection-broker (env*, jndiname)&gt;<br>
     * &lt;!ATTLIST connection-broker impl CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT env (#PCDATA)&gt;<br>
     * &lt;!ATTLIST env variable CDATA #REQUIRED&gt;
     * <br>
     * &lt;!ELEMENT jndiname (#PCDATA)&gt;<br>
     * </code></blockquote>
     *
     * The expression <code>connection-broker/@impl</code> must be the
     * fully-qualified name of this class (otherwise the specified
     * implementation will be used).  The <i>&lt;env&gt;</i> element(s) define
     * the necessary environment variables for obtaining the initial JNDI
     * context.
     *
     * @param e A dom4j <code>Element</code>.
     * @throws WorkflowException If the JMS <code>ConnectionFactory</code>
     * specified in the XML could nt be located.
     */
    public JndiLookupConnectionBroker(Element e) throws WorkflowException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Prepare the environment.
        Hashtable env = new Hashtable();
        Iterator it = e.selectNodes("env").iterator();
        while (it.hasNext()) {
            Element var = (Element) it.next();
            env.put(var.attributeValue("variable"), var.getTextTrim());
        }

        try {
            // Find the ConnectionFactory.
            Context ctx = new InitialContext(env);
            Element jName = (Element) e.selectSingleNode("jndiname");
            this.fac = (TopicConnectionFactory) ctx.lookup(jName.getTextTrim());
        } catch (NamingException ne) {
            String msg = "Unable to lookup the specified ConnectionFactory.";
            throw new WorkflowException(msg, ne);
        }

    }

    /**
     * Provides a new JMS <code>Connection</code> instance.
     *
     * @return A new <code>Connection</code>.
     * @throws WorkflowException If the connection broker failed to create a
     * <code>Connection</code>.
     */
    public TopicConnection newConnection() throws WorkflowException {

        try {
            return fac.createTopicConnection();
        } catch (JMSException jmse) {
            String msg = "Unable to create a JMS connection.";
            throw new WorkflowException(msg, jmse);
        }

    }

}
