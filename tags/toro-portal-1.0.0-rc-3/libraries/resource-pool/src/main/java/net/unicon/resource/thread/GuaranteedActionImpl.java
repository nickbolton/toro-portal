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

import net.unicon.resource.smalltalk.SimpleBlock;

abstract public class GuaranteedActionImpl implements GuaranteedAction {
    protected String name;
    protected long number;
    protected Object returnValue;
    protected boolean successful = false;
    protected Throwable throwable;
    protected boolean interrupted = false;
    protected boolean timedOut = false;
    protected boolean resolved = false;
    protected long timeLimit;
    protected long creationTime;
    protected long runStartTime;
    protected long resolutionStartTime;
    protected long resolutionEndTime;
    protected long endTime;

    public GuaranteedActionImpl(String name, long timeLimit, SimpleBlock action) {
        creationTime = System.currentTimeMillis();
        this.name = name;
        number = GuaranteedActionImpl.nextNumber();
        this.timeLimit = timeLimit;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean erroredOut() {
        return throwable != null;
    }

    public boolean interrupted() {
        return interrupted;
    }

    public boolean timedOut() {
        return timedOut;
    }

    @Override
    public String toString() {
        return "GuaranteedAction " + number;
    }

    public synchronized void run() {
        runStartTime = System.currentTimeMillis();
        startThreads();
        try {
            wait();
        } catch (InterruptedException e) {
            interrupt();
        } catch (Throwable t) {
            error(t);
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
        }
        endTime = System.currentTimeMillis();
    }

    protected synchronized void interrupt() {
        if (!resolved) {
            resolutionStartTime = System.currentTimeMillis();
            resolved = true;
            interrupted = true;
            timedOut = false;
            successful = false;
            terminateForInterrupt();
            resolutionEndTime = System.currentTimeMillis();
            notify();
        }
    }

    protected synchronized void timeout() {
        if (!resolved) {
            resolutionStartTime = System.currentTimeMillis();
            resolved = true;
            interrupted = false;
            timedOut = true;
            successful = false;
            terminateForTimeout();
            resolutionEndTime = System.currentTimeMillis();
            notify();
        }
    }

    public synchronized void error(Throwable t) {
        if (!resolved) {
            resolutionStartTime = System.currentTimeMillis();
            resolved = true;
            throwable = t;
            interrupted = false;
            timedOut = false;
            successful = false;
            terminateForError();
            resolutionEndTime = System.currentTimeMillis();
            notify();
        }
    }

    public synchronized void finished(Object o) {
        if (!resolved) {
            resolutionStartTime = System.currentTimeMillis();
            resolved = true;
            returnValue = o;
            interrupted = false;
            timedOut = false;
            successful = true;
            terminateForFinished();
            resolutionEndTime = System.currentTimeMillis();
            notify();
        }
    }

    abstract protected void startThreads();
    abstract protected void terminateForError();
    abstract protected void terminateForFinished();
    abstract protected void terminateForInterrupt();
    abstract protected void terminateForTimeout();

    protected static long actionCount = 1;

    protected synchronized static long nextNumber() {
        return actionCount++;
    }
}

