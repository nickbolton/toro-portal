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
import java.util.Iterator;
import java.util.List;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.MessagingApplicationContext;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.civis.CivisRuntimeException;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.EntityType;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.fac.email.EmailMessageFactory;
import net.unicon.mercury.fac.email.EmailRecipientType;
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
import net.unicon.warlock.fac.AbstractWarlockFactory;
import net.unicon.warlock.fac.SimpleActionResponse;

import org.dom4j.Attribute;
import org.dom4j.Element;

public final class SendMessageAction extends AbstractWarlockFactory
                                            .AbstractAction {

    private static final String UNDISCLOSED_RECIPIENTS_LABEL =
            "Undisclosed-Recipients";
    
    // Instance Members.
    private MessagingApplicationContext app;
    private IScreen screen;
    private IScreen screenError;

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
        if (p == null)
            throw new XmlFormatException(
                    "ComposeNavigateAction requires attribute 'inpt'.");
        choices = p.getValue().split(",");

        return new SendMessageAction(owner, handle, choices);

    }

    public void init(StateMachine m) {

        // Assertions.
        if (m == null) {
            String msg = "Argument 'm [StateMachine]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        app = (MessagingApplicationContext) m.getContext();
        screen = m.getScreen(Handle.create("message_list"));
        screenError = m.getScreen(Handle.create("message_compose"));
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
        ErrorMessage error = null;

        if (decisions.length > 0) {
            // Save the Subject, Body, and Priority from decision
            // cRecipients will contain recipients to remove, if action is remove recips. Ignore otherwise.
            // cAttachments will contain attachments to remove, if action is remove attachment. Ignore otherwise.
            IDecisionCollection d = decisions[0];
            error = ComposeMessageAction.handleComposeDecisions(muc, d);            
        }
        
        DraftMessage draft = muc.getDraft();
        boolean notFailure = false;
        DraftMessage emailDraft = new DraftMessage(draft);
        // System email is the source for email Notifications
        IMessageFactory systemEmail = app.getSystemEmail();

        if (error == null) {
            // add the recipients to the draft
            // TODO have to figure out a way to handle the different types of recipients. 
            // This could be read a DC from the screen
            // will be getting the recipient list from muc.getPrevEntitySelection()    
            draft.removeRecipients();
            ICivisEntity[] entities = muc.getPrevEntitySelection();
            IDecisionCollection dColl = null;
            StringBuffer name = new StringBuffer();
            String email = "";
            List failed = new ArrayList();
            
            // set up the email recipients too 
            boolean sendEmail = muc.getDraftSendEmailCopy();
            ICivisFactory addressbook = app.getAddressBook();
            
            String recipientType = null;
            if (entities.length == 1 && (entities[0] instanceof IPerson)) {
                // Only one recipient (not a group), so send in "TO" field
                recipientType = EmailRecipientType.TO.getLabel();
            }
            else { // Add System email as the lone TO recipient, actual 
                   // notification recipients will be BCC'd for obscurity. 
                   // See JIRA Issue AC-344
                
                email = ((EmailMessageFactory)systemEmail).getPreference(
                        "mail.from");
                emailDraft.addRecipient(EmailRecipientType.TO.getLabel(),
                        UNDISCLOSED_RECIPIENTS_LABEL, email, EntityType.USER);
                
                // All other recipients will be BCC'd
                recipientType = EmailRecipientType.BCC.getLabel();
            }
            
            for (int i = 0; i < entities.length; i++) {
                try{
		            if (entities[i] instanceof IPerson){
	                    dColl = entities[i].getAttributes();
	                    name.append(dColl.getDecision("lName").getFirstSelectionValue())
	                    	.append(", ")
	                    	.append(dColl.getDecision("fName").getFirstSelectionValue());
	                    draft.addRecipient("to", name.toString()
	                            , entities[i].getName(), EntityType.USER);
	                    
	                    if(sendEmail){
	                        dColl = addressbook.getPerson(entities[i].getName()).getAttributes();
		                    email = (String)dColl.getDecision("email")
		                                                .getFirstSelectionValue();
		
		                    emailDraft.addRecipient(recipientType, 
                                    name.toString(), email, EntityType.USER);
	                    }
	                    name.setLength(0);
		                
		            }else{
		                draft.addRecipient("to", ((IGroup)entities[i]).getName()
	                            , ((IGroup)entities[i]).getPath(), EntityType.GROUP);
		                
		                if(sendEmail){
			                IPerson[] persons = addressbook
			                	.getGroupByPath(((IGroup)entities[i]).getPath())
			                	.getMembers(true);
		                    for(int j = 0; j < persons.length; j++){
		                        dColl = persons[j].getAttributes();
		                        name.append(dColl.getDecision("lName").getFirstSelectionValue())
		                        	.append(", ")
		                        	.append(dColl.getDecision("fName").getFirstSelectionValue());
		                        
		                        email = (String)dColl.getDecision("email")
			                                                .getFirstSelectionValue();
			
			                    emailDraft.addRecipient(recipientType,
                                        name.toString(), email, 
                                        EntityType.USER);
		                        name.setLength(0);
		                    }
		                }
	                    
		            }    
                } catch (CivisRuntimeException e) {
                    // TODO: Log exception!!!
                    e.printStackTrace(System.err);

                    // Add it to the failed list.
                    failed.add(name);
                }catch(Exception e){
                    e.printStackTrace(System.err);
                    error = new ErrorMessage("other",
                            "Unable to send email/notification copy." +
                           "Please see the error message : " + e.getMessage(), "");
                }
	        }
            
            if (!failed.isEmpty()) {
                StringBuffer buf = new StringBuffer();

                Iterator it = failed.iterator();
                while (it.hasNext()) {
                    buf.append(", ")
                       .append((String)it.next());
                }

                // Remove the initial ", ".
                buf.delete(0, 2);

                notFailure = true; // Warning, not failure.
                error = new ErrorMessage("other",
                                "Unable to send email copy to the following recipients: "
                              + buf.toString(),
                                "The notification was still sent to these "
                              + "recipients, but not the requested email "
                              + "copy.");
                
                failed.clear();
            }
            error = validate(draft);
        }

        if (error == null) {
            try {
                muc.getFactorySelection().getFactory().sendMessage(draft);
                // clear out the selections once the message is sent
                muc.setPrevEntitySelection(new ICivisEntity[0]);
                muc.setEntitySelection(new ICivisEntity[0]);
                
            } catch (Exception e) {
                // TODO: Log exception!!!
                e.printStackTrace(System.err);
                error = new ErrorMessage("other",
                        "Unable to send message. "+e.getMessage(),
                        "Please correct the problem, or contact a "
                      + "Portal administrator for assistance.");
            }
        }

        if (error == null && muc.getDraftSendEmailCopy()) {
            // Send copy via email

            StringBuffer buf = new StringBuffer();
            buf.append("Notification from '")
               .append(muc.getUsername())
               .append("': ")
               .append(draft.getSubject());
            emailDraft.setSubject(buf.toString());
            buf = null;

            error = validate(emailDraft);

            if (error == null) {
                try {
                    systemEmail.sendMessage(emailDraft);
                } catch(MercuryException e) {
                    // TODO: Log exception!!!
                    e.printStackTrace(System.err);

                    notFailure = true; // Warning, not failure.
                    error = new ErrorMessage("other",
                            "Unable to send email copy. "+e.getMessage(),
                            "The notification was still sent, but no email copy was sent.");
                }
            } else {
                // Only used for validation.
                notFailure = true; // Warning, not failure.
                error = new ErrorMessage("other",
                      "Unable to send email copy. ",
		                "The notification was still sent, but no email copy was sent.");
            }
        }

        IActionResponse rslt = null;
        ErrorMessage[] errors = null;

        if (error != null) {
            errors = new ErrorMessage[] { error };
        }

        // notFailure == true means its a warning, not a failure.
        if (errors != null && notFailure == false) {
            rslt = new SimpleActionResponse(screenError, new ComposeMessageQuery(muc, errors));
        } else {
            // TODO: Display success message
            muc.clearDraft();
            rslt = new SimpleActionResponse(screen, new MessageListQuery(muc, errors));
        }
        return rslt;

    }

    /*
     * Package API.
     */

    private SendMessageAction(IWarlockFactory owner, Handle handle, String[] choices) {

        super(owner, handle, choices);

        // Instance Members.
        this.screen = null;
        this.app = null;
    }

    private ErrorMessage validate(DraftMessage draft) {
        ErrorMessage error = null;

        // Require recipients, subject, body.
        IRecipient[] recips = draft.getRecipients();
        String subject = draft.getSubject();
        String body = draft.getBody();
        if (recips == null || recips.length == 0) {
            error = new ErrorMessage("other",
                    "No recipients provided.",
                    "Please add recipients and try again.");
        } else if (subject == null || subject.trim().equals("")) {
            error = new ErrorMessage("other",
                    "No subject provided.",
                    "Please provide a subject and try again.");
        } else if (subject.length() > 256) {
            error = new ErrorMessage("other",
                    "Subject length exceeded. Current length: "+subject.length(),
                    "The message subject exceeds the 256 character limit. Please shorten your subject text and try again.");
        } else if (body == null || body.trim().equals("")) {
            error = new ErrorMessage("other",
                    "No message body provided.",
                    "Please provide a message body and try again.");
        } else if (body.length() > 4000) {
            error = new ErrorMessage("other",
                    "Body length exceeded. Current length: "+body.length(),
                    "The message body exceeds the 4000 character limit. Please shorten your body text and try again.");
        }

        return error;
    }

}
