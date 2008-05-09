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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.fac.AbstractWarlockFactory;

public abstract class BriefcaseAbstractAction extends AbstractWarlockFactory
                                            .AbstractAction {
    
    protected BriefcaseAbstractAction(IWarlockFactory owner, Handle handle, String[] choices) {
        super(owner, handle, choices);
    }
    
    protected void updatePermissions(IDecisionCollection dc, BriefcaseUserContext buc){
    	IChoiceCollection cc = dc.getChoiceCollection();
        IChoice c = cc.getChoice(net.unicon.penelope.Handle.create("selectedItems"));
        Iterator itc = Arrays.asList(c.getOptions()).iterator();
        
        // map of all the sharees and their accessTypes
        Map accessEntry = new HashMap();
        while(itc.hasNext()){
            accessEntry.put(((IOption)itc.next()).getHandle().getValue(), new LinkedList());                        
        }  
        
        IDecision d = dc.getDecision(cc.getChoice(net.unicon.penelope.Handle.create("readPermittedItems")));
        Iterator it = Arrays.asList(d.getSelections()).iterator();
       
        // get all the sharees with read permissions
        while (it.hasNext()) {
            
            ISelection s = (ISelection) it.next();
            String shareeId = s.getOption().getHandle().getValue();
            ((List)accessEntry.get(shareeId)).add(new AccessRule(BriefcaseAccessType.VIEW, true));                        
        }
        
        d = dc.getDecision(cc.getChoice(net.unicon.penelope.Handle.create("writePermittedItems")));
        it = Arrays.asList(d.getSelections()).iterator();
        
        // get a list of all the sharees with write permissions
        while (it.hasNext()) {
            
            ISelection s = (ISelection) it.next();
            String shareeId = s.getOption().getHandle().getValue();
            ((List)accessEntry.get(shareeId)).add(new AccessRule(BriefcaseAccessType.ADD, true));                         
        }
        
        d = dc.getDecision(cc.getChoice(net.unicon.penelope.Handle.create("deletePermittedItems")));
        it = Arrays.asList(d.getSelections()).iterator();
        
        // get a list of all the sharees with delete permissions
        while (it.hasNext()) {
            
            ISelection s = (ISelection) it.next();
            String shareeId = s.getOption().getHandle().getValue();
            ((List)accessEntry.get(shareeId)).add(new AccessRule(BriefcaseAccessType.DELETE, true));                         
        }
        
        // update the permissions in the current selection
        it = accessEntry.keySet().iterator();
        while (it.hasNext()){
            String entityId = (String)it.next();
            buc.getTargetSelection().updateAccessRule(entityId
                    , (AccessRule[])((List)accessEntry.get(entityId))
                    .toArray(new AccessRule[0]));
        }

    }
 

}
