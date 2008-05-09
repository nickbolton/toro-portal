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

import java.util.Map;

public interface IExportFormat {

    public void summaryTitle(StringBuffer out);
    public void pageTitle(StringBuffer out, XMLData data);
    public void question(StringBuffer out, XMLData data, boolean hasSubQuestions);
    public void questionWithAnswers(StringBuffer out, XMLData data);
    public void subQuestion(StringBuffer out, XMLData data);
    public void subQuestionWithAnswers(StringBuffer out, XMLData data);
    public void response(StringBuffer out, XMLData data);
    public void numResponses(StringBuffer out, DataDetailData data);
    public void percent(StringBuffer out, DataDetailData data);
    public void notFoundPercent(StringBuffer out, XMLData data);
    public void textSummary(StringBuffer out);
    public void respondentsTitle(StringBuffer out);
    public void respondentsUser(StringBuffer out, FormDataData data);
    public void detailsUser(StringBuffer out, SurveyData sData, FormDataData data);
    public void answer(StringBuffer out, AnswerKey key, Map answers, Object[] data);
}
