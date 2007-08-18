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
package net.unicon.portal.groups.framework;

import jargs.gnu.CmdLineParser;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.ICompositeGroupService;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;

public final class GroupsTester implements Runnable {

    private static final Log log = LogFactory.getLog(GroupsTester.class);

    Stats getContainingGroupsStats = new Stats("getContainingGroups");
    Stats getAllContainingGroupsStats = new Stats("getAllContainingGroups");
    Stats containsStats = new Stats("contains");
    Stats deepContainsStats = new Stats("deepContains");
    Stats isMemberOfStats = new Stats("isMemberOf");
    Stats isDeepMemberOfStats = new Stats("isDeepMemberOf");
    Stats getMembersStats = new Stats("getMembers");
    Stats getAllMembersStats = new Stats("getAllMembers");
    Stats traverseTreeStats = new Stats("traverseTree");
    Stats findMembersStats = new Stats("findMembers");
    Stats findContainingGroupsStats = new Stats("findContainingGroups");
    Stats findGroupStats = new Stats("findGroup");
    Stats findGroupWithLockStats = new Stats("findGroupWithLock");
    Stats getEntityStats = new Stats("getEntity");
    Stats searchForEntitiesStats = new Stats("searchForEntities");
    Stats searchForGroupsStats = new Stats("searchForGroups");
    Stats getAllEntitiesStats = new Stats("getAllEntities");
    Stats getEntitiesStats = new Stats("getEntities");
    Stats getMemberGroupNamedStats = new Stats("getMemberGroupNamed");
    Stats hasMembersStats = new Stats("hasMembers");
    
    /*
    Stats addMemberStats = new Stats("addMember");
    Stats removeMemberStats = new Stats("removeMember");
    Stats deleteStats = new Stats("delete");
    Stats getCreatorIDStats = new Stats("getCreatorID");
    Stats getDescriptionStats = new Stats("getDescription");
    Stats getLocalKeyStats = new Stats("getLocalKey");
    Stats getNameStats = new Stats("getName");
    Stats getServiceNameStats = new Stats("getServiceName");
    Stats isEditableStats = new Stats("isEditable");
    Stats setCreatorIDStats = new Stats("setCreatorID");
    Stats setDescriptionStats = new Stats("setDescription");
    Stats setNameStats = new Stats("setName");
    Stats updateStats = new Stats("update");
    Stats updateMembersStats = new Stats("updateMembers");
    */
    
    

    Stats[] allStats = {
    	getContainingGroupsStats, getAllContainingGroupsStats,
        containsStats, deepContainsStats, isMemberOfStats, isDeepMemberOfStats,
        getMembersStats, getAllMembersStats, traverseTreeStats,
		findMembersStats, findContainingGroupsStats,
		findGroupStats, findGroupWithLockStats, getEntityStats,
		searchForEntitiesStats, searchForGroupsStats, getAllEntitiesStats,
		getEntitiesStats, getMemberGroupNamedStats, hasMembersStats,

		/*
		addMemberStats, removeMemberStats, deleteStats,
        getCreatorIDStats, getDescriptionStats, getLocalKeyStats,
        getNameStats, getServiceNameStats, isEditableStats, setCreatorIDStats,
        setDescriptionStats, setNameStats, updateStats, updateMembersStats
        */
    };

    private int count=0;
    private int iteration=0;
    private boolean finished = false;
    private boolean doTreeTraversals = false;
    private boolean functional = false;
    private static NumberFormat nf = null;

    private int numIterations=0;
    private IEntityGroup[] theGroups;
    private IGroupMember[] theEntities;
    private IIndividualGroupService groupService = null;
    private Class entityType = null;

    public int getCount() { return count; }
    public boolean isFinished() { return finished; }

    public Stats[] getStats() { return allStats; }

    StringBuffer resultValue = new StringBuffer();

    Set results = null;
    
    IGroupMember[] allGroupMembers = null;

    private void traverseTree(IGroupMember member, String topLevelKey) throws Exception {

        long t;

        if (results != null) {
            addResult(new StringBuffer("traverseTree:").append(topLevelKey).
                      append(':').append(member.getKey()).toString());
        }

        if (member instanceof IEntityGroup) {
            IEntityGroup group = (IEntityGroup)member;

            t = System.currentTimeMillis();
            Iterator itr = group.getMembers();
            getMembersStats.addValue(System.currentTimeMillis() - t);

            while (itr.hasNext()) {
                traverseTree((IGroupMember)itr.next(), topLevelKey);
            }
        }
    }
    
    public GroupsTester(Vector groupKeys, Vector principalKeys,
        String principalType, int numIterations, boolean doTreeTraversals,
        boolean keepResults, boolean functional) throws Exception {
        this.numIterations = numIterations;
        this.doTreeTraversals = doTreeTraversals;
        this.functional = functional;
        
        this.theGroups = new IEntityGroup[groupKeys.size()];
        for (int i=0; i<groupKeys.size(); i++) {
            this.theGroups[i] = GroupService.findGroup((String)groupKeys.get(i));
        }
        
        this.theEntities = new IGroupMember[principalKeys.size()];
        for (int i=0; i<principalKeys.size(); i++) {
            this.theEntities[i] = AuthorizationService.instance().getGroupMember(
                AuthorizationService.instance().newPrincipal((String)principalKeys.get(i), Class.forName(principalType))
            );
        }
        
        if (theEntities.length > 0) {
            entityType = theEntities[0].getEntityType();
        }

        if (keepResults) {
            results = new TreeSet();
        }
        
        ICompositeGroupService compositeService = GroupService.getCompositeGroupService();
        List keys =  new ArrayList(compositeService.getComponentServices().keySet());
        groupService = (IIndividualGroupService)compositeService.getComponentServices().get(keys.get(0));
    }

    private void addResult(String result) {
        if (results != null) {
            results.add(result);
        }
    }

    private void addResults(Iterator itr, String label) {
    	if (itr == null) {
    		addResult(new StringBuffer(label).append("null").toString());
    	} else {
    		while (itr != null && itr.hasNext()) {
    			addResult(new StringBuffer(label).append(((IGroupMember)itr.next()).getKey()).toString());
    		}
        }
    }

    public Set getResults() {
        if (results == null) return new TreeSet();
        return results;
    }

    public int getResultsSize() {
        if (results == null) return 0;
        return results.size();
    }

    public String getResultsChecksum() throws Exception {
        if (results == null) return null;

        MessageDigest md = MessageDigest.getInstance("MD5");
        Iterator itr = results.iterator();
        while (itr.hasNext()) {
            md.update(((String)itr.next()).getBytes());
        }
        byte[] digest = md.digest();
        StringBuffer sb = new StringBuffer(32);
        for (int i=0; i<digest.length; i++) {
            if (digest[i] < 0x10 && digest[i] >= 0x0) {
                // add a leading 0
                sb.append("0");
            }
            sb.append(Integer.toHexString((0xff & digest[i])));
        }
        return sb.toString();
    }
    
    public void run() {
        try {
            long t;

            count=0;
            boolean result;
            Iterator resultsItr;
            IGroupMember gmResult;
            
            IEntityGroup group;
            IGroupMember entity;
            String sResult;
            
            for (int i=0; i<numIterations; i++) {
                for (int g=0; g<theGroups.length; g++) {
                    for (int e=0; e<theEntities.length; e++, count++) {
                        group = theGroups[g];
                        entity = theEntities[e];
                        
                        if (doTreeTraversals) {
                            t = System.currentTimeMillis();
                            traverseTree(group, group.getKey());
                            traverseTreeStats.addValue(System.currentTimeMillis() - t);
                        }
                        
                        
                        
                        t = System.currentTimeMillis();
                        result = group.contains(entity);
                        containsStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(group.getKey()).append(".contains.").
                            append(entity.getKey()).append(':').append(result).toString());
                        
                        t = System.currentTimeMillis();
                        result = group.deepContains(entity);
                        deepContainsStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(group.getKey()).append(".deepContains.").
                            append(entity.getKey()).append(':').append(result).toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = group.getMembers();
                        getMembersStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(group.getKey()).append(".getMembers:").toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = group.getAllMembers();
                        getAllMembersStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(group.getKey()).
                            append(".getAllMembers:").toString());
                        
                        t = System.currentTimeMillis();
                        result = entity.isMemberOf(group);
                        isMemberOfStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(entity.getKey()).append(".isMemberOf.").
                            append(group.getKey()).append(':').append(result).toString());
                        
                        t = System.currentTimeMillis();
                        result = entity.isDeepMemberOf(group);
                        isDeepMemberOfStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(entity.getKey()).append(".isDeepMemberOf.").
                            append(group.getKey()).append(':').append(result).toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = entity.getContainingGroups();
                        getContainingGroupsStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(entity.getKey()).
                            append(".getContainingGroups:").toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = entity.getAllContainingGroups();
                        getAllContainingGroupsStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(entity.getKey()).
                            append(".getAllContainingGroups:").toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = groupService.findMembers(group);
                        findMembersStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(group.getKey()).
                            append(".findMembers:").toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = groupService.findContainingGroups(entity);
                        findContainingGroupsStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(entity.getKey()).
                            append(".findContainingGroups:").toString());
                        
                        t = System.currentTimeMillis();
                        gmResult = GroupService.findGroup(group.getKey());
                        findGroupStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(group.getKey()).append(".findGroup:").
                            append(gmResult != null ? gmResult.getKey() : "null").toString());
                        
/*
                        t = System.currentTimeMillis();
                        try {
                            gmResult = GroupService.findLockableGroup(group.getKey(), "admin");
                            findGroupWithLockStats.addValue(System.currentTimeMillis() - t);
                            addResult(new StringBuffer(group.getKey()).append(".findGroupWithLock:").
                                append(gmResult != null ? gmResult.getKey() : "null").toString());
                        } catch (GroupsException ge) {
                            findGroupWithLockStats.addValue(System.currentTimeMillis() - t);
                            addResult(new StringBuffer(group.getKey()).append(".findGroupWithLock:").
                                append("locked").toString());
                        }
*/
                        
                        t = System.currentTimeMillis();
                        gmResult = GroupService.getEntity(entity.getKey(), entityType);
                        getEntityStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(entity.getKey()).append(".getEntitiy:").
                            append(gmResult != null ? gmResult.getKey() : "null").toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = group.getAllEntities();
                        getAllEntitiesStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(group.getKey()).
                            append(".getAllEntities:").toString());
                        
                        t = System.currentTimeMillis();
                        resultsItr = group.getEntities();
                        getEntitiesStats.addValue(System.currentTimeMillis() - t);
                        addResults(resultsItr, new StringBuffer(group.getKey()).
                            append(".getEntities:").toString());
                        
                        
                        t = System.currentTimeMillis();
                        gmResult = group.getMemberGroupNamed(getGroupName(group));
                        getMemberGroupNamedStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(group.getKey()).append(".getMemberGroupNamed:").
                            append(gmResult != null ? gmResult.getKey() : "null").toString());
                        
                        t = System.currentTimeMillis();
                        result = group.hasMembers();
                        hasMembersStats.addValue(System.currentTimeMillis() - t);
                        addResult(new StringBuffer(group.getKey()).append(".hasMembers:").
                            append(result).toString());
                        
						// searchForEntitiesStats, searchForGroupsStats, 
                        
                    }
                }
            }
            
            if (false && functional && entityType != null) {
                TestSuite suite= new TestSuite("GroupsFunctionalTest");
                suite.addTest(new FunctionalGroupTest(entityType.getName()));
                TestResult testResult = junit.textui.TestRunner.run(suite);
                if (testResult.failureCount() > 0 || testResult.errorCount() > 0) {
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finished = true;
    }
    
    private String getGroupName(IEntityGroup group) throws Exception {
        if (functional) {
            return getOrderedGroupName(group);
        }
        
        String namedGroup = "booya";
        Iterator itr = group.getMembers();
        IGroupMember gm = null;
        while (itr.hasNext()) {
            gm = (IGroupMember)itr.next();
            if (gm.isGroup()) {
                namedGroup = ((IEntityGroup)gm).getName();
                break;
            }
        }
        return namedGroup;
    }

    private String getOrderedGroupName(IEntityGroup group) throws Exception {
        String namedGroup = "booya";
        Iterator itr = group.getMembers();
        IGroupMember gm = null;
        Set s = new TreeSet();
        while (itr.hasNext()) {
            gm = (IGroupMember)itr.next();
            if (gm.isGroup()) {
                s.add(((IEntityGroup)gm).getName());
            }
        }
        if (s.size() > 0) {
            itr = s.iterator();
            namedGroup = (String)itr.next();
        }
        return namedGroup;
    }

    private static NumberFormat getDecimalFormat() {
        if (nf == null) {
            nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
        }
        return nf;
    }

    public static void doIt(Vector groupKeys, Vector principalKeys, String principalType,
        int numThreads, int numIterations, boolean doTreeTraversals,
        boolean displayResultsChecksum, boolean dumpResults, boolean functional) throws Exception {
        try {
            
            ICompositeGroupService compositeService = GroupService.getCompositeGroupService();
            List keys =  new ArrayList(compositeService.getComponentServices().keySet());
            IIndividualGroupService gs = (IIndividualGroupService)compositeService.getComponentServices().get(keys.get(0));
            
            System.out.println("GroupsTester : Using group service: " + gs.getClass().getName());
            
            if (doTreeTraversals) {
                System.out.println("GroupsTester : performing tree traversals...");
            }
            if (displayResultsChecksum) {
                System.out.println("GroupsTester : calculating operation results checksum...");
            }
            

            NumberFormat nf = getDecimalFormat();
            System.out.println("GroupsTester : Initializing threads...");
            Thread[] threads = new Thread[numThreads];
            GroupsTester[] testers = new GroupsTester[numThreads];
            for (int i=0; i<threads.length; i++) {
                testers[i] = new GroupsTester(
                    groupKeys, principalKeys, principalType, numIterations,
                    doTreeTraversals, displayResultsChecksum, functional);
                threads[i] = new Thread(testers[i]);
            }

            System.out.println("GroupsTester : Starting threads...");
            for (int i=0; i<threads.length; i++) {
                threads[i].start();
            }

            boolean cont = true;

            long t1 = System.currentTimeMillis();
            long t2 = 0;
            
            int lastCount = 0;
            long intermediateElapsedTime = 0;
            ActionCountQueue actionCountQueue = new ActionCountQueue(10);
            long t = System.currentTimeMillis();
            int numActions = numThreads * numIterations * groupKeys.size() * principalKeys.size();
            while (cont) {
                
                Thread.sleep(2000);

                t2 = System.currentTimeMillis();
                intermediateElapsedTime = t2 - t1;
                t1 = t2;
                
                int count = 0;
                
                cont = false;
                for (int i=0; i<testers.length; i++) {
                    count += testers[i].getCount();
                    cont |= !testers[i].isFinished();
                }
                
                actionCountQueue.enqueue(count-lastCount);
                lastCount = count;
                
                double avgActions = (double)actionCountQueue.getTotalActions() / (double)actionCountQueue.size();
                long remainingTime = (long)(((numActions-count)*2000)/avgActions);
                
                System.out.print("\rGroupsTester : Action " + count + " of " + numActions + " - " + nf.format(((count*100.0)/numActions)) + "% - Remaining: " + 
                    formatRemainingTime(remainingTime, false) + "                 ");

            }
            
            if (!functional) {
                t2 = System.currentTimeMillis();
                dumpStats(testers);
                System.out.println("\n\nGroupsTester : " + numThreads + " threads / " + numIterations + " iterations Total processing time: " + (t2-t) + " ms");
            }

            if (displayResultsChecksum) {
                System.out.println("\n\n");
                String md5sum = null;
                int resultsSize = -1;
                boolean ok = true;
                for (int i=0; i<testers.length; i++) {
                    if (resultsSize < 0) {
                        resultsSize = testers[i].getResultsSize();
                    }
                    if (md5sum == null) {
                        md5sum = testers[i].getResultsChecksum();
                    }

                    if (resultsSize != testers[i].getResultsSize()) {
                        System.out.println("GroupsTester : Not all testers had the same results size: " +
                            resultsSize + ", " + testers[i].getResultsSize());
                        ok = false;
                        break;
                    }

                    if (md5sum == null) {
                        System.out.println("GroupsTester : Not all testers had a valid checksum!");
                        ok = false;
                        break;
                    }

                    if (!md5sum.equals(testers[i].getResultsChecksum())) {
                        System.out.println("GroupsTester : Not all testers had the same results: " +
                            md5sum + ", " + testers[i].getResultsChecksum());
                        ok = false;
                        break;
                    }
                }             
                if (ok && testers.length > 0) {
                    System.out.println("GroupsTester : results size, checksum: " +
                        testers[0].getResultsSize() + ", " + testers[0].getResultsChecksum());

                    if (dumpResults) {
                        System.out.println("Results:");
                        Iterator itr = testers[0].getResults().iterator();
                        while (itr.hasNext()) {
                            System.out.println((String)itr.next());
                        }
                    }
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void dumpStats(GroupsTester[] testers) {
        
        if (testers == null || testers.length == 0) return;

        NumberFormat nf = getDecimalFormat();

        String headerLine10 = makeHeaderLine(10);
        String headerLine30 = makeHeaderLine(30);
        String sep = "  ";

        Stats[] allStats = testers[0].getStats();
        System.out.println("\n\nStatistics:");
        System.out.println(formatField("Operation", 30) + sep +
            formatField("Occurrence", 10) + sep + formatField("Minimum", 10) + sep +
            formatField("Maximum", 10) + sep + formatField("Mean", 10) + sep + formatField("Median", 10));
        System.out.println(headerLine30 + sep +
            headerLine10 + sep + headerLine10 + sep +
            headerLine10 + sep + headerLine10 + sep + headerLine10);

        int start = 0;
        int end = testers.length;

/* cuts off the first and last stats
        if (testers.length > 2) {
            start = 1;
            end = testers.length-1;
        }
*/
      
 
        for (int i=0; i<testers[0].getStats().length; i++) {

            long count = 0;
            long min=Long.MAX_VALUE;
            long max=0;
            long sum=0;
            Map occurrences = new TreeMap();


            for (int j=start; j<end; j++) {
                Stats stats = testers[j].getStats()[i];
                count += stats.getCount();
                sum += stats.getSum();
                if (stats.getMin() < min) {
                    min = stats.getMin();
                }
                if (stats.getMax() > max) {
                    max = stats.getMax();
                }

                Iterator itr = testers[j].getStats()[i].getValues().iterator();
                while (itr.hasNext()) {
                    Long key = (Long)itr.next();
                    Long ct = (Long)occurrences.get(key);
                    if (ct == null) {
                        ct = new Long(0);
                    }
                    occurrences.put(key, new Long(ct.longValue() + 1));
                }
            }

            if (count > 0) {
                System.out.println(formatField(allStats[i].getLabel(), 30) + sep +
                    formatField(count, 10) + sep + formatField(min, 10) + sep +
                    formatField(max, 10) + sep + formatField((double)sum/(double)count, 10) +
                    sep + formatField(getMedian(occurrences), 10));
            }
        }
        testers[0].getStats()[3].getMedian();
    }

    private static List getValues(Map occurrences) {
        List list = new ArrayList();
        Iterator itr = occurrences.keySet().iterator();
        Long key;
        Long ct;
        while (itr.hasNext()) {
            key = (Long)itr.next();
            ct = (Long)occurrences.get(key);
            for (long i=0; i<ct.longValue(); i++) {
                list.add(key);
            }
        }
        return list;
    }

    private static double getMedian(Map occurrences) {
        List list = getValues(occurrences);

        int size = list.size();
        int mod = size%2;
        int mid = (size/2) + mod;

        if (size == 0) return 0.0;

        double med1 = ((Long)list.get(mid-1)).doubleValue();
        if (mod == 1) {
            return med1;
        }
        return (med1 + ((Long)list.get(mid)).doubleValue())/2.0;
    }

    private static String makeHeaderLine(int size) {
        StringBuffer sb = new StringBuffer(size);
        for (int i=0; i<size; i++) {
            sb.append('-');
        }
        return sb.toString();
    }

    private static String formatField(String field, int size) {
        StringBuffer sb = new StringBuffer(size);

        for (int i=0; i<(size-field.length()); i++) {
            sb.append(' ');
        }
        sb.append(field);
        return sb.toString();
    }

    private static String formatField(long val, int size) {
        return formatField(Long.toString(val), size);
    }

    private static String formatField(double val, int size) {
        String field;
        if (val < 1.0) {
            field = getDecimalFormat().format(val);
        } else {
            field = Long.toString((long)val);
        }

        return formatField(field, size);
    }

    public class Stats {
        private long count=0;
        private long min=Long.MAX_VALUE;
        private long max=0;
        private long sum=0;
        private String label;
        private NumberFormat nf;
        Map occurrences = new TreeMap();

        public Stats(String label) {
            this.label = label;
            this.nf = NumberFormat.getInstance();
            this.nf.setMaximumFractionDigits(2);
        }

        public void addValue(long value) {
//            wr.println("STATS: " + label + " - " + value);
            count++;
            sum+=value;
            if (value < min) {
                min = value;
            }
            if (value > max) {
                max = value;
            }
            Long key = new Long(value);
            Long ct = (Long)occurrences.get(key);
            if (ct == null) {
                ct = new Long(0);
            }
            occurrences.put(key, new Long(ct.longValue() + 1));
        }

        public String getLabel() { return label; }
        public long getMin() { return min; }
        public long getMax() { return max; }
        public long getCount() { return count; }
        public long getSum() { return sum; }

        public double getMean() {
            return ((double)sum / (double)count);
        }

        public List getValues() {
            List list = new ArrayList();
            Iterator itr = occurrences.keySet().iterator();
            Long key;
            Long ct;
            while (itr.hasNext()) {
                key = (Long)itr.next();
                ct = (Long)occurrences.get(key);
                for (long i=0; i<ct.longValue(); i++) {
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

            double med1 = ((Long)list.get(mid-1)).doubleValue();
            if (mod == 1) {
                return med1;
            }
            return (med1 + ((Long)list.get(mid)).doubleValue())/2.0;
        }
    }

    private static final String usage = "Usage: GroupsTester [-A --allGroupsEntities] [-f --functional] {{-g, --groupKey} val} {{-p, --principalKey} val} {{-t, --principalType} val} " +
    "{{-c, --threadCount} val} {{-i, --iterations} val} [{-a, --performTreeTraversals} (default no)] " +
    "[{-C, --displayResultsChecksum} (default no)] [{-r, --dumpResults} (default no)]\n\n" +
    "Example: GroupsTester --groupKey=local.0 --principalKey=demo --principalType=org.jasig.portal.security.IPerson --threadCount=10 --iterations=100 --startGroupsInitializer --dumpResults";
    
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();                 
        CmdLineParser.Option allGroupsEntities = parser.addBooleanOption('A', "allGroupsEntities");     
        CmdLineParser.Option groupKey = parser.addStringOption('g', "groupKey");     
        CmdLineParser.Option principalKey = parser.addStringOption('p', "principalKey"); 
        CmdLineParser.Option principalType = parser.addStringOption('t', "principalType");       
        CmdLineParser.Option threadCount = parser.addIntegerOption('c', "threadCount");
        CmdLineParser.Option iterations = parser.addIntegerOption('i', "iterations");
        CmdLineParser.Option performTreeTraversals = parser.addBooleanOption('a', "performTreeTraversals");
        CmdLineParser.Option displayResultsChecksum = parser.addBooleanOption('C', "displayResultsChecksum");
        CmdLineParser.Option dumpResults = parser.addBooleanOption('r', "dumpResults");
        CmdLineParser.Option sleep = parser.addBooleanOption("sleep");
        CmdLineParser.Option functional = parser.addBooleanOption('f', "functional");
        
        
        
        Vector groups = null;
        Vector entities = null;
        String principalTypeValue = null;
        Integer threadCountValue = null;
        Integer iterationsValue = null;
        boolean functionalValue = false;
        
        
        

        try {
            parser.parse(args);
            
            principalTypeValue = (String)parser.getOptionValue(principalType);
            if (principalTypeValue != null) {
                if (((Boolean)parser.getOptionValue(allGroupsEntities, Boolean.FALSE)).booleanValue()) {
                    groups = fetchAllGroups(principalTypeValue);
                    entities = fetchAllEntities(principalTypeValue);
                } else {
                    groups = parser.getOptionValues(groupKey);
                    entities = parser.getOptionValues(principalKey);
                }
            }
            
            functionalValue = ((Boolean)parser.getOptionValue(functional, Boolean.FALSE)).booleanValue();
            
            if (functionalValue) {
                threadCountValue = new Integer(1);
                iterationsValue = threadCountValue;
            } else {
                threadCountValue = (Integer)parser.getOptionValue(threadCount);
                iterationsValue = (Integer)parser.getOptionValue(iterations);
            }
            
            if (principalTypeValue != null && groups != null && entities != null &&
                iterationsValue != null && threadCountValue != null) {

                System.out.println("Group Count: " + groups.size());
                System.out.println("Entity Count: " + entities.size());
                System.out.println("Total Combinations: " + (groups.size()*entities.size()));
            
                doIt(
                    groups,
                    entities,
                    principalTypeValue,
                    threadCountValue.intValue(),
                    iterationsValue.intValue(),
                    ((Boolean)parser.getOptionValue(performTreeTraversals, Boolean.FALSE)).booleanValue(),
                    ((Boolean)parser.getOptionValue(displayResultsChecksum, Boolean.FALSE)).booleanValue(),
                    ((Boolean)parser.getOptionValue(dumpResults, Boolean.FALSE)).booleanValue(),
                    functionalValue
                );
            
                System.out.flush();
                if (((Boolean)parser.getOptionValue(sleep, Boolean.FALSE)).booleanValue()) {
                    System.out.println("Sleeping...");
                    System.out.flush();
                    Thread.sleep(10000000);
                }
            } else {
                if (principalTypeValue == null) {
                    System.out.println("Missing --principalType parameter.");
                }
                if (principalTypeValue == null) {
                    System.out.println("Missing --principalType parameter.");
                }
                if (groups == null) {
                    System.out.println("Missing --groupKey parameter(s).");
                }
                if (entities == null) {
                    System.out.println("Missing --principalKey parameter(s).");
                }
                if (threadCountValue == null) {
                    System.out.println("Missing --threadCount parameter.");
                }
                if (iterationsValue == null) {
                    System.out.println("Missing --iterations parameter.");
                }
                System.out.println(usage);
            }
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            System.out.println(usage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    
    private static Vector fetchAllGroups(String principalType) throws Exception {
        Vector ret = new Vector();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select g.group_id from up_group g, up_entity_type et where g.entity_type_id = et.entity_type_id and et.entity_type_name = ?";
		
		try {
			conn = RDBMServices.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, principalType);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				ret.add("local."+rs.getString("group_id"));
			}
		} finally {
			RDBMServices.closeResultSet(rs);
			RDBMServices.closePreparedStatement(ps);
			RDBMServices.releaseConnection(conn);
		}

		return ret;
    }
    
    private static Vector fetchAllEntities(String principalType) throws Exception {
        Vector ret = new Vector();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select m.member_key from up_group_membership m, up_group g, up_entity_type et where g.entity_type_id = et.entity_type_id and m.group_id = g.group_id and m.member_is_group = 'F' and et.entity_type_name = ?";
		
		try {
			conn = RDBMServices.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, principalType);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				ret.add(rs.getString("member_key"));
			}
		} finally {
			RDBMServices.closeResultSet(rs);
			RDBMServices.closePreparedStatement(ps);
			RDBMServices.releaseConnection(conn);
		}

		return ret;
    }
    
    public static String formatRemainingTime(long ms, boolean showMills) {

        long scratch = ms;
        long hours;
        long minutes;
        long seconds;
        long mills;

        hours = scratch/3600000;

        scratch = scratch - (hours*3600000);

        minutes = scratch/60000;
        scratch = scratch - (minutes*60000);

        seconds = scratch/1000;
        mills = scratch - (seconds*1000);

        StringBuffer sb = new StringBuffer(40);
        if (hours > 0) {
            sb.append(hours).append(" hour");
            if (hours > 1) {
                sb.append("s");
            }
        }
        if (hours > 0 || minutes > 0) {
            sb.append(" ").append(minutes).append(" minute");
            if (minutes != 1) {
                sb.append("s");
            }
        }
        if (seconds > 0 || !showMills) {
            sb.append(" ").append(seconds).append(" second");
            if (seconds != 1) {
                sb.append("s");
            }
        }

        if (showMills) {
            sb.append(" ").append(mills).append(" ms");
        }

        for (int i=sb.length(); i<=40; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
