                                                                    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client;

import java.util.concurrent.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.ClientBuilder;

/**
 *
 * @author macbook
 */

public class App{
    
//    private static final String BASE_URI = "http://34.221.90.157:8080/Ec2JerseyServer/webresources/";
    private static final String BASE_URI = "http://localhost:8080/wearableDeviceEc2Server/webresources/";        
    private static final String PATH = "ec2Server";
    
//    private static final String BASE_URI = "https://ldl841pti9.execute-api.us-west-2.amazonaws.com/Prod/";
//    private static final String PATH = "lambdatest";

    private static List<Thread> threadList = new ArrayList<>();
            
    private static void executeWorkThreads(WebTarget webTarget, Stat stat, int threadCount) {
        for(int i=0; i<threadCount; i++) {
            Thread r = new WorkThread(webTarget, stat);
            threadList.add(r);
        }
        for(Thread t: threadList) {
            t.start();
        }
        for(Thread t : threadList){
            try{
                t.join();
            }catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
            
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException{
        int maxThread = 100;
        System.out.println("client " + maxThread + " 100 " + BASE_URI + PATH);
        
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(BASE_URI).path(PATH);
        Stat stat = new Stat();
        
        long startTimeTotal = System.currentTimeMillis();
        System.out.println("Client start...... Time: " + startTimeTotal);
        
        //warmup
        long startTimeWarmup = System.currentTimeMillis();
        System.out.println("Warmup phase: All threads running...");
        executeWorkThreads(webTarget, stat, maxThread/10);
        long endTimeWarmup = System.currentTimeMillis();
        long wallTimeWarmup = endTimeWarmup - startTimeWarmup;
        System.out.println("Warmup phase complete: Time " + wallTimeWarmup/1000.0 + " seconds");
        
        //loading
        threadList.clear();
        long startTimeLoading = System.currentTimeMillis();
        System.out.println("Loading phase: All threads running...");
        executeWorkThreads(webTarget, stat, maxThread/2);
        long endTimeLoading = System.currentTimeMillis();
        long wallTimeLoading = endTimeLoading - startTimeLoading;
        System.out.println("Loading phase complete: Time " + wallTimeLoading/1000.0 + " seconds");
        
        //peak
        threadList.clear();
        System.out.println("Peak phase: All threads running...");
        long startTimePeak = System.currentTimeMillis();
        executeWorkThreads(webTarget, stat, maxThread);
        long endTimePeak = System.currentTimeMillis();
        long wallTimePeak = endTimePeak - startTimePeak;  
        System.out.println("Peak phase complete: Time " + wallTimePeak/1000.0 + " seconds");

        //cooldown
        threadList.clear();
        System.out.println("Cooldown phase: All threads running...");
        long startTimeCooldown = System.currentTimeMillis();
        executeWorkThreads(webTarget, stat, maxThread/4);
        long endTimeCooldown = System.currentTimeMillis();
        long wallTimeCooldown = endTimeCooldown - startTimeCooldown; 
        System.out.println("Cooldown phase complete: Time " + wallTimeCooldown/1000.0 + " seconds");

        long endTimeTotal = System.currentTimeMillis();
        long wallTimeTotal = endTimeTotal - startTimeTotal;
        
        System.out.println("=======================================");
        System.out.println("Total number of requests sent: " + Stat.getRequestNum());
        System.out.println("Total number of Successful responses: " + Stat.getRequestSuccessNum());
        System.out.println("Test Wall Time: " + wallTimeTotal/1000.0 + " seconds");

        NumberFormat formatter = new DecimalFormat("#0.0");  
        System.out.println("Overall throughput accross all phases:" + formatter.format(Stat.getRequestNum()/(wallTimeTotal/1000.0)));
        System.out.println("Mean latency for all requests: " + formatter.format(Stat.getMeanLatency()) + " milliseconds");
        System.out.println("Median latency for all requests: " + formatter.format(Stat.getMedianLatency()) + " milliseconds");
        System.out.println("99th percentile latency for all requests: " + Stat.get99Latency() + " milliseconds");
        System.out.println("95th percentile latency for all requests: " + Stat.get95Latency() + " milliseconds");
    }
    
 
}
