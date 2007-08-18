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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.FactoryInfo;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.academus.apps.messaging.ViewMode;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.paging.PagingState;
import net.unicon.mercury.IAttachment;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.IRecipient;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.SortMethod;
import net.unicon.mercury.SpecialFolder;
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
import net.unicon.warlock.WarlockException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * State Query for the message_list screen.
 * 
 * @author eandresen
 */
public class MessageListQuery extends InitialQuery {
	private static Log log = LogFactory.getLog(MessageListQuery.class); 
	private static final DateFormat formatter = new SimpleDateFormat();

	public MessageListQuery(MessagingUserContext muc) {
		super(muc);
	}

	public MessageListQuery(MessagingUserContext muc, ErrorMessage[] errors) {
		super(muc, errors);
	}

	public String query() throws WarlockException {
		StringBuffer rslt = new StringBuffer();
		FactoryInfo[] facts = muc.listFactories();

		rslt.append("<state>");
		super.commonQueries(rslt);

		IFolder f = muc.getFolderSelection();
		IMessage[] msgs = null;
		IMessage m = null;
		ViewMode mode = muc.getViewMode();

		// Clear any message selections.
		muc.clearMessageSelection();
		muc.clearDraft();
		muc.getDetailPref().setCurrentMessageIndex(-1);

		// Synchronize on muc for message list cache purposes.
		synchronized (muc) {
			msgs = muc.getMessageList();
			if (msgs == null || msgs.length == 0 || muc.isMessageListDirty()) {
				if (log.isDebugEnabled())
					log.debug("["+muc.getUsername()+"] Refreshing message list.");
	
				try {
					msgs = f.getMessages();

					// Narrow the results based on view mode.
					if (msgs.length > 0 && mode != ViewMode.ALL) {
						List mlist = new ArrayList();
						try {
							for (int i = 0; i < msgs.length; i++) {
								m = msgs[i];
								boolean unread = m.isUnread();
								if ((mode == ViewMode.READ && !unread)
										|| (mode == ViewMode.UNREAD && unread))
									mlist.add(m);
							}
						} catch (MercuryException me) {
							throw new WarlockException(
									"Error enumerating messages for folder: "
											+ f.getLabel(), me);
						}
						msgs = (IMessage[]) mlist.toArray(new IMessage[0]);
					}
				} catch (MercuryException me) {
					throw new WarlockException(
							"Error retrieving messages for folder: "
									+ f.getLabel(), me);
				}
			}

			// Sort the list.
			Arrays.sort(msgs, muc.getMessageSortMethod().getComparator());

			// Save the list (for Detail View navigation).
			muc.setMessageList(msgs);
		}

		// Paging.
		int perpage = muc.getMessagePaging().getItemsPerPage();
		int currentPage = muc.getMessagePaging().getCurrentPage();
		int totalPages = 0;
		if (perpage != PagingState.SHOW_ALL_ITEMS)
			totalPages = (int) Math.ceil((double) msgs.length
					/ (double) perpage);
		totalPages = (totalPages > 0 ? totalPages : 1);

		if (currentPage == PagingState.LAST_PAGE || currentPage > totalPages) {
			currentPage = totalPages;
			muc.getMessagePaging().setCurrentPage(totalPages);
		}

		int firstItem = (currentPage - 1) * perpage + 1;
		int lastItem = firstItem + perpage - 1;
		if (lastItem > msgs.length || perpage == PagingState.SHOW_ALL_ITEMS)
			lastItem = msgs.length;

		rslt.append("<contents currentpage=\"").append(currentPage).append(
				"\" perpage=\"").append(perpage).append("\" totalpages=\"")
				.append(totalPages).append("\" firstdisplayed=\"").append(
						firstItem).append("\" lastdisplayed=\"").append(
						lastItem).append("\" totalitems=\"")
				.append(msgs.length).append("\">");
		try {
			for (int i = firstItem - 1; i < lastItem; i++) {
				messageQuery(msgs[i], rslt);
			}
		} catch (MercuryException me) {
			throw new WarlockException(
					"Error enumerating messages for folder: " + f.getLabel(),
					me);
		}
		rslt.append("</contents>");

		rslt.append("</state>");

		return rslt.toString();
	}

	private void messageQuery(IMessage msg, StringBuffer rslt)
			throws MercuryException {
		IRecipient[] recipients = null;
		boolean isSent = muc.getFolderSelection().getLabel().equalsIgnoreCase(
				SpecialFolder.OUTBOX.getLabel());

		if (isSent)
			recipients = msg.getRecipients();

		// Begin.
		rslt.append("<message ");

		if (isSent)
			rslt.append("recip-count=\"").append(recipients.length).append(
					"\" ");

		// Message identifier.
		rslt.append("id=\"").append(EntityEncoder.encodeEntities(msg.getId()))
				.append("\"> ");

		// Received Date.
		rslt.append("<received>").append(formatter.format(msg.getDate()))
				.append("</received>");

		// Read status.
		rslt.append("<status>").append((msg.isUnread() ? "unread" : "read"))
				.append("</status>");

		// Priority.
		rslt.append("<priority>").append(msg.getPriority().toInt()).append(
				"</priority>");

		// Sender.
		rslt.append("<sender>");
		rslt.append(EntityEncoder.encodeEntities(msg.getSender().getLabel()));
		rslt.append("</sender>");

		if (isSent && recipients.length > 0) {
			// Recipients.
			rslt.append("<recipient type=\"").append(
					EntityEncoder.encodeEntities(recipients[0].getType()
							.getLabel())).append("\">").append(
					EntityEncoder.encodeEntities(recipients[0].getAddress()
							.getLabel())).append("</recipient>");
		}

		// Subject
		rslt.append("<subject>").append(
				EntityEncoder.encodeEntities(msg.getSubject())).append(
				"</subject>");

		/*
		 * Not used in message list. // Body. rslt.append("<body>")
		 * .append(EntityEncoder.encodeEntities(msg.getBody())) .append("</body>");
		 */

		// Attachments.
		IAttachment[] attachments = msg.getAttachments();
		rslt.append("<attachments total=\"").append(attachments.length).append(
				"\"></attachments>");

		/*
		 * TODO: Not implemented yet. // Expiration rslt.append("<expires></expires>");
		 */

		// End.
		rslt.append("</message>");
	}

	public IDecisionCollection[] getDecisions() throws WarlockException {
		List rslt = new ArrayList();
		IEntityStore store = new JvmEntityStore();

		// Choice Collection: messageListForm.
		try {
			// Selected folder.
			String folder = muc.getFolderSelection().getIdString();
			IOption oFolderSelection = store.createOption(
					Handle.create(folder), null, TypeNone.INSTANCE);
			IChoice cFolderSelection = store.createChoice(Handle
					.create("chooseFolder"), null,
					new IOption[] { oFolderSelection }, 0, 1);
			ISelection sFolderSelection = store.createSelection(
					oFolderSelection, TypeNone.INSTANCE.parse(null));
			IDecision dFolderSelection = store.createDecision(null,
					cFolderSelection, new ISelection[] { sFolderSelection });

			// Sorting.
			SortMethod method = muc.getMessageSortMethod();
			IOption oSort = store.createOption(Handle.create(method
					.getDirection()), null, TypeNone.INSTANCE);
			IChoice cSort = store.createChoice(Handle.create(method.getMode()
					+ "SortDirection"), null, new IOption[] { oSort }, 0, 1);
			ISelection sSort = store.createSelection(oSort, TypeNone.INSTANCE
					.parse(null));
			IDecision dSort = store.createDecision(null, cSort,
					new ISelection[] { sSort });

			// Items Per Page.
			int ipp = muc.getMessagePaging().getItemsPerPage();
			IOption oNumItems = store.createOption(Handle.create(Integer
					.toString(ipp)), null, TypeNone.INSTANCE);
			IChoice cNumItems = store.createChoice(Handle
					.create("chooseDisplayNumber"), null,
					new IOption[] { oNumItems }, 0, 1);
			ISelection sNumItems = store.createSelection(oNumItems,
					TypeNone.INSTANCE.parse(null));
			IDecision dNumItems = store.createDecision(null, cNumItems,
					new ISelection[] { sNumItems });

			// View filter
			String view = muc.getViewMode().toString();
			IOption oViewFilter = store.createOption(Handle.create(view), null,
					TypeNone.INSTANCE);
			IChoice cViewFilter = store.createChoice(Handle
					.create("viewFilter"), null, new IOption[] { oViewFilter },
					0, 1);
			ISelection sViewFilter = store.createSelection(oViewFilter,
					TypeNone.INSTANCE.parse(null));
			IDecision dViewFilter = store.createDecision(null, cViewFilter,
					new ISelection[] { sViewFilter });

			// Choices.
			Handle h = Handle.create("mainFolderForm");
			IChoiceCollection choices = store.createChoiceCollection(h, null,
					new IChoice[] { cFolderSelection, cNumItems, cSort,
							cViewFilter });

			IDecisionCollection dColl = store.createDecisionCollection(choices,
					new IDecision[] { dFolderSelection, dNumItems, dSort,
							dViewFilter });
			rslt.add(dColl);

		} catch (Throwable t) {
			throw new RuntimeException(
					"MessageListQuery failed to build its decision collection "
							+ "for messageListForm.", t);
		}

		return (IDecisionCollection[]) rslt.toArray(new IDecisionCollection[0]);
	}
}
