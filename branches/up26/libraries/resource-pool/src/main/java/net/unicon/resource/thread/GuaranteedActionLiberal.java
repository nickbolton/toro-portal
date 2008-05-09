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

public class GuaranteedActionLiberal extends GuaranteedActionImpl {
    protected UniconThread actionThread;
    protected UniconThread timeLimitThread;
    protected boolean killActionThread = false;
    protected long actionSpawnTime;
    protected long actionStartTime;
    protected long timeLimitSpawnTime;
    protected long timeLimitStartTime;

    public GuaranteedActionLiberal(String name, long timeLimit, SimpleBlock action) {
        this(name, timeLimit, action, false);
    }

    public GuaranteedActionLiberal(String name, long timeLimit, SimpleBlock action, boolean killActionThread) {
        super(name, timeLimit, action);
        actionThread = createActionThread(action);
        timeLimitThread = createTimeLimitThread(timeLimit);
        this.killActionThread = killActionThread;
    }

    @Override
    public String toString() {
        return "GuaranteedActionLiberal " + number;
    }
    public void killActionThread() {
        killActionThread = true;
    }

    @Override
    protected void terminateForError() {
        timeLimitThread.end();
    }

    @Override
    protected void terminateForFinished() {
        timeLimitThread.end();
    }

    @Override
    protected void terminateForInterrupt() {
        if (killActionThread) {
            actionThread.kill();
        }
        timeLimitThread.end();
    }

    @Override
    protected void terminateForTimeout() {
        if (killActionThread) {
            actionThread.kill();
        }
    }

    @Override
    protected void startThreads() {
        actionThread.setStartTime(runStartTime);
        timeLimitThread.setStartTime(runStartTime);
        actionSpawnTime = actionStartTime = System.currentTimeMillis();
        actionThread.start();
        timeLimitSpawnTime = timeLimitStartTime = System.currentTimeMillis();
        timeLimitThread.start();
    }

    protected UniconThread createActionThread(final SimpleBlock action) {
        return new UniconThread("Action " + number) {
                @Override
                public void run() {
                    try {
                        Object result = action.evaluate();
                        finished(result);
                    } catch (Throwable t) {
                        if (t instanceof ThreadDeath) {
                            throw (ThreadDeath) t;
                        }

                        error(t);
                    }
                }};
    }

    protected UniconThread createTimeLimitThread(long sleepTime) {
        UniconThread answer = new UniconThread("TimeLimit " + number) {
                @Override
                public void run() {
                    guaranteedSleep();
                    if (continueRunning) {
                        timeout();
                    }
                }};
        answer.setSleepTime(sleepTime);
        return answer;
    }
}

