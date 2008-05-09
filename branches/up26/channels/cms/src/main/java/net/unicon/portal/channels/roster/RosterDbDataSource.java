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
package net.unicon.portal.channels.roster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.jasig.portal.RDBMServices;
import net.unicon.sdk.catalog.db.IDbEntryConvertor;
import net.unicon.sdk.catalog.db.ADbMode;
import net.unicon.sdk.catalog.CatalogException;
import net.unicon.sdk.catalog.IDataSource;
import net.unicon.sdk.catalog.IFilterMode;
import net.unicon.sdk.catalog.IPageMode;
import net.unicon.sdk.catalog.ISortMode;

public final class RosterDbDataSource implements IDataSource {
    private String queryBase;
    private IDbEntryConvertor ec;
    /*
     * PUBLIC API
     */
    /**
     * Creates a new <code>RosterDbDataSource</code> from the specified
     * <code>queryBase</code> and <code>DbEntryConvertor</code>.  The
     * <code>queryBase</code> represents a 'SELECT' query before any filtering
     * criteria or sorting criteria have been applied.
     *
     * @param queryBase the beginnings of a 'SELECT' query.
     * @param ec a <code>DbEntryConvertor</code> to use in converting
     * <code>ResultSet</code> rows into catalog entries.
     */
    public RosterDbDataSource(String queryBase, IDbEntryConvertor ec) {
        // Assertions.
        if (queryBase == null) {
            String msg = "Argument queryBase cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (ec == null) {
            String msg = "Argument ec (DbEntryConvertor) cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Members.
        this.queryBase = queryBase.trim();
        this.ec = ec;
    }
    /**
     * Returns the collection of entries that conforms to the specified sorts,
     * filters, and page inputs.  Use <code>SortMode</code> objects to specify
     * the order of entries in the resultant collection.  Use
     * <code>FilterMode</code> objects to exclude some entries from the results.
     * Where appropriate, use the <code>PageMode</code> to specify how many
     * entries should appear on each page and which page(s) should be included
     * in the results.
     * <p>
     * Note that this method signature is identical to <code>subCatalog</code>
     * on the <code>Catalog</code> itself.  It is up to individual
     * <code>Catalog</code> implementations to decide when and how to access
     * their data.  For example, one <code>Catalog</code> might call
     * <code>fetchData</code> at creation time to store the entire entry set,
     * while another might only call <code>fetchData</code> at the last second
     * (when it needs to hand out its entries).
     * <p>
     * The <code>PageMode</code> also allows the <code>RosterDbDataSource</code> to
     * let its client know how many pages of entries are available given the
     * specified page size.
     *
     * @param sorts objects that specify how the resulting <code>Catalog</code>
     * should be ordered.
     * @param filters objects that specify which entries should be excluded from
     * the resulting <code>Catalog</code>.
     * @param page specifies how many entires should appear on each page and
     * which page(s) should be included in the resulting <code>Catalog</code>.
     * @return the collection of entries that conforms to the specified sorts,
     * filters, and page.
     */
    public List fetchData(ISortMode[] sorts, IFilterMode[] filters,
                                IPageMode page) throws CatalogException {
        // Assertions.
        if (sorts == null) {
            String msg = "Argument sorts cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (filters == null) {
            String msg = "Argument filters cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (page == null) {
            String msg = "Argument page cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        // Results.
        List rslts = new ArrayList();
        RDBMServices rdbms = new RDBMServices();
        // JDBC triad.
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // Hit the Db.
        String sql = null;
        try {
            conn = rdbms.getConnection();
            // Sql.
            sql = buildQuery(sorts, filters);
            pstmt = conn.prepareStatement(sql);
            // Parameters.
            Object[] objs = gatherParameters(sorts, filters);
            for (int i = 0; i < objs.length; i++) {
                pstmt.setObject(i + 1, objs[i]);
            }
            // Page constraints.
            int pgSize = page.getPageSize();
            int pgNum = page.getPageNumber();
            int fstRow = (pgSize * (pgNum - 1)) + 1;
            int lstRow = pgSize * pgNum;    // will be 0 if "View All"
            // Execute, loop & load.
            int rowCount = 0;
            rs = pstmt.executeQuery();
            List allRows = new ArrayList();
            while (rs.next()) {
                rowCount++;
                allRows.add(ec.convertRow(rs));
            }
            // Sort elements by first and last name.
            Collections.sort(allRows, new FullnameAscComparator());
            int currentRow = 0;
            for (int i = 0; i < allRows.size(); i++) {
                currentRow++;
                if (currentRow < fstRow) continue;
                if (lstRow != 0 && currentRow > lstRow) continue;
                rslts.add(allRows.get(i));
            }
            // Set the pageCount.
            int pgCnt = 1;  // Default.
            if (pgSize != 0) {
                pgCnt = (rowCount / pgSize)
                    + ((rowCount % pgSize == 0) ? 0 : 1);
            }
            if (pgCnt == 0) pgCnt++;    // Must always be a page, even if empty.
            page.setPageCount(pgCnt);
        } catch (SQLException sqle) {
            String msg = "RosterDbDataSource.fetchData failed.  The query was:  ";
            throw new CatalogException(msg + sql, sqle);
        } finally {
            StringBuffer msg = new StringBuffer();
            msg.append("RosterDbDataSource.fetchData failed ");
            msg.append("to release resources.");

            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (SQLException sqle) {
                throw new CatalogException(msg.toString(), sqle);
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                    pstmt = null;
                }
            } catch (SQLException sqle) {
                throw new CatalogException(msg.toString(), sqle);
            }
            rdbms.releaseConnection(conn);
        }
        return rslts;
    }
    /*
     * IMPLEMENTATION
     */
    private String buildQuery(ISortMode[] sorts, IFilterMode[] filters) {
        // Evaluate whether the queryBase has joins (where clauses) or not.
        boolean hasJoins = false;
        if (queryBase.toUpperCase().indexOf(" WHERE ") != -1) {
            hasJoins = true;
        }
        // Start the query.
        StringBuffer sql = new StringBuffer();
        sql.append(queryBase);
        // Add the filters.
        for (int i = 0; i < filters.length; i++) {
            sql.append((!hasJoins && i == 0) ? " WHERE " : " AND ");
            sql.append(((ADbMode)filters[i]).toSql());
        }
        // Add the sorts.
        for (int i = 0; i < sorts.length; i++) {
            sql.append((i == 0) ? " ORDER BY " : ", ");
            sql.append(((ADbMode)sorts[i]).toSql());
        }
        // Return.
        return sql.toString();
    }
    private static Object[] gatherParameters(ISortMode[] sorts,
                                            IFilterMode[] filters) {
        List objs = new ArrayList();
        Object[] oAry = null;
        // Filters.
        for (int i = 0; i < filters.length; i++) {
            oAry = ((ADbMode)filters[i]).getParameters();
            for (int j = 0; j < oAry.length; j++) objs.add(oAry[j]);
        }
        // sorts.
        for (int i = 0; i < sorts.length; i++) {
            oAry = ((ADbMode)sorts[i]).getParameters();
            for (int j = 0; j < oAry.length; j++) objs.add(oAry[j]);
        }
        return objs.toArray();
    }
}
