package io.getunleash.util;

import io.getunleash.metric.MetricSender;
import java.util.function.Function;

public interface MetricSenderFactory extends Function<UnleashConfig, MetricSender> {}
