package io.getunleash.event;

import static org.assertj.core.api.Assertions.assertThat;

import io.getunleash.UnleashContext;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.Test;

class EventDispatcherTest {

    /** Records what was handed to the executor instead of running it on another thread. */
    private static class RecordingExecutor implements UnleashScheduledExecutor {
        private final List<Runnable> scheduled = new ArrayList<>();

        @Override
        public void setInterval(Runnable command, long initialDelaySec, long periodSec)
                throws RejectedExecutionException {}

        @Override
        public Future<Void> scheduleOnce(Runnable runnable) {
            scheduled.add(runnable);
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class OnlyOnReady implements UnleashSubscriber {
        private final List<UnleashEvent> received = new ArrayList<>();

        @Override
        public void onReady(UnleashReady unleashReady) {
            received.add(unleashReady);
        }
    }

    private static class OnlyToggleEvaluated implements UnleashSubscriber {
        private final List<ToggleEvaluated> received = new ArrayList<>();

        @Override
        public void toggleEvaluated(ToggleEvaluated toggleEvaluated) {
            received.add(toggleEvaluated);
        }
    }

    private static class OnlyImpressions implements UnleashSubscriber {
        private final List<ImpressionEvent> received = new ArrayList<>();

        @Override
        public void impression(ImpressionEvent impressionEvent) {
            received.add(impressionEvent);
        }
    }

    private static class CatchAll implements UnleashSubscriber {
        private final List<UnleashEvent> received = new ArrayList<>();

        @Override
        public void on(UnleashEvent unleashEvent) {
            received.add(unleashEvent);
        }
    }

    /** Guards against gating on the exact NoOpSubscriber class instead of the callbacks. */
    private static class NoOpSubclassThatListens extends NoOpSubscriber {
        private final List<ToggleEvaluated> received = new ArrayList<>();

        @Override
        public void toggleEvaluated(ToggleEvaluated toggleEvaluated) {
            received.add(toggleEvaluated);
        }
    }

    private RecordingExecutor executor;

    private EventDispatcher dispatcherFor(UnleashSubscriber subscriber) {
        executor = new RecordingExecutor();
        return new EventDispatcher(
                new UnleashConfig.Builder()
                        .appName("EventDispatcherTest")
                        .unleashAPI("http://localhost:4242/api")
                        .subscriber(subscriber)
                        .scheduledExecutor(executor)
                        .build());
    }

    private static ImpressionEvent impressionEvent() {
        return new IsEnabledImpressionEvent("some-toggle", true, UnleashContext.builder().build());
    }

    @Test
    void defaultSubscriberGetsNoPerEvaluationEvents() {
        EventDispatcher dispatcher = dispatcherFor(new NoOpSubscriber());

        assertThat(dispatcher.isSubscribedTo(ToggleEvaluated.class)).isFalse();
        assertThat(dispatcher.isSubscribedTo(IsEnabledImpressionEvent.class)).isFalse();
        assertThat(dispatcher.isSubscribedTo(VariantImpressionEvent.class)).isFalse();

        dispatcher.dispatch(new ToggleEvaluated("some-toggle", true));
        dispatcher.dispatch(impressionEvent());

        assertThat(executor.scheduled).isEmpty();
    }

    @Test
    void subscriberListeningOnlyForReadyGetsNoPerEvaluationEvents() {
        OnlyOnReady subscriber = new OnlyOnReady();
        EventDispatcher dispatcher = dispatcherFor(subscriber);

        assertThat(dispatcher.isSubscribedTo(ToggleEvaluated.class)).isFalse();
        assertThat(dispatcher.isSubscribedTo(IsEnabledImpressionEvent.class)).isFalse();

        dispatcher.dispatch(new ToggleEvaluated("some-toggle", true));
        dispatcher.dispatch(impressionEvent());
        assertThat(executor.scheduled).isEmpty();

        dispatcher.dispatch(new UnleashReady());
        assertThat(subscriber.received).hasSize(1);
    }

    @Test
    void subscriberListeningForToggleEvaluatedStillGetsIt() {
        OnlyToggleEvaluated subscriber = new OnlyToggleEvaluated();
        EventDispatcher dispatcher = dispatcherFor(subscriber);

        assertThat(dispatcher.isSubscribedTo(ToggleEvaluated.class)).isTrue();
        assertThat(dispatcher.isSubscribedTo(IsEnabledImpressionEvent.class)).isFalse();

        dispatcher.dispatch(new ToggleEvaluated("some-toggle", true));
        dispatcher.dispatch(impressionEvent());

        assertThat(subscriber.received).hasSize(1);
        assertThat(executor.scheduled).hasSize(1);
    }

    @Test
    void subscriberListeningForImpressionsStillGetsThem() {
        OnlyImpressions subscriber = new OnlyImpressions();
        EventDispatcher dispatcher = dispatcherFor(subscriber);

        assertThat(dispatcher.isSubscribedTo(ToggleEvaluated.class)).isFalse();
        assertThat(dispatcher.isSubscribedTo(IsEnabledImpressionEvent.class)).isTrue();
        assertThat(dispatcher.isSubscribedTo(VariantImpressionEvent.class)).isTrue();

        dispatcher.dispatch(new ToggleEvaluated("some-toggle", true));
        dispatcher.dispatch(impressionEvent());
        dispatcher.dispatch(
                new VariantImpressionEvent(
                        "some-toggle", true, UnleashContext.builder().build(), "blue"));

        assertThat(subscriber.received).hasSize(2);
    }

    @Test
    void subscriberListeningOnTheCatchAllGetsEverything() {
        CatchAll subscriber = new CatchAll();
        EventDispatcher dispatcher = dispatcherFor(subscriber);

        assertThat(dispatcher.isSubscribedTo(ToggleEvaluated.class)).isTrue();
        assertThat(dispatcher.isSubscribedTo(IsEnabledImpressionEvent.class)).isTrue();

        dispatcher.dispatch(new ToggleEvaluated("some-toggle", true));
        dispatcher.dispatch(impressionEvent());

        assertThat(subscriber.received).hasSize(2);
    }

    @Test
    void noOpSubscriberSubclassThatOverridesACallbackIsStillNotified() {
        NoOpSubclassThatListens subscriber = new NoOpSubclassThatListens();
        EventDispatcher dispatcher = dispatcherFor(subscriber);

        assertThat(dispatcher.isSubscribedTo(ToggleEvaluated.class)).isTrue();

        dispatcher.dispatch(new ToggleEvaluated("some-toggle", true));

        assertThat(subscriber.received).hasSize(1);
    }

    @Test
    void eventTypesOutsideTheEvaluationPathAlwaysDispatch() {
        EventDispatcher dispatcher = dispatcherFor(new NoOpSubscriber());

        assertThat(dispatcher.isSubscribedTo(UnleashReady.class)).isTrue();

        dispatcher.dispatch(new UnleashReady());

        assertThat(executor.scheduled).hasSize(1);
    }
}
