/**
 * Copyright (c) 2003-2005, David A. Czarnecki
 * All rights reserved.
 *
 * Portions Copyright (c) 2003-2005 by Mark Lussier
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the "David A. Czarnecki" and "blojsom" nor the names of
 * its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Products derived from this software may not be called "blojsom",
 * nor may "blojsom" appear in their name, without prior written permission of
 * David A. Czarnecki.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.blojsom.plugin.admin.event;

import org.blojsom.event.BlojsomEvent;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;

import java.util.Date;

/**
 * BlogEntryEvent
 *
 * @author David Czarnecki
 * @version $Id$
 * @since blojsom 2.22
 */
public class BlogEntryEvent extends BlojsomEvent {

    protected BlogEntry _blogEntry;
    protected BlogUser _blogUser;

    /**
     * Create a new event indicating something happened with an entry in the system.
     *
     * @param source Source of the event
     * @param timestamp Event timestamp
     * @param blogEntry {@link BlogEntry}
     * @param blogUser {@link BlogUser}
     */
    public BlogEntryEvent(Object source, Date timestamp, BlogEntry blogEntry, BlogUser blogUser) {
        super(source, timestamp);
        _blogEntry = blogEntry;
        _blogUser = blogUser;
    }

    /**
     * Retrieve the {@link BlogEntry} associated with the event
     *
     * @return {@link BlogEntry}
     */
    public BlogEntry getBlogEntry() {
        return _blogEntry;
    }

    /**
     * Retrieve the {@link BlogUser} associated with the event
     *
     * @return {@link BlogUser}
     */
    public BlogUser getBlogUser() {
        return _blogUser;
    }
}