/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client;

import com.shufan.jersey.client.file.LatencyFileReader;
import com.shufan.jersey.client.file.TimeFileWriter;
import com.shufan.jersey.client.file.LatencyFileWriter;
import java.util.concurrent.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author macbook
 */
public class App {
    public static final int maxStepCount = 5000; //0 -> maxStepCount
    public static int userPopulation; //1 -> userPopulation
    public static int dayNumber; // 1 -> dayNumber

    public static String timeFile = "./temp/resp_times.csv";
    public static String latencyFile = "./temp/latencies.csv";

    private static void executeWorkThreads(WebTarget webTarget, Stat stat, CountDownLatch _latch, 
            ConcurrentLinkedQueue<Long> timeQueue,  ConcurrentLinkedQueue<Long> latencyQueue, 
            int threadCount, int phaseStart, int phaseLength, int numberOfTestsperPhase, int day) throws InterruptedException {
        int iterNum = numberOfTestsperPhase * phaseLength;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread r = new WorkThread(webTarget, stat, _latch, timeQueue, latencyQueue, phaseStart, phaseLength, iterNum, day);
            executor.execute(r);
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

    }

    public static Response deleteAll(WebTarget webTarget) {
        return webTarget.path("/deleteAll").request(MediaType.APPLICATION_JSON).post(Entity.entity(null, MediaType.TEXT_PLAIN));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            ConcurrentLinkedQueue<Long> timeQueue = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<Long> latencyQueue = new ConcurrentLinkedQueue<>();
            
            int maxThread = 64;//default: 64
            System.out.println("client maxthread: " + maxThread + ";");

            String BASE_URI = "http://localhost:8080/wearableDeviceEc2Server/webresources";
//            String BASE_URI = "http://localhost:8084/wd/webresources";
//            String BASE_URI = "http://localhost:8081/WearableDevice/rest/tomcat";

//            String BASE_URI = "http://18.236.75.89:8080/WearableDevice/rest/tomcat";

//            String BASE_URI = "http://wd-lb-983955425.us-west-2.elb.amazonaws.com:8080/WearableDeviceServer/webresources";

            System.out.println("url: " + BASE_URI);

            dayNumber = 1;
            System.out.println("dayNumber: " + dayNumber);

            int day = 1; //NOTE: only test the day 1 now

            userPopulation = 1000000; //default: 100,000 
            System.out.println("userPolulation: " + userPopulation);

            int numberOfTestsPerPhase = 100; //default 100
            System.out.println("numberOfTestsPerPhase: " + numberOfTestsPerPhase);

            Client client = ClientBuilder.newClient();
            WebTarget webTarget = client.target(BASE_URI);
            Stat stat = new Stat();

            //clean old records in table;
            Response  deletedAllRes = null;
            try {
                deletedAllRes = deleteAll(webTarget);
                String s = deletedAllRes.readEntity(String.class);
                System.out.println("deleted old records in table:" + s);
            } catch (Exception e) {
                System.out.println("Failure in delete all: " + e.getMessage());
            }
            
            if (deletedAllRes == null || deletedAllRes.getStatus() < 200 || deletedAllRes.getStatus() >= 300) { //TODO: change
            
                System.out.println("Failure in post 3: " );
            }
            
            //start work            
            long startTimeTotal = System.currentTimeMillis();
            System.out.println("Client start...... Time: " + startTimeTotal);


            new Thread(new TimeFileWriter(timeFile, timeQueue, startTimeTotal)).start();//The thread of monitoring, continuously from the queue read and write data to a file
            new Thread(new LatencyFileWriter(latencyFile, latencyQueue)).start();//The thread of monitoring, continuously from the queue read and write data to a file

            //warmup
            long startTimeWarmup = System.currentTimeMillis();
            System.out.println("Warmup phase: All threads running...");
            int warmupThreadNum = maxThread / 10;
            CountDownLatch warmupLatch = new CountDownLatch(Math.max(warmupThreadNum / 2, 1));
            executeWorkThreads(webTarget, stat, warmupLatch, timeQueue, latencyQueue, warmupThreadNum, 0, 3, numberOfTestsPerPhase, 1);
            warmupLatch.await();
            long endTimeWarmup = System.currentTimeMillis();
            long wallTimeWarmup = endTimeWarmup - startTimeWarmup;
            System.out.println("Warmup phase complete: Time " + wallTimeWarmup / 1000.0 + " seconds");

            //loading
            long startTimeLoading = System.currentTimeMillis();
            System.out.println("Loading phase: All threads running...");
            int loadingThreadNum = maxThread / 2;
            CountDownLatch loadingLatch = new CountDownLatch(Math.max(loadingThreadNum / 2, 1));
            executeWorkThreads(webTarget, stat, loadingLatch, timeQueue, latencyQueue, loadingThreadNum, 3, 5, numberOfTestsPerPhase, 1);
            loadingLatch.await();
            long endTimeLoading = System.currentTimeMillis();
            long wallTimeLoading = endTimeLoading - startTimeLoading;
            System.out.println("Loading phase complete: Time " + wallTimeLoading / 1000.0 + " seconds");

            //peak
            System.out.println("Peak phase: All threads running...");
            long startTimePeak = System.currentTimeMillis();
            int peakThreadNum = maxThread;
            CountDownLatch peakLatch = new CountDownLatch(Math.max(peakThreadNum / 2, 1));
            executeWorkThreads(webTarget, stat, peakLatch, timeQueue, latencyQueue, peakThreadNum, 8, 11, numberOfTestsPerPhase, 1);
            peakLatch.await();
            long endTimePeak = System.currentTimeMillis();
            long wallTimePeak = endTimePeak - startTimePeak;
            System.out.println("Peak phase complete: Time " + wallTimePeak / 1000.0 + " seconds");

            //cooldown
            System.out.println("Cooldown phase: All threads running...");
            long startTimeCooldown = System.currentTimeMillis();
            int cooldownThreadNum = maxThread / 4;
            CountDownLatch cooldownLatch = new CountDownLatch(cooldownThreadNum);
            executeWorkThreads(webTarget, stat, cooldownLatch, timeQueue, latencyQueue, cooldownThreadNum, 19, 5, numberOfTestsPerPhase, 1);
            cooldownLatch.await();
            long endTimeCooldown = System.currentTimeMillis();
            long wallTimeCooldown = endTimeCooldown - startTimeCooldown;
            System.out.println("Cooldown phase complete: Time " + wallTimeCooldown / 1000.0 + " seconds");

            long endTimeTotal = System.currentTimeMillis();
            long wallTimeTotal = endTimeTotal - startTimeTotal;
            
            //stop the work of file writer
            timeQueue.add(-1L);
            latencyQueue.add(-1L);

            System.out.println("=======================================");
            System.out.println("Total number of requests sent: " + Stat.getRequestNum());
            System.out.println("Total number of Successful responses: " + Stat.getRequestSuccessNum());
            System.out.println("Test Wall Time: " + wallTimeTotal / 1000.0 + " seconds");

            NumberFormat formatter = new DecimalFormat("#0.0");
            System.out.println("Overall throughput accross all phases:" + formatter.format(Stat.getRequestNum() / (wallTimeTotal / 1000.0)));
            
            //calculate latency stat
            LatencyFileReader.read(latencyFile, Stat.getLatencyList());
            
            System.out.println("Mean latency for all requests: " + formatter.format(Stat.getMeanLatency()) + " milliseconds");
            System.out.println("Median latency for all requests: " + formatter.format(Stat.getMedianLatency()) + " milliseconds");
            System.out.println("99th percentile latency for all requests: " + Stat.get99Latency() + " milliseconds");
            System.out.println("95th percentile latency for all requests: " + Stat.get95Latency() + " milliseconds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
