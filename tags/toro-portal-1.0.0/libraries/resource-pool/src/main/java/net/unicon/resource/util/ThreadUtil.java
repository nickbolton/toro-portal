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
package net.unicon.resource.util;

import java.util.*;

public class ThreadUtil {
    public static void sleep(long millisecondsToSleep) throws InterruptedException {
        Thread.sleep(millisecondsToSleep);
    }

    public static void safeSleep(long millisecondsToSleep) {
        try {
            ThreadUtil.sleep(millisecondsToSleep);
        } catch (InterruptedException e) {
        }
    }

    public static void guaranteedSleep(long millisecondsToSleep) {
        long wakeupTime = System.currentTimeMillis() + millisecondsToSleep;;
        while (wakeupTime > System.currentTimeMillis()) {
            ThreadUtil.safeSleep(wakeupTime - System.currentTimeMillis());
        }
    }

    public static String getSenderString() {
        StackTraceElement element = getSender(1);
        return (element == null) ? null : element.toString();
    }

    public static List getSenders(int ancestorDepth, int senderCount) {
        List answer = new ArrayList(senderCount * 3 / 2);
        StackTraceElement[] stack = getStack();
        int startFrame = 3 + ancestorDepth;
        int endFrame = Math.min(stack.length, startFrame + senderCount);
        for (int i = startFrame; i < endFrame; i++) {
            answer.add(stack[i]);
        }
        return answer;
    }

    public static StackTraceElement getSender(int ancestorDepth) {
        StackTraceElement[] stack = getStack();
        if (stack == null || stack.length < (3 + ancestorDepth)) {
            return null;
        }

        return stack[3 + ancestorDepth];
    }

    public static StackTraceElement[] getStack() {
        Throwable throwable = null;
        try {
            throw new Error("Generate Stack Trace");
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }

            throwable = t;
        }

        return (throwable == null) ? null : throwable.getStackTrace();
    }
}
