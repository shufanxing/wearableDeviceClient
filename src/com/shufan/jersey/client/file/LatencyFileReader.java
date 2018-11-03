/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.shufan.jersey.client.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author frg
 */
public class LatencyFileReader {

    private static FileReader fileReader;
    private static BufferedReader bufferedReader;

    public static void read(String fileName, List<Long> list) {
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String line = null;

        while (true) {
            synchronized (bufferedReader) {
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        list.add(Long.parseLong(line));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (line == null) {
                break;
            }
        }
        try {
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
