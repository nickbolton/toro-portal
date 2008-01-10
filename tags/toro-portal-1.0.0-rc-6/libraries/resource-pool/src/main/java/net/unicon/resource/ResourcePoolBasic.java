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

import net.unicon.resource.smalltalk.SimpleBlock;
import net.unicon.resource.thread.*;
import net.unicon.resource.util.ErrorUtils;
import net.unicon.resource.util.ThreadUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResourcePoolBasic implements ResourcePool {
    protected final Log commonsLog = LogFactory.getLog(getClass());
    protected ResourcePoolConfiguration config;
    protected ResourceFactory resourceFactory;
    protected ResourceLog log;
    protected int allocatedSize;
    protected boolean useGuaranteedActions;
    protected boolean logMicroTimes;
    protected SynchronizedQueue allocatedResources;
    protected SynchronizedQueue createResources;
    protected SynchronizedQueue deadResources;
    protected SynchronizedQueue freeResources;
    protected SynchronizedQueue recycleResources;
    protected QueueThread createThread;
    protected QueueThread destroyThread;
    protected Set recycleThreads = new HashSet();
    protected QueueThread refreshThread;
    protected UniconThread sizeThread;
    protected QueueThread timeoutThread;
    protected final GuaranteedActionSupport guaranteedActionSupport = new GuaranteedActionSupport();

    public ResourcePoolBasic(ResourcePoolConfiguration config, ResourceFactory resourceFactory) {
        this.config = config;
        this.resourceFactory = resourceFactory;
        this.log = new ResourceLog(config.logLevel);
        allocatedSize = config.getSize();
        useGuaranteedActions = config.useGuaranteedActions();
        logMicroTimes = config.logMicroTimes();

        initializeResources(allocatedSize);
        initializeThreads();
    }

    public String getName() {
        return config.getName();
    }

    public String getDescriptor() {
        return config.getDescriptor();
    }

    public ResourcePoolConfiguration getConfig() {
        return config;
    }

    public int size() {
        return allocatedResources.size() + freeResources.size() + createResources.size() + recycleResources.size();
    }

    public int allocatedSize() {
        return allocatedSize;
    }

    public int usedSize() {
        return allocatedResources.size();
    }

    public int freeSize() {
        return freeResources.size();
    }

    public void reportStats() {
    }

    public void error(Object o, Throwable t) {
        log.error(this, o, t);
    }

    public Resource allocate(String requester) {
        long microTime = System.currentTimeMillis();
        GuaranteedAction action = null;
        boolean removed = false;
        Resource answer = null;

        try {
            log.log(5, this, "allocate", "begin", requester);
            microTime = microtime("ALLOCATE", "STEP a", microTime, requester);
            long startTime = System.currentTimeMillis();
            microTime = microtime("ALLOCATE", "STEP b", microTime, requester);
            final Resource resource = (Resource) freeResources.next();
            microTime = microtime("ALLOCATE", "STEP c", microTime, requester + " for " + resource);
            if (resource != null) {
                removed = true;
                answer = resource;
            } else {
                throw new ResourceUnavailableException("No resources available at this time");
            }
            microTime = microtime("ALLOCATE", "STEP d", microTime, requester + " for " + resource);

            log.time(5, this, startTime, "allocate", "wait", requester + " --> " + resource.shortString(), config.getAllocationWaitWarnTime());
            microTime = microtime("ALLOCATE", "STEP e", microTime, requester + " for " + resource);

            if (!useGuaranteedActions) {
                answer.assign(requester);
                microTime = microtime("ALLOCATE", "STEP f", microTime, requester + " for " + resource);
                allocatedResources.add(answer);
                microTime = microtime("ALLOCATE", "STEP g", microTime, requester + " for " + resource);
                removed = false;
            } else {
                SimpleBlock block = new SimpleBlock() {
                        public Object evaluate() throws Exception {
                            if (!resource.verify()) {
                                throw new java.sql.SQLException("Resource is no longer valid.");
                            }
                            return null;
                        }};
                microTime = microtime("ALLOCATE", "STEP h", microTime, requester + " for " + resource);
                action = new GuaranteedActionStrict("verify " + resource, config.getVerifyLimit(), block, false);
                microTime = microtime("ALLOCATE", "STEP i", microTime, requester + " for " + resource);
                action.run();
                microTime = microtime("ALLOCATE", "STEP j", microTime, requester + " for " + resource);
                if (action.isSuccessful()) {
                    answer.assign(requester);
                    microTime = microtime("ALLOCATE", "STEP k", microTime, requester + " for " + resource);
                    allocatedResources.add(answer);
                    microTime = microtime("ALLOCATE", "STEP l", microTime, requester + " for " + resource);
                    removed = false;
                }

                guaranteedActionSupport.log(action, "allocate", answer);
                microTime = microtime("ALLOCATE", "STEP m", microTime, requester + " for " + resource);
            }

            log.time(2, this, startTime, "allocate", "end", answer, config.getAllocationWarnTime());
            log.time(2, this, startTime, "allocate", "bonus", asMessage(answer));
            microTime = microtime("ALLOCATE", "STEP n", microTime, requester + " for " + resource);
        } catch (Throwable t) {
            log.error(this, t);
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            } else if (t instanceof ResourceUnavailableException) {
                throw (ResourceUnavailableException) t;
            }
            ErrorUtils.swallow(t);
        } finally {
            if (removed) {
                createResources.add(answer);
                microTime = microtime("ALLOCATE", "STEP o", microTime, requester + " for " + answer);
                log.log(8, this, "allocate", "finally", asMessage(answer));
                microTime = microtime("ALLOCATE", "STEP p", microTime, requester + " for " + answer);
            }
        }

        microTime = microtime("ALLOCATE", "STEP q", microTime, requester + " for " + answer);
        return removed ? allocate(requester) : answer;
    }

    protected String asMessage(Object answer) {
        String message = "";
        if (config.getTraceCallers() > 0) {
            StringBuffer messageBuffer = new StringBuffer(answer.toString());
            messageBuffer.append("\n\tCalled by:\n");

            List trace = ThreadUtil.getSenders(1, config.getTraceCallers());
            for (int i = 0; i < trace.size(); i++) {
                messageBuffer.append("\t\t");
                messageBuffer.append(trace.get(i));
                messageBuffer.append("\n");
            }
            message = messageBuffer.toString();
        } else {
            message = answer.toString();
        }

        return message;
    }

    public void release(Resource resource) {
        long microTime = System.currentTimeMillis();
        log.time(2, this, resource.getAssignmentTime(), "release", "", resource);
        microTime = microtime("RELEASE", "STEP a", microTime, resource);
        resource.release();
        microTime = microtime("RELEASE", "STEP b", microTime, resource);
        move("release",resource, allocatedResources, recycleResources, true);
        microTime = microtime("RELEASE", "STEP c", microTime, resource);
    }

    public List getAllocationInfo() {
        List queueContents = allocatedResources.contents();
        List answer = new ArrayList(queueContents.size());

        for (int i = 0; i < queueContents.size(); i++) {
            List row = new ArrayList(6);
            Resource resource = (Resource) queueContents.get(i);
            row.add(resource.getRawResourceID());
            row.add(resource.getRequester());
            row.add(new Long(resource.getAssignmentTime()));
            row.add(new Long(resource.getLastUseTime()));
            answer.add(row);
        }

        return answer;
    }

    protected Throwable guaranteedCreate(Resource previous) {
        long microTime = System.currentTimeMillis();
        Throwable answer = null;
        boolean removed = true;
        GuaranteedAction action = null;

        try {
            SimpleBlock block = new SimpleBlock() {
                    public Object evaluate() throws Exception {
                        return resourceFactory.createResource(ResourcePoolBasic.this, "ResourcePool");
                    }};

            microTime = microtime("CREATE", "STEP a", microTime, previous);
            log.log(5, this, "create", "begin", previous);
            microTime = microtime("CREATE", "STEP b", microTime, previous);
            long startTime = System.currentTimeMillis();
            microTime = microtime("CREATE", "STEP c", microTime, previous);

            Object newResource = null;

            if (!useGuaranteedActions) {
                newResource = resourceFactory.createResource(ResourcePoolBasic.this, "ResourcePool");
                microTime = microtime("CREATE", "STEP d", microTime, previous);
                freeResources.add(newResource);
                microTime = microtime("CREATE", "STEP e", microTime, previous);
                deadResources.add(previous);
                microTime = microtime("CREATE", "STEP f", microTime, previous);
                removed = false;
            } else {
                action = new GuaranteedActionLiberal("create " + previous, config.getCreateLimit(), block, true);
                microTime = microtime("CREATE", "STEP g", microTime, previous);
                action.run();
                microTime = microtime("CREATE", "STEP h", microTime, previous);
                if (action.isSuccessful()) {
                    newResource = action.getReturnValue();
                    microTime = microtime("CREATE", "STEP i", microTime, previous);
                    freeResources.add(action.getReturnValue());
                    microTime = microtime("CREATE", "STEP j", microTime, previous);
                    deadResources.add(previous);
                    microTime = microtime("CREATE", "STEP k", microTime, previous);
                    removed = false;
                }

                guaranteedActionSupport.log(action, "create", (Resource) action.getReturnValue());
                microTime = microtime("CREATE", "STEP l", microTime, previous);
            }

            log.time(2, this, startTime, "create", "end", newResource, config.getCreationWarnTime());
            microTime = microtime("CREATE", "STEP m", microTime, previous);
        } catch (Throwable t) {
            answer = t;
            log.error(this, t);
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            ErrorUtils.swallow(t);
        } finally {
            if (removed) {
                if (action.erroredOut() && action.getThrowable() instanceof NoClassDefFoundError) {
                    log.log(0, this, "create", "ALERT", "Aborting creation of new resource because of " + guaranteedActionSupport.describe(action));
                    microTime = microtime("CREATE", "STEP n", microTime, previous);
                } else {
                    createResources.add(previous);
                    microTime = microtime("CREATE", "STEP o", microTime, previous);
                    log.log(8, this, "create", "finally", previous);
                    log.log(0, this, "create", "ALERT", "Unable to create new resource because of " + guaranteedActionSupport.describe(action));
                    microTime = microtime("CREATE", "STEP p", microTime, previous);
                }
            }
        }

        microTime = microtime("CREATE", "STEP q", microTime, previous);
        return answer;
    }

    protected Throwable guaranteedDestroy(final Resource resource) {
        long microTime = System.currentTimeMillis();
        SimpleBlock block = new SimpleBlock() {
                public Object evaluate() throws Exception {
                    resource.destroy();
                    return null;
                }};
        microTime = microtime("DESTROY", "STEP a", microTime, resource);
        log.log(5, this, "destroy", "begin", resource);
        microTime = microtime("DESTROY", "STEP b", microTime, resource);
        long startTime = System.currentTimeMillis();
        microTime = microtime("DESTROY", "STEP c", microTime, resource);
        if (!useGuaranteedActions) {
            try {
                resource.destroy();
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                }
            }
            microTime = microtime("DESTROY", "STEP d", microTime, resource);
        } else {
            GuaranteedAction action = new GuaranteedActionLiberal("destroy " + resource, config.getDestroyLimit(), block, true);
            microTime = microtime("DESTROY", "STEP e", microTime, resource);
            action.run();
            microTime = microtime("DESTROY", "STEP f", microTime, resource);
            guaranteedActionSupport.log(action, "destroy", resource);
            microTime = microtime("DESTROY", "STEP g", microTime, resource);
        }
        log.time(5, this, startTime, "destroy", "end", resource);

        microTime = microtime("DESTROY", "STEP h", microTime, resource);
        return null;
    }

    protected Throwable guaranteedRecycle(final Resource resource) {
        long microTime = System.currentTimeMillis();
        Throwable answer = null;
        boolean removed = true;

        try {
            microTime = microtime("RECYCLE", "STEP a", microTime, resource);
            SimpleBlock block = new SimpleBlock() {
                    public Object evaluate() throws Exception {
                        resource.recycle();
            if (!resource.verify()) {
                throw new java.sql.SQLException("Resource is no longer valid.");
            }
                        return null;
                    }};

            microTime = microtime("RECYCLE", "STEP b", microTime, resource);
            log.log(5, this, "recycle", "begin", resource);
            microTime = microtime("RECYCLE", "STEP c", microTime, resource);
            long startTime = System.currentTimeMillis();
            microTime = microtime("RECYCLE", "STEP d", microTime, resource);

            if (!useGuaranteedActions) {
                microTime = microtime("RECYCLE", "STEP e", microTime, resource);
                resource.recycle();
                resource.verify();
                microTime = microtime("RECYCLE", "STEP f", microTime, resource);
                freeResources.add(resource);
                microTime = microtime("RECYCLE", "STEP g", microTime, resource);
                removed = false;
            } else {
                microTime = microtime("RECYCLE", "STEP h", microTime, resource);
                GuaranteedAction action = new GuaranteedActionStrict("recycle " + resource, config.getRecycleLimit(), block, true);
                microTime = microtime("RECYCLE", "STEP i", microTime, resource);
                action.run();
                microTime = microtime("RECYCLE", "STEP j", microTime, resource);
                if (action.isSuccessful()) {
                    microTime = microtime("RECYCLE", "STEP k", microTime, resource);
                    freeResources.add(resource);
                    microTime = microtime("RECYCLE", "STEP l", microTime, resource);
                    removed = false;
                }

               guaranteedActionSupport.log(action, "recycle", resource);
                microTime = microtime("RECYCLE", "STEP m", microTime, resource);
            }
            log.time(5, this, startTime, "recycle", "end", resource, config.getRecycleWarnTime());
            microTime = microtime("RECYCLE", "STEP n", microTime, resource);
        } catch (Throwable t) {
            answer = t;
            log.error(this, t);
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            ErrorUtils.swallow(t);
        } finally {
            if (removed) {
                createResources.add(resource);
                log.log(8, this, "recycle", "finally", resource);
                microTime = microtime("RECYCLE", "STEP o", microTime, resource);
            }
        }

        microTime = microtime("RECYCLE", "STEP p", microTime, resource);
        return answer;
    }

    protected long microtime(String action, String phase, long startTime, Object resource) {
        if (commonsLog.isTraceEnabled()) {
            log.time(0, this, startTime, "MICRO " + action, phase, resource);
            return System.currentTimeMillis();
        }

        return 0L;
    }

    protected Throwable guaranteedRefresh(Resource resource) {
        long microTime = System.currentTimeMillis();
        log.log(8, this, "refresh", "", resource);
        microTime = microtime("REFRESH", "STEP a", microTime, resource);
        resource.unassign();
        microTime = microtime("REFRESH", "STEP b", microTime, resource);
        Throwable answer = move("refresh", resource, freeResources, recycleResources, false);
        microTime = microtime("REFRESH", "STEP c", microTime, resource);
        return answer;
    }

    protected Throwable guaranteedTimeout(Resource resource) {
        long microTime = System.currentTimeMillis();
        log.log(2, this, "timeout", "", resource);
        microTime = microtime("TIMEOUT", "STEP a", microTime, resource);
        log.log(0, this, "resource timeout", "ALERT", resource);
        microTime = microtime("TIMEOUT", "STEP b", microTime, resource);
        resource.timeout();
        microTime = microtime("TIMEOUT", "STEP c", microTime, resource);
        Throwable answer = move("timeout", resource, allocatedResources, recycleResources, true);
        microTime = microtime("TIMEOUT", "STEP d", microTime, resource);
        return answer;
    }

    protected Throwable move(String title, Resource resource, SynchronizedQueue source, SynchronizedQueue destination, boolean recreateResource) {
        long microTime = System.currentTimeMillis();
        boolean removed = false;
        Throwable answer = null;
        Resource newResource = resource;

        try {
            if (source.remove(resource)) {
                microTime = microtime("MOVE " + title, "STEP a", microTime, newResource);
                removed = true;
                if (recreateResource) {
                    newResource = resourceFactory.recreateResource(this, resource);
                    resource.clear();
                }
                destination.add(newResource);
                microTime = microtime("MOVE " + title, "STEP b", microTime, newResource);
                removed = false;
            }
            microTime = microtime("MOVE " + title, "STEP c", microTime, newResource);
        } catch (Throwable t) {
            log.error(this, t);
            answer = t;
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            ErrorUtils.swallow(t);
        } finally {
            if (removed) {
                destination.add(newResource);
                microTime = microtime("MOVE " + title, "STEP d", microTime, newResource);
                log.log(8, this, title, "finally", newResource);
                microTime = microtime("MOVE " + title, "STEP e", microTime, newResource);
            }
        }

        microTime = microtime("MOVE " + title, "STEP f", microTime, newResource);
        return answer;
    }

    protected void initializeThreads() {
        useCreateThread();
        useDestroyThread();
        useRecycleThread();
        useRefreshThread();
        useTimeoutThread();

        if (log.isEnabled(0)) {
            useSizeThread();
        }
    }

    protected void useCreateThread() {
        if (createThread != null) {
            createThread.end();
        }

        createThread = new FeederThread(createResources) {
                protected void doWork(Object object) throws Exception {
                    guaranteedCreate((Resource) object);
                }};

        createThread.start();
    }

    protected void useDestroyThread() {
        if (destroyThread != null) {
            destroyThread.end();
        }

        destroyThread = new FeederThread(deadResources) {
                protected void doWork(Object object) throws Exception {
                    Resource resource = (Resource) object;
                    if (resource.getRawResource() != null) {
                        guaranteedDestroy(resource);
                        resource.setRawResource(null);
                    }
                }};

        destroyThread.start();
    }

    protected void useRecycleThread() {
        for (int i = 1; i <= config.getNumberOfRecycleThreads(); i++) {
            UniconThread recycleThread = new FeederThread(recycleResources, createResources) {
                    protected void doWork(Object object) throws Exception {
                        guaranteedRecycle((Resource) object);
                    }};

            recycleThread.start();
            recycleThreads.add(recycleThread);
        }
    }

    protected void useRefreshThread() {
        if (refreshThread != null) {
            refreshThread.end();
        }

        refreshThread = new TimerThread(config.getRefreshSleepTime(), freeResources) {
                protected void doWork(Object object) throws Exception {
                    Resource resource = (Resource) object;
                    long currentTime = System.currentTimeMillis();
                    long refreshTime = resource.getLastUseTime() + config.getRefreshPeriod();
                    if (currentTime > refreshTime) {
                        guaranteedRefresh((Resource) object);
                    }
                }};

        refreshThread.start();
    }

    protected void useSizeThread() {
        if (sizeThread != null) {
            sizeThread.end();
        }

        sizeThread = new UniconThread() {
                public void run() {
                    while (continueRunning) {
                        guaranteedSleep();
                        if (continueRunning) {
                            log.log(0, ResourcePoolBasic.this, "size", null, "ALLOCATED = " + usedSize());
                            log.log(0, ResourcePoolBasic.this, "size", null, "FREE = " + freeSize());
                            log.log(0, ResourcePoolBasic.this, "size", null, "RECYCLE = " + recycleResources.size());
                            log.log(0, ResourcePoolBasic.this, "size", null, "CREATE = " + createResources.size());
                            log.log(0, ResourcePoolBasic.this, "size", null, "SIZE = " + size());
                            log.log(0, ResourcePoolBasic.this, "size", null, "CONNECTIONS = " + ConnectionFactory.getConnectionCount());
                            try {
                                StringWriter sw = new StringWriter();
                                ThreadReporter.getInstance().report(new PrintWriter(sw));
                                log.log(0, ResourcePoolBasic.this, "size", null, "\n" + sw.toString());
                            } catch (Throwable t) {
                                if (t instanceof ThreadDeath) {
                                    throw (ThreadDeath) t;
                                }
                            }
                        }
                    }
                }};

        sizeThread.setSleepTime(config.getSizeSleepTime());

        sizeThread.start();
    }

    protected void useTimeoutThread() {
        if (timeoutThread != null) {
            timeoutThread.end();
        }

        timeoutThread = new TimerThread(config.getTimeoutSleepTime(), allocatedResources) {
                protected void doWork(Object object) throws Exception {
                    Resource resource = (Resource) object;
                    long currentTime = System.currentTimeMillis();
                    long timeoutTime = resource.getAssignmentTime() + config.getTimeoutPeriod();
                    if (currentTime > timeoutTime) {
                        guaranteedTimeout((Resource) object);
                    }
                }};

        timeoutThread.start();
    }

    protected void initializeResources(int size) {
        allocatedResources = new SynchronizedQueue(size * 2);
        createResources = new SynchronizedQueue(size * 2);
        deadResources = new SynchronizedQueue(size * 2);
        freeResources = new SynchronizedQueue(size * 2);
        recycleResources = new SynchronizedQueue(size * 2);

        for (int i = 1; i <= size; i++) {
            freeResources.add(resourceFactory.createResource(this, "ResourcePool"));
        }
    }




}
