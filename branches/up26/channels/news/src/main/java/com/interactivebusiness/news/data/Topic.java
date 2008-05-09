/*
 * Created on Mar 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.interactivebusiness.news.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class Topic {

    
    // instance members
    private final String topicId;
    private String topicName;
    private String description;
    private final Date dateCreated; 
    private Map articles;
    
    /**
     * 
     */
    public Topic(String topicId, String topicName, String desc, Date created){
        this.topicId = topicId;
        this.topicName = topicName;
        this.description = desc;
        this.dateCreated = created;   
        this.articles = new LinkedHashMap();
    }
    
    public void addArticle(NewsInfo newsInfo){
        this.articles.put(newsInfo.getID(), newsInfo);        
    }
    
    public NewsInfo getArticle(String newsId){
        return (NewsInfo)this.articles.get(newsId);
    }

    public String getName(){
        return this.topicName;
    }
    
    public void setName(String name){
        this.topicName = name;
    }
    
    public String getDesc(){
        return this.topicName;
    }
    
    public void setDesc(String desc){
        this.description = desc;
    }
    
    public String getId(){
        return this.topicId;
    }
    
    public Date getDateCreated(){
        return this.dateCreated;
    }
    
    public void deleteArticle(String newsId){
        articles.remove(newsId);
    }
    
    public void updateArticle(NewsInfo newsInfo){
        addArticle(newsInfo);
    }
    
    public ArrayList getArticles(){
        return new ArrayList(articles.values());
    }
}
