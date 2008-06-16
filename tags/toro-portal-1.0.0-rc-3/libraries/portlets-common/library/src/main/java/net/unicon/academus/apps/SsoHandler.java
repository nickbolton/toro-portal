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

package net.unicon.academus.apps;

import net.unicon.academus.apps.util.IAttributeEvaluator;
import org.dom4j.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Single Sign-On Handler base class.
 */
public abstract class SsoHandler {

    /**
     * Parse an InputStream into an array of SsoEntries.
     */
    public abstract SsoEntry[] parse(InputStream in) throws IOException;

    /**
     * Internal evaluation for the implementing serializer.
     * All parameters passed directly through this method are to override the
     * values found in the original SsoTarget. These will already be processed
     * as necessary.
     *
     * The processing steps that will have occurred include:
     * <ul>
     *  <li>SsoAuthentication, if any, will have been evaluated. This will set
     *  the 'needsAuth' flag as appropriate.</li>
     *  <li>Parameter tokens will have been expanded to their values.</li>
     *  <li>The url with tokens expanded (to allow for HTTP-Basic support)</li>
     * </ul>
     *
     * @param url Overridden url variable, with any tokens expanded.
     * @param resolvedParams SsoTarget parameters that have had their tokens
     * expanded.
     * @param needsAuth true if the SsoAuthentication module requires an
     * @param exposeParameters true if credentials need to be exposed
     * authentication mapping to be created.
     */
    protected abstract String evaluate(SsoEntry entry, Map url,
        Map resolvedParams, boolean needsAuth, String sequenceId,
        boolean exposeParameters);

    /**
     * Create or update an authentication mapping for a given SsoTarget and set
     * of user attributes.
     *
     * @see SsoAuthentication#createAuthentication(Map,String,String)
     */
    public void authenticate(SsoEntry entry, Map userAttribs,
        String username, String password) {
        entry.getAuthentication().createAuthentication(userAttribs, username, password);
    }


    /**
     * Evaluate a set of SsoTargets and generate a map of their handles to
     * their serialized versions.
     */
    public Map evaluate(SsoEntry[] entries, Map userAttribs,
        String[] sequenceIds, boolean exposeCredentials) {
        Map rslt = new HashMap();

        for (int i = 0; i < entries.length; i++) {
            rslt.put(entries[i].getHandle()
                    , evaluate(entries[i], userAttribs, sequenceIds[i], exposeCredentials));
        }

        return rslt;
    }

    /**
     * Perform the evaluation and serialization of an SsoTarget for a given set
     * of user attributes.
     */
    public String evaluate(SsoEntry entry, Map userAttribs,
        String sequenceId, boolean exposeCredentials) {
        boolean needsauth = false;
        Map attribs = userAttribs;

        // Perform authentication if necessary
        if (entry.getAuthentication() != null) {
            try {
                attribs = entry.getAuthentication().resolve(userAttribs);
            } catch (NeedsAuthException nae) {
                needsauth = true;
            }
        }

        // Expand tokens in target parameters
        SsoTarget[] targets = entry.getTargets();
        Map params = new HashMap();
        Map urls = new HashMap();
        
        Map tempParams;
        Map tempEvals;
        String value;
        
        for(int i = 0; i < targets.length; i++){
            tempParams = targets[i].getParameters();
            tempEvals = targets[i].getEvaluators();
	        Iterator it = tempParams.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry e = (Map.Entry)it.next();                
                value=expandTokens((String)e.getValue(), attribs);
                //check to see if the tokenized parameter needs to be further evaluated for more processing
                if(tempEvals.containsKey(e.getKey())){
                    IAttributeEvaluator evalImpl;
                    try {
                        //instantiate evaluator
                        evalImpl = (IAttributeEvaluator) Class.forName((String) tempEvals.get(e.getKey())).newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException("Problem instantiating IAttributeEvaluator for attribute " +e.getKey(), ex);
                    } 
                    //evalute and return new parameter value
                    value=evalImpl.evaluate(value);
                }
	            e.setValue(value);
	        }
	
	        params.put(targets[i].getHandle(), tempParams);
	        // expand URL tokens, primarily for HTTP-Basic support, but possibly
	        // allowing other unique possibilities.
	        urls.put(targets[i].getHandle(), expandTokens(targets[i].getURL(), attribs));
        }
        return evaluate(entry, urls, params, needsauth, sequenceId,
            exposeCredentials);
    }

    /**
     * Expand tokens to their values within the attributes map.
     *
     * <p>This takes a string that may or may not contain tokenized variables
     * in the form of '{mykey}', and replaces them with the values of the
     * specified key from the input map.</p>
     *
     * <p>For example, if the input '{username}' was given, the result would be
     * the value of the call attribs.get("username").</p>
     *
     * <p>If a key does not map to a value, the string 'null' will take its
     * place.</p>
     *
     * @param value The tokenized string to expand
     * @param attribs The input map containing the values to using in expansion
     * @return The input string with all tokens expanded
     */
    protected String expandTokens(String value, Map attribs) {
        Matcher m = tokenMatch.matcher(value);
        StringBuffer rslt = new StringBuffer();
        int last = 0;

        while (m.find()) {
            // Copy any skipped text
            rslt.append(value.substring(last, m.start()));
            last = m.end();

            // Expand the token
            String key = m.group(1);
            String val = (String)attribs.get(key);
//System.out.println("Untokenizing key: "+key+" to value: "+val);
            rslt.append(val);
        }
        if (last == 0)
            rslt.append(value);
        else if (last != value.length())
            rslt.append(value.substring(last));

        return rslt.toString();
    }

    private static final Pattern tokenMatch = Pattern.compile("\\{([^}]+)\\}");
}
