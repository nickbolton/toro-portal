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

package net.unicon.sdk.error;

import java.io.Serializable;

/**
 * Represents a vocabulary of error codes designed for use within a specific
 * context.  The set of <code>ErrorCode</code> instances associated with a
 * lexicon should be comprehensive for that context.
 */
public interface IErrorLexicon extends Serializable {

    /**
     * Gets the title of the lexicon.
     *
     * @return The title of the lexicon.
     */
    public String getTitle();

    /**
     * Gets the description of the lexicon.
     *
     * @return The description of the lexicon.
     */
    public String getSummary();

    /**
     * Creates an XML representation of the <code>ErrorLexicon</code> object and
     * returns it.  The <code>toXml</code> method generates XML in the following
     * format:
     * <blockquote><code>
     * &lt;ELEMENT lexicon (title, summary, members)&gt;<br>
     *
     * &lt;ELEMENT title (#PCDATA)&gt;<br>
     *
     * &lt;ELEMENT summary (#PCDATA)&gt;<br>
     *
     * &lt;ELEMENT members (error-code*)&gt;<br>
     *
     * &lt;ELEMENT error-code (#PCDATA)&gt;<br>
     * &lt;ATTLIST error-code   id      CDATA   #REQUIRED&gt;<br>
     * </code></blockquote>
     *
     * @return An XML representation of the lexicon.
     */
    public String toXml();

}
