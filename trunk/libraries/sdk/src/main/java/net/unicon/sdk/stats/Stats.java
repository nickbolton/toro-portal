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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Stats {

    public static final String NODE_SEP = "%STATS_NODE_SEPARATOR%";

    private double maxValue = 0;
    private double minValue = Double.MAX_VALUE;
    private long count = 0;
    private double sum;
    private String label;
    private Map occurrences = new TreeMap();
    private Set children = new HashSet();
    private Stats parent;

    public Stats(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public long getCount() {
        return count;
    }

    public double getMean() {
        return sum/count;
    }

    public void addChild(Stats stats) {
        children.add(stats);
    }

    public Set getChildren() {
        return children;
    }

    public void setParent(Stats stats) {
        this.parent = stats;
    }

    public Stats getParent() {
        return parent;
    }

    public String getKey() {
        StringBuffer sb = new StringBuffer(label);
        if (parent != null) {
            sb.append(NODE_SEP).append(parent.getKey());
        }
        return sb.toString();
    }

    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Stats) {
            Stats aStats = (Stats)obj;
            ret = this.label.equals(aStats.getLabel());
        }
        return ret;
    }

    public String toString() {
        return getKey();
    }

    public List getValues() {
        List list = new ArrayList();
        Iterator itr = occurrences.keySet().iterator();
        Double key;
        StatValue statValue;
        while (itr.hasNext()) {
            key = (Double)itr.next();
            statValue = (StatValue)occurrences.get(key);
            for (long i=0; i<statValue.getCount(); i++) {
                list.add(key);
            }
        }
        return list;
    }

    public double getMedian() {
        List list = getValues();

        int size = list.size();
        int mod = size%2;
        int mid = (size/2) + mod;

        if (size == 0) return 0.0;

        double med1 = ((Double)list.get(mid-1)).doubleValue();
        if (mod == 1) {
            return med1;
        }
        return (med1 + ((Double)list.get(mid)).doubleValue())/2.0;
    }

    public void addValue(double value) {
        count++;
        sum+=value;
        if (value > maxValue) {
            maxValue = value;
        }
        if (value < minValue) {
            minValue = value;
        }
        Double key = new Double(value);
        StatValue statValue = (StatValue)occurrences.get(key);
        if (statValue == null) {
            occurrences.put(key, new StatValue(value));
        } else {
            statValue.increment();
        }
    }
}
