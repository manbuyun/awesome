package com.manbuyun.awesome.flink

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.api.scala._
import org.apache.flink.api.scala._

/**
  * @author jinhai
  * @date 2018/12/03
  */
object SqlTest {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tEnv = TableEnvironment.getTableEnvironment(env)

    val orderA: DataStream[Order] = env.fromCollection(Seq(
      Order(1L, "beer", 3),
      Order(1L, "diaper", 4),
      Order(3L, "rubber", 2)))

    val orderB: DataStream[Order] = env.fromCollection(Seq(
      Order(2L, "pen", 3),
      Order(2L, "rubber", 3),
      Order(4L, "beer", 1)))

    // convert DataStream to Table
    var tableA = tEnv.fromDataStream(orderA, 'user, 'product, 'amount)
    // register DataStream as Table
    tEnv.registerDataStream("OrderB", orderB, 'user, 'product, 'amount)

    // union the two tables
    val result = tEnv.sqlQuery(
      s"SELECT * FROM $tableA WHERE amount > 2 UNION ALL " +
        "SELECT * FROM OrderB WHERE amount < 2")

    result.toAppendStream[Order].print()

    env.execute()
  }

  case class Order(user: Long, product: String, amount: Int)

}
