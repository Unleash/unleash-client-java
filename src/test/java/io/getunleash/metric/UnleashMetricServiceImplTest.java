package io.getunleash.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class UnleashMetricServiceImplTest {

    @Test
    public void should_register_future_for_sending_interval_regualry() {
        long interval = 10;
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(interval)
                        .unleashAPI("http://unleash.com")
                        .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashMetricService unleashMetricService = new UnleashMetricServiceImpl(config, executor);

        verify(executor, times(1)).setInterval(any(Runnable.class), eq(interval), eq(interval));
    }

    @Test
    public void should_register_client() {
        long interval = 10;
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(interval)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricService unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        Set<String> strategies = new HashSet<>();
        strategies.add("default");
        strategies.add("custom");
        unleashMetricService.register(strategies);

        ArgumentCaptor<ClientRegistration> argument =
                ArgumentCaptor.forClass(ClientRegistration.class);

        verify(sender).registerClient(argument.capture());
        assertThat(argument.getValue().getAppName()).isEqualTo(config.getAppName());
        assertThat(argument.getValue().getInstanceId()).isEqualTo(config.getInstanceId());
        assertThat(argument.getValue().getStarted()).isNotNull();
        assertThat(argument.getValue().getStrategies()).hasSize(2);
        assertThat(argument.getValue().getStrategies()).contains("default", "custom");
    }

    @Test
    public void should_register_client_with_env() {
        long interval = 10;
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .environment("dev")
                        .sendMetricsInterval(interval)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricService unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        Set<String> strategies = new HashSet<>();
        strategies.add("default");
        strategies.add("custom");
        unleashMetricService.register(strategies);

        ArgumentCaptor<ClientRegistration> argument =
                ArgumentCaptor.forClass(ClientRegistration.class);

        verify(sender).registerClient(argument.capture());
        assertThat(argument.getValue().getEnvironment()).isEqualTo(config.getEnvironment());
    }

    @Test
    public void should_send_metrics() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricService unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);

        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());

        sendMetricsCallback.getValue().run();
        verify(sender, times(1)).sendMetrics(any(ClientMetrics.class));
    }

    @Test
    public void should_record_and_send_metrics() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .environment("prod")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricService unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.count("someToggle", true);
        unleashMetricService.count("someToggle", false);
        unleashMetricService.count("someToggle", true);
        unleashMetricService.count("otherToggle", true);

        // Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        sendMetricsCallback.getValue().run();

        ArgumentCaptor<ClientMetrics> clientMetricsArgumentCaptor =
                ArgumentCaptor.forClass(ClientMetrics.class);
        verify(sender).sendMetrics(clientMetricsArgumentCaptor.capture());

        ClientMetrics clientMetrics = clientMetricsArgumentCaptor.getValue();
        MetricsBucket bucket = clientMetricsArgumentCaptor.getValue().getBucket();

        assertThat(clientMetrics.getAppName()).isEqualTo(config.getAppName());
        assertThat(clientMetrics.getEnvironment()).isEqualTo(config.getEnvironment());
        assertThat(clientMetrics.getInstanceId()).isEqualTo(config.getInstanceId());
        assertThat(bucket.getStart()).isNotNull();
        assertThat(bucket.getStop()).isNotNull();
        assertThat(bucket.getToggles()).hasSize(2);
        assertThat(bucket.getToggles().get("someToggle").getYes()).isEqualTo(2l);
        assertThat(bucket.getToggles().get("someToggle").getNo()).isEqualTo(1l);
    }

    @Test
    public void should_record_and_send_variant_metrics() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricService unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v2");
        unleashMetricService.countVariant("someToggle", "disabled");

        // Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        sendMetricsCallback.getValue().run();

        ArgumentCaptor<ClientMetrics> clientMetricsArgumentCaptor =
                ArgumentCaptor.forClass(ClientMetrics.class);
        verify(sender).sendMetrics(clientMetricsArgumentCaptor.capture());

        ClientMetrics clientMetrics = clientMetricsArgumentCaptor.getValue();
        MetricsBucket bucket = clientMetricsArgumentCaptor.getValue().getBucket();

        assertThat(clientMetrics.getAppName()).isEqualTo(config.getAppName());
        assertThat(clientMetrics.getInstanceId()).isEqualTo(config.getInstanceId());
        assertThat(bucket.getStart()).isNotNull();
        assertThat(bucket.getStop()).isNotNull();
        assertThat(bucket.getToggles()).hasSize(1);
        assertThat(bucket.getToggles().get("someToggle").getVariants().get("v1").longValue())
                .isEqualTo(3l);
        assertThat(bucket.getToggles().get("someToggle").getVariants().get("v2").longValue())
                .isEqualTo(1l);
        assertThat(bucket.getToggles().get("someToggle").getVariants().get("disabled").longValue())
                .isEqualTo(1l);
        assertThat(bucket.getToggles().get("someToggle").getYes()).isEqualTo(0l);
        assertThat(bucket.getToggles().get("someToggle").getNo()).isEqualTo(0l);
    }

    @Test
    public void should_backoff_when_told_to_by_429_code() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricServiceImpl unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v2");
        unleashMetricService.countVariant("someToggle", "disabled");

        // Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());

        when(sender.sendMetrics(any(ClientMetrics.class)))
                .thenReturn(429)
                .thenReturn(429)
                .thenReturn(429)
                .thenReturn(200)
                .thenReturn(200)
                .thenReturn(200)
                .thenReturn(200);

        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(1);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(2);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run(); // NO-OP because interval > 0
        sendMetricsCallback.getValue().run(); // NO-OP because interval > 0
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(3);
        assertThat(unleashMetricService.getFailures()).isEqualTo(3);
        sendMetricsCallback.getValue().run();
        sendMetricsCallback.getValue().run();
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(3);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(2);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run();
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(1);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(0);
    }

    @Test
    public void server_errors_should_also_incrementally_backoff() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricServiceImpl unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v2");
        unleashMetricService.countVariant("someToggle", "disabled");

        // Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        when(sender.sendMetrics(any(ClientMetrics.class)))
                .thenReturn(500)
                .thenReturn(502)
                .thenReturn(503)
                .thenReturn(304)
                .thenReturn(304)
                .thenReturn(304)
                .thenReturn(304);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(1);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(2);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run(); // NO-OP because interval > 0
        sendMetricsCallback.getValue().run(); // NO-OP because interval > 0
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(3);
        assertThat(unleashMetricService.getFailures()).isEqualTo(3);
        sendMetricsCallback.getValue().run();
        sendMetricsCallback.getValue().run();
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(3);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(2);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run();
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(2);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(1);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        assertThat(unleashMetricService.getFailures()).isEqualTo(0);
    }

    @Test
    public void failure_to_authenticate_immediately_increases_interval_to_max() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricServiceImpl unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v2");
        unleashMetricService.countVariant("someToggle", "disabled");

        // Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        when(sender.sendMetrics(any(ClientMetrics.class))).thenReturn(403).thenReturn(200);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(30);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        for (int i = 0; i < 30; i++) {
            sendMetricsCallback.getValue().run();
        }
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getFailures()).isEqualTo(0);
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
    }

    @Test
    public void url_not_found_immediately_increases_interval_to_max() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricServiceImpl unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v2");
        unleashMetricService.countVariant("someToggle", "disabled");

        // Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        when(sender.sendMetrics(any(ClientMetrics.class))).thenReturn(404).thenReturn(200);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getSkips()).isEqualTo(30);
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        for (int i = 0; i < 30; i++) {
            sendMetricsCallback.getValue().run();
        }
        assertThat(unleashMetricService.getFailures()).isEqualTo(1);
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
        sendMetricsCallback.getValue().run();
        assertThat(unleashMetricService.getFailures()).isEqualTo(0);
        assertThat(unleashMetricService.getSkips()).isEqualTo(0);
    }

    @Test
    public void should_add_new_metrics_data_to_bucket() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        DefaultHttpMetricsSender sender = mock(DefaultHttpMetricsSender.class);

        UnleashMetricService unleashMetricService =
                new UnleashMetricServiceImpl(config, sender, executor);

        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());

        sendMetricsCallback.getValue().run();
        ArgumentCaptor<ClientMetrics> metricsSent = ArgumentCaptor.forClass(ClientMetrics.class);
        verify(sender, times(1)).sendMetrics(metricsSent.capture());
        ClientMetrics metrics = metricsSent.getValue();
        assertThat(metrics.getSpecVersion()).isNotEmpty();
        assertThat(metrics.getYggdrasilVersion()).isNull();
        assertThat(metrics.getPlatformName()).isNotEmpty();
        assertThat(metrics.getPlatformVersion()).isNotEmpty();
    }

    @Test
    public void client_registration_also_includes_new_metrics_metadata() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .sendMetricsInterval(10)
                        .unleashAPI("http://unleash.com")
                        .build();
        Set<String> strategies = new HashSet<>();
        strategies.add("default");
        ClientRegistration reg = new ClientRegistration(config, LocalDateTime.now(), strategies);
        assertThat(reg.getPlatformName()).isNotEmpty();
        assertThat(reg.getPlatformVersion()).isNotEmpty();
        assertThat(reg.getSpecVersion()).isEqualTo(config.getClientSpecificationVersion());
        assertThat(reg.getYggdrasilVersion()).isNull();
    }
}
