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

package net.unicon.alchemist.access;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Controls access over a collection of arbitrary entiries.  Implementations of
 * <code>AccessBroker</code> must provide a static <code>parse</code> method
 * that returns an <code>AccessBroker</code> instance based on XML configuration
 * data contained in a dom4j <code>Element</code>.
 */
public abstract class AccessBroker {

    // Static Members.
    private static Map instances = Collections.synchronizedMap(new HashMap());

    // Instance Members.
    private String handle;

    /*
     * Public API.
     */

    /**
     * Provides the broker defined by the specified <code>Element</code>.
     * First, this method checks the in-memory cache of brokers for one with the
     * handle indicated within the XML.  If an instance is not found, this
     * method attempts to create a new broker instance based on the
     * specifications within the XML.  If successful, this method stores the new
     * instance within the in-memory cache.
     *
     * @param e XML that defines the desired broker.
     * @return A broker with the characteristics specified in the XML.
     */
    public static AccessBroker parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("access-broker")) {
            String msg = "Argument 'e [Element]' must be an <access-broker> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        AccessBroker rslt = null;

        // Call parse() in the specified implementation.
        Attribute impl = e.attribute("impl");
        if (impl == null) {
            String msg = "Element <access-broker> is missing required "
                                            + "attribute 'impl'.";
            throw new IllegalArgumentException(msg);
        }
        try {
            Class c = Class.forName(impl.getValue());
            Method m = c.getDeclaredMethod("parse", new Class[] {
                                            Element.class });
            rslt = (AccessBroker) m.invoke(null, new Object[] { e });
        } catch (Throwable t) {
            String msg = "Unable to create the specified broker:  "
                                            + impl.getValue();
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

    /**
     * Provides the broker instance with the specified handle.  Throws
     * <code>IllegalArgumentException</code> if there is no broker with the
     * specified handle.
     *
     * @param handle The unique handle of the desired broker.
     * @return The broker with the specified handle.
     */
    public static AccessBroker getInstance(String handle) {

        // Assertions.
        if (handle == null) {
            String msg = "Argument 'handle' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!instances.containsKey(handle)) {
            String msg = "Broker instance not found:  " + handle;
            throw new IllegalArgumentException(msg);
        }

        return (AccessBroker) instances.get(handle);

    }

    /**
     * Provides the handle for this broker instance.  Broker handles are unique
     * within the JVM.
     *
     * @return A handle uniquely identifying this broker instance.
     */
    public final String getHandle() {
        return handle;
    }

    /**
     * Sets the specified identity's access to the specified access rules for the
     * specified target.  This method will update an existing entry if possible
     * or create a new one if necessary.
     *
     * @param y The identity receiving access.
     * @param rules The level(s) of access being granted/denied.
     * @param r The object over which access is being granted/denied.
     * @return The new or updated access entry.
     */
    public abstract IAccessEntry setAccess(Identity y, AccessRule[] rules,
                                                        Object target);

    /**
     * Deletes the specified entry from the broker.
     *
     * @param e The entry to remove.
     */
    public final void removeAccess(IAccessEntry e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [IAccessEntry]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        removeAccess(new IAccessEntry[] { e });

    }

    /**
     * Deletes the specified entries from the broker.
     *
     * @param entries The entries to remove.
     */
    public abstract void removeAccess(IAccessEntry[] entries);

    /**
     * Provides the entry governing the specified identity's rights over the
     * specified target, or <code>null</code> if no such entry exists.
     *
     * @param y An access principal.
     * @param target An object that is governed by access rights.
     * @return An entry defining the identity's access rights, or null if there
     * is no entry.
     */
    public abstract IAccessEntry getEntry(Identity y, Object target);

    /**
     * Provides the entries governing the specified identity's rights as
     * defined by this broker instance.
     *
     * @param y An access identity.
     * @return The entries defining the principal's access rights.
     */
    public final IAccessEntry[] getEntries(Identity y) {

        // Assertions.
        if (y == null) {
            String msg = "Argument 'y [Identity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Principal p = new Principal(new Identity[] { y });
        return getEntries(p, new AccessRule[0]);

    }


    /**
     * Provides the entries governing the specified identity's rights as
     * defined by this broker instance.
     *
     * @param y An access identity.
     * @return The entries defining the principal's access rights.
     */
    public final IAccessEntry[] getEntries(Principal p) {

        // Assertions.
        if (p == null) {
            String msg = "Argument 'y [Identity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

       return getEntries(p, new AccessRule[0]);

    }

    /**
     * Provides the entries governing the specified identity's rights as
     * defined by this broker instance where those rights include the specified
     * access rule(s).  In other words, this method will return the complete
     * entry if it includes one or more of the specified rule(s).  If a
     * zero-length rules array is specified, this method will return all entries
     * (i.e. any rule).
     *
     * @param y An access identity.
     * @param rules The rule(s) of access to include, or include all rules if
     * the array is zero-length.
     * @return The entries defining the principal's access rights.
     */
    public final IAccessEntry[] getEntries(Identity y, AccessRule[] rules) {

        // Assertions.
        if (y == null) {
            String msg = "Argument 'y [Identity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Principal p = new Principal(new Identity[] { y });
        return getEntries(p, rules);

    }

    /**
     * Provides the entries governing the specified principal's rights as
     * defined by this broker instance where those rights include the specified
     * access rule(s).  In other words, this method will return the complete
     * entry if it includes one or more of the specified rule(s).  If a
     * zero-length rules array is specified, this method will return all entries
     * (i.e. any rule).
     *
     * @param p An access principal.
     * @param rules The rule(s) of access to include, or include all rules if
     * the array is zero-length.
     * @return The entries defining the principal's access rights.
     */
    public abstract IAccessEntry[] getEntries(Principal p, AccessRule[] rules);

    /**
     * Provides the entries governing access rights over the specified target.
     *
     * @param target An object over which access is governed by this broker
     * instance.
     * @return The entries defining access rights over the target.
     */
    public final IAccessEntry[] getEntries(Object target) {

        // Assertions.
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        return getEntries(target, new AccessRule[0]);

    }

    /**
     * Provides the entries governing access rights over the specified target
     * where one or more of the specified access rule(s) is included.  In other
     * words, this method will return the complete entry if it includes one or
     * more of the specified rule(s).  If a zero-length rules array is
     * specified, this method will return all entries (i.e. any rule).
     *
     * @param target An object over which access is governed by this broker
     * instance.
     * @param rules The rule(s) of access to include, or include all rules if
     * the array is zero-length.
     * @return The entries defining access rights over the target.
     */
    public abstract IAccessEntry[] getEntries(Object target,
                                    AccessRule[] rules);

    /**
     * Provides all the entries in the broker.
     *
     * @return All the entries defined in the broker.
     */
    public abstract IAccessEntry[] getEntries();

    /*
     * Protected API.
     */

    /**
     * Constructs a new <code>AccessBroker</code> using information from the
     * specified XML.
     *
     * @param e XML that defines the desired broker.
     * @return A broker with the characteristics specified in the XML.
     */
    protected AccessBroker(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("access-broker")) {
            String msg = "Argument 'e [Element]' must be an <access-broker> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
            String msg = "Element <access-broker> is missing required "
                                            + "attribute 'handle'.";
            throw new IllegalArgumentException(msg);
        }
        String val = h.getValue().trim();
        if (val.length() == 0) {
            String msg = "Attribute 'handle' cannot be zero-length or contain "
                                                        + "only whitespace.";
            throw new IllegalArgumentException(msg);
        }
        this.handle = val;

        // Add to collection.
        instances.put(val, this);

    }

    /*
     * Implementation.
     */

    private static Object evaluateUrl(String url) {

        // Assertions.
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Object rslt = null;
        try {

            String[] tokens = url.split("/");
            String className = tokens[2];
            Class c = Class.forName(className);
            Method m = c.getDeclaredMethod("fromUrl", new Class[] {
                                                String.class });
            rslt = m.invoke(null, new Object[] { url });

        } catch (Throwable t) {
            String msg = "Unable to evaluate the specified entity:  " + url;
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

    /*
     * Nested Types.
     */

    protected static final class AccessEntryImpl implements IAccessEntry {

        // Instance Members.
        private final Identity id;
        private final AccessRule[] rules;
        private final Object target;

        /*
         * Public API.
         */

        public static IAccessEntry parse(Element e) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("entry")) {
                String msg = "Argument 'e [Element]' must be an <entry> "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Identity.
            Identity id = null;
            Element y = (Element) e.selectSingleNode("identity");
            if (y != null) {
                IdentityType type = null;
                Attribute p = y.attribute("type");
                if (p == null) {
                    String msg = "Element <identity> is missing required "
                                                + "attribute 'type'.";
                    throw new IllegalArgumentException(msg);
                }
                if (p.getValue().equalsIgnoreCase("GROUP")) {
                    type = IdentityType.GROUP;
                } else {
                    type = IdentityType.USER;
                }
                id = new Identity(y.getText(), type);
            } else {
                String msg = "Element <entry> is missing required child "
                                            + "element <identity>.";
                throw new IllegalArgumentException(msg);
            }

            // AccessRules.
            y = (Element) e.selectSingleNode("access");
            if (y == null) {
                String msg = "Element <access> is missing. ";
                throw new IllegalArgumentException(msg);
            }
            AccessRule[] rules = AccessRule.parse(y);

            // Target.
            Object target = null;
            Attribute r = e.attribute("target");
            if (r != null) {
                target = evaluateUrl(r.getValue());
            } else if (e.selectSingleNode("target/*") != null) {
                Element n =  (Element) e.selectSingleNode("target/*");
                Attribute impl = n.attribute("impl");
                if (impl == null) {
                    String msg = "Missing required attribute 'impl'.";
                    throw new IllegalArgumentException(msg);
                }
                try {
                    Class c = Class.forName(impl.getValue());
                    Method m = c.getDeclaredMethod("parse", new Class[]
                                                { Element.class });
                    target = m.invoke(null, new Object[] { n });
                } catch (Throwable t) {
                    String msg = "Unable to parse the specified target.";
                    throw new RuntimeException(msg, t);
                }

            } else {
                String msg = "Element <entry> must either have a 'target' "
                            + "attribute or contain a <target> element.";
                throw new IllegalArgumentException(msg);
            }

            return new AccessEntryImpl(id, rules, target);

        }

        public AccessEntryImpl(Identity id, AccessRule[] rules, Object target) {

            // Assertions.
            if (id == null) {
                String msg = "Argument 'id' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (rules == null) {
                String msg = "Argument 'rules' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (rules.length == 0) {
                String msg = "Argument 'rules' must contain at least one "
                                                        + "element.";
                throw new IllegalArgumentException(msg);
            }
            if (target == null) {
                String msg = "Argument 'target' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.id = id;
            this.rules = new AccessRule[rules.length];
            System.arraycopy(rules, 0, this.rules, 0, rules.length);
            this.target = target;

        }

        public Identity getIdentity() {
            return id;
        }

        public AccessRule[] getAccessRules() {
            AccessRule[] rslt = new AccessRule[rules.length];
            System.arraycopy(rules, 0, rslt, 0, rules.length);
            return rslt;
        }

        public Object getTarget() {
            return target;
        }

public IAccessElement getAccessId() { throw new UnsupportedOperationException("This method will be removed."); }

public int[] getTypes() { throw new UnsupportedOperationException("This method will be removed."); }

public String getOwner() { throw new UnsupportedOperationException("This method will be removed."); }


    }   

}
