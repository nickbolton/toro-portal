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
package net.unicon.resource;

public class DatabaseMetaDataComponent extends ResourceComponent implements java.sql.DatabaseMetaData {
	public DatabaseMetaDataComponent(ResourceThing parent, java.sql.DatabaseMetaData rawResourceComponent) {
		super(parent, rawResourceComponent);
	}

	private java.sql.DatabaseMetaData myRawResourceComponent() {
		return (java.sql.DatabaseMetaData) rawResourceComponent;
	}

	@Override
    protected void closeRawResource() {
	}

	public java.lang.String getURL() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getURL();
		return answer;
	}

	public boolean isReadOnly() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isReadOnly();
		return answer;
	}

	public java.sql.ResultSet getAttributes(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, java.lang.String parameter3) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getAttributes(parameter0, parameter1, parameter2, parameter3);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public boolean allProceduresAreCallable() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().allProceduresAreCallable();
		return answer;
	}

	public boolean allTablesAreSelectable() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().allTablesAreSelectable();
		return answer;
	}

	public java.lang.String getUserName() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getUserName();
		return answer;
	}

	public boolean nullsAreSortedHigh() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().nullsAreSortedHigh();
		return answer;
	}

	public boolean nullsAreSortedLow() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().nullsAreSortedLow();
		return answer;
	}

	public boolean nullsAreSortedAtStart() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().nullsAreSortedAtStart();
		return answer;
	}

	public boolean nullsAreSortedAtEnd() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().nullsAreSortedAtEnd();
		return answer;
	}

	public java.lang.String getDatabaseProductName() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getDatabaseProductName();
		return answer;
	}

	public java.lang.String getDatabaseProductVersion() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getDatabaseProductVersion();
		return answer;
	}

	public java.lang.String getDriverName() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getDriverName();
		return answer;
	}

	public java.lang.String getDriverVersion() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getDriverVersion();
		return answer;
	}

	public int getDriverMajorVersion() {
		checkActive();
		int answer = myRawResourceComponent().getDriverMajorVersion();
		return answer;
	}

	public int getDriverMinorVersion() {
		checkActive();
		int answer = myRawResourceComponent().getDriverMinorVersion();
		return answer;
	}

	public boolean usesLocalFiles() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().usesLocalFiles();
		return answer;
	}

	public boolean usesLocalFilePerTable() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().usesLocalFilePerTable();
		return answer;
	}

	public boolean supportsMixedCaseIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsMixedCaseIdentifiers();
		return answer;
	}

	public boolean storesUpperCaseIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().storesUpperCaseIdentifiers();
		return answer;
	}

	public boolean storesLowerCaseIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().storesLowerCaseIdentifiers();
		return answer;
	}

	public boolean storesMixedCaseIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().storesMixedCaseIdentifiers();
		return answer;
	}

	public boolean supportsMixedCaseQuotedIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsMixedCaseQuotedIdentifiers();
		return answer;
	}

	public boolean storesUpperCaseQuotedIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().storesUpperCaseQuotedIdentifiers();
		return answer;
	}

	public boolean storesLowerCaseQuotedIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().storesLowerCaseQuotedIdentifiers();
		return answer;
	}

	public boolean storesMixedCaseQuotedIdentifiers() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().storesMixedCaseQuotedIdentifiers();
		return answer;
	}

	public java.lang.String getIdentifierQuoteString() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getIdentifierQuoteString();
		return answer;
	}

	public java.lang.String getSQLKeywords() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getSQLKeywords();
		return answer;
	}

	public java.lang.String getNumericFunctions() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getNumericFunctions();
		return answer;
	}

	public java.lang.String getStringFunctions() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getStringFunctions();
		return answer;
	}

	public java.lang.String getSystemFunctions() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getSystemFunctions();
		return answer;
	}

	public java.lang.String getTimeDateFunctions() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getTimeDateFunctions();
		return answer;
	}

	public java.lang.String getSearchStringEscape() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getSearchStringEscape();
		return answer;
	}

	public java.lang.String getExtraNameCharacters() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getExtraNameCharacters();
		return answer;
	}

	public boolean supportsAlterTableWithAddColumn() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsAlterTableWithAddColumn();
		return answer;
	}

	public boolean supportsAlterTableWithDropColumn() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsAlterTableWithDropColumn();
		return answer;
	}

	public boolean supportsColumnAliasing() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsColumnAliasing();
		return answer;
	}

	public boolean nullPlusNonNullIsNull() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().nullPlusNonNullIsNull();
		return answer;
	}

	public boolean supportsConvert() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsConvert();
		return answer;
	}

	public boolean supportsConvert(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsConvert(parameter0, parameter1);
		return answer;
	}

	public boolean supportsTableCorrelationNames() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsTableCorrelationNames();
		return answer;
	}

	public boolean supportsDifferentTableCorrelationNames() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsDifferentTableCorrelationNames();
		return answer;
	}

	public boolean supportsExpressionsInOrderBy() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsExpressionsInOrderBy();
		return answer;
	}

	public boolean supportsOrderByUnrelated() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsOrderByUnrelated();
		return answer;
	}

	public boolean supportsGroupBy() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsGroupBy();
		return answer;
	}

	public boolean supportsGroupByUnrelated() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsGroupByUnrelated();
		return answer;
	}

	public boolean supportsGroupByBeyondSelect() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsGroupByBeyondSelect();
		return answer;
	}

	public boolean supportsLikeEscapeClause() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsLikeEscapeClause();
		return answer;
	}

	public boolean supportsMultipleResultSets() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsMultipleResultSets();
		return answer;
	}

	public boolean supportsMultipleTransactions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsMultipleTransactions();
		return answer;
	}

	public boolean supportsNonNullableColumns() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsNonNullableColumns();
		return answer;
	}

	public boolean supportsMinimumSQLGrammar() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsMinimumSQLGrammar();
		return answer;
	}

	public boolean supportsCoreSQLGrammar() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCoreSQLGrammar();
		return answer;
	}

	public boolean supportsExtendedSQLGrammar() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsExtendedSQLGrammar();
		return answer;
	}

	public boolean supportsANSI92EntryLevelSQL() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsANSI92EntryLevelSQL();
		return answer;
	}

	public boolean supportsANSI92IntermediateSQL() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsANSI92IntermediateSQL();
		return answer;
	}

	public boolean supportsANSI92FullSQL() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsANSI92FullSQL();
		return answer;
	}

	public boolean supportsIntegrityEnhancementFacility() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsIntegrityEnhancementFacility();
		return answer;
	}

	public boolean supportsOuterJoins() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsOuterJoins();
		return answer;
	}

	public boolean supportsFullOuterJoins() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsFullOuterJoins();
		return answer;
	}

	public boolean supportsLimitedOuterJoins() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsLimitedOuterJoins();
		return answer;
	}

	public java.lang.String getSchemaTerm() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getSchemaTerm();
		return answer;
	}

	public java.lang.String getProcedureTerm() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getProcedureTerm();
		return answer;
	}

	public java.lang.String getCatalogTerm() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getCatalogTerm();
		return answer;
	}

	public boolean isCatalogAtStart() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().isCatalogAtStart();
		return answer;
	}

	public java.lang.String getCatalogSeparator() throws java.sql.SQLException {
		checkActive();
		java.lang.String answer = myRawResourceComponent().getCatalogSeparator();
		return answer;
	}

	public boolean supportsSchemasInDataManipulation() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSchemasInDataManipulation();
		return answer;
	}

	public boolean supportsSchemasInProcedureCalls() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSchemasInProcedureCalls();
		return answer;
	}

	public boolean supportsSchemasInTableDefinitions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSchemasInTableDefinitions();
		return answer;
	}

	public boolean supportsSchemasInIndexDefinitions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSchemasInIndexDefinitions();
		return answer;
	}

	public boolean supportsSchemasInPrivilegeDefinitions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSchemasInPrivilegeDefinitions();
		return answer;
	}

	public boolean supportsCatalogsInDataManipulation() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCatalogsInDataManipulation();
		return answer;
	}

	public boolean supportsCatalogsInProcedureCalls() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCatalogsInProcedureCalls();
		return answer;
	}

	public boolean supportsCatalogsInTableDefinitions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCatalogsInTableDefinitions();
		return answer;
	}

	public boolean supportsCatalogsInIndexDefinitions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCatalogsInIndexDefinitions();
		return answer;
	}

	public boolean supportsCatalogsInPrivilegeDefinitions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCatalogsInPrivilegeDefinitions();
		return answer;
	}

	public boolean supportsPositionedDelete() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsPositionedDelete();
		return answer;
	}

	public boolean supportsPositionedUpdate() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsPositionedUpdate();
		return answer;
	}

	public boolean supportsSelectForUpdate() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSelectForUpdate();
		return answer;
	}

	public boolean supportsStoredProcedures() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsStoredProcedures();
		return answer;
	}

	public boolean supportsSubqueriesInComparisons() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSubqueriesInComparisons();
		return answer;
	}

	public boolean supportsSubqueriesInExists() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSubqueriesInExists();
		return answer;
	}

	public boolean supportsSubqueriesInIns() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSubqueriesInIns();
		return answer;
	}

	public boolean supportsSubqueriesInQuantifieds() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSubqueriesInQuantifieds();
		return answer;
	}

	public boolean supportsCorrelatedSubqueries() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsCorrelatedSubqueries();
		return answer;
	}

	public boolean supportsUnion() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsUnion();
		return answer;
	}

	public boolean supportsUnionAll() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsUnionAll();
		return answer;
	}

	public boolean supportsOpenCursorsAcrossCommit() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsOpenCursorsAcrossCommit();
		return answer;
	}

	public boolean supportsOpenCursorsAcrossRollback() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsOpenCursorsAcrossRollback();
		return answer;
	}

	public boolean supportsOpenStatementsAcrossCommit() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsOpenStatementsAcrossCommit();
		return answer;
	}

	public boolean supportsOpenStatementsAcrossRollback() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsOpenStatementsAcrossRollback();
		return answer;
	}

	public int getMaxBinaryLiteralLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxBinaryLiteralLength();
		return answer;
	}

	public int getMaxCharLiteralLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxCharLiteralLength();
		return answer;
	}

	public int getMaxColumnNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxColumnNameLength();
		return answer;
	}

	public int getMaxColumnsInGroupBy() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxColumnsInGroupBy();
		return answer;
	}

	public int getMaxColumnsInIndex() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxColumnsInIndex();
		return answer;
	}

	public int getMaxColumnsInOrderBy() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxColumnsInOrderBy();
		return answer;
	}

	public int getMaxColumnsInSelect() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxColumnsInSelect();
		return answer;
	}

	public int getMaxColumnsInTable() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxColumnsInTable();
		return answer;
	}

	public int getMaxConnections() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxConnections();
		return answer;
	}

	public int getMaxCursorNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxCursorNameLength();
		return answer;
	}

	public int getMaxIndexLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxIndexLength();
		return answer;
	}

	public int getMaxSchemaNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxSchemaNameLength();
		return answer;
	}

	public int getMaxProcedureNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxProcedureNameLength();
		return answer;
	}

	public int getMaxCatalogNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxCatalogNameLength();
		return answer;
	}

	public int getMaxRowSize() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxRowSize();
		return answer;
	}

	public boolean doesMaxRowSizeIncludeBlobs() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().doesMaxRowSizeIncludeBlobs();
		return answer;
	}

	public int getMaxStatementLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxStatementLength();
		return answer;
	}

	public int getMaxStatements() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxStatements();
		return answer;
	}

	public int getMaxTableNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxTableNameLength();
		return answer;
	}

	public int getMaxTablesInSelect() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxTablesInSelect();
		return answer;
	}

	public int getMaxUserNameLength() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getMaxUserNameLength();
		return answer;
	}

	public int getDefaultTransactionIsolation() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getDefaultTransactionIsolation();
		return answer;
	}

	public boolean supportsTransactions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsTransactions();
		return answer;
	}

	public boolean supportsTransactionIsolationLevel(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsTransactionIsolationLevel(parameter0);
		return answer;
	}

	public boolean supportsDataDefinitionAndDataManipulationTransactions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsDataDefinitionAndDataManipulationTransactions();
		return answer;
	}

	public boolean supportsDataManipulationTransactionsOnly() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsDataManipulationTransactionsOnly();
		return answer;
	}

	public boolean dataDefinitionCausesTransactionCommit() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().dataDefinitionCausesTransactionCommit();
		return answer;
	}

	public boolean dataDefinitionIgnoredInTransactions() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().dataDefinitionIgnoredInTransactions();
		return answer;
	}

	public java.sql.ResultSet getProcedures(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getProcedures(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getProcedureColumns(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, java.lang.String parameter3) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getProcedureColumns(parameter0, parameter1, parameter2, parameter3);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getTables(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, java.lang.String[] parameter3) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getTables(parameter0, parameter1, parameter2, parameter3);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getSchemas() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getSchemas();
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getCatalogs() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getCatalogs();
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getTableTypes() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getTableTypes();
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getColumns(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, java.lang.String parameter3) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getColumns(parameter0, parameter1, parameter2, parameter3);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getColumnPrivileges(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, java.lang.String parameter3) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getColumnPrivileges(parameter0, parameter1, parameter2, parameter3);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getTablePrivileges(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getTablePrivileges(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getBestRowIdentifier(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, int parameter3, boolean parameter4) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getBestRowIdentifier(parameter0, parameter1, parameter2, parameter3, parameter4);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getVersionColumns(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getVersionColumns(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getPrimaryKeys(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getPrimaryKeys(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getImportedKeys(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getImportedKeys(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getExportedKeys(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getExportedKeys(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getCrossReference(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, java.lang.String parameter3, java.lang.String parameter4, java.lang.String parameter5) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getCrossReference(parameter0, parameter1, parameter2, parameter3, parameter4, parameter5);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getTypeInfo() throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getTypeInfo();
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getIndexInfo(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, boolean parameter3, boolean parameter4) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getIndexInfo(parameter0, parameter1, parameter2, parameter3, parameter4);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public boolean supportsResultSetType(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsResultSetType(parameter0);
		return answer;
	}

	public boolean supportsResultSetConcurrency(int parameter0, int parameter1) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsResultSetConcurrency(parameter0, parameter1);
		return answer;
	}

	public boolean ownUpdatesAreVisible(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().ownUpdatesAreVisible(parameter0);
		return answer;
	}

	public boolean ownDeletesAreVisible(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().ownDeletesAreVisible(parameter0);
		return answer;
	}

	public boolean ownInsertsAreVisible(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().ownInsertsAreVisible(parameter0);
		return answer;
	}

	public boolean othersUpdatesAreVisible(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().othersUpdatesAreVisible(parameter0);
		return answer;
	}

	public boolean othersDeletesAreVisible(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().othersDeletesAreVisible(parameter0);
		return answer;
	}

	public boolean othersInsertsAreVisible(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().othersInsertsAreVisible(parameter0);
		return answer;
	}

	public boolean updatesAreDetected(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().updatesAreDetected(parameter0);
		return answer;
	}

	public boolean deletesAreDetected(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().deletesAreDetected(parameter0);
		return answer;
	}

	public boolean insertsAreDetected(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().insertsAreDetected(parameter0);
		return answer;
	}

	public boolean supportsBatchUpdates() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsBatchUpdates();
		return answer;
	}

	public java.sql.ResultSet getUDTs(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2, int[] parameter3) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getUDTs(parameter0, parameter1, parameter2, parameter3);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.Connection getConnection() throws java.sql.SQLException {
		checkActive();
        return (java.sql.Connection) getTopResource();
	}

	public boolean supportsSavepoints() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsSavepoints();
		return answer;
	}

	public boolean supportsNamedParameters() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsNamedParameters();
		return answer;
	}

	public boolean supportsMultipleOpenResults() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsMultipleOpenResults();
		return answer;
	}

	public boolean supportsGetGeneratedKeys() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsGetGeneratedKeys();
		return answer;
	}

	public java.sql.ResultSet getSuperTypes(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getSuperTypes(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public java.sql.ResultSet getSuperTables(java.lang.String parameter0, java.lang.String parameter1, java.lang.String parameter2) throws java.sql.SQLException {
		checkActive();
		java.sql.ResultSet answer = myRawResourceComponent().getSuperTables(parameter0, parameter1, parameter2);
        answer = new ResultSetComponent(this, answer);
		return answer;
	}

	public boolean supportsResultSetHoldability(int parameter0) throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsResultSetHoldability(parameter0);
		return answer;
	}

	public int getResultSetHoldability() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getResultSetHoldability();
		return answer;
	}

	public int getDatabaseMajorVersion() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getDatabaseMajorVersion();
		return answer;
	}

	public int getDatabaseMinorVersion() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getDatabaseMinorVersion();
		return answer;
	}

	public int getJDBCMajorVersion() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getJDBCMajorVersion();
		return answer;
	}

	public int getJDBCMinorVersion() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getJDBCMinorVersion();
		return answer;
	}

	public int getSQLStateType() throws java.sql.SQLException {
		checkActive();
		int answer = myRawResourceComponent().getSQLStateType();
		return answer;
	}

	public boolean locatorsUpdateCopy() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().locatorsUpdateCopy();
		return answer;
	}

	public boolean supportsStatementPooling() throws java.sql.SQLException {
		checkActive();
		boolean answer = myRawResourceComponent().supportsStatementPooling();
		return answer;
	}

}
