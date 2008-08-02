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

package net.unicon.academus.apps.permissions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletSession;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.civisselector.ISelectorUserContext;
import net.unicon.academus.civisselector.SelectorWorkflow;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.fac.AbstractCivisFactory;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;

public class PermissionsUserContext implements IUserContext, ISelectorUserContext {

    // Instance Members.
    private final PermissionsApplicationContext app;
    private PortletHelper portletSel;
    private PortletAccessHelper portletAccessSel;
    private final PortletSession session;
    private final String username;

    private final PagingState permPaging;
    private boolean nameSortAsc;
    private String charSel;
    private String viewSel;
    private Map civisAccessEntry;
    
    // members for ISelectorUserContext
    private IScreen screen = null;
    private IStateQuery query = null;
    private ICivisEntity[] entities = new ICivisEntity[0];			// entries sent from the selector
    private ICivisEntity[] prevEntities = new ICivisEntity[0];		// entries to be sent to the selector (to show as perviously selected)
    private SelectorWorkflow workflow = null;
    
    /*
     * Public API.
     */

    public PermissionsUserContext(PermissionsApplicationContext app, PortletSession session, String username) {
        // Assertions.
    	if(app == null) {
        	throw new IllegalArgumentException(
        			"Argument 'app' cannot be null.");
        }
    	if(session == null) {
        	throw new IllegalArgumentException(
        			"Argument 'session' cannot be null.");
        }
    	if(username == null) {
        	throw new IllegalArgumentException(
        			"Argument 'username' cannot be null.");
        }

    	if ("".equals(username)) {
            throw new IllegalArgumentException(
                    "Argument 'username' cannot be empty.");
        }

        // Instance Members.
        this.app               	= app;
        this.portletSel    		= null;
        this.session           	= session;
        this.username          	= username;
        this.permPaging         = new PagingState();
        this.portletAccessSel  	= null;
        this.nameSortAsc     	= true;
        this.charSel 			= "all";
        this.viewSel 			= "all";
        this.civisAccessEntry 	= new HashMap();
    }

    public PermissionsApplicationContext getAppContext() { return this.app; }

    public String getUsername() {
    	assert username != null;
    	return username;
    }

    // TODO needs to be able to handle all civis factories specified in the config
    public Principal getPrincipal() {
        Principal rslt = null;
        try {
            IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade(true);
            IAcademusGroup[] groups = facade.getAllContainingGroups(username);
            
            // Create an Identity array that includes all containing groups,
            // all containing groups formatted to include groupId, and username.
            Identity[] ids = new Identity[(groups.length * 2) + 1];
            for (int i=0; i < groups.length; i++) {
                ids[i] = new Identity(
                            groups[i].getGroupPaths(
                                IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR,
                                false)[0],
                                IdentityType.GROUP);
                ids[i + groups.length] = new Identity(
                			groups[i].getGroupPaths(
                				IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, 
                				false)[0] + "[" + groups[i].getKey() + "]", 
                				IdentityType.GROUP);
            }
            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            rslt = new Principal(ids);
        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new RuntimeException(msg, t);
        }
        assert rslt != null;
        return rslt;
    }
    
    public boolean hasPortletSelection(){
        return (this.portletSel != null);
    }

    public void setPortletSelection(PortletHelper helper){
        this.portletSel = helper;
    }
    
    public PortletHelper getPortletSelection(){
        return this.portletSel;
    }
    
    public boolean hasPortletAccessSelection(){
        return (this.portletAccessSel != null);
    }

    public void setPortletAccessSelection(PortletAccessHelper helper){
        this.portletAccessSel = helper;
    }
    
    public PortletAccessHelper getPortletAccessSelection(){
        return this.portletAccessSel;
    }
    
    public PagingState getPermissionsPaging() {
        return permPaging;
    }
    
    public boolean isNameSortAsc(){
        return nameSortAsc;
    }
    
    public void toggleNameSort(){
        if(this.nameSortAsc){
            nameSortAsc = false;
        }else{
            nameSortAsc = true;
        }
    }
    
    public void setCharSelection(String sel){
        this.charSel = sel;
    }
    
    public String getCharSelection(){
        return charSel;
    }
    
    public void setViewSelection(String sel){
        this.viewSel = sel;
    }
    
    public String getViewSelection(){
        return viewSel;
    }
    
    public void clearCivicAccessEntries(){
        this.civisAccessEntry.clear();
    }
    
    public CivisAccessEntry[] getCivisAccessEntries(){
        return (CivisAccessEntry[])this.civisAccessEntry.values().toArray(new CivisAccessEntry[0]);
    }
    
    public void addCivisAccessEntry(CivisAccessEntry entry){
        this.civisAccessEntry.put(entry.getEntityId(), entry);
    }
    
    public void removeCivisAccessEntry(String id){
        this.civisAccessEntry.remove(id);
    }
    
    public AccessRule[] getCivisEntryAccessRules(String civisAccessEntryId){
        return ((CivisAccessEntry)this.civisAccessEntry.get(civisAccessEntryId)).getAccessRules();
    }
    
    public void setCivisEntryAccessRules(String civisAccessEntryId, AccessRule[] rules){
        ((CivisAccessEntry)this.civisAccessEntry.get(civisAccessEntryId)).setAccessRules(rules);
    }
    
    public ICivisEntity[] getCivisEntities(){
        
        Iterator it = this.civisAccessEntry.keySet().iterator();
        ICivisEntity[] rslt = new ICivisEntity[this.civisAccessEntry.size()];
        int index= 0;
        
        while (it.hasNext()){
            String entityId = (String)it.next();
          rslt[index++] =  AbstractCivisFactory.entityFromUrl(entityId); 
        }
        
        return rslt;
    }
    
     /*
     * Civis Selector Interface
     */
        
    public IScreen retrieveScreen() {
        return this.screen;
    }

    public IStateQuery retrieveQuery() {
        return this.query;
    }
    
    public void registerScreen(IScreen screen) {
        this.screen = screen;
    }

    public void registerQuery(IStateQuery query) {
        this.query = query;
    }

    public ICivisEntity[] getEntitySelection() {
       return this.entities;
    }

    public void setEntitySelection(ICivisEntity[] sel) {
        this.entities = new ICivisEntity[sel.length];
        System.arraycopy(sel, 0, entities, 0 , sel.length);        
    }
    
    public ICivisEntity[] getPrevEntitySelection() {
        return this.prevEntities;
     }

     public void setPrevEntitySelection(ICivisEntity[] sel) {
         this.prevEntities = new ICivisEntity[sel.length];
         System.arraycopy(sel, 0, prevEntities, 0 , sel.length);        
     }

    public SelectorWorkflow getSelectorWorkflow() {
        return this.workflow;
    }

    public void setSelectorWorkflow(SelectorWorkflow sWorkflow) {
        this.workflow = sWorkflow;        
    }
    
    public boolean includeOwner(){
        return true;
    }

    public ICivisFactory[] getCivisFactories() {
        return this.app.getCivisFactories();
    }
    
    public Map getGroupRestrictors() {
        return this.app.getGroupRestrictors();
    }

}
