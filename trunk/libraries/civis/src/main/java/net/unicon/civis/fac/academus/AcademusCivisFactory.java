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

package net.unicon.civis.fac.academus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.api.IAcademusUser;
import net.unicon.civis.CivisRuntimeException;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.civis.ITransaction;
import net.unicon.civis.fac.AbstractCivisFactory;
import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeText64;
import net.unicon.penelope.store.jvm.JvmEntityStore;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class AcademusCivisFactory extends AbstractCivisFactory {
        
    // Static members.

    private static IChoiceCollection groupAttr = createGroupAttributes();
    
    // Instance members.

    private ExtendedAttribute[] personExtraAttrs = null;
    private IChoiceCollection personAttr = null;
    private static final String url = "CIVIS://" +  AcademusCivisFactory.class.getName(); 

    /*
     * Static API.
     */

    public static ICivisFactory parse(Element e) {
        List el = e.selectNodes("person-attributes/attribute");
        List attrs = new ArrayList(el.size());
        for (Iterator it = el.iterator(); it.hasNext(); ) {
            attrs.add( ExtendedAttribute.parse((Element)it.next()) );
        }
        ExtendedAttribute[] personExtraAttrs =
                (ExtendedAttribute[])attrs.toArray(new ExtendedAttribute[0]);

        return new AcademusCivisFactory(personExtraAttrs);
    }

    public static ICivisFactory fromUrl(String url) {
        return new AcademusCivisFactory();
    }

    /*
     * Public API.
     */

    public AcademusCivisFactory() {
        this(new ExtendedAttribute[0]);        
    }

    public IChoiceCollection getGroupAttributes() {
        return groupAttr;
    }

    public IChoiceCollection getPersonAttributes() {
        return personAttr;
    }
    
    public IGroup getRoot() {
        IGroup rslt = null;

        try {
            IAcademusGroup group = AcademusFacadeContainer.retrieveFacade().getRootGroup();
            rslt = createGroup(group);
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getRoot() ", e);
        }

        return rslt;
    }

    public IPerson getPerson(String name) {
        IPerson rslt = null;

        try {
            rslt = createPerson(AcademusFacadeContainer.retrieveFacade().getUser(name));
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getPerson() ", e);
        }

        return rslt;
    }
    
    /* (non-Javadoc)
     * @see net.unicon.civis.ICivisFactory#getGroupByPath(java.lang.String)
     */
    public IGroup getGroupByPath(String groupPath) {
        IGroup rslt = null;

        try {
            IAcademusGroup group = AcademusFacadeContainer.retrieveFacade().getGroupByPath(groupPath);
            rslt = createGroup(group);            
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getGroupByPath() for groupPath " + groupPath, e);
        }

        return rslt;
    }
    
    /* (non-Javadoc)
     * @see net.unicon.civis.ICivisFactory#getGroupById(java.lang.String)
     */
    public IGroup getGroupById(String groupId) {
        IGroup rslt = null;

        try {
            IAcademusGroup group = AcademusFacadeContainer.retrieveFacade().getGroup(groupId);
            rslt = createGroup(group);            
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getGroupById() for groupId " + groupId, e);
        }

        return rslt;
    }
    
    public String getUrl(){
        return url; 
    }
    
    /* (non-Javadoc)
     * @see net.unicon.civis.ICivisFactory#getUsers()
     */
    public IPerson[] getUsers() {
        
        IAcademusUser[] users;
        try {
            users = AcademusFacadeContainer.retrieveFacade().getAcademusUsers();
        
	        IPerson[] rslt = new IPerson[users.length];
	        
	        for(int i = 0; i < users.length; i++) {
	            rslt[i] = createPerson(users[i]);
	        }
	        
	        return rslt;
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getUsers ", e);
        }       
    }
    
    public IGroup[] searchGroupsByName(String searchString){
        IGroup[] rslt = null;

        try {
            IAcademusGroup[] groups = AcademusFacadeContainer.retrieveFacade().findAcademusGroups(searchString);
            rslt = new IGroup[groups.length];
            for(int i = 0; i < groups.length; i++){
                rslt[i] = createGroup(groups[i]);
            }
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"searchGroupsByName() for search string " + searchString, e);
        }

        return rslt;
    }
    
    public IPerson[] searchUsers(String username, String firstName
            , String lastName, String email, boolean matchAll){
        IPerson[] rslt = null;

        try {
            IAcademusUser[] users = AcademusFacadeContainer.retrieveFacade().findAcademusUsers(username, firstName
                    , lastName, email, matchAll);

            HashMap p = new HashMap();
            for(int i = 0; i < users.length; i++) {
                p.put(users[i].getUsername(), createPerson(users[i]));
            }
            rslt = (IPerson[])p.values().toArray(new IPerson[0]);
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"searchUsers(). " , e);
        }

        return rslt;
    }
    
    // protected API

    protected IGroup[] getGroups(IPerson p) {
        IGroup[] rslt = null;

        try {
            IAcademusGroup[] groups = AcademusFacadeContainer.retrieveFacade().getAllContainingGroups(p.getName());
            rslt = new IGroup[groups.length];
            for(int i = 0; i < groups.length; i++){
                rslt[i] = createGroup(groups[i]);
            }
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getGroups() for user " + p.getName(), e);
        }

        return rslt;
    }

    protected IPerson[] getMembers(IGroup g) {
        return getMembers(g, false);
    }

    protected IGroup[] getSubgroups(IGroup g) {
        IGroup[] rslt = null;

        try {
            IAcademusGroup[] groups = AcademusFacadeContainer.retrieveFacade().getGroup(g.getId()).getDescendantGroups();
            rslt = new IGroup[groups.length];
            for(int i = 0; i < groups.length; i++){
                rslt[i] = createGroup(groups[i]);
            }
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getSubgroups() for group " + g.getName(), e);
        }

        return rslt;
    }
    
    protected IGroup[] getDescendentGroups(IGroup g) {
        IGroup[] rslt = null;
        IGroup parent = null;

        try {
            IAcademusGroup[] groups = AcademusFacadeContainer.retrieveFacade().getGroup(g.getId()).getAllDescendantGroups();
            rslt = new IGroup[groups.length];
            for(int i = 0; i < groups.length; i++){
                rslt[i] = createGroup(groups[i]);
            }
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getSubgroups() for group " + g.getName(), e);
        }

        return rslt;
    }

    protected IPerson doCreatePerson(ITransaction trans, String name,
            IDecisionCollection attr) {
        throw new UnsupportedOperationException();
    }

    protected void doRename(ITransaction trans, IPerson p, String name) {
        throw new UnsupportedOperationException();
    }

    protected void doUpdateAttributes(ITransaction trans, IPerson p,
            IDecisionCollection attr) {
        throw new UnsupportedOperationException();

    }

    protected void doDelete(ITransaction trans, IPerson p) {
        throw new UnsupportedOperationException();

    }

    protected IGroup doCreateGroup(ITransaction trans, IGroup parent,
            String name, IDecisionCollection attr) {
        throw new UnsupportedOperationException();
    }

    protected void doRename(ITransaction trans, IGroup g, String name) {
        throw new UnsupportedOperationException();

    }

    protected void doUpdateAttributes(ITransaction trans, IGroup g,
            IDecisionCollection attr) {
        throw new UnsupportedOperationException();

    }

    protected void doDelete(ITransaction trans, IGroup g) {
        throw new UnsupportedOperationException();

    }

    
    protected IPerson[] getMembers(IGroup g, boolean deep) {
        IPerson[] rslt = null;

        try {
            IAcademusUser[] users = null;

            if (deep == false) {
                users = AcademusFacadeContainer.retrieveFacade().getGroup(g.getId()).getContainedUsers();
            } else {
	            users = AcademusFacadeContainer.retrieveFacade().getGroup(g.getId()).getAllContainedUsers();
            }

            HashMap p = new HashMap();
            for(int i = 0; i < users.length; i++) {
                p.put(users[i].getUsername(), createPerson(users[i]));
            }
            rslt = (IPerson[])p.values().toArray(new IPerson[0]);
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getMembers(deep) for group. " + g.getName(), e);
        }

        return rslt;
    }
    
    protected String getPath(IGroup g) {
        try {
        
            return AcademusFacadeContainer.retrieveFacade().getGroup(g.getId())
            .getGroupPaths(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, false)[0];            
                
        } catch (AcademusFacadeException e) {
            throw new CivisRuntimeException("Error in AcademusCivisFactory : " +
            		"getPath for group. " + g.getName(), e);
        }

    }

    /*
     * Private methods.
     */

    private AcademusCivisFactory(ExtendedAttribute[] personExtraAttrs) {
        this.personExtraAttrs = personExtraAttrs;
        this.personAttr = createPersonAttributes(personExtraAttrs);
    }

    private IPerson createPerson(IAcademusUser user) throws AcademusFacadeException {
        IPerson rslt = null;
        IEntityStore store = personAttr.getOwner(); 
        IChoice c = null;
        IOption o = null;
        ISelection s = null;
        IDecision d = null;
        List decisions = new ArrayList();

        try {
            // First Name
            c = personAttr.getChoice("fName");
            o = c.getOptions()[0];
            s = store.createSelection(o
                    , o.getComplementType().parse(user.getFirstName()));
            d = store.createDecision(Label.create("First Name")
                    , c, new ISelection[] {s});
            decisions.add(d);

            // Last Name
            c = personAttr.getChoice("lName");
            o = c.getOptions()[0];
            s = store.createSelection(o
                    , o.getComplementType().parse(user.getLastName()));
            d = store.createDecision(Label.create("Last Name")
                    , c, new ISelection[] {s});
            decisions.add(d);

            // Extended attributes
            for (int i = 0; i < personExtraAttrs.length; i++) {
                String attr = personExtraAttrs[i].getHandle();
                String attrVal = user.getAttribute(attr);

                // Handle the null case...
                if (attrVal == null) {
                    attrVal = "";
                    // TODO: Log this instead
                    System.err.println(
                              "AcademusCivisFactory::createPerson(): "
                            + "Null return for user attribute '"+attr+"'");
                }

                c = personAttr.getChoice(attr);
                o = c.getOptions()[0];
                s = store.createSelection(o,
                        o.getComplementType().parse(attrVal));
                d = store.createDecision(c.getLabel(), c, new ISelection[] {s});
                decisions.add(d);
            }

            IDecisionCollection dc = 
                store.createDecisionCollection(personAttr
                        , (IDecision[])decisions.toArray(new IDecision[0]));
            rslt = new AbstractCivisFactory.PersonImpl(Long.toString(user.getId())
                    , this, user.getUsername(), dc);
        } catch (EntityCreateException e) {
            throw new CivisRuntimeException("Error creating IPerson for " +
                    "the user " + user.getUsername(), e);
        }

        return rslt;
    }

    private IGroup createGroup(IAcademusGroup group) throws AcademusFacadeException {
        IGroup rslt = null;
        IEntityStore store = groupAttr.getOwner(); 
        IChoice c = null;
        IOption o = null;
        ISelection s = null;

        try {
            // Group key.
            c = groupAttr.getChoice("key");
            o = c.getOptions()[0];
            s = store.createSelection(o
                    , o.getComplementType().parse(group.getKey()));
            IDecision dKey = store.createDecision(Label.create("Key")
                    , c , new ISelection[] {s});

            IDecisionCollection dc = 
                store.createDecisionCollection(groupAttr
                        , new IDecision[] {dKey});
            rslt = new AbstractCivisFactory.GroupImpl(group.getKey()
                    , this, group.getName(), dc, group.getGroupPaths(null, false)[0]);

        } catch (EntityCreateException e) {
            throw new CivisRuntimeException("Error creating IGroup for " +
                    "the group " + group.getKey(), e);
        }

        return rslt;
    }

    private static IChoiceCollection createPersonAttributes(ExtendedAttribute[] attrs) {
        IChoiceCollection rslt = null;
        IEntityStore store = new JvmEntityStore();
        IOption o = null;
        IChoice c = null;
        List choices = new ArrayList();
        
        try {
            // First Name
            o = store.createOption(Handle.create("fName")
                    , Label.create("First Name")
                    , TypeText64.INSTANCE);
            
            c = store.createChoice(Handle.create("fName")
                    , Label.create("First Name")
                    , new IOption[] {o}
                    , 1
                    , 1);
            choices.add(c);
            
            // Last Name
            o = store.createOption(Handle.create("lName")
                    , Label.create("Last Name")
                    , TypeText64.INSTANCE);
            
            c = store.createChoice(Handle.create("lName")
                    , Label.create("Last Name")
                    , new IOption[] {o}
                    , 1
                    , 1);
            choices.add(c);

            // Extended attributes
            for (int i = 0; i < attrs.length; i++) {
                o = store.createOption(Handle.create(attrs[i].getHandle()),
                                       Label.create(attrs[i].getLabel()),
                                       TypeText64.INSTANCE); /* TODO: Will 64 always be enough? */
                c = store.createChoice(Handle.create(attrs[i].getHandle()),
                                       Label.create(attrs[i].getLabel()),
                                       new IOption[] {o}, 1, 1);
                choices.add(c);
            }

            rslt = store.createChoiceCollection(Handle.create("Person Information")
                    , Label.create("Person Information")
                    , (IChoice[])choices.toArray(new IChoice[0])); 
                
        } catch (EntityCreateException e) {
            throw new CivisRuntimeException("Error in creating a choice collection for person.", e);
        }
            
        return rslt;
    }

    private static IChoiceCollection createGroupAttributes() {
        IChoiceCollection rslt = null;
        IEntityStore store = new JvmEntityStore();
        
        try {
            IOption oKey = store.createOption(Handle.create("key")
                    , Label.create("Key")
                    , TypeText64.INSTANCE);
            
            IChoice cKey = store.createChoice(Handle.create("key")
                    , Label.create("Key")
                    , new IOption[] {oKey}
                    , 1
                    , 1);
            
            rslt = store.createChoiceCollection(Handle.create("Group Information")
                    , Label.create("Group Information")
                    , new IChoice[] {cKey});
        } catch (EntityCreateException e) {
            throw new CivisRuntimeException("Error in creating a choice collection for group.", e);
        }
        return rslt;
    }

    private static class ExtendedAttribute {
        private String handle;
        private String label;

        public ExtendedAttribute(String handle, String label) {
            if (handle == null || handle.trim().equals(""))
                throw new IllegalArgumentException(
                        "Argument 'handle' cannot be null or empty.");
            if (label == null || label.trim().equals(""))
                throw new IllegalArgumentException(
                        "Argument 'label' cannot be null or empty.");

            this.handle = handle;
            this.label = label;
        }

        public String getHandle() { return this.handle; }
        public String getLabel() { return this.label; }

        public static ExtendedAttribute parse(Element e) {
            Attribute t = e.attribute("handle");
            return new ExtendedAttribute(t.getValue(), e.getText());
        }
    }    
}
