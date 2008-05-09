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

public class HumanReadableFormat implements IExportFormat {

    private static SimpleDateFormat fmt;

    static {
        fmt = new SimpleDateFormat(
            Channel.getRADProperty("survey.export.readableDateFormat"));
        TimeZone z = TimeZone.getDefault(); // default (duh)...
        String id = Channel.getRADProperty("display.timezone");
        if (id != null && !id.trim().equals("")) {
            z = TimeZone.getTimeZone(id);
        }
        fmt.setTimeZone(z);
    }

    public HumanReadableFormat() {
    }

    public void summaryTitle(StringBuffer out) {
        if (out != null) {
            out.append("#### Survey Summary ####\n\n");
        }
    }

    public void pageTitle(StringBuffer out, XMLData data) {
        if (out != null) {
            out.append("\n\t[");
            if (data != null) {
                out.append((String)data.getA("title"));
            }
            out.append("]\n");
        }
    }

    public void question(StringBuffer out, XMLData data, boolean hasSubQuestions) {
        if (out != null) {
            out.append("\n\t");
            if (data != null) {
                out.append((String)data.get());
            }
            out.append("\n");
        }
    }

    // This does the same thing as question(), but is meant to be formatted with answers
    public void questionWithAnswers(StringBuffer out, XMLData data) {
        if (out != null) {
            out.append("\n\t");
            if (data != null) {
                out.append((String)data.get());
            }
            out.append("\n");
        }
    }

    public void subQuestion(StringBuffer out, XMLData data) {
        if (out != null) {
            out.append("\n\t\t");
            if (data != null) {
                out.append((String)data.get());
            }
            out.append("\n");
        }
    }

    // This does the same thing as subQuestion(), but is meant to be formatted with answers
    public void subQuestionWithAnswers(StringBuffer out, XMLData data) {
        if (out != null) {
            out.append("\n\t\t");
            if (data != null) {
                out.append((String)data.get());
            }
            out.append("\n");
        }
    }

    public void response(StringBuffer out, XMLData data) {
        if (out != null) {
            out.append("\t\t\t");
            if (data != null) {
                out.append((String)data.get());
            }
        }
    }

    public void numResponses(StringBuffer out, DataDetailData data) {
        if (out != null) {
            out.append("\t");
            if (data != null) {
                out.append(data.getNumResp().toString());
            }
        }
    }

    public void percent(StringBuffer out, DataDetailData data) {
        if (out != null) {
            out.append("\t");
            if (data != null) {
                out.append(data.getPercent().toString());
            }
            out.append("\n");
        }
    }

    public void notFoundPercent(StringBuffer out, XMLData data) {
        if (out != null) {
            out.append("\t\t\t");
            if (data != null) {
                out.append(data.get());
            }
            out.append("\t0\t0%\n");
        }
    }

    public void textSummary(StringBuffer out) {
        if (out != null) {
            out.append("\t\t\t[see individual responses...]\n");
        }
    }

    public void respondentsTitle(StringBuffer out) {
        if (out != null) {
            out.append("\n#### List of Respondents ####\n\n");
        }
    }

    public void respondentsUser(StringBuffer out, FormDataData data) {
        if (out != null) {
            out.append("\tUser ");
            if (data != null) {
                String uName = null;
                if (data.getUserName() != null && !data.getUserName().trim().equals("")) {
                    uName = data.getUserName();
                }
                out.append(uName != null?uName:"[not provided]").append(" responded on ");
                if (data.getA("Created") != null && data.getA("Created") instanceof Date) {
                    Date d = (Date) data.getA("Created");
                    out.append(fmt.format(d));
                } else {
                    out.append("[not specified]");
                }
            }
            out.append("\n");
        }
    }

    public void detailsUser(StringBuffer out, SurveyData sData, FormDataData data) {
        if (out != null) {
            out.append("\n#### Response for User:  ");
            if (data != null) {
                String uName = null;
                if (data.getUserName() != null && !data.getUserName().trim().equals("")) {
                    if (sData.getType().equals("Election")) {
                        uName = "[Anonymous]";
                    } else {
                        uName = data.getUserName();
                    }
                }
                out.append(uName != null?uName:"[not provided]").append(" ####");
                if (data.getA("Created") != null && data.getA("Created") instanceof Date) {
                    Date d = (Date) data.getA("Created");
                    out.append("\n#### Submitted:  ").append(fmt.format(d)).append(" ####\n");
                }
            }
            out.append("\n");
        }
    }

    public void answer(StringBuffer out, AnswerKey key, Map answers, Object[] data) {
        if (out != null) {
            out.append("\t\t\t");
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
                                    out.append(singleAnswer).append("\n");
                                    return;
                                }
                            }
                            if (singleAnswer != null) {
                                if (ans.length() > 0) {
                                    ans.append(", ");
                                }
                                ans.append(singleAnswer);
                            }
                        }
                    }
                } else if (answers.get(key) instanceof String) {   // textual question...
                    ans.append((String) answers.get(key));
                }
                out.append(ans);
            }
            out.append("\n");
        }
    }
}
