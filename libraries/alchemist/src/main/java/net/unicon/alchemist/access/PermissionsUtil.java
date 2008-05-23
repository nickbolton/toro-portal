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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.ntree.NAryTree;

public class PermissionsUtil {
    public static final String GROUP_PATH_SEPARATOR = " - ";
    
    public static AccessRule[] coalesce(IAccessEntry[] entries
            , AccessType[] access){
        
        // assertions
        if(entries == null || entries.length == 0){
            throw new IllegalArgumentException("Argument 'entries' can not be null.");
        }
        if(access == null || access.length == 0){
            throw new IllegalArgumentException("Argument 'access' can not be null or empty.");
        }
        
        // set up the access rule with default deny permissions
        /*Map rules = new HashMap();
        for(int i = 0; i < access.length; i++){
            rules.put(access[i], new AccessRule(access[i], false));
        }
        */
        Map civisAccessEntries = new HashMap();
        NAryTree root = null;
        // convert all the identities to CivisEntities
        List persons = new ArrayList();
        Map rules = new HashMap();
        
        String entityId;
        
        for(int i = 0; i < entries.length; i++){
            entityId = entries[i].getIdentity().getId();
            if (entries[i].getIdentity().getType().equals(IdentityType.GROUP)){
                root = addGroupToTree(entityId, root);
            }else{
                persons.add(entityId);
            }  
            civisAccessEntries.put(entityId, toMap(entries[i].getAccessRules()));
        }
        
        rules = flattenTree(root, civisAccessEntries);
        
        // merge with the person entity if it exists
        if(!persons.isEmpty()){
            Map pRules;
            for(int i = 0; i < persons.size(); i++){
                pRules = (Map)civisAccessEntries.get(((String)persons.get(i)));
                Iterator it = pRules.keySet().iterator();
                AccessType type;
                while(it.hasNext()){
                    type = (AccessType)it.next();
                    rules.put(type, (AccessRule)pRules.get(type));                
                }
            }
        }
        
        AccessRule[] accessRules = new AccessRule[access.length];
        
        AccessRule rule;
        for(int i = 0; i < access.length; i++){
            rule = (AccessRule)rules.get(access[i]);
            if(rule == null){
                rule = new AccessRule(access[i], false);
            }
            accessRules[i] = rule;
        }
        
        return accessRules;
    }  
    
    private static NAryTree addGroupToTree(String id, NAryTree root){
        
        String[] gPath = id.split(GROUP_PATH_SEPARATOR);
        int startIndex = 1;
        
        // no tree yet
        if(root == null){
            root = new NAryTree(gPath[0]);            
        }
        
        StringBuffer treeId = new StringBuffer(gPath[0]);
        NAryTree parent = root;
        NAryTree child = null;
        for(int i = startIndex; i < gPath.length; i++){
            treeId.append(GROUP_PATH_SEPARATOR).append(gPath[i]);
            child = new NAryTree(treeId.toString());
            if(!parent.hasChild(child)){
                parent.addChild(child);
            }else{
                child = parent.getChild(treeId.toString());
            }
            parent = child;
        }
        return root;
    }
    
    private static Map flattenTree(NAryTree tree, Map rules){
        
        Map rslt = new HashMap();
        if(tree != null){
	        if(tree.isLeaf()){
	            rslt =  (Map)rules.get(tree.getId());
	        }else{
	            NAryTree[] children = tree.getChildren();
	            // merge rules with children's permissions
	            Map s2Rules;
	            Map mergedRules = flattenTree(children[0], rules);
	            for(int i = 1; i < children.length; i++){
	                s2Rules = flattenTree(children[i], rules);
	                mergedRules = mergeRules(mergedRules, s2Rules);
	            }
	            
	            // parent rules get overridden by the childrens merged rules
	            Map myRules = (Map)rules.get(tree.getId());
	            if(myRules == null){
	                myRules = new HashMap();
	            }
	            Iterator it = mergedRules.keySet().iterator();
	            AccessType type;
	            while(it.hasNext()){
	                type = (AccessType)it.next();
	                myRules.put(type, (AccessRule)mergedRules.get(type));                
	            }
	            rules.put(tree.getId(), myRules);
	            rslt = myRules;
	        }
        }
        return rslt;
    }
    
    private static Map mergeRules(Map s1Rules, Map s2Rules){
        
        // sibling grant rules will override
        // go through the second map and override 
        // with grant permissions if they exist
        Iterator it = s2Rules.keySet().iterator();
        AccessRule rule;
        AccessType type;
        while(it.hasNext()){
            type = (AccessType)it.next();
            rule = (AccessRule)s2Rules.get(type);
            if(rule.getStatus()){
                s1Rules.put(type, rule);
            }
        }
        return s1Rules;
    }
    
    private static Map toMap(AccessRule[] rules){
        Map rslt = new HashMap();
        
        for(int i = 0; i < rules.length; i++){
            rslt.put(rules[i].getAccessType(), rules[i]);
        }
        return rslt;
    }
    
}
