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

import java.util.Arrays;

import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
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

public class SelectBasketQuery implements IStateQuery {

    // instance members
    private SelectionBasket basket = null;
    private ISelectorUserContext suc = null;
    private IDecisionCollection[] decisions = null;
    
    private static final String FIRSTNAME_HANDLE = "fName";
    private static final String LASTNAME_HANDLE = "lName";

    
    public SelectBasketQuery(ISelectorUserContext suc){
        this.suc = suc;
    	this.basket = suc.getSelectorWorkflow().getSelectionBasket();
    	suc.getSelectorWorkflow().setLastScreen("select_basket");
    }
    
    public String query() throws WarlockException {
        
        StringBuffer rslt = new StringBuffer();
        
        rslt.append("<state><selections members=\"")
        	.append(basket.getMemberCount())
        	.append("\" groups=\"")
        	.append(basket.getGroupCount())
        	.append("\"> </selections>")      
        
        	.append(this.getXml())
        	.append("</options></state>");
        
        return rslt.toString();
    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        IEntityStore store = new JvmEntityStore();
        try {

            // Items Per Page.
            int ipp = suc.getSelectorWorkflow().getSelBasketPreferences().getPageState().getItemsPerPage();
            IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);

            // Items Per Page Value.
            ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });
            
            // Name sort
            boolean nameSortAsc = suc.getSelectorWorkflow().getSelBasketPreferences().sortAsc();
            IOption oNameSort = store.createOption(Handle.create(nameSortAsc ? "asc":"des"), null, TypeNone.INSTANCE);
            IChoice cNameSort = store.createChoice(Handle.create("nameSortDirection"), null, new IOption[] { oNameSort }, 0, 1);

            // Name Sort Value.
            ISelection sNameSort = store.createSelection(oNameSort, TypeNone.INSTANCE.parse(null));
            IDecision dNameSort = store.createDecision(null, cNameSort, new ISelection[] { sNameSort });

            // Choices.
            Handle h = Handle.create("selectMemberGroupForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null, new IChoice[] { cNumItems, cNameSort });

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dNumItems, dNameSort });
            this.decisions = new IDecisionCollection[] {dColl};

        } catch (Throwable t) {
            String msg = "SelectBasketQuery failed to build its decision collection "
                                            + "for Paging.";
            throw new RuntimeException(msg, t);
        }        
        return decisions;
    }
    
    private String getXml() {
        StringBuffer xml = new StringBuffer();
        
        IEntity entity = null;
        IPerson user = null;
        IGroup group = null;
        IEntity[] entities = basket.getEntities();
    	UserPreferences up = suc.getSelectorWorkflow().getSelBasketPreferences();
    	if (up.sortAsc()) {
    		Arrays.sort(entities, new EntityAscComparator());
    	} else {
    		Arrays.sort(entities, new EntityDescComparator());
    	}
        
    	// Paging
        PagingState ps = suc.getSelectorWorkflow().getSelBasketPreferences().getPageState();
        int totalPages = -1;
        switch (ps.getItemsPerPage()) {
            case PagingState.SHOW_ALL_ITEMS:
                totalPages = 1;
                break;
            default:
                // Account for complete pages.
                totalPages = (entities.length / ps.getItemsPerPage());
                // Account for partial page.
                if (entities.length % ps.getItemsPerPage() > 0) {
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
        int firstDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) + 1;
        int lastDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) + ps.getItemsPerPage();
        if (lastDisplayed > entities.length || ps.getItemsPerPage() == PagingState.SHOW_ALL_ITEMS) {
            lastDisplayed = entities.length;
        }
        
        xml.append("<options currentpage=\"")
        	.append(Integer.toString(ps.getCurrentPage()))
        	.append("\" perpage=\"")
        	.append(Integer.toString(ps.getItemsPerPage()))
        	.append("\" totalpages=\"")
        	.append(Integer.toString(totalPages))
        	.append("\" firstdisplayed=\"")
        	.append(Integer.toString(firstDisplayed))
        	.append("\" lastdisplayed=\"")
        	.append(Integer.toString(lastDisplayed))
        	.append("\" totalitems=\"")
        	.append(Integer.toString(entities.length))
        	.append("\">");    	
        
        IDecisionCollection dColl = null;
        
        for (int i = firstDisplayed - 1; i < lastDisplayed; i++) {
            entity = (IEntity)entities[i];
            if(entity.getType().equals(EntityType.MEMBER)){
                user = (IPerson)entity.getObject();
                dColl = user.getAttributes();
                
	            xml.append("<member id=\"")
	            .append(EntityEncoder.encodeEntities(entity.getEntityId()))
                .append("\" selectable=\"true\" inbasket=\"true\" inprevious=\"false\">")
                .append("<name>")
                .append(EntityEncoder.encodeEntities((String)dColl.getDecision(LASTNAME_HANDLE).getFirstSelectionValue()))
                .append(", ")
                .append(EntityEncoder.encodeEntities((String)dColl.getDecision(FIRSTNAME_HANDLE).getFirstSelectionValue()))
                .append("</name>")
                .append("</member>");
                
            }else{
                group = (IGroup)entity.getObject();
	            xml.append("<group id=\"")
	            	.append(EntityEncoder.encodeEntities(entity.getEntityId()))
	            	.append("\" selectable=\"true\" inbasket=\"true\" inprevious=\"false\">")
	            	.append("<name>")
	            	.append(EntityEncoder.encodeEntities(group.getName()))
	            	.append("</name>")
	            	.append("</group>");
                
            }      
        }
        return xml.toString();
    }

}
