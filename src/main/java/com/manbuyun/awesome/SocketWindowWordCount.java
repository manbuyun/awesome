package com.manbuyun.awesome;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * Description
 * <p>
 * User: zhishui
 * Date: 2018-08-20
 */
public class SocketWindowWordCount {

    public static class WordWithCount {
        public String word;
        public long count;

        public WordWithCount() {
        }

        public WordWithCount(String word, long count) {
            this.word = word;
            this.count = count;
        }

        @Override
        public String toString() {
            return word + " : " + count;
        }
    }

    public static void main(String[] args) throws Exception {
        ParameterTool params = ParameterTool.fromArgs(args);
        int port = params.getInt("port");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> text = env.socketTextStream("localhost", port, "\n");

        // 这里的flatmap必须设置String、Collector<WordWithCount>
        // map、flatMap因为类型擦除需要添加returns方法指定Collector的对象
        DataStream<WordWithCount> windowCounts = text.flatMap((String value, Collector<WordWithCount> out) -> {
            for (String word : value.split("\\s")) {
                out.collect(new WordWithCount(word, 1L));
            }
        }).returns(WordWithCount.class).keyBy("word").timeWindow(Time.seconds(5), Time.seconds(1)).reduce((a, b) -> new WordWithCount(a.word, a.count + b.count));

        windowCounts.print().setParallelism(1);

        env.execute("Socket Window WordCount");
    }
}
