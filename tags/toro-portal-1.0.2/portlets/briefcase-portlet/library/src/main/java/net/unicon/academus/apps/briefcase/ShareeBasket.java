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

package net.unicon.academus.apps.briefcase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.unicon.alchemist.access.AccessRule;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.IPerson;
import net.unicon.civis.fac.AbstractCivisFactory;

public class ShareeBasket {
    
    // instance members
    // map with key - shareeId and value CivisAccessEntry.
    private Map shareeSel;    
    private int memberCount;
    private int groupCount;
    private int newMemberCount;
    private int newGroupCount;
    
    public ShareeBasket(){
        this.shareeSel = new HashMap();
        this.memberCount = 0;
        this.groupCount = 0;
        this.newGroupCount = 0;
        this.newMemberCount = 0;
    }
    
    /**
     * Clears up the selection basket and resets the member and group count
     *
     */
    public void clearShareeSelection() {
        this.shareeSel.clear();
        this.memberCount = 0;
        this.groupCount = 0;
        this.newGroupCount = 0;
        this.newMemberCount = 0;
    }
    
    /**
     * Gets the members and groups in the selection basket
     * @return an array of <code>ICivisEntity</code>
     */
    public ICivisEntity[] getSharees(){
        Iterator it = this.shareeSel.keySet().iterator();
        ICivisEntity[] rslt = new ICivisEntity[this.shareeSel.size()];
        int index= 0;
        
        while (it.hasNext()){
            String entityId = (String)it.next();
          rslt[index++] =  AbstractCivisFactory.entityFromUrl(entityId); 
        }
        
        return rslt;
    }
    
    /**
     * Gets the members and groups in the selection basket
     * that were added recently
     * @return an array of <code>ICivisEntity</code>
     */
    public ICivisEntity[] getNewSharees(){
        List rslt = new LinkedList();
        Iterator it = shareeSel.values().iterator();
        int i = 0;
        CivisAccessEntry sharee = null;
        while(it.hasNext()){
            sharee = (CivisAccessEntry)it.next();
            if(sharee.isNew()){
                rslt.add(AbstractCivisFactory.entityFromUrl(sharee.getEntityId()));
            }
        }
        return (ICivisEntity[])rslt.toArray(new ICivisEntity[rslt.size()]);
    }
    
    public Map getShareeMap(){
        return shareeSel;
    }
    
    public AccessRule[] getShareeAccess(String shareeId){
        return ((CivisAccessEntry)shareeSel.get(shareeId)).getAccessRules();
    }
    
    /**
     * Sets the selection basket to the array of <code>ICivisEntity</code>
     * This method clears out the selection basket of all previous selections. 
     * @param selection array of <code>ICivisEntity</code>
     */
    public void setShareeSelection(ICivisEntity[] selection, AccessRule[][] accessRule, boolean isNew) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }  
        if (accessRule == null) {
            String msg = "Argument 'accessRule' cannot be null.";
            throw new IllegalArgumentException(msg);
        }  
        if (accessRule.length != selection.length) {
            String msg = "Argument 'selection' and 'accessRule' should be the same length.";
            throw new IllegalArgumentException(msg);
        }         

        clearShareeSelection();
        
        for(int i = 0; i < selection.length; i++){
            shareeSel.put(selection[i].getUrl()
                    , new CivisAccessEntry(selection[i].getUrl()
                         , accessRule[i], isNew));            
            if(selection[i] instanceof IPerson){
                memberCount++;
            }else{
                groupCount++;
            }
        }       
        
    }
    
    /**
     * Sets the selection basket to the array of <code>ICivisEntity</code>
     * This method clears out the selection basket of all previous selections. 
     * @param selection array of <code>ICivisEntity</code>
     */
    public void setShareeSelection(ICivisEntity[] selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        } 
        
        clearShareeSelection();
        BriefcaseAccessType[] types = new BriefcaseAccessType[] {BriefcaseAccessType.VIEW}; 
        
        for(int i = 0; i < selection.length; i++){
            shareeSel.put(selection[i].getUrl()
                    , new CivisAccessEntry(selection[i].getUrl()
                           , new AccessRule[] {new AccessRule (BriefcaseAccessType.VIEW, true )}
                           , true));
            if(selection[i] instanceof IPerson){
                memberCount++;
            }else{
                groupCount++;
            }
        }
    }
    
    /**
     * Sets the selection basket to the array of <code>ICivisEntity</code>
     * This method clears out the selection basket of all previous selections. 
     * @param selection array of <code>ICivisEntity</code>
     */
    public void addShareeSelection(ICivisEntity selection, AccessRule[] accessRule, boolean isNew) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }  
        if (accessRule == null) {
            String msg = "Argument 'accessRule' cannot be null.";
            throw new IllegalArgumentException(msg);
        }  
        
        shareeSel.put(selection.getUrl()
                , new CivisAccessEntry(selection.getUrl()
                        , accessRule
                        , isNew));
        if(selection instanceof IPerson){
            memberCount++;
            newMemberCount++;
        }else{
            groupCount++;
            newGroupCount++;
        }
        
    }
    
    /**
     * Adds sharees to the selection basket.
     * This method will append the selection list to the basket.
     * @param selection array of <code>ISharee</code>
     */
    public void addShareeSelection(ICivisEntity[] selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
         // reset the count
        BriefcaseAccessType[] types = new BriefcaseAccessType[] {BriefcaseAccessType.VIEW};
        for(int i = 0; i < selection.length; i++){
            shareeSel.put(selection[i].getUrl()
                    , new CivisAccessEntry(selection[i].getUrl()
                    , new AccessRule[] {new AccessRule(BriefcaseAccessType.VIEW, true)}
                    , true));
            if(selection[i] instanceof IPerson){
                newMemberCount++;
                memberCount++;
            }else{
                newGroupCount++;
                groupCount++;
            }
        }      
    }
    
    /**
     * Removes the given sharee from the basket.
     * @param selection <code>ISharee</code> object
     */
    public void removeSelection(ICivisEntity selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        CivisAccessEntry sharee = (CivisAccessEntry)shareeSel.remove(selection.getUrl());
        
        if(selection instanceof IPerson){          
            memberCount--;
            if(sharee.isNew()){
                newMemberCount--;
            }
        }else{
            groupCount--;
            if(sharee.isNew()){
                newGroupCount--;
            }
        }
        
    }
    
    /**
     * Removes the given sharee from the basket.
     * @param selection <code>ISharee</code> object
     */
    public void removeSelection(String shareeId) {

        // Assertions.
        if (shareeId == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        CivisAccessEntry sharee = (CivisAccessEntry)shareeSel.remove(shareeId);
        
        if(AbstractCivisFactory.entityFromUrl(shareeId) instanceof IPerson){          
            memberCount--;
            if(sharee.isNew()){
                newMemberCount--;
            }
        }else{
            groupCount--;
            if(sharee.isNew()){
                newGroupCount--;
            }
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
     * Checks to see if the given sharee is in the sharee basket.
     * @param id String value representing the id of the <code>IAcademusGroup</code>
     * and <code>IAcademusUser</code>
     * @param type <code>ShareeType</code> represeting the type of sharee, 
     * namely group or member
     * @return	boolean to represent if the given sharee is in the basket.
     */
    public boolean contains(String shareeId){
        return shareeSel.containsKey(shareeId);
    }
    
    public int size(){
        return this.shareeSel.size();
    }
    
    public void updateAccessRule(String shareeId, AccessRule[] accessRule){
        // Assertions.
        if (shareeId == null) {
            String msg = "Argument 'shareeId' cannot be null.";
            throw new IllegalArgumentException(msg);
        }  
        if (accessRule == null) {
            String msg = "Argument 'accessRule' cannot be null.";
            throw new IllegalArgumentException(msg);
        }  
        
        ((CivisAccessEntry)shareeSel.get(shareeId)).setAccessRules(accessRule);
        
    }
    
    /**
     * 
     * @param shareeAccess Map with key = shareeId and value = List of accessRule
     */
  /*  public void updateAccessRule(Map shareeAccess){
        // Assertions.
        if (shareeAccess == null) {
            String msg = "Argument 'shareeAccess' cannot be null.";
            throw new IllegalArgumentException(msg);
        }       
                
        String[] sharees = (String[])shareeAccess.keySet().toArray(new String[0]);
        for(int i = 0; i < shareeAccess.size(); i++){
            ((Sharee)shareeSel.get(sharees[i])).setRule((List)shareeAccess.get(sharees[i]));
        }
        
    }
    
    public String toString(){
        
        StringBuffer strb = new StringBuffer();
        String[] sharees = (String[])shareeSel.keySet().toArray(new String[shareeSel.size()]);
        List temp = null;
        for(int i = 0; i < sharees.length; i++){
            strb.append("Sharee : " + sharees[i] + "  Access " );
            temp = getShareeAccess(sharees[i]);
            for(int j = 0; j < temp.size(); j++){
               strb.append("\t   " + ((AccessRule)temp.get(j)).getAccessType().getName());
            }
        }
        
        return strb.toString();
    }*/

}
