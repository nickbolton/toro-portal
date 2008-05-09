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

public class FatThreadTest {
    public static void main(String[] args) throws Exception {
        int threadCount = 0;
        int maximumDepth = 10;

        if (args.length > 0) {
            maximumDepth = Integer.parseInt(args[0]);
        }

        try {
            while (true) {
                Thread t = new FatThread(maximumDepth);
                t.setDaemon(true);
                t.start();
                threadCount++;
                if ((threadCount % 100) == 0) {
                    System.out.println("Achieved thread count of " + threadCount);
                }
            }
        } catch (Throwable t) {
            System.out.println("maximum thread count = " + threadCount + "; error thrown was: ");
            t.printStackTrace(System.out);

            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
    }
}

class FatThread extends Thread {
    protected int maximumDepth;
    protected boolean continueRunning = true;
    protected long startTime;
    protected long sleepTime = -1;
    protected long endTime = -1;

    public FatThread(int maximumDepth) {
        super();
        this.maximumDepth = maximumDepth;
    }

    @Override
    public void run() {
        fatMethod("jojo", 1);
    }

    protected void fatMethod(String s1, int depth) {
        if (depth >= maximumDepth) {
            while (true) {
                try {
                    Thread.sleep(10000);
                } catch (Throwable t) {
                    if (t instanceof ThreadDeath) {
                        throw (ThreadDeath) t;
                    }
                }
            }
        } else {
            fatMethod(s1, depth + 1);
        }
    }
}
