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

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.unicon.academus.apps.DownloadURLUtil;
import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.briefcase.BriefcaseAccessType;
import net.unicon.academus.apps.briefcase.BriefcaseApplicationContext;
import net.unicon.academus.apps.briefcase.BriefcaseSortMethod;
import net.unicon.academus.apps.briefcase.BriefcaseUserContext;
import net.unicon.academus.apps.briefcase.Drive;
import net.unicon.academus.apps.briefcase.Location;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.AccessType;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.encrypt.EncryptionService;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.demetrius.DemetriusException;
import net.unicon.demetrius.IFolder;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.demetrius.OperationTimeoutException;
import net.unicon.demetrius.ResourceType;
import net.unicon.demetrius.fac.AbstractResourceFactory;
import net.unicon.penelope.Handle;
import net.unicon.penelope.IChoice;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecision;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.penelope.IEntityStore;
import net.unicon.penelope.IOption;
import net.unicon.penelope.ISelection;
import net.unicon.penelope.complement.TypeNone;
import net.unicon.penelope.store.jvm.JvmEntityStore;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FolderQuery implements IStateQuery {

    // Static Members.
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat();
    private static final String SIZE_FORMAT = "##0.##";
    private static DecimalFormat formatter = new DecimalFormat(SIZE_FORMAT);

    // Instance Members.
    private Location location = null;
    private BriefcaseApplicationContext app;
    private BriefcaseUserContext user;
    private List<ErrorMessage> errorList = new LinkedList<ErrorMessage>();
    private static final String BriefcaseAccessEntry = null;
    private final Log log = LogFactory.getLog(getClass());

    public FolderQuery(Location l, BriefcaseApplicationContext app,
                                BriefcaseUserContext user) {

        // Assertions.
        if (l == null) {
            String msg = "Argument 'l [Location]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (user == null) {
            String msg = "Argument 'user' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.location = l;
        this.app = app;
        this.user = user;
    }

    public FolderQuery(Location l, BriefcaseApplicationContext app,
                                BriefcaseUserContext user,
                                ErrorMessage[] errors) {

        // Assertions.
        if (l == null) {
            String msg = "Argument 'l [Location]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (user == null) {
            String msg = "Argument 'user' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (errors == null) {
            String msg = "Argument 'user' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.location = l;
        this.app = app;
        this.user = user;
        this.errorList.addAll(Arrays.asList(errors));

    }

    public String query() {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing Briefcase Query: " + getClass().getName());
        }

        StringBuffer rslt = new StringBuffer();
        //FactoryBrokerEntity[] facs = null;
        EncryptionService encryptionService = app.getEncryptionService();

        // Location Info.
        String id = null;
        String type = null;
        String name = null;
        String path = null;
        String spaceAvailable = null;
        Object obj = location.getObject();
        AccessRule[] access = null;						// access levels that the user has on the current factory
        IResource[] contents = null;
        boolean addedTimeoutError = false;
        
        try{
	        if (obj instanceof IFolder) {
	            IFolder f = (IFolder) obj;
	            id = f.getUrl();
	            type = "folder";
	            name = f.getName();
	            path = f.getPath("/", true);
	            long sizeLimit = f.getOwner().getSizeLimit();
	            if (sizeLimit != 0) {
	                long space = sizeLimit - f.getOwner().getRoot().getSize();
	                space = space > 0L ? space : 0L;
	                spaceAvailable = formatSize(space);
	            }
	            
	            access = user.getAccessRules(f.getOwner());
	            
                try {
		            contents = f.getContents();
                } catch (OperationTimeoutException ote) {
                    if (!addedTimeoutError) {
                        addedTimeoutError = true;
	                    errorList.add(new ErrorMessage("other", "Operation timed out.", ""));
                    }
                    contents = new IResource[0];
                }
	            
	        } else if (obj instanceof Drive) {
	            Drive d = (Drive) obj;
	            id = d.getHandle();
	            type = "drive";
	            name = d.getLabel();
	            path = d.getHandle();
	            
	            Iterator entries = Arrays.asList(
	                    d.getBroker().getEntries(user.getPrincipal()))
	                    .iterator();
	            Set facs = new HashSet();
	            
	            // check for duplicate targets and view access
	            while (entries.hasNext()) {
	                IAccessEntry e = (IAccessEntry) entries.next();
	                if(hasAccess(e.getAccessRules(), BriefcaseAccessType.VIEW)){
	                    facs.add(e.getTarget());
	                }
	                
	            }
	            
	            entries = facs.iterator();
	            int i = 0;
	            contents = new IResource[facs.size()]; 
	            while (entries.hasNext()) {
	                IResourceFactory fac = (IResourceFactory) entries.next();
	                contents[i++] = fac.getRoot();
	            }       
	        }

	        // Encrypt location url
	        id = encryptionService.encrypt(id);
	
	        // Begin.
	        rslt.append("<state>");
	
	        // Settings.
	        rslt.append("<settings>");
	        rslt.append("<sharing disabled=\"")
	                        .append(Boolean.toString(!user.getDriveSelection().isSharing()).toLowerCase())
	                        .append("\" />");
	        rslt.append("</settings>");
	
	        // Navigation
	        String hasBack = user.getHistory().hasBack()?"true":"false";
	        rslt.append("<navigation hasBack=\"").append(hasBack).append("\">");
	
	        // Drives.
	        Iterator drives = user.getDrives().iterator();
	        while (drives.hasNext()) {
	
	            Drive d = (Drive) drives.next();
	
	            String encryptedHandle = encryptionService.encrypt(d.getHandle());
	            
	            rslt.append("<drive id=\"").append(EntityEncoder.encodeEntities(encryptedHandle)).append("\" class-large=\"")
	                        .append(d.getLargeIcon()).append("\" class-opened=\"")
	                        .append(d.getOpenIcon()).append("\" class-closed=\"")
	                        .append(d.getClosedIcon()).append("\">");
	            rslt.append("<label>").append(EntityEncoder.encodeEntities(d.getLabel())).append("</label>");
	            rslt.append("<description>").append(EntityEncoder.encodeEntities(d.getDescription())).append("</description>");
	
	            if (user.getDriveSelection().equals(d)) {
	
	                // Append Factories if the drive matches the current drive.
	                Iterator entries = Arrays.asList(
	                        d.getBroker().getEntries(user.getPrincipal()))
	                        .iterator();
	                Set facs = new HashSet();
	                
	                // check for duplicate targets
	                while (entries.hasNext()) {
	                    IAccessEntry e = (IAccessEntry) entries.next();
	                    if(hasAccess(e.getAccessRules(), BriefcaseAccessType.VIEW)){
	                        facs.add(e.getTarget());
	                    }                   
	                }
	                
	                entries = facs.iterator();
	                while (entries.hasNext()) {
	                    IResourceFactory fac = (IResourceFactory) entries.next();
                        try {
                            StringBuffer folderXml = new StringBuffer();
                            evaluateFolderXml(fac.getRoot(), location, folderXml);
                            rslt.append(folderXml);
                        } catch (OperationTimeoutException ote) {
                            if (!addedTimeoutError) {
                                addedTimeoutError = true;
	                            errorList.add(new ErrorMessage("other", "Operation timed out.", ""));
                            }
                        }
	                }                
	            }
	
	            rslt.append("</drive>");
	
	        }
	        rslt.append("</navigation>");

	        rslt.append("<location><current id=\"")
	        	.append(EntityEncoder.encodeEntities(id))
	        	.append("\" type=\"")
	        	.append(type)
	        	.append("\" isOpen=\"")
	        	.append(user.getHistory().isFolderOpen())
	        	.append("\">");
	        rslt.append("<name>");
	        rslt.append(EntityEncoder.encodeEntities(name));
	        rslt.append("</name>");
	        rslt.append("<path>");
	        rslt.append(EntityEncoder.encodeEntities(path));
	        rslt.append("</path>");
	        
	        if (access != null) {
	            for(int i = 0; i < access.length; i++){
	                if(access[i].getStatus()){
		                rslt.append("<accesstype>")
		                	.append(access[i].getAccessType().getName())
		                	.append("</accesstype>");
	                }
	            }
	        }
        
	        //rslt.append("<accesstype>View</accesstype>");
	        //rslt.append("<accesstype>Add</accesstype>");
	        //rslt.append("<accesstype>Delete</accesstype>");
	        rslt.append("</current>");
	        // not sure what the previous folder will be . Assuming that it will be sent in the folderView
	        /*rslt.append("<previous id=\"path\">");
	        rslt.append("<name>");
	        rslt.append("</name>");
	        rslt.append("<path>");
	        rslt.append("</path>");
	        rslt.append("<accesstype>View</accesstype>");
	        rslt.append("<accesstype>Add</accesstype>");
	        rslt.append("<accesstype>Delete</accesstype>");
	        rslt.append("</previous>");*/
	        rslt.append("</location>");
	
	        // Contents, paging, & sorting.
	        PagingState ps = user.getFolderPaging();
	        /*IResource[] contents = null;
	        if (obj instanceof IFolder) {
	            IFolder f = (IFolder) obj;
	            contents = f.getContents();
	        } else if (obj instanceof Drive) {
	            Drive d = (Drive) obj;
	            Iterator entries = Arrays.asList(
	                    d.getBroker().getEntries(user.getPrincipal()))
	                    .iterator();
	            Set facs = new HashSet();
	            
	            // check for duplicate targets
	            while (entries.hasNext()) {
	                IAccessEntry e = (IAccessEntry) entries.next();
	                facs.add(e.getTarget());
	            }
	            
	            entries = facs.iterator();
	            int i = 0;
	            contents = new IResource[facs.size()]; 
	            while (entries.hasNext()) {
	                IResourceFactory fac = (IResourceFactory) entries.next();
	                contents[i++] = fac.getRoot();
	            }      
	        }*/
	        
	        contents = AbstractResourceFactory.sortResources(contents, user.getFolderSortMethod().getComparator());
	        int totalPages = -1;
	        switch (ps.getItemsPerPage()) {
	            case PagingState.SHOW_ALL_ITEMS:
	                totalPages = 1;
	                break;
	            default:
	                // Account for complete pages.
	                totalPages = (contents.length / ps.getItemsPerPage());
	                // Account for partial page.
	                if (contents.length % ps.getItemsPerPage() > 0) {
	                    totalPages += 1;
	                }
	                // Account for empty contents (must always be a page).
	                if (totalPages == 0) {
	                    totalPages = 1;
	                }
	                break;
	        }
	        if (ps.getCurrentPage() == PagingState.LAST_PAGE) {
	            // action has been invoked to move to the last page...
	            ps.setCurrentPage(totalPages);
	        }
	        while (ps.getCurrentPage() > totalPages) {
	            // ratchet-down the current page where necessary.
	            ps.setCurrentPage(ps.getCurrentPage() - 1);
	        }
	        int firstDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) + 1;
	        int lastDisplayed = ((ps.getCurrentPage() -1) * ps.getItemsPerPage()) + ps.getItemsPerPage();
	        if (lastDisplayed > contents.length || ps.getItemsPerPage() == PagingState.SHOW_ALL_ITEMS) {
	            lastDisplayed = contents.length;
	        }
	        rslt.append("<contents currentpage=\"").append(Integer.toString(ps.getCurrentPage()))
	                    .append("\" perpage=\"").append(Integer.toString(ps.getItemsPerPage()))
	                    .append("\" totalpages=\"").append(Integer.toString(totalPages))
	                    .append("\" firstdisplayed=\"").append(Integer.toString(firstDisplayed))
	                    .append("\" lastdisplayed=\"").append(Integer.toString(lastDisplayed))
	                    .append("\" totalitems=\"").append(Integer.toString(contents.length)).append("\">");
	
	        for (int i = firstDisplayed - 1; i < lastDisplayed; i++) {
	        	
	        	IResource r = contents[i];
	        	if (r.isAvailable()) {
					String encryptedResourceUrl = encryptionService.encrypt(r.getUrl());
		
					
					rslt.append("<resource id=\"");
					rslt.append(EntityEncoder.encodeEntities(encryptedResourceUrl));
					rslt.append("\" name=\"");
					rslt.append(EntityEncoder.encodeEntities(r.getName()));
		
		            // Sharing.
		            if (user.getDriveSelection().isSharing() && r instanceof IFolder) {
		                Drive shareDrive = app.getDrive(user.getDriveSelection().getShareTarget());             
		                rslt.append("\" shared=\"");
		                rslt.append(app.getSharedEntries((IFolder) r, user.getUsername(), shareDrive).length > 0 ? "yes" : "no");
		            } else if (r instanceof IFolder) {
		            	rslt.append("\" shared=\"");
		            	rslt.append("no");
		            }
	
		            rslt.append("\">");
		            rslt.append("<mimetype>");
		            rslt.append(EntityEncoder.encodeEntities(r.getMimeType()));
		            rslt.append("</mimetype>");
		            rslt.append("<type>");
		            if (r.getType().equals(ResourceType.FOLDER)) {
		            	rslt.append("Folder");
		            } else {
		            	rslt.append("File");
		            }
		            rslt.append("</type>");
	
		            // Add download url only to file resources
		            if (r.getType().equals(ResourceType.FILE)) {
		                // Encrypt resource URL                                
		            	rslt.append("<url>");
		            	rslt.append(EntityEncoder.encodeEntities(DownloadURLUtil.createDownloadURL(encryptedResourceUrl, app.getName())));
		            	rslt.append("</url>");
		            }
	
		            rslt.append("<lastmod>");
		            rslt.append(dateFormatter.format(r.getDateModified()));
		            rslt.append("</lastmod>");
		            rslt.append("<size>");
		            if (r.getType().equals(ResourceType.FOLDER)) {
		            	rslt.append(((IFolder)r).getNumFolders() + ((IFolder)r).getNumFiles());
		            	rslt.append(" Items");
		            } else {
		            	rslt.append(formatSize(r.getSize()));
		            }
		            rslt.append("</size>");
		            rslt.append("</resource>");
	        	}
	        }
	        rslt.append("</contents>");
            
	        // Status.
            rslt.append("<status>");
            if (spaceAvailable != null) {
                rslt.append("<available-space>").append(spaceAvailable).append("</available-space>");
            }
            rslt.append("<total-shared>")
                            .append("0")        // ToDo:  Implement!
                            .append("</total-shared>");
            Iterator it = errorList.iterator();
            while (it.hasNext()) {
                ErrorMessage msg = (ErrorMessage) it.next();
                rslt.append(msg.toXml());
            }
            rslt.append("</status>");

            rslt.append("</state>");
        }catch(DemetriusException de){
            throw new RuntimeException("Error in creating the Folder State Query", de);
        }
//System.out.println("FolderQuery " + rslt.toString());
        return rslt.toString();
    }
    
    private boolean canAccess(IResource r) {
    	return r.getSize() >= 0;
    }

    public IDecisionCollection[] getDecisions() throws WarlockException {
        List rslt = new ArrayList();

        IEntityStore store = new JvmEntityStore();

        // Choice Collection:  changeFolderLocation.
        try {

            // Location.
            Object obj = user.getHistory().getLocation().getObject();
            String id = null;
            if (obj instanceof IFolder) {
                IFolder f = (IFolder) obj;
                id = f.getUrl();
            } else if (obj instanceof Drive) {
                Drive d = (Drive) obj;
                id = d.getHandle();
            }
            id = app.getEncryptionService().encrypt(id);
            IOption oLocation = store.createOption(Handle.create(id), null, TypeNone.INSTANCE);
            IChoice cLocation = store.createChoice(Handle.create("goLocation"), null, new IOption[] { oLocation }, 0, 1);

            // Choices.
            Handle h = Handle.create("changeFolderLocation");
            IChoiceCollection choices = store.createChoiceCollection(h, null, new IChoice[] { cLocation });

            // Location Value.
            ISelection sLocation = store.createSelection(oLocation, TypeNone.INSTANCE.parse(null));
            IDecision dLocation = store.createDecision(null, cLocation, new ISelection[] { sLocation });

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dLocation });
            rslt.add(dColl);
        } catch (Throwable t) {
            String msg = "FolderQuery failed to build its decision collection "
                                            + "for changeFolderLocation.";
            throw new WarlockException(msg, t);
        }

        // Choice Collection:  mainFolderForm.
        try {

            // Sorting.
            BriefcaseSortMethod method = user.getFolderSortMethod();
            IOption oSort = store.createOption(Handle.create(method.getDirection()), null, TypeNone.INSTANCE);
            IChoice cSort = cSort = store.createChoice(Handle.create(method.getMode() + "SortDirection"), null, new IOption[] { oSort }, 0, 1);
            ISelection sSort = store.createSelection(oSort, TypeNone.INSTANCE.parse(null));
            IDecision dSort = store.createDecision(null, cSort, new ISelection[] { sSort });

            // Items Per Page.
            int ipp = user.getFolderPaging().getItemsPerPage();
            IOption oNumItems = store.createOption(Handle.create(Integer.toString(ipp)), null, TypeNone.INSTANCE);
            IChoice cNumItems = store.createChoice(Handle.create("chooseDisplayNumber"), null, new IOption[] { oNumItems }, 0, 1);
            ISelection sNumItems = store.createSelection(oNumItems, TypeNone.INSTANCE.parse(null));
            IDecision dNumItems = store.createDecision(null, cNumItems, new ISelection[] { sNumItems });

            // Choices.
            Handle h = Handle.create("mainFolderForm");
            IChoiceCollection choices = store.createChoiceCollection(h, null, new IChoice[] { cNumItems, cSort });

            IDecisionCollection dColl = store.createDecisionCollection(choices, new IDecision[] { dNumItems, dSort });
            rslt.add(dColl);
        } catch (Throwable t) {
            String msg = "FolderQuery failed to build its decision collection "
                                            + "for mainFolderForm.";
            throw new WarlockException(msg, t);
        }

        return (IDecisionCollection[]) rslt.toArray(new IDecisionCollection[0]);

    }

    private String formatSize(long size) {
        StringBuffer formatted = new StringBuffer();
        double dSize = size;
        String units;
        if (dSize > 1048576)    {
            dSize /= 1048576.0;
            units = "MB";
        } else {
            dSize /= 1024.0;
            units = "KB";
        }
        formatted.append(formatter.format(dSize));
        formatted.append(" ");
        formatted.append(units);
        return formatted.toString();
    }

    private void evaluateFolderXml(IFolder f, Location location, StringBuffer out) throws DemetriusException{

        // Assertions.
        if (f == null) {
            String msg = "Argument 'f [IFolder]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (location == null) {
            String msg = "Argument 'location' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (out == null) {
            String msg = "Argument 'out' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        String fUrl = f.getUrl();
        String lUrl = "";   // default;;
        if (location.getObject() instanceof IFolder) {
            lUrl = ((IFolder) location.getObject()).getUrl();
        }

        // Evaluate shared status.
        String shared = "no";   // default...
        if (user.getDriveSelection().isSharing()) {
            Drive shareDrive = app.getDrive(user.getDriveSelection().getShareTarget());
            shared = app.getSharedEntries(f, user.getUsername(), shareDrive).length > 0 ? "yes" : "no";
        }

        EncryptionService encryptionService = app.getEncryptionService();
        String encryptedUrl = encryptionService.encrypt(fUrl);

        // Begin.
        out.append("<folder id=\"").append(EntityEncoder.encodeEntities(encryptedUrl))
                        .append("\" name=\"")
                        .append(EntityEncoder.encodeEntities(f.getName()))
                        .append("\" shared=\"")
                        .append(shared)
                        .append("\">");

        // Contents.
        if (lUrl.indexOf(fUrl) != -1) {
            // show contents up to and including the location folder...
            // check if the folder is in closed state
            if(!lUrl.equals(fUrl) || (lUrl.equals(fUrl) && user.getHistory().isFolderOpen())){
	            ResourceType[] types = new ResourceType[] { ResourceType.FOLDER };
	            IResource[] contents = f.getContents(types);
	            AbstractResourceFactory.sortResources(contents, BriefcaseSortMethod.NAME_ASCENDING.getComparator());
	            Iterator it = Arrays.asList(contents).iterator();
	            while (it.hasNext()) {
	                IFolder sub = (IFolder) it.next();
	                evaluateFolderXml(sub, location, out);
	            }
            }
        }


        // End.
        out.append("</folder>");

    }  
    
    static boolean hasAccess(AccessRule[] rules, AccessType type){
        
        for(int i = 0; i < rules.length; i++){
            if(rules[i].getAccessType().equals(type) && rules[i].getStatus()){
                return true;
            }
        }
        return false;
    }

}
