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

package net.unicon.academus.spell;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.URLDecoder;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory; 

public class SpellCheckerServlet extends HttpServlet {
   private static Log log = LogFactory.getLog(SpellCheckerServlet.class); 

    /** Encoding for URL decoding/encoding. Not passed to aspell. */
    private static final String ENCODING = "UTF-8";

    private String lang = "en_US";
    private String aspell_loc = "aspell";
    private String known_good = "A";
    private boolean hasHTMLFilter = false;

    private String header_resource = "/rendering/fragments/spell/header.html";
    private String footer_resource = "/rendering/fragments/spell/footer.html";

    private ServletContext ctx = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ctx = config.getServletContext();

        lang = getParam(config, "lang", lang);
        aspell_loc = getParam(config, "aspell", aspell_loc);
        known_good = getParam(config, "known-good", known_good);

        try {
            hasHTMLFilter = checkHTMLFilter();
        } catch (Exception ex) {
            log.warn("Unable to verify Aspell HTML Filter support", ex);
            hasHTMLFilter = false;
        }

        header_resource = getParam(config, "header", header_resource);
        footer_resource = getParam(config, "footer", footer_resource);
    
        log.info("SpellCheckerServlet initialized.");
    }

    private static String getParam(ServletConfig config, String name, String def) {
        String ret = config.getInitParameter(name);

        if (ret == null)
            ret = def;

        return ret;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
                                                 throws ServletException {
        doPost(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res)
                                                 throws ServletException {
        res.setContentType("text/html");
        PrintWriter out = null;
        String[] chk = req.getParameterValues("textinputs");

        if (log.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < chk.length; i++)
                buf.append('"').append(chk[i]).append("\"; ");
            log.debug("Spell check request: "+buf.toString());
        }

        try {
            out = res.getWriter();

            printHeader(out);
            performSpellCheck(out, chk);
            printFooter(out);

            out.close();
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private boolean checkHTMLFilter() throws Exception {
        boolean rslt = false;

        String[] cmdline = new String[3];
        cmdline[0] = aspell_loc;
        cmdline[1] = "dump";
        cmdline[2] = "filters";

        Process p = Runtime.getRuntime().exec(cmdline);
        BufferedReader pin = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = pin.readLine();
        while (!rslt && line != null) {
            if (line.startsWith("html"))
                rslt = true;

            line = pin.readLine();
        }
        pin.close();
        p.destroy();

        log.info("Aspell HTML Filter support: "+rslt);
        return rslt;
    }

    /**
     * Perform the actual spellcheck operation using the aspell process.
     */
    private synchronized void performSpellCheck(PrintWriter out, String[] chk) throws Exception {
        ArrayList cmdline = new ArrayList();
        cmdline.add(aspell_loc);
        cmdline.add("-a");
        cmdline.add("--lang="+lang);
        if (hasHTMLFilter)
            cmdline.add("--mode=html");

        if (log.isDebugEnabled()) {
            StringBuffer cmd = new StringBuffer();
            Iterator it = cmdline.iterator();
            while (it.hasNext()) {
                cmd.append(it.next()).append(' ');
            }
            log.debug("Running aspell command: "+cmd.toString());
        }

        Process p = Runtime.getRuntime().exec((String[])cmdline.toArray(new String[0]));
        PrintWriter pout = new PrintWriter(p.getOutputStream());

        // Start the reader thread. This is threaded so that we do not run into
        // problems with the input stream buffer running out prior to
        // processing the results.
        Thread inThread = new InReader(p.getInputStream(), chk, out);
        inThread.start();

        for (int i = 0; i < chk.length; i++) {
            // Decode the input string if necessary.
            chk[i] = URLDecoder.decode(chk[i], ENCODING);

            // Force a line containing '*' to signal the input switch
            pout.println("%"); // Exit terse mode
            pout.println("^"+known_good); // Emit a known-good word
            pout.println("!"); // Enter terse mode

            String[] lines = chk[i].split("\n");
            for (int k = 0; k < lines.length; k++) {
                pout.println(lines[k]);
            }

            pout.flush();
        }

        // Close the input stream to signal completion to aspell
        pout.flush();
        pout.close();

        // Wait for input reader thread to finish.
        inThread.join();

        // Kill the aspell process
        p.destroy();

        log.debug("SpellCheckerServlet completed processing with aspell.");
    }

    // This thread performs the input stream reading from aspell. This is
    // necessary so that the input stream buffer does not overflow prior to
    // reading the results. The size of the buffer is operating system
    // dependent, and as such cannot be depended on.
    private class InReader extends Thread {
        private InputStream in;
        private String[] chk;
        private PrintWriter out;

        public InReader(InputStream in, String[] chk, PrintWriter out) {
            this.in = in;
            this.chk = chk;
            this.out = out;
        }

        public void run() {
            try {
                StringBuffer buf = new StringBuffer();
                StringBuffer errorBuf = new StringBuffer();

                printInputs(buf, chk);

                BufferedReader pin = new BufferedReader(new InputStreamReader(in));

                String line = pin.readLine();
                int inputIdx = -1;
                int badWordIdx = 0;
                boolean error = false;

                log.debug("InReader beginning processing.");

                if (line == null)
                    log.warn("Read null as first line!");

                while (line != null) {
                    if (line.length() > 0) {
                        if (log.isDebugEnabled())
                            log.debug("Read line: "+line);
                        char t = line.charAt(0);

                        if (t == '&' || t == '#') {
                            // if '&', then not in dictionary but has suggestions
                            // if '#', then not in dictionary and no suggestions
                            String[] tmp = line.split(" ", 5);
                            printWord(buf, tmp[1], inputIdx, badWordIdx);

                            String[] suggs = null;
                            if (tmp.length == 5) {
                                suggs = tmp[4].split(", ");
                            } else
                                suggs = new String[0];
                            printSuggestions(buf, inputIdx, badWordIdx, suggs);

                            badWordIdx++;

                        } else if (t == '*') {
                            // if '*', then it is a delimiter between text inputs

                            inputIdx++;
                            printInputDecl(buf, inputIdx);
                            badWordIdx = 0;
                        } else if (t == '@') {
                            // Version... Ignore.
                        } else {
                            log.warn("Aspell warning: "+line);
                            error = true;
                            errorBuf.append(line).append("\\n");
                        }
                    } else if (log.isDebugEnabled()) {
                        log.debug("Read empty line");
                    }

                    line = pin.readLine();
                }

                if (error) {
                    printError(out, errorBuf.toString());
                } else {
                    out.print(buf.toString());
                }
                out.flush();

                pin.close();

                log.debug("InReader completed processing.");

            } catch (Exception ex) {
                throw new RuntimeException("Failed to perform spell check: InReader", ex);
            }
        }
    }

    private void printError(PrintWriter out, String error) throws IOException {
        out.print("error = 'Error executing `");
        out.print(aspell_loc);
        out.print("`: \\n");
        out.print(escapeQuotes(error));
        out.println("';");
        log.error("SpellCheckerServlet: Error occurred during execution of `"+aspell_loc+"`: "+error);
    }

    private void printInputs(StringBuffer buf, String[] inpt) {
        for (int i = 0; i < inpt.length; i++) {
            buf.append("textinputs[")
               .append(i)
               .append("] = decodeURIComponent('")
               .append(escapeQuotes(inpt[i]))
               .append("');\n");
        }
    }

    private void printInputDecl(StringBuffer buf, int tidx) {
        buf.append("words[").append(tidx).append("] = [];\n");
        buf.append("suggs[").append(tidx).append("] = [];\n");
    }

    private void printWord(StringBuffer buf, String word, int tidx, int widx) {
        buf.append("words[")
           .append(tidx)
           .append("][")
           .append(widx)
           .append("] = '")
           .append(escapeQuotes(word))
           .append("';\n");
    }

    private void printSuggestions(StringBuffer buf, int tidx, int widx, String[] suggs) {
        buf.append("suggs[")
           .append(tidx)
           .append("][")
           .append(widx)
           .append("] = [");
        for (int i = 0; i < suggs.length; i++) {
            buf.append("'")
               .append(escapeQuotes(suggs[i]))
               .append("'");
            if (i+1 < suggs.length)
                buf.append(", ");
        }
        buf.append("];\n");
    }

    private String escapeQuotes(String str) {
        String rslt = str;

        rslt = rslt.replaceAll("\\\\", "\\\\\\\\");
        rslt = rslt.replaceAll("\r", "");
        rslt = rslt.replaceAll("\n", "\\\\n");
        rslt = rslt.replaceAll("'", "\\\\'");

        return rslt;
    }

    private void printResource(PrintWriter out, String resource) throws IOException {
        InputStreamReader is = new InputStreamReader(ctx.getResourceAsStream(resource));

        char[] buf = new char[4096];
        int r = 0;
        while ((r = is.read(buf, 0, buf.length)) != -1) {
            out.write(buf, 0, r);
        }
        is.close();
        out.flush();
    }

    private void printHeader(PrintWriter out) throws IOException {
        printResource(out, header_resource);
    }

    private void printFooter(PrintWriter out) throws IOException {
        printResource(out, footer_resource);
    }
}

