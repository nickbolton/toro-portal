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

package net.unicon.academus.apps.briefcase.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.UserEditPreferences;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.CivisEntityAscComparator;
import net.unicon.civis.CivisEntityDescComparator;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.demetrius.IFolder;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

/**
 * @author ibiswas
 *
 */
public class EditPermissionQuery implements IStateQuery {

    private static DateFormat dateFormat = new SimpleDateFormat();    
    private IDecisionCollection[] decisions = null; 
    private BriefcaseUserContext buc = null;
    private final Log log = LogFactory.getLog(getClass());
    
    public EditPermissionQuery(BriefcaseUserContext buc){
        
        // assertions
        if(buc == null){
            throw new IllegalArgumentException("Argument 'buc [BriefcaseUserContext]' cannot be null");
        }
        
        this.buc = buc;
                
    }
 
    public String query() throws WarlockException {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }
        
        IFolder folder = (IFolder)buc.getResourceSelection()[0];
        
        if(buc.getEntitySelection().length > 0){
            buc.getTargetSelection().addShareeSelection(buc.getEntitySelection());
            buc.setEntitySelection(new ICivisEntity[0]);
        }
        
        StringBuffer strb = new StringBuffer();
        strb.append("<state><shared-items>");
        strb.append("<resource id=\"").append(EntityEncoder.encodeEntities(folder.getUrl())).append("\" name=\"").append(EntityEncoder.encodeEntities(folder.getName()));
        strb.append("\" shared=\"no\">");
        strb.append("<mimetype>").append(EntityEncoder.encodeEntities(folder.getMimeType())).append("</mimetype>");
        strb.append("<type>").append(EntityEncoder.encodeEntities(folder.getType().getName())).append("</type>");
        strb.append("<lastmod>").append(dateFormat.format(folder.getDateModified())).append("</lastmod>");
        strb.append("<size>").append(folder.getSize()).append("</size>");
        strb.append("</resource></shared-items>");
        
        //you just added 
        strb.append("<additions>");
        strb.append("<members count=\"").append(buc.getTargetSelection().getNewMemberCount())
        .append("\" />");
        strb.append("<groups count=\"").append(buc.getTargetSelection().getNewGroupCount())
        .append("\" />");        
        strb.append("</additions>");
                
        ICivisEntity[] sharees = null;
        if(((UserEditPreferences)buc.getEditPreferences()).showAll()){
            sharees = buc.getTargetSelection().getSharees();
        }else{
            sharees = buc.getTargetSelection().getNewSharees();
        }
            
        sharees = this.sortSharees(sharees);
        
        // Paging
        PagingState ps = buc.getEditPreferences().getPageState();
        int totalPages = -1;
        switch (ps.getItemsPerPage()) {
            case PagingState.SHOW_ALL_ITEMS:
                totalPages = 1;
                break;
            default:
                // Account for complete pages.
                totalPages = (sharees.length / ps.getItemsPerPage());
                // Account for partial page.
                if (sharees.length % ps.getItemsPerPage() > 0) {
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
        if (lastDisplayed > sharees.length || ps.getItemsPerPage() == PagingState.SHOW_ALL_ITEMS) {
            lastDisplayed = sharees.length;
        }
        
        strb.append("<sharees currentpage=\"").append(Integer.toString(ps.getCurrentPage()))
        .append("\" perpage=\"").append(Integer.toString(ps.getItemsPerPage()))
        .append("\" totalpages=\"").append(Integer.toString(totalPages))
        .append("\" firstdisplayed=\"").append(Integer.toString(firstDisplayed))
        .append("\" lastdisplayed=\"").append(Integer.toString(lastDisplayed))
        .append("\" totalitems=\"").append(Integer.toString(sharees.length)).append("\">");
        
        IPerson user = null;
        IGroup group = null;
        
        for(int i = firstDisplayed - 1; i < lastDisplayed; i++){ 
            if(sharees[i] instanceof IPerson){
                strb.append("<member id=\"").append(EntityEncoder.encodeEntities(sharees[i].getUrl())).append("\">");
                strb.append("<name>").append(EntityEncoder.encodeEntities(getEntryName(sharees[i])));
                strb.append("</name></member>");
            }else{
                strb.append("<group id=\"").append(EntityEncoder.encodeEntities(sharees[i].getUrl())).append("\">");
                strb.append("<name>").append(EntityEncoder.encodeEntities(getEntryName(sharees[i])));
                strb.append("</name></group>");
                
            }               
            
        }
        strb.append("</sharees></state>");
        return strb.toString();
    }

    /**
     * 
     */
    public IDecisionCollection[] getDecisions() throws WarlockException { 
        IEntityStore store = new JvmEntityStore();
        try {
            List options = new LinkedList();
            
            Set readSels = new HashSet();
            Set writeSels = new HashSet();
            Set deleteSels = new HashSet();
            
            AccessRule[] rule = null;
            IOption temp = null;
            
            Label lbl = Label.create("To set VIEW permissions");
                
            ICivisEntity[] sharees = buc.getTargetSelection().getSharees();
            for(int i = 0; i < sharees.length; i++){
                rule = buc.getTargetSelection().getShareeAccess(sharees[i].getUrl());
              
                temp = store.createOption(
                        Handle.create(sharees[i].getUrl()), null,
                        TypeNone.INSTANCE);
                options.add(temp);
                
                for(int j = 0; j < rule.length; j++){
                    switch(((AccessRule)rule[j]).getAccessType().toInt()){
                    	case 2 :
                    	    writeSels.add(store.createSelection(temp, TypeNone.INSTANCE.parse(null)));
                    	    readSels.add(store.createSelection(temp, TypeNone.INSTANCE.parse(null)));
                    	    break;
                    	case 3 :
                    	    deleteSels.add(store.createSelection(temp, TypeNone.INSTANCE.parse(null)));                  	        
                    	case 1 :
                    	    readSels.add(store.createSelection(temp, TypeNone.INSTANCE.parse(null)));
                    	    break;                    	
                    }
                }
            }
            IChoice read = store.createChoice(Handle.create("readPermittedItems"),
                    lbl, (IOption[])options.toArray(new IOption[0]),
                    0, 0);
            IChoice delete = store.createChoice(Handle.create("deletePermittedItems"),
                    lbl, (IOption[])options.toArray(new IOption[0]),
                    0, 0);
            IChoice write = store.createChoice(Handle.create("writePermittedItems"),
                    lbl, (IOption[])options.toArray(new IOption[0]),
                    0, 0);
            
            // Choices.
            Handle h = Handle.create("editPermissionForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null,
                                                new IChoice[] {read, delete, write });   
            
            IDecision readDec = store.createDecision(null, read
                    , (ISelection[])readSels.toArray(new ISelection[0])
                    );
            IDecision deleteDec = store.createDecision(null, delete
                    , (ISelection[])deleteSels.toArray(new ISelection[0]));
            IDecision writeDec = store.createDecision(null, write
                    , (ISelection[])writeSels.toArray(new ISelection[0]));

            IDecisionCollection dColl = store.createDecisionCollection(choices
                    , new IDecision[] { readDec, deleteDec, writeDec });
            this.decisions = new IDecisionCollection[] { dColl, null, null };

           // Items Per Page.
	        int ipp = buc.getEditPreferences().getPageState().getItemsPerPage();
	        IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
	        IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);
	
	        // Choices.
	        choices = store.createChoiceCollection(h, null, new IChoice[] { cNumItems });
	
	        // Items Per Page Value.
	        ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
	        IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });
	
	        dColl = store.createDecisionCollection(choices, new IDecision[] { dNumItems });
	        this.decisions[1] = dColl;
	        
	        //	ViewAll/Veiw New additions only
	        boolean newAdds = ((UserEditPreferences)buc.getEditPreferences()).showAll();
	        IOption oNewAdds = null;
	        if(newAdds){
	        	oNewAdds = store.createOption(Handle.create("all"), null, TypeNone.INSTANCE);
	        }else{
	        	oNewAdds = store.createOption(Handle.create("new"), null, TypeNone.INSTANCE);
	        }
	        IChoice cNewAdds = store.createChoice(Handle.create("newAdditionFilter"), null, new IOption[] { oNewAdds }, 0, 1);
	
	        // Items Per Page Value.
	        ISelection sNewAdds = store.createSelection(oNewAdds, TypeNone.INSTANCE.parse(null));
	        IDecision dNewAdds = store.createDecision(null, cNewAdds, new ISelection[] { sNewAdds });
	
	        // Name sort
            boolean nameSortAsc = buc.getEditPreferences().sortAsc();
            IOption oNameSort = store.createOption(Handle.create(nameSortAsc ? "asc":"des"), null, TypeNone.INSTANCE);
            IChoice cNameSort = store.createChoice(Handle.create("nameSortDirection"), null, new IOption[] { oNameSort }, 0, 1);

            // Name Sort Value.
            ISelection sNameSort = store.createSelection(oNameSort, TypeNone.INSTANCE.parse(null));
            IDecision dNameSort = store.createDecision(null, cNameSort, new ISelection[] { sNameSort });

	        
	        // Choices.
	        choices = store.createChoiceCollection(h, null, new IChoice[] { cNewAdds, cNameSort });
	
	        dColl = store.createDecisionCollection(choices, new IDecision[] { dNewAdds, dNameSort });
	        this.decisions[2] = dColl;

        } catch (Throwable t) {
            String msg = "SelectGroupQuery failed to build its decision collection "
                                            + "for Paging.";
            throw new RuntimeException(msg, t);
        }    
        
        return decisions;
    }
    
    private ICivisEntity[] sortSharees(ICivisEntity[] sharees) {
    	// Assertions.
    	if (sharees == null) {
    		throw new IllegalArgumentException("SelectGroupQuery: groups " +
    				"must first be intialized.");
    	}
    	if(buc.getEditPreferences().sortAsc() == true){
    	    Arrays.sort(sharees, new CivisEntityAscComparator());
    	}else{
    	    Arrays.sort(sharees, new CivisEntityDescComparator());
    	}
    	return sharees;
    }
    
    private String getEntryName(ICivisEntity e){
        StringBuffer rslt = new StringBuffer();
        
        if (e instanceof IGroup){
            rslt.append(((IGroup)e).getName());
        }else{
            IDecisionCollection dColl = e.getAttributes();
            rslt.append((String)dColl.getDecision("lName")
    		        .getFirstSelectionValue());
            rslt.append(", ");
            rslt.append((String)dColl.getDecision("fName")
    		        .getFirstSelectionValue());
            
        }            
        return rslt.toString();
    }

}
