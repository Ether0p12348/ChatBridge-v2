package com.ethanrobins.chatbridge_v2;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Batch {
    private String id = "batch_" + ChatBridge.genId(8);
    private final List<Payload> payloads = new ArrayList<>();

    public Batch (@Nullable String id, Payload... payload) {
        this.id = id != null ? id : this.id;
        payloads.addAll(Arrays.asList(payload));
    }

    public Batch(){}

    public Batch setId (String id) {
        this.id = id;
        return this;
    }

    public Batch add (Payload payload) {
        this.payloads.add(payload);
        return this;
    }

    public Batch remove (String id) {
        payloads.removeIf(payload -> payload.getId().equals(id));
        return this;
    }

    public String getId() {
        return this.id;
    }

    public List<Payload> getPayloads() {
        return this.payloads;
    }

    public Payload get (String id) {
        for (Payload p : this.payloads) {
            if (p.getId().equals(id)) {
                return p;
            }
        }

        return null;
    }

    public Batch queue (long delay) {
        AtomicReference<Float> percent = new AtomicReference<> (0.0f);
        System.out.println("\u001B[34mBeginning Batch: \u001B[33m" + this.id + "\u001B[0m");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Map<Payload, CompletableFuture<String>> futures = new HashMap<>();

        StringJoiner sj = new StringJoiner(", ");

        for (int i = 0; i < payloads.size(); i++) {
            CompletableFuture<String> future = new CompletableFuture<>();
            final int in = i;
            scheduler.schedule(() -> {
                payloads.get(in).translateAsync(true).whenComplete((result, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                        sj.add("\u001B[31m" + payloads.get(in).getId() + "\u001B[0m");
                    } else {
                        future.complete(result);
                        sj.add("\u001B[32m" + payloads.get(in).getId() + "\u001B[0m");
                    }
                    percent.set(percent.get() + (100.0f / payloads.size()));

                    System.out.print("\r\u001B[34mBatch Progress: \u001B[35m- \u001B[33m" + percent.get() + "%\u001B[0m");
                });
            }, i * delay, TimeUnit.SECONDS);

            futures.put(payloads.get(i), future);
        }

        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();
        scheduler.shutdown();

        System.out.print("\r\u001B[34mBatch Progress: \u001B[35m- \u001B[33m100.0%\u001B[0m");
        System.out.println("\n  \u001B[33m" + this.id + " \u001B[35m\u2190\u001B[0m " + sj);
        System.out.println("\u001B[34mBatch Completed: \u001B[33m" + this.id + "\u001B[0m");

        return this;
    }
}
