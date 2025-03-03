package com.ethanrobins.chatbridge_v2;

import com.ethanrobins.chatbridge_v2.events.MessageInteraction;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ChatBridge {
    private static JDA jda;
    private static Ini secret;
    private static String token;

    private static boolean debug = false;
    private static boolean dev = false;

    public static void main(String[] args) {
        if (args.length > 0) {
            if (Arrays.stream(args).anyMatch("--debug"::equalsIgnoreCase)) {
                debug = true;
            }
            if (Arrays.stream(args).anyMatch("--dev"::equalsIgnoreCase)) {
                dev = true;
            }
        }
        loadSecret();

        if (!dev) {
            testDatabase();
        } else {
            System.out.println("Running in dev mode! Database usage is disabled!");
        }

        if (token != null) {
            JDABuilder jdaBuilder = JDABuilder.createDefault(token);

            jda = jdaBuilder
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES)
                    .addEventListeners(new MessageInteraction())
                    .build();

            jda.updateCommands().addCommands(
                    //localeSlashCommand("translate", "Translate a message or input to another language"),
                    //Commands.slash("translate", "Translate a message or input to another language"),
                    localeContextCommand(Command.Type.MESSAGE, "Secret Translation" + (dev ? " (dev)" : "")),
                    localeContextCommand(Command.Type.MESSAGE, "Public Translation" + (dev ? " (dev)" : ""))
            ).queue();
        } else {
            throw new RuntimeException("Token not found!");
        }
    }

    private static void testDatabase() {
        try {
            MySQL mysql = new MySQL();
            System.out.println("MySQL Connection test successful.");
            mysql.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadSecret() {
        URL resource = ChatBridge.class.getClassLoader().getResource("secret.ini");
        try {
            if (resource == null) {
                throw new RuntimeException("Secret file not found!");
            }

            try (InputStream inputStream = resource.openStream()) {
                secret = new Ini(inputStream);
                String tokenKey = dev ? "devToken" : "token";
                token = secret.get("discord", tokenKey);
            }
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    public static JDA getJDA() {
        return jda;
    }

    public static Ini getSecret() {
        return secret;
    }

    public static boolean isDebug() {
        return debug;
    }

    public static boolean isDev() {
        return dev;
    }

    private static SlashCommandData localeSlashCommand (@NotNull String name, @NotNull String description) {
        SlashCommandData cmd = Commands.slash(name, description);
        List<DiscordLocale> locales = Arrays.stream(DiscordLocale.values())
                .filter(locale -> locale != DiscordLocale.UNKNOWN)
                .collect(Collectors.toList());

        List<SlashCommandPayload> payloads = new ArrayList<>();
        for (DiscordLocale l : locales) {
            Payload namePayload = new Payload(l.getLocale(), null, TranslateType.PLAIN.getSystemPrompt(), "(" + l.getLocale() + ")" + name, null);
            Payload descPayload = new Payload(l.getLocale(), null, TranslateType.PLAIN.getSystemPrompt(), "(" + l.getLocale() + ")" + description, null);

            SlashCommandPayload payload = new SlashCommandPayload(null, namePayload, descPayload);
            payloads.add(payload);
        }

        SlashCommandBatch batch = new SlashCommandBatch().setId("slash_translate");
        for (SlashCommandPayload p : payloads) {
            batch.add(p);
        }
        batch.queue(0);

        StringJoiner namesj = new StringJoiner(", ");
        StringJoiner descsj = new StringJoiner(", ");
        List<DiscordLocale> checkLocales = new ArrayList<>(locales);
        int i = 0;
        for (SlashCommandPayload p : batch.getPayloads()) {
            DiscordLocale loc = DiscordLocale.ENGLISH_US;
            for (DiscordLocale l : locales) {
                if (l.getLocale().equalsIgnoreCase(p.getId())) {
                    loc = l;
                }
            }

            if (p.getName().getResult() != null) {
                checkLocales.remove(loc);
                if (loc != DiscordLocale.UNKNOWN && !p.getId().equalsIgnoreCase("unknown")) {
                    try {
                        cmd.setNameLocalization(loc, p.getName().getResult());
                    } catch (IllegalArgumentException err) {
                        cmd.setNameLocalization(loc, batch.get("en-US").getName().getResult());
                    }
                    if (debug) {
                        System.out.println(loc.getLocale() + " has been added to " + name + " nameLocalizations");
                    } else {
                        namesj.add("\u001B[32m" + loc.getLocale() + "\u001B[0m");
                    }
                } else {
                    System.out.println(p.getId() + " was unable to be added to " + name + " nameLocalizations: Set to " + loc.getLocale());
                    if (!debug) {
                        namesj.add("\u001B[31m" + loc.getLocale() + "\u001B[0m");
                    }
                }
                System.out.println(checkLocales);
            }
            if (p.getDescription().getResult() != null) {
                if (debug) {
                    System.out.println(loc.getLocale() + " has been added to " + name + " descriptionLocalizations");
                } else {
                    descsj.add("\u001B[32m" + loc.getLocale() + "\u001B[0m");
                }
                //checkLocales.remove(loc);
                cmd.setDescriptionLocalization(loc, p.getDescription().getResult());
                //System.out.println(checkLocales);
            }

            i++;
        }

        System.out.println("\u001B[33m" + name + "\u001B[0m SlashCommand nameLocalizations \u001B[35m-\u001B[0m " + namesj);
        System.out.println("\u001B[33m" + name + "\u001B[0m SlashCommand descriptionLocalizations \u001B[35m-\u001B[0m " + descsj);

        return cmd;
    }

    private static CommandData localeContextCommand (@NotNull Command.Type type, @NotNull String name) {
        CommandData cmd = Commands.context(type, name);
        List<DiscordLocale> locales = Arrays.stream(DiscordLocale.values())
                .filter(locale -> !locale.getLocale().equalsIgnoreCase("unknown"))
                .collect(Collectors.toList());

        List<Payload> payloads = new ArrayList<>();
        for (DiscordLocale l : locales) {
            Payload payload = new Payload(l.getLocale(), null, TranslateType.PLAIN.getSystemPrompt(), "(" + l.getLocale() + ")" + name, null);
            payloads.add(payload);
        }

        Batch batch = new Batch().setId("context_translate");
        for (Payload p : payloads) {
            batch.add(p);
        }
        batch.queue(0);

        StringJoiner sj = new StringJoiner(", ");

        List<DiscordLocale> checkLocales = new ArrayList<>(locales);
        for (Payload p : batch.getPayloads()) {
            DiscordLocale loc = DiscordLocale.UNKNOWN;
            for (DiscordLocale l : locales) {
                if (l.getLocale().equalsIgnoreCase(p.getId())) {
                    loc = l;
                }
            }

            if (p.getResult() != null) {
                if (debug) {
                    System.out.println(loc.getLocale() + " has been added to " + name + " nameLocalizations");
                } else {
                    sj.add("\u001B[32m" + loc.getLocale() + "\u001B[0m");
                }
                checkLocales.remove(loc);
                String result = p.getResult();
                if (p.getResult().length() > 32) {
                    result = p.getResult().substring(0, 32);
                    System.out.println(loc + " name localization was too long. set to " + result);
                }
                cmd.setNameLocalization(loc, result);
            } else {
                if (debug) {
                    System.out.println(p.getId() + " was unable to be added to " + name + " nameLocalizations: Set to " + loc.getLocale());
                } else {
                    sj.add("\u001B[31m" + loc.getLocale() + "\u001B[0m");
                }
            }
        }

        System.out.println("\u001B[33m" + name + "\u001B[0m ContextCommand nameLocalizations \u001B[35m-\u001B[0m " + sj);

        return cmd;
    }

    public static String genId (int length) {
        String availChars = "0123456789";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = rand.nextInt(availChars.length());
            sb.append(availChars.charAt(randomIndex));
        }
        return sb.toString();
    }

    public static class SlashCommandPayload {
        private String id = "slashpayload_" + ChatBridge.genId(8);
        private Payload name;
        private Payload description;

        public SlashCommandPayload(@Nullable String id, @NotNull Payload name, @NotNull Payload description) {
            this.id = id != null ? id : this.id;
            this.name = name;
            this.description = description;
        }

        public SlashCommandPayload(){}

        public SlashCommandPayload setId (@NotNull String id) {
            this.id = id;
            return this;
        }

        public SlashCommandPayload setName (@NotNull Payload name) {
            this.name = name;
            return this;
        }

        public SlashCommandPayload setDescription (@NotNull Payload description) {
            this.description = description;
            return this;
        }

        public String getId() {
            return this.id;
        }

        public Payload getName() {
            return this.name;
        }

        public Payload getDescription() {
            return this.description;
        }
    }

    public static class SlashCommandBatch {
        private String id = "slashbatch_" + ChatBridge.genId(8);
        private final List<SlashCommandPayload> payloads = new ArrayList<>();

        public SlashCommandBatch (@Nullable String id, SlashCommandPayload... payload) {
            this.id = id != null ? id : this.id;
            payloads.addAll(Arrays.asList(payload));
        }

        public SlashCommandBatch(){}

        public SlashCommandBatch setId (String id) {
            this.id = id;
            return this;
        }

        public SlashCommandBatch add (SlashCommandPayload payload) {
            this.payloads.add(payload);
            return this;
        }

        public SlashCommandBatch remove (String id) {
            payloads.removeIf(payload -> payload.getId().equals(id));
            return this;
        }

        public String getId() {
            return this.id;
        }

        public List<SlashCommandPayload> getPayloads() {
            return this.payloads;
        }

        public SlashCommandPayload get (String id) {
            for (SlashCommandPayload p : this.payloads) {
                if (p.getId().equals(id)) {
                    return p;
                }
            }

            return null;
        }

        public SlashCommandBatch queue(long delay) {
            System.out.println("Beginning batch: " + this.id);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            Map<Payload, CompletableFuture<String>> nameFutures = new HashMap<>();
            Map<Payload, CompletableFuture<String>> descFutures = new HashMap<>();

            for (int i = 0; i < payloads.size(); i++) {
                CompletableFuture<String> nameFuture = new CompletableFuture<>();
                CompletableFuture<String> descFuture = new CompletableFuture<>();
                final int in = i;
                scheduler.schedule(() -> {
                    payloads.get(in).getName().translateAsync().whenComplete((result, ex) -> {
                        if (ex != null) {
                            nameFuture.completeExceptionally(ex);
                        } else {
                            nameFuture.complete(result);
                        }
                    });
                    payloads.get(in).getDescription().translateAsync().whenComplete((result, ex) -> {
                        if (ex != null) {
                            descFuture.completeExceptionally(ex);
                        } else {
                            descFuture.complete(result);
                        }
                    });
                }, i * delay, TimeUnit.SECONDS);

                nameFutures.put(payloads.get(i).getName(), nameFuture);
                descFutures.put(payloads.get(i).getDescription(), descFuture);
            }

            CompletableFuture.allOf(nameFutures.values().toArray(new CompletableFuture[0])).join();
            CompletableFuture.allOf(descFutures.values().toArray(new CompletableFuture[0])).join();
            scheduler.shutdown();

            System.out.println("Batch completed: " + this.id);

            return this;
        }
    }
}
