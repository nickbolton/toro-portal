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

package net.unicon.academus.api;

// Java API
import java.io.PrintWriter;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;

// Academus Facade API
import net.unicon.academus.api.AcademusFacadeContainer;
import net.unicon.academus.api.AcademusFacadeException;
import net.unicon.academus.api.IAcademusFacade;


/**
	A facade to the Academus data source. The class delegates all
	calls to the actual Academus data source instance set up
	by the Academus portal.
*/
public class AcademusDataSource implements DataSource {

	private DataSource getDataSource () throws SQLException {

		IAcademusFacade facade = AcademusFacadeContainer.retrieveFacade();
		DataSource ds = null;

		try {

			ds = facade.getAcademusDataSource();

		} catch (AcademusFacadeException afe) {

			StringBuffer errorMsg = new StringBuffer(128);

			errorMsg.append("AcademusDataSource:");
			errorMsg.append("getDataSource():");
			errorMsg.append("Failed to retrieve Academus data source:");
			errorMsg.append(afe.getMessage());

			throw new SQLException(errorMsg.toString());
		}

		return ds;
	}

	public Connection getConnection() throws SQLException {

		DataSource ds = getDataSource();

		return ds.getConnection();
	}

	public Connection getConnection(String username, String password)
	throws SQLException {

		DataSource ds = getDataSource();

		return ds.getConnection(username, password);
	}

	public int getLoginTimeout() throws SQLException {

		DataSource ds = getDataSource();

		return ds.getLoginTimeout();
	}

	public PrintWriter getLogWriter() throws SQLException {

		DataSource ds = getDataSource();

		return ds.getLogWriter();
	}

	public void setLoginTimeout(int seconds) throws SQLException {

		DataSource ds = getDataSource();

		ds.setLoginTimeout(seconds);
	}

	public void setLogWriter(PrintWriter out)  throws SQLException {

		DataSource ds = getDataSource();

		ds.setLogWriter(out);
	}

	public AcademusDataSource() {

		// Nothing to do here. This class is just a facade to
		// the real data source.
    }
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new SQLException("Not implemented.");
	}
	public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLException("Not implemented.");
	}
}

