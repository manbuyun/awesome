package com.manbuyun.awesome.flink;

import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.junit.jupiter.api.Test;

/**
 * @author jinhai
 * @date 2018/12/05
 */
public class RetractTest {

    /**
     * 2> (true,Word{count=1, frequency=1})
     * 2> (false,Word{count=1, frequency=1})
     * 2> (true,Word{count=1, frequency=2})
     * 2> (false,Word{count=1, frequency=2})
     * 2> (true,Word{count=1, frequency=3})
     * 2> (false,Word{count=1, frequency=3})
     * 2> (true,Word{count=1, frequency=2})
     * 1> (true,Word{count=2, frequency=1})
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.getTableEnvironment(env);
        env.getConfig().disableSysoutLogging();

        DataStream<Tuple2<String, Integer>> dataStream = env.fromElements(
                new Tuple2<>("hello", 1),
                new Tuple2<>("word", 1),
                new Tuple2<>("bark", 1),
                new Tuple2<>("hello", 1)
        );
        tEnv.registerDataStream("demo", dataStream, "word, num");

        Table table = tEnv.sqlQuery("select * from demo").groupBy("word")
                .select("word AS word, num.sum AS count")
                .groupBy("count").select("count, word.count as frequency");
        tEnv.toRetractStream(table, Word.class).print();

        env.execute("demo");
    }

    @Getter
    @Setter
    public static class Word {
        private Integer count;
        private Long frequency;

        @Override
        public String toString() {
            return "Word{" + "count=" + count + ", frequency=" + frequency + '}';
        }
    }
}
