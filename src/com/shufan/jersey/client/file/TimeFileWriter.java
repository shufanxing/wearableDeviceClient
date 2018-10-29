/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimeFileWriter implements Runnable {
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    private ConcurrentLinkedQueue<Long> queue;
    private long startTimeTotal;

    public TimeFileWriter(String fileName, ConcurrentLinkedQueue<Long> queue, long startTimeTotal) {
        this.queue = queue;
        this.startTimeTotal = startTimeTotal;

        try {
            this.fileWriter = new FileWriter(fileName);
            this.bufferedWriter = new BufferedWriter(fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        synchronized (queue) {
            while (true) {
                if (!queue.isEmpty()) {
                    try {
                        Long time = queue.poll();

                        if (time == -1L) {
//                            System.out.println("No data anymore, stop write data to file");
                            break;
                        }
                        bufferedWriter.write(Long.toString((time - startTimeTotal) / 1000) + "\n");

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                bufferedWriter.close();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
