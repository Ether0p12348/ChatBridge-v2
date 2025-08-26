package com.ethanrobins.chatbridge_v2.events;

import com.ethanrobins.chatbridge_v2.ChatBridge;
import com.ethanrobins.chatbridge_v2.drivers.MySQL;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GuildEvents extends ListenerAdapter {
    private final List<String> readyGuilds = new ArrayList<>();

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent e) {
        super.onGuildJoin(e);

        CompletableFuture.runAsync(() -> {
            MySQL mysql = null;
            try {
                mysql = new MySQL();
                MySQL.Status status = mysql.setGuildStatus(e.getGuild().getId(), true);

                if (ChatBridge.isDebug()) {
                    System.out.println("onGuildJoin: " + Arrays.toString(status.get()));
                }

                if (status.isUpdated() || status.isInserted()) {
                    System.out.println("ChatBridge has been " + (status.isInserted() ? "invited" : "reinvited") + " to " + e.getGuild().getName() + " [" + e.getGuild().getId() + "]");
                } else {
                    System.out.println("ChatBridge has been invited to " + e.getGuild().getName() + " [" + e.getGuild().getId() + "], but the status failed to update");
                }
            } catch (SQLException | RuntimeException ex) {
                ex.printStackTrace();
            }

            if (mysql != null && !mysql.getStatus().isClosed()) {
                mysql.close();
            }
        });
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent e) {
        super.onGuildLeave(e);

        CompletableFuture.runAsync(() -> {
            MySQL mysql = null;
            try {
                mysql = new MySQL();
                MySQL.Status status = mysql.setGuildStatus(e.getGuild().getId(), false);

                if (ChatBridge.isDebug()) {
                    System.out.println("onGuildLeave: " + Arrays.toString(status.get()));
                }

                if (status.isUpdated() || status.isInserted()) {
                    System.out.println("ChatBridge has been removed from " + e.getGuild().getName() + " [" + e.getGuild().getId() + "]");
                } else {
                    System.out.println("ChatBridge has been removed from " + e.getGuild().getName() + " [" + e.getGuild().getId() + "], but status failed to update");
                }
            } catch (SQLException | RuntimeException ex) {
                ex.printStackTrace();
            }

            if (mysql != null && !mysql.getStatus().isClosed()) {
                mysql.close();
            }
        });
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent e) {
        super.onGuildReady(e);

        this.readyGuilds.add(e.getGuild().getId());
        System.out.println("ChatBridge is ready for use in " + e.getGuild().getName() + " [" + e.getGuild().getId() + "]");
    }

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        super.onReady(e);

        if (ChatBridge.isDev()) return;

        CompletableFuture.runAsync(() -> {
            final Set<String> finReadyGuilds;
            synchronized (this.readyGuilds) {
                finReadyGuilds = new HashSet<>(this.readyGuilds);
                this.readyGuilds.clear();
            }

            MySQL mysql = null;
            try {
                mysql = new MySQL();
                final Map<String, Boolean> registeredGuilds = mysql.getGuilds();

                for (String guildId : registeredGuilds.keySet()) {
                    boolean isReady = finReadyGuilds.contains(guildId);
                    mysql.setGuildStatus(guildId, isReady);
                }

                for (String guildId : finReadyGuilds) {
                    if (!registeredGuilds.containsKey(guildId)) {
                        mysql.setGuildStatus(guildId, true);
                    }
                }
            } catch (SQLException | RuntimeException ex) {
                ex.printStackTrace();
            }

            if (mysql != null && !mysql.getStatus().isClosed()) {
                mysql.close();
            }
        });
    }
}
