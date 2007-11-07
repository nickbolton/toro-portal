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

package net.unicon.academus.apps.messaging.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.civis.ICivisEntity;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.Priority;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.ISelection;
import net.unicon.warlock.Handle;
import net.unicon.warlock.IAction;
import net.unicon.warlock.IActionResponse;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.IWarlockFactory;
import net.unicon.warlock.StateMachine;
import net.unicon.warlock.WarlockException;
import net.unicon.warlock.XmlFormatException;
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class ComposeMessageAction extends AbstractWarlockFactory
                                            .AbstractAction {

    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screen;
    private final String mode;

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
            String msg = "Argument 'owner' cannot be null.";
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
            choices = p.getValue().split(",");
        }

        // Mode (Edit or Preview).
        String mode = "edit";
        p = e.attribute("mode");
        if (p != null) {
            mode = p.getValue().toLowerCase();
        }

        return new ComposeMessageAction(owner, handle, choices, mode);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("message_compose"));
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

        MessagingUserContext muc = (MessagingUserContext)ctx;
        String msg = null;
        String mode = this.mode;

        ErrorMessage error = null;

        if (decisions.length > 0) {
            IDecisionCollection d = decisions[0];
            
            error = handleComposeDecisions(muc, d);
            if (error == null) {
                DraftMessage draft = muc.getDraft();
                try {
                    // Attachment removal
                    if (mode.equals("attachment_remove")) {
                        draft.removeAttachment(
                                Integer.parseInt(
                                    d.getDecision("removeAttachment")
                                    .getFirstSelectionHandle()));
                        mode = "edit";
                    }

                    // Recipient removal
                    if (mode.equals("recipient_remove")) {
                        ISelection[] s = d.getDecision("cRecipients")
                            .getSelections();
                        int[] sels;
                        ICivisEntity[] recipients; 
                        if(s.length > 0){
                            if(s.length > 1){
	                            sels = new int[s.length];
		                        List entityList = new ArrayList(Arrays.asList(muc.getPrevEntitySelection()));
		                        for (int i = 0; i < s.length; i++) {
		                            sels[i] = Integer.parseInt((s[i].getOption()
		                                    .getHandle().getValue()));                            
		                        }
		                        Arrays.sort(sels);
	                        }else{
	                            sels = new int[]{Integer.parseInt((s[0].getOption()
	                                    .getHandle().getValue()))};
	                        }
                            
                            recipients = new ICivisEntity[muc.getPrevEntitySelection().length - sels.length];
                            ICivisEntity[] entities = muc.getPrevEntitySelection();
                            int selIndex = 0;
                            int rIndex = 0;
                            for(int i = 0; i < entities.length; i++){
                                if(selIndex < sels.length && sels[selIndex] == i){
                                    selIndex++;
                                }else{
                                    recipients[rIndex++] = entities[i];
                                }
                            }
                            muc.setPrevEntitySelection(recipients);
                        }   
                        mode = "edit";
                    }
                } catch (Exception e) {
                    // TODO: Log exception!!!
                    e.printStackTrace(System.err);
                    error = new ErrorMessage("other",
                            "Error parsing decisions. "+e.getMessage(),
                            "Please contact a Portal administrator for assistance.");
                }
            }
        }

        IActionResponse rslt = null;
        if (error == null)
            rslt = new SimpleActionResponse(screen, new ComposeMessageQuery(muc, mode));
        else
            rslt = new SimpleActionResponse(screen, new ComposeMessageQuery(muc, new ErrorMessage[] { error }, mode));
        return rslt;

    }

    /*
     * Package API.
     */

    static ErrorMessage handleComposeDecisions(MessagingUserContext muc, IDecisionCollection d) {
        // Save the Subject, Body, and Priority from decision
        //
        // cRecipients will contain recipients to remove, if mode is remove
        // recips. Ignore otherwise.
        //
        // cAttachments will contain attachments to remove, if modes remove
        // attachment. Ignore otherwise.
        ErrorMessage rslt = null;
        DraftMessage draft = muc.getDraft();
        String buf = null;
        IDecision de = null;

        try {
            de = d.getDecision("cBody");
            if (de != null) {
                buf = (String)de.getFirstSelectionValue();
                if (buf != null) {
                    draft.setBody(buf);
                }
            }

            de = d.getDecision("cSubject");
            if (de != null) {
                buf = (String)de.getFirstSelectionValue();
                if (buf != null)
                    draft.setSubject(buf);
            }

            de = d.getDecision("cPriority");
            if (de != null) {
                buf = de.getFirstSelectionHandle();
                if (buf != null)
                    draft.setPriority(Priority.getInstance(Integer.parseInt(buf)));
            }

            de = d.getDecision("cEmail");
            if (de != null) {
                buf = de.getFirstSelectionHandle();
                if (buf != null) {
                    // Must send by email, too.
                    muc.setDraftSendEmailCopy("yes".equals(buf));
                }
            }

        } catch (Exception e) {
            // TODO: Log exception!!!
            e.printStackTrace(System.err);
            rslt = new ErrorMessage("other",
                    "Error parsing decisions. "+e.getMessage(),
                    "Please contact a Portal administrator for assistance.");
        }

        return rslt;
    }

    private ComposeMessageAction(IWarlockFactory owner, Handle handle, String[] choices, String mode) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.app = null;
        this.mode = mode;
    }

}
