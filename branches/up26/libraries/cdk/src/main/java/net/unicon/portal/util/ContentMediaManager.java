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
package net.unicon.portal.util;

import org.jasig.portal.MediaManager;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.serialize.BaseMarkupSerializer;
import org.jasig.portal.serialize.CachingHTMLSerializer;
import org.jasig.portal.serialize.CachingXHTMLSerializer;
import org.jasig.portal.serialize.OutputFormat;
import org.jasig.portal.serialize.XMLSerializer;

public class ContentMediaManager {
	
	private MediaManager mediaManager = MediaManager.getMediaManager();
	private static boolean outputIndenting =
	      PropertiesManager.getPropertyAsBoolean("org.jasig.portal.MediaManager.output_indenting", false);

	public ContentMediaManager() {
	}
	
	public BaseMarkupSerializer getSerializerByName (String serializerName, java.io.Writer out) {
	    if (serializerName != null && serializerName.equals("WML")) {
	      OutputFormat frmt = new OutputFormat("wml", "UTF-8", true);
	      frmt.setDoctype("-//WAPFORUM//DTD WML 1.1//EN", "http://www.wapforum.org/DTD/wml_1.1.xml");
	      return  new XMLSerializer(out, frmt);
	    } /* else if (serializerName != null && serializerName.equals("PalmHTML")) {
	      OutputFormat frmt = new OutputFormat("HTML", "UTF-8", true);
	      return  new PalmHTMLSerializer(out, frmt);
	      } */ else if (serializerName != null && serializerName.equals("XML")) {
	      OutputFormat frmt = new OutputFormat("XML", "UTF-8", true);
	      return  new XMLSerializer(out, frmt);
	    } else if (serializerName != null && serializerName.equals("XHTML")) {
	      OutputFormat frmt = new OutputFormat("XHTML", "UTF-8", true);
	      frmt.setPreserveSpace(true);
	      frmt.setIndenting(outputIndenting);
	      frmt.setDoctype("-//W3C//DTD XHTML 1.0 Transitional//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
	      frmt.setOmitDocumentType(true);
	      return  new CachingXHTMLSerializer(out, frmt);
	    } else {
	      // default case is HTML, such as that for netscape and explorer
	      OutputFormat frmt = new OutputFormat("HTML", "UTF-8", true);
	      frmt.setPreserveSpace(true);
	      frmt.setIndenting(outputIndenting);
	      frmt.setDoctype("-//W3C//DTD HTML 4.01 Transitional//EN", "http://www.w3.org/TR/1999/REC-html401-19991224/loose.dtd");
	      frmt.setOmitDocumentType(true);
	      return  new CachingHTMLSerializer(out, frmt);
	    }
	  }

}
