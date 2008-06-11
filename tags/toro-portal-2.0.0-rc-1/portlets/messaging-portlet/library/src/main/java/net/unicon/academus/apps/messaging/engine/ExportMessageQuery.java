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

import net.unicon.academus.apps.messaging.AttachmentsHelper;
import net.unicon.academus.apps.messaging.ImportExportHelper;
import net.unicon.academus.apps.messaging.MessagingAccessType;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.mercury.MercuryException;
import net.unicon.warlock.WarlockException;

public class ExportMessageQuery extends InitialQuery {

    /*
     * Public API.
     */

    public ExportMessageQuery(MessagingUserContext user) {
        super(user);
    }

    public String query() throws WarlockException {

        StringBuffer rslt = new StringBuffer();

        String exportXML = null;
        try {
            exportXML =
                ImportExportHelper.exportMessagesXml(muc.getMessageSelection()
                        , muc.getFactorySelection().hasAccessType(MessagingAccessType.DETAIL_REPORT),
                        muc.getAppContext().allowXHTML());
        } catch (MercuryException ex) {
            throw new RuntimeException("Failed to export messages in XML", ex);
        }

        // Prepare the download
        AttachmentsHelper.prepareExport(muc, exportXML);

        rslt.append("<state>");
        super.commonQueries(rslt);

        rslt.append("<export>");
        rslt.append("<name>messages.xml</name>");
        rslt.append("<url>")
            .append(AttachmentsHelper.getExportURL(muc))
            .append("</url>");
        rslt.append("</export>");

        rslt.append("</state>");

        return rslt.toString();

    }
}
