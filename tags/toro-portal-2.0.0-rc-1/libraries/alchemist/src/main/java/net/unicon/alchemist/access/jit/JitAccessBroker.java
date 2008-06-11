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

package net.unicon.alchemist.access.jit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;

public final class JitAccessBroker extends AccessBroker {

    // Instance Members.
    private final AccessBroker enclosed;
    private final Rule[] rules;

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

        // Rules.
        List rList = e.selectNodes("jit-rule");
        if (rList.size() == 0) {
            String msg = "Element <access-broker> must contain at least one "
                                            + "<jit-rule> chield element.";
            throw new IllegalArgumentException(msg);
        }
        Rule[] rules = new Rule[rList.size()];
        for (int i=0; i < rules.length; i++) {
            Element r = (Element) rList.get(i);
            rules[i] = Rule.parse(r);
        }

        // Enclosed.
        Element b = (Element) e.selectSingleNode("access-broker");
        if (b == null) {
            String msg = "Element <acess-broker> is missing required child "
                                            + "element <acess-broker>.";
            throw new IllegalArgumentException(msg);
        }
        AccessBroker enclosed = AccessBroker.parse(b);

        return new JitAccessBroker(e, rules, enclosed);

    }

    public JitAccessBroker(Element e, Rule[] rules, AccessBroker enclosed) {

        super(e);

        // Assertions.
        if (rules == null) {
            String msg = "Argument 'rules' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // NB:  Argument 'rules' may contain zero elements.
        if (enclosed == null) {
            String msg = "Argument 'enclosed' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.rules = new Rule[rules.length];
        System.arraycopy(rules, 0, this.rules, 0, rules.length);
        this.enclosed = enclosed;

    }

    public IAccessEntry setAccess(Identity y, AccessRule[] aRules,
                                            Object target) {
        return enclosed.setAccess(y, aRules, target);
    }

    public void removeAccess(IAccessEntry[] entries) {
        enclosed.removeAccess(entries);
    }

    public IAccessEntry getEntry(Identity y, Object target) {
        return enclosed.getEntry(y, target);
    }

    public IAccessEntry[] getEntries(Principal p, AccessRule[] aRules) {

        /*
         * This method is where the 'just-in-time' behavior takes place.  We
         * need to make checks to see if new entries must be created for the
         * specified principal.,
         */
/*System.out.println("Principal Data : ");
Identity[] identities = p.getIdentities();
for(int i = 0; i < identities.length; i++){
	System.out.println("Identity : " + identities[i].getId());
}*/
        ArrayList rslt = new ArrayList(Arrays.asList(enclosed.getEntries(p, aRules)));

        // Look at our rule(s).
        Iterator it = Arrays.asList(rules).iterator();
        while (it.hasNext()) {

            Rule r = (Rule) it.next();

            /*
             * ~~ QUALIFICATION ~~
             * We need to evaluate whether to invoke the behavior or not.
             */

            // CHECK #1:  See if there is a behavior defined for this principal.
            Behavior b = r.chooseBehavior(p);
            if (b == null) {
                // no behavior, so get out...
                continue;
            }

            // CHECK #2:  Check to see if the target specified
            // by the behavior is included in the principal.
            Identity id = null;
            Identity[] ids = p.getIdentities();
            for (int i=0; i < ids.length; i++) {
                if (b.getTarget().matches(ids[i])) {
                    id = ids[i];
                    break;
                }
            }
            
 
            if (id == null) {
                // target not included, so get out...
            	continue;
            }

            // CHECK #3:  Check the results from the enclosed broker to
            // see if an entry already exists for the specified target.
            IAccessEntry match = null;
            Iterator entries = rslt.iterator();
            while (entries.hasNext()) {
                IAccessEntry e = (IAccessEntry) entries.next();
                if (b.getTarget().matches(e.getIdentity())) {
                    match = e;
                    break;
                }
            }
            if (match != null) {
                // entry already there, so get out...
            	continue;
            }

            // CHECK #4:  Check the access type(s) defined for the behavior, to
            // see if it *should* be included in the results of this method.
            boolean overlap = false;
            List aList = Arrays.asList(aRules);
            Iterator access = Arrays.asList(b.getAccess()).iterator();
            while (access.hasNext()) {
                AccessRule y = (AccessRule) access.next();
                if (aList.contains(y)) {
                    overlap = true;
                    break;
                }
            }
            if (overlap) {
                // no overlap of access, so get out...
            	continue;
            }

            /*
             * ~~ INVOKATION ~~
             * If we've reached this point in the
             * method, we need to invoke the behavior.
             */

            // Create the new object.
            Object o = b.getCreator().create(id);

            // Create the entry on the enclosed broker.
            IAccessEntry entry = enclosed.setAccess(id, b.getAccess(), o);
            
            // Add it to the results.
            rslt.add(entry);

        }
        return (IAccessEntry[]) rslt.toArray(new IAccessEntry[0]);

    }

   public IAccessEntry[] getEntries(Object target, AccessRule[] aRules) {
        return enclosed.getEntries(target, aRules);
    }
   
   public IAccessEntry[] getEntries() {
       return enclosed.getEntries();
   }

    /*
     * Nested Types.
     */

    private static final class IdentityMatcher {

        // Instance Members.
        private final IdentityType type;
        private final String id;

        /*
         * Public API.
         */

        public static IdentityMatcher parse(Element e) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("trigger") && !e.getName()
                                        .equals("target")) {
                String msg = "Argument 'e [Element]' must be either a "
                                + "<trigger> or a <target> element.";
                throw new IllegalArgumentException(msg);
            }

            // Type.
            Attribute y = e.attribute("type");
            if (y == null) {
                String msg = "Element <" + e.getName() + "> is missing "
                                    + "required attribute 'icon'.";
                throw new IllegalArgumentException(msg);
            }
            IdentityType type = IdentityType.getInstance(y.getValue());

            // Id.
            String id = null;
            String text = e.getText().trim();
            if (text.length() != 0) {
                id = text;
            }

            return new IdentityMatcher(type, id);

        }

        public IdentityMatcher(IdentityType type, String id) {

            // Assertions.
            if (type == null) {
                String msg = "Argument 'type' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            // NB:  id may be null.

            // Instance Members.
            this.type = type;
            this.id = id;

        }

        public String getId() {
            return id;
        }

        public IdentityType getType() {
            return type;
        }

        public boolean matches(Identity y) {

            return y.getType().equals(type) && (id == null ||
                                    y.getId().equalsIgnoreCase(id));

        }

    }

    private static final class Behavior {

        // Instance Members.
        private final IdentityMatcher trigger;
        private final IdentityMatcher target;
        private final AccessRule[] aRules;
        private final ICreator creator;

        /*
         * Public API.
         */

        public static Behavior parse(Element e) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("behavior")) {
                String msg = "Argument 'e [Element]' must be an <behavior> "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Trigger.
            Element g = (Element) e.selectSingleNode("trigger");
            if (g == null) {
                String msg = "Element <behavior> is missing required child "
                                                + "element <trigger>.";
                throw new IllegalArgumentException(msg);
            }
            IdentityMatcher trigger = IdentityMatcher.parse(g);

            // Target.
            Element r = (Element) e.selectSingleNode("target");
            if (r == null) {
                String msg = "Element <behavior> is missing required child "
                                                    + "element <target>.";
                throw new IllegalArgumentException(msg);
            }
            IdentityMatcher target = IdentityMatcher.parse(r);

            // Access.
            r = (Element) e.selectSingleNode("access");   
            if (r == null) {
                String msg = "Element <access> is missing. ";                                        
                throw new IllegalArgumentException(msg);
            }
            AccessRule[] accessRules = AccessRule.parse(r);
            
            // Creator.
            Element a = (Element) e.selectSingleNode("creator");
            ICreator creator = null;
            if (a != null) {
                Attribute impl = a.attribute("impl");
                if (impl == null) {
                    String msg = "Element <creator> is missing required "
                                                + "attribute 'impl'.";
                    throw new IllegalArgumentException(msg);
                }
                try {
                    Class c = Class.forName(impl.getValue());
                    Method m = c.getDeclaredMethod("parse", new Class[] {
                                                    Element.class });
                    creator = (ICreator) m.invoke(null, new Object[] { a });
                } catch (Throwable t) {
                    String msg = "Unable to create the specified creator:  "
                                                        + impl.getValue();
                    throw new RuntimeException(msg, t);
                }
            } else {
                String msg = "Element <behavior> is missing required child "
                                                + "element <creator>.";
                throw new IllegalArgumentException(msg);
            }

            return new Behavior(trigger, target, accessRules, creator);

        }

        public Behavior(IdentityMatcher trigger, IdentityMatcher target,
                            AccessRule[] access, ICreator creator) {

            // Assertions.
            if (trigger == null) {
                String msg = "Argument 'trigger' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (target == null) {
                String msg = "Argument 'target' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (access == null) {
                String msg = "Argument 'access' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (access.length == 0) {
                String msg = "Argument 'access' must contain at least one "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }
            if (creator == null) {
                String msg = "Argument 'creator' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.trigger = trigger;
            this.target = target;
            this.aRules = new AccessRule[access.length];
            System.arraycopy(access, 0, this.aRules, 0, access.length);
            this.creator = creator;

        }

        public IdentityMatcher getTrigger() {
            return trigger;
        }

        public IdentityMatcher getTarget() {
            return target;
        }

        public AccessRule[] getAccess() {
            AccessRule[] rslt = new AccessRule[aRules.length];
            System.arraycopy(aRules, 0, rslt, 0, aRules.length);
            return rslt;
        }

        public ICreator getCreator() {
            return creator;
        }

    }

    private static final class Rule {

        // Instance Members.
        private final Behavior[] behaviors;

        /*
         * Public API.
         */

        public static Rule parse(Element e) {

            // Assertions.
            if (e == null) {
                String msg = "Argument 'e [Element]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (!e.getName().equals("jit-rule")) {
                String msg = "Argument 'e [Element]' must be a <jit-rule> "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Behaviors.
            List bList = e.selectNodes("behavior");
            if (bList.size() == 0) {
                String msg = "Element <jit-rule> must contain at least one "
                                        + "<behavior> chield element.";
                throw new IllegalArgumentException(msg);
            }
            Behavior[] behaviors = new Behavior[bList.size()];
            for (int i=0; i < behaviors.length; i++) {
                Element b = (Element) bList.get(i);
                behaviors[i] = Behavior.parse(b);
            }

            return new Rule(behaviors);

        }

        public Rule(Behavior[] behaviors) {

            // Assertions.
            if (behaviors == null) {
                String msg = "Argument 'behaviors' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (behaviors.length == 0) {
                String msg = "Argument 'behaviors' must contain at least one "
                                                            + "element.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members
            this.behaviors = new Behavior[behaviors.length];
            System.arraycopy(behaviors, 0, this.behaviors, 0, behaviors.length);

        }

        public Behavior chooseBehavior(Principal p) {

            // Assertions.
            if (p == null) {
                String msg = "Argument 'p [Principal]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            Behavior rslt = null;   // default...

            // Look for a behavior that triggers.
            Iterator it = Arrays.asList(behaviors).iterator();
            while (it.hasNext()) {

                Behavior b = (Behavior) it.next();
                IdentityMatcher trigger = b.getTrigger();

                // Select the behavior that matches the trigger, if any.
                Identity[] ids = p.getIdentities();
                for (int i=0; i < ids.length; i++) {
                    if (trigger.matches(ids[i])) {
                        rslt = b;
                        break;
                    }
                }

                // The first matching behavior is the only one
                // that matters, so no need to continue.
                if (rslt != null) {
                    break;
                }

            }

            return rslt;

        }

    }

}
