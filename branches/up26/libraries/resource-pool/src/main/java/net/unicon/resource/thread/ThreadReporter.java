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
package net.unicon.resource.thread;

import java.io.*;
import java.util.*;

public class ThreadReporter {
    protected int createdCount = 0;
    protected MiniBag createdByThreadClass = new MiniBag();
    protected MiniBag createdByRunnableClass = new MiniBag();
    protected int activeCount = 0;
    protected long activeStackSize = 0L;
    protected MiniBag activeByThreadClass = new MiniBag();
    protected MiniBag activeByRunnableClass = new MiniBag();

    public static final ThreadReporter soleInstance = new ThreadReporter();

    public static ThreadReporter getInstance() {
        return soleInstance;
    }

    private ThreadReporter() {
    }

    public synchronized void registerCreation(Thread thread, boolean daemon, Runnable target, ThreadGroup group, long stackSize) {
        Class threadClass = thread.getClass();
        String runnableClassName = (target == null) ? "Null" : target.getClass().getName();
        createdByThreadClass.add(threadClass);
        createdByRunnableClass.add(runnableClassName);
        activeByThreadClass.add(threadClass);
        activeByRunnableClass.add(runnableClassName);
        createdCount++;
        activeCount++;
        activeStackSize += stackSize;
    }

    public synchronized void registerDestruction(Thread thread, boolean daemon, Runnable target, ThreadGroup group, long stackSize) {
        Class threadClass = thread.getClass();
        String runnableClassName = (target == null) ? "Null" : target.getClass().getName();
        activeByThreadClass.remove(threadClass);
        activeByRunnableClass.remove(runnableClassName);
        activeCount--;
        activeStackSize -= stackSize;
    }

    public void report(PrintStream out) {
        if (createdCount == 0) {
            return;
        }

        try {
            StringBuffer message = new StringBuffer("Report from ThreadReporter:");
            message.append("\n\tcreatedCount = " + createdCount);
            message.append("\n\tactiveCount = " + activeCount);
            message.append("\n\tdestroyedCount = " + getDestroyedCount());
            message.append("\n\tactiveStackSize = " + activeStackSize);

            List threadClasses = createdByThreadClass.getKeys();
            Map nameMap = new HashMap(threadClasses.size() * 3 / 2);
            for (int i = 0; i < threadClasses.size(); i++) {
                Class c = (Class) threadClasses.get(i);
                nameMap.put(c.getName(), c);
            }
            List threadClassNames = new ArrayList(nameMap.keySet());
            Collections.sort(threadClassNames);
            message.append("\n\tThreads by Thread Class");
            message.append("\n\t  Total    Active   Thread Class");
            for (int i = 0; i < threadClassNames.size(); i++) {
                String name = (String) threadClassNames.get(i);
                Class c = (Class) nameMap.get(name);
                long active = activeByThreadClass.get(c);
                long total = createdByThreadClass.get(c);
                message.append("\n\t");
                pad(message, total, 8);
                message.append("    ");
                pad(message, active, 5);
                message.append("   ");
                message.append(name);
            }

            List runnableClassNames = createdByRunnableClass.getKeys();
            Collections.sort(runnableClassNames);
            message.append("\n\tThreads by Runnable Class");
            message.append("\n\t  Total    Active   Runnable Class");
            for (int i = 0; i < runnableClassNames.size(); i++) {
                String name = (String) runnableClassNames.get(i);
                long active = activeByRunnableClass.get(name);
                long total = createdByRunnableClass.get(name);
                message.append("\n\t");
                pad(message, total, 8);
                message.append("    ");
                pad(message, active, 5);
                message.append("   ");
                message.append(name);
            }

            out.println(message.toString());
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
    }

    public void report(PrintWriter out) {
        if (createdCount == 0) {
            return;
        }

        try {
            StringBuffer message = new StringBuffer("Report from ThreadReporter:");
            message.append("\n\tcreatedCount = " + createdCount);
            message.append("\n\tactiveCount = " + activeCount);
            message.append("\n\tdestroyedCount = " + getDestroyedCount());
            message.append("\n\tactiveStackSize = " + activeStackSize);

            List threadClasses = createdByThreadClass.getKeys();
            Map nameMap = new HashMap(threadClasses.size() * 3 / 2);
            for (int i = 0; i < threadClasses.size(); i++) {
                Class c = (Class) threadClasses.get(i);
                nameMap.put(c.getName(), c);
            }
            List threadClassNames = new ArrayList(nameMap.keySet());
            Collections.sort(threadClassNames);
            message.append("\n\tThreads by Thread Class");
            message.append("\n\t  Total    Active   Thread Class");
            for (int i = 0; i < threadClassNames.size(); i++) {
                String name = (String) threadClassNames.get(i);
                Class c = (Class) nameMap.get(name);
                long active = activeByThreadClass.get(c);
                long total = createdByThreadClass.get(c);
                message.append("\n\t");
                pad(message, total, 8);
                message.append("    ");
                pad(message, active, 5);
                message.append("   ");
                message.append(name);
            }

            List runnableClassNames = createdByRunnableClass.getKeys();
            Collections.sort(runnableClassNames);
            message.append("\n\tThreads by Runnable Class");
            message.append("\n\t  Total    Active   Runnable Class");
            for (int i = 0; i < runnableClassNames.size(); i++) {
                String name = (String) runnableClassNames.get(i);
                long active = activeByRunnableClass.get(name);
                long total = createdByRunnableClass.get(name);
                message.append("\n\t");
                pad(message, total, 8);
                message.append("    ");
                pad(message, active, 5);
                message.append("   ");
                message.append(name);
            }

            out.println(message.toString());
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
    }

    protected void pad(StringBuffer buffer, long value, int fieldSize) {
        String string = String.valueOf(value);
        for (int i = string.length(); i < fieldSize; i++) {
            buffer.append(' ');
        }
        buffer.append(string);
    }

    public int getCreatedCount() {
        return createdCount;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public int getDestroyedCount() {
        return createdCount - activeCount;
    }

    public long getActiveStackSize() {
        return activeStackSize;
    }

    public Map getCreatedByThreadClass() {
        return createdByThreadClass.asMap();
    }

    public Map getCreatedByRunnableClass() {
        return createdByRunnableClass.asMap();
    }

    public Map getActiveByThreadClass() {
        return activeByThreadClass.asMap();
    }

    public Map getActiveByRunnableClass() {
        return activeByRunnableClass.asMap();
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 5; i++) {
            ThreadReporter.getInstance().report(System.out);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }
    }
}

class MiniBag {
    protected Map contents = new HashMap();

    public List getKeys() {
        return new ArrayList(contents.keySet());
    }

    public long get(Object o) {
        MyLong count = (MyLong) contents.get(o);
        return (count == null) ? 0L : count.getValue();
    }

    public void add(Object o) {
        MyLong count = (MyLong) contents.get(o);
        if (count == null) {
            count = new MyLong(0L);
            contents.put(o, count);
        }
        count.addOne();
    }

    public void remove(Object o) {
        MyLong count = (MyLong) contents.get(o);
        if (count != null && count.isPositive()) {
            count.subtractOne();
        }
    }

    public Map asMap() {
        Map answer = new HashMap(contents.size() * 4 / 3);
        Iterator it = contents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Object o = entry.getKey();
            MyLong count = (MyLong) entry.getValue();
            answer.put(o, new Long(count.getValue()));
        }
        return answer;
    }
}

class MyLong {
    protected long value = 0L;

    public MyLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public boolean isPositive() {
        return value > 0L;
    }

    public void addOne() {
        value++;
    }

    public void subtractOne() {
        value--;
    }
}
