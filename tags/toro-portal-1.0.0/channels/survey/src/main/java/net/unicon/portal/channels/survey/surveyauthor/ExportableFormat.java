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
package net.unicon.portal.channels.survey.surveyauthor;

import net.unicon.academus.apps.form.DataDetailData;
import net.unicon.academus.apps.form.FormDataData;
import net.unicon.academus.apps.rad.XMLData;
import net.unicon.academus.apps.survey.SurveyData;
import net.unicon.portal.channels.rad.Channel;

import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

public class ExportableFormat implements IExportFormat {

    private static String fieldSep =
        Channel.getRADProperty("survey.export.fieldSeparator");
    private static String multiAnswerSep =
        Channel.getRADProperty("survey.export.multiAnswerSeparator");

    private static SimpleDateFormat fmt;

    static {
        fmt = new SimpleDateFormat(
            Channel.getRADProperty("survey.export.exportableDateFormat"));
        TimeZone z = TimeZone.getDefault(); // default (duh)...
        String id = Channel.getRADProperty("display.timezone");
        if (id != null && !id.trim().equals("")) {
            z = TimeZone.getTimeZone(id);
        }
        fmt.setTimeZone(z);
    }

    public ExportableFormat(String fieldSep, String multiAnswerSep) {
    	this.fieldSep = fieldSep;
    	this.multiAnswerSep = multiAnswerSep;
    }

    public void summaryTitle(StringBuffer out) {
        if (out != null) {
            out.append("Respondent").append(fieldSep);
            out.append("Date").append(fieldSep);
        }
    }

    public void pageTitle(StringBuffer out, XMLData data) {}

    public void question(StringBuffer out, XMLData data, boolean hasSubQuestions) {
        if (!hasSubQuestions && out != null) {
            if (data != null) {
                out.append((String)data.get());
            }
            out.append(fieldSep);
        }
    }

    public void questionWithAnswers(StringBuffer out, XMLData data) {}

    public void subQuestion(StringBuffer out, XMLData data) {
        if (out != null) {
            if (data != null) {
                out.append((String)data.get());
            }
            out.append(fieldSep);
        }
    }

    public void subQuestionWithAnswers(StringBuffer out, XMLData data) {}

    public void response(StringBuffer out, XMLData data) {}

    public void numResponses(StringBuffer out, DataDetailData data) {}

    public void percent(StringBuffer out, DataDetailData data) {}

    public void notFoundPercent(StringBuffer out, XMLData data) {}

    public void textSummary(StringBuffer out) {}

    public void respondentsTitle(StringBuffer out) {}

    public void respondentsUser(StringBuffer out, FormDataData data) {}

    public void detailsUser(StringBuffer out, SurveyData sData, FormDataData data) {
        if (out != null) {
            out.append("\n");
            if (data != null) {
                String uName = null;
                if (data.getUserName() != null && !data.getUserName().trim().equals("")) {
                    if (sData.getType().equals("Election")) {
                        uName = "[Anonymous]";
                    } else {
                        uName = data.getUserName();
                    }
                }
                out.append(uName != null?uName:"[not provided]").append(fieldSep);
                if (data.getA("Created") != null && data.getA("Created") instanceof Date) {
                    Date d = (Date) data.getA("Created");
                    out.append(fmt.format(d));
                }
                out.append(fieldSep);
            }
        }
    }

    public void answer(StringBuffer out, AnswerKey key, Map answers, Object[] data) {
        if (out != null) {
            if (key != null && answers != null) {
                StringBuffer ans = new StringBuffer();
                if (answers.get(key) instanceof List) { // multiple-choice question...
                    Iterator ansCodes = ((List) answers.get(key)).iterator();
                    while (ansCodes.hasNext()) {
                        McResponse ansc = (McResponse) ansCodes.next();
                        for (int i=0; data != null && i<data.length; i++) {
                            XMLData xData = (XMLData)data[i];
                            String singleAnswer = null;
                            if (xData.getA("data-id").equals(ansc.getDcId().toString())) {
                                singleAnswer = xData.get();
                                if (ansc.getText() != null) {
                                    // text writein answer .. display only the writein value
                                    // removing the prepended text ID. Ignore the multiple choice values
                                    // that get saved with it
                                    singleAnswer = ansc.getText().replaceAll(
                                        "[0-9]+\\|[0-9]+\\|[0-9]+\\|[0-9]+\\|[0-9]+\\|[0-9]+\\|","");
                                    out.append(singleAnswer).append(fieldSep);
                                    return;
                                }
                            }
                            if (singleAnswer != null) {
                                if (ans.length() > 0) {
                                    ans.append(multiAnswerSep);
                                }
                                ans.append(singleAnswer);
                            }
                        }
                    }
                } else if (answers.get(key) instanceof String) {   // textual question...
                    // replace all newlines and control-M's so they aren't treated as record separators
                    ans.append(((String) answers.get(key)).replaceAll("\n", " ").replaceAll("\r", " "));
                }
                out.append(ans);
            }
            out.append(fieldSep);
        }
    }
}
