package com.manbuyun.awesome.metrics.source

import com.codahale.metrics.MetricRegistry

/**
  * User: cs
  * Date: 2018-03-15
  */
private[awesome] trait Source {

    def sourceName: String

    def metricRegistry: MetricRegistry
}