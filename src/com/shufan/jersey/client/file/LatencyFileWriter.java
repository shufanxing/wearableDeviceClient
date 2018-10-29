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

public class LatencyFileWriter implements Runnable {
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    private ConcurrentLinkedQueue<Long> queue;

    public LatencyFileWriter(String fileName, ConcurrentLinkedQueue<Long> queue) {
        this.queue = queue;

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
                        Long latency = queue.poll();

                        if (latency == -1L) {
//                            System.out.println("No data anymore, stop write data to file");
                            break;
                        }
                        bufferedWriter.write(Long.toString(latency) + "\n");

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
