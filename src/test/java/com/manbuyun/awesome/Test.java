package com.manbuyun.awesome;

import org.joda.time.DateTime;

/**
 * Description
 * <p>
 * User: zhishui
 * Date: 2018-09-13
 */
public class Test {

    public static void main(String[] args) {
        DateTime dt = new DateTime(1536840533000L);
        System.out.println(dt.minusDays(1).toString());
    }
}
