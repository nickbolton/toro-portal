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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.MessagingAccessType;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.academus.apps.messaging.XHTMLFilter;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.IPerson;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.Priority;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.complement.TypeText64;
import net.unicon.penelope.complement.TypeText;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.WarlockException;


/**
 * State Query for the message_compose screen.
 * @author eandresen
 */
public class ComposeMessageQuery extends InitialQuery
{
    private static final DateFormat formatter = new SimpleDateFormat();
    private static IChoiceCollection ccol = initChoiceCollection();
    private final String mode;

    public ComposeMessageQuery(MessagingUserContext muc) {
        this(muc, "edit");
    }

    public ComposeMessageQuery(MessagingUserContext muc, String mode) {
        this(muc, null, mode);
    }

    public ComposeMessageQuery(MessagingUserContext muc, ErrorMessage[] errors) {
        this(muc, errors, "edit");
    }

    public ComposeMessageQuery(MessagingUserContext muc, ErrorMessage[] errors, String mode) {
        super(muc, errors);
        this.mode = mode;
    }

    protected void queryStatus(StringBuffer rslt) throws WarlockException {
        super.queryStatus(rslt);

        rslt.append("<view-type>")
            .append(this.mode)
            .append("</view-type>");

        DraftMessage draft = muc.getDraft();
        if (draft.getBody() != null && draft.getBody().length() > 4000) {
            rslt.append(
                    new ErrorMessage("other",
                    "Body length exceeded. Current length: "+draft.getBody().length(),
                    "The message body exceeds the 4000 character limit. Please shorten your body text and try again.")
                    .toXml());
        }

        if (draft.getSubject() != null && draft.getSubject().length() > 256) {
            rslt.append(
                    new ErrorMessage("other",
                    "Subject length exceeded. Current length: "+draft.getSubject().length(),
                    "The message subject exceeds the 256 character limit. Please shorten your subject and try again.")
                    .toXml());
        }

    }

    protected void querySelections(StringBuffer rslt) throws WarlockException {
        super.querySelections(rslt);

        if (muc.getAppContext().getUploadLimit() != 0
                && muc.getFactorySelection()
                      .hasAccessType(MessagingAccessType.ATTACH)) {
            rslt.append("<accesstype>")
                .append(MessagingAccessType.ATTACH.getName())
                .append("</accesstype>");
        }
    }

    public String query() throws WarlockException {
    	StringBuffer rslt = new StringBuffer();
        DraftMessage draft = muc.getDraft();

        rslt.append("<state>");
        super.commonQueries(rslt);

        // TODO: Handle differing recipient types. Attribute in <recipients>?
        rslt.append("<recipients type=\"to\">");
        /*IRecipient[] recipients = draft.getRecipients();
        for (int i = 0; i < recipients.length; i++) {
            rslt.append("<address native-format=\"")
                .append(EntityEncoder.encodeEntities(recipients[i].getAddress().toNativeFormat()))
                .append("\" id=\"")
                .append(((DraftMessage.RecipientImpl)recipients[i]).getId())
                .append("\"><label>")
                .append(EntityEncoder.encodeEntities(recipients[i].getAddress().getLabel()))
                .append("</label></address>");
        }*/
        
        // add the current recipients selection to the recipients already in the list
        if(muc.getEntitySelection().length > 0){
	        ICivisEntity[] entities = 
	            new ICivisEntity[muc.getPrevEntitySelection().length 
	                             + muc.getEntitySelection().length];
	        System.arraycopy(muc.getPrevEntitySelection(), 0, entities
	                , 0, muc.getPrevEntitySelection().length);
	        System.arraycopy(muc.getEntitySelection(), 0, entities
	                , muc.getPrevEntitySelection().length, muc.getEntitySelection().length);
	        muc.setPrevEntitySelection(entities);
	        
	        // clear the current selection
	        muc.setEntitySelection(new ICivisEntity[0]);
        }
        
        ICivisEntity[] entities = muc.getPrevEntitySelection();
        IDecisionCollection dColl = null;
        StringBuffer name = new StringBuffer();
        for (int i = 0; i < entities.length; i++) {
            if (entities[i] instanceof IPerson){
                dColl = entities[i].getAttributes();
                name.append(dColl.getDecision("lName").getFirstSelectionValue())
                	.append(", ")
                	.append(dColl.getDecision("fName").getFirstSelectionValue());
                
            }else{
                name.append(entities[i].getName());
            }                
            rslt.append("<address native-format=\"")
            .append(EntityEncoder.encodeEntities(entities[i].getName()))
            .append("\" id=\"")
            .append(i)
            .append("\"><label>")
            .append(EntityEncoder.encodeEntities(name.toString()))
            .append("</label></address>");
            name.setLength(0);
        }
	    rslt.append("</recipients>");

        IAttachment[] a = draft.getAttachments();
        rslt.append("<attachments total=\"")
            .append(a.length)
            .append("\">");
        for (int i = 0; i < a.length; i++)
            rslt.append("<file id=\"")
                .append(a[i].getId())
                .append("\"><name>")
                .append(EntityEncoder.encodeEntities(a[i].getName()))
                .append("</name><size>")
                .append(super.simpleFormatSize(a[i].getSize()))
                .append("</size><mime>")
                .append(EntityEncoder.encodeEntities(a[i].getContentType()))
                .append("</mime></file>");
        rslt.append("</attachments>");

        if (mode.equals("preview")) {
            rslt.append("<sent>")
                .append(formatter.format(new Date()))
                .append("</sent>");

            rslt.append("<sender>")
                .append("You")
                .append("</sender>");

            rslt.append("<subject>")
                .append(EntityEncoder.encodeEntities(draft.getSubject()))
                .append("</subject>");

            rslt.append("<body>");

            String body = draft.getBody();
            if (muc.getAppContext().allowXHTML() && body.startsWith("<html>"))
                // Don't encode: will be XHTML
                rslt.append(XHTMLFilter.filterHTML(body));
            else
                rslt.append("<![CDATA[")
                    .append(body)
                    .append("]]>");

            rslt.append("</body>");
            
            rslt.append("<priority>")
                .append(draft.getPriority().toInt())
                .append("</priority>");
        }

        rslt.append("</state>");
//System.out.println("ComposeMessageQuery " + rslt.toString());
        return rslt.toString();
    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        IDecisionCollection rslt = null;
        List decisions = new ArrayList();
        DraftMessage draft = muc.getDraft();
        IEntityStore store = ccol.getOwner();

        try {
            IChoice c = null;
            ISelection s = null;
            IDecision d = null;

            // Recipients and Attachments belong in the state query xml.

            // Subject.
            c = ccol.getChoice("cSubject");
            s = store.createSelection(c.getOptions()[0], TypeText64.INSTANCE.parse(draft.getSubject()));
            d = store.createDecision(null, c, new ISelection[] { s });
            decisions.add(d);

            // Message.
            c = ccol.getChoice("cBody");
            s = store.createSelection(c.getOptions()[0], TypeText.INSTANCE.parse(draft.getBody()));
            d = store.createDecision(null, c, new ISelection[] { s });
            decisions.add(d);

            // Priority.
            c = ccol.getChoice("cPriority");
            s = store.createSelection(c.getOption(String.valueOf(draft.getPriority().toInt())), TypeNone.INSTANCE.parse(null));
            d = store.createDecision(null, c, new ISelection[] { s });
            decisions.add(d);

            // "Send copy via E-Mail"
            if (muc.getDraftSendEmailCopy()) {
                c = ccol.getChoice("cEmail");
                s = store.createSelection(c.getOption("yes"), TypeNone.INSTANCE.parse(null));
                d = store.createDecision(null, c, new ISelection[] { s });
                decisions.add(d);
            }

            // Build decision collection.
            rslt = store.createDecisionCollection(ccol,
                    (IDecision[])decisions.toArray(new IDecision[0]));

        } catch (Throwable t) {
            throw new WarlockException(
                    "ComposeMessageQuery failed to build its decision collection "
                    + "for composeForm.", t);
        }

        return new IDecisionCollection[] { rslt };
    }

    /*
     * Private implementation.
     */

    private static IChoiceCollection initChoiceCollection() {
        IEntityStore store = new JvmEntityStore();
        List choices = new ArrayList();
        IChoiceCollection rslt = null;
        IOption o = null;
        IChoice c = null;

        try {
            // Subject.
            o = store.createOption(Handle.create("oSubject"), null, TypeText64.INSTANCE);
            c = store.createChoice(Handle.create("cSubject"), null, new IOption[] { o }, 1, 1);
            choices.add(c);

            // Message.
            o = store.createOption(Handle.create("oBody"), null, TypeText.INSTANCE);
            c = store.createChoice(Handle.create("cBody"), null, new IOption[] { o }, 1, 1);
            choices.add(c);

            // Priority.
            IOption o1 = store.createOption(Handle.create(String.valueOf(Priority.getInstance(1).toInt())), null, TypeNone.INSTANCE);
            IOption o2 = store.createOption(Handle.create(String.valueOf(Priority.getInstance(2).toInt())), null, TypeNone.INSTANCE);
            IOption o3 = store.createOption(Handle.create(String.valueOf(Priority.getInstance(3).toInt())), null, TypeNone.INSTANCE);
            c = store.createChoice(Handle.create("cPriority"), null, new IOption[] { o1, o2, o3 }, 1, 1);
            choices.add(c);

            // "Send copy via E-Mail"
            o = store.createOption(Handle.create("yes"), null, TypeNone.INSTANCE);
            c = store.createChoice(Handle.create("cEmail"), null, new IOption[] { o }, 0, 0);
            choices.add(c);

            // Build choice/decision collection.
            rslt = store.createChoiceCollection(Handle.create("composeForm"), null,
                    (IChoice[])choices.toArray(new IChoice[0]));
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create IChoiceCollection for ComposeMessageQuery", t);
        }

        return rslt;
    }
}
