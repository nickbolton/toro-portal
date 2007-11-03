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

package net.unicon.mercury;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.unicon.mercury.fac.AbstractRecipient;
import net.unicon.mercury.fac.AddressImpl;
import net.unicon.mercury.fac.AttachmentImpl;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Representation of a "draft", or unset, message.
 * This is used for message composition, as well as imports.
 *
 * @author eandresen
 */
public class DraftMessage {

    /*
     * Instance members
     */

    private List recipients   = new ArrayList();
    private String subject    = "";
    private Priority priority = null;
    private String body       = "";
    private List attachments  = new ArrayList();
    private int attachid      = 0;
    private int recipientid   = 0;

    /*
     * Public API.
     */

    public DraftMessage() {}

    /**
     * Copy constructor.
     */
    public DraftMessage(DraftMessage src) {
        this.subject     = src.subject;
        this.body        = src.body;
        this.attachid    = src.attachid;
        this.recipientid = src.recipientid;
        this.priority    = src.priority;
        this.recipients  = new ArrayList(src.recipients);
        this.attachments = new ArrayList(src.attachments);
    }

    public void addRecipient(String type, IAddress addr, EntityType eType) {
        this.recipients.add(new RecipientImpl(recipientid++,
                                addr,
                                new RecipientTypeImpl(type),
                                eType)
                           );
    }

    public void addRecipient(String type, String label, String nativeFormat
            , EntityType eType) {
        addRecipient(type, new AddressImpl(label, nativeFormat), eType);
    }

    public boolean removeRecipient(int id) {
        boolean done = false;
        Iterator it = this.recipients.iterator();
        while (!done && it.hasNext()) {
            RecipientImpl at = (RecipientImpl)it.next();
            if (at.getId() == id) {
                it.remove();
                done = true;
            }
        }
        return done;
    }

    public void removeRecipients() {
        this.recipients.clear();
    }

    public IRecipient[] getRecipients() {
        return getRecipients(new IRecipientType[0]);
    }

    public IRecipient[] getRecipients(IRecipientType[] types) {
        List rslt = new ArrayList();

        if (types != null && types.length > 0) {
            // For each recipient, check its type against the requested
            // types; if requested, add it to the result set.
            for (Iterator it = recipients.iterator(); it.hasNext();) {
                IRecipient r = (IRecipient)it.next();
                for (int j = 0; j < types.length; j++) {
                    if (r.getType().equals(types[j]))
                        rslt.add(r);
                }
            }
        } else {
            // All recipients requested
            rslt = recipients;
        }

        return (IRecipient[])rslt.toArray(new IRecipient[0]);
    }

    public void setSubject(String subject) { this.subject = subject; }
    public String getSubject() {
        return this.subject;
    }

    public void setBody(String body) { this.body = body; }
    public String getBody() {
        return this.body;
    }

    public void setPriority(Priority pri) { this.priority = pri; }
    public Priority getPriority() {
        if (this.priority == null)
            this.priority = Priority.PRIORITY_MEDIUM;
        return this.priority;
    }
 
    public void addAttachment(String filename, String mimetype,
                              InputStream stream) throws MercuryException {
        this.attachments.add(new AttachmentImpl(this.attachid++,
                                   filename, mimetype, stream));
    }

    public boolean removeAttachment(int id) {
        boolean done = false;
        Iterator it = this.attachments.iterator();
        while (!done && it.hasNext()) {
            IAttachment at = (IAttachment)it.next();
            if (at.getId() == id) {
                it.remove();
                done = true;
            }
        }
        return done;
    }

    public IAttachment[] getAttachments() {
        return (IAttachment[])this.attachments.toArray(new IAttachment[0]);
    }

    /*
     * Message imports handling.
     */

    public static DraftMessage[] parseInputStream(InputStream stream)
                                throws IOException, MercuryException {
        List rslt = new ArrayList();

        try {
            Element e = new SAXReader()
                .read(stream)
                .getRootElement();
            List list = e.elements("message");
            if (list.size() < 1)
                throw new XmlFormatException(
                        "At least one <message> element must be present.");
            for (Iterator it = list.iterator(); it.hasNext();) {
                rslt.add(parse((Element)it.next()));
            }
        } catch (DocumentException e) {
            throw new XmlFormatException("Unable to parse input stream", e);
        }

        return (DraftMessage[])rslt.toArray(new DraftMessage[0]);
    }

    /**
     * Parse an XML fragment containing a message.
     * @param e Element object representing the <code>&lt;message&gt;</code>
     *          element.
     * @return IMessage representing the given XML element
     */
    public static DraftMessage parse(Element e)
                                 throws MercuryException {
        // Assertions.
        assert e != null : "Argument 'e [Element]' cannot be null.";
        if (!e.getName().equals("message"))
            throw new XmlFormatException(
                    "Argument 'e [Element]' must be a <message> element.");

        DraftMessage msg = new DraftMessage();
        String buf = null;
        Attribute t = null;
        Attribute et = null;
        List list = null;

        // Priority.
        list = e.elements("priority");
        if (!list.isEmpty())
            msg.priority = Priority.getInstance(Integer.parseInt(((Element)list.get(0)).getText()));

        // Recipient.
        list = e.elements("recipient");
        for (Iterator it = list.iterator(); it.hasNext();) {
            Element el = (Element)it.next();
            t = el.attribute("type");
            et = el.attribute("entity-type");
            msg.addRecipient(t.getValue()
                    , AddressImpl.parse((Element)el.selectSingleNode("address"))
                    , EntityType.getInstance(et.getValue()));
        }

        // Subject.
        list = e.elements("subject");
        if (!list.isEmpty())
            msg.subject = ((Element)list.get(0)).getText();

        // Body.
        list = e.elements("body");
        if (!list.isEmpty())
            msg.body = ((Element)list.get(0)).getText();
/*
        list = e.elements("body");
        if (!list.isEmpty()) {
            Element b = (Element)list.get(0);
            b = b.createCopy();
            b.setName("xhtml");
            msg.body = b.asXML();
        }
*/

        //list = e.elements("expires");

        return msg;
    }

    private static class RecipientTypeImpl implements IRecipientType {
        private String label;
        public RecipientTypeImpl(String label) { this.label = label; }
        public String getLabel() { return this.label; }
        
        public boolean equals(Object o){
            if(!(o instanceof IRecipientType)){
                return false;
            }
            
            if(((IRecipientType)o).getLabel().equalsIgnoreCase(this.getLabel())){
                return true;
            }
                
            return false;         
        }
        
        public int hashCode() {return this.getLabel().hashCode(); }
    }

    public static class RecipientImpl extends AbstractRecipient {
        private final int id;

        public RecipientImpl(int id, IAddress address,
                IRecipientType type, EntityType eType) {
            super(address, type, eType);
            this.id = id;
            
        }

        public int getId() { return this.id; }
           
    }
}
