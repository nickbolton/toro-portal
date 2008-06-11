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

package net.unicon.academus.apps.messaging;

import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.unicon.alchemist.EntityEncoder;
import net.unicon.civis.CivisRuntimeException;
import net.unicon.civis.ICivisFactory;
import net.unicon.civis.IGroup;
import net.unicon.civis.IPerson;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.EntityType;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientDetail;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;
import net.unicon.mercury.XmlFormatException;
import net.unicon.penelope.IDecisionCollection;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ImportExportHelper {
    
    
    /*
     * Message imports handling.
     */
    
    public static DraftMessage[] parseInputStream(InputStream stream,
                               ICivisFactory[] factories)
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
                rslt.add(parse((Element)it.next(), factories));
            }
        } catch (DocumentException e) {
            throw new XmlFormatException(e.getMessage(), e);
        }

        return (DraftMessage[])rslt.toArray(new DraftMessage[0]);
    }

    /**
     * Parse an XML fragment containing a message.
     * @param e Element object representing the <code>&lt;message&gt;</code>
     *          element.
     * @param facs Civis factories used to validate users and groups
     * @param gexpand Flag to indicate whether groups should be expanded into
     *                their users, or left as groups.
     * @return IMessage representing the given XML element
     */
    public static DraftMessage parse(Element e, ICivisFactory[] facs)
                                 throws MercuryException {
        // Assertions.
        if(e == null) {
        	throw new IllegalArgumentException(
        			"Argument 'e [Element]' cannot be null.");
        }
        if (!e.getName().equals("message")) {
            throw new XmlFormatException(
                    "Argument 'e [Element]' must be a <message> element.");
        }
        
        DraftMessage msg = new DraftMessage();
        String buf = null;
        Attribute recipType = null;
        Element e2 = null;
        List list = null;

        // Priority.
        e2 = (Element)e.selectSingleNode("priority");
        if (e2 != null) {
            String tmp = e2.getText();
            Priority p = null;
            try {
                p = Priority.getInstance(tmp);
            } catch (IllegalArgumentException ex) {
                try {
                    p = Priority.getInstance(Integer.parseInt(tmp));
                } catch (Exception ex2) {
                    p = null;
                }
            }

            if (p == null) {
                throw new XmlFormatException("Illegal priority specified: "+tmp);
            }

            msg.setPriority(p);
        }

        // Recipient.
        list = e.elements("recipient");
        Element recipElement = null;
        Element eType = null;
        Element recipId = null;
        for (Iterator it = list.iterator(); it.hasNext();) {
            recipElement = (Element)it.next();
            recipType = recipElement.attribute("type");

            // get the id 
            recipId = (Element)recipElement.selectSingleNode("id");
            if (recipId == null) {
                throw new XmlFormatException("Missing Element <id> under <recipient>");
            }

            // get the entityType
            eType = (Element)recipElement.selectSingleNode("entity-type");
            if(eType == null){
                throw new XmlFormatException("Missing Element <entity-type> under <recipient> for entity " 
                        + recipId.getText());
            }

            // entity type is a group
            if (eType.getTextTrim().equalsIgnoreCase(EntityType.GROUP.toString())) {
               // If we have factories to check with, make sure they are valid
               // groups.
               if (facs != null && facs.length > 0) {
                  IGroup group = findGroup(facs, recipId.getText());
                  if (group == null) {
                     throw new RuntimeException(
                           "Could not find the specified group: "
                                 + recipId.getText());
                  }
               }

               // Don't expand groups -- it is handled by the message factory.
               msg.addRecipient(recipType.getValue(), recipId.getText(),
                                recipId.getText(), EntityType.GROUP);
              
            } else if (eType.getTextTrim().equalsIgnoreCase(EntityType.USER.toString())) {
                Element eLabel = (Element)recipElement.selectSingleNode("label");
                String uid = recipId.getText();
                String name = uid;

                if (eLabel != null)
                    name = eLabel.getText();
                
                name = validateUsername(facs, uid, name);

                msg.addRecipient(recipType.getValue(), name, uid, EntityType.USER);
            } else {
               throw new XmlFormatException("Illegal value for <entity-type> element: "+eType.getText());
            }
        }

        // Subject.
        e2 = (Element)e.selectSingleNode("subject");
        if (e2 != null)
            msg.setSubject(e2.getText());
        else
            throw new XmlFormatException(
                    "Missing element <subject>: Message subject required.");

        // Body.
        e2 = (Element)e.selectSingleNode("body");
        if (e2 != null) {
            Element e3 = (Element)e2.selectSingleNode("html");
            if (e3 != null)
                msg.setBody(e3.asXML()); 
            else
                msg.setBody(e2.getText()); 
        } else
            throw new XmlFormatException(
                    "Missing element <body>: Message body required.");

        //e2 = (Element)e.selectSingleNode("expires");

        return msg;
    }

   private static String validateUsername(ICivisFactory[] facs, String uid, String name) {
      // Verify username
       if (facs != null && facs.length > 0) {
           IPerson p = null;
           for (int j = 0; p == null && j < facs.length; j++) {
               try {
                   p = facs[j].getPerson(uid);
               } catch(CivisRuntimeException cre) {
                   // Ignore until all sources have been checked.
                   // TODO: Differentiate exception types?
               }
           }

           if (p == null) {
               throw new RuntimeException(
                       "Unable to find a user with the specified name: "
                     + uid);
           }

           if (uid.equals(name)) {
               // Look up the proper name, since we already have a reference.
               StringBuffer n = new StringBuffer();
               IDecisionCollection dColl = p.getAttributes();
               n.append(dColl.getDecision("lName").getFirstSelectionValue())
                .append(", ")
                .append(dColl.getDecision("fName").getFirstSelectionValue());

               name = n.toString();
           }
       }
      return name;
   }

    private static IGroup findGroup(ICivisFactory[] facs, String gpath) {
		IGroup group = null;

		if (facs != null && facs.length > 0) {
			// Confirm groups are valid
			for (int j = 0; j < facs.length && group == null; j++) {
				try {
					group = facs[j].getGroupByPath(gpath);
				} catch (CivisRuntimeException cre) {
					// Ignore until all sources have been checked.
					// TODO: Differentiate exception types?
				}
			}
		}

		return group;
	}

	public static String exportMessagesXml(IMessage[] messages, boolean includeReadStatus, boolean xhtmlAllowed) throws MercuryException{
        StringBuffer rslt = new StringBuffer();
        
        rslt.append("<messages>");

        for(int i = 0; i < messages.length; i++){
            rslt.append(exportMessageXml(messages[i], includeReadStatus, xhtmlAllowed));
	    }

        rslt.append("</messages>");

        return rslt.toString();
    }
    
    public static String exportMessageXml(IMessage message, boolean includeReadStatus, boolean xhtmlAllowed) throws MercuryException{
        StringBuffer rslt = new StringBuffer();
        
        rslt.append("<message>");
        
        IRecipient[] recipients = message.getRecipients();
        for (int i = 0; i < recipients.length; i++) {
	        rslt.append("<recipient type=\"")
		        	.append(EntityEncoder.encodeEntities(recipients[i].getType().getLabel()))
		        	.append("\"> <id>")
		        	.append(EntityEncoder.encodeEntities(recipients[i].getAddress().toNativeFormat()))
		        	.append("</id>")
		        	.append("<label>")
		        	.append(EntityEncoder.encodeEntities(recipients[i].getAddress().getLabel()))
		        	.append("</label>")
		        	.append("<entity-type>")
	            .append(recipients[i].getEntityType().toString())
	            .append("</entity-type>");
	        if (includeReadStatus && recipients[i] instanceof IRecipientDetail) {
	        	   rslt.append("<status>");
	            rslt.append(((IRecipientDetail) recipients[i]).hasReadMessage() ? "read" : "unread");
	            rslt.append("</status>");
	         }
	         rslt.append("</recipient>");
	      }
         rslt.append("<subject>")
        	    .append(EntityEncoder.encodeEntities(message.getSubject()))
        	    .append("</subject>");

        String body = message.getBody();
        rslt.append("<body>");
        if (xhtmlAllowed && body.startsWith("<html>")) {
	        // Don't encode: will be XHTML
	        rslt.append(XHTMLFilter.filterHTML(body));
	     } else {
	        rslt.append("<![CDATA[")
	            .append(body)
	            .append("]]>");
	     }
        rslt.append("</body>");

        rslt.append("<priority>")
            .append(message.getPriority().toInt())
            .append("</priority>")
        	// TODO get the date expires
            .append("<expires>")
            .append("</expires>")
            .append("</message>");
        
        return rslt.toString();
    
    }
}
