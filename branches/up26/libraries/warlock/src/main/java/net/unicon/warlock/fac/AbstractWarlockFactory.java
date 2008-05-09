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

package net.unicon.warlock.fac;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParserFactory;

import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IWarlockEntity;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.XMLReader;

import net.unicon.alchemist.EntityEncoder;

public abstract class AbstractWarlockFactory implements IWarlockFactory {

    /*
     * Public API.
     */

    /*
     * Protected API.
     */

    protected AbstractWarlockFactory() {}

    /*
     * Implementation.
     */

    private static ILayoutElement parseLayoutElement(Node n,
                                IWarlockFactory owner)
                                throws WarlockException {

        // Assertions.
        if (n == null) {
            String msg = "Argument 'n [Node]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (owner == null) {
            String msg = "Argument 'owner' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        ILayoutElement rslt = null;

        // Choose which type to create.
        switch (n.getNodeType()) {

            case Node.TEXT_NODE:
                rslt = IgnoredLayoutText.parse(n);
                break;
            case Node.ELEMENT_NODE:
                Element e = (Element) n;
                String tagName = e.getName();
                if (tagName.equals("value-of")) {
                    rslt = XpathValue.parse(e);
                } else if (tagName.equals("copy-of")) {
                    rslt = CopyOf.parse(e);
                } else if (tagName.equals("call-template")) {
                    rslt = TemplateCall.parse(e);
                } else {
                    rslt = IgnoredLayoutElement.parse(e, owner);
                }
                break;
            default:
                rslt = EmptyLayoutElement.INSTANCE;
                break;
        }

        return rslt;

    }

    /*
     * Nested Types.
     */

    private static abstract class BaseWarlockEntity implements IWarlockEntity {

    	private static Log log = LogFactory.getLog(BaseWarlockEntity.class);
    	
        // Instance Members.
        private final IWarlockFactory owner;

        /*
         * Public API.
         */

        public BaseWarlockEntity(IWarlockFactory owner) {

            // Assertions.
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.owner = owner;

        }

        public final IWarlockFactory getOwner() {
            return owner;
        }
        
        protected Log getLog() {
        	return log;
        }
        
    }

    public static abstract class AbstractAction extends BaseWarlockEntity
                                                implements IAction {
        
        // Instance Members.
        protected final Log log = LogFactory.getLog(getClass());

        private final Handle handle;
        private final String[] choices;
        
        /*
         * Public API.
         */

        public final Handle getHandle() {
            return handle;
        }

        public final String[] getRequiredChoices() {
            String rslt[] = new String[choices.length];
            System.arraycopy(choices, 0, rslt, 0, choices.length);
            return rslt;
        }

        /*
         * Protected API.
         */

        protected AbstractAction(IWarlockFactory owner, Handle handle,
                                                String[] choices) {

            super(owner);

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (choices == null) {
                String msg = "Argument 'choices' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = handle;
            this.choices = new String[choices.length];
            System.arraycopy(choices, 0, this.choices, 0, choices.length);

        }

    }

    protected static final class ScreenImpl extends BaseWarlockEntity
                                            implements IScreen {

        // Instance Members.
        private final Handle handle;
        private final Layout layout;
        private final Map actions;
        private final Map templates;

        /*
         * Public API.
         */

        public static IScreen parse(Element e, IWarlockFactory owner)
                                        throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("screen")) {
                String msg = "Argument 'e [Element]' must be a <screen> "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Handle.
            Attribute h = e.attribute("handle");
            if (h == null) {
                String msg = "Element <screen> is missing required attribute "
                                                            + "'handle'.";
                throw new XmlFormatException(msg);
            }
            Handle handle = Handle.create(h.getValue());

            // Layout.
            Element y = e.element("layout");
            if (y == null) {
                String msg = "Element <screen> is missing required child "
                                                + "element <layout>.";
                throw new XmlFormatException(msg);
            }
            Layout layout = Layout.parse(y, owner);

            // Actions.
            List list = e.selectNodes("descendant::action");
            IAction[] actions = new IAction[list.size()];
            for (int i=0; i < list.size(); i++) {
                Element a = (Element) list.get(i);
                Attribute impl = a.attribute("impl");
                if (impl == null) {
                    String msg = "Element <action> is missing required "
                                                + "attribute 'impl'.";
                    throw new XmlFormatException(msg);
                }
                try {
                    Class c = Class.forName(impl.getValue());
                    Method m = c.getDeclaredMethod(
                                    "parse",
                                    new Class[] {
                                        Element.class,
                                        IWarlockFactory.class
                                    });
                    actions[i] = (IAction) m.invoke(
                                    null,
                                    new Object[] {
                                        a,
                                        owner
                                    });
                } catch (Throwable t) {
                    String msg = "Unable to create the specified action:  "
                                                    + impl.getValue();
                    throw new XmlFormatException(msg, t);
                }
            }

            // Templates.
            list = e.selectNodes("template");
            Template[] templates = new Template[list.size()];
            for (int i=0; i < list.size(); i++) {
                Element m = (Element) list.get(i);
                templates[i] = Template.parse(m, owner);
            }

            return new ScreenImpl(handle, layout, actions, templates, owner);

        }

        public ScreenImpl(Handle handle, Layout layout, IAction[] actions,
                        Template[] templates, IWarlockFactory owner) {

            super(owner);

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (layout == null) {
                String msg = "Argument 'layout' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (actions == null) {
                String msg = "Argument 'actions' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (templates == null) {
                String msg = "Argument 'templates' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = handle;
            this.layout = layout;
            this.actions = new HashMap();
            Iterator it = Arrays.asList(actions).iterator();
            while (it.hasNext()) {
                IAction a = (IAction) it.next();
                // Make sure there aren't 2 actions w/ the same handle.
                if (this.actions.containsKey(a.getHandle())) {
                    String msg = "Handle must be unique in context.  Duplicate "
                                    + "handle:  " + a.getHandle().getValue();
                    throw new IllegalArgumentException(msg);
                }
                this.actions.put(a.getHandle(), a);
            }
            this.templates = new HashMap();
            it = Arrays.asList(templates).iterator();
            while (it.hasNext()) {
                Template m = (Template) it.next();
                // Make sure there aren't 2 actions w/ the same handle.
                if (this.templates.containsKey(m.getHandle())) {
                    String msg = "Handle must be unique in context.  Duplicate "
                                    + "handle:  " + m.getHandle().getValue();
                    throw new IllegalArgumentException(msg);
                }
                this.templates.put(m.getHandle(), m);
            }

        }

        public Handle getHandle() {
            return handle;
        }

        public IAction[] getActions() {
            return (IAction[]) actions.values().toArray(new IAction[0]);
        }

        public IAction getAction(Handle h) {

            // Assertions.
            if (!actions.containsKey(h)) {
                String msg = "Unrecognized action.  Screen does not contain an "
                        + "action with the specified handle:  " + h.getValue();
                throw new IllegalArgumentException(msg);
            }

            return (IAction) actions.get(h);

        }

        public Template getTemplate(Handle h) {

            // Assertions.
            if (!templates.containsKey(h)) {
                String msg = "Unrecognized template.  Screen does not contain "
                                + "a template with the specified handle:  "
                                + h.getValue();
                throw new IllegalArgumentException(msg);
            }

            return (Template) templates.get(h);

        }

        public String evaluate(IStateQuery q) throws WarlockException {

            // Assertions.
            if (q == null) {
                String msg = "Argument 'q [IStateQuery]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Query the state and convert to document.
            Document state = null;
            try {
                StringReader sr = new StringReader(q.query());
                state = new SAXReader(getXMLReader()).read(sr);
            } catch (Throwable t) {
                String msg = "Error querying application state.";
                throw new WarlockException(msg, t);
            }

            StringBuffer buff = new StringBuffer();
            layout.writeXml(buff, this, state);
            return buff.toString();

        }
        
        public void evaluate(IStateQuery q, ContentHandler ch)
        throws WarlockException {
            
            // Assertions.
            if (q == null) {
                String msg = "Argument 'q [IStateQuery]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Query the state and convert to document.
            Document state = null;
            try {
                StringReader sr = new StringReader(q.query());
                state = new SAXReader(getXMLReader()).read(sr);
                layout.writeXml(ch, this, state);
            } catch (Throwable t) {
                String msg = "Error querying application state.";
                throw new WarlockException(msg, t);
            }

        }

    }

    private interface ILayoutElement {

        void writeXml(StringBuffer buff, ScreenImpl screen, Node state);
        
        void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException;

    }

    private static final class Layout extends BaseWarlockEntity
                                implements ILayoutElement {

        // Instance Members.
        private final ILayoutElement[] elements;

        /*
         * Public API.
         */

        public static Layout parse(Element e, IWarlockFactory owner)
                                        throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("layout")) {
                String msg = "Argument 'e [Element]' must be a <layout> "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Elements.
            List list = e.content();
            ILayoutElement[] elements = new ILayoutElement[list.size()];
            for (int i=0; i < elements.length; i++) {
                Node n = (Node) list.get(i);
                elements[i] = AbstractWarlockFactory.parseLayoutElement(n,
                                                                owner);
            }

            return new Layout(elements, owner);

        }

        public Layout(ILayoutElement[] elements, IWarlockFactory owner) {

            super(owner);

            // Assertions.
            if (elements == null) {
                String msg = "Argument 'elements' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.elements = new ILayoutElement[elements.length];
            System.arraycopy(elements, 0, this.elements, 0, elements.length);

        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Begin.
            buff.append("<layout>");

            // Elements.
            Iterator it = Arrays.asList(elements).iterator();
            while (it.hasNext()) {
                ILayoutElement e = (ILayoutElement) it.next();
                e.writeXml(buff, screen, state);
            }

            // End.
            buff.append("</layout>");

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            
            final String layoutText = "layout";
            final String emptyText = "";
            AttributesImpl attributes = new AttributesImpl();

            // Begin.
            ch.startElement(emptyText, layoutText, layoutText, attributes);

            // Elements.
            Iterator it = Arrays.asList(elements).iterator();
            while (it.hasNext()) {
                ILayoutElement e = (ILayoutElement) it.next();
                e.writeXml(ch, screen, state);
            }

            // End.
            ch.endElement(emptyText, layoutText, layoutText);
        }
        
    }

    private static final class EmptyLayoutElement implements ILayoutElement {

        /*
         * Public API.
         */

        public static final ILayoutElement INSTANCE = new EmptyLayoutElement();

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {
            // do nothing at all...
        }

        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {
            // do nothing at all...
        }

        /*
         * Implementation.
         */

        private EmptyLayoutElement() {}

    }

    private static final class IgnoredLayoutElement implements ILayoutElement {

        // Instance Members.
        private final String name;
        private final Map attributes;
        private final ILayoutElement[] children;

        /*
         * Public API.
         */

        public static ILayoutElement parse(Element e, IWarlockFactory owner)
                                            throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            // NB:  Can get an ILE for *any* element type.
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Name.
            String name = e.getName();

            // Attributes.
            Iterator it = e.attributes().iterator();
            Map attributes = new HashMap();
            while (it.hasNext()) {
                Attribute a = (Attribute) it.next();
                // Choose static or dynamic.
                Object value = null;
                String str = a.getValue();
                if (str.startsWith("{") && str.endsWith("}")) {
                    String xpath = str.substring(1, str.length() - 1);
                    value = new XpathValue(xpath);
                } else {
                    value = str;
                }
                attributes.put(a.getName(), value);
            }

            // Children.
            List list = new ArrayList(e.content());    // defensive copy
            ILayoutElement[] children = new ILayoutElement[list.size()];
            for (int i=0; i < list.size(); i++) {
                Node n = (Node) list.get(i);
                children[i] = AbstractWarlockFactory.parseLayoutElement(n,
                                                                owner);
            }

            return new IgnoredLayoutElement(name, attributes, children);

        }

        public IgnoredLayoutElement(String name, Map attributes,
                                ILayoutElement[] children) {

            // Assertions.
            if (name == null) {
                String msg = "Argument 'name' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (attributes == null) {
                String msg = "Argument 'attributes' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (children == null) {
                String msg = "Argument 'children' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.name = name;
            this.attributes = new HashMap(attributes);
            this.children = new ILayoutElement[children.length];
            System.arraycopy(children, 0, this.children, 0, children.length);

        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Begin.
            buff.append("<").append(name);

            // Attributes.
            Iterator it = attributes.entrySet().iterator();
            while (it.hasNext()) {
            	Map.Entry en = (Map.Entry)it.next();
                String key = (String)en.getKey();
                Object value = en.getValue();
                
                buff.append(" ").append(key).append("=\"");
                if (value instanceof String) {
                	buff.append((String)value);
                } else {
                    ((ILayoutElement)value).writeXml(buff, screen, state);
                }
                buff.append('"');
            }

            // Children.
            if (children.length == 0) {
                buff.append(" />");
            } else {
                buff.append(">");
                for (int i = 0; i < children.length; i++) {
                    children[i].writeXml(buff, screen, state);
                }
                buff.append("</").append(name).append(">");
            }

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            final String emptyText = "";
            final String cdataText = "CDATA";
            AttributesImpl attrs = new AttributesImpl();

            // Begin.
            // Attributes.
            Iterator it = this.attributes.entrySet().iterator();
            StringBuffer sb = new StringBuffer();
            while (it.hasNext()) {
                Map.Entry en = (Map.Entry)it.next();
                String key = (String)en.getKey();
                Object value = en.getValue();
                
                if (value instanceof String) {
                    attrs.addAttribute(emptyText, key,
                        key, cdataText, (String)value);
                } else {
                    sb.setLength(0);
                    ((XpathValue)value).writeXml(sb, screen, state, false);
                    attrs.addAttribute(emptyText, key,
                        key, cdataText, sb.toString());
                }
            }
            
            ch.startElement(emptyText, name, name, attrs);

            // Children.
            for (int i = 0; i < children.length; i++) {
                children[i].writeXml(ch, screen, state);
            }
            
            ch.endElement(emptyText, name, name);
        }

    }

    private static final class IgnoredLayoutText implements ILayoutElement {

        // Instance Members.
        private final String value;

        /*
         * Public API.
         */

        public static ILayoutElement parse(Node n) throws WarlockException {

            // Assertions.
            if (n == null) {
                String msg = "Argument 'n [Node]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Value.
            String value = n.getText();

            return new IgnoredLayoutText(value);

        }

        public IgnoredLayoutText(String value) {

            // Assertions.
            if (value == null) {
                String msg = "Argument 'value' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.value = value;

        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            buff.append(EntityEncoder.encodeEntities(value));

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            ch.characters(value.toCharArray(), 0, value.length());
        }

    }

    private static final class XpathValue implements ILayoutElement {

        // Instance Members.
        private final String xpath;

        /*
         * Public API.
         */

        public static ILayoutElement parse(Element e) throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("value-of")) {
                String msg = "Argument 'e [Element]' must be a <value-of> "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Xpath.
            Attribute x = e.attribute("select");
            if (x == null) {
                String msg = "Element <value-of> is missing required attribute "
                                                                + "'select'.";
                throw new XmlFormatException(msg);
            }
            String xpath = x.getValue();

            return new XpathValue(xpath);

        }

        public XpathValue(String xpath) {

            // Assertions.
            if (xpath == null) {
                String msg = "Argument 'xpath' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.xpath = xpath;

        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {
            writeXml(buff, screen, state, true);
        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state, boolean encode) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (encode) {
            buff.append(EntityEncoder.encodeEntities(state.valueOf(xpath)));
            } else {
                buff.append(state.valueOf(xpath));
            }

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            
            String val = state.valueOf(xpath);
            ch.characters(val.toCharArray(), 0, val.length());

        }

    }

    private static final class CopyOf implements ILayoutElement {

        // Instance Members.
        private final String xpath;

        /*
         * Public API.
         */

        public static ILayoutElement parse(Element e) throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("copy-of")) {
                String msg = "Argument 'e [Element]' must be a <copy-of> "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Xpath.
            Attribute x = e.attribute("select");
            if (x == null) {
                String msg = "Element <copy-of> is missing required attribute "
                                                            + "'select'.";
                throw new XmlFormatException(msg);
            }
            String xpath = x.getValue();

            return new CopyOf(xpath);

        }

        public CopyOf(String xpath) {

            // Assertions.
            if (xpath == null) {
                String msg = "Argument 'xpath' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.xpath = xpath;

        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Copy everything that matches the expression.
            Iterator it = state.selectNodes(xpath).iterator();
            while(it.hasNext()) {
                Node n = (Node) it.next();
                buff.append(n.asXML()); // NB:  already XML, don't encode.
            }

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            
            // Copy everything that matches the expression.
            Iterator it = state.selectNodes(xpath).iterator();
            SAXWriter saxWriter = new SAXWriter(ch);
            while(it.hasNext()) {
                Node n = (Node) it.next();
                saxWriter.write(n);
            }

        }


    }

    private static final class TemplateCall implements ILayoutElement {

        // Instance Members.
        private final Handle template;
        private final String xpath;

        /*
         * Public API.
         */

        public static TemplateCall parse(Element e) throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("call-template")) {
                String msg = "Argument 'n [Node]' must be a <call-template> "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Template.
            Attribute m = e.attribute("handle");
            if (m == null) {
                String msg = "Element <call-template> is missing required "
                                                + "attribute 'handle'.";
                throw new XmlFormatException(msg);
            }
            Handle template = Handle.create(m.getValue());

            // Xpath.
            Attribute x = e.attribute("select");
            if (x == null) {
                String msg = "Element <call-template> is missing required "
                                                + "attribute 'select'.";
                throw new XmlFormatException(msg);
            }
            String xpath = x.getValue();

            return new TemplateCall(template, xpath);

        }

        public TemplateCall(Handle template, String xpath) {

            // Assertions.
            if (template == null) {
                String msg = "Argument 'template' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (xpath == null) {
                String msg = "Argument 'xpath' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.template = template;
            this.xpath = xpath;

        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            Template m = screen.getTemplate(template);

            Iterator it = state.selectNodes(xpath).iterator();
            while (it.hasNext()) {
                Node n = (Node) it.next();
                m.writeXml(buff, screen, n);
            }

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            Template m = screen.getTemplate(template);
            
            Iterator it = state.selectNodes(xpath).iterator();
            while (it.hasNext()) {
                Node n = (Node) it.next();
                m.writeXml(ch, screen, n);
            }

        }

    }

    private static final class Template {

        // Instance Members.
        private final Handle handle;
        private final ILayoutElement[] elements;

        /*
         * Public API.
         */

        public static Template parse(Element e, IWarlockFactory owner)
                                        throws WarlockException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("template")) {
                String msg = "Argument 'e [Element]' must be a <template> "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Handle.
            Attribute h = e.attribute("handle");
            if (h == null) {
                String msg = "Element <template> is missing required attribute "
                                                                + "'handle'.";
                throw new XmlFormatException(msg);
            }
            Handle handle = Handle.create(h.getValue());

            // Elements.
            List list = e.content();
            ILayoutElement[] elements = new ILayoutElement[list.size()];
            for (int i=0; i < elements.length; i++) {
                Node n = (Node) list.get(i);
                elements[i] = AbstractWarlockFactory.parseLayoutElement(n,
                                                    owner);
            }

            return new Template(handle, elements);

        }

        public Template(Handle handle, ILayoutElement[] elements) {

            // Assertions.
            if (handle == null) {
                String msg = "Argument 'handle' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (elements == null) {
                String msg = "Argument 'elements' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = handle;
            this.elements = new ILayoutElement[elements.length];
            System.arraycopy(elements, 0, this.elements, 0, elements.length);

        }

        public Handle getHandle() {
            return handle;
        }

        public void writeXml(StringBuffer buff, ScreenImpl screen, Node state) {

            // Assertions.
            if (buff == null) {
                String msg = "Argument 'buff' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            for (int i=0; i < elements.length; i++) {
                elements[i].writeXml(buff, screen, state);
            }

        }
        
        public void writeXml(ContentHandler ch, ScreenImpl screen, Node state)
        throws SAXException {

            // Assertions.
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (screen == null) {
                String msg = "Argument 'screen' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (state == null) {
                String msg = "Argument 'state' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            for (int i=0; i < elements.length; i++) {
                elements[i].writeXml(ch, screen, state);
            }

        }

    }

    private static Object saxParserLock = new Object();
    private static SAXParserFactory saxParserFactory = null;

    private static XMLReader getXMLReader() throws SAXException, javax.xml.parsers.ParserConfigurationException {
        XMLReader rslt = null;

        synchronized(saxParserLock) {
            if (saxParserFactory == null) {
                saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setValidating(false);
                saxParserFactory.setNamespaceAware(false);
            }

            rslt = saxParserFactory.newSAXParser().getXMLReader();
        }

        return rslt;
    }

}
