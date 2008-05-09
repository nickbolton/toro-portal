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

import java.util.Date;

import net.unicon.academus.apps.ErrorMessage;
import net.unicon.academus.apps.messaging.FactoryInfo;
import net.unicon.academus.apps.messaging.MessagingAccessType;
import net.unicon.academus.apps.messaging.MessagingUserContext;
import net.unicon.alchemist.EntityEncoder;
import net.unicon.alchemist.access.AccessType;
import net.unicon.mercury.IFolder;
import net.unicon.mercury.IMessage;
import net.unicon.mercury.MercuryException;
import net.unicon.mercury.SpecialFolder;
import net.unicon.penelope.IDecisionCollection;
import net.unicon.warlock.IStateQuery;
import net.unicon.warlock.WarlockException;

/**
 * State Query to be used as the base type for all Messaging Portlet queries.
 * InitialQuery is also the state query for the message_welcome screen.
 *
 * <p>Provided in this base implementation are a set a common queries that can
 * be referenced by the subclass using the following pattern:</p>
 * <pre>
 * public String query() throws WarlockException {
 *    StringBuffer rslt = new StringBuffer();
 *    rslt.append("&lt;state&gt;");
 *    super.commonQueries(rslt);
 *
 *    // ...Add any additional state information here.
 *
 *    rslt.append("&lt;/state&gt;");
 *    return rslt.toString();
 * }
 * </pre>
 *
 * <p>If any of the queries called by {@link #commonQueries(StringBuffer) commonQueries} need
 * extra information appended to them, use the following pattern:</p>
 * <pre>
 * protected void queryStatus(StringBuffer rslt) throws WarlockException {
 *     super.queryStatus(rslt);
 *
 *     // ...Append any other state information that belongs within
 *     // &lt;status&gt;&lt;/status&gt; elements.
 * }
 * </pre>
 *
 * @see #commonQueries(StringBuffer)
 */
public class InitialQuery implements IStateQuery
{
    private static final DateFormat formatter = new SimpleDateFormat();

    /** The user context associated with this state query instance. */
    protected final MessagingUserContext muc;
    /** The errors reported from the calling Action. */
    protected final ErrorMessage[] errors;

    /**
     * Construct an InitialQuery for the given user context.
     * @param muc User context to associate with the query.
     */
    public InitialQuery(MessagingUserContext muc) {
        this(muc, null);
    }

    /**
     * Construct an InitialQuery for the given user context.
     * @param muc User context to associate with the query.
     * @param errors Error messages to include in the state query
     */
    public InitialQuery(MessagingUserContext muc, ErrorMessage[] errors) {
        // Assertions
        if(muc == null){
            throw new IllegalArgumentException(
                    "Arguement 'muc [MessagingUserContext]' cannot be null.");
        }

        this.muc = muc;        
        this.errors = (errors != null ? errors : new ErrorMessage[0]);
    }

    /**
     * Retrieve the state information.
     *
     * <p>This default implementation returns the results of
     * {@link #commonQueries(StringBuffer) commonQueries} wrapped in
     * &lt;state&gt; elements.</p>
     * 
     * @see net.unicon.warlock.IStateQuery#query()
     * @see #commonQueries(StringBuffer)
     */
    public String query() throws WarlockException {
        StringBuffer rslt = new StringBuffer();

        rslt.append("<state>");
        commonQueries(rslt);
        rslt.append("</state>");

        return rslt.toString();
    }

    /**
     * Append a common (across Messaging Portlet) set of queries to the
     * provided StringBuffer.
     *
     * The following elements and methods are added:
     * <ul>
     * <li>&lt;status&gt;     - {@link #queryStatus(StringBuffer) queryStatus}
     * <li>&lt;settings&gt;   - {@link #querySettings(StringBuffer) querySettings}
     * <li>&lt;navigation&gt; - {@link #queryNavigation(StringBuffer) queryNavigation}
     * <li>&lt;current&gt;    - {@link #querySelections(StringBuffer) querySelections}
     * </ul>
     *
     * @see InitialQuery
     */
    protected void commonQueries(StringBuffer rslt) throws WarlockException {
        rslt.append("<status>");
        queryStatus(rslt);
        rslt.append("</status>");

        rslt.append("<settings>");
        querySettings(rslt);
        rslt.append("</settings>");

        rslt.append("<navigation>");
        queryNavigation(rslt);
        rslt.append("</navigation>");

        rslt.append("<current>");
        querySelections(rslt);
        rslt.append("</current>");
    }

    /**
     * Append any status information to the provided StringBuffer.
     *
     * The following elements are added:
     * <ul>
     * <li>&lt;errors&gt;
     * </ul>
     *
     * @see ErrorMessage#toXml()
     */
    protected void queryStatus(StringBuffer rslt) throws WarlockException {
        // Errors.
        for (int i = 0; i < errors.length; i++)
            rslt.append(errors[i].toXml());
    }

    /**
     * Append any global settings to the provided StringBuffer.
     *
     * The following elements are added:
     * <ul>
     * <li>&lt;upload-limit&gt;
     * </ul>
     *
     * @see net.unicon.academus.apps.messaging.MessagingApplicationContext#getUploadLimit()
     */
    protected void querySettings(StringBuffer rslt) throws WarlockException {
        rslt.append("<upload-limit>")
            .append(simpleFormatSize(muc.getAppContext().getUploadLimit()))
            .append("</upload-limit>");
        if (muc.getAppContext().allowXHTML())
            rslt.append("<xhtml value=\"allowed\" />");
    }

    /**
     * Append any current selections to the provided StringBuffer.
     *
     * The following elements are added, if they have relevent values:
     * <ul>
     * <li>&lt;account&gt;    - Currently active Account (MessageFactory)
     * <li>&lt;folder&gt;     - Currently selected message folder
     * <li>&lt;message&gt;    - Currently selected message(s) [ids and subject]
     * <li>&lt;time&gt;       - The current time, formatted using the default format for the locale.
     * <li>&lt;accesstype&gt; - Access types the current user has been granted.
     * </ul>
     *
     * @see net.unicon.academus.apps.messaging.MessagingUserContext
     * @see net.unicon.academus.apps.messaging.FactoryInfo
     * @see java.text.SimpleDateFormat
     */
    protected void querySelections(StringBuffer rslt) throws WarlockException {
        // Add current selections (factory, viewmode, folder and message as applicable)

        // Account.
        if (muc.hasFactorySelection()) {
            FactoryInfo fInfo = muc.getFactorySelection();
            rslt.append(fInfo.toXml());

            try {
                // Folder. (Inbox, Sent, Saved?)
                if (!muc.hasFolderSelection()) {
                    // Default to root (Inbox)
                    muc.setFolderSelection(fInfo.getFactory().getRoot());
                }
                rslt.append("<folder id=\"")
                    .append(EntityEncoder.encodeEntities(muc.getFolderSelection().getIdString()))
                    .append("\"><label>")
                    .append(EntityEncoder.encodeEntities(muc.getFolderSelection().getLabel()))
                    .append("</label></folder>");

                // Message selections.
                if (muc.hasMessageSelection()) {
                    IMessage[] msgSel = muc.getMessageSelection();
                    if (muc.getFolderSelection().getIdString().equals(SpecialFolder.SYSFOLDER.getLabel())){
	                    for (int i = 0; i < msgSel.length; i++) {
	                        rslt.append("<message id=\"")
	                            .append(EntityEncoder.encodeEntities(msgSel[i].getId()))
	                            .append("\"><subject>")
	                            .append(EntityEncoder.encodeEntities(msgSel[i].getSubject()))
	                            .append("</subject><status>read</status></message>");
	                    }
                    }else{
                        for (int i = 0; i < msgSel.length; i++) {
                            rslt.append("<message id=\"")
                                .append(EntityEncoder.encodeEntities(msgSel[i].getId()))
                                .append("\"><subject>")
                                .append(EntityEncoder.encodeEntities(msgSel[i].getSubject()))
                                .append("</subject><status>")
                                .append(msgSel[i].isUnread() ? "unread":"read")
                                .append("</status></message>");
                        }
                    }
                }

            } catch (MercuryException me) {
                throw new WarlockException(
                        "Failed to query IMessageFactory: "+fInfo.getId(), me);
            }

            rslt.append("<time>")
                .append(formatter.format(new Date(System.currentTimeMillis())))
                .append("</time>");

            AccessType[] atypes = fInfo.getAccessTypes();
            AccessType[] exclude = {
                    MessagingAccessType.SAVE,
                    MessagingAccessType.DETAIL_REPORT,
                    MessagingAccessType.ATTACH
            };
            for (int i = 0; i < atypes.length; i++) {
                boolean ex = false;
                for (int k = 0; !ex && k < exclude.length; k++)
                    if (exclude[k].equals(atypes[i]))
                        ex = true;
                if (!ex) {
                    rslt.append("<accesstype>")
                        .append(atypes[i].getName())
                        .append("</accesstype>");
                }
            }

            // Only provide SAVE accesstype if a save folder exists, and they have that permission.
            boolean saveFound = false;
            for (int i = 0; !saveFound && i < atypes.length; i++)
                if (atypes[i].equals(MessagingAccessType.SAVE))
                    saveFound = true;
            if (saveFound && !muc.getFolderSelection().getLabel()
                    .equals(SpecialFolder.SYSFOLDER.getLabel())
                    && !muc.getFolderSelection().getLabel()
                    .equals(SpecialFolder.SAVE.getLabel())) {
                IFolder f = null;
                try {
                    f = muc.getFactorySelection().getFactory().getSpecialFolder(SpecialFolder.SAVE);
                } catch (MercuryException me) {
                    // Swallow it.
                }
                if (f != null) {
                    rslt.append("<accesstype>")
                        .append(MessagingAccessType.SAVE.getName())
                        .append("</accesstype>");
                }
            }

            // ATTACH handled by ComposeMessageQuery, where it is needed.
            // DETAIL_REPORT handled by ReadMessageQuery, where it is needed.
        }
    }

    /**
     * Append navigation information to the provided StringBuffer.
     *
     * The following elements are added:
     * <ul>
     * <li>&lt;account&gt; - Accounts the user has available.
     *     <ul>
     *     <li>&lt;folder&gt; - Folders within the account that can be navigated to.
     *     </ul>
     * </ul>
     *
     * @see net.unicon.academus.apps.messaging.FactoryInfo#toXml(boolean)
     */
    protected void queryNavigation(StringBuffer rslt) throws WarlockException {
        boolean selection = muc.hasFactorySelection();
        FactoryInfo selected = null;
        FactoryInfo[] facts = muc.listFactories();

        if (selection)
            selected = muc.getFactorySelection();

        for (int i = 0; i < facts.length; i++) {
            rslt.append(facts[i].toXml( (selection && selected == facts[i]) ));
        }
    }

    /**
     * Retrieve any decisions to supply to the screens.
     *
     * This implementation returns an array of length zero.
     *
     * @see net.unicon.warlock.IStateQuery#getDecisions()
     */
    public IDecisionCollection[] getDecisions() throws WarlockException {
    	return new IDecisionCollection[0];
    }

    /**
     * Format a filesize, given in byes, into a user friendly string. For the
     * purposes of this method, multiples of 1024 are used to reach the next
     * unit prefix.
     *
     * <p>For example, if 3670016 were passed in, the returned value would be
     * "3.5 MB".</p>
     *
     * <p>Supported suffixes include:</p>
     * <ul>
     * <li>bytes
     * <li>KB
     * <li>MB
     * </ul>
     *
     * @param in Size to format
     * @return User friendly formatted String
     */
    protected String simpleFormatSize(long in) {
        StringBuffer rslt = new StringBuffer();

        if (in > 1024) {
            in = in/1024;
            if (in > 1024) {
                in = in/1024;
                rslt.append(String.valueOf(in))
                    .append("MB");
            } else {
                rslt.append(String.valueOf(in))
                    .append("KB");
            }
        } else {
            rslt.append(String.valueOf(in))
                .append(" bytes");
        }

        return rslt.toString();
    }
}
