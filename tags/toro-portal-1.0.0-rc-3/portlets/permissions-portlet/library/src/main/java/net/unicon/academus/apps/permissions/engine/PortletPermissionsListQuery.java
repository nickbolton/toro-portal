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

package net.unicon.academus.apps.permissions.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.permissions.CivisAccessEntry;
import net.unicon.academus.apps.permissions.PermissionsUserContext;
import net.unicon.academus.apps.permissions.TypeGrantDenyDefer;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.CivisEntityAscComparator;
import net.unicon.civis.CivisEntityDescComparator;
import net.unicon.civis.ICivisEntity;
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
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.WarlockException;

public class PortletPermissionsListQuery extends InitialQuery{

    // Instance Members.
    ErrorMessage[] errors;
    ICivisEntity[] entries;

    /**
     * @param puc
     */
    public PortletPermissionsListQuery(PermissionsUserContext puc) {
        super(puc);

        // Instance Members.
        this.entries = null;

    }

    public PortletPermissionsListQuery(PermissionsUserContext puc, ErrorMessage[] errors) {
        super(puc, errors);

        // Instance Members.
        this.entries = null;

    }

    public String query() throws WarlockException {

        StringBuffer rslt = new StringBuffer();

        rslt.append("<state>");
        super.commonQueries(rslt);

        // add the selections from the civisSelector
        if(puc.getEntitySelection().length > 0){
            ICivisEntity[] entities = puc.getEntitySelection();
            for(int i = 0; i < entities.length; i++){
                puc.addCivisAccessEntry(new CivisAccessEntry(entities[i].getUrl(), new AccessRule[0]));
            }

            puc.setEntitySelection(new ICivisEntity[0]);
        }

        // add the information about the users/groups
        entries = puc.getCivisEntities();

        // filter the entries
        entries = nameFilter();

        // filter based on the view Filter
        entries = viewFilter();

        // sort the entries
        if (puc.isNameSortAsc()){
            Arrays.sort(entries, new CivisEntityAscComparator());
        } else{
            Arrays.sort(entries, new CivisEntityDescComparator());
        }

        // Paging.
        int perpage = puc.getPermissionsPaging().getItemsPerPage();
        int currentPage = puc.getPermissionsPaging().getCurrentPage();
        int totalPages = 0;
        if (perpage != PagingState.SHOW_ALL_ITEMS)
            totalPages = (int)Math.ceil((double)entries.length / (double)perpage);
        totalPages = (totalPages > 0 ? totalPages : 1);

        if (currentPage == PagingState.LAST_PAGE || currentPage > totalPages) {
            currentPage = totalPages;
            puc.getPermissionsPaging().setCurrentPage(totalPages);
        }

        int firstItem = (currentPage-1) * perpage + 1;
        int lastItem = firstItem + perpage - 1;
        if (lastItem > entries.length || perpage == PagingState.SHOW_ALL_ITEMS)
            lastItem = entries.length;

        rslt.append("<privileged currentpage=\"")
            .append(currentPage)
            .append("\" perpage=\"")
            .append(perpage)
            .append("\" totalpages=\"")
            .append(totalPages)
            .append("\" firstdisplayed=\"")
            .append(firstItem)
            .append("\" lastdisplayed=\"")
            .append(lastItem)
            .append("\" totalitems=\"")
            .append(entries.length)
            .append("\">");

        for (int i = firstItem - 1; i < lastItem; i++) {
            if(entries[i] instanceof IPerson){
                rslt.append("<member id=\"")
                    .append(entries[i].getUrl())
                    .append("\"><name>")
                    .append(getEntryName(entries[i]))
                    .append("</name> </member>");
            }else{
                rslt.append("<group id=\"")
                .append(entries[i].getUrl())
                .append("\"><name>")
                .append(getEntryName(entries[i]))
                .append("</name> </group>");
            }
        }

        rslt.append("</privileged>");

        // add the information about the permissions
        rslt.append("<permissions>");
        AccessType[] accessTypes = puc.getPortletAccessSelection().getAccessTypes();

        for(int i = 0; i < accessTypes.length; i++){
        rslt.append("<permission handle=\"")
            .append(accessTypes[i].toInt())
            .append("\"> <label>")
            .append(accessTypes[i].getName())
            .append("</label> <description>")
            .append(accessTypes[i].getDescription())
            .append("</description> </permission>");
        }
        rslt.append("</permissions>");

        rslt.append("</state>");

        //System.out.println("PermissionsQuery " + rslt.toString());
        return rslt.toString();
    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        List rslt = new ArrayList();
        IEntityStore store = new JvmEntityStore();

        // Choice Collection:  editPermissionForm.
        try {
            // Sorting.
            boolean nameSortAsc = puc.isNameSortAsc();
            String direction = nameSortAsc ? "asc" : "des";
            IOption oSort = store.createOption(Handle.create(direction), null, TypeNone.INSTANCE);
            IChoice cSort = store.createChoice(Handle.create("nameSortDirection"), null, new IOption[] { oSort }, 0, 1);
            ISelection sSort = store.createSelection(oSort, TypeNone.INSTANCE.parse(null));
            IDecision dSort = store.createDecision(null, cSort, new ISelection[] { sSort });

            // Items Per Page.
            int ipp = puc.getPermissionsPaging().getItemsPerPage();
            IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);
            ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });

            // View filter
            String view = puc.getViewSelection();
            IOption oViewFilter = store.createOption(Handle.create(view), null, TypeNone.INSTANCE);
            IChoice cViewFilter = store.createChoice(Handle.create("viewFilter"), null, new IOption[] { oViewFilter }, 0, 1);
            ISelection sViewFilter = store.createSelection(oViewFilter, TypeNone.INSTANCE.parse(null));
            IDecision dViewFilter = store.createDecision(null, cViewFilter, new ISelection[] { sViewFilter });

            // nameFilter
            String charSel = puc.getCharSelection();
            IOption oNameFilter = store.createOption(Handle.create(charSel), null, TypeNone.INSTANCE);
            IChoice cNameFilter = store.createChoice(Handle.create("chooseNameFilter"), null, new IOption[] { oNameFilter }, 0, 1);
            ISelection sNameFilter = store.createSelection(oNameFilter, TypeNone.INSTANCE.parse(null));
            IDecision dNameFilter = store.createDecision(null, cNameFilter, new ISelection[] { sNameFilter });

            // permission per user
            Map sMap = new HashMap();                   // map of selection list by choice handle
            String identity = null;
            AccessRule[] accessRules = null;
            IChoice cPerm;
            IOption oPerm;
            ISelection sPerm;
            List selList = null;
            List oList = new ArrayList();
            String cHandle = null;
            List aTypes; // list of accessTypes not present in the accessRules
            for(int i = 0; i < entries.length; i++){
                aTypes = new ArrayList(Arrays.asList(puc.getPortletAccessSelection().getAccessTypes()));
                identity = entries[i].getUrl();
                accessRules = puc.getCivisEntryAccessRules(entries[i].getUrl());
                oPerm = store.createOption(Handle.create(identity), null, TypeGrantDenyDefer.INSTANCE);
                oList.add(oPerm);
                for(int j = 0; j < accessRules.length; j++){
                    cHandle = "PermittedItems_" + accessRules[j].getAccessType().toInt();
                    aTypes.remove(accessRules[j].getAccessType());
                    selList = (List)sMap.get(cHandle);
                    if(selList == null){
                        selList = new ArrayList();
                    }
                    sPerm = store.createSelection(oPerm
                            , TypeGrantDenyDefer.INSTANCE.parse(accessRules[j].getStatus() ? "grant" : "deny"));
                    selList.add(sPerm);
                    sMap.put(cHandle, selList);
                }
                // add information for accessTypes that don't have an accessRule
                for(int j = 0; j < aTypes.size(); j++){
                    cHandle = "PermittedItems_" + ((AccessType)aTypes.get(j)).toInt();

                    selList = (List)sMap.get(cHandle);
                    if(selList == null){
                        selList = new ArrayList();
                    }
                    // TODO have to specify the GRANT/DENY fro each accessType
                    sPerm = store.createSelection(oPerm, TypeGrantDenyDefer.INSTANCE.parse("defer"));
                    selList.add(sPerm);
                    sMap.put(cHandle, selList);
                }
            }

            Iterator it = sMap.keySet().iterator();
            List decisions = new ArrayList();
            List cList = new ArrayList();

            while(it.hasNext()){
                cHandle = (String)it.next();
                cPerm = store.createChoice(Handle.create(cHandle),
                        Label.create(""), (IOption[])oList.toArray(new IOption[0]),
                        0, 0);
                selList = (List)sMap.get(cHandle);
                cList.add(cPerm);
                decisions.add(store.createDecision(null
                        , cPerm
                        , (ISelection[])selList.toArray(new ISelection[0])));
            }

            // Choices.
            Handle h = Handle.create("editPermissionForm");
            cList.add(cNumItems);
            cList.add(cSort);
            cList.add(cNameFilter);
            cList.add(cViewFilter);
            IChoiceCollection choices = store.createChoiceCollection(h, null,
                    (IChoice[])cList.toArray(new IChoice[0]));

            decisions.add(dNumItems);
            decisions.add(dSort);
            decisions.add(dNameFilter);
            decisions.add(dViewFilter);

            IDecisionCollection dColl = store.createDecisionCollection(choices,
                    (IDecision[]) decisions.toArray(new IDecision[0]));

            rslt.add(dColl);

        } catch (Throwable t) {
            throw new RuntimeException(
                    "PortletPermissionsListQuery failed to build its decision collection "
                    + "for editPermissionForm.", t);
        }

        return (IDecisionCollection[]) rslt.toArray(new IDecisionCollection[0]);
    }

    /*
     * private methods
     */
    private ICivisEntity[] nameFilter() {
        String charSel = puc.getCharSelection();

        List filtered = new LinkedList();

        if (charSel.equalsIgnoreCase("All")) {
            return entries;
        } else if (charSel.equalsIgnoreCase("XYZ")) {
            String c1 = String.valueOf(charSel.charAt(0));
            String c2 = String.valueOf(charSel.charAt(1));
            String c3 = String.valueOf(charSel.charAt(2));

            String id = null;
            for (int i = 0; i < entries.length; i++) {
                id = getEntryName(entries[i]);
                if ((id.toLowerCase().indexOf(c1.toLowerCase()) == 0)
                  ||(id.toLowerCase().indexOf(c2.toLowerCase()) == 0)
                  ||(id.toLowerCase().indexOf(c3.toLowerCase()) == 0)) {
                    filtered.add(entries[i]);
                }
            }

        } else {
            String c = String.valueOf(charSel.charAt(0));
            String id = null;
            for (int i = 0; i < entries.length; i++) {
                id = getEntryName(entries[i]);
                if (id.toLowerCase().indexOf(c.toLowerCase()) == 0) {
                    filtered.add(entries[i]);
                }
            }

        }
        return (ICivisEntity[])filtered.toArray(new ICivisEntity[0]);
    }

    private String getEntryName(ICivisEntity e){
        StringBuffer rslt = new StringBuffer();

        if (e instanceof IGroup){
            rslt.append(((IGroup)e).getPath());
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

    private ICivisEntity[] viewFilter(){

        List filtered;

        if(puc.getViewSelection().equalsIgnoreCase("all")){
            return entries;
        }else {
            if (puc.getViewSelection().equalsIgnoreCase("active")){
                filtered = new LinkedList();
                for(int i = 0; i < entries.length; i++){
                    if(puc.getCivisEntryAccessRules(entries[i].getUrl()).length > 0){
                        filtered.add(entries[i]);
                    }
                }
            } else{
                filtered = new LinkedList();
                for(int i = 0; i < entries.length; i++){
                    if(puc.getCivisEntryAccessRules(entries[i].getUrl()).length == 0){
                        filtered.add(entries[i]);
                    }
                }
            }

            return (ICivisEntity[])filtered.toArray(new ICivisEntity[0]);
        }
    }


}
