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

package net.unicon.academus.delivery.virtuoso;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.unicon.academus.delivery.DeliveryException;
import net.unicon.academus.delivery.virtuoso.activation.ActivationAdjunctAgent;
import net.unicon.academus.delivery.virtuoso.content.ContentAdjunctAgent;
import net.unicon.academus.delivery.virtuoso.theme.ThemeBroker;
import net.unicon.portal.domain.ChannelClass;
import net.unicon.portal.domain.ChannelClassFactory;
import net.unicon.portal.permissions.ActivityFactory;
import net.unicon.portal.permissions.DirtyEvent;

public final class Bootstrap {

    /*
     * Public API.
     */

    public static void init() throws DeliveryException {

        // Content Groups.
        ContentAdjunctAgent.bootstrap();

        // Activation Parameters.
        ActivationAdjunctAgent.bootstrap();

        // Themes & Styles.
        Document doc = null;
        try {
            SAXReader reader = new SAXReader();
            doc = reader.read(Bootstrap.class.getResourceAsStream("/properties/virtuoso-themes.xml"));
        } catch (Throwable t) {
            String msg = "Virtuoso Integration Bootstrap was unable to load "
                                                + "the themes collection.";
            throw new DeliveryException(msg, t);
        }
        ThemeBroker.bootstrap((Element) doc.selectSingleNode("theme-set"));

        // Add permissions for Instructor Notes.
        try {
            ChannelClass cls = ChannelClassFactory.getChannelClass("CurriculumUsabilityChannel");
            String handle = "viewInstructorNotes";
            String label = "View Instructor Notes";
            String desc = "User may view Instructor Notes embedded within curriculum.";
            Map def = new HashMap();
            def.put("Sponsor", Boolean.TRUE);
            cls.addActivity(ActivityFactory.createActivity(handle, label, desc, def, new DirtyEvent[0]));
        } catch (Throwable t) {
            String msg = "Virtuoso Iliad integration failed to initialize permissions for Instructor Notes.";
            throw new DeliveryException(msg, t);
        }

    }

    /*
     * Implementation.
     */

    private Bootstrap() {}

}
