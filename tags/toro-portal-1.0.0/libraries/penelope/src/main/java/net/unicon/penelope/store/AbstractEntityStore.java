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

package net.unicon.penelope.store;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.unicon.alchemist.EntityEncoder;
import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IChoiceCollectionParser;
import net.unicon.penelope.IComplement;
import net.unicon.penelope.IComplementType;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.IPenelopeEntity;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.PenelopeException;
import net.unicon.penelope.XmlFormatException;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXContentHandler;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public abstract class AbstractEntityStore implements IEntityStore {

    /*
     * Public API.
     */

    /*
     * Protected API..
     */

    protected AbstractEntityStore() {}

    protected abstract ISequencer sequencer();
    
    private static final String XML_BEAN_FACTORY = 
        "org.springframework.beans.factory.xml.XmlBeanFactory";

    /*
     * Nested Types.
     */

    protected static abstract class AbstractEntity implements IPenelopeEntity {

        // Instance Members.
        private final long id;
        private final IEntityStore owner;
        

        /*
         * Public API.
         */

        public long getId() {
            return id;
        }

        public IEntityStore getOwner() {
            return owner;
        }

        public final IPenelopeEntity select(String expression) {

            if (expression == null) {
                String msg = "Argument 'expression' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            String[] tokens = expression.split("/");
            IPenelopeEntity rslt = null;
            try {
                rslt = this.doSelect(tokens, 0);
            } catch (PenelopeException pe) {
                String msg = "Invalid select expression for the current "
                                        + "context:  " + expression;
                throw new IllegalArgumentException(msg);
            }
            return rslt;

        }

        public final boolean equals(Object o) {

            // Handle nulls.
            if (o == null) {
                return false;
            }

            // ** Rule **
            // Any 2 IPenelopeEntity objects with
            // the same id and owner are equal.

            boolean rslt = false;   // default...

            if (o instanceof IPenelopeEntity) {
                IPenelopeEntity e = (IPenelopeEntity) o;
                rslt = (e.getId() == id && e.getOwner().equals(owner));
            }

            return rslt;

        }

        public int hashCode() {
            return id > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) id;
        }

        /*
         * Protected API.
         */

        protected AbstractEntity(long id, IEntityStore owner) {

            // Assertions.
            if (id < 1) {
                String msg = "Argument 'id' cannot be less than 1.";
                throw new IllegalArgumentException(msg);
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.id = id;
            this.owner = owner;

        }

        protected abstract IPenelopeEntity doSelect(String[] tokens,
                        int nextIndex) throws PenelopeException;

    }
    
    public IChoiceCollectionParser getChoiceCollectionParser(ContentHandler ch) {
        return new ChoiceCollectionSaxParser(this, ch);
    }
    
    protected static class OptionImpl extends AbstractEntity
                                    implements IOption {

        // Instance Members.
        private final Handle handle;
        private final Label label;
        private final IComplementType complementType;

        /*
         * Public API.
         */

        public static IOption parse(Element e, AbstractEntityStore owner)
                                            throws PenelopeException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("option")) {
                String msg = "Argument 'e' must be an <option> element.";
                throw new IllegalArgumentException(msg);
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Handle.
            Attribute h = e.attribute("handle");
            if (h == null) {
                String msg = "Element <option> is missing required attribute "
                                                            + "'handle'.";
                throw new XmlFormatException(msg);
            }
            Handle handle = Handle.create(h.getValue());

            // Label.
            Element l = e.element("label");
            Label label = null; // NB:  This is permissible.
            if (l != null) {
                label = Label.create(l.getText());
            }

            // ComplementType.
            IComplementType complementType = null;
            
            Attribute y = e.attribute("complement-type");
            if (y == null) {
                String msg = "Element <option> is missing required attribute "
                                                    + "'complement-type'.";
                throw new XmlFormatException(msg);
            }
            try {
                
                // We could dynamically gen the BeanFactory type, but
                // chances are it will be XmlBeanFactory
                if (XML_BEAN_FACTORY.equals(y.getValue())) {
                    Element beanElement = e.element("bean");
                    byte[] beanDef = beanElement.asXML().getBytes();
                    XmlBeanFactory factory = new XmlBeanFactory(
                            new ByteArrayResource(beanDef));
                    complementType = (IComplementType)factory.getBean(
                            "complementType", IComplementType.class);
                }
                else {
                    Class c = Class.forName(y.getValue());
                    Field f = c.getDeclaredField("INSTANCE");
                    complementType = (IComplementType) f.get(null);
                }
            } catch (Throwable t) {
                String msg = "Unable to obtain the specified compliment type:  "
                    + y.getValue();
                throw new EntityCreateException(msg);
            }

            return new OptionImpl(owner.sequencer().next(), owner, handle,
                                                label, complementType);
        }

        public OptionImpl(long id, IEntityStore owner, Handle h, Label l,
                                    IComplementType complementType) {

            super(id, owner);

            // Assertions.
            if (h == null) {
                String msg = "Argument 'h [Handle]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            // NB:  label may be null.
            if (complementType == null) {
                String msg = "Argument 'complementType' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.handle = h;
            this.label = l;
            this.complementType = complementType;

        }

        public Handle getHandle() {
            return handle;
        }

        public Label getLabel() {
            return label;
        }

        public IComplementType getComplementType() {
            return complementType;
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<option ");

            // Handle.
            rslt.append("handle=\"");
            rslt.append(EntityEncoder.encodeEntities(handle.getValue()));
            rslt.append("\" ");

            // ComplementType.
            rslt.append("complement-type=\"");
            rslt.append(complementType.getClass().getName());
            rslt.append("\">");

            // Label.
            if (label != null) {
                rslt.append("<label>");
                rslt.append(EntityEncoder.encodeEntities(label.getValue()));
                rslt.append("</label>");
            }

            // End.
            rslt.append("</option>");
            return rslt.toString();

        }
        
        public void sendXmlEvents(ContentHandler ch) throws SAXException {
            final String handleText = "handle";
            final String complementTypeText = "complement-type";
            final String labelText = "label";
            final String optionText = "option";
            final String cdataText = "CDATA";
            final String emptyText = "";
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(emptyText, handleText, handleText, cdataText,
                EntityEncoder.encodeEntities(handle.getValue()));
            attributes.addAttribute(emptyText, complementTypeText, complementTypeText,
                cdataText, complementType.getClass().getName());
            ch.startElement(emptyText, optionText, optionText, attributes);
            
            if (label != null) {
                attributes.clear();
                ch.startElement(emptyText, labelText, labelText, attributes);
                String labelString = EntityEncoder.encodeEntities(label.getValue());
                ch.characters(labelString.toCharArray(), 0,
                    labelString.length());
                ch.endElement(emptyText, labelText, labelText);
            }
            ch.endElement(emptyText, optionText, optionText);
        }

        /*
         * Protected API.
         */

        protected final IPenelopeEntity doSelect(String[] tokens, int nextIndex)
                                                throws PenelopeException {

            // Assertions.
            if (tokens == null) {
                String msg = "Argument 'tokens' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (nextIndex == tokens.length || tokens[nextIndex].equals(".")) {
                return this;
            } else {
                throw new PenelopeException("BAD EXPRESSION");
            }

        }

    }

    protected static class ChoiceImpl extends AbstractEntity
                                    implements IChoice {

        // Instance Members.
        private final Handle handle;
        private final Label label;
        private final Map options;
        private final int minSelections;
        private final int maxSelections;

        /*
         * Public API.
         */

        public static IChoice parse(Element e, AbstractEntityStore owner)
                                            throws PenelopeException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("choice")) {
                String msg = "Argument 'e' must be an <choice> element.";
                throw new IllegalArgumentException(msg);
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Handle.
            Attribute h = e.attribute("handle");
            if (h == null) {
                String msg = "Element <choice> is missing required attribute "
                                                            + "'handle'.";
                throw new XmlFormatException(msg);
            }
            Handle handle = Handle.create(h.getValue());

            // Label.
            Element l = e.element("label");
            Label label = null; // NB:  This is permissible.
            if (l != null) {
                label = Label.create(l.getText());
            }

            // Options.
            List list = e.selectNodes("descendant::option");
            IOption[] options = new IOption[list.size()];
            for (int i=0; i < list.size(); i++) {
                Element o = (Element) list.get(i);
                options[i] = OptionImpl.parse(o, owner);
            }

            // MinSelections.
            Attribute min = e.attribute("min-selections");
            if (min == null) {
                String msg = "Element <choice> is missing required attribute "
                                                    + "'min-selections'.";
                throw new XmlFormatException(msg);
            }
            int minSelections = -1;
            try {
                minSelections = Integer.parseInt(min.getValue());
            } catch (NumberFormatException nfe) {
                String msg = "Attribute 'min-selections' must be a valid "
                        + "integer.  Illegal value:  " + min.getValue();
                throw new XmlFormatException(msg);
            }

            // MaxSelections.
            Attribute max = e.attribute("max-selections");
            if (max == null) {
                String msg = "Element <choice> is missing required attribute "
                                                    + "'max-selections'.";
                throw new XmlFormatException(msg);
            }
            int maxSelections = -1;
            try {
                maxSelections = Integer.parseInt(max.getValue());
            } catch (NumberFormatException nfe) {
                String msg = "Attribute 'max-selections' must be a valid "
                        + "integer.  Illegal value:  " + max.getValue();
                throw new XmlFormatException(msg);
            }

            return new ChoiceImpl(owner.sequencer().next(), owner, handle,
                        label, options, minSelections, maxSelections);

        }

        public ChoiceImpl(long id, IEntityStore owner, Handle h, Label l,
                                    IOption[] options, int minSelections,
                                    int maxSelections)
                                    throws EntityCreateException {

            super(id, owner);

            // Assertions.
            if (h == null) {
                String msg = "Argument 'h [Handle]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            // NB:  label may be null.
            if (options == null) {
                throw new IllegalArgumentException(
                            new StringBuffer("Choice '")
                                .append(h.getValue())
                                .append("': Argument 'options' cannot be null.")
                                .toString());
            }
            if (minSelections < 0) {
                String msg = "Argument 'minSelections' cannot be less than "
                            + "zero.  Choice collection handle was:  "
                            + h;
                throw new IllegalArgumentException(msg);
            }
            if (minSelections > options.length) {
                String msg = "Argument 'minSelections' cannot be greater than "
                                        + "the number of options.  Choice "
                                        + "collection handle was:  " + h;
                throw new EntityCreateException(msg);
            }
            if (maxSelections < 0) {
                String msg = "Argument 'maxSelections' cannot be less than "
                            + "zero.  Choice collection handle was:  "
                            + h;
                throw new IllegalArgumentException(msg);
            }
            if (maxSelections != 0 && maxSelections < minSelections) {
                String msg = "Argument 'maxSelections' must either be zero or "
                                + "greater than or equal to minSelections.  "
                                + "Choice collection handle was:  " + h;
                throw new EntityCreateException(msg);
            }

            // Instance Members.
            this.handle = h;
            this.label = l;
            this.minSelections = minSelections;
            this.maxSelections = maxSelections;
            this.options = new HashMap(options.length);

            for (int i = 0; i < options.length; i++) {
                IOption o = options[i];
                // NB:  A choice *may* contain zero options, so long as a decision
                // with zero selections would be valid for that choice.
                if (o == null)
                    throw new EntityCreateException(
                            new StringBuffer("Choice '")
                                .append(h.getValue())
                                .append("': Argument 'options' cannot contain ")
                                .append("null values.")
                                .toString());

                if (this.options.put(o.getHandle(), o) != null) {
                    throw new EntityCreateException(
                            new StringBuffer("Choice '")
                                .append(h.getValue())
                                .append("': Argument 'options' cannot contain ")
                                .append("two options with the same handle.")
                                .append("Duplicate handle: ")
                                .append(o.getHandle().getValue())
                                .toString());
                }
            }
        }

        public Handle getHandle() {
            return handle;
        }

        public Label getLabel() {
            return label;
        }

        public IOption getOption(String h) {
            return getOption(Handle.create(h));
        }

        public IOption getOption(Handle h) {
            // Assertions.
            if (h == null) {
                String msg = "Argument 'h [Handle]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            IOption rslt = (IOption)options.get(h);

            // It's an error if we don't have a match.
            if (rslt == null) {
                String msg = "The choice does not contain the specified "
                                    + "option:  " + h.getValue();
                throw new IllegalArgumentException(msg);
            }

            return rslt;
        }

        public IOption[] getOptions() {
            return (IOption[]) options.values().toArray(new IOption[0]);
        }

        public int getMinSelections() {
            return minSelections;
        }

        public int getMaxSelections() {
            return maxSelections;
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<choice ");

            // Handle.
            rslt.append("handle=\"");
            rslt.append(EntityEncoder.encodeEntities(handle.getValue()));
            rslt.append("\" ");

            // MinSelections.
            rslt.append("min-selections=\"");
            rslt.append(minSelections);
            rslt.append("\" ");

            // MaxSelections.
            rslt.append("max-selections=\"");
            rslt.append(maxSelections);
            rslt.append("\">");

            // Label.
            if (label != null) {
                rslt.append("<label>");
                rslt.append(EntityEncoder.encodeEntities(label.getValue()));
                rslt.append("</label>");
            }

            // Options.
            Iterator it = options.values().iterator();
            while (it.hasNext()) {
                IOption o = (IOption) it.next();
                rslt.append(o.toXml());
            }

            // End.
            rslt.append("</choice>");
            return rslt.toString();

        }
        
        public void sendXmlEvents(ContentHandler ch) throws SAXException {

            final String handleText = "handle";
            final String choiceText = "choice";
            final String minSelectionsText = "min-selections";
            final String maxSelectionsText = "max-selections";
            final String labelText = "label";
            final String cdataText = "CDATA";
            final String emptyText = "";
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(emptyText, handleText, handleText, cdataText,
                EntityEncoder.encodeEntities(handle.getValue()));
            attributes.addAttribute(emptyText, minSelectionsText, minSelectionsText,
                cdataText, Integer.toString(minSelections));
            attributes.addAttribute(emptyText, maxSelectionsText, maxSelectionsText,
                cdataText, Integer.toString(maxSelections));
            ch.startElement(emptyText, choiceText, choiceText, attributes);
            
            // label
            if (label != null) {
                attributes.clear();
                ch.startElement(emptyText, labelText, labelText, attributes);
                String labelString = EntityEncoder.encodeEntities(label.getValue());
                ch.characters(labelString.toCharArray(), 0,
                    labelString.length());
                ch.endElement(emptyText, labelText, labelText);
            }
            
            // Options.
            Iterator it = options.values().iterator();
            while (it.hasNext()) {
                IOption o = (IOption) it.next();
                o.sendXmlEvents(ch);
            }

            // End.
            ch.endElement(emptyText, choiceText, choiceText);
        }
        
        /*
         * Protected API.
         */

        protected final IPenelopeEntity doSelect(String[] tokens, int nextIndex)
                                                throws PenelopeException {

            // Assertions.
            if (tokens == null) {
                String msg = "Argument 'tokens' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (nextIndex == tokens.length || tokens[nextIndex].equals(".")) {
                return this;
            } else if (tokens[nextIndex].startsWith("@")) {
                String h = tokens[nextIndex].substring(1);
                return ((AbstractEntity) this.getOption(Handle.create(h)))
                                        .doSelect(tokens, ++nextIndex);
            } else {
                throw new PenelopeException("BAD EXPRESSION");
            }

        }

    }

    protected static class ChoiceCollectionImpl extends AbstractEntity
                                    implements IChoiceCollection {

        // Instance Members.
        private final Handle handle;
        private final Label label;
        private final Map choices;

        /*
         * Public API.
         */

        public static IChoiceCollection parse(Element e,
                            AbstractEntityStore owner)
                            throws PenelopeException {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("choice-collection")) {
                String msg = "Argument 'e' must be an <choice-collection> "
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
                String msg = "Element <choice> is missing required attribute "
                                                            + "'handle'.";
                throw new XmlFormatException(msg);
            }
            Handle handle = Handle.create(h.getValue());

            // Label.
            Element l = e.element("label");
            Label label = null; // NB:  This is permissible.
            if (l != null) {
                label = Label.create(l.getText());
            }

            // Choices.
            List list = e.selectNodes("descendant::choice");
            IChoice[] choices = new IChoice[list.size()];
            for (int i=0; i < list.size(); i++) {
                Element o = (Element) list.get(i);
                choices[i] = ChoiceImpl.parse(o, owner);
            }

            return new ChoiceCollectionImpl(owner.sequencer().next(), owner,
                                                handle, label, choices);

        }

        public ChoiceCollectionImpl(long id, IEntityStore owner, Handle h,
                                        Label l, IChoice[] choices)
                                        throws EntityCreateException {

            super(id, owner);

            // Assertions.
            if (h == null) {
                String msg = "Argument 'h [Handle]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            // NB:  label may be null.
            if (choices == null) {
                String msg = "Argument 'choices' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (choices.length < 1) {
                String msg = "Argument 'choices' must contain at least one "
                                                            + "element.";
                throw new EntityCreateException(msg);
            }

            // Instance Members.
            this.handle = h;
            this.label = l;
            this.choices = new HashMap(choices.length);
            for (int i = 0; i < choices.length; i++) {
                IChoice c = choices[i];

                if (c == null)
                    throw new EntityCreateException(
                            new StringBuffer("Choice Collection '")
                                .append(h.getValue())
                                .append("': Argument 'choices' may not contain")
                                .append("null values.")
                                .toString());

                if (this.choices.put(c.getHandle(), c) != null) {
                    throw new EntityCreateException(
                            new StringBuffer("Choice Collection '")
                                .append(h.getValue())
                                .append("': Argument 'choices' may not contain")
                                .append("two choices with the same handle. ")
                                .append("Duplicate handle: ")
                                .append(c.getHandle().getValue())
                                .toString());
                }
            }
        }

        public Handle getHandle() {
            return handle;
        }

        public Label getLabel() {
            return label;
        }

        public IChoice getChoice(String h) {
            return getChoice(Handle.create(h));
        }

        public IChoice getChoice(Handle h) {
            // Assertions.
            if (h == null) {
                String msg = "Argument 'h [Handle]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            IChoice rslt = (IChoice)choices.get(h);

            // It's an error if we don't have a match.
            if (rslt == null) {
                String msg = "The choice collection does not contain the "
                            + "specified choice:  " + h.getValue();
                throw new IllegalArgumentException(msg);
            }

            return rslt;

        }

        public IChoice[] getChoices() {
            return (IChoice[]) choices.values().toArray(new IChoice[0]);
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<choice-collection ");

            // Handle.
            rslt.append("handle=\"");
            rslt.append(EntityEncoder.encodeEntities(handle.getValue()));
            rslt.append("\">");

            // Label.
            if (label != null) {
                rslt.append("<label>");
                rslt.append(EntityEncoder.encodeEntities(label.getValue()));
                rslt.append("</label>");
            }

            // Choices.
            Iterator it = choices.values().iterator();
            while (it.hasNext()) {
                IChoice c = (IChoice) it.next();
                rslt.append(c.toXml());
            }

            // End.
            rslt.append("</choice-collection>");
            return rslt.toString();

        }
        
        public void sendXmlEvents(ContentHandler ch) throws SAXException {

            final String handleText = "handle";
            final String choiceCollectionText = "choice-collection";
            final String labelText = "label";
            final String cdataText = "CDATA";
            final String emptyText = "";
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(emptyText, handleText, handleText, cdataText,
                EntityEncoder.encodeEntities(handle.getValue()));
            ch.startElement(emptyText, choiceCollectionText, choiceCollectionText,
                attributes);
            
            // label
            if (label != null) {
                attributes.clear();
                ch.startElement(emptyText, labelText, labelText, attributes);
                String labelString = EntityEncoder.encodeEntities(label.getValue());
                ch.characters(labelString.toCharArray(), 0,
                    labelString.length());
                ch.endElement(emptyText, labelText, labelText);
            }
            
            // Choices.
            Iterator it = choices.values().iterator();
            while (it.hasNext()) {
                IChoice c = (IChoice) it.next();
                c.sendXmlEvents(ch);
            }

            // End.
            ch.endElement(emptyText, choiceCollectionText, choiceCollectionText);
        }

        /*
         * Protected API.
         */

        protected final IPenelopeEntity doSelect(String[] tokens, int nextIndex)
                                                throws PenelopeException {

            // Assertions.
            if (tokens == null) {
                String msg = "Argument 'tokens' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (nextIndex == tokens.length || tokens[nextIndex].equals(".")) {
                return this;
            } else if (tokens[nextIndex].startsWith("@")) {
                String h = tokens[nextIndex].substring(1);
                return ((AbstractEntity) this.getChoice(Handle.create(h)))
                                        .doSelect(tokens, ++nextIndex);
            } else {
                throw new PenelopeException("BAD EXPRESSION");
            }

        }

    }

    protected static class SelectionImpl extends AbstractEntity
                                    implements ISelection {

        // Instance Members.
        private final IOption option;
        private final IComplement complement;

        /*
         * Public API.
         */

        public SelectionImpl(long id, IEntityStore owner, IOption option,
                                            IComplement complement)
                                            throws EntityCreateException {

            super(id, owner);

            // Assertions.
            if (option == null) {
                String msg = "Argument 'option' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (complement == null) {
                String msg = "Argument 'complement' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!complement.getType().equals(option.getComplementType())) {
                String msg = "The complement must match the complement type "
                                            + "specified by the option.";
                throw new EntityCreateException(msg);
            }

            // Instance Members.
            this.option = option;
            this.complement = complement;

        }

        public IOption getOption() {
            return option;
        }

        public IComplement getComplement() {
            return complement;
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<selection ");

            // Option.
            rslt.append("option=\"");
            rslt.append(EntityEncoder.encodeEntities(option.getHandle().getValue()));
            rslt.append("\">");

            // Complement.
            Object obj = complement.getValue();
            if (obj != null) {
                rslt.append(EntityEncoder.encodeEntities(obj.toString()));
            }

            // End.
            rslt.append("</selection>");
            return rslt.toString();

        }
        
        public void sendXmlEvents(ContentHandler ch) throws SAXException {

            final String selectionText = "selection";
            final String optionText = "option";
            final String cdataText = "CDATA";
            final String emptyText = "";
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(emptyText, optionText, optionText, cdataText,
                EntityEncoder.encodeEntities(option.getHandle().getValue()));
            ch.startElement(emptyText, selectionText, selectionText,
                attributes);
            
            if (complement.getValue() != null) {
	            /*String complementString = EntityEncoder.encodeEntities(
                    complement.getValue().toString());*/
            	// Removed the EntityEncoder for seletions in sendXmlEvents. 
            	// Still encoded for toXml method.
            	String complementString = complement.getValue().toString();
                ch.characters(complementString.toCharArray(), 0,
                    complementString.length());
            }
            
            // End.
            ch.endElement(emptyText, selectionText, selectionText);
        }


        /*
         * Protected API.
         */

        protected final IPenelopeEntity doSelect(String[] tokens, int nextIndex)
                                                throws PenelopeException {

            // Assertions.
            if (tokens == null) {
                String msg = "Argument 'tokens' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (nextIndex == tokens.length || tokens[nextIndex].equals(".")) {
                return this;
            } else {
                throw new PenelopeException("BAD EXPRESSION");
            }

        }

    }

    protected static class DecisionImpl extends AbstractEntity
                                    implements IDecision {

        // Instance Members.
        private final Label label;
        private final IChoice choice;
        private final Map selections;
        private Map selectionsHandles;

        /*
         * Public API.
         */

        public DecisionImpl(long id, IEntityStore owner, Label l,
                            IChoice choice, ISelection[] selections)
                            throws EntityCreateException {

            super(id, owner);

            // Assertions.
            // NB:  label may be null.
            assert choice != null : "Argument 'choice' cannot be null.";
            assert selections != null : "Argument 'selections' cannot be null.";

            int min = choice.getMinSelections();
            int max = choice.getMaxSelections();
            if (min != 0 && selections.length < min) {
                String msg = "Argument 'selections' cannot contain fewer than "
                                    + "the minimum number of seelctions.";
                throw new EntityCreateException(msg);
            }
            if (max != 0 && selections.length > max) {
                String msg = "Argument 'selections' cannot contain more than "
                                    + "the maximum number of seelctions.";
                throw new EntityCreateException(msg);
            }

            // Instance Members.
            this.label = l;
            this.choice = choice;
            this.selections = new HashMap(selections.length);
            this.selectionsHandles = null; // Lazy initialization

            List options = Arrays.asList(choice.getOptions());
            for (int i = 0; i < selections.length; i++) {
                ISelection s = selections[i];

                if (s == null) {
                    throw new EntityCreateException(
                        "Argument 'selections' may not contain null values.");
                }

                if (!options.contains(s.getOption())) {
                    String msg = "Argument 'selections' must reference only "
                                            + "options defined by choice.";
                    throw new EntityCreateException(msg);
                }

                this.selections.put(s.getOption(), s);
            }

        }

        public IChoice getChoice() {
            return choice;
        }

        public ISelection getSelection(IOption o) {
            assert o != null : "Argument 'o [IOption]' cannot be null.";

            // Check that the option exists in the associated choice.
            choice.getOption(o.getHandle());

            return (ISelection) selections.get(o);
        }

        private void initSelectionHandles() {
            Collection c = selections.values();
            selectionsHandles = new HashMap(c.size());
            Iterator it = c.iterator();
            while (it.hasNext()) {
                ISelection s = (ISelection)it.next();
                this.selectionsHandles.put(s.getOption().getHandle().getValue(), s);
            }
        }

        public ISelection getSelection(String h) {
            assert h != null : "Argument 'h [String]' cannot be null.";
            if (h.trim().length() == 0)
                throw new IllegalArgumentException(
                            "Argument 'h [String]' cannot be empty.");

            if (selectionsHandles == null)
                initSelectionHandles();

            return (ISelection)selectionsHandles.get(h);
        }

        public String getFirstSelectionHandle() {
            String obj = null;

            if (!selections.isEmpty())
                obj = ((ISelection)
                          selections.values()
                                    .iterator()
                                    .next())
                                    .getOption()
                                    .getHandle()
                                    .getValue();

            return obj;
        }

        public Object getFirstSelectionValue() {
            Object obj = null;

            if (!selections.isEmpty())
                obj = ((ISelection)
                          selections.values()
                                    .iterator()
                                    .next())
                                    .getComplement()
                                    .getValue();

            return obj;
        }

        public ISelection[] getSelections() {
            return (ISelection[]) selections.values().toArray(
                                        new ISelection[0]);
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<decision ");

            // Choice.
            rslt.append("choice=\"");
            rslt.append(EntityEncoder.encodeEntities(choice.getHandle().getValue()));
            rslt.append("\">");

            // Label.
            if (label != null) {
                rslt.append("<label>");
                rslt.append(EntityEncoder.encodeEntities(label.getValue()));
                rslt.append("</label>");
            }

            // Selections.
            Iterator it = selections.values().iterator();
            while (it.hasNext()) {
                ISelection s = (ISelection) it.next();
                rslt.append(s.toXml());
            }

            // End.
            rslt.append("</decision>");
            return rslt.toString();

        }
        
        public void sendXmlEvents(ContentHandler ch) throws SAXException {

            final String decisionText = "decision";
            final String choiceText = "choice";
            final String labelText = "label";
            final String cdataText = "CDATA";
            final String emptyText = "";
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(emptyText, choiceText, choiceText, cdataText,
                EntityEncoder.encodeEntities(choice.getHandle().getValue()));
            ch.startElement(emptyText, decisionText, decisionText,
                attributes);
            
            // Label.
            if (label != null) {
                attributes.clear();
                ch.startElement(emptyText, labelText, labelText, attributes);
                String labelString = EntityEncoder.encodeEntities(label.getValue());
                ch.characters(labelString.toCharArray(), 0,
                    labelString.length());
                ch.endElement(emptyText, labelText, labelText);
            }
            
            // Selections.
            Iterator it = selections.values().iterator();
            while (it.hasNext()) {
                ISelection s = (ISelection) it.next();
                s.sendXmlEvents(ch);
            }

            // End.
            ch.endElement(emptyText, decisionText, decisionText);
        }

        /*
         * Protected API.
         */

        protected final IPenelopeEntity doSelect(String[] tokens, int nextIndex)
                                                throws PenelopeException {

            // Assertions.
            if (tokens == null) {
                String msg = "Argument 'tokens' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (nextIndex == tokens.length || tokens[nextIndex].equals(".")) {
                return this;
            } else if (tokens[nextIndex].startsWith("@")) {
                String h = tokens[nextIndex].substring(1);
                Handle handle = Handle.create(h);
                return ((AbstractEntity) this.getSelection(getChoice().getOption(handle)))
                                                        .doSelect(tokens, ++nextIndex);
            } else {
                throw new PenelopeException("BAD EXPRESSION");
            }

        }

    }

    protected static class DecisionCollectionImpl extends AbstractEntity
                                    implements IDecisionCollection {

        // Instance Members.
        private final IChoiceCollection choiceCollection;
        private final Map decisions;
        private Map decisionsHandles;

        /*
         * Public API.
         */

        public DecisionCollectionImpl(long id, IEntityStore owner,
                                    IChoiceCollection choiceCollection,
                                    IDecision[] decisions)
                                    throws EntityCreateException {

            super(id, owner);

            // Assertions.
            assert choiceCollection != null : "Argument 'choiceCollection' cannot be null.";
            assert decisions != null : "Argument 'decisions' cannot be null.";
            if (decisions.length < 1) {
                String msg = "Argument 'decisions' must contain at least one "
                                                            + "element.";
                throw new EntityCreateException(msg);
            }

            // Instance Members.
            this.choiceCollection = choiceCollection;
            this.decisions = new HashMap(decisions.length);
            this.decisionsHandles = null; // Lazy initialization

            List choices = Arrays.asList(choiceCollection.getChoices());
            for (int i = 0; i < decisions.length; i++) {
                IDecision d = decisions[i];

                if (d == null) {
                    throw new EntityCreateException(
                        "Argument 'decisions' may not contain null values.");
                }

                if (!choices.contains(d.getChoice())) {
                    throw new EntityCreateException(
                                "Argument 'decisions' must reference only "
                              + "choices defined by choiceCollection.");
                }

                this.decisions.put(d.getChoice(), d);
            }

        }

        public IChoiceCollection getChoiceCollection() {
            return choiceCollection;
        }

        public IDecision getDecision(IChoice c) {
            assert c != null : "Argument 'c [IChoice]' cannot be null.";

            // Check that the choice exists in the associated ChoiceCollection
            choiceCollection.getChoice(c.getHandle());

            return (IDecision)decisions.get(c);
        }

        private void initDecisionHandles() {
            decisionsHandles = new HashMap(decisions.values().size());
            Iterator it = decisions.values().iterator();
            while (it.hasNext()) {
                IDecision d = (IDecision)it.next();
                this.decisionsHandles.put(d.getChoice().getHandle().getValue(), d);
            }
        }

        public IDecision getDecision(String h) {
            assert h != null : "Argument 'h [String]' cannot be null.";
            if (h.trim().length() == 0)
                throw new IllegalArgumentException(
                            "Argument 'h [String]' cannot be empty.");

            if (decisionsHandles == null)
                initDecisionHandles();

            return (IDecision) decisionsHandles.get(h);
        }

        public IDecision[] getDecisions() {
            return (IDecision[]) decisions.values().toArray(new IDecision[0]);
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<decision-collection ");

            // ChoiceCollection.
            rslt.append("choice-collection=\"");
            rslt.append(EntityEncoder.encodeEntities(choiceCollection.getHandle().getValue()));
            rslt.append("\">");

            // Decisions.
            Iterator it = decisions.values().iterator();
            while (it.hasNext()) {
                IDecision d = (IDecision) it.next();
                rslt.append(d.toXml());
            }

            // End.
            rslt.append("</decision-collection>");
            return rslt.toString();

        }
        
        public void sendXmlEvents(ContentHandler ch) throws SAXException {

            final String decisionCollectionText = "decision-collection";
            final String choiceCollectionText = "choice-collection";
            final String cdataText = "CDATA";
            final String emptyText = "";
            
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(emptyText, choiceCollectionText, choiceCollectionText,
                cdataText, EntityEncoder.encodeEntities(
                    choiceCollection.getHandle().getValue()));
            ch.startElement(emptyText, decisionCollectionText, decisionCollectionText,
                attributes);
            
            // Decisions.
            Iterator it = decisions.values().iterator();
            while (it.hasNext()) {
                IDecision d = (IDecision) it.next();
                d.sendXmlEvents(ch);
            }

            // End.
            ch.endElement(emptyText, decisionCollectionText, decisionCollectionText);
        }

        /*
         * Protected API.
         */

        protected final IPenelopeEntity doSelect(String[] tokens, int nextIndex)
                                                throws PenelopeException {

            // Assertions.
            if (tokens == null) {
                String msg = "Argument 'tokens' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            if (nextIndex == tokens.length || tokens[nextIndex].equals(".")) {
                return this;
            } else if (tokens[nextIndex].startsWith("@")) {
                String h = tokens[nextIndex].substring(1);
                Handle handle = Handle.create(h);
                return ((AbstractEntity) this.getDecision(getChoiceCollection().getChoice(handle)))
                                                                .doSelect(tokens, ++nextIndex);
            } else {
                throw new PenelopeException("BAD EXPRESSION");
            }

        }

    }
    
    public static class ChoiceCollectionSaxParser
    implements IChoiceCollectionParser {
        
        public static final int NO_STATE_VALUE = 0;
        public static final int OTHER_STATE_VALUE = 1;
        public static final int CHOICE_STATE_VALUE = 2;
        public static final int CHOICE_COLLECTION_STATE_VALUE = 3;
        public static final int LABEL_STATE_VALUE = 4;
        public static final int OPTION_STATE_VALUE = 5;
        public static final int BEAN_STATE_VALUE = 6;
        
        public static final Integer NO_STATE = new Integer(NO_STATE_VALUE);
        public static final Integer OTHER_STATE = new Integer(OTHER_STATE_VALUE);
        public static final Integer CHOICE_COLLECTION_STATE = new Integer(CHOICE_COLLECTION_STATE_VALUE);
        public static final Integer CHOICE_STATE = new Integer(CHOICE_STATE_VALUE);
        public static final Integer LABEL_STATE = new Integer(LABEL_STATE_VALUE);
        public static final Integer OPTION_STATE = new Integer(OPTION_STATE_VALUE);
        public static final Integer BEAN_STATE = new Integer(BEAN_STATE_VALUE);
        
        public static final String CHOICE_COLLECTION_TEXT = "choice-collection";
        public static final String CHOICE_TEXT = "choice";
        public static final String HANDLE_TEXT = "handle";
        public static final String LABEL_TEXT = "label";
        public static final String OPTION_TEXT = "option";
        public static final String BEAN_TEXT = "bean";
        public static final String BEANS_QNAME = "beans";
        public static final String MIN_SELECTIONS_TEXT = "min-selections";
        public static final String MAX_SELECTIONS_TEXT = "max-selections";
        public static final String COMPLEMENT_TYPE_TEXT = "complement-type";
        
        private AbstractEntityStore owner = null;
        private ContentHandler handler = null;
        
        private Handle ccHandle = null;
        private Label ccLabel = null;
        
        private Handle cHandle = null;
        private Label cLabel = null;

        private Handle oHandle = null;
        private Label oLabel = null;
        private IComplementType oComplementType = null;

        private int cMinSelections = 0;
        private int cMaxSelections = 0;
        
        private Stack stateStack = new Stack();
        
        private Stack nestedBeanStack = null;
        private SAXContentHandler saxContentHandler;
        
        private StringBuffer characters = new StringBuffer();

        private List choiceCollections = new ArrayList();
        private List options = new ArrayList();
        private List choices = new ArrayList();
        
        private String getState(int s) {
            switch (s) {
            case NO_STATE_VALUE: return "NONE";
            case OTHER_STATE_VALUE: return "OTHER";
            case CHOICE_STATE_VALUE: return "CHOICE";
            case CHOICE_COLLECTION_STATE_VALUE: return "CHOICE_COLLECTION";
            case LABEL_STATE_VALUE: return "LABEL";
            case OPTION_STATE_VALUE: return "OPTION";
            case BEAN_STATE_VALUE: return "BEAN";
            }
            return "UNDEFINED";
        }
        
        public ChoiceCollectionSaxParser(AbstractEntityStore owner,
            ContentHandler ch) {
            
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (ch == null) {
                String msg = "Argument 'ch' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            this.owner = owner;
            this.handler = ch;
        }

        public IChoiceCollection[] getChoiceCollections() {
            return (IChoiceCollection[])choiceCollections.toArray(
                new IChoiceCollection[choiceCollections.size()]);
        }

        protected int getCurrentState() {
            Integer currentState = null;
            if (stateStack.isEmpty()) {
                currentState = NO_STATE;
            } else {
                currentState = (Integer)stateStack.peek();
            }
            return currentState.intValue();
        }
        
        public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
            
            handler.startElement(namespaceURI, localName, qName, atts);

            int currentState = getCurrentState();
            //System.out.println("startElement: " + qName + ", state: " + getState(currentState));

            boolean changedState = false;

            if (currentState != OTHER_STATE_VALUE && LABEL_TEXT.equals(qName)) {
                stateStack.push(LABEL_STATE);
                characters.setLength(0);
            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
                return;
            }
            
            if (currentState == BEAN_STATE_VALUE) {

                if (BEAN_TEXT.equals(qName)) { // Nested bean element
                    nestedBeanStack.push(qName);
                }
                
                saxContentHandler.startElement(namespaceURI, localName, qName, 
                        atts);
            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
                return;
            }
            
            if (currentState != BEAN_STATE_VALUE && BEAN_TEXT.equals(qName)) {
                stateStack.push(BEAN_STATE);

                nestedBeanStack = new Stack();
                saxContentHandler = new SAXContentHandler();
                saxContentHandler.startDocument();
                saxContentHandler.startElement(namespaceURI, BEANS_QNAME, 
                        BEANS_QNAME, new AttributesImpl());
                saxContentHandler.startElement(namespaceURI, localName, qName, 
                        atts);
                
            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
                return;
            }

            if ((currentState == OTHER_STATE_VALUE || currentState == CHOICE_STATE_VALUE) && OPTION_TEXT.equals(qName)) {
                stateStack.push(OPTION_STATE);

                String attrValue = atts.getValue(HANDLE_TEXT);
                if (attrValue == null) {
                    throw new SAXException("Element <option> " +
                        "is missing required attribute 'handle'.");
                }
                oHandle = Handle.create(attrValue);

                attrValue = atts.getValue(COMPLEMENT_TYPE_TEXT);
                if (attrValue == null) {
                    throw new SAXException("Element <option> " +
                        "is missing required attribute 'complement-type'.");
                }

                try {
                    
                    if (!XML_BEAN_FACTORY.equals(attrValue)) {
                        Class c = Class.forName(attrValue);
                        Field f = c.getDeclaredField("INSTANCE");
                        oComplementType = (IComplementType) f.get(null);
                    }
                } catch (Throwable t) {
                    String msg = "Unable to obtain the specified complement type:  "
                        + attrValue;
                    throw new SAXException(msg);
                }

            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
                return;
            }

            if ((currentState == CHOICE_COLLECTION_STATE_VALUE || currentState == OTHER_STATE_VALUE) && CHOICE_TEXT.equals(qName)) {
                stateStack.push(CHOICE_STATE);
                options.clear();

                String handleAttr = atts.getValue(HANDLE_TEXT);
                if (handleAttr == null) {
                    throw new SAXException("Element <choice> " +
                        "is missing required attribute 'handle'.");
                }
                cHandle = Handle.create(handleAttr);

                try {
                    cMinSelections = Integer.parseInt(
                        atts.getValue(MIN_SELECTIONS_TEXT));
                    cMaxSelections = Integer.parseInt(
                        atts.getValue(MAX_SELECTIONS_TEXT));
                } catch (NumberFormatException nfe) {
                    throw new SAXException("Invalid <choice> min/max attribute.", nfe);
                }

            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
                return;
            }

            if ((currentState == OTHER_STATE_VALUE || currentState == NO_STATE_VALUE) && CHOICE_COLLECTION_TEXT.equals(qName)) {
                stateStack.push(CHOICE_COLLECTION_STATE);
                changedState = true;
                choices.clear();

                String handleAttr = atts.getValue(HANDLE_TEXT);
                if (handleAttr == null) {
                    throw new SAXException("Element <choice-collection> " +
                        "is missing required attribute 'handle'.");
                }
                ccHandle = Handle.create(handleAttr);
            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
                return;
            }

            stateStack.push(OTHER_STATE);
            //System.out.println("startElement: " + qName + ", changed state to: " + getState(getCurrentState()));
        }

        public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
            
            handler.endElement(namespaceURI, localName, qName);
            //System.out.println("endElement: " + qName + ", state: " + getState(getCurrentState()));
            
            switch (getCurrentState()) {
                case OTHER_STATE_VALUE:
                    stateStack.pop();
                break;

                case CHOICE_COLLECTION_STATE_VALUE:
                    if (CHOICE_COLLECTION_TEXT.equals(qName)) {
                        stateStack.pop();
                        try {
                            choiceCollections.add(new ChoiceCollectionImpl(
                                owner.sequencer().next(), owner,
                                ccHandle, ccLabel,
                                (IChoice[])choices.toArray(new IChoice[choices.size()])));
                        } catch (EntityCreateException e) {
                            throw new SAXException(
                                "Failed creating ChoiceCollectionImpl.", e);
                        }
                    }
                break;

                case CHOICE_STATE_VALUE:
                    if (CHOICE_TEXT.equals(qName)) {
                        stateStack.pop();
                        try {
                            choices.add(new ChoiceImpl(
                                owner.sequencer().next(), owner,
                                cHandle, cLabel,
                                (IOption[])options.toArray(new IOption[options.size()]),
                                cMinSelections, cMaxSelections));
                        } catch (EntityCreateException e) {
                            throw new SAXException(
                                "Failed creating ChoiceCollectionImpl.", e);
                        }
                    }
                break;

                case OPTION_STATE_VALUE:
                    if (OPTION_TEXT.equals(qName)) {
                        stateStack.pop();
                        options.add(new OptionImpl(owner.sequencer().next(),
                            owner, oHandle, oLabel, oComplementType));
                    }
                break;

                case LABEL_STATE_VALUE:
                    if (LABEL_TEXT.equals(qName)) {
                        stateStack.pop();

                        switch (getCurrentState()) {
                            case CHOICE_COLLECTION_STATE_VALUE:
                                ccLabel = Label.create(characters.toString());
                            break;

                            case CHOICE_STATE_VALUE:
                                cLabel = Label.create(characters.toString());
                            break;

                            case OPTION_STATE_VALUE:
                                oLabel = Label.create(characters.toString());
                            break;

                        }
                    }
                break;
                
                case BEAN_STATE_VALUE:
                    
                    saxContentHandler.endElement(namespaceURI, localName, 
                            qName);
                    
                    if (BEAN_TEXT.equals(qName) && nestedBeanStack.isEmpty()) {
                        stateStack.pop();
                        saxContentHandler.endElement(namespaceURI, BEANS_QNAME, 
                                BEANS_QNAME);
                        saxContentHandler.endDocument();
                        Document document = saxContentHandler.getDocument();
                        Element beanElement = document.getRootElement();
                        byte[] beanDef = beanElement.asXML().getBytes();
                        DefaultListableBeanFactory factory = 
                            new DefaultListableBeanFactory();
                        XmlBeanDefinitionReader reader = 
                            new XmlBeanDefinitionReader(factory);
                        reader.setValidationMode(
                                XmlBeanDefinitionReader.VALIDATION_NONE);  
                        reader.loadBeanDefinitions(
                                new ByteArrayResource(beanDef));
                        oComplementType = (IComplementType)factory.getBean(
                                "complementType");                        
               
                    }
                    else if (BEAN_TEXT.equals(qName)) {
                        nestedBeanStack.pop();
                    }
                    
                break;
            }
            //System.out.println("endElement: " + qName + ", changed state to: " + getState(getCurrentState()));

        }

        public void endDocument() throws SAXException {
            handler.endDocument();
        }

        public void startDocument() throws SAXException {
            handler.startDocument();
        }

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            handler.characters(ch, start, length);
            characters.append(ch, start, length);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.characters(ch, start, length);
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
            handler.ignorableWhitespace(ch, start, length);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.ignorableWhitespace(ch, start, length);
            }
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            handler.endPrefixMapping(prefix);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.endPrefixMapping(prefix);
            }
        }

        public void skippedEntity(String name) throws SAXException {
            handler.skippedEntity(name);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.skippedEntity(name);  
            }
        }

        public void setDocumentLocator(Locator locator) {
            handler.setDocumentLocator(locator);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.setDocumentLocator(locator);
            }
        }

        public void processingInstruction(String target, String data)
            throws SAXException {
            handler.processingInstruction(target, data);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.processingInstruction(target, data);
            }
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
            handler.startPrefixMapping(prefix, uri);
            if (getCurrentState() == BEAN_STATE_VALUE) {
                saxContentHandler.startPrefixMapping(prefix, uri);
            }
        }
    }

}
