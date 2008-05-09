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

import java.util.Hashtable;

import net.unicon.academus.apps.rad.XMLData;
import net.unicon.portal.channels.rad.Screen;

public class Confirm extends SurveyAuthorScreen {

    public Screen cancel(Hashtable params) throws Exception {

    	Screen scr = (getScreen("Form") == null) ? makeScreen("Form") : getScreen("Form");
        return (scr == null) ? this : scr.invoke("returnFromCancel", params);

    }

    public Screen clickOK(Hashtable params) throws Exception {

        Screen scr = (getScreen("Peephole") == null) ? makeScreen("Peephole") : getScreen("Peephole");
        scr.reinit(params);

        return scr;

    }

    XMLData getData() throws Exception {

        XMLData text = new XMLData();
        text.putE("text", "Canceling will delete any new or updated survey " +
        		"pages or questions authored for this survey. ");
        
        text.putE("instructions", "Click OK to cancel.  Click Cancel to " +
        		"return to the New Survey Form and save all content.");

        m_data.putE("confirm", text);

        return m_data;
    }

    public void init(Hashtable params) throws Exception {

        m_data = new XMLData();
        m_user = m_channel.logonUser();

    }
}
