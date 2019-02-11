package com.manbuyun.awesome.flink;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jinhai
 * @date 2018/12/03
 */
public class TableTest {

    @Test
    public void test() throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);

        DataStream<Tuple3<Integer, Long, String>> dataStream = getTupleDataStream(env);
        tableEnv.registerDataStream("myTable", dataStream, "a, b, c");

        Table result = tableEnv.sqlQuery("select c from myTable");
        DataStream<Row> resultSet = tableEnv.toAppendStream(result, Row.class);

        System.out.println(resultSet.print());

        env.execute();
    }

    public static DataStream<Tuple3<Integer, Long, String>> getTupleDataStream(StreamExecutionEnvironment env) {
        List<Tuple3<Integer, Long, String>> data = new ArrayList<>();
        data.add(new Tuple3<>(1, 1L, "Hi"));
        data.add(new Tuple3<>(2, 2L, "Hello"));
        data.add(new Tuple3<>(3, 2L, "Hello world"));
        Collections.shuffle(data);
        return env.fromCollection(data);
    }
}
