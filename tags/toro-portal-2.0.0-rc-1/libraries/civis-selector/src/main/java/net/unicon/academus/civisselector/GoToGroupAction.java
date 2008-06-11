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

import net.unicon.civis.IGroup;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
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

public final class GoToGroupAction extends CivisSelectorAbstractAction {

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

        return new GoToGroupAction(owner, handle, choices);

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
        SelectorWorkflow workflow = suc.getSelectorWorkflow();

        String groupId = "";
        // Find the target.
        // check to see if we are coming from the group screens
        // or another screen
        if(decisions.length > 0){
	        IDecisionCollection d_mff = decisions[0];
	        IChoiceCollection c_mff = d_mff.getChoiceCollection();
	        IDecision cl = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("selectedItemNavigate")));

	        if(cl.getSelections().length > 0){
	            groupId = cl.getSelections()[0].getOption().getHandle().getValue();
	        }else{
	            cl = d_mff.getDecision(c_mff.getChoice(net.unicon.penelope.Handle.create("chooseLocation")));
	            if(cl.getSelections().length > 0){
	            	groupId = cl.getSelections()[0].getOption().getHandle().getValue();
	    		}
	        }
	        
	        saveSelections(d_mff, suc);
	        
        }else{
		    if(workflow.getGroupHistory().hasGroup()){
		        groupId = GroupEntity.getEntityId(workflow.getGroupHistory().getLocation());
		    }else{
		        groupId = workflow.getGroupHistory().getPath();
		    }
        }
        if(!groupId.equals("") && !groupId.equals("Personal")
                && !groupId.equals("All")){
	        IGroup g;
	        g = (IGroup)EntityType.getEntity(groupId).getObject();
	        // Adjust the history and respond.
	        workflow.getGroupHistory().setLocation(g);
        }else {
            workflow.getGroupHistory().setPath(groupId);
            workflow.getGroupHistory().clear();
        }

        return new SimpleActionResponse(screen, new SelectGroupQuery(suc));

    }

    /*
     * Package API.
     */

    private GoToGroupAction(IWarlockFactory owner, Handle handle,
                                            String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;

    }

}