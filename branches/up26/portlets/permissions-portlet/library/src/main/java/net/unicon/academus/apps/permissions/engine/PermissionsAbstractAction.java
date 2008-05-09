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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.unicon.academus.apps.permissions.PermissionsUserContext;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.fac.AbstractWarlockFactory;

public abstract class PermissionsAbstractAction extends AbstractWarlockFactory
                                            .AbstractAction {
    
    protected PermissionsAbstractAction(IWarlockFactory owner, Handle handle, String[] choices) {
        super(owner, handle, choices);
    }
    
    protected void updatePermissions(IDecisionCollection dc, PermissionsUserContext puc){
        IChoiceCollection cc = dc.getChoiceCollection();
        AccessType[] aType = puc.getPortletAccessSelection().getAccessTypes();
        int totalAccessTypes = aType.length;
        
        IChoice[] c = new IChoice[totalAccessTypes];
        IDecision[] d = new IDecision[totalAccessTypes];
        
        try{
	        Handle h = null;
	        for(int i = 0; i < totalAccessTypes; i++){            
	            c[i] = cc.getChoice("PermittedItems_" + aType[i].toInt());
	            d[i] = dc.getDecision(c[i]);
	        }
        }catch (Exception e){
            // choice collection does not contain the required choices.
            return;
        }
        
        // set up a map of all the entities and their access rules
        Map accessEntry = new HashMap();
        Iterator itc = Arrays.asList(c[0].getOptions()).iterator();
        
        while(itc.hasNext()){
            accessEntry.put(((IOption)itc.next()).getHandle().getValue(), new LinkedList());
            
        }
        
        // fill the map from selections in the
        
        for(int i = 0; i < totalAccessTypes; i++){
            Iterator it = Arrays.asList(d[i].getSelections()).iterator();
            while (it.hasNext()) {	           
 	           ISelection s = (ISelection) it.next();
 	           Object status = s.getComplement().getValue();
 	           if(!status.equals("defer")){
 	              String entityId = s.getOption().getHandle().getValue();
 	           ((List)accessEntry.get(entityId)).add(new AccessRule(aType[i]
 	                         , status.equals("grant")));
 	           }
 	       }
        }        
        
        // update the permissions in the current selection
        Iterator it = accessEntry.keySet().iterator();
        while (it.hasNext()){
            String entityId = (String)it.next();
            puc.setCivisEntryAccessRules(entityId
                    , (AccessRule[])((List)accessEntry.get(entityId))
                    .toArray(new AccessRule[0]));
        }

    }
 

}
