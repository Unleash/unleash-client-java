package no.finn.unleash.metric;

import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        assertThat(argument.getValue().getAppName(), is(config.getAppName()));
        assertThat(argument.getValue().getInstanceId(), is(config.getInstanceId()));
        assertThat(argument.getValue().getStarted(), is(not(IsNull.nullValue())));
        assertThat(argument.getValue().getStrategies().size(), is(2));
        assertTrue(argument.getValue().getStrategies().contains("default"));
        assertTrue(argument.getValue().getStrategies().contains("custom"));
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

        assertThat(clientMetrics.getAppName(), is(config.getAppName()));
        assertThat(clientMetrics.getInstanceId(), is(config.getInstanceId()));
        assertNotNull(bucket.getStart());
        assertNotNull(bucket.getStop());
        assertThat(bucket.getToggles().size(), is(2));
        assertThat(bucket.getToggles().get("someToggle").getYes(), is(2l));
        assertThat(bucket.getToggles().get("someToggle").getNo(), is(1l));
    }

}