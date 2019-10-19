package com.manbuyun.awesome.flink;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author jinhai
 * @date 2019/09/09
 */
@Slf4j
public class ShowCaseTest {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        ExecutionConfig config = env.getConfig();
        config.setRestartStrategy(RestartStrategies.fixedDelayRestart(300, Time.seconds(10)));
        config.disableSysoutLogging();
        env.enableCheckpointing(Time.seconds(300).toMilliseconds());
        env.setParallelism(1);

        DataStream<String> source = env.fromElements("1", "2", "3", "4", "5", "6", "7", "8");

        source.map(s -> {
            if (s.equals("4")) {
                throw new RuntimeException("test");
            }
            return s;
        }).print();

//        ((DataStreamSource<String>) source).filter(String::isEmpty).setParallelism(4).print().setParallelism(4);
//        source.map(String::toString).uid("4").setParallelism(3).print().uid("5").setParallelism(4);

        env.execute("test");
    }
}