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
package net.unicon.sdk.stats;

public class StatValue implements Comparable {

    private double value = 0;
    private long count = 0;

    public StatValue(double value) {
        this.value = value;
        this.count = 1;
    }

    public void increment() {
        count++;
    }

    public double getValue() {
        return value;
    }

    public long getCount() {
        return count;
    }

    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof StatValue) {
            StatValue aStatValue = (StatValue)obj;
            ret = (this.value == aStatValue.getValue());
        }
        return ret;
    }

    public int compareTo(Object o) {
        StatValue aStatValue = (StatValue)o;
        return (int)(aStatValue.getValue() - value);
    }
}