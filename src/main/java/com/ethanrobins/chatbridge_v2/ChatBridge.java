package com.ethanrobins.chatbridge_v2;

import com.ethanrobins.chatbridge_v2.drivers.MySQL;
import com.ethanrobins.chatbridge_v2.events.MessageInteraction;
import com.ethanrobins.chatbridge_v2.events.MessageReceived;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * The main class for the ChatBridge application, responsible for integrating a Discord bot with additional functionality.
 * <p>
 * This class manages the Discord bot lifecycle, command configuration, secret loading, debugging settings,
 * and utility methods for creating and managing payloads for slash commands and context menus.
 * It also provides utilities for database interactions and development mode configurations.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 *     <li>Manages the Discord bot connection using the JDA (Java Discord API) library.</li>
 *     <li>Loads sensitive configuration from an INI file.</li>
 *     <li>Provides utility methods for command creation and localization support.</li>
 *     <li>Supports debugging and developer mode for testing and local usage.</li>
 *     <li>Handles generation of IDs for commands and payloads.</li>
 *     <li>Provides data structures for handling slash command payloads and batches.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <pre>
 * public static void main(String[] args) {
 *     ChatBridge.main(args);
 * }
 * </pre>
 *
 * @see JDA
 * @see CommandData
 * @see Model
 */
public class ChatBridge {
    private static JDA jda;
    private static Ini secret;
    private static String token;
    private static final List<Command> commands = new ArrayList<>();

    private static boolean debug = false;
    private static boolean dev = false;

    /**
     * The main method for launching the ChatBridge application.
     * @param args Command-line arguments for the application.
     */
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
                    .addEventListeners(new MessageInteraction(), new MessageReceived())
                    .build();

            jda.updateCommands().addCommands(
                    //localeSlashCommand("translate", "Translate a message or input to another language"),
                    //Commands.slash("translate", "Translate a message or input to another language"),
                    //localeContextCommand(Command.Type.MESSAGE, "Private Translation" + (dev ? " (dev)" : "")),
                    //LocaleCommandRegistry.localeContextCommand(Command.Type.MESSAGE, "Public Translation" + (dev ? " (dev)" : ""))
                    privateTranslationInit(),
                    publicTranslationInit()
            ).queue(commands::addAll);
        } else {
            throw new RuntimeException("Token not found!");
        }
    }

    /**
     * Tests the MySQL database connection.
     * @throws RuntimeException If a {@link SQLException} is encountered during the test.
     */
    private static void testDatabase() {
        try {
            MySQL mysql = new MySQL();
            System.out.println("MySQL Connection test successful.");
            mysql.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the secret configuration from the {@code secret.ini} file.
     * @throws RuntimeException If the {@code secret.ini} file is not found or an {@link IOException} occurs during loading.
     */
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

    /**
     * Retrieves the JDA instance.
     * @return The {@link JDA} instance or {@code null} if not yet initialized.
     */
    public static JDA getJDA() {
        return jda;
    }

    /**
     * Retrieves the secret configuration.
     * @return The {@link Ini} instance containing the secret configuration.
     */
    public static Ini getSecret() {
        return secret;
    }

    /**
     * Retrieves the list of registered commands.
     * @return A {@link List} of {@link Command} objects.
     */
    public static List<Command> getCommands() {
        return commands;
    }

    /**
     * Retrieves a command by its name.
     * @param name The name of the command to search for.
     * @return The {@link Command} object matching the given name, or {@code null} if not found.
     */
    public static Command getCommand (String name) {
        for (Command c : commands) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Checks whether the application is in debug mode.
     * @return {@code true} if debug mode is enabled, otherwise {@code false}.
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * Checks whether the application is in development mode.
     * @return {@code true} if development mode is enabled, otherwise {@code false}.
     */
    public static boolean isDev() {
        return dev;
    }

    private static CommandData privateTranslationInit() {
        return new ManualCommandRegistry(Command.Type.MESSAGE, "Private Translation" + (dev ? " (dev)" : ""))
                .add(DiscordLocale.BULGARIAN, "Частен превод")
                .add(DiscordLocale.CHINESE_CHINA, "私人翻译")
                .add(DiscordLocale.CHINESE_TAIWAN, "私人翻譯")
                .add(DiscordLocale.CROATIAN, "Privatni prijevod")
                .add(DiscordLocale.CZECH, "Soukromý překlad")
                .add(DiscordLocale.DANISH, "Privat oversættelse")
                .add(DiscordLocale.DUTCH, "Privévertaling")
                .add(DiscordLocale.FINNISH, "Yksityinen käännös")
                .add(DiscordLocale.FRENCH, "Traduction privée")
                .add(DiscordLocale.GERMAN, "Private Übersetzung")
                .add(DiscordLocale.GREEK, "Ιδιωτική μετάφραση")
                .add(DiscordLocale.HINDI, "निजी अनुवाद")
                .add(DiscordLocale.HUNGARIAN, "Privát fordítás")
                .add(DiscordLocale.INDONESIAN, "Terjemahan Pribadi")
                .add(DiscordLocale.ITALIAN, "Traduzione privata")
                .add(DiscordLocale.JAPANESE, "プライベート翻訳")
                .add(DiscordLocale.KOREAN, "개인 번역")
                .add(DiscordLocale.LITHUANIAN, "Privatus vertimas")
                .add(DiscordLocale.NORWEGIAN, "Privat oversettelse")
                .add(DiscordLocale.POLISH, "Prywatne tłumaczenie")
                .add(DiscordLocale.PORTUGUESE_BRAZILIAN, "Tradução privada")
                .add(DiscordLocale.ROMANIAN_ROMANIA, "Traducere privată")
                .add(DiscordLocale.RUSSIAN, "Частный перевод")
                .add(DiscordLocale.SPANISH, "Traducción privada")
                .add(DiscordLocale.SPANISH_LATAM, "Traducción privada")
                .add(DiscordLocale.SWEDISH, "Privat översättning")
                .add(DiscordLocale.THAI, "การแปลแบบส่วนตัว")
                .add(DiscordLocale.TURKISH, "Özel çeviri")
                .add(DiscordLocale.UKRAINIAN, "Приватний переклад")
                .add(DiscordLocale.VIETNAMESE, "Dịch riêng tư")
                .build();
    }

    private static CommandData publicTranslationInit() {
        return new ManualCommandRegistry(Command.Type.MESSAGE, "Public Translation")
                .add(DiscordLocale.BULGARIAN, "Публичен превод")
                .add(DiscordLocale.CHINESE_CHINA, "公开翻译")
                .add(DiscordLocale.CHINESE_TAIWAN, "公開翻譯")
                .add(DiscordLocale.CROATIAN, "Javni prijevod")
                .add(DiscordLocale.CZECH, "Veřejný překlad")
                .add(DiscordLocale.DANISH, "Offentlig oversættelse")
                .add(DiscordLocale.DUTCH, "Openbare vertaling")
                .add(DiscordLocale.FINNISH, "Julkinen käännös")
                .add(DiscordLocale.FRENCH, "Traduction publique")
                .add(DiscordLocale.GERMAN, "Öffentliche Übersetzung")
                .add(DiscordLocale.GREEK, "Δημόσια μετάφραση")
                .add(DiscordLocale.HINDI, "सार्वजनिक अनुवाद")
                .add(DiscordLocale.HUNGARIAN, "Nyilvános fordítás")
                .add(DiscordLocale.INDONESIAN, "Terjemahan Publik")
                .add(DiscordLocale.ITALIAN, "Traduzione pubblica")
                .add(DiscordLocale.JAPANESE, "パブリック翻訳")
                .add(DiscordLocale.KOREAN, "공개 번역")
                .add(DiscordLocale.LITHUANIAN, "Viešas vertimas")
                .add(DiscordLocale.NORWEGIAN, "Offentlig oversettelse")
                .add(DiscordLocale.POLISH, "Publiczne tłumaczenie")
                .add(DiscordLocale.PORTUGUESE_BRAZILIAN, "Tradução pública")
                .add(DiscordLocale.ROMANIAN_ROMANIA, "Traducere publică")
                .add(DiscordLocale.RUSSIAN, "Публичный перевод")
                .add(DiscordLocale.SPANISH, "Traducción pública")
                .add(DiscordLocale.SPANISH_LATAM, "Traducción pública")
                .add(DiscordLocale.SWEDISH, "Offentlig översättning")
                .add(DiscordLocale.THAI, "การแปลสาธารณะ")
                .add(DiscordLocale.TURKISH, "Herkese açık çeviri")
                .add(DiscordLocale.UKRAINIAN, "Публічний переклад")
                .add(DiscordLocale.VIETNAMESE, "Dịch công khai")
                .build();
    }
}
