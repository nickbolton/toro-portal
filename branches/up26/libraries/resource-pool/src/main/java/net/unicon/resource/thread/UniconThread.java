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

abstract public class UniconThread extends Thread {
    protected boolean continueRunning = true;
    protected long startTime;
    protected long sleepTime = -1;
    protected long endTime = -1;

    public UniconThread() {
        super();
        setDaemon(true);
    }

    public UniconThread(String name) {
        super(name);
        setDaemon(true);
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void end() {
        continueRunning = false;
        interrupt();
    }

    public void kill() {
        end();
        if (isAlive()) {
            stop();
        }
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setSleepTimeFromNow(long sleepTime) {
        this.sleepTime = sleepTime;
        this.endTime = sleepTime + System.currentTimeMillis();
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    protected void guaranteedSleep() {
        if (endTime > 0) {
            guaranteedSleepUntil(endTime);
        } else {
            guaranteedSleepFor(sleepTime);
        }
    }

    protected void guaranteedSleepFor(long sleepTimeArg) {
        try {
            long currentTime = System.currentTimeMillis();
            long targetTime = currentTime + sleepTimeArg;
            while (continueRunning && targetTime >= currentTime) {
                try {
                    Thread.sleep(targetTime - currentTime);
                } catch (InterruptedException e) {
                    if (!continueRunning) {
                        return;
                    }
                }

                currentTime = System.currentTimeMillis();
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
    }

    protected void guaranteedSleepUntil(long targetTime) {
        try {
            long currentTime = System.currentTimeMillis();
            while (continueRunning && targetTime >= currentTime) {
                try {
                    Thread.sleep(targetTime - currentTime);
                } catch (InterruptedException e) {
                    if (!continueRunning) {
                        return;
                    }
                }

                currentTime = System.currentTimeMillis();
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
    }
}
