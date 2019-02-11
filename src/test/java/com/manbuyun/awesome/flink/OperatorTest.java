package com.manbuyun.awesome.flink;

import io.flinkspector.datastream.DataStreamTestBase;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * User: jinhai
 * Date: 2018-10-26
 */
public class OperatorTest extends DataStreamTestBase {

    /**
     * after、before都是相对上一个record来言。
     * fritz: 0~5
     * after(15): 0+15~5+15，也就是15~20
     * before(5): 15-5~20-5，也就是10~15
     * before(5): 10-5~15-5，也就是5~10
     * intoWindow: 就是在15~20窗口
     */
    @Test
    public void test() {
        DataStreamSource<Tuple2<Integer, String>> source = createTimedTestStreamWith(Tuple2.of(1, "fritz"))
                .emit(Tuple2.of(1, "hans"), after(1, TimeUnit.SECONDS))
                .emit(Tuple2.of(1, "heidi"), after(1, TimeUnit.SECONDS))
                .emit(Tuple2.of(1, "hi"), after(5, TimeUnit.SECONDS))
                .close();
    }
}
