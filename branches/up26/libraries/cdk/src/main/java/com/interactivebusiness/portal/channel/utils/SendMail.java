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
package com.interactivebusiness.portal.channel.utils;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class SendMail extends ByteArrayDataSource
{
  public void send (String to, String cc, String from, String host, String message, String subject)
    throws MessagingException
  {
    boolean debug = false;
    // create some properties and get the default Session
    Properties props = new Properties();
    props.put("mail.smtp.host", host);
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(debug);

    try
    {
      // create a message
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from));
      InternetAddress[] address = {new InternetAddress(to)};
      msg.setRecipients(Message.RecipientType.TO, address);
      if(cc != null)
      { msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
      }
      msg.setSubject(subject);
      msg.setSentDate(new Date());
      msg.setHeader("Content-Type", "text/html");

      StringBuffer sb = new StringBuffer();
      sb.append(message);
      msg.setDataHandler(new DataHandler(new ByteArrayDataSource(sb.toString(), "text/html")));
      Transport.send(msg);
    }
    catch (MessagingException me)
    {
      throw me;
    }
  }

  public void send (String msgText, String to, String cc, String bcc, String from, String host, String subject)
    throws MessagingException
  {
    boolean debug = false;
    // create some properties and get the default Session
    Properties props = new Properties();
    props.put("mail.smtp.host", host);
    Session session = Session.getDefaultInstance(props, null);
    session.setDebug(debug);

    try
    {
      // create a message
      MimeMessage msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(from));
      InternetAddress[] address = {new InternetAddress(to)};
      msg.setRecipients(Message.RecipientType.TO, address);

      if ( cc != null)
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
      if ( bcc != null)
        msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));

      msg.setSubject(subject);
      msg.setSentDate(new Date());
      // create and fill the first message part
      MimeBodyPart mbp1 = new MimeBodyPart();
      mbp1.setText(msgText);
      Multipart mp = new MimeMultipart();
      mp.addBodyPart(mbp1);
      // add the Multipart to the message
      msg.setContent(mp);
      // send the message
      Transport.send(msg);
    }
    catch (MessagingException me)
    {
      throw me;
    }
  }
}
