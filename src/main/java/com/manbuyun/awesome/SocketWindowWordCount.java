package com.manbuyun.awesome;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeutils.base.IntSerializer;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.util.Collector;

/**
 * Description
 * <p>
 * User: zhishui
 * Date: 2018-08-20
 */
@Slf4j
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
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        CheckpointConfig config = env.getCheckpointConfig();
        config.setCheckpointingMode(CheckpointConfig.DEFAULT_MODE);
        config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        env.setStateBackend(new RocksDBStateBackend("file:///Users/cs/test"));
        env.enableCheckpointing(3000);

        DataStream<String> text = env.socketTextStream("localhost", 9000);

        // 这里的flatmap必须设置String、Collector<WordWithCount>
        // map、flatMap因为类型擦除需要添加returns方法指定Collector的对象
        DataStream<WordWithCount> windowCounts = text.flatMap(new BufferOperator()).returns(WordWithCount.class).keyBy("word").reduce((a, b) -> new WordWithCount(a.word, a.count + b.count));

        windowCounts.addSink(new PrintSinkFunction<>());

        windowCounts.print().setParallelism(3);

        env.execute("Socket Window WordCount");
    }

    public static class BufferOperator implements FlatMapFunction<String, WordWithCount>, CheckpointedFunction {

        private transient ListState<Integer> checkpointedState;

        @Override
        public void flatMap(String value, Collector<WordWithCount> out) throws Exception {
            for (String word : value.split("\\s")) {
                out.collect(new WordWithCount(word, 1L));
            }
        }

        @Override
        public void snapshotState(FunctionSnapshotContext context) throws Exception {
            checkpointedState.clear();
            for (int i = 0; i < 100000; i++) {
                checkpointedState.add(i);
            }
            int i = 0;
        }

        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            ListStateDescriptor<Integer> descriptor = new ListStateDescriptor<>("buffered-elements", IntSerializer.INSTANCE);

            checkpointedState = context.getOperatorStateStore().getListState(descriptor);

            context.isRestored();
        }
    }
}
