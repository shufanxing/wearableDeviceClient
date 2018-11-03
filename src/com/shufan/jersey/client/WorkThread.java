/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.util.concurrent.ThreadLocalRandom;
import javax.ws.rs.core.Response;

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
public class WorkThread extends Thread {

    public static int retry = 1;

    private WebTarget webTarget = null;
    private Stat stat = null;
    private CountDownLatch _latch = null;
    ConcurrentLinkedQueue<Long> timeQueue = null;
    ConcurrentLinkedQueue<Long> latencyQueue = null;
    private int phaseStart;
    private int phaseLength;
    private int iterNum;
    private int day;

    public WorkThread(WebTarget webTarget, Stat stat, CountDownLatch _latch, ConcurrentLinkedQueue<Long> timeQueue, ConcurrentLinkedQueue<Long> latencyQueue, int phaseStart, int phaseLength, int iterNum, int day) {
        this.webTarget = webTarget;
        this.stat = stat;
        this._latch = _latch;
        this.timeQueue = timeQueue;
        this.latencyQueue = latencyQueue;
        this.phaseStart = phaseStart;
        this.phaseLength = phaseLength;
        this.iterNum = iterNum;
        this.day = day;
    }

    public Response postStepCount(WebTarget webTarget, int userID, int day, int timeInterval, int stepCount) {
        return webTarget.path("/" + userID + "/" + day + "/" + timeInterval + "/" + stepCount)
                .request(MediaType.APPLICATION_JSON).post(Entity.entity(null, MediaType.TEXT_PLAIN));
    }

    public Response getCurrent(WebTarget webTarget, int userID) {
        return webTarget.path("/current/" + userID).request(MediaType.APPLICATION_JSON).get(Response.class);
    }

    public Response getSingle(WebTarget webTarget, int userID, int day) {
        return webTarget.path("/single/" + userID + "/" + day).request(MediaType.APPLICATION_JSON).get(Response.class);
    }

    @Override
    public void run() {

        for (int i = 0; i < iterNum; ++i) {
            Response post1Res = null;
            Response get1Res = null;
            Response post2Res = null;
            Response get2Res = null;
            Response post3Res = null;
            try {

                int[] userIDs = new int[3];
                int[] timeIntervals = new int[3];
                int[] stepCounts = new int[3];

                for (int j = 0; j < 3; j++) {
                    userIDs[j] = ThreadLocalRandom.current().nextInt(App.userPopulation) + 1;
                    timeIntervals[j] = ThreadLocalRandom.current().nextInt(phaseLength) + phaseStart;
                    stepCounts[j] = ThreadLocalRandom.current().nextInt(App.maxStepCount + 1);
                }

                //post 1
                stat.increaseRequestNum();
                long startTimePost1 = System.currentTimeMillis();

                int retry1 = retry;
                boolean success1 = false;
                String errorMsg1 = null;
                while (retry1-- > 0) {
                    try {
                        post1Res = postStepCount(webTarget, userIDs[0], day, timeIntervals[0], stepCounts[0]);
                    } catch (Exception e) {
                        errorMsg1 = e.getMessage();
//                        e.printStackTrace();
                    }
                    if (post1Res != null && post1Res.getStatus() >= 200 && post1Res.getStatus() < 300) { //TODO: change
                        stat.increaseRequestSuccessNum();
                        success1 = true;
                        break;
                    }
                }
                if (!success1) {
                    if (post1Res == null) {
                        System.out.println("Failure in post 1: " + errorMsg1);
                    } else {
                        System.out.println("Failure in post 1:" + post1Res.getStatus());
                    }
                }
                if (post1Res != null) {
                    post1Res.close();
                }

                long endTimePost1 = System.currentTimeMillis();

                timeQueue.add(endTimePost1);
                latencyQueue.add(endTimePost1 - startTimePost1);

                //post 2
                stat.increaseRequestNum();
                long startTimePost2 = System.currentTimeMillis();

                int retry2 = retry;
                boolean success2 = false;
                String errorMsg2 = null;
                while (retry2-- > 0) {
                    try {
                        post2Res = postStepCount(webTarget, userIDs[1], day, timeIntervals[1], stepCounts[1]);
                    } catch (Exception e) {
                        errorMsg2 = e.getMessage();
//                        e.printStackTrace();
                    }

                    if (post2Res != null && post2Res.getStatus() >= 200 && post2Res.getStatus() < 300) { //TODO: change
                        stat.increaseRequestSuccessNum();
                        success2 = true;
                        break;
                    }
                }
                if (!success2) {
                    if (post2Res == null) {
                        System.out.println("Failure in post 2:" + errorMsg2);
                    } else {
                        System.out.println("Failure in post 2:" + post2Res.getStatus());
                    }
                }

                if (post2Res != null) {
                    post2Res.close();
                }

                long endTimePost2 = System.currentTimeMillis();

                timeQueue.add(endTimePost2);
                latencyQueue.add(endTimePost2 - startTimePost2);

                //get 1
                stat.increaseRequestNum();
                long startTimeGet1 = System.currentTimeMillis();

                int retry3 = retry;
                boolean success3 = false;
                String errorMsg3 = null;
                while (retry3-- > 0) {
                    try {
                        get1Res = getCurrent(webTarget, userIDs[0]);
                    } catch (Exception e) {
                        errorMsg3 = e.getMessage();
//                        e.printStackTrace();
                    }

                    if (get1Res != null && get1Res.getStatus() >= 200 && get1Res.getStatus() < 300) { //TODO: change
                        stat.increaseRequestSuccessNum();
                        success3 = true;
                        break;
                    }
                }
                if (!success3) {
                    if (get1Res == null) {
                        System.out.println("Failure in get 1: " + errorMsg3);
                    } else {
                        System.out.println("Failure in get 1:" + get1Res.getStatus());
                    }
                }

                if (get1Res != null) {
                    get1Res.close();
                }
                long endTimeGet1 = System.currentTimeMillis();

                timeQueue.add(endTimeGet1);
                latencyQueue.add(endTimeGet1 - startTimeGet1);

                //get 2
                stat.increaseRequestNum();
                long startTimeGet2 = System.currentTimeMillis();

                int retry4 = retry;
                boolean success4 = false;
                String errorMsg4 = null;
                while (retry4-- > 0) {
                    try {
                        get2Res = getSingle(webTarget, userIDs[1], day);
                    } catch (Exception e) {
                        errorMsg4 = e.getMessage();
//                        e.printStackTrace();
                    }

                    if (get2Res != null && get2Res.getStatus() >= 200 && get2Res.getStatus() < 300) { //TODO: change
                        stat.increaseRequestSuccessNum();
                        success4 = true;
                        break;
                    }
                }
                if (!success4) {
                    if (get2Res == null) {
                        System.out.println("Failure in get 2: " + errorMsg4);
                    } else {
                        System.out.println("Failure in get 2:" + get2Res.getStatus());
                    }
                }

                if (get2Res != null) {
                    get2Res.close();
                }

                long endTimeGet2 = System.currentTimeMillis();

                timeQueue.add(endTimeGet2);
                latencyQueue.add(endTimeGet2 - startTimeGet2);

                //post 3
                stat.increaseRequestNum();
                long startTimePost3 = System.currentTimeMillis();

                int retry5 = retry;
                boolean success5 = false;
                String errorMsg5 = null;

                while (retry5-- > 0) {
                    try {
                        post3Res = postStepCount(webTarget, userIDs[2], day, timeIntervals[2], stepCounts[2]);
                    } catch (Exception e) {
                        errorMsg5 = e.getMessage();
//                        e.printStackTrace();
                    }

                    if (post3Res != null && post3Res.getStatus() >= 200 && post3Res.getStatus() < 300) { //TODO: change
                        stat.increaseRequestSuccessNum();
                        success5 = true;
                        break;
                    }
                }
                if (!success5) {
                    if (post3Res == null) {
                        System.out.println("Failure in post 3: null");
                    } else {
                        System.out.println("Failure in post 3:" + post3Res.getStatus());
                    }
                }

                if (post3Res != null) {
                    post3Res.close();
                }
                long endTimePost3 = System.currentTimeMillis();

                timeQueue.add(endTimePost3);
                latencyQueue.add(endTimePost3 - startTimePost3);

            } catch (Throwable t) {
                t.printStackTrace(System.err);
            } finally {

                if (post1Res != null) {
                    post1Res.close();
                }

                if (post2Res != null) {
                    post2Res.close();
                }

                if (get1Res != null) {
                    get1Res.close();
                }
                if (get2Res != null) {
                    get2Res.close();
                }

                if (post3Res != null) {
                    post3Res.close();
                }

            }
        }
        
        if (_latch != null) {
            _latch.countDown();
        }
    }
}
