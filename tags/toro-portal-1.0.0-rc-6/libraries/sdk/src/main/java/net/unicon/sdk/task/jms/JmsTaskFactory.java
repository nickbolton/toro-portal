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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.dom4j.Element;
import org.dom4j.Node;

import net.unicon.sdk.task.WorkflowException;
import net.unicon.sdk.task.IPayload;
import net.unicon.sdk.task.ITask;
import net.unicon.sdk.task.ITaskFactory;
import net.unicon.sdk.task.ITaskResult;
import net.unicon.sdk.task.ITransactionContext;

/**
 * An implementation of <code>ITaskFactory</code> whose tasks initiate JMS
 * messages.  The present implementation supports only the Pub/Sub model, and
 * only text messages.
 */
public final class JmsTaskFactory implements ITaskFactory {

    // private static final String IMPORT_SUBSCRIBER = "unicon-importer";

    private IConnectionBroker cBroker;
    private ITopicBroker tBroker;

    /*
     * Public API.
     */

    /**
     * Creates a new <code>JmsTaskFactory</code> from parameters specified in an
     * XML element.  This is a <i>&lt;factory&gt;</i> element as defined by the
     * UNICON Workflow Toolkit XML specification.  <code>JmsTaskFactory</code>
     * requires the following content model:<p>
     *
     * <blockquote><code>
     * &lt;!ELEMENT factory (connection-broker, topic-broker)&gt;<br>
     * &lt;!ATTLIST factory impl CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT connection-broker ANY&gt;<br>
     * &lt;!ATTLIST connection-broker impl CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT topic-broker ANY&gt;<br>
     * &lt;!ATTLIST topic-broker impl CDATA #REQUIRED&gt;<br>
     * </code></blockquote>
     *
     * The expression <code>factory/@impl</code> must be the fully-qualified
     * name of this class (otherwise the specified implementation will be used).
     * Use the expressions <code>connection-broker/@impl</code> and
     * <code>topic-broker/@impl</code> to specify the desired
     * <code>IConnectionBroker</code> and <code>ITopicBroker</code>
     * implementations respectively.
     *
     * @param e A dom4j <code>Element</code>.
     * @throws WorkflowException If the <code>JmsTaskFactory</code> could not
     * create it's connection broker or its topic broker.
     */
    public JmsTaskFactory(Element e) throws WorkflowException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // cBroker.
        Element cb = (Element) e.selectSingleNode("connection-broker");
        try {
            Class impl = Class.forName(cb.attributeValue("impl"));
            Class[]  args = new Class[] { org.dom4j.Element.class };
            Constructor c = impl.getConstructor(args);
            this.cBroker = (IConnectionBroker) c.newInstance(new Object[] { cb });
        } catch (Throwable t) {
            String msg = "Unable to create the specified connection broker.";
            throw new WorkflowException(msg, t);
        }

        // tBroker.
        Element tb = (Element) e.selectSingleNode("topic-broker");
        try {
            Class impl = Class.forName(tb.attributeValue("impl"));
            Class[]  args = new Class[] { org.dom4j.Element.class };
            Constructor c = impl.getConstructor(args);
            this.tBroker = (ITopicBroker) c.newInstance(new Object[] { tb });
        } catch (Throwable t) {
            String msg = "Unable to create the specified topic broker.";
            throw new WorkflowException(msg, t);
        }

    }

    /**
     * Parses an import task from the specified XML element.  The
     * <code>JmsTaskFactory</code> requires the following content model:<p>
     *
     * <blockquote><code>
     * &lt;!ELEMENT task (message)&gt;<br>
     * &lt;!ATTLIST task factory CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT message (string-property*, text)&gt;<br>
     * &lt;!ATTLIST message topic CDATA #REQUIRED<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;jmstype CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT string-property (#PCDATA)&gt;<br>
     * &lt;!ATTLIST string-property name CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT text (#PCDATA | value-of+)*&gt;<br>
     * &lt;!ATTLIST text name CDATA #REQUIRED&gt;<br>
     * <br>
     * &lt;!ELEMENT value-of EMPTY&gt;<br>
     * &lt;!ATTLIST value-of xpath CDATA #REQUIRED&gt;<br>
     * </code></blockquote>
     *
     * Per the UNICON Workflow Toolkit XML specification, the expression
     * <code>'task/@factory'</code> references a <code>JmsTaskFactory</code>
     * instance.  Use <i>&lt;value-of&gt;</i> elements to include information
     * from the import payload within your message text.
     *
     * @param e A dom4j element.
     * @return An import task based upon the specified XML.
     * @throws WorkflowException If the task factory cannot create an import
     * task from the specified instructions.
     */
    public ITask parseImportTask(Element e) throws WorkflowException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return new JmsTask(e, this, tBroker);

    }

    /**
     * Creates and returns a new transaction context that can govern the tasks
     * created by this factory.
     *
     * @return A new transaction context.
     * @throws WorkflowException If the task factory cannot initiate a
     * transaction context.
     */
    public ITransactionContext createTransactionContext()
                            throws WorkflowException {

        return new JmsTransactionContext(cBroker);

    }

    /*
     * Implementation.
     */

    private final class JmsTask implements ITask {

        // Instance Members.
        private final String name;
        private String topic;
        private String jmsType;
        private Map properties;
        private MessageToken[] tokens;
        private ITaskFactory fac;
        private ITopicBroker tBroker;

        /*
         * Public API.
         */

        public JmsTask(Element e, ITaskFactory f, ITopicBroker tBroker)
                                            throws WorkflowException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (f == null) {
                String msg = "Argument 'f [ITaskFactory]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (tBroker == null) {
                String msg = "Argument 'tBroker' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // In case of error...
            String msg = null;

            // Iterator...
            Iterator it = null;

            // name.
            this.name = e.attributeValue("name");

            // topic.
            Element m = (Element) e.selectSingleNode("message");
            this.topic = m.attributeValue("topic");

            // jmsType.
            this.jmsType = m.attributeValue("jmstype");

            // (String) Properties.
            this.properties = new Hashtable();
            it = m.selectNodes("string-property").iterator();
            while (it.hasNext()) {
                Element sp = (Element) it.next();
                properties.put(sp.attributeValue("name"), sp.getText());
            }

            // Message tokens.
            Element txt = (Element) m.selectSingleNode("text");
            List l = new ArrayList();
            it = txt.content().iterator();
            while (it.hasNext()) {
                Node n = (Node) it.next();
                switch (n.getNodeType()) {
                    case Node.TEXT_NODE:
                        l.add(new StringMessageToken(n.getText()));
                        break;
                    case Node.ELEMENT_NODE:
                        Element ref = (Element) n;
                        if (!ref.getName().equals("value-of")) {
                            msg = "The <message> element supports only "
                                        + "<value-of> child elements.";
                            throw new WorkflowException(msg);
                        }
                        String xpath = ref.attributeValue("xpath");
                        l.add(new XPathMessageToken(xpath));
                        break;
                    default:
                        msg = "Unsupported child node type found under message "
                                                                + "element";
                        throw new WorkflowException(msg);
                }
            }
            int z = l.size();
            this.tokens = (MessageToken[]) l.toArray(new MessageToken[z]);

            // fac.
            this.fac = f;

            // tBroker.
            this.tBroker = tBroker;

        }

        public String getName() {
            return name;
        }

        public ITaskFactory factory() {
            return fac;
        }

        public ITaskResult performTask(IPayload p, ITransactionContext ctx)
                                                throws WorkflowException {

            // Assertions.
            if (p == null) {
                String msg = "Argument 'p [IPaylod]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (ctx == null) {
                String msg = "Argument 'ctx' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Message text.
            StringBuffer txt = new StringBuffer();

            // Construct the text.
            for (int i=0; i < tokens.length; i++) {
                MessageToken mt = tokens[i];
                txt.append(mt.getToken(p.toXml()));
            }

            try {
                // Build the JMS message.
                TopicSession s = ((JmsTransactionContext) ctx).getSession();
                TextMessage m = s.createTextMessage();
                m.setJMSType(jmsType);
                Iterator it = properties.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    m.setStringProperty(key, (String) properties.get(key));
                }
                m.setText(txt.toString());

                // Publish the message.
                TopicPublisher pub = s.createPublisher(tBroker.getTopic(topic));
                pub.publish(m);
            } catch (final JMSException jmse) {
                final String msg = "Unable to send the following JMS message:  "
                                                            + txt.toString();
                return new ITaskResult() {
                    public String getName() { return name; }
                    public boolean isFailure() { return true; }
                    public String getMessage() { return msg; }
                    public Throwable cause() { return jmse; }
                };
            }

            // Indicate success if we've come this far.
            return new ITaskResult.SuccessfulTaskResult(name);

        }

        /*
         * Implementation.
         */

        private abstract class MessageToken {

            /*
             * Public API.
             */

            public abstract String getToken(Node n);

        }

        private final class StringMessageToken extends MessageToken {

            private String value;

            /*
             * Public API.
             */

            public StringMessageToken(String s) {

                // Assertions.
                if (s == null) {
                    String msg = "Argument 's [String]' cannot be null.";
                    throw new IllegalArgumentException(msg);
                }

                this.value = s;

            }

            public String getToken(Node n) {

                // Assertions.
                if (n == null) {
                    String msg = "Argument 'n [Node]' cannot be null.";
                    throw new IllegalArgumentException(msg);
                }

                return value;

            }

        }

        private final class XPathMessageToken extends MessageToken {

            private String xpath;

            /*
             * Public API.
             */

            public XPathMessageToken(String xpath) {

                // Assertions.
                if (xpath == null) {
                    String msg = "Argument 'xpath' cannot be null.";
                    throw new IllegalArgumentException(msg);
                }

                this.xpath = xpath;

            }

            public String getToken(Node n) {

                // Assertions.
                if (n == null) {
                    String msg = "Argument 'n [Node]' cannot be null.";
                    throw new IllegalArgumentException(msg);
                }

                return n.selectSingleNode(xpath).getText();

            }

        }

    }

    private final class JmsTransactionContext implements ITransactionContext {

        // Instance Members.
        private TopicConnection conn;
        private TopicSession session;

        /*
         * Public API.
         */

        public JmsTransactionContext(IConnectionBroker broker)
                                throws WorkflowException {

            // Assertions.
            if (broker == null) {
                String msg = "Argument 'broker' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.conn = broker.newConnection();
            try {
                this.session = conn.createTopicSession(true,
                            TopicSession.AUTO_ACKNOWLEDGE);
            } catch (JMSException jmse) {
                String msg = "Unable to initiate a JMS session.";
                throw new WorkflowException(msg, jmse);
            }

        }

        public void commit() throws WorkflowException {

            try {
                session.commit();
                session.close();
                conn.close();
            } catch (JMSException jmse) {
                String msg = "Error encountered while committing the "
                                                    + "transaction.";
                throw new WorkflowException(msg, jmse);
            }

        }

        public void rollback() throws WorkflowException {

            try {
                session.rollback();
                session.close();
                conn.close();
            } catch (JMSException jmse) {
                String msg = "Error encountered while rolling back the "
                                                    + "transaction.";
                throw new WorkflowException(msg, jmse);
            }

        }

        public TopicConnection getConnection() {
            return conn;
        }

        public TopicSession getSession() {
            return session;
        }

    }

}
