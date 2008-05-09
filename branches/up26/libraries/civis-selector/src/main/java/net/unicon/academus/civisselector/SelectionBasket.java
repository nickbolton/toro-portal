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

package net.unicon.academus.civisselector;

import java.util.HashMap;
import java.util.Map;

public class SelectionBasket {
    
    // instance members
    private Map entitySel;
    private int memberCount;
    private int groupCount;
    private int newMemberCount;
    private int newGroupCount;
    
    public SelectionBasket(){
        this.entitySel = new HashMap();
        this.memberCount = 0;
        this.groupCount = 0;
        this.newGroupCount = 0;
        this.newMemberCount = 0;
    }
    
    /**
     * Clears up the selection basket and resets the member and group count
     *
     */
    public void clearEntitySelection() {
        this.entitySel.clear();
        this.memberCount = 0;
        this.groupCount = 0;
        this.newGroupCount = 0;
        this.newMemberCount = 0;
    }
    
    /**
     * Gets the members and groups in the selection basket
     * @return an array of <code>IEntity</code>
     */
    public IEntity[] getEntities(){
        IEntity[] rslt = new IEntity[0];
        return (IEntity[])(entitySel.values().toArray(rslt));
    }
    
    public Map getEntityMap(){
        return entitySel;
    }
    
    /**
     * Sets the selection basket to the array of <code>IEntity</code>
     * This method clears out the selection basket of all previous selections. 
     * @param selection array of <code>IEntity</code>
     */
    public void setEntitySelection(IEntity[] selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }       

        clearEntitySelection();
        
        for(int i = 0; i < selection.length; i++){
            entitySel.put(selection[i].getEntityId(), selection[i]);
            if(selection[i].getType().equals(EntityType.MEMBER)){
                memberCount++;
            }else{
                groupCount++;
            }
        }
    }
    
    /**
     * Adds entities to the selection basket.
     * This method will append the selection list to the basket.
     * @param selection array of <code>IEntity</code>
     */
    public void addEntitySelection(IEntity[] selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
         
        for(int i = 0; i < selection.length; i++){
            if(!entitySel.containsKey(selection[i].getEntityId())){
	            entitySel.put(selection[i].getEntityId(), selection[i]);
	            if(selection[i].getType().equals(EntityType.MEMBER)){
	                memberCount++;
	            }else{
	                groupCount++;
	            }
            }
        }      
    }
    
    /**
     * Removes the given entity from the basket.
     * @param selection <code>IEntity</code> object
     */
    public void removeSelection(IEntity selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        entitySel.remove(selection.getEntityId());
        if(selection.getType().equals(EntityType.MEMBER)){          
            memberCount--;
        }else{
            groupCount--;
        }
        
    }
    
    /**
     * Gets the member count in the basket
     * @return number of members in the basket
     */
    public int getMemberCount(){
        return this.memberCount;
    }
    
    /**
     * Gets the number of groups in the basket
     * @return number of groups in the basket
     */
    public int getGroupCount(){
        return this.groupCount;
    }
    
    public int getNewMemberCount(){
    	return this.newMemberCount;
    }
    
    public int getNewGroupCount(){
    	return this.newGroupCount;
    }
    
    /**
     * Checks to see if the given entity is in the selection basket.
     * @param entityId String value representing the id of the <code>IGroup</code>
     * or <code>IPerson</code>
     * @return	boolean to represent if the given entity is in the basket.
     */
    public boolean contains(String entityId){
        return entitySel.containsKey(entityId);
    }
    
    public int size(){
        return this.entitySel.size();
    }
    
    public void addEntitySelection(SelectionBasket b) {

        // Assertions.
        if (b == null) {
            String msg = "Argument 'b [SelectionBasket]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
            
        entitySel.putAll(b.getEntityMap());
        memberCount += b.getMemberCount();
        groupCount += b.getGroupCount();
        this.newGroupCount += b.getGroupCount();
        this.newMemberCount += b.getMemberCount();
    }    
   
}
