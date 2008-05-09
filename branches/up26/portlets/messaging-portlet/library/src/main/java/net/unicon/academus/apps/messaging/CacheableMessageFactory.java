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

import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.unicon.mercury.DraftMessage;
import net.unicon.mercury.Features;
import net.unicon.mercury.FolderNotFoundException;
import net.unicon.mercury.IAddress;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IMessageFactory;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.IRecipientType;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.Priority;
import net.unicon.mercury.SpecialFolder;
import net.unicon.mercury.cache.IMercuryListener;
import net.unicon.mercury.cache.MercuryCacheValidator;
import net.unicon.mercury.cache.MercuryCacheValidatorContainer;
import net.unicon.penelope.IChoiceCollection;
import net.unicon.penelope.IDecisionCollection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CacheableMessageFactory implements IMessageFactory, IMercuryListener {
	private static Log log = LogFactory.getLog(CacheableMessageFactory.class); 
	
	private IMessageFactory fac = null;
	private boolean dirty = false;
	private String target;
	private boolean listen;

	public CacheableMessageFactory(IMessageFactory fac, String target, boolean listen) {
		if (fac == null)
			throw new RuntimeException("Unable to create instance for null factory");
		this.fac = fac;
		this.target = target;
		this.dirty = false;
		setListen(listen);
	}

	public boolean isDirty() {
		return this.dirty;
	}
	
	public void resetDirty() {
		this.dirty = false;
	}
	
	public void markAsDirty() {
		this.dirty = true;
	}	
	
	public boolean getListen() {
		return this.listen;
	}
	
	public void setListen(boolean newListen) {
		if (newListen)
			register();
		else
			unregister();
	}
	
	private void register() {
		if (!listen) {
			MercuryCacheValidator mcv = MercuryCacheValidatorContainer.getInstance();
			if (mcv != null)
				mcv.registerListener(this, target);
		}
		this.listen = true;
	}

	private void unregister() {
		if (listen) {
			MercuryCacheValidator mcv = MercuryCacheValidatorContainer.getInstance();
			if (mcv != null)
				mcv.unregisterListener(this, target);
		}
		this.listen = false;
	}

	/*
	 * Delegate methods for IMessageFactory.
	 */
	
	public void cleanup() {
		fac.cleanup();
		unregister();
	}

	public IAttachment createAttachment(String filename, String type, InputStream stream) throws MercuryException {
		return fac.createAttachment(filename, type, stream);
	}

	public Features getFeatures() {
		return fac.getFeatures();
	}

	public IFolder getFolder(String id) throws FolderNotFoundException, MercuryException {
		return wrap(fac.getFolder(id));
	}

	public IMessage getMessage(String id) throws MercuryException {
		return wrap(fac.getMessage(id));
	}

	public IDecisionCollection[] getPreferences() {
		return fac.getPreferences();
	}

	public IRecipientType[] getRecipientTypes() {
		return fac.getRecipientTypes();
	}

	public IFolder getRoot() throws MercuryException {
		return wrap(fac.getRoot());
	}

	public IChoiceCollection getSearchCriteria() {
		return fac.getSearchCriteria();
	}

	public IFolder getSpecialFolder(SpecialFolder sFolder) throws MercuryException {
		return wrap(fac.getSpecialFolder(sFolder));
	}

	public String getUrl() {
		return fac.getUrl();
	}

	public void move(IMessage msg, IFolder fromFolder, IFolder toFolder) throws MercuryException {
		fac.move(unwrap(msg), unwrap(fromFolder), unwrap(toFolder));
		if (log.isDebugEnabled())
			log.debug("IMessageFolder::move(): Marking target '"+target+"' as dirty.");
		markOwnerAsDirty();
	}

	public IMessage[] search(IFolder[] where, IDecisionCollection filters) throws MercuryException {
		return fac.search(unwrap(where), filters);
	}

	public IMessage sendMessage(DraftMessage draft) throws MercuryException {
		IMessage rslt = fac.sendMessage(draft);
		markAsDirty(rslt);
		return rslt;
	}

	public IMessage sendMessage(IRecipient[] recipients, String subject, String body, IAttachment[] attachments, Priority priority) throws MercuryException {
		IMessage rslt = fac.sendMessage(recipients, subject, body, attachments, priority);
		markAsDirty(rslt);
		return rslt;
	}

	public IMessage sendMessage(IRecipient[] recipients, String subject, String body, Priority priority) throws MercuryException {
		IMessage rslt = fac.sendMessage(recipients, subject, body, priority);
		markAsDirty(rslt);
		return rslt;
	}
	
	protected class CacheableFolder implements IFolder {
		private IFolder fold = null;
		private IMessageFactory owner = null;
	
		protected CacheableFolder(CacheableMessageFactory owner, IFolder fold) {
			this.fold = fold;
			this.owner = owner;
		}

		public void addMessage(IMessage msg) throws MercuryException {
			fold.addMessage(unwrap(msg));
			if (log.isDebugEnabled())
				log.debug("IFolder::addMessage(): Marking target '"+target+"' as dirty.");
			markOwnerAsDirty();
		}

		public IFolder createSubfolder(String name) throws MercuryException {
			return wrap(fold.createSubfolder(name));
		}

		public void deleteFolder(boolean recurse) throws MercuryException {
			fold.deleteFolder(recurse);
		}

		public void expunge() throws MercuryException {
			fold.expunge();
		}

		public String getIdString() {
			return fold.getIdString();
		}

		public String getLabel() {
			return fold.getLabel();
		}

		public IMessage getMessage(String id) throws MercuryException {
			return wrap(fold.getMessage(id));
		}

		public IMessage[] getMessages() throws MercuryException {
			return wrap(fold.getMessages());
		}

		public IMessageFactory getOwner() {
			return owner;
		}

		public IFolder getParent() throws MercuryException {
			return wrap(fold.getParent());
		}

		public IFolder getSubfolder(String label) throws MercuryException {
			return wrap(fold.getSubfolder(label));
		}

		public IFolder[] getSubfolders() throws MercuryException {
			return wrap(fold.getSubfolders());
		}

		public int getUnreadCount() throws MercuryException {
			return fold.getUnreadCount();
		}

		public boolean removeMessage(IMessage msg) throws MercuryException {
			boolean rslt = fold.removeMessage(unwrap(msg));
			if (log.isDebugEnabled())
				log.debug("IFolder::removeMessage(): Marking target '"+target+"' as dirty.");
			markOwnerAsDirty();
			return rslt;
		}

		public IMessage[] search(IDecisionCollection filters, boolean recurse) throws MercuryException {
			return wrap(fold.search(filters, recurse));
		}

		public String toXml() throws MercuryException {
			return fold.toXml();
		}
	}

	protected class CacheableMessage implements IMessage {
		private IMessage msg = null;
		private CacheableMessageFactory owner;
		
		protected CacheableMessage(CacheableMessageFactory owner, IMessage msg) {
			this.msg = msg;
			this.owner = owner;
		}

		public String getAbstract() throws MercuryException {
			return msg.getAbstract();
		}

		public IAttachment[] getAttachments() throws MercuryException {
			return msg.getAttachments();
		}

		public String getBody() throws MercuryException {
			return msg.getBody();
		}

		public Date getDate() throws MercuryException {
			return msg.getDate();
		}

		public String getId() throws MercuryException {
			return msg.getId();
		}

		public IMessageFactory getOwner() {
			return owner;
		}

		public Priority getPriority() throws MercuryException {
			return msg.getPriority();
		}

		public IRecipient[] getRecipients() throws MercuryException {
			return msg.getRecipients();
		}

		public IRecipient[] getRecipients(IRecipientType[] types) throws MercuryException {
			return msg.getRecipients(types);
		}

		public IAddress getSender() throws MercuryException {
			return msg.getSender();
		}

		public String getSubject() throws MercuryException {
			return msg.getSubject();
		}

		public boolean isDeleted() throws MercuryException {
			return msg.isDeleted();
		}

		public boolean isUnread() throws MercuryException {
			return msg.isUnread();
		}

		public void setRead(boolean seen) throws MercuryException {
			msg.setRead(seen);
			if (log.isDebugEnabled())
				log.debug("IMessage::setRead(): Marking '"+target+"' as dirty.");
			markOwnerAsDirty();
		}

		public String toXml() throws MercuryException {
			return msg.toXml();
		}
	}

	protected void markOwnerAsDirty() {
      MercuryCacheValidator mcv = MercuryCacheValidatorContainer.getInstance();
      if (mcv != null) {
			mcv.markAsDirty(new String[] { target });
      }
	}

	protected void markAsDirty(IMessage mesg) throws MercuryException {
		Set targets = new HashSet();
		IRecipient[] recips = mesg.getRecipients();
    	for (int i = 0; i < recips.length; i++) {
    		targets.add(recips[i].getAddress().toNativeFormat());
    	}
    	targets.add(mesg.getSender().toNativeFormat());

      MercuryCacheValidator mcv = MercuryCacheValidatorContainer.getInstance();
      if (mcv != null) {
    		mcv.markAsDirty((String[])targets.toArray(new String[targets.size()]));
      }
	}
	
	protected IMessage wrap(IMessage in) {
		return new CacheableMessage(this, in);
	}
	
	protected IFolder wrap(IFolder in) {
		return new CacheableFolder(this, in);
	}
	
	protected IMessage unwrap(IMessage in) {
		return ((CacheableMessage)in).msg;
	}
	
	protected IFolder unwrap(IFolder in) {
		return ((CacheableFolder)in).fold;
	}

	protected IFolder[] wrap(IFolder[] in) {
		IFolder[] rslt = new IFolder[in.length];
		for (int i = 0; i < rslt.length; i++)
			rslt[i] = wrap(in[i]);
		return rslt;
	}
	
	protected IFolder[] unwrap(IFolder[] in) {
		IFolder[] rslt = new IFolder[in.length];
		for (int i = 0; i < rslt.length; i++)
			rslt[i] = unwrap(in[i]);
		return rslt;
	}
	
	protected IMessage[] wrap(IMessage[] in) {
		IMessage[] rslt = new IMessage[in.length];
		for (int i = 0; i < rslt.length; i++)
			rslt[i] = wrap(in[i]);
		return rslt;
	}
	
	protected IMessage[] unwrap(IMessage[] in) {
		IMessage[] rslt = new IMessage[in.length];
		for (int i = 0; i < rslt.length; i++)
			rslt[i] = unwrap(in[i]);
		return rslt;
	}
	
}
