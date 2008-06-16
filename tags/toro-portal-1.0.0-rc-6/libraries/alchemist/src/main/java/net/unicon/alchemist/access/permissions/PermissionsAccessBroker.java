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

package net.unicon.alchemist.access.permissions;

import java.util.ArrayList;
import java.util.List;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.PermissionsUtil;
import net.unicon.alchemist.access.Principal;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * AccessBroker implementation that implements inheriting permissions uncoupled
 * to targets.
 *
 * This implementation uses two AccessBrokers internally to accomplish the
 * disconnect between targets and permissions. By providing this disconnect, it
 * is then possible to provide manageable permissions for user-level target
 * objects with group-level permissions.
 *
 * @author Eric Andresen - eandresen@unicon.net
 */
public class PermissionsAccessBroker extends AccessBroker {
    private final AccessBroker targets;
    private final AccessBroker permissions;
    private final AccessType[] accessTypes;

    private static final AccessRule[] dummyrules =
                    { new AccessRule(DummyAccessType.dummy, true) };

    private PermissionsAccessBroker(Element e,
                                    AccessBroker targets,
                                    AccessBroker permissions,
                                    AccessType[] accessTypes) {
        super(e);

        this.targets = targets;
        this.permissions = permissions;
        this.accessTypes = accessTypes;
    }

    public static AccessBroker parse(Element ce) {
        // Targets Access Broker -- Can be null.
        Element e = (Element)ce.selectSingleNode("targets/access-broker");
        AccessBroker targets = null;
        if (e != null)
            targets = AccessBroker.parse(e);

        // Permissions Access Broker
        e = (Element)ce.selectSingleNode("permissions/access-broker");
        if (e == null)
            throw new IllegalArgumentException(
                    "PermissionsAccessBroker requires an <access-broker> element "
                  + "under element <permissions>.");
        AccessBroker permissions = AccessBroker.parse(e);

        // AccessType[] declaration
        e = (Element)ce.selectSingleNode("access");
        if (e == null)
            throw new IllegalArgumentException(
                    "PermissionsAccessBroker requires an <access> element.");
        Attribute impl = e.attribute("impl");
        if (impl == null)
            throw new IllegalArgumentException(
                    "The element <access> must contain an 'impl' attribute.");

        AccessType[] accessTypes = null;
        try {
            accessTypes = (AccessType[])
                Class.forName(impl.getValue())
                     .getMethod("getInstances", null)
                     .invoke(null, null);
        } catch (ClassNotFoundException ex1) {
            throw new RuntimeException("Could not find the class " + impl.getValue(), ex1);
        } catch (NoSuchMethodException ex2) {
            throw new RuntimeException("Could not find the Method 'getInstances' on class " + impl.getValue(), ex2);
        } catch (Exception ex3) {
            throw new RuntimeException("Unable to execute Method 'getInstances' on class " + impl.getValue(), ex3);
        }

        if (accessTypes == null || accessTypes.length == 0)
            throw new IllegalArgumentException(
                    "No AccessTypes found from <access> element declaration.");

        return new PermissionsAccessBroker(ce, targets, permissions, accessTypes);
    }

    /**
     * Retrieve all permissions entries from the broker.
     *
     * This differs from other implementations in that the internal 'targets'
     * tree is ignored, and only permission entries are returned.
     *
     * @return All permissions access entries defined in the broker.
     */
    public IAccessEntry[] getEntries() {
        // Get all permissions entries (ignores targets)
        IAccessEntry[] rslt = permissions.getEntries();

        /*
        System.out.println("PermissionsAccessBroker::getEntries(): Returning "+rslt.length+" entries.");
        for (int i = 0; i < rslt.length; i++) {
            System.out.println("Identity: "+rslt[i].getIdentity().getId());
        }
        */

        return rslt;
    }

    public IAccessEntry[] getEntries(Principal princ, AccessRule[] rules) {
        // Retrieve the target(s) that the Principal has the specified access to

        IAccessEntry[] p = null;
        IAccessEntry[] t = null;

        // Find ALL targets for the given Principal
        if (targets != null)
            t = targets.getEntries(princ, new AccessRule[0]);

        // Find ALL permissions for the given Principal.
        p = permissions.getEntries(princ, new AccessRule[0]);

        if (p == null || p.length == 0)
            return new IAccessEntry[0];

        // Flatten permissions
        AccessRule[] flat = PermissionsUtil.coalesce(p, accessTypes);

        /*
        for (int i = 0; i < flat.length; i++) {
            System.out.println("Permission: "+flat[i].getAccessType().getName()+" ["+flat[i].getAccessType().toInt()+"] Grant: "+flat[i].getStatus());
        }
        */

        // Filter by AccessRules as provided as 'rules'.
        boolean valid = (flat.length > 0 && rules.length == 0 ? true : false);
        for (int i = 0; !valid && i < rules.length; i++) {
            for (int j = 0; !valid && j < flat.length; j++) {
                if (flat[j].equals(rules[i])) {
                    valid = true;
                }
            }
        }

        IAccessEntry[] rslt = null;
        if (!valid) {
            rslt = new IAccessEntry[0];
        } else {
            // Apply flattened permissions to all target entries
            if (t != null) {
            rslt = new IAccessEntry[t.length];
            for (int i = 0; i < t.length; i++)
                rslt[i] = new AccessBroker.AccessEntryImpl(t[i].getIdentity(), flat, t[i].getTarget());
            } else {
                Identity[] ids = princ.getIdentities();
                Identity ident = ids[0];
                for (int i = 1; !ident.getType().equals(IdentityType.USER) && i < ids.length; i++)
                    if (ids[i].getType().equals(IdentityType.USER))
                        ident = ids[i];

                rslt = new IAccessEntry[] { new AccessBroker.AccessEntryImpl(ident, flat, Dummy.dummy) };
            }
        }

        return rslt;
    }

    public IAccessEntry[] getEntries(Object target, AccessRule[] rules) {
        if (targets == null)
            return new IAccessEntry[0];

        // Retrieve access entries for the given target first.
        IAccessEntry[] t = targets.getEntries(target, dummyrules);
        List rslt = new ArrayList();

        // For each of the returned access entries, check the identity's
        // permissions. XXX: This does NOT take into account inheritance. There
        // is currently no way to extrapolate an Identity into the full
        // Principal.
        for (int i = 0; i < t.length; i++) {
            IAccessEntry p = permissions.getEntry(t[i].getIdentity(), Dummy.dummy);
            AccessRule[] flat = p.getAccessRules();

            boolean valid = (flat.length > 0 && rules.length == 0 ? true : false);
            for (int k = 0; !valid && k < rules.length; k++)
                for (int j = 0; !valid && j < flat.length; j++)
                    if (flat[j].equals(rules[k]))
                        valid = true;

            if (valid) {
                rslt.add(new AccessBroker.AccessEntryImpl(
                            t[i].getIdentity(), flat, t[i].getTarget()));
            }
        }

        return (IAccessEntry[])rslt.toArray(new IAccessEntry[0]);
    }

    // TODO: XXX: This will not function as expected when a user instance has a
    // target, but not specific permissions. AccessBroker.AccessEntryImpl
    // errornously throws IllegalArgumentExceptions when rules is empty.
    public IAccessEntry getEntry(Identity y, Object target) {
        // Retrieve the AccessRules for the given identity

        IAccessEntry p = permissions.getEntry(y, Dummy.dummy);

        if (p == null)
            return null;

        return new AccessBroker.AccessEntryImpl(y, p.getAccessRules(), target);
    }

    public IAccessEntry setAccess(Identity y, AccessRule[] rules,
                                  Object target) {
        boolean isDummy = (rules == null || rules.length == 0 ? true : false);
        for (int i = 0; !isDummy && i < rules.length; i++)
            if (dummyrules[0].getAccessType().equals(rules[i].getAccessType()))
                isDummy = true;

        if (!isDummy) {
            // Send AccessRules to permissions
            permissions.setAccess(y, rules, Dummy.dummy);
        }

        if (targets != null && target != null && !Dummy.dummy.equals(target)) {
            // Send Target to targets
            IAccessEntry t = targets.setAccess(y, dummyrules, target);
        }

        return new AccessBroker.AccessEntryImpl(y, rules, target);
    }

    public void removeAccess(IAccessEntry[] entries) {
        List pEntries = new ArrayList();
        List tEntries = new ArrayList();

        for (int i = 0; i < entries.length; i++) {
            Identity y = entries[i].getIdentity();
            Object o = entries[i].getTarget();

            IAccessEntry p = permissions.getEntry(y, Dummy.dummy);
            IAccessEntry t = null;
            if (targets != null)
                t = targets.getEntry(y, o);

            // It is permitted to not have an entry for each identity, though
            // there should at least be one or the other (or else the
            // AccessEntry couldn't have existed).
            if (p != null)
                pEntries.add(p);
            if (t != null)
                tEntries.add(t);
        }

        if (!pEntries.isEmpty())
            permissions.removeAccess((IAccessEntry[])pEntries.toArray(new IAccessEntry[0]));
        if (!tEntries.isEmpty() && targets != null)
            targets.removeAccess((IAccessEntry[])tEntries.toArray(new IAccessEntry[0]));
    }
}

