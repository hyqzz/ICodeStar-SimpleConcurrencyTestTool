package com.eric.mytest;

public class ConcurrencyTest {

    public static final int USER_COUNTS = 10;
    public static final int USER_START_INTERVAL = 0;
    public static final int USER_REPEAT_COUNTS = 1;
    public static final int USER_REPEAT_INTERVAL = 0;
    public static final String LOG_PATH = "D:\\MyTest\\";

    public static boolean test(int user, int repeat) throws Exception {
        Thread.sleep((long)(Math.random()*5000));
        return (int) (Math.random() * 2) != 0;


    }
}
