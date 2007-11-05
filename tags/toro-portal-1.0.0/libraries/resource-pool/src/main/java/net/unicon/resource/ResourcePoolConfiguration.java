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
package net.unicon.resource;

abstract class ResourcePoolConfiguration {
    protected String name;
    protected int size;
    protected int refreshSleepTime;
    protected int refreshPeriod;
    protected int sizeSleepTime;
    protected int timeoutSleepTime;
    protected int timeoutPeriod;
    protected int createLimit;
    protected int destroyLimit;
    protected int recycleLimit;
    protected int verifyLimit;
    protected int allocationWarnTime;
    protected int allocationWaitWarnTime;
    protected int creationWarnTime;
    protected int recycleWarnTime;
    protected int traceCallers;
    protected int logLevel;
    protected boolean useGuaranteedActions;
    protected boolean logMicroTimes;
    protected int numberOfRecycleThreads;

    abstract public String getDescriptor();

    public void setAllocationWaitWarnTime(int allocationWaitWarnTime) {
        this.allocationWaitWarnTime = allocationWaitWarnTime;
    }

    public void setAllocationWarnTime(int allocationWarnTime) {
        this.allocationWarnTime = allocationWarnTime;
    }

    public void setCreateLimit(int createLimit) {
        this.createLimit = createLimit;
    }

    public void setCreationWarnTime(int creationWarnTime) {
        this.creationWarnTime = creationWarnTime;
    }

    public void setDestroyLimit(int destroyLimit) {
        this.destroyLimit = destroyLimit;
    }

    public void setLogMicroTimes(boolean logMicroTimes) {
        this.logMicroTimes = logMicroTimes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumberOfRecycleThreads(int numberOfRecycleThreads) {
        this.numberOfRecycleThreads = numberOfRecycleThreads;
    }

    public void setRecycleLimit(int recycleLimit) {
        this.recycleLimit = recycleLimit;
    }

    public void setRecycleWarnTime(int recycleWarnTime) {
        this.recycleWarnTime = recycleWarnTime;
    }

    public void setRefreshPeriod(int refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }

    public void setRefreshSleepTime(int refreshSleepTime) {
        this.refreshSleepTime = refreshSleepTime;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSizeSleepTime(int sizeSleepTime) {
        this.sizeSleepTime = sizeSleepTime;
    }

    public void setTimeoutPeriod(int timeoutPeriod) {
        this.timeoutPeriod = timeoutPeriod;
    }

    public void setTimeoutSleepTime(int timeoutSleepTime) {
        this.timeoutSleepTime = timeoutSleepTime;
    }

    public void setTraceCallers(int traceCallers) {
        this.traceCallers = traceCallers;
    }

    public void setUseGuaranteedActions(boolean useGuaranteedActions) {
        this.useGuaranteedActions = useGuaranteedActions;
    }

    public void setVerifyLimit(int verifyLimit) {
        this.verifyLimit = verifyLimit;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getRefreshSleepTime() {
        return refreshSleepTime;
    }

    public int getRefreshPeriod() {
        return refreshPeriod;
    }

    public int getSizeSleepTime() {
        return sizeSleepTime;
    }

    public int getTimeoutSleepTime() {
        return timeoutSleepTime;
    }

    public int getTimeoutPeriod() {
        return timeoutPeriod;
    }

    public int getCreateLimit() {
        return createLimit;
    }

    public int getDestroyLimit() {
        return destroyLimit;
    }

    public int getRecycleLimit() {
        return recycleLimit;
    }

    public int getVerifyLimit() {
        return verifyLimit;
    }

    public int getAllocationWarnTime() {
        return allocationWarnTime;
    }

    public int getAllocationWaitWarnTime() {
        return allocationWaitWarnTime;
    }

    public int getCreationWarnTime() {
        return creationWarnTime;
    }

    public int getRecycleWarnTime() {
        return recycleWarnTime;
    }

    public int getTraceCallers() {
        return traceCallers;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public boolean useGuaranteedActions() {
        return useGuaranteedActions;
    }

    public boolean logMicroTimes() {
        return logMicroTimes;
    }

    public int getNumberOfRecycleThreads() {
        return numberOfRecycleThreads;
    }

    @Override
    public String toString() {
        return "\tname = " + name + "\n\t"
            + "size = " + size + "\n\t"
            + "refreshSleepTime = " + refreshSleepTime + "\n\t"
            + "refreshPeriod = " + refreshPeriod + "\n\t"
            + "sizeSleepTime = " + sizeSleepTime + "\n\t"
            + "timeoutSleepTime = " + timeoutSleepTime + "\n\t"
            + "timeoutPeriod = " + timeoutPeriod + "\n\t"
            + "createLimit = " + createLimit + "\n\t"
            + "destroyLimit = " + destroyLimit + "\n\t"
            + "recycleLimit = " + recycleLimit + "\n\t"
            + "verifyLimit = " + verifyLimit + "\n\t"
            + "allocationWarnTime = " + allocationWarnTime + "\n\t"
            + "allocationWaitWarnTime = " + allocationWaitWarnTime + "\n\t"
            + "creationWarnTime = " + creationWarnTime + "\n\t"
            + "recycleWarnTime = " + recycleWarnTime + "\n\t"
            + "traceCallers = " + traceCallers + "\n\t"
            + "logLevel = " + logLevel + "\n\t"
            + "useGuaranteedActions = " + useGuaranteedActions + "\n\t"
            + "logMicroTimes = " + logMicroTimes + "\n\t"
            + "numberOfRecycleThreads = " + numberOfRecycleThreads + "\n";
    }
}
