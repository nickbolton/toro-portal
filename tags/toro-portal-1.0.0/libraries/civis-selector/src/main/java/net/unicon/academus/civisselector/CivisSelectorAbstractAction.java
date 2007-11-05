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
import java.util.Iterator;
import java.util.List;

import net.unicon.penelope.EntityCreateException;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.fac.AbstractWarlockFactory;

public abstract class CivisSelectorAbstractAction extends AbstractWarlockFactory
                                            .AbstractAction {
    
    protected CivisSelectorAbstractAction(IWarlockFactory owner, Handle handle, String[] choices) {
        super(owner, handle, choices);
    }
    
    protected void saveSelections(IDecisionCollection dc, ISelectorUserContext suc){
    	IChoiceCollection cc = dc.getChoiceCollection();
    	
//System.out.println("Decision Collection " + dc.toXml());
        
    	IChoice c = cc.getChoice(net.unicon.penelope.Handle.create("selectedItems"));
        Iterator itc = Arrays.asList(c.getOptions()).iterator();
        
        IDecision d = dc.getDecision(cc.getChoice(net.unicon.penelope.Handle.create("selectedItems")));
        Iterator it = Arrays.asList(d.getSelections()).iterator();
        
        List options = new ArrayList(Arrays.asList(c.getOptions()));
       
        // get all the selections
        List addedEntities = new ArrayList();
        List removeEntities = new ArrayList();
        while (it.hasNext()) {
            
            ISelection s = (ISelection) it.next();
            String id = s.getOption().getHandle().getValue();
            
            // if selection contains this option add it to temporary storage
            // else remove from temporary storage
            if(options.contains(s.getOption())){
                addedEntities.add(EntityType.getEntity(id));
                options.remove(s.getOption());
            }            
        }        
        
        
        it = options.iterator();
        while(it.hasNext()){
            IOption o = (IOption)it.next();
            IEntity e = EntityType.getEntity(o.getHandle().getValue());
            suc.getSelectorWorkflow().getTempSelectionBasket().removeSelection(e);
        }
        
        suc.getSelectorWorkflow().getTempSelectionBasket()
        .addEntitySelection((IEntity[])addedEntities.toArray(new IEntity[0]));
        
    }
 

}
