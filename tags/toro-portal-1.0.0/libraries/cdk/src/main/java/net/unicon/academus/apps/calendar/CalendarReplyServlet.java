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

package net.unicon.academus.apps.calendar;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.sql.DataSource;

import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.apps.rad.IdentityData;
import net.unicon.portal.channels.rad.GroupData;

import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.RDBMUserIdentityStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.PersonFactory;
import org.jasig.portal.services.GroupService;
import org.jasig.portal.services.PersonDirectory;

/**
 * Servlet used to handle calendar invitation responses from external services.
 */
public class CalendarReplyServlet extends HttpServlet {
    private static final String PARAM_CALENDAR_EVENT_ID = "ceid";
    private static final String PARAM_USERNAME          = "username";
    private static final String PARAM_ACTION            = "action";

    private static final int ERROR       = 0;
    private static final int ACCEPTED    = 1;
    private static final int DECLINED    = 2;
    private static final int NEEDSACCEPT = 3;

    private DataSource ds = null;
    private String selfurl = null;

    public void init() {
        try {
            ds = AcademusFacadeContainer.retrieveFacade().getAcademusDataSource();
        } catch(Exception ne) {
            throw new RuntimeException("Failed to retrieve DataSource", ne);
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        String ceid      = req.getParameter(PARAM_CALENDAR_EVENT_ID);
        String username  = req.getParameter(PARAM_USERNAME);
        String actionstr = req.getParameter(PARAM_ACTION);

        if (this.selfurl == null) {
            this.selfurl = "/" + req.getContextPath() + "/" + req.getServletPath();
        }

        res.setContentType("text/html");
        PrintWriter out = null;

        try {
            out = res.getWriter();

            if (ceid == null || ceid.equals("")
                    || username == null || username.equals("")) {

                renderMsg(out, "Insufficient Parameters.");

            } else {
                if (actionstr == null || actionstr.equals("")) {
                    displayForm(out, username, ceid);

                } else if (actionstr.equals("noti")) {
                    displayNotiForm(out, username, ceid);

                } else {
                    boolean accept = false;
                    if (actionstr.equalsIgnoreCase("accept"))
                        accept = true;
                    else if (actionstr.equalsIgnoreCase("decline"))
                        accept = false;
                    else
                        throw new IllegalArgumentException("Illegal action");

                    int state = checkState(username, ceid);
                    if (state == ERROR)
                        throw new IllegalStateException(
                                "Action requested for an invalid calendar event invitation");
                    else if (state != NEEDSACCEPT)
                        throw new IllegalStateException(
                                "The requested event has already been replied to.");

                    reply(username, ceid, accept);
                    renderMsg(out, (accept ? "Accepted" : "Declined"));
                }
            }

            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);

            renderMsg(out, "CalendarReplyServlet: An error has occurred during"
                  + " processing. Please contact a systems administrator for"
                  + " assistance. (Error: "+ex.getMessage()+")");
        } finally {
            out.close();
        }
    }

    // Direct interaction
    private void displayForm(PrintWriter out, String username, String ceid) {
        // Display buttons
        int state = checkState(username, ceid);

        if (state == ERROR) {
            StringBuffer rslt = new StringBuffer();

            rslt.append("Unable to process request for username ");
            rslt.append(username);
            rslt.append(" and calendar event ");
            rslt.append(ceid);
            rslt.append(". The requested event may no longer exist.");

            renderMsg(out, rslt.toString());

        } else if (state == NEEDSACCEPT) {
            StringBuffer rslt = new StringBuffer();

            rslt.append("<form method=\"post\" action=\"")
                .append(this.selfurl)
                .append("\">")
                .append("<input type=\"hidden\" name=\"username\" value=\"")
                .append(username).append("\" />")
                .append("<input type=\"hidden\" name=\"ceid\" value=\"")
                .append(ceid)
                .append("\" />")
                .append("<input type=\"hidden\" name=\"action\" value=\"accept\" />")
                .append("<input type=\"submit\" name=\"submit\" value=\"Accept\" />")
                .append("</form>");
            rslt.append("<form method=\"post\" action=\"")
                .append(this.selfurl)
                .append("\">")
                .append("<input type=\"hidden\" name=\"username\" value=\"")
                .append(username).append("\" />")
                .append("<input type=\"hidden\" name=\"ceid\" value=\"")
                .append(ceid)
                .append("\" />")
                .append("<input type=\"hidden\" name=\"action\" value=\"decline\" />")
                .append("<input type=\"submit\" name=\"submit\" value=\"Decline\" />")
                .append("</form>");

            renderMsg(out, rslt.toString());
        } else if (state == ACCEPTED) {
            renderMsg(out, "You have previously accepted this invitation.");
        } else { // DECLINED
            renderMsg(out, "You have previously declined this invitation.");
        }
    }

    // MessagingPortlet callback
    private void displayNotiForm(PrintWriter out, String username, String ceid) {
        int state = checkState(username, ceid);

        out.write("<response>");

        if (state == ERROR) {
            out.write("<label>Unable to process request for the specified calendar event. ");
            out.write("The requested event may no longer exist.</label>");

        } else if (state == NEEDSACCEPT) {
            out.write("<label>Invitation</label>");
            out.write("<description>Do you want to accept this invitation?</description>");

            // Accept
            out.write("<user-action ref=\"calendar\" handle=\"accept\">");
            out.write(  "<label>Accept</label>");
            out.write(  "<description>Accept the invitation</description>");
            out.write(  "<param name=\"username\" value=\"");
            out.write(username);
            out.write("\" />");
            out.write(  "<param name=\"ceid\" value=\"");
            out.write(ceid);
            out.write("\" />");
            out.write(  "<param name=\"action\" value=\"accept\" />");
            out.write("</user-action>");

            // Decline
            out.write("<user-action ref=\"calendar\" handle=\"decline\">");
            out.write(  "<label>Decline</label>");
            out.write(  "<description>Decline the invitation</description>");
            out.write(  "<param name=\"username\" value=\"");
            out.write(username);
            out.write("\" />");
            out.write(  "<param name=\"ceid\" value=\"");
            out.write(ceid);
            out.write("\" />");
            out.write(  "<param name=\"action\" value=\"decline\" />");
            out.write("</user-action>");

        } else if (state == ACCEPTED) {
            out.write("<label>You have previously accepted this invitation.</label>");
        } else { // DECLINED
            out.write("<label>You have previously declined this invitation.</label>");
        }
        out.write("</response>");

    }

    private void renderMsg (PrintWriter out, String inmsg) {

        StringBuffer msg = new StringBuffer(256);

        msg.append("<html><head><title>Calendar Event Reply</title></head><body>");
        msg.append(inmsg);
        msg.append("</body></html>");

        out.write(msg.toString());
    }

    private String getRADGroupList(String username) throws Exception {
        IGroupMember gm = GroupService.getGroupMember(username, IPerson.class);
        Iterator it = gm.getAllContainingGroups();
        ArrayList grps = new ArrayList();
        while (it.hasNext()) {
            IEntityGroup ieg = (IEntityGroup)it.next();
            grps.add(new GroupData(ieg, null, false));
        }

        StringBuffer rslt = new StringBuffer();
        if (grps.size() > 0) {
            rslt.append('(');

            it = grps.iterator();
            while (it.hasNext()) {
                GroupData g = (GroupData)it.next();
                rslt.append('\'').append(g.toString().replaceAll("'", "''")).append("',");
            }
            rslt.delete(rslt.length()-1, rslt.length());
            rslt.append(')');
        }
        return rslt.toString();
    }

    private String generateRADString(String username) throws Exception {
        IPerson p = PersonFactory.createPerson();

        p.setAttribute(IPerson.USERNAME, username);
        p.setID(new RDBMUserIdentityStore().getPortalUID(p));
        p.setAttribute("username", username);
        PersonDirectory.instance().getUserDirectoryInformation(username, p);

        String rslt = IdentityData.ENTITY + IdentityData.SEPARATOR + GroupData.S_USER
                + IdentityData.SEPARATOR + p.getID() + IdentityData.SEPARATOR
                + username + IdentityData.SEPARATOR + username;

        return rslt;
    }

    private int checkState(String username, String ceidstr) {
        int rslt = 0;
        int ceseq = 0;
        String ceid = null;
        String cuid;

        String[] toks = ceidstr.split("[_.]", 3);
        if (toks.length < 2) {
            ceid = ceidstr;
            ceseq = 0;
        } else {
            ceid = toks[0];
            ceseq = Integer.parseInt(toks[1]);
        }

        cuid = "%|"+username+"|%";

        Connection c = null;
        Statement s = null;
        ResultSet rs = null;
        try {
            c = ds.getConnection();
            s = c.createStatement();

            // "LIKE"s can't be parameritized, apparently, so let's pretend.
            final String select_sql =
                "SELECT status FROM dpcs_attendee WHERE ceid = '?' AND ceseq = ? AND "
                + "(cuid LIKE '?'?) ORDER BY cuid";

            String grstr = getRADGroupList(username);

            String query = null;

            query = select_sql;
            query = query.replaceFirst("\\?", ceid);
            query = query.replaceFirst("\\?", String.valueOf(ceseq));
            query = query.replaceFirst("\\?", cuid);
            query = query.replaceFirst("\\?", (!grstr.equals("") ? " OR cuid IN "+grstr : "") );

//System.out.println("CalendarReplyServlet: checkState query: "+query);

            rs = s.executeQuery(query);
            if (!rs.next()) {
                rslt = 0;
            } else {
                String tmp = rs.getString(1);
                if (tmp.equalsIgnoreCase("ACCEPTED"))
                    rslt = 1;
                else if (tmp.equalsIgnoreCase("DECLINED"))
                    rslt = 2;
                else
                    rslt = 3; // needs-accept
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Calendar invitation status check failed", e);

        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ex) {}
            try { if (s != null) s.close(); } catch (Exception ex) {}
            try { if (c != null) c.close(); } catch (Exception ex) {}
        }

        return rslt;
    }

    private void reply(String username, String ceidstr, boolean accept) {
        int ceseq = 0;
        String ceid = null;
        String cuid;

        String[] toks = ceidstr.split("[_.]", 3);
        if (toks.length < 2) {
            ceid = ceidstr;
            ceseq = 0;
        } else {
            ceid = toks[0];
            ceseq = Integer.parseInt(toks[1]);
        }

        cuid = "%|"+username+"|%";

        Connection c = null;
        Statement s = null;
        PreparedStatement ps = null;
        int ret = 0;
        try {
            c = ds.getConnection();
            s = c.createStatement();

            // "LIKE"s can't be parameritized, apparently, so let's pretend.
            final String update_sql =
                "UPDATE dpcs_attendee SET status = '?' WHERE ceid = '?' AND ceseq = ? AND cuid LIKE '?'";
            String query = update_sql;
            query = query.replaceFirst("\\?", (accept ? "ACCEPTED" : "DECLINED"));
            query = query.replaceFirst("\\?", ceid);
            query = query.replaceFirst("\\?", String.valueOf(ceseq));
            query = query.replaceFirst("\\?", cuid);

            ret = s.executeUpdate(query);
            if (ret == 0) {
                // Need to perform insert instead.
                final String insert_sql = "INSERT INTO dpcs_attendee VALUES('?',?,'?','?','?',?,'?')";

                query = insert_sql;
                query = query.replaceFirst("\\?", ceid);
                query = query.replaceFirst("\\?", String.valueOf(ceseq));
                query = query.replaceFirst("\\?", generateRADString(username));
                query = query.replaceFirst("\\?", "REQ_PARTICIPANT");
                query = query.replaceFirst("\\?", (accept ? "ACCEPTED" : "DECLINED"));
                query = query.replaceFirst("\\?", "1");
                query = query.replaceFirst("\\?", "");
 
                s.executeUpdate(query);
            }
            s.close();

            if (accept) {
                // Now add the event to the user's personal calendar
                final String insert_event_sql = "INSERT INTO DPCS_CAL_X_ENTRY VALUES (?,?,?)";
                ps = c.prepareStatement(insert_event_sql);
                ps.setString(1, username);
                ps.setString(2, ceid);
                ps.setInt(3, ceseq);
                ps.executeUpdate();
                ps.close();

                // And update the ref count on the entry..
                final String update_refs_sql = "UPDATE DPCS_ENTRY SET REFS = REFS+1 WHERE CEID=? AND CESEQ=?";
                ps = c.prepareStatement(update_refs_sql);
                ps.setString(1, ceid);
                ps.setInt(2, ceseq);
                ps.executeUpdate();
                ps.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Calendar invite update failed", e);

        } finally {
            try { if (ps != null) ps.close(); } catch (Exception ex) {}
            try { if (s != null) s.close(); } catch (Exception ex) {}
            try { if (c != null) c.close(); } catch (Exception ex) {}
        }
    }
}
