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

package net.unicon.academus.apps.briefcase.engine;


import java.util.List;

import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ibiswas
 */
public class WelcomeQuery implements IStateQuery {

    private BriefcaseApplicationContext bac = null;
    private BriefcaseUserContext buc = null;
    private final Log log = LogFactory.getLog(getClass());
    
    public WelcomeQuery(BriefcaseApplicationContext bac, 
            BriefcaseUserContext buc){
        
        // Assertions
        if (bac == null) {
            throw new IllegalArgumentException("Argument 'bac [BriefcaseApplicationContext]' cannot be null.");
        }        
        if (buc == null) {
            throw new IllegalArgumentException("Argument 'buc [BriefcaseUserContext]' cannot be null.");
        }        
        this.bac = bac;
        this.buc = buc;
    }
    /* (non-Javadoc)
     * @see net.unicon.warlock.IStateQuery#query()
     */
    public String query() throws WarlockException {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }
        
    	StringBuffer rslt = new StringBuffer();

        EncryptionService encryptionService = bac.getEncryptionService();

    	rslt.append("<state>");
    	rslt.append("<settings>");
    	rslt.append("<sharing disabled=\"false\"></sharing>");
    	rslt.append("</settings>");
    	rslt.append("<status>");
    	rslt.append("<available-space>50.2 MB</available-space>");
    	rslt.append("<total-shared>12</total-shared>");
    	rslt.append("</status>");
	
    	rslt.append("<welcome>");
    	rslt.append("<user>");
    	rslt.append("<fullname>John Doe</fullname>");	
    	rslt.append("</user>");
    	rslt.append("</welcome>");

    	rslt.append("<briefcase>");
        
    	List<Drive> drives = buc.getDrives();
        String encryptedHandle = null; 
        
        for (Drive drive : drives) {

		    encryptedHandle = encryptionService.encrypt(drive.getHandle());
		                
    		rslt.append("<drive handle=\"").append(EntityEncoder.encodeEntities(encryptedHandle)).append("\" ");
			rslt.append("class-large=\"").append(drive.getLargeIcon()).append("\" ");
    		rslt.append("class-opened=\"").append(drive.getOpenIcon()).append("\" ");
    		rslt.append("class-closed=\"").append(drive.getClosedIcon()).append("\" ");
    		rslt.append("sharing=\"").append(drive.isSharing()).append("\" ");
    		rslt.append("share-target=\"").append(drive.getShareTarget()).append("\">");
    		rslt.append("<label>").append(EntityEncoder.encodeEntities(drive.getLabel())).append("</label>");
    		rslt.append("<description>")
                .append(EntityEncoder.encodeEntities(drive.getDescription()))
                .append("</description>");
    		rslt.append("</drive>");
    	}
    	rslt.append("</briefcase>");
    	rslt.append("</state>");
    	
        //return "<state msg=\"hello\"><item id=\"1\" msg=\"test1\" /><item id=\"2\" msg=\"test2\" /></state>";
    	return rslt.toString();    
    	
    }

    /* (non-Javadoc)
     * @see net.unicon.warlock.IStateQuery#getDecisions()
     */
    public IDecisionCollection[] getDecisions() throws WarlockException {
    	return new IDecisionCollection[0];
    }
    
}
    
