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
import java.util.StringTokenizer;

import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class AddToSelBasketAction extends AbstractWarlockFactory
                                                .AbstractAction {

    // Instance Members.    
    private String toScreen;
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
        
        // ToScreen.
        Attribute s = e.attribute("to-screen");
        if (s == null) {
            String msg = "Element <action> is missing required attribute "
                                                        + "'to-screen'.";
            throw new XmlFormatException(msg);
        }
        String toScreen = s.getValue();

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

        return new AddToSelBasketAction(owner, handle, choices, toScreen);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
       
        screen = m.getScreen(Handle.create(toScreen));

    }

    public IActionResponse invoke(IDecisionCollection[] decisions,
                    IUserContext ctx) throws WarlockException {

        // Assertions.
        if (decisions == null) {
            String msg = "Argument 'decisions' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (decisions.length != 1) {
            String msg = "Argument 'decisions' must contain exactly one "
                                                        + "element.";
            throw new IllegalArgumentException(msg);
        }
        if (ctx == null) {
            String msg = "Argument 'ctx' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        ISelectorUserContext suc = (ISelectorUserContext) ctx;

        // Find the slected items.
        IDecisionCollection dCol = decisions[0];
        IDecision d = dCol.getDecision("selectedItems");
        Iterator it = Arrays.asList(d.getSelections()).iterator();
        List entities = new ArrayList();
        
        // get a list of all the sharees
        while (it.hasNext()) {
            
            ISelection s = (ISelection) it.next();
            String id = s.getOption().getHandle().getValue();
            
            entities.add(EntityType.getEntity(id));            
        }
        
        suc.getSelectorWorkflow().getSelectionBasket()
        .addEntitySelection((IEntity[])entities.toArray(new IEntity[0]));
        
        // add the users from the remporary selection basket
        suc.getSelectorWorkflow().getSelectionBasket()
        .addEntitySelection(suc.getSelectorWorkflow().getTempSelectionBasket().getEntities());
        
        suc.getSelectorWorkflow().getTempSelectionBasket().clearEntitySelection();

        IStateQuery query = null;
        
        // decide which screen we are coming from
        if(toScreen.equals("select_groups")){	        
	        query = new SelectGroupQuery(suc);
        }else if(toScreen.equals("select_basket")){
            query = new SelectBasketQuery(suc);
        }else if(toScreen.equals("select_members")){
            query = new SelectMemberQuery(suc);
        }else if(toScreen.equals("select_search_results")){
            query = new SearchShareeQuery(suc);
        }
        
        if(query != null){
            return new SimpleActionResponse(screen, query);
        }else{
            throw new RuntimeException("Add To Selection Basket was not able to find a screen to go to.");
        }        

    }

    /*
     * Package API.
     */

    private AddToSelBasketAction(IWarlockFactory owner, Handle handle,
                                            String[] choices, String toScreen) {

        super(owner, handle, choices);

        // Instance Members.   
        this.screen = null;
        this.toScreen = toScreen;

    }

}
