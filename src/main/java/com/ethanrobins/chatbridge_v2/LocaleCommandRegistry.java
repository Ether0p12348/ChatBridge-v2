package com.ethanrobins.chatbridge_v2;

import com.ethanrobins.chatbridge_v2.drivers.Batch;
import com.ethanrobins.chatbridge_v2.drivers.Payload;
import com.ethanrobins.chatbridge_v2.drivers.TranslateType;
import com.ethanrobins.chatbridge_v2.utils.RandomString;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocaleCommandRegistry {
    /**
     * Creates a localized slash command with translations for its name and description.
     * @param name The base name of the slash command.
     * @param description The base description of the slash command.
     * @return The {@link SlashCommandData} object containing localized fields.
     */
    public static SlashCommandData localeSlashCommand (@NotNull String name, @NotNull String description) {
        SlashCommandData cmd = Commands.slash(name, description);
        List<DiscordLocale> locales = Arrays.stream(DiscordLocale.values())
                .filter(locale -> locale != DiscordLocale.UNKNOWN)
                .toList();

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
                    if (ChatBridge.isDebug()) {
                        System.out.println(loc.getLocale() + " has been added to " + name + " nameLocalizations");
                    } else {
                        namesj.add("\u001B[32m" + loc.getLocale() + "\u001B[0m");
                    }
                } else {
                    System.out.println(p.getId() + " was unable to be added to " + name + " nameLocalizations: Set to " + loc.getLocale());
                    if (!ChatBridge.isDebug()) {
                        namesj.add("\u001B[31m" + loc.getLocale() + "\u001B[0m");
                    }
                }
                System.out.println(checkLocales);
            }
            if (p.getDescription().getResult() != null) {
                if (ChatBridge.isDebug()) {
                    System.out.println(loc.getLocale() + " has been added to " + name + " descriptionLocalizations");
                } else {
                    descsj.add("\u001B[32m" + loc.getLocale() + "\u001B[0m");
                }
                //checkLocales.remove(loc);
                cmd.setDescriptionLocalization(loc, p.getDescription().getResult());
                //System.out.println(checkLocales);
            }
        }

        System.out.println("\u001B[33m" + name + "\u001B[0m SlashCommand nameLocalizations \u001B[35m-\u001B[0m " + namesj);
        System.out.println("\u001B[33m" + name + "\u001B[0m SlashCommand descriptionLocalizations \u001B[35m-\u001B[0m " + descsj);

        return cmd;
    }

    /**
     * Creates a localized context command with translations for its name.
     * @param type The type of the context command.
     * @param name The base name of the context command.
     * @return The {@link CommandData} object containing localized fields.
     */
    public static CommandData localeContextCommand (@NotNull Command.Type type, @NotNull String name) {
        CommandData cmd = Commands.context(type, name);
        List<DiscordLocale> locales = Arrays.stream(DiscordLocale.values())
                .filter(locale -> !locale.getLocale().equalsIgnoreCase("unknown"))
                .toList();

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
                if (ChatBridge.isDebug()) {
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
                if (ChatBridge.isDebug()) {
                    System.out.println(p.getId() + " was unable to be added to " + name + " nameLocalizations: Set to " + loc.getLocale());
                } else {
                    sj.add("\u001B[31m" + loc.getLocale() + "\u001B[0m");
                }
            }
        }

        System.out.println("\u001B[33m" + name + "\u001B[0m ContextCommand nameLocalizations \u001B[35m-\u001B[0m " + sj);

        return cmd;
    }

    /**
     * Represents a payload for a Slash Command, including a unique ID, a name, and a description.
     * The class handles localization for the name and description fields.
     *
     * <p>Each instance is identified by a unique `id` which is either provided or
     * auto-generated using a prefixed random 8-character string. The `name` and
     * `description` fields are represented as {@link Payload} objects to support localization.</p>
     *
     * <p>This class provides methods for setting and retrieving each of its fields, allowing for
     * flexibility in customization and use in applications requiring localized commands.</p>
     */
    public static class SlashCommandPayload {
        private String id = "slashpayload_" + RandomString.generate(8, RandomString.Content.NUMBERS);
        private Payload name;
        private Payload description;

        /**
         * Constructs a SlashCommandPayload with the provided ID, name, and description.
         * If the ID is null, a default ID is generated.
         *
         * @param id The optional ID for the payload.
         * @param name The payload for the name.
         * @param description The payload for the description.
         */
        public SlashCommandPayload(@Nullable String id, @NotNull Payload name, @NotNull Payload description) {
            this.id = id != null ? id : this.id;
            this.name = name;
            this.description = description;
        }

        /**
         * Default constructor for SlashCommandPayload.
         */
        public SlashCommandPayload(){}

        /**
         * Sets the ID for this payload.
         *
         * @param id The ID to set.
         * @return The current instance of {@link SlashCommandPayload}.
         */
        public SlashCommandPayload setId (@NotNull String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the name payload for this SlashCommandPayload.
         *
         * @param name The name payload to set.
         * @return The current instance of {@link SlashCommandPayload}.
         */
        public SlashCommandPayload setName (@NotNull Payload name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description payload for this SlashCommandPayload.
         *
         * @param description The description payload to set.
         * @return The current instance of {@link SlashCommandPayload}.
         */
        public SlashCommandPayload setDescription (@NotNull Payload description) {
            this.description = description;
            return this;
        }

        /**
         * Retrieves the ID of this payload.
         *
         * @return The ID of the payload.
         */
        public String getId() {
            return this.id;
        }

        /**
         * Retrieves the name payload of this SlashCommandPayload.
         *
         * @return The name payload.
         */
        public Payload getName() {
            return this.name;
        }

        /**
         * Retrieves the description payload of this SlashCommandPayload.
         *
         * @return The description payload.
         */
        public Payload getDescription() {
            return this.description;
        }
    }

    /**
     * Represents a batch of slash commands with a unique ID and a list of {@link SlashCommandPayload} objects.
     *
     * <p>Each batch is uniquely identified by an `id`, which can either be provided by the user or
     * auto-generated using a predefined pattern and a random string. The batch allows adding,
     * removing, and managing multiple {@link SlashCommandPayload} instances and provides
     * an option to queue the batch for asynchronous processing with a delay.</p>
     */
    @Getter
    public static class SlashCommandBatch {
        /**
         * Retrieves the ID of the batch.
         * <br>The ID of this batch.
         */
        private String id = "slashbatch_" + RandomString.generate(8, RandomString.Content.NUMBERS);
        /**
         * Retrieves the list of payloads in the batch.
         * <br>A {@link List} of {@link SlashCommandPayload} objects in the batch.
         */
        private final List<SlashCommandPayload> payloads = new ArrayList<>();

        /**
         * Constructs a SlashCommandBatch with the given ID and an optional list of payloads.
         * If the ID is null, a default one is generated.
         *
         * @param id     The optional ID for the batch.
         * @param payload The payloads to initialize the batch with.
         */
        public SlashCommandBatch (@Nullable String id, SlashCommandPayload... payload) {
            this.id = id != null ? id : this.id;
            payloads.addAll(Arrays.asList(payload));
        }

        /**
         * Default constructor for SlashCommandBatch.
         */
        public SlashCommandBatch(){}

        /**
         * Sets the ID for this batch.
         *
         * @param id The ID to set.
         * @return The current instance of {@link SlashCommandBatch}.
         */
        public SlashCommandBatch setId (String id) {
            this.id = id;
            return this;
        }

        /**
         * Adds a payload to the batch.
         *
         * @param payload The payload to add.
         * @return The current instance of {@link SlashCommandBatch}.
         */
        public SlashCommandBatch add (SlashCommandPayload payload) {
            this.payloads.add(payload);
            return this;
        }

        /**
         * Removes a payload from the batch by its ID.
         *
         * @param id The ID of the payload to remove.
         * @return The current instance of {@link SlashCommandBatch}.
         */
        public SlashCommandBatch remove (String id) {
            payloads.removeIf(payload -> payload.getId().equals(id));
            return this;
        }

        /**
         * Retrieves a specific payload from the batch by its ID.
         *
         * @param id The ID of the payload to retrieve.
         * @return The corresponding {@link SlashCommandPayload}, or {@code null} if not found.
         */
        public SlashCommandPayload get (String id) {
            for (SlashCommandPayload p : this.payloads) {
                if (p.getId().equals(id)) {
                    return p;
                }
            }

            return null;
        }

        /**
         * Queues the batch for processing with a specified delay between payload processing.
         * Translates the names and descriptions of each payload asynchronously.
         *
         * @param delay The delay (in seconds) between the processing of each payload.
         * @return The current instance of {@link SlashCommandBatch}.
         */
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
