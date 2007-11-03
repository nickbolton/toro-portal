/*
 * Created on Mar 23, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.interactivebusiness.news.data;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author ibiswas
 *
 * Implementation of a timed cache that flushes out all data 
 * every 15 minutes. The order of the objects added in the map 
 * is maintained.
 */
public class TimedCache extends LinkedHashMap {

    // instance memebrs
    private Date expireDate;
    private static final int TIMEOUT_VALUE = 15; // in minutes
    
    /**
     * 
     */
    public TimedCache() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, TIMEOUT_VALUE);
        expireDate = cal.getTime();
    }
    
    public Object get(Object key){
        if(expireDate.before(new Date(System.currentTimeMillis()))){
            this.clear();
            return null;
        }
        return super.get(key);
    }
    
    public Object put(Object key, Object value){
        if(expireDate.before(new Date(System.currentTimeMillis()))){
            this.clear();
            return null;
        }
        return super.put(key, value);
    }
    
    public boolean isEmpty(){
        if(expireDate.before(new Date(System.currentTimeMillis()))){
            this.clear();
            return true;
        }
        return super.isEmpty();
    }
    
    public Object remove(Object key){
        if(expireDate.before(new Date(System.currentTimeMillis()))){
            this.clear();
            return null;
        }
        return super.remove(key);
    }
    
    public void clear(){
        System.out.println("Clearing timedCache. **************************************************************");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, TIMEOUT_VALUE);
        expireDate = cal.getTime();
        super.clear();
    }
    
}
