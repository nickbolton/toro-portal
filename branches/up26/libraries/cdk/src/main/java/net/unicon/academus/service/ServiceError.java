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

public final class ServiceError {

    // Lexicon.
    private static final IErrorLexicon lex;
    static {
        String title = "Academus Service Layer Error Codes";
        StringBuffer summary = new  StringBuffer();
        summary.append("The following error codes describe problems that may ")
                            .append("be encountered when introducing foreign ")
                            .append("entities into an Academus deployment ")
                            .append("through the Academus Service Layer.");
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

    public static final ErrorCode USER_NOT_FOUND = new ErrorCode(
                    "01-01",
                    "Academus was unable to locate the specified user.",
                    lex
                );

    public static final ErrorCode USER_NO_SYSTEM_ROLE = new ErrorCode(
                    "01-02",
                    "No system role was provided.  You must specify a system "
                                        + "role to import or update a user.",
                    lex
                );

    public static final ErrorCode USER_INVALID_SYSTEM_ROLE = new ErrorCode(
                    "01-03",
                    "The specified system role is not a valid default system "
                                                + "role within Academus.",
                    lex
                );

    public static final ErrorCode USER_NO_TEMPLATE_USER = new ErrorCode(
                    "01-04",
                    "There is no template user defined for the specified "
                                                        + "system role",
                    lex
                );

    // Group Errors -- 02 series.

    public static final ErrorCode GROUP_NOT_FOUND = new ErrorCode(
                    "02-01",
                    "Academus was unable to locate the specified group.",
                    lex
                );

    public static final ErrorCode GROUP_ALREADY_EXISTS = new ErrorCode(
                    "02-02",
                    "Academus was unable to import the specified group "
                                        + "because it already exists.",
                    lex
                );

    // Topic Errors -- 03 series.

    public static final ErrorCode TOPIC_NOT_FOUND = new ErrorCode(
                    "03-01",
                    "Academus was unable to locate the specified topic.",
                    lex
                );

    public static final ErrorCode TOPIC_EXTERNAL_ROOT_NOT_FOUND = new ErrorCode(
                    "03-02",
                    "Academus was unable to obtain the external root group for "
                                            + "the relevent system of record.",
                    lex
                );

    public static final ErrorCode TOPIC_PARENT_NOT_FOUND = new ErrorCode(
                    "03-03",
                    "The topic could not be imported/updated because Academus "
                                + "was unable to locate the specified parent "
                                + "group.",
                    lex
                );

    // Offering Errors -- 04 series.

    public static final ErrorCode OFFERING_PARENT_NOT_FOUND = new ErrorCode(
                    "04-01",
                    "The offering could not be imported/updated because Academus "
                                + "was unable to locate one or more specified parent "
                                + "topics.",
                    lex
                );

    public static final ErrorCode OFFERING_ENROLLMENT_MODEL_NOT_FOUND = new ErrorCode(
                    "04-02",
                    "Academus was unable to import or update the offering "
                                        + "becuase the specified enrollment "
                                        + "model does not exist.",
                    lex
                );

    public static final ErrorCode OFFERING_DEFAULT_ROLE_NOT_FOUND = new ErrorCode(
                    "04-03",
                    "Academus was unable to import or update the offering "
                                        + "becuase the specified default "
                                        + "role could not be located.",
                    lex
                );

    public static final ErrorCode OFFERING_CREATOR_NOT_FOUND = new ErrorCode(
                    "04-04",
                    "Academus was unable to import or update the offering "
                                        + "becuase the user account to be used "
                                        + "as a creator could not be located.  "
                                        + "The creator user is specified in an "
                                        + "Academus configuration file per "
                                        + "deployment.",
                    lex
                );

    public static final ErrorCode OFFERING_OPTIONAL_ATTRIBUTES_NOT_FOUND = new ErrorCode(
                    "04-05",
                    "Academus was unable to import or update the offering "
                                        + "becuase the there was a problem setting "
                                        + "the offering optional attributes.",
                    lex
                );
    // Membership Errors -- 05 series.

    public static final ErrorCode MEMBERSHIP_USER_NOT_FOUND = new ErrorCode(
                    "05-01",
                    "The membership could not be imported/updated because Academus "
                                + "was unable to locate the specified user.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_GROUP_NOT_FOUND = new ErrorCode(
                    "05-02",
                    "The membership could not be imported/updated because Academus "
                                + "was unable to locate the specified group.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_OFFERING_NOT_FOUND = new ErrorCode(
                    "05-03",
                    "The membership could not be imported/updated because Academus "
                                + "was unable to locate the specified offering.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_ROLE_NOT_FOUND = new ErrorCode(
                    "05-04",
                    "The membership could not be imported/updated because Academus "
                                + "was unable to locate the specified offering role.",
                    lex
                );

    public static final ErrorCode MEMBERSHIP_ENROLLMENT_STATUS_NOT_FOUND = new ErrorCode(
                    "05-05",
                    "The membership could not be imported/updated because Academus "
                            + "was unable to locate the specified enrollment status.",
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
