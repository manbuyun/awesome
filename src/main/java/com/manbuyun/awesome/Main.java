package com.manbuyun.awesome;

import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2018-03-25
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int i = 0;
        while (true) {
            Files.append("i: " + i++ + "\n", new File("/Users/cs/Desktop/1.txt"), StandardCharsets.UTF_8);
            TimeUnit.SECONDS.sleep(3);
        }
    }
}
