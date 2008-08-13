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

package net.unicon.academus.apps.briefcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletSession;

import net.unicon.alchemist.access.AccessBroker;
import net.unicon.alchemist.access.AccessRule;
import net.unicon.alchemist.access.IAccessEntry;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.PermissionsUtil;
import net.unicon.alchemist.access.Principal;
import net.unicon.alchemist.access.permissions.Dummy;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.civisselector.ISelectorUserContext;
import net.unicon.academus.civisselector.SelectorWorkflow;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.demetrius.IResource;
import net.unicon.demetrius.IResourceFactory;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BriefcaseUserContext implements IUserContext, ISelectorUserContext {

    // Instance Members.
    /**
     * Contains a list of <code>Drive</code>'s that the user associated with 
     * this context can interact with. This does not imply that the user has 
     * permission to access the drive
     */
    private final List<Drive> mountableDrives;
    private final String username;
    private final PortletSession session;
    private final BriefcaseApplicationContext app;
    private Drive driveSel;
    private final UserFolderHistory history;
    private final UserGroupHistory gHistory;
    private final PagingState folderPaging;
    private BriefcaseSortMethod folderSortMethod;
    private IResource[] resourceSel;
    private ShareeBasket targetSharees;
    private String charSel;
    private List sharedFolders;
    private UserPreferences editPermPagePref;
    protected final Log log = LogFactory.getLog(getClass());
    
    //  members for ISelectorUserContext
    private IScreen screen = null;
    private IStateQuery query = null;
    private ICivisEntity[] entities = new ICivisEntity[0];			// entries sent from the selector
    private ICivisEntity[] prevEntities = new ICivisEntity[0];		// entries to be sent to the selector (to show as perviously selected)
    private SelectorWorkflow workflow = null;
    
    /*
     * Public API.
     */

    public BriefcaseUserContext(String username, PortletSession session,
                                    BriefcaseApplicationContext app) {

        // Assertions.
        if (username == null || "".equals(username.trim())) {
            String msg = "Argument 'username' cannot be null or empty.";
            throw new IllegalArgumentException(msg);
        }
        if (session == null) {
            String msg = "Argument 'session' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (app == null) {
            String msg = "Argument 'app' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.username = username;
        this.session = session;
        this.app = app;
        this.gHistory = new UserGroupHistory();
        this.folderPaging = new PagingState();
        this.folderSortMethod = BriefcaseSortMethod.NAME_ASCENDING;
        this.resourceSel = null;
        this.targetSharees = new ShareeBasket();
        this.charSel = "all";
        this.sharedFolders = new ArrayList();
        this.editPermPagePref = new UserEditPreferences(); 

        Drive[] allDrives = app.getDrives();
        List<Drive> allDrivesList = Arrays.asList(allDrives);
        this.mountableDrives = getMountableDrives(username, allDrivesList);
        
        this.driveSel = this.mountableDrives.get(0);
        this.history = new UserFolderHistory(new Location(this.driveSel));
    }

    public String getUsername() {
        return username;
    }

    public PortletSession getSession() {
        return session;
    }

    public Principal getPrincipal() {
        Principal rslt = null;
        try {
            IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade();
            IAcademusGroup[] groups = facade.getAllContainingGroups(username);
            
            // Create an Identity array that includes all containing groups,
            // all containing groups formatted to include groupId, and username.
            Identity[] ids = new Identity[(groups.length * 2) + 1];
            for (int i=0; i < groups.length; i++) {
                ids[i] = new Identity(groups[i].getGroupPaths(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, false)[0],
                                                IdentityType.GROUP);
                ids[i + groups.length] = new Identity(groups[i].getGroupPaths(IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR, false)[0]
                                             + "[" + groups[i].getKey() + "]", IdentityType.GROUP);
            }
            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            rslt = new Principal(ids);
        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new RuntimeException(msg, t);
        }
        return rslt;
    }

    public Drive getDriveSelection() {
        return driveSel;
    }

    public void setDriveSelection(Drive d) {

        // Assertions.
        if (d == null) {
            String msg = "Argument 'd [Drive]' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        driveSel = d;

    }

    public UserFolderHistory getHistory() {
        return history;
    }

    public UserGroupHistory getGroupHistory() {
        return gHistory;
    }

    public PagingState getFolderPaging() {
        return folderPaging;
    }

    public BriefcaseSortMethod getFolderSortMethod() {
        return folderSortMethod;
    }

    public void setFolderSortMethod(BriefcaseSortMethod method) {

        // Assertions.
        if (method == null) {
            String msg = "Argument 'method' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        folderSortMethod = method;

    }

    public void setResourceSelection(IResource[] selection) {

        // Assertions.
        if (selection == null) {
            String msg = "Argument 'selection' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (selection.length == 0) {
            String msg = "Argument 'selection' must contain at least one "
                                                        + "element.";
            throw new IllegalArgumentException(msg);
        }

        resourceSel = new IResource[selection.length];
        System.arraycopy(selection, 0, resourceSel, 0, selection.length);

    }

    public boolean hasResourceSelection() {
        return resourceSel != null;
    }

    public IResource[] getResourceSelection() {

        // Assertions.
        if (resourceSel == null) {
            String msg = "There is currently no resource selection.";
            throw new IllegalStateException(msg);
        }

        IResource[] rslt = new IResource[resourceSel.length];
        System.arraycopy(resourceSel, 0, rslt, 0, resourceSel.length);
        return rslt;
    }

    public void clearResourceSelection() {
        resourceSel = null;
    }

    public boolean hasTargetSelection() {
        return targetSharees != null;
    }

    public ShareeBasket getTargetSelection() {

        // Assertions.
        if (targetSharees == null) {
            String msg = "There is currently no Target selection.";
            throw new IllegalStateException(msg);
        }

        return targetSharees;

    }

    public boolean hasSharedFolders() {
        return !sharedFolders.isEmpty();
    }

    public String getCharSelection() {
        return charSel;
    }

    public void clearCharSelection() {
        this.charSel = "all";
    }

    public void setCharSelection(String c) {
        this.charSel = c;
    }

   public UserPreferences getEditPreferences(){
        return this.editPermPagePref;
    }

    public void clearSharingPageSelections() {
        this.editPermPagePref.getPageState().setCurrentPage(PagingState.FIRST_PAGE);        
    }

    public BriefcaseApplicationContext getAppContext(){
        return this.app;
    }
    
    /*
     * Civis Selector Interface
     */
        
    public IScreen retrieveScreen() {
        return this.screen;
    }

    public IStateQuery retrieveQuery() {
        return this.query;
    }
    
    public void registerScreen(IScreen screen) {
        this.screen = screen;
    }

    public void registerQuery(IStateQuery query) {
        this.query = query;
    }

    public ICivisEntity[] getEntitySelection() {
       return this.entities;
    }

    public void setEntitySelection(ICivisEntity[] sel) {
        this.entities = new ICivisEntity[sel.length];
        System.arraycopy(sel, 0, entities, 0 , sel.length);        
    }
    
    public ICivisEntity[] getPrevEntitySelection() {
        return this.prevEntities;
     }

     public void setPrevEntitySelection(ICivisEntity[] sel) {
         this.prevEntities = new ICivisEntity[sel.length];
         System.arraycopy(sel, 0, prevEntities, 0 , sel.length);        
     }

    public SelectorWorkflow getSelectorWorkflow() {
        return this.workflow;
    }

    public void setSelectorWorkflow(SelectorWorkflow sWorkflow) {
        this.workflow = sWorkflow;        
    }
    
    public boolean includeOwner(){
        return false;
    }

    public ICivisFactory[] getCivisFactories() {
        return this.app.getCivisFactories();        
    }
    
    public Map getGroupRestrictors() {
        return this.app.getGroupRestrictors();        
    }
    
    /**
     * Obtains the access rules for a given resource.
     * 
     * @param target a resource to be accessed
     * @return An array of rules (permissions) that this user has on the target
     * @throws RuntimeException if the user does not have any permissions to
     *         access the target
     */
    public final AccessRule[] getAccessRules(IResourceFactory target) {
        
        // Assertions
        if(target == null){
          String msg = "Argument 'target' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        AccessRule[] rslt = null;
        
            // Get principal object with all groups to which the user belongs,
            // the same groups formatted to include groupId, and the username.
            Principal p = getPrincipal(); 
            
            List entryList = new LinkedList();
            List<Drive> failedDrives = new ArrayList<Drive>();
            
            // get the access types for all the mountableDrives for the given 
            // resource Factory.
            AccessBroker broker = null;
            IAccessEntry[] tempAccessEntry = null;
            for (Drive drive : mountableDrives) {
                broker = drive.getBroker();                
                
                // get all the accessTypes for the principal(user and groups the
                // user belongs to) on the given drive.
                try {
                    tempAccessEntry = broker.getEntries(p);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        // If there is a problem accessing/creating the Drive
                        // through its broker, then it should be removed from 
                        // the mountable list.
                        StringBuilder msg = new StringBuilder();
                        msg.append("User '");
                        msg.append(username);
                        msg.append("' failed to access the Drive: '");
                        msg.append(drive.getLabel());
                        msg.append("' after at least one previously ");
                        msg.append("successful access.");
                        msg.append("\nRemoving the drive from this users ");
                        msg.append("list.");
                        log.error(msg.toString(), e);
                    }
                    failedDrives.add(drive);
                }
                
                if(tempAccessEntry != null){
                    for(int j = 0; j < tempAccessEntry.length; j++){
                        if(tempAccessEntry[j].getTarget().equals(target)
                                || tempAccessEntry[j].getTarget().equals(
                                        new Dummy())){
                            entryList.add(tempAccessEntry[j]);
                        }
                    }
                }
            }
            if (failedDrives.size() > 0) {
                mountableDrives.removeAll(failedDrives);
            }
            if(entryList.isEmpty()){
                final String msg = 
                    "You do not have any access on the given resource: ";
                throw new RuntimeException(msg + target.getRoot().getName());
            }else{
                rslt = PermissionsUtil.coalesce(
                        (IAccessEntry[])entryList.toArray(new IAccessEntry[0]), 
                        BriefcaseAccessType.getInstances());
            }
               
        return rslt;
    }
    
    /**
     * Returns a list of Drives available to this user.
     * 
     * @return an immutable <code>List</code> of drives.
     */
    public List<Drive> getDrives() {
        return Collections.unmodifiableList(mountableDrives);
    }
    
    /**
     * Checks all of the configured drives to insure the current user can 
     * successfully interact with them (i.e. create the 
     * {@link IResourceFactory}'s through the {@link AccessBroker}'s that the 
     * drives represent). Returns a list of only those drives that are 
     * successful for this user.
     * 
     * <p>External resources such as a WebDAV hosted file system may have 
     * another level of permissions besides those managed by the 
     * <code>AccessBroker</code>'s which may fail during 
     * <code>IResourceFactory</code> creation. If that is the case, then those 
     * associated drives need to be removed from the list of "mountable" drives
     * for this user so that the Briefcase does not continually attempt to 
     * access them.</p>
     * 
     * <p>The user may or may not have portal level permissions to actually 
     * access the mountable drives.</p>
     * 
     * @param userName name of the user associated with this context
     * @param allConfiguredDrives <code>List</code> of all drives configured for
     *        the Briefcase
     * @return <code>List</code> of all drives available for the user associated
     *         with this context
     */
    private final List<Drive> getMountableDrives(String userName,
            List<Drive> allConfiguredDrives) {
        
        // Assertions
        if (userName == null || "".equals(userName.trim())) {
            final String msg = "Argument 'allConfiguredDrives' cannot be " +
                    "null or empty.";
            throw new IllegalArgumentException(msg);
        }
        if (allConfiguredDrives == null) {
            final String msg = "Argument 'allConfiguredDrives' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        // Get principal object with all groups to which the user belongs,
        // the same groups formatted to include groupId, and the username.
        Principal p = getPrincipal(); 
        
        List<Drive> mountableDriveList = new ArrayList<Drive>();
        
        // Get the access types for all the mountableDrives for the given 
        // resource Factory.
        AccessBroker broker = null;
        
        for (Drive drive : allConfiguredDrives) {
            
            broker = drive.getBroker();                
            
            // Attempt to access the drive through its broker
            try {
                
                broker.getEntries(p);
          
                // If there were no problems getting entries, then the factory
                // creations were successful. 
                mountableDriveList.add(drive);
            
            }
            catch (Exception e) {
                if (log.isErrorEnabled()) {
                    // If there is a problem accessing/creating the Drive
                    // through its broker, then it should not be in the 
                    // mountable list.
                    StringBuilder msg = new StringBuilder();
                    msg.append("User '");
                    msg.append(userName);
                    msg.append("' failed to access the Drive: '");
                    msg.append(drive.getLabel());
                    msg.append("'\nRemoving the drive from this users list.");
                    log.error(msg.toString(), e);
                }
            }
        }
        
        return mountableDriveList;
    }
}
