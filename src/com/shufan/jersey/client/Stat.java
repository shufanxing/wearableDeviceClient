/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author macbook
 */
  public class Stat {
        //data from threads
        private static AtomicInteger requestNum = new AtomicInteger(0);
        private static AtomicInteger requestSuccessNum = new AtomicInteger(0);

        public static void increaseRequestNum() { requestNum.addAndGet(1); }
        public static int getRequestNum() { return requestNum.get();}
        public static void increaseRequestSuccessNum() { requestSuccessNum.addAndGet(1); }
        public static int getRequestSuccessNum() { return requestSuccessNum.get();}

        //latency stat
        private static List latencyList = new ArrayList<Long>();
        
        public static List getLatencyList() { 
            return latencyList;
        }
        
        private static List<Long> sortedLatencyList = new ArrayList<>();
        private static void handleSortLatencyList() {
            sortedLatencyList.addAll(latencyList);
            Collections.sort(sortedLatencyList);
        }
        
        public static double getMeanLatency() {
            if(sortedLatencyList.isEmpty()) {
                handleSortLatencyList();
            }
            
            double totalLatency = 0.0;
            
            Iterator<Long> iter = sortedLatencyList.iterator();
            while(iter.hasNext()) {
                totalLatency += iter.next();
            }
            
            return totalLatency/sortedLatencyList.size();
        }
        
        public static double getMedianLatency() {
            if(sortedLatencyList.isEmpty()) {
                handleSortLatencyList();
            }
            
            double latency = 0.0;
            
            int listSize = sortedLatencyList.size();
            
            if(listSize%2 == 0 && listSize > 0) {
                latency = (sortedLatencyList.get(listSize/2 - 1) + sortedLatencyList.get(listSize/2)) / 2.0;
            }else if(listSize >0 ){
                latency = sortedLatencyList.get(listSize/2);
            }else{
                System.out.println("Error: failed to calculate latency median");
            }
            
            return latency;
        }
        
        public static long get99Latency() {
            if(sortedLatencyList.isEmpty()) {
                handleSortLatencyList();
            }
                       
            int listSize = sortedLatencyList.size();
            
            return sortedLatencyList.get((int)(listSize*0.99));
        }
        
        public static long get95Latency() {
            if(sortedLatencyList.isEmpty()) {
                handleSortLatencyList();
            }

            int listSize = sortedLatencyList.size();
            
            return sortedLatencyList.get((int)(listSize*0.95));
        }
    }
