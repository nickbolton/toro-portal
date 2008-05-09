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

package net.unicon.civis.fac;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Element;

import net.unicon.penelope.IDecisionCollection;

import net.unicon.civis.CivisRuntimeException;
import net.unicon.civis.Event;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IEventListener;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.civis.ITransaction;
import net.unicon.civis.ITransactionContext;
import net.unicon.civis.ITransactionRegistry;

public abstract class AbstractCivisFactory implements ICivisFactory {

    // Instance Members.
    private final Map listeners = Collections.synchronizedMap(new HashMap());

    /*
     * Public API.
     */

    public static ICivisFactory parse(Element e) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("civis-factory")) {
            String msg = "Argument 'e [Element]' must be a <civis-factory> "
                                                            + "element.";
            throw new IllegalArgumentException(msg);
        }

        // Impl.
        Attribute p = e.attribute("impl");
        if (p == null) {
            String msg = "Element <civis-factory> is missing required "
                                            + "attribute 'impl'.";
            throw new IllegalArgumentException(msg);
        }
        String impl = p.getValue();

        ICivisFactory rslt = null;
        try {

            Class c = Class.forName(impl);
            Method m = c.getDeclaredMethod("parse", new Class[]
                                        { Element.class });
            rslt = (ICivisFactory) m.invoke(null, new Object[] { e });

        } catch (Throwable t) {
            String msg = "Unable to evaluate the specified factory:  " + impl;
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }

    public static ICivisFactory fromUrl(String url) {

        // Assertions.
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        ICivisFactory rslt = null;
        try {

            String[] tokens = url.split("/");
            String className = tokens[2];
            Class c = Class.forName(className);
            Method m = c.getDeclaredMethod("fromUrl", new Class[] {
                                                String.class });
            rslt = (ICivisFactory) m.invoke(null, new Object[] { url });

        } catch (Throwable t) {
            String msg = "Unable to evaluate the specified factory:  " + url;
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }
    
    public static ICivisEntity entityFromUrl(String url) {

        // Assertions.
        if (url == null) {
            String msg = "Argument 'url' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        ICivisEntity rslt = null;
        try {

            ICivisFactory fac = fromUrl(url);
            String[] tokens = url.split("/");
            tokens = tokens[3].split("@");
            if(tokens.length == 2){
                if(tokens[0].indexOf("Person") > -1)
                    rslt = fac.getPerson(tokens[1]);
                else
                    rslt = fac.getGroupById(tokens[1]);
            }else{
                throw new Exception();   
            }
        } catch (Throwable t) {
            String msg = "Unable to evaluate the specified entity:  " + url;
            throw new RuntimeException(msg, t);
        }

        return rslt;

    }    

    public void startListening(Event e, IEventListener listener) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Event]' cannot be null";
        }
        if (listener == null) {
            String msg = "Argument 'listener' cannot be null";
        }

        // NB:  Using a synchronized set instance here,
        // so no need to micro-manage thread safety.

        Set s = (Set) listeners.get(e);
        if (s == null) {
            s = Collections.synchronizedSet(new HashSet());
            listeners.put(e, s);
        }
        s.add(e);

    }

    public void stopListening(Event e, IEventListener listener) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Event]' cannot be null";
        }
        if (listener == null) {
            String msg = "Argument 'listener' cannot be null";
        }

        // NB:  Using a synchronized set instance here,
        // so no need to micro-manage thread safety.

        Set s = (Set) listeners.get(e);
        if (s == null) {
            // This is a no-op...
            return;
        }
        s.remove(e);
        if (s.isEmpty()) {
            // Perform a little cleanup...
            listeners.remove(e);
        }

    }

    public ITransaction beginTransaction() {
        return new TransactionImpl();
    }

    public IPerson createPerson(String name, IDecisionCollection attr) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        IPerson rslt = createPerson(trans, name, attr);
        trans.commit();
        return rslt;

    }

    public IPerson createPerson(ITransaction trans, String name,
                                IDecisionCollection attr) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (name == null) {
            String msg = "Argument 'name' cannot be null";
        }
        if (attr == null) {
            String msg = "Argument 'attr' cannot be null";
        }

        IPerson rslt = this.doCreatePerson(trans, name, attr);

        // Trigger events...
        invokeEvent(Event.PERSON_CREATED, trans, rslt);

        return rslt;

    }

    public void rename(IPerson p, String name) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        rename(trans, p, name);
        trans.commit();

    }

    public void rename(ITransaction trans, IPerson p, String name) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (p == null) {
            String msg = "Argument 'p [IPerson]' cannot be null";
        }
        if (name == null) {
            String msg = "Argument 'name' cannot be null";
        }

        this.doRename(trans, p, name);

        // Trigger events...
        invokeEvent(Event.PERSON_MODIFIED, trans, p);

    }

    public void updateAttributes(IPerson p, IDecisionCollection attr) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        updateAttributes(trans, p, attr);
        trans.commit();

    }

    public void updateAttributes(ITransaction trans, IPerson p,
                                IDecisionCollection attr) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (p == null) {
            String msg = "Argument 'p [IPerson]' cannot be null";
        }
        if (attr == null) {
            String msg = "Argument 'attr' cannot be null";
        }

        this.doUpdateAttributes(trans, p, attr);

        // Trigger events...
        invokeEvent(Event.PERSON_MODIFIED, trans, p);

    }

    public void delete(IPerson p) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        delete(trans, p);
        trans.commit();

    }

    public void delete(ITransaction trans, IPerson p) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (p == null) {
            String msg = "Argument 'p [IPerson]' cannot be null";
        }

        this.doDelete(trans, p);

        // Trigger events...
        invokeEvent(Event.PERSON_DELETED, trans, p);

    }

    public IGroup createGroup(IGroup parent, String name,
                            IDecisionCollection attr) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        IGroup rslt = createGroup(trans, parent, name, attr);
        trans.commit();
        return rslt;

    }

    public IGroup createGroup(ITransaction trans, IGroup parent, String name,
                                                IDecisionCollection attr) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null";
        }
        if (name == null) {
            String msg = "Argument 'name' cannot be null";
        }
        if (attr == null) {
            String msg = "Argument 'attr' cannot be null";
        }

        IGroup rslt = this.doCreateGroup(trans, parent, name, attr);

        // Trigger events...
        invokeEvent(Event.GROUP_CREATED, trans, rslt);

        return rslt;

    }

    public void rename(IGroup g, String name) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        rename(trans, g, name);
        trans.commit();

    }

    public void rename(ITransaction trans, IGroup g, String name) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (g == null) {
            String msg = "Argument 'g [IGroup]' cannot be null";
        }
        if (name == null) {
            String msg = "Argument 'name' cannot be null";
        }

        this.doRename(trans, g, name);

        // Trigger events...
        invokeEvent(Event.GROUP_MODIFIED, trans, g);

    }

    public void updateAttributes(IGroup g, IDecisionCollection attr) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        updateAttributes(trans, g, attr);
        trans.commit();

    }

    public void updateAttributes(ITransaction trans, IGroup g,
                                IDecisionCollection attr) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (g == null) {
            String msg = "Argument 'g [IGroup]' cannot be null";
        }
        if (attr == null) {
            String msg = "Argument 'attr' cannot be null";
        }

        this.doUpdateAttributes(trans, g, attr);

        // Trigger events...
        invokeEvent(Event.GROUP_MODIFIED, trans, g);

    }

    public void delete(IGroup g) {

        // Assertions.
        // NB:  Handled by the overload...

        ITransaction trans = new TransactionImpl();
        delete(trans, g);
        trans.commit();

    }

    public void delete(ITransaction trans, IGroup g) {

        // Assertions.
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (g == null) {
            String msg = "Argument 'g [IGroup]' cannot be null";
        }

        this.doDelete(trans, g);

        // Trigger events...
        invokeEvent(Event.GROUP_DELETED, trans, g);

    }
    
    public boolean equals(Object o){
        if(!(o instanceof ICivisFactory))
            return false;
        if(((ICivisFactory)o).getUrl().equals(this.getUrl())) 
            return true;
        return false;
    }

    /*
     * Protected API.
     */

    protected abstract IGroup[] getGroups(IPerson p);

    protected abstract IPerson[] getMembers(IGroup g, boolean deep);

    protected abstract IGroup[] getSubgroups(IGroup g);
    
    protected abstract IGroup[] getDescendentGroups(IGroup g);

    protected abstract IPerson doCreatePerson(ITransaction trans, String name,
                                                IDecisionCollection attr);

    protected abstract void doRename(ITransaction trans, IPerson p,
                                                    String name);

    protected abstract void doUpdateAttributes(ITransaction trans, IPerson p,
                                                IDecisionCollection attr);

    protected abstract void doDelete(ITransaction trans, IPerson p);

    protected abstract IGroup doCreateGroup(ITransaction trans, IGroup parent,
                                    String name, IDecisionCollection attr);

    protected abstract void doRename(ITransaction trans, IGroup g, String name);

    protected abstract void doUpdateAttributes(ITransaction trans, IGroup g,
                                                IDecisionCollection attr);

    protected abstract void doDelete(ITransaction trans, IGroup g);
    
    /*
     * Implementation.
     */

    private void invokeEvent(Event e, ITransaction trans, ICivisEntity target) {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Event]' cannot be null";
        }
        if (trans == null) {
            String msg = "Argument 'trans' cannot be null";
        }
        if (target == null) {
            String msg = "Argument 'target' cannot be null";
        }

        // Trigger events...
        Set s = (Set) listeners.get(e);
        if (s != null) {
            try {
                Iterator it = s.iterator();
                while (it.hasNext()) {
                    IEventListener el = (IEventListener) it.next();
                    el.invoke(((TransactionImpl) trans).getRegistry(), target);
                }
            } catch (Throwable t) {
                String msg = "Civis transaction aborted due to exception in "
                                                        + "event listener.";
                throw new CivisRuntimeException(msg, t);
            }
        }

    }

    /*
     * Nested Types.
     */

    private static final class TransactionRegistryImpl
                implements ITransactionRegistry {

        // Instance Members.
        private final Set contexts;

        /*
         * Public API.
         */

        public TransactionRegistryImpl() {

            // Instance Members.
            this.contexts = Collections.synchronizedSet(new HashSet());

        }

        public void registerContext(ITransactionContext ctx) {

            // Assertions.
            if (ctx == null) {
                String msg = "Argument 'ctx' cannot be null";
            }

            contexts.add(ctx);

        }

        public Iterator iterator() {
            return contexts.iterator();
        }

    }

    private static final class TransactionImpl implements ITransaction {

        // Instance Members.
        private final TransactionRegistryImpl registry;
        private boolean completed;

        /*
         * Public API.
         */

        public TransactionImpl() {

            // Instance Members.
            this.registry = new TransactionRegistryImpl();
            this.completed = false;

        }

        public TransactionRegistryImpl getRegistry() {
            return registry;
        }

        public void commit() {

            // Assertions.
            if (completed) {
                String msg = "This transaction is already completed.";
                throw new IllegalStateException(msg);
            }

            Iterator it = registry.iterator();
            while (it.hasNext()) {
                ITransactionContext ctx = (ITransactionContext) it.next();
                ctx.commit();
            }
            completed = true;

        }

        public void rollback() {

            // Assertions.
            if (completed) {
                String msg = "This transaction is already completed.";
                throw new IllegalStateException(msg);
            }

            Iterator it = registry.iterator();
            while (it.hasNext()) {
                ITransactionContext ctx = (ITransactionContext) it.next();
                ctx.rollback();
            }
            completed = true;

        }


    }

    protected static abstract class BaseCivisEntity implements ICivisEntity {

        // Instance Members.
        private final String id;
        private final ICivisFactory owner;
        private String name;
        private IDecisionCollection attributes;

        /*
         * Public API.
         */

        public final String getId() {
            return id;
        }

        public final ICivisFactory getOwner() {
            return owner;
        }

        public final String getName() {
            return name;
        }

        public final IDecisionCollection getAttributes() {
            return attributes;
        }
        
        public boolean equals(Object o){
            if(!(o instanceof BaseCivisEntity))
                return false;
            if(((BaseCivisEntity)o).getOwner().equals(this.owner) 
                    && ((BaseCivisEntity)o).getId() == this.id)
                return true;
            return false;
        }
        
        public int compareTo(Object o){
            
            int rslt = 0;
            
            if(!this.equals(o)){
	            if(o instanceof IGroup){
	                if(this instanceof IGroup){
	                    String[] gPath1 = ((IGroup)this).getPath().split(IGroup.GROUP_PATH_SEPARATOR);
	                    String[] gPath2 = ((IGroup)o).getPath().split(IGroup.GROUP_PATH_SEPARATOR);
	                    
	                    if (gPath1.length == gPath2.length){ 
	                        rslt = 0;
	                    }else if (gPath1.length < gPath2.length){
	                        rslt = 1;
	                    }else{
	                        rslt = -1;
	                    }
	                }else{
	                    return -1;
	                }
	            }else{
	                if(this instanceof IGroup){
	                    rslt = 1;
	                }
	            }
            }
            return rslt;
        }


        /*
         * Protected API.
         */

        protected BaseCivisEntity(String id, ICivisFactory owner, String name,
                                        IDecisionCollection attributes) {

            // Assertions.
            if (id == null) {
                String msg = "Argument 'id' cannot be null.";
            }
            if (owner == null) {
                String msg = "Argument 'owner' cannot be null";
            }
            if (name == null) {
                String msg = "Argument 'name' cannot be less than zero.";
            }
            if (attributes == null) {
                String msg = "Argument 'attributes' cannot be less than zero.";
            }

            // Instance Members.
            this.id = id;
            this.owner = owner;
            this.name = name;
            this.attributes = attributes;

        }

    }

    protected static final class PersonImpl extends BaseCivisEntity
                                            implements IPerson {

        /*
         * Public API.
         */

        public PersonImpl(String id, ICivisFactory owner, String name,
                                    IDecisionCollection attributes) {
            super(id, owner, name, attributes);
        }

        public IGroup[] getGroups() {
            return ((AbstractCivisFactory) getOwner()).getGroups(this);
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<person ");

            // Id.
            rslt.append("id=\"").append(String.valueOf(getId())).append("\">");

            // Label.
            rslt.append("<name>").append(getName()).append("</name>");

            // Attributes.
            rslt.append(getAttributes().toXml());

            // End.
            rslt.append("</person>");
            return rslt.toString();

        }
        
        
        public boolean equals(Object o){
            if(!(o instanceof IPerson))
                return false;
            if(((IPerson)o).getOwner().equals(this.getOwner()) 
                    && ((IPerson)o).getName().equals(this.getName()))
                return true;
            return false;
        }
        
        public String getUrl(){
            StringBuffer buf = new StringBuffer(this.getOwner().getUrl());
            buf.append("/").append(this.getClass().getName());
            buf.append("@").append(this.getName());
            return buf.toString();
        }

    }

    protected static final class GroupImpl extends BaseCivisEntity
                                            implements IGroup {

        // instance members
        private final String path;
        
        /*
         * Public API.
         */

        public GroupImpl(String id, ICivisFactory owner, String name,
                                    IDecisionCollection attributes, String path) {
            super(id, owner, name, attributes);
            this.path = path;
        }

        public IPerson[] getMembers(boolean deep) {
            return ((AbstractCivisFactory) getOwner()).getMembers(this, deep);
        }

        public IGroup[] getSubgroups() {
            return ((AbstractCivisFactory) getOwner()).getSubgroups(this);
        }
        
        public IGroup[] getDescendentGroups() {
            return ((AbstractCivisFactory) getOwner()).getDescendentGroups(this);
        }
        
        public String toString() {
        	return path;
        }
        
        public boolean equals(Object o){
            if(!(o instanceof IGroup))
                return false;
            return (((IGroup)o).getOwner().equals(this.getOwner()) 
                    && ((IGroup)o).getPathAndId().equals(this.getPathAndId()));
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<group ");

            // Id.
            rslt.append("id=\"").append(getId()).append("\">");

            // Label.
            rslt.append("<name>").append(getName()).append("</name>");

            // Attributes.
            rslt.append(getAttributes().toXml());

            // End.
            rslt.append("</group>");
            return rslt.toString();

        }
        
        public String getPath() {
            return path;
        }
        
        public String getUrl(){
            StringBuffer buf = new StringBuffer(this.getOwner().getUrl());
            buf.append("/").append(this.getClass().getName());
            buf.append("@").append(this.getId());
            return buf.toString();
        }

		public String getPathAndId() {
			return path + "[" + getId() + "]";
		}        

    }

}
