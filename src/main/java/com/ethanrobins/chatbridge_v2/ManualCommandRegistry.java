package com.ethanrobins.chatbridge_v2;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ManualCommandRegistry {
    private final List<LocaleCommand> commands = new ArrayList<>();
    private final Command.Type type;
    private final String name;
    private String description = "";

    /**
     * Constructor method for Message/User interaction commands. Slash commands possible too, without description.
     * @param type {@link Command.Type}.
     * @param name Command name.
     */
    public ManualCommandRegistry(@NotNull Command.Type type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Constructor method for {@link Command.Type#SLASH} only.
     * @param name Command name.
     * @param description Command description.
     */
    public ManualCommandRegistry(@NotNull String name, @NotNull String description) {
        this.type = Command.Type.SLASH;
        this.name = name;
        this.description = description;
    }

    public ManualCommandRegistry add(@NotNull DiscordLocale locale, @NotNull String name) throws IllegalArgumentException {
        return this.add(new LocaleCommand(locale, name));
    }

    public ManualCommandRegistry add(@NotNull LocaleCommand command) throws IllegalArgumentException {
        for (LocaleCommand c : commands) {
            if (c.getLocale() == command.getLocale()) {
                throw new IllegalArgumentException("Command with locale " + command.getLocale().getLocale() + " already exists!");
            }
        }

        if (this.type == Command.Type.SLASH && command.description.isEmpty()) {
            throw new IllegalArgumentException("Command description must be provided for slash commands!");
        }

        commands.add(command);
        return this;
    }

    @Nullable
    public LocaleCommand getCommand(@NotNull DiscordLocale locale) {
        for (LocaleCommand c : commands) {
            if (c.getLocale() == locale) {
                return c;
            }
        }
        return null;
    }

    public ManualCommandRegistry setDescription(@NotNull String description) {
        this.description = description;
        return this;
    }

    public CommandData build() throws IllegalStateException{
        if (this.type == Command.Type.SLASH) {
            SlashCommandData cmd = Commands.slash(this.name, this.description);

            if (this.description.isEmpty()) {
                throw new IllegalStateException("Command description must be provided for slash commands!");
            }

            for (LocaleCommand c : this.commands) {
                cmd.setNameLocalization(c.getLocale(), c.getName());
                cmd.setDescriptionLocalization(c.getLocale(), c.getDescription());
            }

            return cmd;
        } else if (this.type != Command.Type.UNKNOWN) {
            CommandData cmd = Commands.context(this.type, this.name);

            for (LocaleCommand c : this.commands) {
                cmd.setNameLocalization(c.getLocale(), c.getName());
            }

            return cmd;
        } else {
            throw new IllegalStateException("Command type must be set!");
        }
    }

    @Getter
    public static class LocaleCommand {
        private final DiscordLocale locale;
        private final String name;
        private final String description;

        /**
         * Constructor for if the command is for {@link Command.Type#USER} or {@link Command.Type#MESSAGE} only.
         * @param locale The {@link DiscordLocale} the translation is for.
         * @param name The name for this translation.
         */
        public LocaleCommand(@NotNull DiscordLocale locale, @NotNull String name) {
            this.locale = locale;
            this.name = name;
            this.description = "";
        }

        /**
         * Constructor for if the command is for {@link Command.Type#SLASH} only.
         * @param locale The {@link DiscordLocale} this translation is for.
         * @param name The name for this translation.
         * @param description The description for this translation.
         */
        public LocaleCommand(@NotNull DiscordLocale locale, @NotNull String name, @NotNull String description) {
            this.locale = locale;
            this.name = name;
            this.description = description;
        }

    }
}
