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

import net.unicon.academus.civisselector.UserGroupHistory;
import net.unicon.academus.civisselector.UserPreferences;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;

public class SelectorWorkflow {
    
    private final UserGroupHistory gHistory;
    private String lastScreen;
    private String selLastScreen;
    private String searchString;
    private String charSel;
    private final UserPreferences groupPref;
    private final UserPreferences personPref;
    private final UserPreferences searchPref;
    private final UserPreferences selBasketPref;
    private ICivisFactory fac = null;
    private SelectionBasket basket = null;
    private SelectionBasket tempBasket = null;			// temporary storage between paging and tabs
    private IGroupRestrictor gRestrictor = null;
    private IPerson[] users = null;						// cache of the users in the selected factory.
    
    SelectorWorkflow(){
        this.gHistory = new UserGroupHistory();
        this.lastScreen = "";
        this.selLastScreen = "";
        this.searchString = "";
        this.charSel = "all";
        this.groupPref = new UserPreferences();
        this.personPref = new UserPreferences();
        this.searchPref = new UserPreferences();
        this.selBasketPref = new UserPreferences();  
        this.basket = new SelectionBasket();
        this.tempBasket = new SelectionBasket();
    }
    
    UserGroupHistory getGroupHistory(){
        return gHistory;
    }
    
    void setLastScreen(String screen){
        this.lastScreen = screen;
    }
    
    String getLastScreen(){
        return this.lastScreen;
    }
    
    void setSelLastScreen(String screen){
        this.selLastScreen = screen;
    }
    
    String getSelLastScreen(){
        return this.selLastScreen;
    }
    
    void setSearchString(String search){
        this.searchString = search;
    }
    
    String getSearchString(){
        return searchString;
    }
    
    void setCharSelection(String sel){
        this.charSel = sel;
    }
    
    String getCharSelection(){
        return charSel;
    }
    
    UserPreferences getGroupPreferences(){
        return this.groupPref;
    }
    
    UserPreferences getMemberPreferences(){
        return this.personPref;
    }
    
    UserPreferences getSelBasketPreferences(){
        return this.selBasketPref;
    }
    
    UserPreferences getSearchShareePreferences(){
        return this.searchPref;
    }
    
    ICivisFactory getSelectedFactory(){
        return fac;
    }
    
    void setSelectedFactory(ICivisFactory fac){
        this.fac = fac;
        this.users = null;			// reset the member list.
    }
    
    IGroupRestrictor getSelectedRestrictor(){
        return gRestrictor;
    }
    
    void setSelectedRestrictor(IGroupRestrictor gr){
        this.gRestrictor = gr;
    }
    
    SelectionBasket getSelectionBasket(){
        return this.basket;
    }  
    
    SelectionBasket getTempSelectionBasket(){
        return this.tempBasket;
    }  
    
    IPerson[] getUsers(){
        if (this.users == null){
            this.users = this.fac.getUsers();
        }
        
        return this.users;
    }
    
    IPerson[] findUsers(String searchString){
       
        return this.fac.searchUsers(searchString, searchString, searchString, "", false);
    }
    
    IGroup[] findGroups(String searchString){
        
         return this.fac.searchGroupsByName(searchString);
     }

}
