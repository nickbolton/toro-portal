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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

public class SearchShareeQuery implements IStateQuery {

    private ISelectorUserContext suc = null;
    private IDecisionCollection[] decisions = null;
    private int lastDisplayed = 0;
    private int firstDisplayed = 0;    
    private IEntity[] selectedEntities;
    private static final String FIRSTNAME_HANDLE = "fName";
    private static final String LASTNAME_HANDLE = "lName";
    private ErrorMessage[] errors = null;

    
    public SearchShareeQuery(ISelectorUserContext suc){
        
        // Assertions
        if(suc == null){
            throw new IllegalArgumentException("Argument 'suc " +
            			"[ISelectorUserContext]' cannot be null.");
        } 
        
        this.suc = suc;
        suc.getSelectorWorkflow().setSelLastScreen("select_search_results");
        suc.getSelectorWorkflow().setLastScreen("select_search_results");
    }
    
    public SearchShareeQuery(ISelectorUserContext suc, ErrorMessage[] errors){
        
        // Assertions
        if(suc == null){
            throw new IllegalArgumentException("Argument 'suc " +
            			"[ISelectorUserContext]' cannot be null.");
        } 
        
        this.suc = suc;
        suc.getSelectorWorkflow().setSelLastScreen("select_search_results");
        suc.getSelectorWorkflow().setLastScreen("select_search_results");
        this.errors = errors;
    }
    
    public String query() throws WarlockException {
        StringBuffer strb = new StringBuffer();
        List duplicateList = new ArrayList();
        Set selectedSet = new HashSet();
        
        if(errors != null){
            strb.append("<state><status>");
            for(int i = 0; i <  errors.length; i++){
                strb.append(errors[i].toXml());
            }
            strb.append("</status></state>");
        }else{
		int memberCount = 0;
		int groupCount = 0;

        lastDisplayed = 0;
        firstDisplayed = 0;    
        selectedEntities = new IEntity[0];
		
		SelectorWorkflow workflow = suc.getSelectorWorkflow();
		SelectionBasket basket = workflow.getSelectionBasket();
		
		String searchString = workflow.getSearchString();
		
        strb.append("<state><selections members=\"")
        	.append(basket.getMemberCount())
        	.append("\" groups=\"")
        	.append(basket.getGroupCount())
        	.append("\" /><search>")
        	.append(searchString)
        	.append("</search>");
        
    	IPerson[] users = workflow.findUsers(searchString);
        IGroup[] subGroups = workflow.getSelectedFactory().getRoot().getDescendentGroups();
        IGroup[] groups = new IGroup[subGroups.length+1];
        System.arraycopy(subGroups, 0, groups, 0, subGroups.length);
        groups[subGroups.length] = workflow.getSelectedFactory().getRoot();

		List tempEntities = new ArrayList();
        StringBuffer temp = new StringBuffer();
        
        IDecisionCollection dColl = null;
        
        for(int i = 0; i < users.length; i++) {
            dColl = users[i].getAttributes();
            
			temp.append(dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue())
				.append(", ")
				.append(dColl.getDecision(FIRSTNAME_HANDLE).getFirstSelectionValue());
			if (temp.toString().toLowerCase().indexOf(
					searchString.toLowerCase()) != -1) {
			    tempEntities.add(new UserEntity(users[i]));
	        	memberCount++;
			}
			temp.setLength(0);
        }
        for(int i = 0; i < groups.length; i++) {
            if(groups[i].getName().toLowerCase().indexOf(searchString
                        .toLowerCase()) != -1) {
                tempEntities.add(new GroupEntity(groups[i]));
                groupCount++;
            }
        }
        
        IEntity[] entities = (IEntity[])tempEntities.toArray(new 
                IEntity[tempEntities.size()]);
        // sorting occurs
		if (workflow.getSearchShareePreferences().sortAsc() == true) {
			Arrays.sort(entities, new EntityAscComparator());				
		} else {
		    Arrays.sort(entities, new EntityDescComparator());				
        }
        
		calculatePaging(strb, entities.length
		        , workflow.getSearchShareePreferences()
				.getPageState());
		
        IPerson user = null;
        IGroup group = null;
        
        for (int i = this.firstDisplayed - 1; i < this.lastDisplayed; i++) {
        	if (entities[i].getType().equals(EntityType.GROUP)) {
        		group = (IGroup)entities[i].getObject();
        		selectedSet.add(entities[i]);
                strb.append(" <group id=\"")
                	.append(EntityEncoder.encodeEntities(entities[i].getEntityId()))
                	.append("\" selectable=\"")
                	// integrate with the group restrictor
                	.append(workflow.getSelectedRestrictor().checkUsersGroupPermission(
                	        workflow.getSelectedFactory().getPerson(suc.getUsername())
                	        , group
                	        , IGroupRestrictor.SELECT_GROUP_ACTIVITY
                	        , true))
                	.append("\" inbasket=\"")
					.append(basket.contains(entities[i].getEntityId())).append("\" ")
		        	.append("inprevious=\"")
		        	.append((Arrays.asList(suc.getPrevEntitySelection())).contains(group))
		        	.append("\"><name>")
		        	.append(EntityEncoder.encodeEntities(group.getName()))
		        	.append("</name>")
		        	.append("</group>");
        	} 
        	if (entities[i].getType().equals(EntityType.MEMBER)) {
        		user = (IPerson)entities[i].getObject();
        		if(!selectedSet.add(entities[i])){
        		    duplicateList.add(user);
	    		}else{
	        		dColl = user.getAttributes();
	        		
					strb.append(" <member id=\"")
						.append(EntityEncoder.encodeEntities(entities[i].getEntityId()))
						.append("\" selectable=\"");
					if(user.getName().equals(suc.getUsername())){
		        	    strb.append(suc.includeOwner());
		        	}else{
		        	    strb.append(true);
		        	}
					strb.append("\" inbasket=\"")
						.append(basket.contains(entities[i].getEntityId()))
						.append("\" inprevious=\"")
						.append((Arrays.asList(suc.getPrevEntitySelection())).contains(user))
						.append("\"><name>")
						.append(EntityEncoder.encodeEntities(
						        (String)dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue()))
						.append(", ")
						.append(EntityEncoder.encodeEntities(
						        (String)dColl.getDecision(FIRSTNAME_HANDLE).getFirstSelectionValue()))
						.append("</name>")
						.append("</member>");
				}
        	}
		}
            
        this.selectedEntities = (IEntity[])selectedSet.toArray(new IEntity[0]);
        
        strb.append("</options>");
        
        if(!duplicateList.isEmpty()){
            strb.append("<status><error type=\"other\">")
	        	.append("<problem>Members with duplicate usernames were found. \n");
	        for(int i = 0; i < duplicateList.size(); i++){
	            strb.append(((IPerson)duplicateList.get(i)).getName() + "\n");
	        }
	        strb.append("</problem>")
	        	.append("<solution></solution>")
	        	.append("</error></status>");            
        }
        
        strb.append("<selections members=\"")
	        .append(memberCount)
	        .append("\" groups=\"")
	        .append(groupCount)
	        .append("\"/></state>");
        }
        
// System.out.println("SearchShareeQuery XML: \n\t" + strb);

        return strb.toString();
    }
    
    public IDecisionCollection[] getDecisions() throws WarlockException {
        IEntityStore store = new JvmEntityStore();
        try {

            // Items Per Page.
            int ipp = suc.getSelectorWorkflow().getSearchShareePreferences().getPageState()
													.getItemsPerPage();
            IOption oNumItems = store.createOption(Handle
            		.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle
            		.create("chooseDisplayNumber"), 
					null, new IOption[] { oNumItems }, 0, 1);

            // Items Per Page Value.
            ISelection sNumItems = store.createSelection(oNumItems, 
            							TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, 
            							new ISelection[] { sNumItems });
            
            // Name sort
            boolean nameSortAsc = suc.getSelectorWorkflow().getSearchShareePreferences().sortAsc();
            IOption oNameSort = store.createOption(Handle.create(nameSortAsc ? "asc":"des"), null, TypeNone.INSTANCE);
            IChoice cNameSort = store.createChoice(Handle.create("nameSortDirection"), null, new IOption[] { oNameSort }, 0, 1);

            // Name Sort Value.
            ISelection sNameSort = store.createSelection(oNameSort, TypeNone.INSTANCE.parse(null));
            IDecision dNameSort = store.createDecision(null, cNameSort, new ISelection[] { sNameSort });

            // selected members and groups
            List options = new ArrayList();
            List sels = new ArrayList();
            if(this.selectedEntities != null && this.selectedEntities.length > 0){
                SelectionBasket basket = suc.getSelectorWorkflow().getTempSelectionBasket();
                List entities = Arrays.asList(basket.getEntities());
	            IOption temp = null;
	            
	            IEntity entity = null;
                // set all the selections
	            for( int i = 0; i < selectedEntities.length; i++) {
	                entity = (IEntity)selectedEntities[i];
	                temp = store.createOption(
	                        Handle.create(EntityEncoder.encodeEntities(entity.getEntityId()))
	                        , null, TypeNone.INSTANCE);
	                options.add(temp);
	                
	                if(entities.contains(entity)){
	                    sels.add(store.createSelection(temp, TypeNone.INSTANCE.parse(null)));
	                }	                            
	            }    
            }
            
            IChoice cMemberSel = store.createChoice(Handle.create("selectedItems")
                    , null
                    , (IOption[])options.toArray(new IOption[0])
                    , 0
                    , 0); 
            
            IDecision dMemberSel = store.createDecision(null, cMemberSel
                    , (ISelection[])sels.toArray(new ISelection[0])
                    );

            // Choices.
            Handle h = Handle.create("selectMemberGroupForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null,
            									new IChoice[] { cNumItems, cMemberSel, cNameSort});
            
            IDecisionCollection dColl = store.createDecisionCollection(choices,
            							new IDecision[] { dNumItems, dMemberSel, dNameSort});
            this.decisions = new IDecisionCollection[] { dColl };

        } catch (Throwable t) {
            String msg = "SearchShareeQuery failed to build its decision collection "
                                            + "for Paging.";
            throw new RuntimeException(msg, t);
        }
        
        return this.decisions;
    }
    
    private void setPaging(StringBuffer out, int currentPage, int itemsPerPage
            , int totalPages, int firstDisplayed, int lastDisplayed
            , int total){
        
        out.append("<options currentpage=\"")
        	.append(currentPage)
        	.append("\" perpage=\"")
        	.append(itemsPerPage)
        	.append("\" totalpages=\"")
        	.append(totalPages)
        	.append("\" firstdisplayed=\"")
        	.append(firstDisplayed)
        	.append("\" lastdisplayed=\"")
        	.append(lastDisplayed)
        	.append("\" totalitems=\"")
        	.append(total)
        	.append("\">");        
    }
    
    private void calculatePaging(StringBuffer out, int total, PagingState ps){
        
        
        int totalPages = -1;
        switch (ps.getItemsPerPage()) {
            case PagingState.SHOW_ALL_ITEMS:
                totalPages = 1;
                break;
            default:
                // Account for complete pages.
                totalPages = (total / ps.getItemsPerPage());
                // Account for partial page.
                if (total % ps.getItemsPerPage() > 0) {
                    totalPages += 1;
                }
                // Account for empty contents (must always be a page).
                if (totalPages == 0) {
                    totalPages = 1;
                }
                break;
        }
        if (ps.getCurrentPage() == PagingState.LAST_PAGE) {
            // action has been invoked to move to the last page...
            ps.setCurrentPage(totalPages);
        }
        while (ps.getCurrentPage() > totalPages) {
            // ratchet-down the current page where necessary.
            ps.setCurrentPage(ps.getCurrentPage() - 1);
        }
        this.firstDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) 
								+ 1;
        this.lastDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage())
								+ ps.getItemsPerPage();
        if (lastDisplayed > total || ps.getItemsPerPage() == 
        								PagingState.SHOW_ALL_ITEMS) {
            this.lastDisplayed = total;
        }
        
        setPaging(out, ps.getCurrentPage(), ps.getItemsPerPage()
                , totalPages, firstDisplayed, lastDisplayed
                , total);
        
    }
    
}
