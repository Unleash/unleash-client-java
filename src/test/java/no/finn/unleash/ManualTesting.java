package no.finn.unleash;

import java.net.URI;
import java.util.Random;

import no.finn.unleash.repository.*;
import no.finn.unleash.util.UnleashConfig;

public class ManualTesting {
    public static void main(String[] args) throws Exception {
        UnleashConfig unleashConfig = new UnleashConfig.Builder()
                .appName("java-test")
                .instanceId("instance x")
                .unleashAPI("http://localhost:4242")
                .fetchTogglesInterval(1)
                .sendMetricsInterval(10)
                .build();

        Unleash unleash = new DefaultUnleash(unleashConfig);

        for(int i=0;i<10;i++) {
            (new Thread(new UnleashThread(unleash, "thread-"+i, 100))).start();
        }
    }

    static class UnleashThread implements Runnable {

        final Unleash unleash;
        final String name;
        final int maxRounds;
        int currentRound = 0;

        UnleashThread(Unleash unleash, String name, int maxRounds) {
            this.unleash = unleash;
            this.name = name;
            this.maxRounds = maxRounds;
        }

        public void run() {
            while(currentRound < maxRounds) {
                currentRound++;
                long startTime = System.nanoTime();
                boolean enabled = unleash.isEnabled("featureX");
                long timeUsed = System.nanoTime() - startTime;

                System.out.println(name + "\t" +"featureX" +":"  + enabled + "\t " + timeUsed + "ns");

                try {
                    //Wait 1 to 10ms before next round
                    Thread.sleep(new Random().nextInt(10000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
