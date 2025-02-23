package com.ethanrobins.chatbridge_v2;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.ini4j.Ini;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ChatBridge {
    private static JDA jda;
    private static Ini secret;
    private static String token;

    public static void main(String[] args) {
        loadSecret();

        if (token != null) {
            JDABuilder jdaBuilder = JDABuilder.createDefault(token);

            jda = jdaBuilder
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES)
                    .build();
        } else {
            throw new RuntimeException("Token not found!");
        }
    }

    private static void loadSecret() {
        URL resource = ChatBridge.class.getClassLoader().getResource("secret.ini");
        try {
            if (resource == null) {
                throw new RuntimeException("Secret file not found!");
            }

            /*File configFile = new File(resource.getFile());
            secret = new Ini(configFile);
            token = secret.get("discord", "token");*/
            try (InputStream inputStream = resource.openStream()) {
                secret = new Ini(inputStream);
                token = secret.get("discord", "token");
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
}
