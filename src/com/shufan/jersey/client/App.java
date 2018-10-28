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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author macbook
 */

public class App{
    
//    private static final String BASE_URI = "http://34.221.90.157:8080/Ec2JerseyServer/webresources/";

    
//    private static final String BASE_URI = "https://ldl841pti9.execute-api.us-west-2.amazonaws.com/Prod/";
//    private static final String PATH = "lambdatest";
    
    public static final int maxStepCount = 5000; //0 -> maxStepCount
    public static int userPopulation; //1 -> userPopulation
    public static int dayNumber; // 1 -> dayNumber
    
    private static List<Thread> threadList = new ArrayList<>();
    
    private static void executeWorkThreads(WebTarget webTarget, Stat stat, int threadCount, int phaseStart, int phaseLength, int numberOfTestsperPhase, int day) {
        int iterNum = numberOfTestsperPhase * phaseLength;
        
        for(int i=0; i<threadCount; i++) {
            Thread r = new WorkThread(webTarget, stat, phaseStart, phaseLength, iterNum, day);
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
    
    public static Integer deleteAll(WebTarget webTarget) {
        return webTarget.path("/deleteAll").request(MediaType.TEXT_PLAIN).post(Entity.entity(null, MediaType.TEXT_PLAIN), Integer.class);
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException{
        int maxThread = 64;//default: 64
        System.out.println("client maxthread: " + maxThread + ";");
        
        String BASE_URI = "http://localhost:8080/wearableDeviceServer/webresources";
        System.out.println("url: " + BASE_URI);
        
        dayNumber = 1; 
        System.out.println("dayNumber: " + dayNumber);
        
        int day = 1; //NOTE: only test the day 1 now
        
        userPopulation = 1000000; //default: 100,000 
        System.out.println("userPolulation: " + userPopulation);
        
        int numberOfTestsPerPhase = 100; //default 100
        System.out.println("numberOfTestsPerPhase: " + numberOfTestsPerPhase);
        
        
        Client client = ClientBuilder.newClient();
//        WebTarget webTarget = client.target(BASE_URI).path("");
        WebTarget webTarget = client.target(BASE_URI);
        Stat stat = new Stat();
        
        //clean old records in table;
        try {
            Integer deletedRecordsNum = deleteAll(webTarget);
            System.out.println("deleted old records in table:" + deletedRecordsNum);
        } catch (Exception e) {
            System.out.println("Failure in delete all: " + e.getMessage());
        }
        
        long startTimeTotal = System.currentTimeMillis();
        System.out.println("Client start...... Time: " + startTimeTotal);
        
        //warmup
        long startTimeWarmup = System.currentTimeMillis();
        System.out.println("Warmup phase: All threads running...");
        executeWorkThreads(webTarget, stat, maxThread/10, 0, 3, numberOfTestsPerPhase, 1);
        long endTimeWarmup = System.currentTimeMillis();
        long wallTimeWarmup = endTimeWarmup - startTimeWarmup;
        System.out.println("Warmup phase complete: Time " + wallTimeWarmup/1000.0 + " seconds");
        
        //loading
        threadList.clear();
        long startTimeLoading = System.currentTimeMillis();
        System.out.println("Loading phase: All threads running...");
        executeWorkThreads(webTarget, stat, maxThread/2, 3, 5, numberOfTestsPerPhase, 1);
        long endTimeLoading = System.currentTimeMillis();
        long wallTimeLoading = endTimeLoading - startTimeLoading;
        System.out.println("Loading phase complete: Time " + wallTimeLoading/1000.0 + " seconds");
        
        //peak
        threadList.clear();
        System.out.println("Peak phase: All threads running...");
        long startTimePeak = System.currentTimeMillis();
        executeWorkThreads(webTarget, stat, maxThread, 8, 11, numberOfTestsPerPhase, 1);
        long endTimePeak = System.currentTimeMillis();
        long wallTimePeak = endTimePeak - startTimePeak;  
        System.out.println("Peak phase complete: Time " + wallTimePeak/1000.0 + " seconds");

        //cooldown
        threadList.clear();
        System.out.println("Cooldown phase: All threads running...");
        long startTimeCooldown = System.currentTimeMillis();
        executeWorkThreads(webTarget, stat, maxThread/4, 19, 5, numberOfTestsPerPhase, 1);
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
        
        ExcelWriter.write(Stat.getResponseTimeList(), startTimeTotal, "./temp/resultStat.xlsx");
    }
    
 
}
