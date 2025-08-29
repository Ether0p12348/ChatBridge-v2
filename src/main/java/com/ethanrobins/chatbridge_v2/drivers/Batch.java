//package com.ethanrobins.chatbridge_v2.drivers;
//
//import com.ethanrobins.chatbridge_v2.utils.RandomString;
//import lombok.Getter;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicReference;
//
//public class Batch {
//    @Getter
//    private String id = "batch_" + RandomString.generate(8, RandomString.Content.NUMBERS);
//    private final List<Payload> payloads = new ArrayList<>();
//
//    public Batch (@Nullable String id, Payload... payload) {
//        this.id = id != null ? id : this.id;
//        payloads.addAll(Arrays.asList(payload));
//    }
//
//    public Batch (@Nullable String id, @NotNull List<Payload> payloads) {
//        this.id = id != null ? id : this.id;
//        this.payloads.addAll(payloads);
//    }
//
//    public Batch(){}
//
//    public Batch setId (String id) {
//        this.id = id;
//        return this;
//    }
//
//    public Batch add (Payload payload) {
//        this.payloads.add(payload);
//        return this;
//    }
//
//    public Batch remove (String id) {
//        payloads.removeIf(payload -> payload.getId().equals(id));
//        return this;
//    }
//
//    public List<Payload> getPayloads() {
//        return this.payloads;
//    }
//
//    public Payload get (String id) {
//        for (Payload p : this.payloads) {
//            if (p.getId().equals(id)) {
//                return p;
//            }
//        }
//
//        return null;
//    }
//
//    public Batch queue (long delay) {
//        AtomicReference<Float> percent = new AtomicReference<> (0.0f);
//        System.out.println("\u001B[34mBeginning Batch: \u001B[33m" + this.id + "\u001B[0m");
//
//        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//        Map<Payload, CompletableFuture<String>> futures = new HashMap<>();
//
//        StringJoiner sj = new StringJoiner(", ");
//
//        for (int i = 0; i < payloads.size(); i++) {
//            CompletableFuture<String> future = new CompletableFuture<>();
//            final int in = i;
//            scheduler.schedule(() -> {
//                payloads.get(in).translateAsync(true).whenComplete((result, ex) -> {
//                    if (ex != null) {
//                        future.completeExceptionally(ex);
//                        sj.add("\u001B[31m" + payloads.get(in).getId() + "\u001B[0m");
//                    } else {
//                        future.complete(result);
//                        sj.add("\u001B[32m" + payloads.get(in).getId() + "\u001B[0m");
//                    }
//                    percent.set(percent.get() + (100.0f / payloads.size()));
//                    BigDecimal roundedPercent = BigDecimal.valueOf(percent.get()).setScale(1, RoundingMode.CEILING);
//
//                    System.out.print("\r\u001B[34mBatch Progress: \u001B[35m- \u001B[33m" + roundedPercent.toPlainString() + "%      \u001B[0m");
//                });
//            }, i * delay, TimeUnit.SECONDS);
//
//            futures.put(payloads.get(i), future);
//        }
//
//        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();
//        scheduler.shutdown();
//
//        System.out.print("\r\u001B[34mBatch Progress \u001B[35m- \u001B[33m100.0%      \u001B[0m\n");
//        System.out.println("  \u001B[33m" + this.id + " \u001B[35m-\u001B[0m " + sj);
//        System.out.println("\u001B[34mBatch Completed: \u001B[33m" + this.id + "\u001B[0m");
//
//        return this;
//    }
//}
