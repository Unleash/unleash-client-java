package no.finn.unleash.metric;

import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UnleashMetricServiceImplTest {

    @Test
    public void should_register_future_for_sending_interval_regualry() {
        long interval = 10;
        UnleashConfig config = UnleashConfig
                .builder()
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
        UnleashConfig config = UnleashConfig
                .builder()
                .appName("test")
                .sendMetricsInterval(interval)
                .unleashAPI("http://unleash.com")
                .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashMetricsSender sender = mock(UnleashMetricsSender.class);

        UnleashMetricService unleashMetricService = new UnleashMetricServiceImpl(config, sender, executor);
        Set<String> strategies = new HashSet<>();
        strategies.add("default");
        strategies.add("custom");
        unleashMetricService.register(strategies);

        ArgumentCaptor<ClientRegistration> argument = ArgumentCaptor.forClass(ClientRegistration.class);

        verify(sender).registerClient(argument.capture());
        assertThat(argument.getValue().getAppName()).isEqualTo(config.getAppName());
        assertThat(argument.getValue().getInstanceId()).isEqualTo(config.getInstanceId());
        assertThat(argument.getValue().getStarted()).isNotNull();
        assertThat(argument.getValue().getStrategies()).hasSize(2);
        assertThat(argument.getValue().getStrategies()).contains("default", "custom");
    }


    @Test
    public void should_send_metrics() {
        UnleashConfig config = UnleashConfig
                .builder()
                .appName("test")
                .sendMetricsInterval(10)
                .unleashAPI("http://unleash.com")
                .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashMetricsSender sender = mock(UnleashMetricsSender.class);

        UnleashMetricService unleashMetricService = new UnleashMetricServiceImpl(config, sender, executor);

        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());


        sendMetricsCallback.getValue().run();
        verify(sender, times(1)).sendMetrics(any(ClientMetrics.class));
    }

    @Test
    public void should_record_and_send_metrics() {
        UnleashConfig config = UnleashConfig
                .builder()
                .appName("test")
                .sendMetricsInterval(10)
                .unleashAPI("http://unleash.com")
                .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashMetricsSender sender = mock(UnleashMetricsSender.class);

        UnleashMetricService unleashMetricService = new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.count("someToggle", true);
        unleashMetricService.count("someToggle", false);
        unleashMetricService.count("someToggle", true);
        unleashMetricService.count("otherToggle", true);

        //Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        sendMetricsCallback.getValue().run();

        ArgumentCaptor<ClientMetrics> clientMetricsArgumentCaptor = ArgumentCaptor.forClass(ClientMetrics.class);
        verify(sender).sendMetrics(clientMetricsArgumentCaptor.capture());


        ClientMetrics clientMetrics = clientMetricsArgumentCaptor.getValue();
        MetricsBucket bucket = clientMetricsArgumentCaptor.getValue().getBucket();

        assertThat(clientMetrics.getAppName()).isEqualTo(config.getAppName());
        assertThat(clientMetrics.getInstanceId()).isEqualTo(config.getInstanceId());
        assertThat(bucket.getStart()).isNotNull();
        assertThat(bucket.getStop()).isNotNull();
        assertThat(bucket.getToggles()).hasSize(2);
        assertThat(bucket.getToggles().get("someToggle").getYes()).isEqualTo(2l);
        assertThat(bucket.getToggles().get("someToggle").getNo()).isEqualTo(1l);
    }

    @Test
    public void should_record_and_send_variant_metrics() {
        UnleashConfig config = UnleashConfig
                .builder()
                .appName("test")
                .sendMetricsInterval(10)
                .unleashAPI("http://unleash.com")
                .build();

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashMetricsSender sender = mock(UnleashMetricsSender.class);

        UnleashMetricService unleashMetricService = new UnleashMetricServiceImpl(config, sender, executor);
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v1");
        unleashMetricService.countVariant("someToggle", "v2");
        unleashMetricService.countVariant("someToggle", "disabled");

        //Call the sendMetricsCallback
        ArgumentCaptor<Runnable> sendMetricsCallback = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).setInterval(sendMetricsCallback.capture(), anyLong(), anyLong());
        sendMetricsCallback.getValue().run();

        ArgumentCaptor<ClientMetrics> clientMetricsArgumentCaptor = ArgumentCaptor.forClass(ClientMetrics.class);
        verify(sender).sendMetrics(clientMetricsArgumentCaptor.capture());


        ClientMetrics clientMetrics = clientMetricsArgumentCaptor.getValue();
        MetricsBucket bucket = clientMetricsArgumentCaptor.getValue().getBucket();

        assertThat(clientMetrics.getAppName()).isEqualTo(config.getAppName());
        assertThat(clientMetrics.getInstanceId()).isEqualTo(config.getInstanceId());
        assertThat(bucket.getStart()).isNotNull();
        assertThat(bucket.getStop()).isNotNull();
        assertThat(bucket.getToggles()).hasSize(1);
        assertThat(bucket.getToggles().get("someToggle").getVariants().get("v1").longValue()).isEqualTo(3l);
        assertThat(bucket.getToggles().get("someToggle").getVariants().get("v2").longValue()).isEqualTo(1l);
        assertThat(bucket.getToggles().get("someToggle").getVariants().get("disabled").longValue()).isEqualTo(1l);
        assertThat(bucket.getToggles().get("someToggle").getYes()).isEqualTo(0l);
        assertThat(bucket.getToggles().get("someToggle").getNo()).isEqualTo(0l);
    }

}