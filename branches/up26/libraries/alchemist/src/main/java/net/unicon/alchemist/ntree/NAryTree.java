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

package net.unicon.alchemist.ntree;

import java.util.Map;
import java.util.HashMap;

public class NAryTree {
    
    private Map children;
    private String id;
    private NAryTree parent;
    
    public NAryTree(String id){
        this.children = new HashMap();
        this.id = id;
        this.parent = null;
    }
    
    public String getId(){
        return this.id;
    }
    
    public void addChild(NAryTree tree){
        this.children.put(tree.getId(), tree);
        tree.setParent(this);
    }
    
    public NAryTree[] getChildren(){
        return (NAryTree[])this.children.values().toArray(new NAryTree[0]);
    }
    
    public boolean isLeaf(){
        return this.children.isEmpty();
    }
    
    public void setParent(NAryTree parent){
        this.parent = parent;
    }
    
    public NAryTree getParent(){
        return this.parent;
    }
    
    public boolean hasChild(NAryTree tree){
        return this.children.containsKey(tree.getId());
    }
    
    public NAryTree getChild(String id){
        return (NAryTree)this.children.get(id);
    }

}
