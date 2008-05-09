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
package net.unicon.academus.cms.api.impl;

//Java SDK API
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

// Academus CMS

// Academus API
import net.unicon.academus.cms.api.IAcademusOffering;
import net.unicon.academus.domain.lms.Offering;
import net.unicon.academus.domain.lms.ensemble.AdjunctOfferingData;

/**
 *  The Academus CMS Facade Offering implementation.
 */
public class AcademusOffering implements IAcademusOffering {

    private long offeringId;
    private String name;
    private String description;
    private String location;
    private String startTime;
    private String endTime;
    private String startDate;
    private String endDate;
    private String weekDays;
    private boolean published;
    private boolean buyNowEnabled;
    private Set topicIds;


    public static final int[] DAY_CODES = {2, 4, 8, 16, 32, 64, 128};
    public static final String[] DAY_NAMES =
            {"M","T", "W", "Th", "F", "Sa", "Su"};

    /**
     * Constructor for AcademusOffering. Requires an offering
     * instance. The offering instance is used to initialize
     * all values of the AcademusOffering wrapper object.
     * @param offering The offering instance to be used
     * to initialize this instance.
     */
    public AcademusOffering (Offering offering) {

        if (offering == null) {

            throw new IllegalArgumentException(
                    "Offering instance cannot be null.");
        }

        this.offeringId = offering.getId();

        setOfferingName(offering.getName());
        setOfferingDescription(offering.getDescription());
        setLocation(offering.getOptionalLocation());

        // Set offering starting time
        int minutes = offering.getOptionalMinuteStart();
        int hours = offering.getOptionalHourStart();
        boolean pm = (offering.getOptionalAmPmStart() == 2) ? true: false;
        setOfferingStartTime(minutes, hours, pm);

        // Set offering ending time
        minutes = offering.getOptionalMinuteEnd();
        hours = offering.getOptionalHourEnd();
        pm = (offering.getOptionalAmPmEnd() == 2) ? true: false;

        setOfferingEndTime(minutes, hours, pm);

        // Set offering start date
        int day = offering.getOptionalDayStart();
        int month = offering.getOptionalMonthStart();
        int year = offering.getOptionalYearStart();
        setOfferingStartDate(day, month, year);

        // Set offering end date
        day = offering.getOptionalDayEnd();
        month = offering.getOptionalMonthEnd();
        year = offering.getOptionalYearEnd();
        setOfferingEndDate(day, month, year);

        setOfferingWeekDays(offering.getDaysOfWeek());

        topicIds = offering.getTopicIds();
    }

    /**
     * AcademusOffering constructor that takes an offering
     * and an adjuct offering data instance. The 2 instances
     * are used to initialize all values of AcademusOffering.
     * @param offering The offering instance to be used
     * to initialize this instance.
     * @param offeringData The adjuct offering data instance to
     * be used to initialize this instance.
     */
     public AcademusOffering(
            Offering offering, AdjunctOfferingData offeringData) {

        this(offering);

        this.buyNowEnabled = offeringData.hasBuyNowEnabled();
        this.published= offeringData.isPublished();
     }

    /**
     * The offering id of this offering.
     * @return The offering id.
     */
    public long getOfferingId () {

        return this.offeringId;
    }

    /**
     * The offering name of this offering.
     * @return The offering name.
     */
    public String getOfferingName() {

        return this.name;
    }

    private void setOfferingName(String offeringName) {

        this.name = offeringName;
    }

    /**
     * The offering description of this offering.
     * @return The offering description.
     */
    public String getOfferingDescription() {

        return this.description;
    }

    private void setOfferingDescription(String offeringDescription) {

        this.description = offeringDescription;
    }

    /**
     * The offering location of this offering.
     * @return The offering location.
     */
    public String getLocation () {

        return this.location;
    }

    private void setLocation (String location) {

        this.location = location;
    }

    /**
     * The end time of this offering in the form of HH:MM AM/PM.
     * @return The offering end time.
     */
    public String getOfferingEndTime() {

        return this.endTime;
    }

    private void setOfferingEndTime(int minutes, int hours, boolean pm) {

        this.endTime = convertToTime(minutes, hours, pm);
    }

    /**
     * The start time of this offering in the form of HH:MM AM/PM.
     * @return The offering start time.
     */
    public String getOfferingStartTime() {

        return this.startTime;
    }

    private void setOfferingStartTime(int minutes, int hours, boolean pm) {

        this.startTime = convertToTime(minutes, hours, pm);
    }

    /**
     * The days during which the offering is offered in the
     * following format:
     * MTWThFSaSu
     *
     * @return The days this offering is offered.
     */
    public String getOfferingWeekDays() {

        return this.weekDays;
    }

    private void setOfferingWeekDays(int weekDays) {

        int days = DAY_NAMES.length;

        StringBuffer daysBuffer = new StringBuffer(80);

        for (int index = 0; index < days; index++) {

            int dayCode = DAY_CODES[index];

            if ((dayCode & weekDays) != 0) {
                daysBuffer.append(DAY_NAMES[index]);
            }
        }

        this.weekDays = daysBuffer.toString();
    }

    /**
     * The start date of this offering.
     * @return The offering start date.
     */
    public String getOfferingStartDate() {

        return this.startDate;
    }

    private void setOfferingStartDate(int day, int month, int year) {

        this.startDate = convertToDate(day, month, year);
    }

    /**
     * The end date of this offering.
     * @return The offering end date.
     */
    public String getOfferingEndDate() {

        return this.endDate;
    }

    /**
     * Determines if offering is published in the
     * course catalog.
     * @return Returns true if offering is published,
     * false otherwise.
     */
    public boolean isPublished() {

        return this.published;
    }

    /**
     * Determines if offering has "Buy Now" enabled.
     * @return Returns true if offering has "Buy Now"
     * enabled, false otherwise.
     */
    public boolean isBuyNowEnabled() {

        return this.buyNowEnabled;
    }

    private void setOfferingEndDate(int day, int month, int year) {

        this.endDate = convertToDate(day, month, year);
    }

    /**
     * Returns an id set of topics this offering belongs to.
     * @return A set of topic ids.
     */
    public Set getTopicIds() {

        return this.topicIds;
    }

    // Converts day, month and year into a string representation of a date
    private String convertToDate (int day, int month, int year) {

        Calendar calendar = new GregorianCalendar(year, month - 1, day);

        StringBuffer dateBuffer = new StringBuffer(80);

        month = calendar.get(Calendar.MONTH);

        switch (month) {

            case Calendar.JANUARY:
                dateBuffer.append("Jan.");
                break;
            case Calendar.FEBRUARY:
                dateBuffer.append("Feb.");
                break;
            case Calendar.MARCH:
                dateBuffer.append("Mar.");
                break;
            case Calendar.APRIL:
                dateBuffer.append("Apr.");
                break;
            case Calendar.MAY:
                dateBuffer.append("May");
                break;
            case Calendar.JUNE:
                dateBuffer.append("Jun.");
                break;
            case Calendar.JULY:
                dateBuffer.append("Jul.");
                break;
            case Calendar.AUGUST:
                dateBuffer.append("Aug.");
                break;
            case Calendar.SEPTEMBER:
                dateBuffer.append("Sept.");
                break;
            case Calendar.OCTOBER:
                dateBuffer.append("Oct.");
                break;
            case Calendar.NOVEMBER:
                dateBuffer.append("Nov.");
                break;
            case Calendar.DECEMBER:
                dateBuffer.append("Dec.");
                break;
        }

        dateBuffer.append(" ");
        dateBuffer.append(day);
        dateBuffer.append(", ");
        dateBuffer.append(year);

        return dateBuffer.toString();
    }

    // Converts hours. minutes and AM/PM flag into a string representation of time
    private String convertToTime (int minutes, int hours, boolean pm) {

        StringBuffer timeBuffer = new StringBuffer(80);

        timeBuffer.append(Integer.toString(hours));
        timeBuffer.append(":");
        if (minutes < 10){
            timeBuffer.append("0");
        }

        timeBuffer.append(minutes);
        timeBuffer.append(" ");

        if (pm) {
            timeBuffer.append("PM");
        } else {
            timeBuffer.append("AM");
        }

        return timeBuffer.toString();
    }
}

