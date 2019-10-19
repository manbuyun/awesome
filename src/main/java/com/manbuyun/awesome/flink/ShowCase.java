package com.manbuyun.awesome.flink;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author jinhai
 * @date 2019/09/09
 */
public class ShowCase {

    private static final String GROUP = "test";

    private static final String CONSUMER_TOPIC = "consumer_test";
    private static final String PRODUCER_TOPIC = "producer_test";

    private static final int SOURCE_PARALLELISM = 10;
    private static final int SINK_PARALLELISM = 10;

    public static void main(String[] args) throws Exception {
        ParameterTool parameter = ParameterTool.fromArgs(args);
        String jobName = parameter.get("job.name") != null ? parameter.get("job.name") : "test";

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 禁止流计算时打印sout日志，并且设置失败重启策略：每10s重启一次，执行n次
        ExecutionConfig config = env.getConfig();
        config.disableSysoutLogging();
        config.setRestartStrategy(RestartStrategies.fixedDelayRestart(Integer.MAX_VALUE, Time.seconds(10)));

        // 快照，保存状态信息。状态保存在hdfs，服务端已经设置好保存路径
        env.enableCheckpointing(Time.seconds(300).toMilliseconds());
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE);
        checkpointConfig.setMinPauseBetweenCheckpoints(Time.seconds(180).toMilliseconds());
        checkpointConfig.setCheckpointTimeout(Time.seconds(300).toMilliseconds());
        checkpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        // 根据kafka topic partition数来设置，最好是倍数。比如partition数是32，则并发度可以是16，也可以设32。但不能超过32
        env.setParallelism(SOURCE_PARALLELISM);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", GROUP);

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(CONSUMER_TOPIC, new SimpleStringSchema(), properties);
        DataStream<String> source = env.addSource(consumer);

        DataStream<String> result = source.map(log -> {
            return log;
        });

        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<>(PRODUCER_TOPIC, (log, timestamp) -> {
            return new ProducerRecord<>(PRODUCER_TOPIC, log.getBytes(StandardCharsets.UTF_8));
        }, properties, FlinkKafkaProducer.Semantic.AT_LEAST_ONCE);
        // 设置sink并发度为topic分区数: FlinkFixedPartitioner.class
        result.addSink(producer).setParallelism(SINK_PARALLELISM);

        env.execute(jobName);
    }

    @Test
    public void produce() {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>(CONSUMER_TOPIC, ""));
    }
}