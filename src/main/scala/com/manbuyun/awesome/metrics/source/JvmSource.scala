package com.manbuyun.awesome.metrics.source

import java.lang.management.ManagementFactory

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jvm.{BufferPoolMetricSet, GarbageCollectorMetricSet, MemoryUsageGaugeSet}

/**
  * User: cs
  * Date: 2018-03-15
  */
private[awesome] class JvmSource extends Source {

    override def sourceName: String = "jvm"

    override def metricRegistry: MetricRegistry = new MetricRegistry()

    metricRegistry.registerAll(new GarbageCollectorMetricSet)
    metricRegistry.registerAll(new MemoryUsageGaugeSet)
    metricRegistry.registerAll(new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer))
}