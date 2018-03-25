package com.manbuyun.awesome.metrics.sink

/**
  * User: cs
  * Date: 2018-03-15
  */
private[awesome] trait Sink {

    def start

    def stop

    def report
}