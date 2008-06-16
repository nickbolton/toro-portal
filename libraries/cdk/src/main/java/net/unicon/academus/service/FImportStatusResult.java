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

import java.io.Serializable;

import net.unicon.sdk.error.ErrorCode;

/**
 * @author Kevin Gary
 *
 */
public final class FImportStatusResult implements Serializable {

    // some well-defined enumerable codes and status results
    public static final int SUCCESSFUL_WARNINGS = 2;
    public static final int SUCCESSFUL = 1;

    public static final int FAILED     = -1;
    public static final int FAILED_EXCEPTION = -2;
    public static final int FAILED_WARNINGS  = -3;

    public static final FImportStatusResult DEFAULT_SUCCESS =
        new FImportStatusResult(SUCCESSFUL, "Import Successful");
    public static final FImportStatusResult DEFAULT_FAILURE =
        new FImportStatusResult(FAILED, "Import Failed");
    public static final FImportStatusResult FAILED_PRECONDITION =
        new FImportStatusResult(FAILED_WARNINGS, "Precondition for import failed");
    public static final FImportStatusResult FAILED_BAD_DATA =
        new FImportStatusResult(FAILED, "Bad input data for import");
    public static final FImportStatusResult DEFAULT_EXCEPTION =
        new FImportStatusResult(FAILED_EXCEPTION, "Import Failed due to an Exception");
    public static final FImportStatusResult DEFAULT_WARNING =
        new FImportStatusResult(SUCCESSFUL_WARNINGS, "Import Successful with Warnings");

    /**
     * Represents some message provided back to the caller
     */
    private final String __message;

    /**
     * A well-defined code, one of the defined constants for this class, that describes the
     * result of this operation.
     */
    private final int __statusCode;

    /**
     * A hook to allow the import method or its extensions to return anything
    */
    private final Serializable __obj;

    private final ErrorCode __errorCode;

    /**
     * Constructor that initializes the result
     * @param code
     * @param msg
     */
    public FImportStatusResult(int code, String msg) {
        __message = msg;
        __statusCode = code;
        __obj = null;
        __errorCode = null;
    }

    /**
     * Constructor that initializes the result and includes a serializable object
     * @param code
     * @param msg
     */
    public FImportStatusResult(int code, String msg, Serializable obj) {
        __message = msg;
        __statusCode = code;
        __obj = obj;
        __errorCode = null;
     }

    /**
     *
     */
    public FImportStatusResult(int code, String msg, ErrorCode c,
                                            Serializable obj) {
        __statusCode = code;
        __message = msg;
        __errorCode = c;
        __obj = obj;
    }

    /**
     * Convenience copy constructor that adds a serializable object. Primary use is
     * when someone uses one of the enumerable ones above and wants to add extra
     * information such as the exception.
     * @param importResult an already existing result object
     * @param obj serializable object
     */
    public FImportStatusResult(FImportStatusResult statusResult, Serializable obj) {
        __message = statusResult.getMessage();
        __statusCode = statusResult.getStatusCode();
        __obj = obj;
        __errorCode = null;
    }

    public String getMessage() {
        return __message;
    }

    public int getStatusCode() {
        return __statusCode;
    }

    public Object getObject() {
        return __obj;
    }

    public ErrorCode getErrorCode() {
        return __errorCode;
    }

    /**
     * Asks whether the import operation was successful.
     */
    public final boolean wasImportSuccessful() {
        return __statusCode > 0;
    }

}
