/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.getunleash;

import io.getunleash.repository.ToggleBootstrapFileProvider;
import io.getunleash.util.UnleashConfig;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Warmup(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
public class UnleashClientBenchmark {

    @State(Scope.Thread)
    public static class MyState {

        public Unleash unleash;
        public UnleashContext context;

        @Setup(Level.Trial)
        public void doSetup() {
            System.out.println("dosetup");
            unleash = new DefaultUnleash(UnleashConfig.builder().unleashAPI("https://localhost:1500").apiKey("irrelevant").appName("UnleashBenchmarks").toggleBootstrapProvider(new ToggleBootstrapFileProvider("classpath:unleash-repo-v2-with-impression-data.json"))
                .fetchTogglesInterval(0).disablePolling().disableMetrics().build());
            context = new UnleashContext.Builder().environment("benchmarking").build();
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            System.out.println("Do TearDown");
        }

    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(UnleashClientBenchmark.class.getSimpleName()).forks(1).build();
        new Runner(opt).run();
    }

    @Benchmark
    public void isEnabled(MyState myState, Blackhole bh) {
        bh.consume(myState.unleash.isEnabled("Test.impressionDataPresent"));
    }

    @Benchmark
    public void isEnabledWithContext(MyState myState, Blackhole bh) {
        bh.consume(myState.unleash.isEnabled("Test.impressionDataPresent", myState.context));
    }

    @Benchmark
    public void getDefaultVariant(MyState myState, Blackhole bh) {
        bh.consume(myState.unleash.getVariant("Test.impressionDataPresent"));
    }

    @Benchmark
    public void getVariant(MyState myState, Blackhole bh) {
        bh.consume(myState.unleash.getVariant("Test.variants"));
    }

}
