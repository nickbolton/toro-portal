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
package net.unicon.portal.channels.survey.survey;



import java.io.*;

import java.util.*;






import net.unicon.academus.apps.form.*;
import net.unicon.academus.apps.rad.*;
import net.unicon.academus.apps.survey.*;
import net.unicon.portal.channels.rad.*;



public class Peephole extends SurveyScreen {

  FormData[] m_fdr = null;



  public void init(Hashtable params) throws Exception {

    super.init(params);

    refresh();

  }



  public void reinit(Hashtable params) throws Exception {

    init(params);

  }



  public void refresh() throws Exception {

    m_fdr = getBo().listFormSurveyforSurvey(m_user, getIDList());

  }



  public XMLData getData() throws Exception {

    m_data.putE("Form", m_fdr);

    return m_data;

  }



  public String getIDList() throws Exception {

    String ids = "(" + SQL.list(m_user.getIdentifiers());

    if( GroupData.isPortalUser(m_user)) {

      GroupData[] ancestors = GroupData.getAncestors(m_user, false);

      if(ancestors != null)

        for(int i = 0; i<ancestors.length; i++)

          ids += "," + SQL.esc(ancestors[i].getIdentifier());

    }

    return ids + ")";

  }

}

