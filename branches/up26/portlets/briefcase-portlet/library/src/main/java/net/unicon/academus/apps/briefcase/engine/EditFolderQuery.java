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

import java.text.DateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.Label;
import net.unicon.penelope.complement.TypeTextConfigurableLimit;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IApplicationContext;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;
import net.unicon.warlock.WarlockException;

import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.ResourceType;

public class EditFolderQuery implements IStateQuery {

    // Static Members.
    private static final Integer MAX_FILE_AND_FOLDER_NAME_CHARS = Integer.valueOf(255);
    private static final Integer MAX_DESCRIPTION_CHARS = Integer.valueOf(1024);
    private static DateFormat fmt = DateFormat.getDateTimeInstance();

    // Instance members.
    IFolder target;
    boolean shared = false;
    boolean sharing = false;
    private BriefcaseApplicationContext app = null;
    private BriefcaseUserContext buc;
    private final Log log = LogFactory.getLog(getClass());

    /*
     * Public API.
     */

    public EditFolderQuery(IFolder target, boolean shared, boolean sharing, 
            IApplicationContext app, IUserContext user) {

        // Assertions.
        if (target == null) {
            String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.target = target;
        this.shared = shared;
        this.sharing = sharing;
        this.app = (BriefcaseApplicationContext)app;
        this.buc = (BriefcaseUserContext)user;

    }

    public String query() throws WarlockException {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }

        // Begin.
        StringBuffer rslt = new StringBuffer();
        
        try{
	        rslt.append("<state><properties>");
	
	        // shared
	        rslt.append(" <metadata type=\"sharing\"><value>");
	        if(this.shared){
	        	rslt.append("shared");
	        }else{
	        	rslt.append("not shared");
	        }
	        rslt.append("</value> </metadata>");
	        
	        // drive sharing
	        rslt.append("<metadata type=\"isSharing\"> <value>");
	        if(this.sharing){
	        	rslt.append("true");
	        }else{
	        	rslt.append("false");
	        }
	        rslt.append("</value></metadata>");
	        
	        // Type.
	        rslt.append("<metadata type=\"type\"><label>Type</label><value>Folder</value></metadata>");
	
	        // Location.
	        rslt.append("<metadata type=\"location\"><label>Location</label><value>")
	                        .append(EntityEncoder.encodeEntities(target.getPath("/", false)))
	                        .append("</value></metadata>");
	
	        // Contains.
	        int folderCount = target.getContents(new ResourceType[] { ResourceType.FOLDER }).length;
	        int fileCount = target.getContents(new ResourceType[] { ResourceType.FILE }).length;
	        rslt.append("<metadata type=\"contains\"><label>Contains</label><value>")
	                        .append("<folders count=\"").append(Integer.toString(folderCount)).append("\" />")
	                        .append("<files count=\"").append(Integer.toString(fileCount)).append("\" />")
	                        .append("</value></metadata>");
	
	        // Size.
	        rslt.append("<metadata type=\"size\"><label>Size</label><value>")
	                        .append(Integer.toString(target.getContents().length))
	                        .append(" Items</value></metadata>");

	        // Modified.
	        rslt.append("<metadata type=\"lastmod\"><label>Last Modified</label><value>")
	                        .append(fmt.format(target.getDateModified()))
	                        .append("</value></metadata>");
	
	        // End.
	        rslt.append("</properties>");
	        
	        rslt.append("<status>");
		    rslt.append("<command>edit</command>");
		    rslt.append("</status>");
		    
		    //	folder id
		    String url = target.getUrl();
		    EncryptionService encryptionService = app.getEncryptionService();
	        url = encryptionService.encrypt(url);
	        
	        rslt.append("<location> <current "); 
	        rslt.append("id=\"").append(EntityEncoder.encodeEntities(url)).append("\" >");
	        
	        AccessRule[] access = buc.getAccessRules(target.getOwner());
	        if (access != null) {
	            for(int i = 0; i < access.length; i++){
	                if(access[i].getStatus()){
		                rslt.append("<accesstype>")
		                	.append(access[i].getAccessType().getName())
		                	.append("</accesstype>");
	                }
	            }	            
	        }
	    	rslt.append("</current></location>");
        
	    	rslt.append("</state>");
        }catch(DemetriusException de){
            throw new WarlockException("Error in creating state query in EditFolderQuery. ", de);
        }

        return rslt.toString();

    }

    public IDecisionCollection[] getDecisions() throws WarlockException {

        IDecisionCollection decisions = null;

        try {

            IEntityStore store = new JvmEntityStore();

            // Folder Name.
            IOption oName = store.createOption(Handle.create("oName"), null,
                    TypeTextConfigurableLimit.createInstance(
                            MAX_FILE_AND_FOLDER_NAME_CHARS));
            IChoice cName = store.createChoice(Handle.create("cName"),
                                        Label.create("Folder Name"),
                                        new IOption[] { oName },
                                        1, 1);

            // Description.
            IOption oDescription = store.createOption(
                    Handle.create("oDescription"), null, 
                    TypeTextConfigurableLimit.createInstance(MAX_DESCRIPTION_CHARS));
            IChoice cDescription = store.createChoice(
                    Handle.create("cDescription"), Label.create("Description"), 
                    new IOption[] { oDescription }, 1, 0);

            // Choices.
            Handle h = Handle.create("folderInfoForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null,
                                    new IChoice[] { cName, cDescription });

            // Folder Name Value.
            ISelection sName = store.createSelection(oName, 
                    TypeTextConfigurableLimit.createInstance(
                            MAX_FILE_AND_FOLDER_NAME_CHARS).parse(
                                    target.getName()));
            IDecision dName = store.createDecision(null, cName, new ISelection[] { sName });

            // Description Value.
            ISelection sDescription = store.createSelection(oDescription, 
                    TypeTextConfigurableLimit.createInstance(
                            MAX_DESCRIPTION_CHARS).parse(""));
            // ToDo:  Implement if we end up going w/ description.
            IDecision dDescription = store.createDecision(null, cDescription, new ISelection[] { sDescription });

            decisions = store.createDecisionCollection(choices, new IDecision[] { dName, dDescription });

        } catch (Throwable t) {
            String msg = "EditFolderQuery failed to build its decision "
                                                + "collection(s).";
            throw new WarlockException(msg, t);
        }

        return new IDecisionCollection[] { decisions };

    }

}
