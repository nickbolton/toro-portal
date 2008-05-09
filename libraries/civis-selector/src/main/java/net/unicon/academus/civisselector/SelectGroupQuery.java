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
import java.util.LinkedList;
import java.util.List;

import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.GroupAscComparator;
import net.unicon.civis.GroupDescComparator;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.civis.UserAscComparator;
import net.unicon.civis.UserDescComparator;
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


public class SelectGroupQuery implements IStateQuery {

    private String groupId = "";
    private ISelectorUserContext suc = null;
    private IDecisionCollection[] decisions = null;
    private int lastDisplayed = 0;
    private int firstDisplayed = 0;
    private List selectedEntities = new ArrayList();
    
    private static final String FIRSTNAME_HANDLE = "fName";
    private static final String LASTNAME_HANDLE = "lName";

    public SelectGroupQuery(ISelectorUserContext suc){

        // Assertions
        if(suc == null){
            throw new IllegalArgumentException("Argument 'suc [ISelectorUserContext]' cannot be null.");
        }

        this.suc = suc;
        if(suc.getSelectorWorkflow() == null){
            ShowGroupAction.initializeSelectorWorkflow(suc);
        }
        suc.getSelectorWorkflow().setLastScreen("select_groups");
        suc.getSelectorWorkflow().setSelLastScreen("select_groups");
    }
    /* (non-Javadoc)
     * @see net.unicon.warlock.IStateQuery#query()
     */
    public String query() throws WarlockException {
        StringBuffer strb = new StringBuffer();
        List duplicates = new ArrayList();
        
        lastDisplayed = 0;
        firstDisplayed = 0;
        selectedEntities = new ArrayList();

        SelectorWorkflow workflow = suc.getSelectorWorkflow();

        if(workflow.getGroupHistory().hasGroup()){
	        groupId = GroupEntity.getEntityId(workflow.getGroupHistory().getLocation());
	    }else{
	        groupId = workflow.getGroupHistory().getPath();
	    }

        SelectionBasket basket = workflow.getSelectionBasket();

        strb.append("<state><selections members=\"")
	        .append(basket.getMemberCount())
	        .append("\" groups=\"")
	        .append(basket.getGroupCount())
	        .append("\"/>");

        try{
            IEntityStore store = new JvmEntityStore();
            List options = new LinkedList();
            IOption temp = null;
            ISelection sel = null;

            strb.append("<group-path current=\"")
            	.append(groupId)
            	.append("\"><group name=\"Group Options\" id=\"\" />");

            options.add(store.createOption(Handle.create(""),
                    null, TypeNone.INSTANCE));

            if (groupId.trim().equals("")) {
                sel = store.createSelection((IOption) options.get(options
                        .size() - 1), TypeNone.INSTANCE.parse(null));
            }else if (groupId.trim().equals("Personal")) {

                strb.append("<group name=\"My Personal Groups\" id=\"Personal\" />");
                temp = store.createOption(Handle
                        .create("Personal"), null,
                        TypeNone.INSTANCE);
                options.add(temp);
                sel = store.createSelection(temp, TypeNone.INSTANCE
                        .parse(null));
            } else if (groupId.trim().equals("All")){
                strb.append("<group name=\"All Campus Groups\" id=\"All\" />");

                temp = store.createOption(Handle
                        .create("All"), null,
                        TypeNone.INSTANCE);
                options.add(temp);
                sel = store.createSelection(temp, TypeNone.INSTANCE
                        .parse(null));
            }else {
                if(workflow.getGroupHistory().getPath().equals("All")){
                    strb.append("<group name=\"All Campus Groups\" id=\"All\" />");
                }else{
                    strb.append("<group name=\"My Personal Groups\" id=\"Personal\" />");
                }
                IGroup g = (IGroup)EntityType.getEntity(groupId).getObject();
                evaluateGroupPath(g);

                IGroup[] groups = workflow.getGroupHistory()
                        .getLocations();

                for (int i = 0; i < groups.length; i++) {
                    strb.append("<group id=\"")
                    	.append(GroupEntity.getEntityId(groups[i]))
                    	.append("\" name=\"")
                    	.append(groups[i].getName())
                        .append("\"/>");

                    options.add(store.createOption(Handle
                            .create(GroupEntity.getEntityId(groups[i])), null,
                            TypeNone.INSTANCE));
                    if (GroupEntity.getEntityId(groups[i]).equals(groupId)) {
                        sel = store.createSelection((IOption) options
                                .get(options.size() - 1),
                                TypeNone.INSTANCE.parse(null));
                        break;
                    }
                }
            }
	        IChoice choice = store.createChoice(Handle.create("chooseLocation"),
	                null, (IOption[])options.toArray(new IOption[0]),
	                0, 0);

	        Handle h = Handle.create("selectMemberGroupForm");
	        IChoiceCollection choices = store.createChoiceCollection(h, null,
	                                            new IChoice[] {choice });

	        IDecision dec = store.createDecision(null, choice
	                , new ISelection[] {sel});

	        IDecisionCollection dColl = store.createDecisionCollection(choices
	                , new IDecision[] { dec });

	        this.decisions = new IDecisionCollection[] { dColl, null};
	        
	        strb.append("</group-path>");
        }catch(Throwable t){
            String msg = "SelectGroupQuery failed to build its decision collection.";
            throw new RuntimeException(msg, t);
        }

        if(groupId.trim().equals("")){
            calculatePaging(strb, 2, workflow.getGroupPreferences().getPageState());
            addRootXml(strb);
        }else if(groupId.trim().equals("Personal")){
            workflow.getGroupHistory().clear();
            evaluatePersonalGroupXml(strb);
        }
        else if (groupId.equals("All")) {
            calculatePaging(strb, 1, workflow.getGroupPreferences().getPageState());
            strb.append("<group id=\"")
            	.append(EntityEncoder.encodeEntities(
            	        GroupEntity.getEntityId(workflow.getSelectedFactory().getRoot())))
            	.append("\" selectable=\"")
	               // check to see if the group has been restricted from the user
	            .append(workflow.getSelectedRestrictor().checkUsersGroupPermission(
	                    workflow.getSelectedFactory().getPerson(suc.getUsername())
						, workflow.getSelectedFactory().getRoot()
						, IGroupRestrictor.SELECT_GROUP_ACTIVITY
						, true))	
				.append("\" inbasket=\"")
	            .append(basket.contains(GroupEntity.getEntityId(workflow.getSelectedFactory().getRoot())))
	            .append("\" inprevious=\"")
	            .append(Arrays.asList(suc.getPrevEntitySelection())
	                    .contains(workflow.getSelectedFactory().getRoot()))
	            .append("\"><name>")
	            .append(workflow.getSelectedFactory().getRoot().getName())
	            .append("</name></group>");
            
            selectedEntities.add(new GroupEntity(workflow.getSelectedFactory().getRoot()));

        }else{
            IGroup[] subGroups = new IGroup[0];
            List sharees = new LinkedList();

            // get all the subgroups
            subGroups = ((IGroup)EntityType.getEntity(groupId).getObject()).getSubgroups();
            subGroups = this.sortGroups(subGroups);
            for(int i = 0; i < subGroups.length; i++){
                sharees.add(new GroupEntity(subGroups[i]));
            }

	       IPerson[] members = new IPerson[0];
           if(workflow.getGroupHistory().showMembers()){
                // add members
	            members = ((IGroup)EntityType.getEntity(groupId).getObject()).getMembers(false);
	            members = this.sortMembers(members);
	            for(int i = 0; i < members.length; i++){
	                sharees.add(new UserEntity(members[i]));
	            }

            }

           calculatePaging(strb, sharees.size()
                   , workflow.getGroupPreferences().getPageState());

           IEntity entity = null;
           IDecisionCollection dColl = null;
           for(int i = firstDisplayed - 1; i < lastDisplayed; i++){
               entity = (IEntity)sharees.get(i);               
              if(entity.getType().equals(EntityType.GROUP)){
                  selectedEntities.add(entity);
                  strb.append("<group id=\"");
	              strb.append(EntityEncoder.encodeEntities(entity.getEntityId()));
	              strb.append("\" selectable=\"");
					   // check to see if the group has been restricted from the user
	              strb.append(workflow.getSelectedRestrictor().checkUsersGroupPermission(
						        workflow.getSelectedFactory().getPerson(suc.getUsername())
								, (IGroup)entity.getObject()
								, IGroupRestrictor.SELECT_GROUP_ACTIVITY
								, true));	
	              strb.append("\" inbasket=\"");
	              strb.append(basket.contains(entity.getEntityId()));
	              strb.append("\" inprevious=\"");	                   
	              strb.append(Arrays.asList(suc.getPrevEntitySelection()).contains(entity.getObject()));
	              strb.append("\"><name>");
	              strb.append(EntityEncoder.encodeEntities(((IGroup)entity.getObject()).getName()));
	              strb.append("</name></group>");	 	                
	           }else{
	               dColl = ((IPerson)entity.getObject()).getAttributes();
	               
	               if(this.selectedEntities.contains(entity)){
	                   duplicates.add(entity.getObject());
	               }else{	               
		               selectedEntities.add(entity);
	                   strb.append("<member id=\"")
	                   		.append(EntityEncoder.encodeEntities(entity.getEntityId()))
	                   		.append("\" selectable=\"");
		               if (suc.getUsername().equals(((IPerson)entity.getObject()).getName())) {
		                   strb.append(suc.includeOwner());
				        } else {
				        	strb.append(true);
				        }
		               strb.append("\" inbasket=\"")
		               		.append(basket.contains(entity.getEntityId()))
							.append("\" inprevious=\"")
							.append(Arrays.asList(suc.getPrevEntitySelection()).contains(entity.getObject()))
							.append("\"><name>")
							.append(EntityEncoder.encodeEntities(
							        (String)dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue()))
							.append(", ")
							.append(EntityEncoder.encodeEntities(
							        (String)dColl.getDecision(FIRSTNAME_HANDLE).getFirstSelectionValue()))
							.append("</name></member>");
		           }
	           }
           }


        }

        strb.append("</options>");
        
        if(!duplicates.isEmpty()){
            strb.append("<status><error type=\"other\">")
	        	.append("<problem>Members with duplicate usernames were found. \n");
	        for(int i = 0; i < duplicates.size(); i++){
	            strb.append(((IPerson)duplicates.get(i)).getName() + "\n");
	        }
	        strb.append("</problem>")
	        	.append("<solution></solution>")
	        	.append("</error></status>");            
        }
        
        strb.append("</state>");
        return strb.toString();
    }

    /* (non-Javadoc)
     * @see net.unicon.warlock.IStateQuery#getDecisions()
     */
    public IDecisionCollection[] getDecisions() throws WarlockException {
        IEntityStore store = new JvmEntityStore();
        try {

            // Items Per Page.
            int ipp = suc.getSelectorWorkflow().getGroupPreferences().getPageState().getItemsPerPage();
            IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);

            // Items Per Page Value.
            ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });

            // Hide/show members
            boolean showMembers = suc.getSelectorWorkflow().getGroupHistory().showMembers();
            IOption oShow = null;

            if(showMembers){
            	oShow = store.createOption(Handle.create("show"), null, TypeNone.INSTANCE);
            }else{
            	oShow = store.createOption(Handle.create("hide"), null, TypeNone.INSTANCE);
            }
            IChoice cShow = store.createChoice(Handle.create("chooseMemberFilter"), null, new IOption[] { oShow }, 0, 1);

            // Hide/show members.
            ISelection sShow = store.createSelection(oShow, TypeNone.INSTANCE.parse(null));
            IDecision dShow= store.createDecision(null, cShow, new ISelection[] { sShow });

            // Name sort
            boolean nameSortAsc = suc.getSelectorWorkflow().getGroupPreferences().sortAsc();
            IOption oNameSort = store.createOption(Handle.create(nameSortAsc ? "asc":"des"), null, TypeNone.INSTANCE);
            IChoice cNameSort = store.createChoice(Handle.create("nameSortDirection"), null, new IOption[] { oNameSort }, 0, 1);

            // Name Sort Value.
            ISelection sNameSort = store.createSelection(oNameSort, TypeNone.INSTANCE.parse(null));
            IDecision dNameSort = store.createDecision(null, cNameSort, new ISelection[] { sNameSort });

            // selected members and groups
            List options = new ArrayList();
            List sels = new ArrayList();
            if(this.selectedEntities != null && this.selectedEntities.size() > 0){
                SelectionBasket basket = suc.getSelectorWorkflow().getTempSelectionBasket();
                List entities = Arrays.asList(basket.getEntities());
	            IOption temp = null;
	            
	            IEntity entity = null;
                // set all the selections
	            for( int i = 0; i < selectedEntities.size(); i++) {
	                entity = (IEntity)selectedEntities.get(i);
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
            IChoiceCollection choices = store.createChoiceCollection(h, null, new IChoice[] { cNumItems
                    , cShow
                    , cNameSort
                    , cMemberSel});

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dNumItems
                    , dShow
                    , dNameSort
                    , dMemberSel});

            this.decisions[1] = dColl;

        } catch (Throwable t) {
            String msg = "SelectGroupQuery failed to build its decision collection "
                                            + "for Paging.";
            throw new RuntimeException(msg, t);
        }

        return this.decisions;
    }

    private void evaluateGroupPath(IGroup g ) {

		// Assertions.
		if (g == null) {
		String msg = "Argument 'g [IAcademusGroup]' cannot be null.";
		throw new IllegalArgumentException(msg);
		}

		SelectorWorkflow workflow = suc.getSelectorWorkflow();
		String lUrl = workflow.getGroupHistory().getCrumbTrail(IGroup.GROUP_PATH_SEPARATOR);
		String[] gUrl;

		gUrl = new String[] {g.getPath()};

           // Contents.
			for(int i = 0; i < gUrl.length; i++){
			    if (lUrl.indexOf(gUrl[i]) != -1 && !lUrl.equals(gUrl[i])) {
				    while(!lUrl.equals(gUrl[i])){
				        if(workflow.getGroupHistory().hasBack()){
				            workflow.getGroupHistory().goBack();
				        }
				        lUrl = workflow.getGroupHistory().getCrumbTrail(IGroup.GROUP_PATH_SEPARATOR);
				    }
				    break;
				}
			}


    }

    private void evaluatePersonalGroupXml(StringBuffer strb){
        SelectorWorkflow workflow = suc.getSelectorWorkflow();

        IGroup[] groups = workflow.getSelectedFactory().getPerson(suc.getUsername()).getGroups();

        groups = this.sortGroups(groups);
        calculatePaging(strb, groups.length
                , workflow.getGroupPreferences().getPageState());
        String id = null;
        for(int i = firstDisplayed - 1; i < lastDisplayed; i++){
            id = GroupEntity.getEntityId(groups[i]);
            strb.append("<group id=\"")
            	.append(id)
            	.append("\" selectable=\"")
	            // check with group restrictor
				.append(workflow.getSelectedRestrictor().checkUsersGroupPermission(
				        workflow.getSelectedFactory().getPerson(suc.getUsername())
						, groups[i]
						, IGroupRestrictor.SELECT_GROUP_ACTIVITY
						, true))	
            	.append("\" inbasket=\"")
            	.append(workflow.getSelectionBasket().contains(id))
            	.append("\" inprevious=\"")
				.append(Arrays.asList(suc.getPrevEntitySelection()).contains(groups[i]))
				.append("\"><name>")
				.append(groups[i].getName())
				.append("</name></group>");
        }

    }

    private IPerson[] sortMembers(IPerson[] members) {
    	// Assertions.
    	if (members == null) {
    		throw new IllegalArgumentException("SelectGroupQuery: members " +
    				"must first be intialized.");
    	}
    	boolean sort = suc.getSelectorWorkflow().getGroupPreferences().sortAsc();
    	if(sort == true){
    	    Arrays.sort(members, new UserAscComparator());
    	}else{
    	    Arrays.sort(members, new UserDescComparator());
    	}
    	return members;
    }

    private IGroup[] sortGroups(IGroup[] groups) {
    	// Assertions.
    	if (groups == null) {
    		throw new IllegalArgumentException("SelectGroupQuery: groups " +
    				"must first be intialized.");
    	}
    	boolean sort = suc.getSelectorWorkflow().getGroupPreferences().sortAsc();
    	if(sort == true){
    	    Arrays.sort(groups, new GroupAscComparator());    	    
    	}else{
    	    Arrays.sort(groups, new GroupDescComparator());
    	}
    	return groups;
    }

    private void addRootXml(StringBuffer out){
        if(suc.getSelectorWorkflow().getGroupPreferences().sortAsc() == true){
            out.append("<group id=\"All")
            .append("\" selectable=\"false\" inbasket=\"false")
            .append("\" inprevious=\"false\">")
            .append("<name> All Campus Groups")
            .append("</name></group>")
    	    .append("<group id=\"Personal")
            .append("\" selectable=\"false\" inbasket=\"false")
            .append("\" inprevious=\"false\">")
            .append("<name> My Personal Groups")
            .append("</name></group>");
    	}else{
    	    out.append("<group id=\"Personal")
            .append("\" selectable=\"false\" inbasket=\"false")
            .append("\" inprevious=\"false\">")
            .append("<name> My Personal Groups")
            .append("</name></group>")
            .append("<group id=\"All")
            .append("\" selectable=\"false\" inbasket=\"false")
            .append("\" inprevious=\"false\">")
            .append("<name> All Campus Groups")
            .append("</name></group>");
    	}
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
