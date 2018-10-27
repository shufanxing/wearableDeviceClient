                                                                                                                                                                                                                                            /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 * Jersey REST client generated for REST resource:Ec2Server [ec2Server]<br>
 * USAGE:
 * <pre>
 *        NewJerseyClient client = new NewJerseyClient();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author macbook
 */
public class WorkThread extends Thread{

    private WebTarget webTarget = null;
    private Stat stat = null;
    
    private int iterNum = 100;
    private String postContent = "test";

    public void setWebTarget(WebTarget webTarget) {
        this.webTarget = webTarget;
    }
    
    public void setIterNum(int iterNum) {
        this.iterNum = iterNum;
    }
    
    public void setPostContent(String str){
        this.postContent = str;
    }

    public WorkThread(WebTarget webTarget, Stat stat) {
        this.webTarget = webTarget;
        this.stat = stat;
    }

    public <T> T postText(Object requestEntity, Class<T> responseType) throws ClientErrorException {
        return webTarget.request(MediaType.TEXT_PLAIN).post(Entity.entity(requestEntity, MediaType.TEXT_PLAIN), responseType);
    }

    public String getStatus() throws ClientErrorException {
        return webTarget.request(MediaType.TEXT_PLAIN).get(String.class);
    }
   
    @Override
    public void run() {
       for(int i=0; i<iterNum; ++i) {
           //getStatus
           stat.increaseRequestNum();
           long startTimeGet = System.currentTimeMillis();
           
           String getRes = "";
           try{
                getRes = getStatus();
           }catch(Exception e) {
               System.out.println("Failure in getStatus: " + e.getMessage());
           } 
           if(getRes.contains("alive")){
               stat.increaseRequestSuccessNum();
           }
           long endTimeGet = System.currentTimeMillis();
           stat.addLatency(endTimeGet - startTimeGet);
           
           //postText
           stat.increaseRequestNum();
           long startTimePost = System.currentTimeMillis();
           String postRes = "";
           try {
                postRes = postText(postContent, String.class);
           }catch(Exception e) {
               System.out.println("Failure in postContent: " + e.getMessage());
           }           
           if(postRes.length()>0 && Integer.valueOf(postRes) == postContent.length()) {
               stat.increaseRequestSuccessNum();
           }
           long endTimePost = System.currentTimeMillis();
           stat.addLatency(endTimePost - startTimePost);
       }
    }
}


 