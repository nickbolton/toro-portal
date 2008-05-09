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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.IPerson;
import net.unicon.civis.UserAscComparator;
import net.unicon.civis.UserDescComparator;
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

public class SelectMemberQuery implements IStateQuery {

    private ISelectorUserContext suc = null;
    private IDecisionCollection[] decisions = null;
    private IPerson[] members;
    private IPerson[] displayedMembers = new IPerson[0];
    private int lastDisplayed = 0;
    private int firstDisplayed = 0;
    private SelectorWorkflow workflow = null;
    private static final String FIRSTNAME_HANDLE = "fName";
    private static final String LASTNAME_HANDLE = "lName";

    public SelectMemberQuery(ISelectorUserContext suc){

        // Assertions
        if(suc == null){
            throw new IllegalArgumentException("Argument 'suc [ISelectorUserContext]' cannot be null.");
        }

        this.suc = suc;
        this.decisions = null;
        this.workflow = suc.getSelectorWorkflow();
        workflow.setLastScreen("select_members");
        workflow.setSelLastScreen("select_members");

        members = workflow.getUsers();
		this.sortMembers();

    }

	public String query() throws WarlockException {
        StringBuffer strb = new StringBuffer();

        strb.append("<state><selections members=\"")
        	.append(workflow.getSelectionBasket().getMemberCount())
        	.append("\" groups=\"")
        	.append(workflow.getSelectionBasket().getGroupCount())
        	.append("\"/>");

        members = this.filter();

        calculatePaging(strb, members.length
                , workflow.getMemberPreferences().getPageState());

        IPerson[] duplicates= this.memberXml(strb, members);

        strb.append("</options>");
        
        if(duplicates.length > 0){
            strb.append("<status><error type=\"other\">")
	        	.append("<problem>Members with duplicate usernames were found. \n");
	        for(int i = 0; i < duplicates.length; i++){
	            strb.append(duplicates[i].getName() + "\n");
	        }
	        strb.append("</problem>")
	        	.append("<solution></solution>")
	        	.append("</error></status>");            
        }
        
        strb.append("</state>");

//Uncomment to see Query XML
//System.out.println("******************\nSelectMemberQuery XML\n" + strb);
        return strb.toString();

	}

    public IDecisionCollection[] getDecisions() throws WarlockException {
        IEntityStore store = new JvmEntityStore();
        try {

            // Items Per Page.
            int ipp = workflow.getMemberPreferences().getPageState().getItemsPerPage();
            IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);

            // Items Per Page Value.
            ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });

            // Name Filter
            String nameFilter = workflow.getCharSelection();
            if(nameFilter.equals("")){
            	nameFilter = "all";
            }
            IOption oNameFilter = store.createOption(Handle.create(nameFilter), null, TypeNone.INSTANCE);
            IChoice cNameFilter = store.createChoice(Handle.create("chooseNameFilter"), null, new IOption[] { oNameFilter }, 0, 1);

            // Name Filter Value.
            ISelection sNameFilter = store.createSelection(oNameFilter, TypeNone.INSTANCE.parse(null));
            IDecision dNameFilter = store.createDecision(null, cNameFilter, new ISelection[] { sNameFilter });

            // Name sort
            boolean nameSortAsc = workflow.getMemberPreferences().sortAsc();
            IOption oNameSort = store.createOption(Handle.create(nameSortAsc ? "asc":"des"), null, TypeNone.INSTANCE);
            IChoice cNameSort = store.createChoice(Handle.create("nameSortDirection"), null, new IOption[] { oNameSort }, 0, 1);

            // Name Sort Value.
            ISelection sNameSort = store.createSelection(oNameSort, TypeNone.INSTANCE.parse(null));
            IDecision dNameSort = store.createDecision(null, cNameSort, new ISelection[] { sNameSort });

            // selected members
            Map options = new HashMap();
            Map sels = new HashMap();
            if(this.displayedMembers != null && this.displayedMembers.length > 0){
                SelectionBasket basket = suc.getSelectorWorkflow().getTempSelectionBasket();
                List entities = Arrays.asList(basket.getEntities());
	            IOption temp = null;
	            
	            UserEntity entity = null;
                // set all the selections
	            for( int i = 0; i < displayedMembers.length; i++) {
	                entity = new UserEntity(displayedMembers[i]);
                    // Create entries only for selected members: all others are
                    // ignored in the screen definition.
	                if(entities.contains(entity)){
                        temp = store.createOption(
	                        Handle.create(EntityEncoder.encodeEntities(entity.getEntityId()))
	                        , null, TypeNone.INSTANCE);
                        options.put(entity.getEntityId(), temp);
	                
	                    sels.put(entity.getEntityId(), store.createSelection(temp, TypeNone.INSTANCE.parse(null)));
	                }
	                            
	            }            
	            
            }
            
            IChoice cMemberSel = store.createChoice(Handle.create("selectedItems")
                    , null
                    , (IOption[])options.values().toArray(new IOption[0])
                    , 0
                    , 0); 
            
            IDecision dMemberSel = store.createDecision(null, cMemberSel
                    , (ISelection[])sels.values().toArray(new ISelection[0])
                    );
            
            // Choices.
            Handle h = Handle.create("selectMemberGroupForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null, new IChoice[] { cNumItems
                    , cNameFilter 
                    , cNameSort
                    , cMemberSel});

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dNumItems
                    , dNameFilter
                    , dNameSort
                    , dMemberSel});
            
            this.decisions = new IDecisionCollection[] { dColl};    
            
//System.out.println("SelectMemeberQuery DC " + dColl.toXml());

        } catch (Throwable t) {
            String msg = "SelectMemberQuery failed to build its decision collection "
                                            + "for Paging.";
            throw new RuntimeException(msg, t);
        }

        return this.decisions;
    }

    private void sortMembers() {
    	// Assertions.
    	if (this.members == null) {
    		throw new IllegalArgumentException("SelectMemberQuery: members " +
    				"must first be intialized.");
    	}
    	UserPreferences up = workflow.getMemberPreferences();
    	if (up.sortAsc()) {
    		Arrays.sort(members, new UserAscComparator());
    	} else {
    		Arrays.sort(members, new UserDescComparator());
    	}

    }

    private IPerson[] filter() {
        String charSel = workflow.getCharSelection();
        IPerson user = null;
    	String lastName = "";
    	List filtered = new LinkedList();
    	IDecisionCollection dColl = null;
    	
    	if (charSel.equalsIgnoreCase("All")) {
    		return members;
    	} else if (charSel.equalsIgnoreCase("XYZ")) {
			String c1 = String.valueOf(charSel.charAt(0));
			String c2 = String.valueOf(charSel.charAt(1));
			String c3 = String.valueOf(charSel.charAt(2));
			
    		for (int i = 0; i < members.length; i++) {
    			user = members[i];
    			
    			dColl = user.getAttributes();
    			lastName = (String)dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue();

    			if ((lastName.toLowerCase().indexOf(c1.toLowerCase()) == 0)
    			  ||(lastName.toLowerCase().indexOf(c2.toLowerCase()) == 0)
				  ||(lastName.toLowerCase().indexOf(c3.toLowerCase()) == 0)) {
    				filtered.add(user);
    			}
    		}

    	} else {
    		String c = String.valueOf(charSel.charAt(0));
			for (int i = 0; i < members.length; i++) {
    			user = this.members[i];
    			
    			dColl = user.getAttributes();
    			lastName = (String)dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue();

    			if (lastName.toLowerCase().indexOf(c.toLowerCase()) == 0) {
    				filtered.add(user);
    			}
    		}

    	}
    	return (IPerson[])filtered.toArray(new IPerson[0]);
    }

    private IPerson[] memberXml(StringBuffer strb, IPerson[] users) {
    	IPerson user = null;
    	IDecisionCollection dColl;
    	Set shareeList = new HashSet();
    	List duplicateList = new ArrayList();
    	for(int i = this.firstDisplayed -1; i < this.lastDisplayed; i++) {
    		user = users[i];
    		if(!shareeList.add(user)){
    		    duplicateList.add(user);
    		}else{
	    		strb.append("<member id=\"")
		        	.append(EntityEncoder.encodeEntities(UserEntity.getEntityId(user)))
		        	.append("\" selectable=\"");
		        	if(user.getName().equals(suc.getUsername())){
		        	    strb.append(suc.includeOwner());
		        	}else{
		        	    strb.append(true);
		        	}
		        strb.append("\" inbasket=\"")
					.append(suc.getSelectorWorkflow().getSelectionBasket()
					        .contains(UserEntity.getEntityId(user)))
				    .append("\" inprevious=\"")
				    .append(Arrays.asList(suc.getPrevEntitySelection())
				            .contains(user))
		        	.append("\"> <name>");
	
		        dColl = user.getAttributes();
				
		        strb.append(EntityEncoder.encodeEntities(
		                (String)dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue()))
		            .append(", ")
		        	.append(EntityEncoder.encodeEntities(
		                (String)dColl.getDecision(FIRSTNAME_HANDLE).getFirstSelectionValue()))
		        	.append("</name>")
		        	.append("</member>");
	    	}
		}
    	
    	this.displayedMembers = (IPerson[])shareeList.toArray(new IPerson[0]);

		return (IPerson[])duplicateList.toArray(new IPerson[0]);
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
        this.firstDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) + 1;
        this.lastDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) + ps.getItemsPerPage();
        if (lastDisplayed > total || ps.getItemsPerPage() == PagingState.SHOW_ALL_ITEMS) {
            this.lastDisplayed = total;
        }

        setPaging(out, ps.getCurrentPage(), ps.getItemsPerPage()
                , totalPages, firstDisplayed, lastDisplayed
                , total);

    }
}
