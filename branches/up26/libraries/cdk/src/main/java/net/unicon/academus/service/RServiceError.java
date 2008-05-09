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

package net.unicon.academus.service;

import net.unicon.sdk.error.ErrorCode;
import net.unicon.sdk.error.IErrorLexicon;

public final class RServiceError {

    // Lexicon.
    private static final IErrorLexicon lex;
    static {
        String title = "Academus Remote Import Service Error Codes";
        StringBuffer summary = new  StringBuffer();
        summary.append("The following error codes describe problems that may ")
                            .append("be encountered when invoking remote ")
                            .append("operations through the Academus  ")
                            .append("import service layer.");
        lex = ErrorCode.CreateLexicon(title, summary.toString());
    }

    /*
     * General Errors -- 00 series.
     */

    public static final ErrorCode UNRECOGNIZED_ERROR = new ErrorCode(
                    "00-01",
                    "The server encountered an unanticipated error.  Please "
                                            + "refer to the stack trace "
                                            + "included with this message.",
                    lex
                );

    public static final ErrorCode MISSING_INPUT = new ErrorCode(
                    "00-02",
                    "The requested transaction is missing required data.",
                    lex
                );

    public static final ErrorCode SOR_NOT_FOUND = new ErrorCode(
                    "00-03",
                    "The specified System of Record (foreign entity source) "
                                + "could not be located within Academus.",
                    lex
                );

    /*
     * Type-Specific Errors.
     */

    // User Errors -- 01 series.

    public static final ErrorCode USER_IMPORT_FAILURE = new ErrorCode(
                    "01-01",
                    "Academus was unable to complete the import user operation.",
                    lex
                );

    public static final ErrorCode USER_BATCH_IMPORT_FAILURE = new ErrorCode(
                    "01-02",
                    "Academus was unable to complete the batch import user operation.",
                    lex
                );

    public static final ErrorCode USER_DELETE_FAILURE = new ErrorCode(
                    "01-03",
                    "Academus was unable to complete the delete user operation.",
                    lex
                );

    public static final ErrorCode USER_BATCH_DELETE_FAILURE = new ErrorCode(
                    "01-04",
                    "Academus was unable to complete the batch delete user operation.",
                    lex
                );

    public static final ErrorCode USER_UPDATE_FAILURE = new ErrorCode(
                    "01-05",
                    "Academus was unable to complete the update user operation.",
                    lex
                );

    public static final ErrorCode USER_BATCH_UPDATE_FAILURE = new ErrorCode(
                    "01-06",
                    "Academus was unable to complete the batch update user operation.",
                    lex
                );

    public static final ErrorCode USER_SYNC_FAILURE = new ErrorCode(
                    "01-07",
                    "Academus was unable to complete the sync user operation.",
                    lex
                );

    // Group Errors -- 02 series.

    public static final ErrorCode GROUP_IMPORT_FAILURE = new ErrorCode(
                    "02-01",
                    "Academus was unable to complete the import group operation.",
                    lex
                );

    public static final ErrorCode GROUP_BATCH_IMPORT_FAILURE = new ErrorCode(
                    "02-02",
                    "Academus was unable to complete the batch import group operation.",
                    lex
                );

    public static final ErrorCode GROUP_DELETE_FAILURE = new ErrorCode(
                    "02-03",
                    "Academus was unable to complete the delete group operation.",
                    lex
                );

    public static final ErrorCode GROUP_BATCH_DELETE_FAILURE = new ErrorCode(
                    "02-04",
                    "Academus was unable to complete the batch delete group operation.",
                    lex
                );

    public static final ErrorCode GROUP_UPDATE_FAILURE = new ErrorCode(
                    "02-05",
                    "Academus was unable to complete the update group operation.",
                    lex
                );

    public static final ErrorCode GROUP_BATCH_UPDATE_FAILURE = new ErrorCode(
                    "02-06",
                    "Academus was unable to complete the batch update group operation.",
                    lex
                );

    public static final ErrorCode GROUP_SYNC_FAILURE = new ErrorCode(
                    "02-07",
                    "Academus was unable to complete the sync group operation.",
                    lex
                );

    // Topic Errors -- 03 series.

    public static final ErrorCode TOPIC_IMPORT_FAILURE = new ErrorCode(
                    "03-01",
                    "Academus was unable to complete the import topic operation.",
                    lex
                );

    public static final ErrorCode TOPIC_DELETE_FAILURE = new ErrorCode(
                    "03-02",
                    "Academus was unable to complete the delete topic operation.",
                    lex
                );

    public static final ErrorCode TOPIC_UPDATE_FAILURE = new ErrorCode(
                    "03-03",
                    "Academus was unable to complete the update topic operation.",
                    lex
                );

    public static final ErrorCode TOPIC_SYNC_FAILURE = new ErrorCode(
                    "03-04",
                    "Academus was unable to complete the sync topic operation.",
                    lex
                );

    // Offering Errors -- 04 series.

    public static final ErrorCode OFFERING_IMPORT_FAILURE = new ErrorCode(
                    "04-01",
                    "Academus was unable to complete the import offering operation.",
                    lex
                );

    public static final ErrorCode OFFERING_BATCH_IMPORT_FAILURE = new ErrorCode(
                    "04-02",
                    "Academus was unable to complete the batch import offering operation.",
                    lex
                );

    public static final ErrorCode OFFERING_INACTIVATE_FAILURE = new ErrorCode(
                    "04-03",
                    "Academus was unable to complete the inactivate offering operation.",
                    lex
                );

    public static final ErrorCode OFFERING_BATCH_INACTIVATE_FAILURE = new ErrorCode(
                    "04-04",
                    "Academus was unable to complete the batch inactivate offering operation.",
                    lex
                );

    public static final ErrorCode OFFERING_UPDATE_FAILURE = new ErrorCode(
                    "04-05",
                    "Academus was unable to complete the update offering operation.",
                    lex
                );

    public static final ErrorCode OFFERING_BATCH_UPDATE_FAILURE = new ErrorCode(
                    "04-06",
                    "Academus was unable to complete the batch update offering operation.",
                    lex
                );

    public static final ErrorCode OFFERING_SYNC_FAILURE = new ErrorCode(
                    "04-07",
                    "Academus was unable to complete the sync offering operation.",
                    lex
                );

    // Membership Errors -- 05 series.

    public static final ErrorCode MEMBERSHIP_IMPORT_FAILURE = new ErrorCode(
                    "05-01",
                    "Academus was unable to complete the import membership operation.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_UPDATE_FAILURE = new ErrorCode(
                    "05-02",
                    "Academus was unable to complete the update membership operation.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_DELETE_FAILURE = new ErrorCode(
                    "05-03",
                    "Academus was unable to complete the delete membership operation.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_SYNC_FAILURE = new ErrorCode(
                    "05-04",
                    "Academus was unable to complete the sync membership operation.",
                    lex
                );

    // Enrollment Model -- 06 series

    public static final ErrorCode DEFAULT_ROLE_NOT_FOUND = new ErrorCode(
                    "06-01",
                    "The offering could not be imported/updated because Academus "
                                + "was unable to locate the specified default role.",
                    lex
                );
}
