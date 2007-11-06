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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides information about a system error.  <code>ErrorCode</code> objects
 * help distributed, independent systems communicate with each other when a
 * problem arises.
 */
public final class ErrorCode implements Serializable {

    // Static Members.
    private static List lexica = new ArrayList();

    // Instance Members.
    private String id;
    private String description;

    /*
     * Public API.
     */

    /**
     * Creates a new lexicon with the specified <code>title</code> and
     * <code>summary</code>.
     *
     * @param title The title for the new lexicon.
     * @param summary The description for the new lexicon.
     * @returns A new lexicon with the specified title and description..
     */
    public static IErrorLexicon CreateLexicon(String title, String summary) {

        // Assertions.
        if (title == null) {
            String msg = "Argument 'title' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (summary == null) {
            String msg = "Argument 'summary' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        IErrorLexicon rslt = new ErrorLexiconImpl(title, summary);
        lexica.add(rslt);
        return rslt;

    }

    /**
     * Creates a new error code with the specified <code>id</code>
     * and <code>description</code> and associates it with the specified
     * <code>IErrorLexicon</code>.
     *
     * @param id Succinct identifier for the error that is unique in context.
     * @param description General explanation of the circumstances that give
     * rise to the error.
     * @param lexicon The lexicon with which the error code is associated.
     */
    public ErrorCode(String id, String description, IErrorLexicon lexicon) {

        // Assertions.
        if (id == null) {
            String msg = "Argument 'id' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (description == null) {
            String msg = "Argument 'description' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (lexicon == null) {
            String msg = "Argument 'lexicon' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        if (!(lexicon instanceof ErrorLexiconImpl)) {
            String msg = "The specified lexicon is an unsupported "
                            + "implementation.  Use ErrorCode.CreateLexicon "
                            + "to obtain a lexicon objebct.";
            throw new IllegalArgumentException(msg);
        }

        // Instance Members.
        this.id = id;
        this.description = description;

        // Add to collection.
        ErrorLexiconImpl eli = (ErrorLexiconImpl) lexicon;
        eli.addMember(this);

    }

    /**
     * Gets the identifier for the error.  Error ids should be succinct and
     * unique in context.
     *
     * @return The error id.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the general explanation for the error.  Error descriptions contain
     * only information that applies in every case -- never information specific
     * to the present circumstances.
     *
     * @return An explanation of the error.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates an XML representation of the <code>ErrorCode</code> object and
     * returns it.  The <code>toXml</code> method generates XML in the following
     * format:
     * <blockquote><code>
     * &lt;ELEMENT error-code (#PCDATA)&gt;<br>
     * &lt;ATTLIST error-code   id      CDATA   #REQUIRED&gt;<br>
     * </code></blockquote>
     *
     * @return An XML representation of the error.
     */
    public String toXml() {

        StringBuffer rslt = new StringBuffer();
        rslt.append("<error-code id=\"").append(id).append("\">")
                    .append(description).append("</error-code>");
        return rslt.toString();

    }

    /**
     * Indicates whether some other object is "equal to" this one.  This
     * implementation of <code>equals</code> specifies that two
     * <code>ErrorCode</code> objects that have the same <code>id</code> are
     * equal.
     *
     * @param o The reference object with which to compare.
     * @return <code>true</code> if this object is the same as the 'o' argument;
     * otherwise, <code>false</code>.
     */
    public boolean equals(Object o) {

        // Per equals contract -- false if the reference object is null.
        if (o == null) {
            return false;
        }

        // False if the reference object is not an ErrorCode instance.
        if (!(o instanceof ErrorCode)) {
            return false;
        }

        // Convert to ErrorCode and compare id's.
        ErrorCode c = (ErrorCode) o;
        return id.equals(c.getId());

    }

    /**
     * Returns a hash code value for the object.  This method is overridden to
     * maintain parity with the override of <code>equals<code>.
     *
     * @return A hash code value for this object.
     */
    public int hashCode() {
        return id.hashCode();
    }

    /*
     * Nested Types.
     */

    private static final class ErrorLexiconImpl implements IErrorLexicon {

        // Instance Members.
        private String title;
        private String summary;
        private List members;

        /*
         * Public API.
         */

        public ErrorLexiconImpl(String title, String summary) {

            // Assertions.
            if (title == null) {
                String msg = "Argument 'title' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (summary == null) {
                String msg = "Argument 'summary' cannot be null.";
                throw new IllegalArgumentException(msg);
            }

            // Instance Members.
            this.title = title;
            this.summary = summary;
            this.members = new ArrayList();

        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String toXml() {

            // Begin.
            StringBuffer rslt = new StringBuffer();
            rslt.append("<lexicon>");

            // Title.
            rslt.append("<title>");
            rslt.append(title);
            rslt.append("</title>");

            // Summary.
            rslt.append("<summary>");
            rslt.append(summary);
            rslt.append("</summary>");

            // Members.
            rslt.append("<members>");
            synchronized (ErrorLexiconImpl.class) {
                Iterator it = members.iterator();
                while (it.hasNext()) {
                    ErrorCode c = (ErrorCode) it.next();
                    rslt.append(c.toXml());
                }
            }
            rslt.append("</members>");

            // End.
            rslt.append("</lexicon>");
            return rslt.toString();

        }

        public void addMember(ErrorCode c) {

            // Assertions.
            if (c == null) {
                String msg = "Argument 'c [ErrorCode]' cannot be null.";
                throw new IllegalArgumentException(msg);
            }
            if (members.contains(c)) {
                String msg = "The lexicon already contains the specified "
                                                            + "member.";
                throw new IllegalArgumentException(msg);
            }

            synchronized (ErrorLexiconImpl.class) {
                members.add(c);
            }

        }

    }

}
