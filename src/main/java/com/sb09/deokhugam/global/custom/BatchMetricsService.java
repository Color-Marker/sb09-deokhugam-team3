package com.sb09.deokhugam.global.custom;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BatchMetricsService {

  private final MeterRegistry meterRegistry;

  public BatchMetricsService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    preRegister("notification");
  }

  private void preRegister(String batchName) {
    Counter.builder("batch.items.deleted")
        .tag("batch", batchName)
        .register(meterRegistry);

    Counter.builder("batch.failed")
        .tag("batch", batchName)
        .register(meterRegistry);
  }

  public void recordDeleted(String batchName, long count) {
    Counter.builder("batch.items.deleted")
        .tag("batch", batchName)
        .register(meterRegistry)
        .increment(count);
  }

  public void recordFailure(String batchName) {
    Counter.builder("batch.failed")
        .tag("batch", batchName)
        .register(meterRegistry)
        .increment();
  }
}