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
package net.unicon.portal.channels.survey.surveyauthor;



import java.io.*;

import java.util.*;



import org.jasig.portal.MultipartDataSource;







import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;




public class Upload extends SurveyAuthorScreen {



  public void init(Hashtable params) throws Exception {

    super.init(params);

  }



  public void reinit(Hashtable params) throws Exception {}



  public XMLData getData() {

    return m_data;

  }



  public Screen upload(Hashtable params) throws Exception {

    MultipartDataSource fileSource;

    try {

      fileSource = ((MultipartDataSource[])params.get("file"))[0];

    } catch (Exception e) {

      return error(CSurveyAuthor.ERROR_FILE_NOT_FOUND,"Upload");

    }

    TextFileData td = TextFileData.parse(fileSource);

    try {

      td.parse(new StringBufferInputStream(td.getContent()));

    } catch (Exception e) {

      return error(CSurveyAuthor.ERROR_XML,"Upload");

    }

    params.put("xmlfile", td);

    Screen scr = getScreen("Form");

    return (scr == null) ? this : scr.invoke("returnFromUpload", params);

  }



  public Screen close(Hashtable params) throws Exception {

    Screen scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");

    return (scr == null) ? this : scr;

  }

}
