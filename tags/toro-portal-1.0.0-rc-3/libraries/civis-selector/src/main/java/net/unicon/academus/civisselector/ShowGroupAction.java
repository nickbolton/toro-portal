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

import java.util.StringTokenizer;

import net.unicon.civis.ICivisFactory;
import net.unicon.civis.grouprestrictor.IGroupRestrictor;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class ShowGroupAction extends CivisSelectorAbstractAction {

    // Instance Members.
    private IScreen screen;          

    /*
     * Public API.
     */

    public static IAction parse(Element e, IWarlockFactory owner)
                                    throws WarlockException {

        // Assertions.
        if (e == null) {
            String msg = "Argument 'e [Element]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!e.getName().equals("action")) {
            String msg = "Argument 'e [Element]' must be an <action> element.";
            throw new IllegalArgumentException(msg);
        }
        if (owner == null) {
            String msg = "Argument 'owner+' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Handle.
        Attribute h = e.attribute("handle");
        if (h == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'handle'.";
            throw new XmlFormatException(msg);
        }
        Handle handle = Handle.create(h.getValue());

        // Choices.
        String[] choices = new String[0];
        Attribute p = e.attribute("inpt");
        if (p != null) {
            StringTokenizer tokens = new StringTokenizer(p.getValue(), ",");
            choices = new String[tokens.countTokens()];
            for (int i=0; tokens.hasMoreTokens(); i++) {
                choices[i] = tokens.nextToken();
            }
        }

        return new ShowGroupAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        screen = m.getScreen(Handle.create("select_groups"));        
    }

    public IActionResponse invoke(IDecisionCollection[] decisions,
                    IUserContext ctx) throws WarlockException {

        // Assertions.
        if (decisions == null) {
            String msg = "Argument 'decisions' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (ctx == null) {
            String msg = "Argument 'ctx' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        ISelectorUserContext suc = (ISelectorUserContext) ctx;
        
        initializeSelectorWorkflow(suc);
        
        // if the previous screen was search, clean temporary selection.
		if(suc.getSelectorWorkflow().getLastScreen().equals("select_search_results")){
		    suc.getSelectorWorkflow().getTempSelectionBasket().clearEntitySelection();
		}
        
		if(decisions.length > 0){
		    saveSelections(decisions[0], suc);
		}
		
		// set the last screen to the group screen
		suc.getSelectorWorkflow().setLastScreen(screen.getHandle().getValue());
		
        return new SimpleActionResponse(screen, new SelectGroupQuery(suc));

    }

    /*
     * Package API.
     */

    private ShowGroupAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;

    }
    
    static void initializeSelectorWorkflow(ISelectorUserContext suc){
        
        //      initialize the workflow 
        if(suc.getSelectorWorkflow() == null){
	        suc.setSelectorWorkflow(new SelectorWorkflow());
	        ICivisFactory[] factories = suc.getCivisFactories();
	        
	        // set the default Civis Factory selection to the first one in the list
	        if(factories != null && factories.length == 0){
	            throw new RuntimeException("No valid Civis Factories have been assigned.");
	        }else{
	            suc.getSelectorWorkflow().setSelectedFactory(factories[0]);
	        }        
	        
	        // set the default Civis group Restrictor selection to the first one in the list
	        if(suc.getGroupRestrictors() != null && suc.getGroupRestrictors().isEmpty()){
	            throw new RuntimeException("No valid Civis Group Restrictors " +
	            		"have been assigned for the selected factory.");
	        }else{
	            suc.getSelectorWorkflow().setSelectedRestrictor(
	                    (IGroupRestrictor)suc.getGroupRestrictors().get(factories[0].getUrl()));
	        }    
        }
    }

}