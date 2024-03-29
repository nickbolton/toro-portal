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
package org.blojsom.plugin.statistics;

import org.blojsom.plugin.BlojsomPlugin;
import org.blojsom.plugin.BlojsomPluginException;
import org.blojsom.blog.BlojsomConfiguration;
import org.blojsom.blog.BlogEntry;
import org.blojsom.blog.BlogUser;
import org.blojsom.util.BlojsomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Word Count plugin.
 *
 * @author David Czarnecki
 * @since blojsom 2.15
 * @version $Id$
 */
public class WordCountPlugin implements BlojsomPlugin {

    private Log _logger = LogFactory.getLog(WordCountPlugin.class);

    public static final String WORD_COUNT_PLUGIN_HELPER = "WORD_COUNT_PLUGIN_HELPER";
    public static final String BLOJSOM_PLUGIN_WORD_COUNT_METADATA = "blojsom-plugin-word-count";

    /**
     * Default constructor.
     */
    public WordCountPlugin() {
    }

    /**
     * Initialize this plugin. This method only called when the plugin is instantiated.
     *
     * @param servletConfig        Servlet config object for the plugin to retrieve any initialization parameters
     * @param blojsomConfiguration {@link org.blojsom.blog.BlojsomConfiguration} information
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error initializing the plugin
     */
    public void init(ServletConfig servletConfig, BlojsomConfiguration blojsomConfiguration) throws BlojsomPluginException {
        _logger.debug("Initialized Word Count plugin");
    }

    /**
     * Process the blog entries
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param user                {@link org.blojsom.blog.BlogUser} instance
     * @param context             Context
     * @param entries             Blog entries retrieved for the particular request
     * @return Modified set of blog entries
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error processing the blog entries
     */
    public BlogEntry[] process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlogUser user, Map context, BlogEntry[] entries) throws BlojsomPluginException {
        for (int i = 0; i < entries.length; i++) {
            BlogEntry entry = entries[i];
            Map entryMetaData = entry.getMetaData();

            if (entry.getDescription() == null) {
                entryMetaData.put(BLOJSOM_PLUGIN_WORD_COUNT_METADATA, new Integer(0));
            } else {
                String entryWithoutTags = entry.getDescription();
                entryWithoutTags = entryWithoutTags.replaceAll("\\<.*?\\>", "");
                StringTokenizer tokenizer = new StringTokenizer(entryWithoutTags);
                entryMetaData.put(BLOJSOM_PLUGIN_WORD_COUNT_METADATA, new Integer(tokenizer.countTokens()));
            }

            entry.setMetaData(entryMetaData);
        }

        context.put(WORD_COUNT_PLUGIN_HELPER, new WordCountHelper());

        return entries;
    }

    /**
     * Perform any cleanup for the plugin. Called after {@link #process}.
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error performing cleanup for this plugin
     */
    public void cleanup() throws BlojsomPluginException {
    }

    /**
     * Called when BlojsomServlet is taken out of service
     *
     * @throws org.blojsom.plugin.BlojsomPluginException
     *          If there is an error in finalizing this plugin
     */
    public void destroy() throws BlojsomPluginException {
    }

    /**
     * Class to handle word count for text in templates
     */
    public class WordCountHelper {

        /**
         * Count the number of words in a piece of text
         *
         * @param text Text
         * @return # of words in text, 0 if text is <code>null</code> or blank
         */
        public Integer countWords(String text) {
            if (BlojsomUtils.checkNullOrBlank(text)) {
                return new Integer(0);
            } else {
                String textWithoutTokens = text.replaceAll("\\<.*?\\>", "");
                StringTokenizer tokenizer = new StringTokenizer(textWithoutTokens);

                return new Integer(tokenizer.countTokens());
            }
        }
    }
}