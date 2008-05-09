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

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

public class StatsCollector {

    private static StatsCollector singleton = null;
    private static NumberFormat realFormat = NumberFormat.getInstance();
    
    static {
        realFormat = NumberFormat.getInstance();
        realFormat.setMaximumFractionDigits(2);
    }

    // Map of all the stats for all threads
    private Map statsMap = new HashMap();

    // Map of the current stats for all threads
    private Map currentStatsMap = new HashMap();

    private Map timers = new HashMap();

    private Set topLevelStats = new HashSet();

    private boolean active = true;

    public static synchronized StatsCollector instance() {
        if (singleton == null) {
            singleton = new StatsCollector();
        }
        return singleton;
    }

    public void clear() {
        statsMap.clear();
        currentStatsMap.clear();
        timers.clear();
        topLevelStats.clear();
        active = true;
    }

    public void startTimer(String label) {
        if (!active) return;

        Stats stats = getStatsForThread(label);
        pushStats(stats);
        getTimerMap().put(stats.getKey(), new Long(System.currentTimeMillis()));
    }

    private String getStatKey(String label) {
        Stats currentStats = getCurrentStats();
        StringBuffer sb = new StringBuffer(label);
        if (currentStats != null) {
            sb.append(Stats.NODE_SEP).append(currentStats.getKey());
        }
        return sb.toString();
    }

    private Stats getStatsForThread(String label) {
        Map threadMap = (Map)statsMap.get(getCurrentThreadKey());
        if (threadMap == null) {
            threadMap = new HashMap();
            statsMap.put(getCurrentThreadKey(), threadMap);
        }

        String statKey = getStatKey(label);
        Stats stats = (Stats)threadMap.get(statKey);

        if (stats == null) {
            stats = createNewStats(label);
            threadMap.put(statKey, stats);
        }
        return stats;
    }

    private Stats createNewStats(String label) {
        Stats stats = new Stats(label);
        Stats currentStats = getCurrentStats();
        if (currentStats != null) {
            currentStats.addChild(stats);
            stats.setParent(currentStats);
        } else {
            topLevelStats.add(stats.getKey());
        }
        return stats;
    }

    public void endTimer(String label) {
        if (!active) return;

        Stats stats = popStats();
        if (!stats.getLabel().equals(label)) {
            System.err.println("StatsCollector in invalid state. Trying to pop '" + label + "' but the current stats was '" + stats.getLabel() + "'. Halting operation!");
            active = false;
        }
        Long startTime = (Long)getTimerMap().get(stats.getKey());
        stats.addValue((double)(System.currentTimeMillis() - startTime.longValue()));
    }

    private Map getTimerMap() {
        Map m = (Map)timers.get(getCurrentThreadKey());
        if (m == null) {
            m = new HashMap();
            timers.put(getCurrentThreadKey(), m);
        }
        return m;
    }

    public Set collate() {
        return collate(null, 0.0);
    }

    public Set collate(Comparator comparator) {
        return collate(comparator, 0.0);
    }

    public Set collate(Comparator comparator, double thres) {
        if (!active) return null;

        Set collatedSet;
        Map collatedMap = new HashMap();

        if (comparator != null) {
            collatedSet = new TreeSet(comparator);
        } else {
            collatedSet = new HashSet();
        }
        
        Stats collatedStats;
        Double value;
        Map threadMap;
        Stats stats;
        Iterator itr = statsMap.keySet().iterator();
        while (itr.hasNext()) {
            threadMap = (Map)statsMap.get(itr.next());
            Iterator itr2 = threadMap.keySet().iterator();
            while (itr2.hasNext()) {
                stats = (Stats)threadMap.get(itr2.next());
                Iterator itr3 = stats.getValues().iterator();
                while (itr3.hasNext()) {
                    value = (Double)itr3.next();
                    collatedStats = (Stats)collatedMap.get(stats.getLabel());
                    if (collatedStats == null) {
                        collatedStats = new Stats(stats.getLabel());
                        collatedMap.put(stats.getKey(), collatedStats);
                    }
                    collatedStats.addValue(value.doubleValue());
                }
            }
        }

        itr = collatedMap.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            stats = (Stats)collatedMap.get(key);

            //ensure the hierarchy in intact
            int pos = key.indexOf(Stats.NODE_SEP);
            if (pos >= 0) {
                Stats parent = (Stats)collatedMap.get(key.substring(pos+Stats.NODE_SEP.length()));
                if (parent != null) {
                    parent.addChild(stats);
                    stats.setParent(parent);
                } else {
                    System.err.println("StatsCollector::collate : parent not found for: " + key);
                }
            }
            
            if (stats.getMean() > thres && stats.getMedian() > thres) {
                collatedSet.add(stats);
            }
        }

        return collatedSet;
    }

    public Set topLevelCollation() {
        if (!active) return null;

        Stats stats;
        Set statsSet = new HashSet();
        Iterator itr = collate(null).iterator();
        while (itr.hasNext()) {
            stats = (Stats)itr.next();
            if (topLevelStats.contains(stats.getKey())) {
                statsSet.add(stats);
            }
        }

        return statsSet;
    }

    private Stats getCurrentStats() {
        Stack stack = getStatsStack();
        if (stack.empty()) return null;
        return (Stats)stack.peek();
    }

    private void pushStats(Stats stats) {
        Stack stack = getStatsStack();
        stack.push(stats);
    }

    private Stats popStats() {
        Stack stack = getStatsStack();
        return (Stats)stack.pop();
    }

    private Stack getStatsStack() {
        Stack stack = (Stack)currentStatsMap.get(getCurrentThreadKey());
        if (stack == null) {
            stack = new Stack();
            currentStatsMap.put(getCurrentThreadKey(), stack);
        }
        return stack;
    }

    private String getCurrentThreadKey() {
        return Thread.currentThread().getName();
    }

    private int getMaxLabelSize(Set s) {
        int maxSize = 0;
        int size;
        Stats stats;
        Iterator itr = s.iterator();
        while (itr.hasNext()) {
            stats = (Stats)itr.next();
            size = stats.getLabel().length();
            if (size > maxSize) {
                maxSize = size;
            }
        }
        return maxSize;
    }

    public void dumpStats() {
        dumpStats(0.0);
    }

    public void dumpStats(double thres) {
        if (!active) return;
        System.out.println(getStatsByMean(thres));
    }

    public String getStatsByMean() {
        return getStatsByMean(0.0);
    }

    public String getStatsByMean(double thres) {
        if (!active) return null;
        return getStats(new MeanComparator(), thres);
    }

    public String getStatsByMedian() {
        return getStatsByMedian(0.0);
    }

    public String getStatsByMedian(double thres) {
        if (!active) return null;
        return getStats(new MedianComparator(), thres);
    }

    public String getStats() {
        return getStats(0.0);
    }

    public String getStats(double thres) {
        if (!active) return null;
        return getStats(null, thres);
    }

    public String getTimerHierarchy() {
        StringBuffer sb = new StringBuffer();
        sb.append("Timer Hierarchy...\n");
        sb.append("(count, mean, median)\n\n");
        Set topLevelStats = topLevelCollation();

        Iterator itr = topLevelStats.iterator();
        while (itr.hasNext()) {
            sb.append(processTimerHierarchy((Stats)itr.next(), 0));
        }
        return sb.toString();
    }

    private String processTimerHierarchy(Stats stats, int stackLevel) {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<=stackLevel; i++) {
            sb.append("  ");
        }
        if (stats.getChildren().size() > 0) {
            sb.append("+ ");
        }
        sb.append('(').append(stats.getCount()).append(", ");
        sb.append(stats.getMean()).append(", ");
        sb.append(stats.getMedian()).append(") - ").append(stats.getLabel()).append('\n');

        Iterator itr = stats.getChildren().iterator();
        while (itr.hasNext()) {
            sb.append(processTimerHierarchy((Stats)itr.next(), stackLevel+1));
        }
        return sb.toString();
    }

    private String getStats(Comparator comparator, double thres) {
        if (!active) return null;
        StringBuffer sb = new StringBuffer();

        Stats stats;
        Set collatedStatsSet = collate(comparator, thres);
        int maxLabelSize = getMaxLabelSize(collatedStatsSet);
        int statFieldSize = 10;

        String labelHeaderLine = makeHeaderLine(maxLabelSize);
        String statHeaderLine = makeHeaderLine(statFieldSize);

        String sep = "  ";
                
        sb.append("\n\n").append("Statistics:\n");
        sb.append(formatField("Operation", maxLabelSize)).append(sep);
        sb.append(formatField("Occurrence", statFieldSize)).append(sep);
        sb.append(formatField("Minimum", statFieldSize)).append(sep);
        sb.append(formatField("Maximum", statFieldSize)).append(sep);
        sb.append(formatField("Mean", statFieldSize)).append(sep);
        sb.append(formatField("Median", statFieldSize)).append('\n');

        sb.append(labelHeaderLine).append(sep);
        sb.append(statHeaderLine).append(sep);
        sb.append(statHeaderLine).append(sep);
        sb.append(statHeaderLine).append(sep);
        sb.append(statHeaderLine).append(sep);
        sb.append(statHeaderLine).append('\n');

        Iterator itr = collatedStatsSet.iterator();
        while (itr.hasNext()) {
            stats = (Stats)itr.next();
            if (stats.getCount() > 0) {
                sb.append(formatField(stats.getLabel(), maxLabelSize)).append(sep);
                sb.append(formatField(stats.getCount(), statFieldSize)).append(sep);
                sb.append(formatField(stats.getMinValue(), statFieldSize)).append(sep);
                sb.append(formatField(stats.getMaxValue(), statFieldSize)).append(sep);
                sb.append(formatField(stats.getMean(), statFieldSize)).append(sep);
                sb.append(formatField(stats.getMedian(), statFieldSize)).append('\n');
            }
        }
        return sb.toString();
    }

    private String makeHeaderLine(int size) {
        StringBuffer sb = new StringBuffer(size);
        for (int i=0; i<size; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    private String formatField(String field, int size) {
        StringBuffer sb = new StringBuffer(size);

        for (int i=0; i<(size-field.length()); i++) {
            sb.append(' ');
        }
        sb.append(field);
        return sb.toString();
    }

    private String formatField(long val, int size) {
        return formatField(Long.toString(val), size);
    }

    private String formatField(double val, int size) {
        String field;
        if (val < 1.0) {
            field = realFormat.format(val);
        } else {
            field = Long.toString((long)val);
        }

        return formatField(field, size);
    }

    private StatsCollector() {}

    public class MeanComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Stats s1 = (Stats)o1;
            Stats s2 = (Stats)o2;
            int val = (int)(s2.getMean() - s1.getMean());
            if (val == 0) {
                val = s1.getLabel().compareTo(s2.getLabel());
            }
            return val;
        }

        public boolean equals(Object obj) {
            return obj == this || obj instanceof MeanComparator;
        }
    }

    public class MedianComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            Stats s1 = (Stats)o1;
            Stats s2 = (Stats)o2;
            int val = (int)(s2.getMedian() - s1.getMedian());
            if (val == 0) {
                val = s1.getLabel().compareTo(s2.getLabel());
            }
            return val;
        }

        public boolean equals(Object obj) {
            return obj == this || obj instanceof MedianComparator;
        }
    }
}
