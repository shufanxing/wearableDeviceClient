/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client;

import java.util.concurrent.CountDownLatch;
import javax.ws.rs.ClientErrorException;
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

    private WebTarget webTarget = null;
    private Stat stat = null;
    private CountDownLatch _latch = null;
    private int phaseStart;
    private int phaseLength;
    private int iterNum;
    private int day;

    public WorkThread(WebTarget webTarget, Stat stat, CountDownLatch _latch, int phaseStart, int phaseLength, int iterNum, int day) {
        this.webTarget = webTarget;
        this.stat = stat;
        this._latch = _latch;
        this.phaseStart = phaseStart;
        this.phaseLength = phaseLength;
        this.iterNum = iterNum;
        this.day = day;
    }

    public Response postStepCount(WebTarget webTarget, int userID, int day, int timeInterval, int stepCount) {
        return webTarget.path("/" + userID + "/" + day + "/" + timeInterval + "/" + stepCount)
                .request(MediaType.TEXT_PLAIN).post(Entity.entity(null, MediaType.TEXT_PLAIN));
    }

    public Integer getCurrent(WebTarget webTarget, int userID) {
        return webTarget.path("/current/" + userID).request(MediaType.TEXT_PLAIN).get(Integer.class);
    }

    public Integer getSingle(WebTarget webTarget, int userID, int day) {
        return webTarget.path("/single/" + userID + "/" + day).request(MediaType.TEXT_PLAIN).get(Integer.class);
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < iterNum; ++i) {
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

                Response post1Res = null;

                try {
                    post1Res = postStepCount(webTarget, userIDs[0], day, timeIntervals[0], stepCounts[0]);
                } catch (Exception e) {
                    System.out.println("Failure in post 1: " + e.getMessage());
                }

                if (post1Res.getStatus() >= 200 && post1Res.getStatus() < 300) { //TODO: change
                    stat.increaseRequestSuccessNum();
                }
                long endTimePost1 = System.currentTimeMillis();

                stat.addResponseTime(endTimePost1);
                stat.addLatency(endTimePost1 - startTimePost1);

                //post 2
                stat.increaseRequestNum();
                long startTimePost2 = System.currentTimeMillis();

                Response post2Res = null;

                try {
                    post2Res = postStepCount(webTarget, userIDs[1], day, timeIntervals[1], stepCounts[1]);
                } catch (Exception e) {
                    System.out.println("Failure in post 2: " + e.getMessage());
                }

                if (post1Res.getStatus() >= 200 && post1Res.getStatus() < 300) { //TODO: change
                    stat.increaseRequestSuccessNum();
                }
                long endTimePost2 = System.currentTimeMillis();

                stat.addResponseTime(endTimePost2);
                stat.addLatency(endTimePost2 - startTimePost2);

                //get 1
                stat.increaseRequestNum();
                long startTimeGet1 = System.currentTimeMillis();

                Integer get1Res = -1;

                try {
                    get1Res = getCurrent(webTarget, userIDs[0]);
                } catch (Exception e) {
                    System.out.println("Failure in get 1: " + e.getMessage());
                }

                if (get1Res != -1) { //TODO: change
                    stat.increaseRequestSuccessNum();
                }
                long endTimeGet1 = System.currentTimeMillis();

                stat.addResponseTime(endTimeGet1);
                stat.addLatency(endTimeGet1 - startTimeGet1);

                //get 2
                stat.increaseRequestNum();
                long startTimeGet2 = System.currentTimeMillis();

                Integer get2Res = -1;

                try {
                    get2Res = getCurrent(webTarget, userIDs[1]);
                } catch (Exception e) {
                    System.out.println("Failure in get 1: " + e.getMessage());
                }

                if (get2Res != -1) { //TODO: change
                    stat.increaseRequestSuccessNum();
                }
                long endTimeGet2 = System.currentTimeMillis();

                stat.addResponseTime(endTimeGet2);
                stat.addLatency(endTimeGet2 - startTimeGet2);

                //post 3
                stat.increaseRequestNum();
                long startTimePost3 = System.currentTimeMillis();

                Response post3Res = null;

                try {
                    post3Res = postStepCount(webTarget, userIDs[2], day, timeIntervals[2], stepCounts[2]);
                } catch (Exception e) {
                    System.out.println("Failure in post 3: " + e.getMessage());
                }

                if (post1Res.getStatus() >= 200 && post1Res.getStatus() < 300) { //TODO: change
                    stat.increaseRequestSuccessNum();
                }
                long endTimePost3 = System.currentTimeMillis();

                stat.addResponseTime(endTimePost3);
                stat.addLatency(endTimePost3 - startTimePost3);

            }
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        } finally {
            if (_latch != null) {
                _latch.countDown();
            }
        }
    }
}
