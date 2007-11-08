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

package net.unicon.academus.apps.messaging;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletSession;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.IAcademusFacade;
import net.unicon.academus.api.IAcademusGroup;
import net.unicon.academus.civisselector.ISelectorUserContext;
import net.unicon.academus.civisselector.SelectorWorkflow;
import net.unicon.alchemist.access.Identity;
import net.unicon.alchemist.access.IdentityType;
import net.unicon.alchemist.access.Principal;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.civis.ICivisEntity;
import net.unicon.civis.ICivisFactory;
import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.RecipientSortMethod;
import net.unicon.mercury.SortMethod;
import net.unicon.mercury.cache.IMercuryListener;
import net.unicon.mercury.cache.MercuryCacheValidator;
import net.unicon.mercury.cache.MercuryCacheValidatorContainer;
import net.unicon.warlock.IScreen;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.IUserContext;

public class MessagingUserContext implements IUserContext, ISelectorUserContext, IMercuryListener {

    // Instance Members.
    private final MessagingApplicationContext app;
    private final String factory_handle;
    private final PortletSession session;
    private final String username;

    private IMessage[] messageList;
    private IMessage[] messageSel;
    private IFolder folderSel;
    private FactoryInfo factorySel;
    private ViewMode viewSel;
    private SortMethod msgSortMethod;
    private final PagingState msgPaging;
    private DraftMessage draft;
    private boolean draftSendEmail;
    private final UserDetailViewPreferences detailPref;
    private RecipientSortMethod recpSortMethod; 
    private Map downloadResources;
    private String exportResource;
    private Map callbackActions;
	private boolean messageListDirty;
	private boolean listen;

    // memebrs for ISelectorUserContext
    private IScreen screen = null;
    private IStateQuery query = null;
    private ICivisEntity[] entities = new ICivisEntity[0];
    private ICivisEntity[] prevEntities = new ICivisEntity[0];
    private SelectorWorkflow workflow = null;
    
    /*
     * Public API.
     */

    public MessagingUserContext(MessagingApplicationContext app, PortletSession session, String username) {
        // Assertions.
    	if(app == null) {
        	throw new IllegalArgumentException(
        			"Argument 'app' cannot be null.");
        }
    	if(session == null) {
        	throw new IllegalArgumentException(
        			"Argument 'session' cannot be null.");
        }
    	if(username == null) {
        	throw new IllegalArgumentException(
        			"Argument 'username' cannot be null.");
        }

    	if ("".equals(username)) {
            throw new IllegalArgumentException(
                    "Argument 'username' cannot be empty.");
        }

        // Instance Members.
        this.app               = app;
        this.factory_handle    = app.getId()+":FACTORIES";
        this.session           = session;
        this.username          = username;
        this.messageSel        = null;
        this.factorySel        = null;
        this.folderSel         = null;
        this.viewSel           = ViewMode.ALL;
        this.msgSortMethod     = SortMethod.DATE_DESCENDING;
        this.msgPaging         = new PagingState();
        this.draft             = null;
        this.draftSendEmail    = false;
        this.messageList       = null;
        this.detailPref        = new UserDetailViewPreferences();
        this.recpSortMethod    = RecipientSortMethod.RECIPIENT_ASCENDING;
        this.downloadResources = new HashMap();
        this.exportResource    = null;
        this.callbackActions   = null;
        this.messageListDirty  = true;
        
        register();
    }
    
    private void register() {
		if (!listen) {
			MercuryCacheValidator mcv = MercuryCacheValidatorContainer.getInstance();
			if (mcv != null)
				mcv.registerListener(this, getUsername());
		}
		this.listen = true;
	}
 
	private void unregister() {
		if (listen) {
			MercuryCacheValidator mcv = MercuryCacheValidatorContainer.getInstance();
			if (mcv != null)
				mcv.unregisterListener(this, getUsername());
		}
		this.listen = false;
	}
	
	public void cleanup() {
		unregister();
      AttachmentsHelper.cleanupAttachments(this);
      AttachmentsHelper.cleanupExport(this);
	}

    public MessagingApplicationContext getAppContext() { return this.app; }

    public FactoryInfo[] listFactories() {
        return getFactoryManager().listFactories();
    }

    /**
     * Retrieve the Message Factory identified by the given key.
     * @param key Identifier for the message factory instance.
     * @return The message factory identified by the given key
     */
    public FactoryInfo getFactory(String key) {
        return getFactoryManager().getFactory(key);
    }

    public void addFactory(FactoryInfo finfo) {
        getFactoryManager().addFactory(finfo);
    }

    public String getUsername() {
    	return username;
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
                ids[i] = new Identity(
                        groups[i].getGroupPaths(
                            IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR,
                            false)[0],
                        IdentityType.GROUP);
                ids[i + groups.length] = new Identity(
                        groups[i].getGroupPaths(
                            IAcademusGroup.GROUP_NAME_BASE_PATH_SEPARATOR,
                            false)[0] + "[" + groups[i].getKey() + "]",
                        IdentityType.GROUP);
            }
            ids[ids.length - 1] = new Identity(username, IdentityType.USER);
            rslt = new Principal(ids);
        } catch (Throwable t) {
            String msg = "Unable to evaluate the user's identity within academus.";
            throw new RuntimeException(msg, t);
        }
        return rslt;
    }

    public DraftMessage getDraft() {
        if (this.draft == null) {
            this.draft = new DraftMessage();
            this.draftSendEmail = false;
        }
        return this.draft;
    }

    public boolean getDraftSendEmailCopy() {
        return this.draftSendEmail;
    }

    public void setDraftSendEmailCopy(boolean val) {
        this.draftSendEmail = val;
    }

    public void clearDraft() {
        this.draftSendEmail = false;
        this.draft = null;
    }

    // api to access the messages selected
    public IMessage[] getMessageSelection() {

        // Assertions.
    	if (messageSel == null) {
    		String msg = "There currently is no message selection.";
    		throw new IllegalStateException(msg);
    	}
            
        
        IMessage[] rslt = new IMessage[messageSel.length];
        System.arraycopy(messageSel, 0, rslt, 0, messageSel.length);
        return rslt;
    }

    public void clearMessageSelection() {
        messageSel = null;
    }

    public void setMessageSelection(IMessage[] selection) {

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

        messageSel = new IMessage[selection.length];
        System.arraycopy(selection, 0, messageSel, 0, selection.length);

    }

    public boolean hasMessageSelection() {
        return messageSel != null;
    }

    public void setFactorySelection(FactoryInfo fInfo) {
        this.factorySel = fInfo;
        markAsDirty();
    }

    public FactoryInfo getFactorySelection() {
        return this.factorySel;
    }

    public boolean hasFactorySelection() {
        return this.factorySel != null;
    }

    public void clearFactorySelection() {
        this.factorySel = null;
    }

    public ViewMode getViewMode() {
        return this.viewSel;
    }

    public void resetViewMode() {
        this.viewSel = ViewMode.ALL;
		markAsDirty();
    }

    public void setViewMode(ViewMode m) {
        this.viewSel = m;
		markAsDirty();
    }

    public void setFolderSelection(IFolder folder) {
        this.folderSel = folder;
        markAsDirty();
    }

    public IFolder getFolderSelection() {
        return this.folderSel;
    }

    public void clearFolderSelection() {
        this.folderSel = null;
        markAsDirty();
    }

    public boolean hasFolderSelection() {
        return this.folderSel != null;
    }

    public SortMethod getMessageSortMethod() {
        return this.msgSortMethod;
    }

    public void reverseMessageSortMethod() {
        this.msgSortMethod = SortMethod.reverse(msgSortMethod);
    }

    public void setMessageSortMethod(SortMethod method) {
        this.msgSortMethod = method;
    }
    
    public PagingState getMessagePaging() {
        return msgPaging;
    }

    public void setMessageList(IMessage[] list) {
        this.messageList = list;
        this.messageListDirty = false;
    }

    public boolean isMessageListDirty() {
    	return this.messageListDirty;
    }
    
    public IMessage[] getMessageList() {
        return this.messageList;
    }

    public UserDetailViewPreferences getDetailPref() {
        return detailPref;
    }
    
    public RecipientSortMethod getRecipientSortMethod() {
        return this.recpSortMethod;
    }

    public void reverseRecipientSortMethod() {
        this.recpSortMethod = RecipientSortMethod.reverse(recpSortMethod);
    }

    public void setRecipientSortMethod(RecipientSortMethod method) {
        this.recpSortMethod = method;
    }

    /*
     * DownloadService support
     */

    public void addDownloadResource(String res, IMessage msg, IAttachment att) {
        try {
            this.downloadResources.put(msg.getId()+att.getId(), res);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add requested resource", e);
        }
    }

    public String getDownloadResource(IMessage msg, IAttachment att) {
        String rslt = null;
        try {
            rslt = (String)this.downloadResources.get(msg.getId()+att.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to add requested resource", e);
        }
        return rslt;
    }

    public String[] getDownloadResources() {
        return (String[])this.downloadResources.values().toArray(new String[0]);
    }

    public void removeDownloadResource(String res) {
        this.downloadResources.values().remove(res);
    }

    public void clearDownloadResources() {
        this.downloadResources.clear();
    }
    
    public void setExportResource(String res) {
        this.exportResource = res;
    }

    public String getExportResource() {
        return this.exportResource;
    }

    public Map getCallbackActions() { return this.callbackActions; }
    public void setCallbackActions(Map callbackActions) { this.callbackActions = callbackActions; }

    /*
     * Civis Selector Interface
     */
        
    public ICivisFactory[] getCivisFactories() {
        return new ICivisFactory[]{this.app.getAddressBook()};
    }
    
    public Map getGroupRestrictors() {
        return this.app.getGroupRestrictors();
    }

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
        return true;
    }

    /*
     * Private implementation.
     */

    private FactoryManager getFactoryManager() {
    	
    	// Assertions..
    	if (session == null) {
    		String msg = "No session created";
    		throw new IllegalStateException(msg);
    	}
        FactoryManager rslt = (FactoryManager)session.getAttribute(factory_handle,
                                             PortletSession.APPLICATION_SCOPE);

        if (rslt == null) {
            rslt = new FactoryManager();
            session.setAttribute(factory_handle, rslt,
                                 PortletSession.APPLICATION_SCOPE);
        }
        
        return rslt;
    }

	public void markAsDirty() {
		this.messageListDirty = true;
	}    
}
