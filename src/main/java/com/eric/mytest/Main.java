package com.eric.mytest;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static long beginExecuteTime = 0;
    private static long totalExecuteTime = 0;

    private static long timeResult[][] = new long[ConcurrencyTest.USER_COUNTS][ConcurrencyTest.USER_REPEAT_COUNTS];
    private static boolean testResult[][] = new boolean[ConcurrencyTest.USER_COUNTS][ConcurrencyTest.USER_REPEAT_COUNTS];

    private static int remainCompleteUserCounts = ConcurrencyTest.USER_COUNTS;
    private static File logFile;

    public static boolean printLogLn(String log) {
        return printLog(log + "\n");
    }

    public static boolean printLog(String log) {
        try {
            FileWriter fw = new FileWriter(logFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            System.out.print(log);
            pw.print(log);
            pw.close();
            bw.close();
            fw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    private synchronized static void setComplete() {

        remainCompleteUserCounts--;
        if (remainCompleteUserCounts > 0) {
            return;
        }

        totalExecuteTime = System.currentTimeMillis() - beginExecuteTime;

        printLog("\n\n");

        printLogLn("userCounts=" + ConcurrencyTest.USER_COUNTS);
        printLogLn("userStartInterval=" + ConcurrencyTest.USER_START_INTERVAL);
        printLogLn("userRepeatCounts=" + ConcurrencyTest.USER_REPEAT_COUNTS);
        printLogLn("userRepeatInterval=" + ConcurrencyTest.USER_REPEAT_INTERVAL);
        printLogLn("Time statistics without failed");
        printLog("\n");

        printLog(String.format("%-11s\t\t", "user"));
        for (int i = 0; i < ConcurrencyTest.USER_REPEAT_COUNTS; i++) {
            printLog(String.format("Repeat%-5d\t\t", i));
        }
        printLog(String.format("%-11s\t\t%-11s\t\t%-11s\t\t%-11s", "max", "min", "avg", "FailRate"));


        printLog("\n");


        long maxTime = 0;
        long minTime = Long.MAX_VALUE;
        long totalTime = 0;
        long failCount = 0;


        for (int i = 0; i < ConcurrencyTest.USER_COUNTS; i++) {
            printLog(String.format("%-11d\t\t", i));

            long userMaxTime = 0;
            long userMinTime = Long.MAX_VALUE;
            long userTotalTime = 0;
            long userFailCount = 0;
            for (int j = 0; j < ConcurrencyTest.USER_REPEAT_COUNTS; j++) {

                if (testResult[i][j]) {

                    if (userMaxTime < timeResult[i][j]) {
                        userMaxTime = timeResult[i][j];
                    }


                    if (userMinTime > timeResult[i][j]) {
                        userMinTime = timeResult[i][j];
                    }

                    userTotalTime += timeResult[i][j];
                } else {
                    userFailCount++;
                }

                printLog(String.format("%-5s|%-5d\t\t", testResult[i][j], timeResult[i][j]));
            }


            if (maxTime < userMaxTime) {
                maxTime = userMaxTime;
            }

            if (minTime > userMinTime) {
                minTime = userMinTime;
            }

            totalTime += userTotalTime;

            failCount += userFailCount;

            printLog(String.format("%-11d\t\t%-11d\t\t%-11d\t\t%f%%", userMaxTime, userMinTime == Long.MAX_VALUE ? 0 : userMinTime, userFailCount == ConcurrencyTest.USER_REPEAT_COUNTS ? 0 : userTotalTime / (ConcurrencyTest.USER_REPEAT_COUNTS - userFailCount), (float) userFailCount / ConcurrencyTest.USER_REPEAT_COUNTS * 100));

            printLog("\n");
        }

        printLog(String.format("Total Max:" + maxTime + " Min:" + (minTime == Long.MAX_VALUE ? 0 : minTime) + " Avg:" + (failCount == ConcurrencyTest.USER_COUNTS * ConcurrencyTest.USER_REPEAT_COUNTS ? 0 : totalTime / (ConcurrencyTest.USER_COUNTS * ConcurrencyTest.USER_REPEAT_COUNTS - failCount)) + " FailRate:%f%%", (float) failCount / (ConcurrencyTest.USER_COUNTS * ConcurrencyTest.USER_REPEAT_COUNTS) * 100));

        printLog("\n\n");
        printLog(String.format("Total Execute Time:" + totalExecuteTime + "\n"));
        printLogLn("Log to " + logFile.getAbsolutePath());


    }


    static class TestThread extends Thread {
        int user;

        public TestThread(int user) {
            this.user = user;
        }

        @Override
        public void run() {
            for (int i = 0; i < ConcurrencyTest.USER_REPEAT_COUNTS; i++) {
                printLog(String.format("User " + user + "'s repeat " + i + " test start\n"));
                boolean result = false;
                long startTime = System.currentTimeMillis();
                try {
                    result = ConcurrencyTest.test(user, i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                long time = System.currentTimeMillis() - startTime;
                printLog(String.format("User " + user + "'s repeat " + i + " test end. TestResult:" + result + " TimeCosume:" + time + "\n"));
                timeResult[user][i] = time;
                testResult[user][i] = result;

                if (ConcurrencyTest.USER_REPEAT_INTERVAL > 0) {
                    try {
                        Thread.sleep(ConcurrencyTest.USER_REPEAT_INTERVAL);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }

            setComplete();


        }

    }


    public static void main(String[] args) {

        beginExecuteTime = System.currentTimeMillis();

        logFile = new File(ConcurrencyTest.LOG_PATH + "ConcurrencyTest_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".log");
        if (!logFile.exists()) {
            if (!logFile.getParentFile().exists()) {


                logFile.getParentFile().mkdirs();
            }
            try {

                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {

            for (int i = 0; i < ConcurrencyTest.USER_COUNTS; i++) {
                TestThread testThread = new TestThread(i);
                testThread.start();
                if (ConcurrencyTest.USER_START_INTERVAL > 0) {
                    try {
                        Thread.sleep(ConcurrencyTest.USER_START_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}