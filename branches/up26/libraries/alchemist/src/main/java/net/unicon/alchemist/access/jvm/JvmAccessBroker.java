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

package net.unicon.alchemist.access.jvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.Principal;

import org.dom4j.Element;

public final class JvmAccessBroker extends AccessBroker {

    // Instance Members.
    private Map byIdentity;
    private Map byTarget;

    /*
     * Public API.
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

        // Entries.
        List eList = e.selectNodes("entry");
        IAccessEntry[] entries = new IAccessEntry[eList.size()];
        for (int i=0; i < eList.size(); i++) {
            Element n = (Element) eList.get(i);
            entries[i] = AccessEntryImpl.parse(n);
        }

        return new JvmAccessBroker(e, entries);

    }

    public JvmAccessBroker(Element e, IAccessEntry[] entries) {

        super(e);

        // Assertions.
        if (entries == null) {
            String msg = "Argument 'entries' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.byIdentity = new HashMap();
        this.byTarget = new HashMap();

        // Populatey the data structures.
        Iterator it = Arrays.asList(entries).iterator();
        while (it.hasNext()) {

            IAccessEntry n = (IAccessEntry) it.next();

            // Identity.
            List iList = (List) byIdentity.get(n.getIdentity());
            if (iList == null) {
                iList = new ArrayList();
                byIdentity.put(n.getIdentity(), iList);
            }
            iList.add(n);

            // Target.
            List tList = (List) byTarget.get(n.getTarget());
            if (tList == null) {
                tList = new ArrayList();
                byTarget.put(n.getTarget(), tList);
            }
            tList.add(n);

        }

    }

    public IAccessEntry setAccess(Identity y, AccessRule[] rules,
                                            Object target) {

        // Assertions.
        if (y == null) {
            String msg = "Argument 'y [Identity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IAccessEntry rslt = new AccessEntryImpl(y, rules, target);

        // Identity.
        List iList = (List) byIdentity.get(y);
        if (iList == null) {
            iList = new ArrayList();
            byIdentity.put(y, iList);
        }
        iList.add(rslt);

        // Target.
        List tList = (List) byTarget.get(target);
        if (tList == null) {
            tList = new ArrayList();
            byTarget.put(target, tList);
        }
        tList.add(rslt);

        return rslt;

    }

    public void removeAccess(IAccessEntry[] entries) {

        // Assertions.
        if (entries == null) {
            String msg = "Argument 'entries' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Iterator it = Arrays.asList(entries).iterator();
        while (it.hasNext()) {

            IAccessEntry e = (IAccessEntry) it.next();

            // Principals.
            List iList = (List) byIdentity.get(e.getIdentity());
            if (iList != null) {
                iList.remove(e);
            }

            // Targets.
            List tList = (List) byTarget.get(e.getTarget());
            if (tList != null) {
                tList.remove(e);
            }

        }

    }

    public IAccessEntry getEntry(Identity y, Object target) {

        // Assertions.
        if (y == null) {
            String msg = "Argument 'y [Identity]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IAccessEntry rslt = null;   // default...

        // Look by identity.
        List iList = (List) byIdentity.get(y);
        if (iList != null) {

            // Now look for matching target.
            Iterator it = iList.iterator();
            while (it.hasNext()) {
                IAccessEntry e = (IAccessEntry) it.next();
                if (e.getTarget().equals(target)) {
                    rslt = e;
                    break;
                }
            }

        }

        return rslt;

    }

    public IAccessEntry[] getEntries(Principal p, AccessRule[] rules) {

        // Assertions.
        if (p == null) {
            String msg = "Argument 'p [Principal]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        List rslt = new ArrayList();

        // Look at all the specified identities.
        Iterator it = Arrays.asList(p.getIdentities()).iterator();
        while (it.hasNext()) {

            Identity y = (Identity) it.next();

            List iList = (List) byIdentity.get(y);
            if (iList != null) {

                // Now look at all the entries for that identity.
                Iterator entries = iList.iterator();
                while (entries.hasNext()) {

                    IAccessEntry e = (IAccessEntry) entries.next();

                    switch (rules.length) {
                        case 0:
                            rslt.add(e);
                            break;
                        default:
                            List access = Arrays.asList(e.getAccessRules());
                            for (int i=0; i < rules.length; i++) {
                                if (access.contains(rules[i])) {
                                    rslt.add(e);
                                    break;
                                }
                            }
                            break;
                    }

                }

            }

        }

        return (IAccessEntry[]) rslt.toArray(new IAccessEntry[0]);

    }

    public IAccessEntry[] getEntries(Object target, AccessRule[] rules) {

        // Assertions.
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        List rslt = new ArrayList();

        // Look at the entries for the specified target.
        List tList = (List) byTarget.get(target);
        if (tList != null) {

            Iterator entries = tList.iterator();
            while (entries.hasNext()) {

                IAccessEntry e = (IAccessEntry) entries.next();

                switch (rules.length) {
                    case 0:
                        rslt.add(e);
                        break;
                    default:
                        List access = Arrays.asList(e.getAccessRules());
                        for (int i=0; i < rules.length; i++) {
                            if (access.contains(rules[i])) {
                                rslt.add(e);
                                break;
                            }
                        }
                        break;
                }

            }

        }

        return (IAccessEntry[]) rslt.toArray(new IAccessEntry[0]);

    }
    public IAccessEntry[] getEntries() {

        List rslt = new ArrayList();

        // get all the entries by target.
        Iterator targets = byTarget.keySet().iterator();
        Object target = null;
        while (targets.hasNext()){
            target = targets.next();
	        List tList = (List) byTarget.get(target);
	        if (tList != null) {
	            Iterator entries = tList.iterator();
	            while (entries.hasNext()) {
	                 IAccessEntry e = (IAccessEntry) entries.next();
	                 rslt.add(e);
	            }
	
	        }
	    }

        return (IAccessEntry[]) rslt.toArray(new IAccessEntry[0]);

    }    
}