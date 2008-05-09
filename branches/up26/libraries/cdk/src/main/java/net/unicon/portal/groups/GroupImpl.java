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
package net.unicon.portal.groups;


import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.DocumentFactory;

import org.jasig.portal.channels.groupsmanager.GroupsManagerConstants;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IUpdatingPermissionManager;

import net.unicon.sdk.properties.UniconPropertiesFactory;
import net.unicon.portal.common.properties.*;
import net.unicon.sdk.FactoryCreateException;
import net.unicon.sdk.util.XmlUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import java.lang.reflect.Method;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This Implementation of IGroup is a wrapper around the uPortal
 * <code>IEntityGroup</code> object.
 */
public class GroupImpl implements IGroup, GroupsManagerConstants {

    protected String key;
    protected String type;
    protected long id;
    private static String localGroupService = null;
    private static String[] availableGroupServices = null;
    private boolean ok = false;

    private String preferredPath = "No preferred path set";

    static {
        localGroupService = UniconPropertiesFactory.getManager(
            PortalPropertiesType.PORTAL).getProperty(
            "net.unicon.portal.groups.GroupImpl.localGroupService");

        availableGroupServices = UniconPropertiesFactory.getManager(
            PortalPropertiesType.PORTAL).getProperty(
            "net.unicon.portal.groups.GroupImpl.availableGroupServices").
               split(",");
    }

    GroupImpl(Long id) {
        this(id.longValue());
    }

    // this constructor assumes a local group service
    GroupImpl(long id) {
        LogService.instance().log(LogService.DEBUG,
            "GroupImpl::GroupImpl() instantiating IGroup object with id: "
            + id);
        this.key = localGroupService+id;
        this.id = id;
        ok = true;
    }

    GroupImpl(String key) {
        LogService.instance().log(LogService.DEBUG,
            "GroupImpl::GroupImpl() instantiating IGroup object with key:"            + key);
        try {
            this.key = key;
            this.id = Long.parseLong(parseKey(key));
            ok = true;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    GroupImpl(IEntityGroup entityGroup) {
        try {
            this.key = entityGroup.getKey();
            this.id = Long.parseLong(parseKey(this.key));
            ok = true;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    /**
     * Retrieves an unique identifier for this group object.
     * @return The unique group identifier.
     */
    public long getGroupId() {
        return id;
    }

    /**
     * Retrieves an unique identifier for this group object.
     * @return The unique group identifier.
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Retrieves the group type. The group type represents some information
     * about the group.
     * @return A unique group type
     */
    public String getType() {
        try {
            // ZZZ - Kludge-e-e-e-e-e-e-e
            return EntityTypes.getEntityTypeID(getEntityGroup().getType()).toString();
        } catch (UniconGroupsException uge) {
            LogService.log(LogService.ERROR, uge);
        }
        return "";
    }
    
    /**
     * Retrieves this group's parent groups.
     * @return List of parent groups.
     */
    public List getParents() throws UniconGroupsException {
        try {
            return fetchParents(getEntityGroup());
        } catch (FactoryCreateException fce) {
            throw new UniconGroupsException(fce);
        }
    }

    /**
     * Retrieves this group's parent keys.
     * @return List of parent keys.
     */
    public List getParentKeys() throws UniconGroupsException {
        try {
            return fetchParentKeys(getEntityGroup());
        } catch (FactoryCreateException fce) {
            throw new UniconGroupsException(fce);
        }
    }

    public boolean ok() {
        return ok;
    }

    /**
     * Retrieves this group's paths as an array of strings. The paths represent
     * the hierarchical base paths of this IGroup instance. The paths
     * consist of group names that are separated by delim Strings.
     * If the delim parameter is null, it will default to the
     * IGroup.GROUP_NAME_BASE_PATH_SEPARATOR separator.
     * @param delim A delimiter that separates the path nodes.
     * @param excludeThisNode If set, this will exclude this node from the
     * results.
     * @return The group paths as a String array.
     */
    public String[] getPathsAsStrings(String delim, boolean excludeThisNode)
    throws UniconGroupsException {

        IGroup group = null;

        // Get all paths leading to this group
        List paths = getPaths(excludeThisNode);

        int pathNum = paths.size();
        String[] pathStrings = new String[pathNum];        

        if (delim == null) {
            delim = IGroup.GROUP_NAME_BASE_PATH_SEPARATOR;
        }

        StringBuffer sb = null;
        List currentPath = null;

        Iterator pathsItr = paths.iterator();

        for (int index = 0; index < pathNum; index++) {
                    
            sb = new StringBuffer();

            // Get a single path
            currentPath = (List) paths.get(index);            

            Iterator pathItr = currentPath.iterator();

            //Construct string for this path
            while (pathItr.hasNext()) {

                group = (IGroup) pathItr.next();

                if (sb.length()>0) {
                    sb.append(delim);
                }

                sb.append(group.getName());
            }

            pathStrings[index] = sb.toString();
        }

        return pathStrings;
    }

    private List getPaths (boolean excludeThisNode)
    throws UniconGroupsException {

        List paths = new ArrayList();
        List visitedGroups = new ArrayList();

        List currentList = null;

        // Get all paths leading to this group
        getGroupPaths(this, visitedGroups, paths);

        Iterator itr = paths.iterator();

        while (itr.hasNext()) {

            // Reverse all paths so that parent groups appear first
            currentList = (List) itr.next();
            Collections.reverse(currentList);

            // Determine if this group will be included in the path
            if (!excludeThisNode)
                currentList.add(this);
        }
        
        Collections.sort(paths, new Comparator() {

            public int compare(Object arg0, Object arg1) {
                
                if(((GroupImpl)((List)arg0).get(0)).getName().equals("Everyone")
                        && !((GroupImpl)((List)arg1).get(0)).getName().equals("Everyone")){
                    return -1;
                }else if(!((GroupImpl)((List)arg0).get(0)).getName().equals("Everyone")
                    && ((GroupImpl)((List)arg1).get(0)).getName().equals("Everyone")){
                    return 1;
                }
                
                return 0;
            }
        
        });

        return paths;
    }

    private void getGroupPaths (IGroup group, List visitedGroups, List paths)
    throws UniconGroupsException {

        // Get this group's parents
        List parents = group.getParents();
       
        IGroup parent = null;

        // Determine if we have reached the end of a path
        if (parents == null) {
            
            // Add this path to the list of paths
            List newPath = new ArrayList();
            newPath.addAll(visitedGroups);            
            paths.add(newPath);                        

        } else {
        
            Iterator itr = parents.iterator();

            // Traverse paths for each parent of the current group
            while (itr.hasNext()) {

                parent = (IGroup) itr.next();

                // Detect any circular references to avoid infinite loops
                if (!visitedGroups.contains(parent)) {
                    // Add the specific parent group from the list of visited nodes
                    visitedGroups.add(parent);                                    
                    //Recursion
                    getGroupPaths(parent, visitedGroups, paths);
                    // Remove the specific parent group from the list of visited nodes
                    visitedGroups.remove(parent);                        
                 } else {
                    // Circular reference in group structure
                    StringBuffer sb = new StringBuffer();
                    sb.append("GroupImpl:getGroupPaths():");
                    sb.append("Circular reference detected while constructing group path.\n");
                    sb.append("Group " + parent.getName() + " ");
                    sb.append("already exists in path:");

                    Iterator itr2 = visitedGroups.iterator();
                    IGroup tmpGroup = null;

                    while (itr2.hasNext()) {
                       
                        tmpGroup = (IGroup) itr2.next();
                        sb.append(tmpGroup.getName());
                        if (itr2.hasNext())
                            sb.append("->"); 
                    }

                    throw new UniconGroupsException(sb.toString());
                }
            }
        }
    }

    /**
     * Retrieves this group's direct descendent groups.. This will pass the
     * burden of retrieval to the underlying <code>IEntityGroup</code> object.
     * @return <code>List<code> of <code>IGroup</code> objects.
     * @throws UniconGroupsException if the underlying member retrieval fails or
     * any of the children cannot be instantiated.
     */
    public List getDescendantGroups() throws UniconGroupsException {
        return getDescendantGroups(this, false);
    }

    /**
     * Retrieves this group's direct descendent members. This will pass the
     * burden of retrieval to the underlying <code>IEntityGroup</code> object.
     * @return <code>List<code> on <code>IMember</code> objects.
     * @throws UniconGroupsException if the underlying member retrieval fails or
     * any of the children cannot be instantiated.
     */
    public List getDescendantMembers() throws UniconGroupsException {
        
        List retList = new ArrayList();

        try {
            IMember member = null;
            IGroupMember groupMember = null;
            Iterator itr = getEntityGroup().getMembers();
            while (itr.hasNext()) {
                groupMember = (IGroupMember)itr.next();
                if (!groupMember.isGroup()) {
                    member = MemberFactory.getMember(groupMember);
                    retList.add(member);
                }
            }
        } catch (FactoryCreateException fce) {
            LogService.instance().log(LogService.ERROR, fce);
            throw new UniconGroupsException(fce);
        } catch (GroupsException ge) {
            LogService.instance().log(LogService.ERROR, ge);
            throw new UniconGroupsException(ge);
        }
        return retList;
    }

    /**
     * Retrieves all of this group's recursive descendent groups.
     * This will pass the burden of retrieval to the underlying
     * <code>IEntityGroup</code> object.
     * @return <code>List<code>
     * @throws UniconGroupsException if the underlying member retrieval fails or
     * any of the children cannot be instantiated.
     */
    public List getAllDescendantGroups() throws UniconGroupsException {
        return getDescendantGroups(this, true);
    }

    /**
     * Builds a <code>Document</code> the represents the group structure
     * from this group on down.
     * <code>Document</code>.
     * @param recurse A true value will build the document recursively. A false
     * value only returns this and the direct descendants.
     * @return <code>Document</code>
     * @throws UniconGroupsException
     */
    public Document buildDocument(boolean recurse)
    throws UniconGroupsException {
        try {
            Document doc = DocumentFactory.getNewDocument();
            Node rootNode = addGroupNode(doc, doc, this);
            buildDescendants(doc, rootNode, this, recurse);
            return doc;
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }
    }

    private void buildDescendants(Document doc, Node parentNode,
        IGroup parentGroup, boolean recurse)
    throws UniconGroupsException {
        try {
            IGroup group = null;
            Node node = null;
            Iterator itr = getDescendantGroups(parentGroup, false).iterator();
            while (itr.hasNext()) {
                group = (IGroup)itr.next();
                node = addGroupNode(doc, parentNode, group);
                if (recurse) {
                    buildDescendants(doc, node, group, true);
                }
            }
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }
    }

    // This adds the group to the given node in the given doc.
    private Node addGroupNode(Document doc, Node node, IGroup group)
    throws Exception {
        Map attributes = new HashMap();
        attributes.put("id", ""+group.getGroupId());
        Node newNode = XmlUtils.addNewNode(
            doc, node, "group", attributes, null);

       attributes.clear();
       XmlUtils.addNewNode(doc, newNode, "name", attributes, group.getName());
       return newNode;
    }

    private List getDescendantGroups(IGroup parentGroup, boolean recurse)
    throws UniconGroupsException {

        List retList = new ArrayList();

        try {
            IGroup group = null;
            IGroupMember member = null;
            Iterator itr = parentGroup.getEntityGroup().getMembers();
            while (itr.hasNext()) {
                member = (IGroupMember)itr.next();
                if (member.isGroup()) {
                    group = GroupFactory.getGroup(member);
                    retList.add(group);
                    if (recurse) {
                        retList.addAll(getDescendantGroups(group, true));
                    }
                }
            }
        } catch (FactoryCreateException fce) {
            LogService.instance().log(LogService.ERROR, fce);
            throw new UniconGroupsException(fce);
        } catch (GroupsException ge) {
            LogService.instance().log(LogService.ERROR, ge);
            throw new UniconGroupsException(ge);
        }
        return retList;
    }

    private String parseKey(String key)
    throws UniconGroupsException {
        String id = "";

        for (int i=0; i<availableGroupServices.length; i++) {
            if (key.indexOf(availableGroupServices[i]) == 0) {
                return key.substring(availableGroupServices[i].length());
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("GroupImpl::retrieveUserEntity() invalid ");
        sb.append("group service type: " + key);
        throw new UniconGroupsException(sb.toString());
    }

    /**
     * Retrieves the name for this group.
     * @return The name for this group.
     */
    public String getName() {
        try {
            return getEntityGroup().getName();
        } catch (UniconGroupsException uge) {
            LogService.instance().log(LogService.ERROR, uge);
        }
        return "";
    }

    /**
     * Sets the name for this group.
     * @param name The new name.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void setName(String name)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::setName() : group: (" + getKey() + ", " +
                getName() + ") setting name: " + name);

            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.setName(name);
            update(entityGroup);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Retrieves the creator of the group.
     * @return The creator
     */
    public String getCreatorId() {
        try {
            return getEntityGroup().getCreatorID();
        } catch (UniconGroupsException uge) {
            LogService.instance().log(LogService.ERROR, uge);
        }
        return "";
    }

    /**
     * Sets the creator of the group
     * @param creatorId An identifier for the creator of the group
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void setCreatorId(String creatorId)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::setCreatorId() : group: (" + getKey() + ", " +
                getName() + ") setting creatorId: " + creatorId);

            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.setCreatorID(creatorId);
            update(entityGroup);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Retrieves the description for this group.
     * @return The description for this group.
     */
    public String getDescription() {
        try {
            return getEntityGroup().getDescription();
        } catch (UniconGroupsException uge) {
            LogService.instance().log(LogService.ERROR, uge);
        }
        return "";
    }

    /**
     * Sets the description for this group.
     * @param description The new description for this group.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void setDescription(String desc)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::setDescription() : group: (" + getKey() + ", " +
                getName() + ") setting desc: " + desc);

            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.setDescription(desc);
            update(entityGroup);
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Sets the preferred group path for this group.
     * @param path The preferred group path for this group.
     */
    public void setPreferredPath(String path) {

        preferredPath = path;
    }

    /**
     * Retrieves the preferred group path for this group.
     * @return The preferred group path for this group.
     */
    public String getPreferredPath() {

        return preferredPath;
    }

    /**
     * Adds a user member to this group.
     * @param member The member to add.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void addMember(IMember member)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::addMember() : group: (" + getKey() + ", " +
                getName() + ") adding member: " + member.getKey());

            IEntity entity = retrieveUserEntity(member.getKey());
            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.addMember(entity);
            entityGroup.updateMembers();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Remove a user from this group.
     * @param member The member to remove.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void removeMember(IMember member)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::removeMember() : group: (" + getKey() + ", " +
                getName() + ") removing member: " + member.getKey());

            IEntity entity = retrieveUserEntity(member.getKey());
            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.removeMember(entity);
            entityGroup.updateMembers();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Add a descendent group to this group
     * @param newDescendent
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void addGroup(IGroup group)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::addGroup() : group: (" + getKey() + ", " +
                getName() + ") adding group: " +
                group.getKey() + " - " + group.getName());
            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.addMember(group.getEntityGroup());
            entityGroup.updateMembers();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Remove a descendent group from this group
     * @param descendent The group to remove.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void removeGroup(IGroup group)
    throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::removeGroup() : group: (" + getKey() + ", " +
                getName() + ") removing group: " +
                group.getKey() + " - " + group.getName());
            IEntityGroup entityGroup = getEntityGroup();
            entityGroup.removeMember(group.getEntityGroup());
            entityGroup.updateMembers();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Store the group object in persistent storage.
     * @throws UniconGroupsException if the underlying group cannot be updated.
     */
    public void update() throws UniconGroupsException {
        update(getEntityGroup());
    }

    private void update(IEntityGroup entityGroup) throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::update() : updating group: " +
                getKey() + " - " + getName());
            entityGroup.update();
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Delete this group object from persistent storage.
     * @throws UniconGroupsException if the underlying group cannot be deleted.
     */
    public void delete() throws UniconGroupsException {
        try {
            LogService.instance().log(LogService.DEBUG,
                "GroupImpl::delete() : deleting group: " +
                getKey() + " - " + getName());

            // remove this group from any of it's current parents
            IEntityGroup parent = null;
            IEntityGroup entityGroup = getEntityGroup();
            Iterator itr = entityGroup.getContainingGroups();
            while (itr.hasNext()) {
                parent = (IEntityGroup)itr.next();

                LogService.instance().log(LogService.DEBUG,
                    "GroupImpl::delete() : removing parent association: " +
                    parent.getName());

                parent.removeMember(entityGroup);
                parent.update();
            }
            deletePermissions((IGroupMember)entityGroup);
            entityGroup.delete();
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Retrieves the associated <code>IEntityGroup</code> object.
     * @return The entity group object.
     */
    public IEntityGroup getEntityGroup() throws UniconGroupsException {
        try {
            IEntityGroup entityGroup =
                GroupService.findGroup(key);
            if (entityGroup == null) {
                StringBuffer sb = new StringBuffer();
                sb.append("GroupImpl::getEntityGroup() : ");
                sb.append("GroupService lookup ");
                sb.append("failed for group: " + id);
                throw new Exception(sb.toString());
            }
            return entityGroup;
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }
    }

    /**
     * Checks if the member identifies by memberKey is a member of this group.
     * @param member The member to check for membership.
     * @return boolean
     * @throws UniconGroupsException
     */
    public boolean contains(IMember member) throws UniconGroupsException {
        try {
            IGroupMember groupMember = AuthorizationService.instance().
                getGroupMember(AuthorizationService.instance().newPrincipal(
                    member.getKey(), org.jasig.portal.security.IPerson.class));
            return getEntityGroup().contains(groupMember);
        } catch (Exception e) {
            throw new UniconGroupsException(e.getMessage(), e);
        }
    }

    public IGroup getMemberGroup(String groupName) throws UniconGroupsException {
        if (groupName == null) return null;

        IGroup group;
        Iterator itr = getDescendantGroups(this, false).iterator();
        while (itr.hasNext()) {
            group = (IGroup)itr.next();
            if (groupName.equals(group.getName())) {
                return group;
            }   
        }   
        return null;
    }

    public boolean contains(String memberName) throws UniconGroupsException {
        if (memberName == null) return false;

        IGroup group;
        Iterator itr = getDescendantGroups(this, false).iterator();
        while (itr.hasNext()) {
            group = (IGroup)itr.next();
            if (memberName.equals(group.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a <code>String</code> representation of the XML that 
     * describes this group object.
     * @return The XML representing this group object.
     */
    public String toXML() {
        StringBuffer xml = new StringBuffer();
        xml.append("<group");
        xml.append(" key=\"");
        xml.append("" + key);
        xml.append("\">");
        xml.append("<name>");
        xml.append(getName());
        xml.append("</name>");
        xml.append("<description>");
        xml.append(getDescription());
        xml.append("</description>");
        xml.append("</group>");
        return xml.toString();
    }

    private List fetchParentKeys(IEntityGroup entityGroup)
    throws UniconGroupsException, FactoryCreateException {

        Iterator itr = null;

        try {
            itr = entityGroup.getContainingGroups();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }

        if (!itr.hasNext()) {
            return null;
        }

        List parentKeys = new ArrayList();

        while (itr.hasNext()) {
            IEntityGroup parentGroup = (IEntityGroup)itr.next();
            parentKeys.add(parentGroup.getKey());
        }

        return parentKeys;
    }

    private List fetchParents(IEntityGroup entityGroup)
    throws UniconGroupsException, FactoryCreateException {

        Iterator itr = null;

        try {
            itr = entityGroup.getContainingGroups();
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
            throw new UniconGroupsException(e);
        }

        if (!itr.hasNext()) {
            return null;
        }

        List parentList = new ArrayList();

        while (itr.hasNext()) {
            IEntityGroup parentGroup = (IEntityGroup)itr.next();
            parentList.add(GroupFactory.getGroup(parentGroup.getKey()));
        }

        return parentList;
    }

    protected IEntity retrieveUserEntity (String key)
    throws UniconGroupsException {
        IEntity ent = null;
        try {
            ent = GroupService.getEntity(key,
                org.jasig.portal.security.IPerson.class);
        } catch (Exception e) {
            throw new UniconGroupsException(e);
        }

        if (ent == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("GroupImpl::retrieveUserEntity() failed to retrieve ");
            sb.append("user entity with key: " + key);
            throw new UniconGroupsException(sb.toString());
        }
        return  ent;
    }

    protected void deletePermissions(IGroupMember member)
    throws Exception {

        String groupKey = member.getKey();
        /*
            first we retrieve all permissions for
            which the group is the principal
        */
        IAuthorizationPrincipal iap =
            AuthorizationService.instance().newPrincipal(member);

        IPermission[] perms1 = iap.getPermissions();

        /*
            next we retrieve all permissions for which the
            group is the target.

            NOTE: OWNER constant comes from GroupsManagerConstants interface.
        */
        IUpdatingPermissionManager upm =
            AuthorizationService.instance().
                newUpdatingPermissionManager(OWNER);

        IPermission[] perms2 = upm.getPermissions(null, groupKey);

        // merge the permissions
        IPermission[] allPerms =
            new IPermission[perms1.length + perms2.length];

        System.arraycopy(perms1,0,allPerms,0,perms1.length);
        System.arraycopy(perms2,0,allPerms,perms1.length,perms2.length);

        upm.removePermissions(allPerms);

    }

} // end GroupImpl class
