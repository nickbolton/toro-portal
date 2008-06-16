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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.download.DownloadServiceServlet;
import net.unicon.academus.apps.messaging.MessagingAccessType;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.academus.apps.messaging.XHTMLFilter;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.mercury.Features;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientDetail;
import net.unicon.mercury.SortMethod;
import net.unicon.mercury.SpecialFolder;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.WarlockException;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ReadMessageQuery extends InitialQuery
{
    private static final DateFormat formatter = new SimpleDateFormat();
    
    public ReadMessageQuery(MessagingUserContext muc) {
        this(muc, null);
    }

    public ReadMessageQuery(MessagingUserContext muc, ErrorMessage[] errors) {
        super(muc, errors);
    }
    
    public String query() throws WarlockException {
    	StringBuffer rslt = new StringBuffer();
    	StringBuffer reportRslt = new StringBuffer();
    	String mode = muc.getDetailPref().getMode();
    	int readUserCount = 0;
        String actions = null;

        rslt.append("<state>");
        super.commonQueries(rslt);

        IMessage[] msgs = muc.getMessageList();
        int msgIndex = muc.getDetailPref().getCurrentMessageIndex();
        IMessage msg = msgs[msgIndex];

        // message Paging.
        int perpage = 1; 				// there will be one message per page
        int currentPage = msgIndex + 1;
        int totalPages = msgs.length;
        
        if (currentPage == PagingState.LAST_PAGE || currentPage > totalPages) {
            currentPage = totalPages;            
        }

        int firstItem = 1;
        int lastItem = totalPages;
        
        rslt.append("<contents currentpage=\"")
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
            .append(msgs.length)
            .append("\">");
        try {
            // put in the message information
            rslt.append("<message ");

            // Message identifier.
            rslt.append("id=\"")
                .append(EntityEncoder.encodeEntities(msg.getId()))
                .append("\"> ");

            // Received Date.
            rslt.append("<received>")
                .append(formatter.format(msg.getDate()))
                .append(" </received>");

            // Read status.
            rslt.append("<status>")
                .append((msg.isUnread() ? "unread" : "read"))
                .append("</status>");

            // Priority.
            rslt.append(msg.getPriority().toXml());

            // Sender.
            rslt.append("<sender>");
            rslt.append(msg.getSender().toXml());
            rslt.append("</sender>");
            
            // Subject
            rslt.append("<subject>")
                .append(EntityEncoder.encodeEntities(msg.getSubject()))
                .append("</subject>");

            // Only display body and attachment information in Details view.
            if (mode.equals("details")) {
                // Body.
                String body = msg.getBody();
                actions = getActions(body);
                body = stripActions(body);
                rslt.append("<body>");

                if (muc.getAppContext().allowXHTML() && body.startsWith("<html>"))
                    rslt.append(XHTMLFilter.filterHTML(body));
                else
                    rslt.append("<![CDATA[")
                        .append(body)
                        .append("]]>");

                rslt.append("</body>");

                // Actions.
                if (actions != null && !actions.trim().equals("")) {
//System.out.println("External choice");
                    rslt.append("<external-choice handle=\"externActions\">")
                        .append(resolveActions(actions))
                        .append("</external-choice>");
                } else {
                    muc.setCallbackActions(null);
                }

                // Attachments.
                IAttachment[] attachments = msg.getAttachments();
                rslt.append("<attachments total=\"")
                    .append(attachments.length)
                    .append("\">");

                for(int i = 0; i < attachments.length; i++){
                    rslt.append("<file>")
                        .append("<name>")
                        .append(EntityEncoder.encodeEntities(attachments[i].getName()))
                        .append("</name>")
                        .append("<uri>")
                        .append(DownloadServiceServlet.getResourceURL(muc.getDownloadResource(msg, attachments[i])))
                        .append("</uri>")
                        .append("<size>")
                        .append(super.simpleFormatSize(attachments[i].getSize()))
                        .append("</size>")
                        .append("<mime>")
                        .append(attachments[i].getContentType())
                        .append("</mime>")
                        .append("</file>");            
                }
                rslt.append("</attachments>");
            }

            // Expiration
            rslt.append("<expires></expires>");
            
            // End.
            rslt.append("</message>");

            // Only display recipient detail on Report view.
            if (mode.equals("report")) {
                // Recipients.
                IRecipient[] recipients = msg.getRecipients();
                
                Arrays.sort(recipients, muc.getRecipientSortMethod().getComparator());

                // report Paging.
                perpage = muc.getDetailPref().getReportPageState().getItemsPerPage();
                currentPage = muc.getDetailPref().getReportPageState().getCurrentPage();
                totalPages = 0;
                if (perpage != PagingState.SHOW_ALL_ITEMS)
                    totalPages = (int)Math.ceil((double)recipients.length / (double)perpage);
                totalPages = (totalPages > 0 ? totalPages : 1);

                if (currentPage == PagingState.LAST_PAGE || currentPage > totalPages) {
                    currentPage = totalPages;
                    muc.getDetailPref().getReportPageState().setCurrentPage(totalPages);
                }

                firstItem = (currentPage-1) * perpage + 1;
                lastItem = firstItem + perpage - 1;
                if (lastItem > recipients.length || perpage == PagingState.SHOW_ALL_ITEMS)
                    lastItem = recipients.length;

                reportRslt.append("<report currentpage=\"")
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
                    .append(recipients.length)
                	.append("\" ");
                    
                boolean hasDetail = muc.getFactorySelection().getFactory()
                                       .getFeatures()
                                       .hasFeature(Features.RECIPIENTDETAIL);
                StringBuffer temp = new StringBuffer();
                for (int i = firstItem-1; i < lastItem; i++) {
                    // report information 
                    temp.append("<item ");

                    if (hasDetail){
                        temp.append("status=\"");
                        if(((IRecipientDetail)recipients[i]).hasReadMessage()
                                || (recipients[i].getAddress().equals(msg.getSender())
                                        && !msg.isUnread())){
                            temp.append("read");
                            readUserCount++;
                        }else{
                            temp.append("unread");
                        }
                        temp.append("\"");
                    }
                    temp.append("><name>")
                        .append(EntityEncoder.encodeEntities(recipients[i].getAddress().getLabel()))
                        .append("</name>")
                        .append("</item>");
                }

                // total read messages
                reportRslt.append("readtotal=\"")
                	.append(readUserCount)                	
                	.append("\">");
                
                // reports user details
                reportRslt.append(temp);
                
                reportRslt.append("</report>");
            }
            
            rslt.append(reportRslt);
            
        } catch (Throwable t) {
            throw new WarlockException(
                    "Error opening the message.", t);
        }
        rslt.append("</contents>");

        rslt.append("</state>");

//System.out.println("ReadMessageQuery  XML : \n" + rslt.toString());
        return rslt.toString();    
    }
    
    protected void querySelections(StringBuffer rslt) throws WarlockException {
        super.querySelections(rslt);

        // DETAIL_RECIPIENT access type handling
        if (muc.getFactorySelection().getFactory()
                .getFeatures().hasFeature(Features.RECIPIENTDETAIL))
        {
            boolean detailFound;
            IMessage[] msgs = muc.getMessageList();
            int msgIndex = muc.getDetailPref().getCurrentMessageIndex();
            IMessage msg = msgs[msgIndex];

            // Provide DETAIL_REPORT if the user sent the message, or they have that permission
            // and user is not in System Messages Folder.
            try {
                detailFound =
                    (msg.getSender().toNativeFormat()
                       .equals(muc.getUsername()) || 
                    muc.getFactorySelection()
                       .hasAccessType(MessagingAccessType.DETAIL_REPORT)) 
                       && !muc.getFolderSelection()
                       .getIdString().equals(SpecialFolder.SYSFOLDER.getLabel());
            } catch (Exception me) {
                throw new RuntimeException(
                        "Unable to check DETAIL_REPORT access type", me);
            }

            if (detailFound) {
                // Okay, lets pass it along.
                rslt.append("<accesstype>")
                    .append(MessagingAccessType.DETAIL_REPORT.getName())
                    .append("</accesstype>");
            }
        }
    }
    
    protected void queryStatus(StringBuffer rslt) throws WarlockException {
        super.queryStatus(rslt);

        rslt.append("<view-type>")
            .append(muc.getDetailPref().getMode())
            .append("</view-type>");
    }
    
    public IDecisionCollection[] getDecisions(){
        IEntityStore store;
        List rslt = new ArrayList();
        
        try{
            store = new JvmEntityStore();
            
            // create choices for paging
            int ipp = muc.getDetailPref().getReportPageState().getItemsPerPage();
            IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);
            ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });

            // Sorting.
            SortMethod method = muc.getMessageSortMethod();
            IOption oSort = store.createOption(Handle.create(method.getDirection()), null, TypeNone.INSTANCE);
            IChoice cSort = cSort = store.createChoice(Handle.create(method.getMode() + "SortDirection"), null, new IOption[] { oSort }, 0, 1);
            ISelection sSort = store.createSelection(oSort, TypeNone.INSTANCE.parse(null));
            IDecision dSort = store.createDecision(null, cSort, new ISelection[] { sSort });
            
            
            // Choices.
            Handle h = Handle.create("messageDetailForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null, new IChoice[] { cNumItems, cSort });

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dNumItems, dSort });
            rslt.add(dColl);
            
        }catch(Exception e){
            throw new RuntimeException("ReadMessageQuery : Error in creating the DC. ", e);
        }
        return (IDecisionCollection[])(rslt.toArray(new IDecisionCollection[rslt.size()]));
    }

    private String stripActions(String body) {
        return body.replaceAll("===<<<.*>>>===", "");
    }

    private String getActions(String body) {
        String rslt = null;
        Pattern p = Pattern.compile("===<<<(.*)>>>===");
        Matcher m = p.matcher(body);
        if (m.find()) {
            rslt = m.group(1);
        }

        if (rslt != null) {
            rslt = rslt.replaceAll("%USERNAME%", muc.getUsername());
        }

        return rslt;
    }

    private String resolveActions(String actions) {
        String rslt = null;
        Map callbackActions = new HashMap();

        try {
            Element e =
                (new SAXReader()).read(new StringReader(actions))
                                 .getRootElement();

            if (e.getName().equals("external-action")) {
                URL url = generateUrl(e);

//System.out.println("Url: "+url.toString());
                InputStream is = url.openStream();
                rslt = bufferStream(is);
            }

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            rslt = null;
        }

//System.out.println("resolveActions(): rslt = "+rslt);

        if (rslt != null) {
            try {
                // Parse the response
                Element e =
                    (new SAXReader()).read(new StringReader(rslt))
                    .getRootElement();

                StringBuffer buf = new StringBuffer();

                Element e2 = (Element)e.selectSingleNode("label");
                if (e2 != null)
                    buf.append(e2.asXML());
                e2 = (Element)e.selectSingleNode("description");
                if (e2 != null)
                    buf.append(e2.asXML());

                List elist = e.selectNodes("user-action");
                Iterator it = elist.iterator();
                while (it.hasNext()) {
                    Element el = (Element)it.next();

                    Attribute handle = el.attribute("handle");
                    if (handle == null)
                        throw new IllegalArgumentException(
                                "Attribute 'handle' required on <user-action>");

                    URL url = generateUrl(el);

//System.out.println("[CallbackAction] Handle: "+handle.getValue()+" Url: "+url.toString());
                    callbackActions.put(handle.getValue(), url);

                    // Now generate the part for the screen...
                    buf.append("<option handle=\"").append(handle.getValue()).append("\">")
                       .append(el.selectSingleNode("label").asXML())
                       .append(el.selectSingleNode("description").asXML())
                       .append("</option>");
                }

                elist = e.selectNodes("message");
                it = elist.iterator();
                while (it.hasNext()) {
                    Element el = (Element)it.next();

                    buf.append("<message>").append(EntityEncoder.encodeEntities(el.getText())).append("</message>");
                }

                rslt = buf.toString();
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                rslt = null;
            }
        }

        muc.setCallbackActions(callbackActions);

        return rslt;
    }

    private URL generateUrl(Element e) throws Exception {
        Attribute t = e.attribute("ref");
        if (t == null)
            throw new IllegalArgumentException(
                    "Attribute 'ref' required");

        String loc = muc.getAppContext().getCallbackLocation(t.getValue());
        if (loc == null)
            throw new IllegalStateException(
                    "Unable to resolve callback reference: "+t.getValue());

        StringBuffer query = new StringBuffer();
        final String sep = "&";
        final String map = "=";

        List params = e.selectNodes("param");
        Iterator it = params.iterator();
        Attribute name = null;
        Attribute val = null;
        Element p = null;
        if (it.hasNext()) {
            p = (Element)it.next();
            name = p.attribute("name");
            val = p.attribute("value");

            query.append(name.getValue()).append(map).append(val.getValue());
        }
        while (it.hasNext()) {
            p = (Element)it.next();
            name = p.attribute("name");
            val = p.attribute("value");

            query.append(sep).append(name.getValue()).append(map).append(val.getValue());
        }

        return new URL(loc+"?"+query.toString());
    }

    private String bufferStream(InputStream is) throws IOException {
        ByteArrayOutputStream rslt = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];
        int r = 0;
        while ((r = is.read(buf)) != -1) {
            rslt.write(buf, 0, r);
        }

        return rslt.toString();
    }
}
