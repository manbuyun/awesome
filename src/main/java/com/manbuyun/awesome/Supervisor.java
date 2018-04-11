package com.manbuyun.awesome;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: cs
 * Date: 2018-03-25
 */
public class Supervisor {

    public static void main(String[] args) throws Exception {
        List<String> commands = new ArrayList<>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("java -jar /Users/cs/iProject/awesome/Main.jar");

        ProcessBuilder builder = new ProcessBuilder(commands);
        builder.start();

        TimeUnit.SECONDS.sleep(300);
    }
}
